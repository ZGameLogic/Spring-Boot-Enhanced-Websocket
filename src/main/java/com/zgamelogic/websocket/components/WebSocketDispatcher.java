package com.zgamelogic.websocket.components;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zgamelogic.websocket.annotations.*;
import com.zgamelogic.websocket.annotations.WebSocketController;
import com.zgamelogic.websocket.data.WebSocketAuthorization;
import com.zgamelogic.websocket.data.WebSocketMessage;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class WebSocketDispatcher {
    private final ApplicationContext applicationContext;
    private final WebSocketService webSocketService;
    private final WebSocketAuthorization webSocketAuthorization;

    private final Map<String, List<ControllerMethod>> controllerMappings;
    private final Map<Class<?>, List<ExceptionMethod>> exceptions;

    private final ObjectMapper objectMapper;

    public WebSocketDispatcher(
        ApplicationContext applicationContext,
        WebSocketService webSocketService,
        @Nullable WebSocketAuthorization webSocketAuthorization,
        ObjectMapper objectMapper
    ) {
        this.applicationContext = applicationContext;
        this.webSocketService = webSocketService;
        this.webSocketAuthorization = webSocketAuthorization;
        this.objectMapper = objectMapper;
        controllerMappings = new HashMap<>();
        exceptions = new HashMap<>();
    }

    @PostConstruct
    public void mapMethods(){
        for(Object bean: applicationContext.getBeansWithAnnotation(WebSocketController.class).values()){
            log.debug("Adding mapping for controller: {}", bean.getClass().getName());
            for(Method method: bean.getClass().getDeclaredMethods()){
                if(!method.isAnnotationPresent(WebSocketMapping.class)) continue;
                WebSocketMapping mapping = method.getAnnotation(WebSocketMapping.class);
                String key = mapping.type() + ":" + mapping.subtype();
                ControllerMethod methodHandle = new ControllerMethod(bean, method);
                log.debug("Adding mappings for method: {}", method.getName());
                log.debug("\tMapping ID: {}", key);
                controllerMappings.merge(key, new ArrayList<>(List.of(methodHandle)), (existingList, newList) -> {
                    existingList.addAll(newList);
                    return existingList;
                });
            }
        }
    }

    @PostConstruct
    public void mapExceptions(){
        for (Object bean : applicationContext.getBeansWithAnnotation(WebSocketController.class).values()) {
            for (Method method : bean.getClass().getDeclaredMethods()) {
                if(!method.isAnnotationPresent(WebSocketExceptionHandler.class)) continue;
                WebSocketExceptionHandler annotation = AnnotationUtils.findAnnotation(method, WebSocketExceptionHandler.class);
                log.debug("Adding exception mapping with method: {}", method.getName());
                ExceptionMethod methodHandle = new ExceptionMethod(bean, method, annotation);
                exceptions.merge(bean.getClass(), new ArrayList<>(List.of(methodHandle)), (existingList, newList) -> {
                    existingList.addAll(newList);
                    return existingList;
                });
            }
        }
    }

    @Transactional
    public void dispatch(WebSocketSession session, TextMessage message){
        WebSocketMessage webSocketMessage;
        try {
            webSocketMessage = objectMapper.readValue(message.getPayload(), WebSocketMessage.class);
        } catch (JsonProcessingException e){
            log.debug("Caught a non json message in our websocket. Probably not meant for me.");
            log.debug("{}", e.getMessage());
            return;
        }
        String eventKey = webSocketMessage.getType() + ":" + (webSocketMessage.getSubtype() != null ? webSocketMessage.getSubtype() : "");
        log.debug("Mapping ID: {}", eventKey);
        controllerMappings.getOrDefault(eventKey, new ArrayList<>()).forEach(controllerMethod -> {
            try {
                Method method = controllerMethod.method();
                Object[] params = resolveParamsForControllerMethod(method, session, webSocketMessage);
                method.setAccessible(true);
                Object returns = method.invoke(controllerMethod.controller(), params);
                if(returns == null) return;
                WebSocketMessage returnMessage;
                if(returns instanceof WebSocketMessage){
                    returnMessage = (WebSocketMessage) returns;
                } else {
                    returnMessage = new WebSocketMessage(webSocketMessage.getType(), webSocketMessage.getSubtype(), webSocketMessage.getReplyId(), returns);
                }
                String returnMessageJson;
                if(method.isAnnotationPresent(JsonView.class) && method.getAnnotation(JsonView.class).value() != null){
                    returnMessageJson = objectMapper.writerWithView(method.getAnnotation(JsonView.class).value()[0]).writeValueAsString(returnMessage);
                } else {
                    returnMessageJson = objectMapper.writeValueAsString(returnMessage);
                }
                webSocketService.sendMessage(session, new TextMessage(returnMessageJson));
            } catch (InvocationTargetException e){
                try {
                    throwControllerException(controllerMethod, session, webSocketMessage, e);
                } catch (InvocationTargetException | IllegalAccessException ex) {
                    log.error("Unable to dispatch websocket exception event.", e);
                }
            } catch (IllegalArgumentException | JsonProcessingException | IllegalAccessException e) {
                log.error("Unable to dispatch websocket event.", e);
            }
        });
    }

    private void throwControllerException(ControllerMethod controllerMethod, WebSocketSession session, WebSocketMessage webSocketMessage, InvocationTargetException e) throws InvocationTargetException, IllegalAccessException {
        for (ExceptionMethod exceptionMethod : exceptions.getOrDefault(controllerMethod.controller.getClass(), new ArrayList<>())) {
            List<Class<?>> classes = List.of(exceptionMethod.annotation.value());
            Class<?> current = e.getTargetException().getClass();
            while(current != null){
                if(classes.contains(current)){
                    Object[] params = resolveParamsForExceptionMethod(exceptionMethod.method, session, webSocketMessage, e.getTargetException());
                    exceptionMethod.method.setAccessible(true);
                    Object returns = exceptionMethod.method.invoke(controllerMethod.controller, params);
                    if(returns == null) return;
                    WebSocketMessage returnMessage;
                    if(returns instanceof WebSocketMessage){
                        returnMessage = (WebSocketMessage) returns;
                    } else {
                        returnMessage = new WebSocketMessage(webSocketMessage.getType(), webSocketMessage.getSubtype(), webSocketMessage.getReplyId(), 400, returns);
                    }
                    String returnMessageJson;
                    try {
                        if (exceptionMethod.method.isAnnotationPresent(JsonView.class) && exceptionMethod.method.getAnnotation(JsonView.class).value() != null) {
                            returnMessageJson = objectMapper.writerWithView(exceptionMethod.method.getAnnotation(JsonView.class).value()[0]).writeValueAsString(returnMessage);
                        } else {
                            returnMessageJson = objectMapper.writeValueAsString(returnMessage);
                        }
                        webSocketService.sendMessage(session, new TextMessage(returnMessageJson));
                    } catch (JsonProcessingException exception){
                        log.error("Unable to send error through websocket. {}", exception.getMessage());
                    }
                    return;
                }
                current = current.getSuperclass();
            }
        }
        throw new RuntimeException(e);
    }

    private Object[] resolveParamsForControllerMethod(Method method, WebSocketSession session, WebSocketMessage webSocketMessage) {
        return resolveParamsForArray(session, webSocketMessage, null, method.getParameters());
    }

    private Object[] resolveParamsForExceptionMethod(Method method, WebSocketSession session, WebSocketMessage webSocketMessage, Throwable throwable) {
        return resolveParamsForArray(session, webSocketMessage, throwable, method.getParameters());
    }

    private Object[] resolveParamsForArray(WebSocketSession session, WebSocketMessage message, Throwable throwable, Parameter...parameters){
        List<Object> params = new ArrayList<>();
        if (parameters == null) return params.toArray();
        for(Parameter parameter : parameters){
            if (message.getData() != null && parameter.isAnnotationPresent(WebSocketData.class)) {
                try {
                    Object converted = objectMapper.convertValue(message.getData(), parameter.getType());
                    params.add(converted);
                    continue;
                } catch (Exception e) {
                    log.debug("Cannot convert LinkedHashMap to {}", parameter.getType().getName());
                    params.add(null);
                    continue;
                }
            }
            if(parameter.isAnnotationPresent(WebSocketAttribute.class)){
                WebSocketAttribute attribute = parameter.getAnnotation(WebSocketAttribute.class);
                String key = attribute.value().isEmpty() ? parameter.getName() : attribute.value();
                params.add(session.getAttributes().get(key));
                continue;
            }
            if(parameter.getType().isAssignableFrom(session.getClass())){
                params.add(session);
                continue;
            }
            if(parameter.getType().isAssignableFrom(message.getClass())){
                params.add(message);
                continue;
            }
            if (throwable != null && parameter.getType().isAssignableFrom(throwable.getClass())) { // if it's the throwable
                params.add(throwable);
                continue;
            } else if(Throwable.class.isAssignableFrom(parameter.getType())){
                params.add(null);
                continue;
            }
            params.add(null);
        }
        return params.toArray();
    }

    public void connectionEstablished(WebSocketSession session){
        if(webSocketAuthorization != null){
            try {
                webSocketAuthorization.authenticate(session);
            } catch (Exception e) {
                try {
                    webSocketService.sendMessage(session, new TextMessage(e.getMessage()));
                    session.close(CloseStatus.NOT_ACCEPTABLE);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        webSocketService.addSession(session);
    }

    public void connectionClosed(WebSocketSession session){
        webSocketService.removeSession(session);
    }

    private record ControllerMethod(Object controller, Method method){}
    private record ExceptionMethod(Object controller, Method method, WebSocketExceptionHandler annotation){}
}
