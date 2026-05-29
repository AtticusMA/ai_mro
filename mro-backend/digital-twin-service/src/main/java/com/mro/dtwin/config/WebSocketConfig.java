package com.mro.dtwin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.mro.dtwin.websocket.DtwinWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final DtwinWebSocketHandler dtwinWebSocketHandler;

    public WebSocketConfig(DtwinWebSocketHandler dtwinWebSocketHandler) {
        this.dtwinWebSocketHandler = dtwinWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(dtwinWebSocketHandler, "/ws/dtwin")
                .setAllowedOriginPatterns("*");
    }
}
