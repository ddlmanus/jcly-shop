package com.zbkj.front.listener;

import com.alibaba.fastjson.JSON;
import com.zbkj.front.websocket.HumanServiceWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis消息监听器 - 小程序端
 * 监听来自商户端的跨应用消息
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Component
public class RedisMessageListener implements MessageListener {

    @Autowired(required = false)
    private HumanServiceWebSocketHandler frontWebSocketHandler;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String messageBody = new String(message.getBody());
            
            log.info("【小程序端Redis监听】收到跨应用消息: channel={}, message={}", channel, messageBody);
            
            // 解析消息 - 处理GenericJackson2JsonRedisSerializer格式
            Map<String, Object> messageData;
            try {
                // GenericJackson2JsonRedisSerializer会产生带类型信息的JSON
                // 格式可能是：["java.util.HashMap",{"key":"value"}]
                if (messageBody.startsWith("[")) {
                    // 尝试解析为数组格式
                    com.alibaba.fastjson.JSONArray jsonArray = JSON.parseArray(messageBody);
                    if (jsonArray.size() >= 2) {
                        // 第二个元素是实际数据
                        Object dataObject = jsonArray.get(1);
                        if (dataObject instanceof com.alibaba.fastjson.JSONObject) {
                            messageData = ((com.alibaba.fastjson.JSONObject) dataObject).getInnerMap();
                        } else {
                            messageData = (Map<String, Object>) dataObject;
                        }
                        log.info("【小程序端Redis监听】解析GenericJackson2格式成功");
                    } else {
                        throw new RuntimeException("数组格式不正确");
                    }
                } else if (messageBody.startsWith("\"") && messageBody.endsWith("\"")) {
                    // 处理双重序列化的JSON字符串
                    String actualJson = messageBody.substring(1, messageBody.length() - 1);
                    actualJson = actualJson.replace("\\\"", "\"");
                    log.info("【小程序端Redis监听】处理双重序列化JSON");
                    messageData = JSON.parseObject(actualJson, Map.class);
                } else {
                    // 直接解析为JSON对象
                    messageData = JSON.parseObject(messageBody, Map.class);
                }
            } catch (Exception e) {
                log.warn("【小程序端Redis监听】JSON解析失败: {}", e.getMessage());
                log.error("【小程序端Redis监听】原始消息: {}", messageBody);
                return;
            }
            
            String messageType = (String) messageData.get("type");
            
            // 根据消息类型处理
            switch (messageType) {
                case "staff_reply_to_frontend":
                    handleStaffReplyToFrontend(messageData);
                    break;
                case "staff_reply":
                    // 处理商户端和平台端发送给用户的消息
                    handleStaffReplyToUser(messageData);
                    break;
                case "ai_reply_to_frontend":
                    handleAiReplyToFrontend(messageData);
                    break;
                case "ai_reply":
                    // 处理AI回复消息
                    handleAiReplyToUser(messageData);
                    break;
                case "system_notification":
                    handleSystemNotification(messageData);
                    break;
                default:
                    log.warn("【小程序端Redis监听】未知的消息类型: {}", messageType);
            }
            
        } catch (Exception e) {
            log.error("【小程序端Redis监听】处理跨应用消息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理客服回复到小程序端
     */
    private void handleStaffReplyToFrontend(Map<String, Object> messageData) {
        try {
            String sessionId = (String) messageData.get("sessionId");
            Integer userId = (Integer) messageData.get("userId");
            Integer staffId = (Integer) messageData.get("staffId");
            Map<String, Object> originalMessage = (Map<String, Object>) messageData.get("messageData");
            
            log.info("【小程序端Redis监听】处理客服回复: sessionId={}, userId={}, staffId={}", 
                    sessionId, userId, staffId);
            
            if (frontWebSocketHandler != null) {
                if (userId != null) {
                    // 发送给指定用户
                    log.info("【小程序端Redis监听】发送给指定用户: userId={}", userId);
                    frontWebSocketHandler.sendMessageToUser(userId, originalMessage);
                } else {
                    // 发送到指定会话
                    log.info("【小程序端Redis监听】发送到指定会话: sessionId={}", sessionId);
                    frontWebSocketHandler.sendAiReplyToSession(sessionId, originalMessage);
                }
                
                log.info("【小程序端Redis监听】客服回复已推送到WebSocket: sessionId={}", sessionId);
            } else {
                log.error("【小程序端Redis监听】WebSocketHandler不可用，无法推送消息");
            }
            
        } catch (Exception e) {
            log.error("【小程序端Redis监听】处理客服回复失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理商户端和平台端回复给用户的消息
     */
    private void handleStaffReplyToUser(Map<String, Object> messageData) {
        try {
            String sessionId = (String) messageData.get("sessionId");
            String messageId = (String) messageData.get("messageId");
            Object senderIdObj = messageData.get("senderId");
            String senderName = (String) messageData.get("senderName");
            String senderAvatar = (String) messageData.get("senderAvatar");
            String content = (String) messageData.get("content");
            String contentType = (String) messageData.get("contentType");
            String messageType = (String) messageData.get("messageType");
            Boolean isFromStaff = (Boolean) messageData.get("isFromStaff");
            
            log.info("【小程序端Redis监听】处理商户/平台回复: sessionId={}, senderId={}, content={}", 
                    sessionId, senderIdObj, content);
            
            if (frontWebSocketHandler != null) {
                // 构建WebSocket消息格式
                Map<String, Object> wsMessage = new HashMap<>();
                wsMessage.put("type", "staff_reply");
                wsMessage.put("sessionId", sessionId);
                wsMessage.put("messageId", messageId);
                wsMessage.put("senderId", senderIdObj);
                wsMessage.put("senderName", senderName != null ? senderName : "客服");
                wsMessage.put("senderAvatar", senderAvatar);
                wsMessage.put("content", content);
                wsMessage.put("contentType", contentType != null ? contentType : "text");
                wsMessage.put("messageType", messageType != null ? messageType : "text");
                wsMessage.put("timestamp", messageData.get("timestamp"));
                wsMessage.put("isFromStaff", isFromStaff != null ? isFromStaff : true);
                
                // 发送到指定会话
                if (sessionId != null) {
                    frontWebSocketHandler.sendAiReplyToSession(sessionId, wsMessage);
                    log.info("【小程序端Redis监听】商户/平台回复已推送到WebSocket: sessionId={}", sessionId);
                } else {
                    log.warn("【小程序端Redis监听】会话ID为空，无法推送消息");
                }
            } else {
                log.error("【小程序端Redis监听】WebSocketHandler不可用，无法推送消息");
            }
            
        } catch (Exception e) {
            log.error("【小程序端Redis监听】处理商户/平台回复失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理AI回复到小程序端
     */
    private void handleAiReplyToFrontend(Map<String, Object> messageData) {
        try {
            String sessionId = (String) messageData.get("sessionId");
            Integer userId = (Integer) messageData.get("userId");
            Map<String, Object> originalMessage = (Map<String, Object>) messageData.get("messageData");
            
            log.info("【小程序端Redis监听】处理AI回复: sessionId={}, userId={}", sessionId, userId);
            
            if (frontWebSocketHandler != null) {
                if (userId != null) {
                    // 发送给指定用户
                    frontWebSocketHandler.sendMessageToUser(userId, originalMessage);
                } else if (sessionId != null) {
                    // 发送到指定会话
                    frontWebSocketHandler.sendAiReplyToSession(sessionId, originalMessage);
                }
                
                log.info("【小程序端Redis监听】AI回复已推送到WebSocket: sessionId={}", sessionId);
            } else {
                log.error("【小程序端Redis监听】WebSocketHandler不可用，无法推送AI回复");
            }
            
        } catch (Exception e) {
            log.error("【小程序端Redis监听】处理AI回复失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理AI回复消息
     */
    private void handleAiReplyToUser(Map<String, Object> messageData) {
        try {
            String sessionId = (String) messageData.get("sessionId");
            String messageId = (String) messageData.get("messageId");
            String content = (String) messageData.get("content");
            String contentType = (String) messageData.get("contentType");
            String messageType = (String) messageData.get("messageType");
            
            log.info("【小程序端Redis监听】处理AI回复: sessionId={}, messageId={}, content={}", 
                    sessionId, messageId, content);
            
            if (frontWebSocketHandler != null) {
                // 构建WebSocket消息格式
                Map<String, Object> wsMessage = new HashMap<>();
                wsMessage.put("type", "ai_reply");
                wsMessage.put("sessionId", sessionId);
                wsMessage.put("messageId", messageId);
                wsMessage.put("content", content);
                wsMessage.put("contentType", contentType != null ? contentType : "text");
                wsMessage.put("messageType", messageType != null ? messageType : "text");
                wsMessage.put("timestamp", messageData.get("timestamp"));
                wsMessage.put("sender", "AI");
                
                // 发送到指定会话
                if (sessionId != null) {
                    frontWebSocketHandler.sendAiReplyToSession(sessionId, wsMessage);
                    log.info("【小程序端Redis监听】AI回复已推送到WebSocket: sessionId={}", sessionId);
                } else {
                    log.warn("【小程序端Redis监听】会话ID为空，无法推送AI回复");
                }
            } else {
                log.error("【小程序端Redis监听】WebSocketHandler不可用，无法推送AI回复");
            }
            
        } catch (Exception e) {
            log.error("【小程序端Redis监听】处理AI回复失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理系统通知
     */
    private void handleSystemNotification(Map<String, Object> messageData) {
        try {
            String notification = (String) messageData.get("notification");
            String targetType = (String) messageData.get("targetType");
            
            log.info("【小程序端Redis监听】处理系统通知: targetType={}, notification={}", 
                    targetType, notification);
            
            if (frontWebSocketHandler != null) {
                // 构建系统通知消息
                Map<String, Object> notificationMessage = new HashMap<>();
                notificationMessage.put("type", "system_notification");
                notificationMessage.put("notification", notification);
                notificationMessage.put("targetType", targetType);
                notificationMessage.put("timestamp", System.currentTimeMillis());
                
                frontWebSocketHandler.broadcastToAllUsers(notificationMessage);
                log.info("【小程序端Redis监听】系统通知已广播到小程序端: {}", notification);
            } else {
                log.error("【小程序端Redis监听】WebSocketHandler不可用，无法推送系统通知");
            }
            
        } catch (Exception e) {
            log.error("【小程序端Redis监听】处理系统通知失败: {}", e.getMessage(), e);
        }
    }
}
