package com.zgamelogic.websocket;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Controller
@AllArgsConstructor
public class WebsocketController extends TextWebSocketHandler {
    private final WebsocketDispatcher websocketDispatcher;
}
