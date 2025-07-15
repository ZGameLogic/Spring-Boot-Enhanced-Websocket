package com.zgamelogic.websocket.components;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Controller
@AllArgsConstructor
public class WebSocketController extends TextWebSocketHandler {
    private final WebSocketDispatcher websocketDispatcher;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        websocketDispatcher.connectionEstablished(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        websocketDispatcher.dispatch(session, message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        websocketDispatcher.connectionClosed(session);
    }
}
