package com.zgamelogic.websocket.components;

import com.zgamelogic.websocket.annotations.WebSocketExceptionHandler;
import com.zgamelogic.websocket.data.WebSocketAuthorization;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.lang.reflect.Method;
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
    private final Map<String, List<ControllerMethod>> authMappings;
    private final Map<Class<?>, List<ExceptionMethod>> exceptions;

    public WebSocketDispatcher(ApplicationContext applicationContext, WebSocketService webSocketService, @Nullable WebSocketAuthorization webSocketAuthorization) {
        this.applicationContext = applicationContext;
        this.webSocketService = webSocketService;
        this.webSocketAuthorization = webSocketAuthorization;
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
