package com.zgamelogic.websocket.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zgamelogic.websocket.data.WebSocketAuthorization;
import com.zgamelogic.websocket.exceptions.WebSocketMessageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;

@Service
@Slf4j
public class WebSocketService {
    private final HashMap<String, WebSocketSession> sessions;
    private final WebSocketAuthorization authorization;
    private final ObjectMapper objectMapper;

    public WebSocketService(@Nullable WebSocketAuthorization authorization, ObjectMapper objectMapper){
        this.authorization = authorization;
        this.objectMapper = objectMapper;
        sessions = new HashMap<>();
    }

    public void logout(WebSocketSession session){
        if(authorization == null) return;
        authorization.logout(session);
    }

    public void sendDataWithView(WebSocketSession session, Object data, Class<?> view){
        try {
            String returnMessageJson = objectMapper.writerWithView(view).writeValueAsString(data);
            sendMessage(session, new TextMessage(returnMessageJson));
        } catch (JsonProcessingException exception){
            log.error("Unable to send error through websocket. {}", exception.getMessage());
        }
    }

    public void sendData(WebSocketSession session, Object data){
        try {
            String returnMessageJson = objectMapper.writeValueAsString(data);
            sendMessage(session, new TextMessage(returnMessageJson));
        } catch (JsonProcessingException exception){
            log.error("Unable to send error through websocket. {}", exception.getMessage());
        }
    }

    public void sendDataToAttribute(String attribute, Object value, Object data){
        sessions.values()
            .stream()
            .filter(session ->
                session.getAttributes().containsKey(attribute) &&
                    session.getAttributes().get(attribute).equals(value)
            ).forEach(session -> sendData(session, data));
    }

    public void sendDataToAttributeWithView(String attribute, Object value, Object data, Class<?> view){
        sessions.values()
            .stream()
            .filter(session ->
                session.getAttributes().containsKey(attribute) &&
                    session.getAttributes().get(attribute).equals(value)
            ).forEach(session -> sendDataWithView(session, data, view));
    }

    public void sendDataToAll(Object data){
        sessions.values().forEach(webSocketSession -> sendData(webSocketSession, data));
    }

    public void sendDataToAllWithView(Object data, Class<?> view){
        sessions.values().forEach(webSocketSession -> sendDataWithView(webSocketSession, data, view));
    }

    public void sendMessage(WebSocketSession session, TextMessage message){
        try {
            session.sendMessage(message);
        } catch(Exception e){
            throw new WebSocketMessageException(e.getMessage());
        }
    }

    public void sendMessageToAll(TextMessage message){
        sessions.values().forEach(session -> sendMessage(session, message));
    }

    public void sendMessageToAttribute(String attribute, Object value, TextMessage message){
        sessions.values()
            .stream()
            .filter(session ->
                session.getAttributes().containsKey(attribute) &&
                session.getAttributes().get(attribute).equals(value)
            ).forEach(session -> sendMessage(session, message));
    }

    protected void addSession(WebSocketSession session){
        sessions.put(session.getId(), session);
    }

    protected void removeSession(WebSocketSession session){
        sessions.remove(session.getId());
    }
}
