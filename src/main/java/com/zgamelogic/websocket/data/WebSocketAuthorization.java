package com.zgamelogic.websocket.data;

import org.springframework.web.socket.WebSocketSession;

public interface WebSocketAuthorization {
    void authenticate(WebSocketSession session);
    void logout(WebSocketSession session);
    boolean authorizedSession(WebSocketSession session);
}
