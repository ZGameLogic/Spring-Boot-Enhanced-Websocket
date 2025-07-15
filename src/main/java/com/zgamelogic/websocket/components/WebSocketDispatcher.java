package com.zgamelogic.websocket.components;

import com.zgamelogic.websocket.annotations.WebSocketExceptionHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class WebSocketDispatcher {
    private final ApplicationContext applicationContext;
    private final WebSocketService webSocketService;

    private final Map<String, List<ControllerMethod>> controllerMappings;
    private final Map<String, List<ControllerMethod>> authMappings;
    private final Map<Class<?>, List<ExceptionMethod>> exceptions;

    public WebSocketDispatcher(ApplicationContext applicationContext, WebSocketService webSocketService) {
        this.applicationContext = applicationContext;
        this.webSocketService = webSocketService;
        controllerMappings = new HashMap<>();
        authMappings = new HashMap<>();
        exceptions = new HashMap<>();
    }

    @PostConstruct
    public void mapAuthentications(){
        // TODO map authentication classes/methods
    }

    @PostConstruct
    public void mapMethods(){
        // TODO map regular methods
    }

    @PostConstruct
    public void mapExceptions(){
        // TODO map exception methods
    }

    public void dispatch(WebSocketSession session, TextMessage message){
        // TODO dispatch method
    }

    public void connectionEstablished(WebSocketSession session){
        // TODO authenticate
        webSocketService.addSession(session);
    }

    public void connectionClosed(WebSocketSession session){
        webSocketService.removeSession(session);
    }

    private record ControllerMethod(Object controller, Method method){}
    private record ExceptionMethod(Object controller, Method method, WebSocketExceptionHandler annotation){}
}
