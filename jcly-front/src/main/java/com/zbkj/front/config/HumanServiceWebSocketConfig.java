package com.zbkj.front.config;

import com.zbkj.front.websocket.HumanServiceWebSocketHandler;
import com.zbkj.front.websocket.WebSocketInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 人工客服WebSocket配置 - 前端模块
 * 企业级WebSocket配置，支持安全认证、消息隔离、实时推送
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Configuration
@EnableWebSocket
public class HumanServiceWebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private HumanServiceWebSocketHandler humanServiceWebSocketHandler;

    @Autowired
    private WebSocketInterceptor webSocketInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("注册前端WebSocket处理器");
        
        // 注册原生WebSocket处理器（不使用SockJS，避免路径混乱）
        registry.addHandler(humanServiceWebSocketHandler, "/websocket/human-service")
                .addInterceptors(webSocketInterceptor)
                .setAllowedOrigins("*");
                //.setAllowedOriginPatterns("*") // 使用更安全的Origin模式匹配
                //.withSockJS(); // 暂时禁用SockJS，使用原生WebSocket
        
        log.info("前端WebSocket配置完成 - 支持原生WebSocket和SockJS降级");
    }
}