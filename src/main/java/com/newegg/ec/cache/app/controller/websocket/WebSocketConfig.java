package com.newegg.ec.cache.app.controller.websocket;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Created by lzz on 2017/7/29.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig extends WebMvcConfigurerAdapter implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(createClusterLogHandler(), "/webSocket/createClusterLog")
                .setAllowedOrigins("*")
                .withSockJS()
                .setStreamBytesLimit(512 * 1024 * 1024)
                .setHttpMessageCacheSize(10 * 1000)
                .setDisconnectDelay(30 * 1000 * 60);

    }

    @Bean
    public WebSocketHandler createClusterLogHandler() {
        return new CreateClusterLogHandler();
    }
}
