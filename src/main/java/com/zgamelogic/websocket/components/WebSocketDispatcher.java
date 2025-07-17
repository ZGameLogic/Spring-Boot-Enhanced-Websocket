package com.zgamelogic.websocket.components;

import com.zgamelogic.websocket.annotations.WebSocketController;
import com.zgamelogic.websocket.annotations.WebSocketExceptionHandler;
import com.zgamelogic.websocket.annotations.WebSocketMapping;
import com.zgamelogic.websocket.data.WebSocketAuthorization;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.lang.reflect.Method;
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

    public WebSocketDispatcher(ApplicationContext applicationContext, WebSocketService webSocketService, @Nullable WebSocketAuthorization webSocketAuthorization) {
        this.applicationContext = applicationContext;
        this.webSocketService = webSocketService;
        this.webSocketAuthorization = webSocketAuthorization;
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
                String key = mapping.Id() + (!mapping.SubId().isEmpty() ? ":" + mapping.SubId() : "");
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

    public void dispatch(WebSocketSession session, TextMessage message){
        // TODO find method
        // TODO map params
        // TODO call method with params
        // TODO handle exceptions
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
