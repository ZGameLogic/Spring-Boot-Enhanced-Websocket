package com.zgamelogic.websocket.components;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {
    private final WebSocketController websocketController;
    private final String endpoint;

    public WebSocketConfiguration(WebSocketController websocketController, @Value("${websocket.endpoint}") String endpoint) {
        this.websocketController = websocketController;
        this.endpoint = endpoint.replaceFirst("^/", "");
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(websocketController, "/" + endpoint).setAllowedOrigins("*");
    }
}
