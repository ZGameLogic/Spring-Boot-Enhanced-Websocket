package com.zgamelogic.websocket.components;

import com.zgamelogic.websocket.data.WebSocketAuthorization;
import com.zgamelogic.websocket.exceptions.WebSocketMessageException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;

@Service
public class WebSocketService {
    private final HashMap<String, WebSocketSession> sessions;
    private final WebSocketAuthorization authorization;

    public WebSocketService(@Nullable WebSocketAuthorization authorization){
        this.authorization = authorization;
        sessions = new HashMap<>();
    }

    public void logout(WebSocketSession session){
        if(authorization == null) return;
        authorization.logout(session);
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
