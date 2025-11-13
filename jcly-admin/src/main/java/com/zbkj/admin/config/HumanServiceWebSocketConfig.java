package com.zbkj.admin.config;

import com.zbkj.admin.websocket.HumanServiceWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 人工客服WebSocket配置
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Configuration
@EnableWebSocket
public class HumanServiceWebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private HumanServiceWebSocketHandler humanServiceWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册人工客服WebSocket处理器 - 纯WebSocket版本
        registry.addHandler(humanServiceWebSocketHandler, "/websocket/human-service")
                .setAllowedOrigins("*"); // 允许跨域，生产环境建议配置具体域名
    }
}
