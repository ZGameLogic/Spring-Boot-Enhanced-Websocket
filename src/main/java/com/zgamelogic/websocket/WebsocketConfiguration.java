package com.zgamelogic.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebsocketConfiguration implements WebSocketConfigurer {
    private final WebsocketController websocketController;
    private final String endpoint;
    public WebsocketConfiguration(WebsocketController websocketController, @Value("${websocket.endpoint}") String endpoint) {
        this.websocketController = websocketController;
        this.endpoint = endpoint.replaceFirst("^/", "");
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(websocketController, "/" + endpoint).setAllowedOrigins("*");
    }
}
