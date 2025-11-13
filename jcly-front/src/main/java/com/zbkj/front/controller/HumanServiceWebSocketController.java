package com.zbkj.front.controller;

import com.zbkj.common.model.chat.UnifiedChatMessage;
import com.zbkj.front.websocket.HumanServiceWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 人工客服WebSocket控制器 - 前端模块
 * 企业级WebSocket接口，提供：
 * - 静态方法接口供其他模块调用
 * - 消息路由和分发
 * - 会话管理和用户隔离
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Component
public class HumanServiceWebSocketController {

    @Autowired
    private HumanServiceWebSocketHandler humanServiceWebSocketHandler;
    
    // 静态引用，用于静态方法调用
    private static HumanServiceWebSocketHandler staticHandler;
    
    @PostConstruct
    public void init() {
        staticHandler = humanServiceWebSocketHandler;
        log.info("前端WebSocket控制器初始化完成，支持企业级消息路由");
    }

    /**
     * 静态方法：发送消息给指定用户
     * 供其他模块通过反射调用
     */
    public static void sendMessageToUser(Integer userId, Object message) {
        try {
            if (staticHandler != null && message instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> messageMap = (Map<String, Object>) message;
                staticHandler.sendMessageToUser(userId, messageMap);
                log.info("前端WebSocket控制器成功发送消息给用户: userId={}, 消息类型={}", 
                        userId, messageMap.get("type"));
            } else {
                log.warn("前端WebSocket处理器未初始化或消息格式错误");
            }
        } catch (Exception e) {
            log.error("前端WebSocket控制器发送消息失败: userId={}, 错误: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * 静态方法：发送AI回复到聊天会话
     * 供AI聊天系统调用
     */
    public static void sendAiReplyToSession(String chatSessionId, Object message) {
        try {
            if (staticHandler != null && message instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> messageMap = (Map<String, Object>) message;
                staticHandler.sendAiReplyToSession(chatSessionId, messageMap);
                log.info("前端WebSocket控制器成功发送AI回复到会话: sessionId={}, 内容={}", 
                        chatSessionId, messageMap.get("content"));
            } else {
                log.warn("前端WebSocket处理器未初始化或消息格式错误");
            }
        } catch (Exception e) {
            log.error("前端WebSocket控制器发送AI回复失败: sessionId={}, 错误: {}", chatSessionId, e.getMessage(), e);
        }
    }

    /**
     * 静态方法：广播统一聊天消息
     * 供统一聊天系统调用
     */
    public static void broadcastChatMessage(UnifiedChatMessage message) {
        try {
            if (staticHandler != null) {
                staticHandler.broadcastMessageToSession(message);
                log.info("前端WebSocket控制器成功广播聊天消息: sessionId={}, messageId={}", 
                        message.getSessionId(), message.getMessageId());
            } else {
                log.warn("前端WebSocket处理器未初始化");
            }
        } catch (Exception e) {
            log.error("前端WebSocket控制器广播消息失败: sessionId={}, 错误: {}", 
                    message.getSessionId(), e.getMessage(), e);
        }
    }

    /**
     * 实例方法：发送消息给指定用户
     */
    public void sendMessage(Integer userId, Map<String, Object> message) {
        if (humanServiceWebSocketHandler != null) {
            humanServiceWebSocketHandler.sendMessageToUser(userId, message);
        }
    }

    /**
     * 实例方法：发送AI回复到会话
     */
    public void sendAiReply(String chatSessionId, Map<String, Object> message) {
        if (humanServiceWebSocketHandler != null) {
            humanServiceWebSocketHandler.sendAiReplyToSession(chatSessionId, message);
        }
    }

    /**
     * 获取在线用户数量
     */
    public static int getOnlineUserCount() {
        if (staticHandler != null) {
            return staticHandler.getOnlineUserCount();
        }
        return 0;
    }

    /**
     * 获取在线用户列表
     */
    public static Map<String, String> getOnlineUsers() {
        if (staticHandler != null) {
            return staticHandler.getOnlineUsers();
        }
        return new HashMap<>();
    }
}
