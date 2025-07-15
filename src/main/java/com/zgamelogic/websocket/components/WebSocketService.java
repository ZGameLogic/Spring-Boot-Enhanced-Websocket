package com.zgamelogic.websocket.components;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;

@Service
public class WebSocketService {
    private final HashMap<String, WebSocketSession> sessions;

    public WebSocketService(){
        sessions = new HashMap<>();
    }

    public void logout(){

    }

    public void sendMessage(){

    }

    public void sendMessageToAll(){}

    public void sendMessageToAttribute(String attribute){}

    protected void addSession(WebSocketSession session){
        sessions.put(session.getId(), session);
    }

    protected void removeSession(WebSocketSession session){
        sessions.remove(session.getId());
    }
}
