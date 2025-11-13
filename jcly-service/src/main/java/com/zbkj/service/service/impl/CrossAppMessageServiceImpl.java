package com.zbkj.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.zbkj.service.service.CrossAppMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 跨应用消息通信服务实现
 * 使用Redis发布订阅机制实现商户端和小程序端之间的消息通信
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Service
public class CrossAppMessageServiceImpl implements CrossAppMessageService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // Redis频道常量
    private static final String MERCHANT_MESSAGE_CHANNEL = "websocket:merchant:message";
    private static final String FRONTEND_MESSAGE_CHANNEL = "websocket:frontend:message";
    private static final String SYSTEM_NOTIFICATION_CHANNEL = "websocket:system:notification";
    private static final String PLATFORM_MESSAGE_CHANNEL = "websocket:platform:message";
    private static final String MERCHANT_TO_MERCHANT_CHANNEL = "websocket:merchant:merchant_message";
    
    @Override
    public void sendToMerchant(String channel, Map<String, Object> message) {
        try {
            // 直接发送Map对象，让RedisTemplate的序列化器处理
            redisTemplate.convertAndSend(channel, message);
            log.info("【跨应用通信】消息已发送到商户端: channel={}, message={}", channel, message.get("type"));
        } catch (Exception e) {
            log.error("【跨应用通信】发送消息到商户端失败: channel={}, 错误: {}", channel, e.getMessage(), e);
        }
    }
    
    @Override
    public void sendToFrontend(String channel, Map<String, Object> message) {
        try {
            // 直接发送Map对象，让RedisTemplate的序列化器处理
            redisTemplate.convertAndSend(channel, message);
            log.info("【跨应用通信】消息已发送到小程序端: channel={}, message={}", channel, message.get("type"));
        } catch (Exception e) {
            log.error("【跨应用通信】发送消息到小程序端失败: channel={}, 错误: {}", channel, e.getMessage(), e);
        }
    }
    
    @Override
    public void sendUserMessageToMerchant(String sessionId, Integer userId, Integer staffId, Map<String, Object> message) {
        try {
            // 构建跨应用消息
            Map<String, Object> crossAppMessage = new HashMap<>();
            crossAppMessage.put("type", "user_message_to_merchant");
            crossAppMessage.put("sessionId", sessionId);
            crossAppMessage.put("userId", userId);
            crossAppMessage.put("staffId", staffId);
            crossAppMessage.put("messageData", message);
            crossAppMessage.put("timestamp", System.currentTimeMillis());
            crossAppMessage.put("source", "frontend");
            
            // 发送到商户端频道
            sendToMerchant(MERCHANT_MESSAGE_CHANNEL, crossAppMessage);
            
            log.info("【跨应用通信】用户消息已发送到商户端: sessionId={}, userId={}, staffId={}", 
                    sessionId, userId, staffId);
        } catch (Exception e) {
            log.error("【跨应用通信】发送用户消息到商户端失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }
    
    @Override
    public void sendStaffReplyToFrontend(String sessionId, Integer userId, Integer staffId, Map<String, Object> message) {
        try {
            // 构建跨应用消息
            Map<String, Object> crossAppMessage = new HashMap<>();
            crossAppMessage.put("type", "staff_reply_to_frontend");
            crossAppMessage.put("sessionId", sessionId);
            crossAppMessage.put("userId", userId);
            crossAppMessage.put("staffId", staffId);
            crossAppMessage.put("messageData", message);
            crossAppMessage.put("timestamp", System.currentTimeMillis());
            crossAppMessage.put("source", "merchant");
            
            // 发送到小程序端频道
            sendToFrontend(FRONTEND_MESSAGE_CHANNEL, crossAppMessage);
            
            log.info("【跨应用通信】客服回复已发送到小程序端: sessionId={}, userId={}, staffId={}", 
                    sessionId, userId, staffId);
        } catch (Exception e) {
            log.error("【跨应用通信】发送客服回复到小程序端失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }
    
    @Override
    public void sendAiReplyToRelevantEndpoints(String sessionId, Integer userId, Integer staffId, Map<String, Object> message) {
        try {
            // 构建AI回复消息
            Map<String, Object> aiMessage = new HashMap<>();
            aiMessage.put("type", "ai_reply");
            aiMessage.put("sessionId", sessionId);
            aiMessage.put("userId", userId);
            aiMessage.put("staffId", staffId);
            aiMessage.put("messageData", message);
            aiMessage.put("timestamp", System.currentTimeMillis());
            aiMessage.put("source", "ai");
            
            // 发送到小程序端（用户看到AI回复）
            if (userId != null) {
                Map<String, Object> frontendMessage = new HashMap<>(aiMessage);
                frontendMessage.put("type", "ai_reply_to_frontend");
                sendToFrontend(FRONTEND_MESSAGE_CHANNEL, frontendMessage);
            }
            
            // 发送到商户端（客服看到对话历史）
            if (staffId != null) {
                Map<String, Object> merchantMessage = new HashMap<>(aiMessage);
                merchantMessage.put("type", "ai_reply_to_merchant");
                merchantMessage.put("isHistoryMessage", true);
                sendToMerchant(MERCHANT_MESSAGE_CHANNEL, merchantMessage);
            }
            
            log.info("【跨应用通信】AI回复已发送到相关端点: sessionId={}, userId={}, staffId={}", 
                    sessionId, userId, staffId);
        } catch (Exception e) {
            log.error("【跨应用通信】发送AI回复失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }
    
    @Override
    public void sendPlatformMessageToMerchant(String sessionId, Integer staffId, Map<String, Object> message) {
        try {
            message.put("targetStaffId", staffId);
            sendToMerchant(PLATFORM_MESSAGE_CHANNEL, message);
            log.info("【跨应用通信】平台消息已发送到商户端: sessionId={}, staffId={}", sessionId, staffId);
        } catch (Exception e) {
            log.error("【跨应用通信】发送平台消息到商户端失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }
    
    @Override
    public void sendMerchantMessageToPlatform(String sessionId, Integer userId, Map<String, Object> message) {
        try {
            message.put("targetUserId", userId);
            sendToMerchant(PLATFORM_MESSAGE_CHANNEL, message);
            log.info("【跨应用通信】商户消息已发送到平台端: sessionId={}, userId={}", sessionId, userId);
        } catch (Exception e) {
            log.error("【跨应用通信】发送商户消息到平台端失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public void sendUserMessageToPlatformStaff(String sessionId, Integer userId, Integer staffId, Map<String, Object> message) {
        try {
            // 构建跨应用消息
            Map<String, Object> crossAppMessage = new HashMap<>();
            crossAppMessage.put("type", "user_message_to_platform");
            crossAppMessage.put("sessionId", sessionId);
            crossAppMessage.put("userId", userId);
            crossAppMessage.put("staffId", staffId);
            crossAppMessage.put("messageData", message);
            crossAppMessage.put("timestamp", System.currentTimeMillis());
            crossAppMessage.put("source", "frontend");

            // 发送到平台端频道（使用sendToMerchant，因为平台端和商户端都是admin端口）
            sendToMerchant(PLATFORM_MESSAGE_CHANNEL, crossAppMessage);

            log.info("【跨应用通信】用户消息已发送到平台端客服: sessionId={}, userId={}, staffId={}",
                    sessionId, userId, staffId);
        } catch (Exception e) {
            log.error("【跨应用通信】发送用户消息到平台端客服失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public void sendMerchantMessageToMerchant(String sessionId, Integer sourceStaffId, Integer targetStaffId, Map<String, Object> message) {
        try {
            // 构建商户到商户的跨应用消息
            Map<String, Object> crossAppMessage = new HashMap<>();
            crossAppMessage.put("type", "merchant_to_merchant_message");
            crossAppMessage.put("sessionId", sessionId);
            crossAppMessage.put("sourceStaffId", sourceStaffId);
            crossAppMessage.put("targetStaffId", targetStaffId);
            crossAppMessage.put("messageData", message);
            crossAppMessage.put("timestamp", System.currentTimeMillis());
            crossAppMessage.put("source", "merchant");

            // 发送到商户到商户专用频道
            sendToMerchant(MERCHANT_TO_MERCHANT_CHANNEL, crossAppMessage);

            log.info("【跨应用通信】商户消息已发送到其他商户端: sessionId={}, sourceStaffId={}, targetStaffId={}",
                    sessionId, sourceStaffId, targetStaffId);
        } catch (Exception e) {
            log.error("【跨应用通信】发送商户消息到其他商户端失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public void broadcastSystemNotification(String notification, String targetType) {
        try {
            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("type", "system_notification");
            systemMessage.put("notification", notification);
            systemMessage.put("targetType", targetType);
            systemMessage.put("timestamp", System.currentTimeMillis());
            
            switch (targetType.toUpperCase()) {
                case "MERCHANT":
                    sendToMerchant(SYSTEM_NOTIFICATION_CHANNEL, systemMessage);
                    break;
                case "FRONTEND":
                    sendToFrontend(SYSTEM_NOTIFICATION_CHANNEL, systemMessage);
                    break;
                case "ALL":
                default:
                    sendToMerchant(SYSTEM_NOTIFICATION_CHANNEL, systemMessage);
                    sendToFrontend(SYSTEM_NOTIFICATION_CHANNEL, systemMessage);
                    break;
            }
            
            log.info("【跨应用通信】系统通知已广播: targetType={}, notification={}", targetType, notification);
        } catch (Exception e) {
            log.error("【跨应用通信】广播系统通知失败: targetType={}, 错误: {}", targetType, e.getMessage(), e);
        }
    }
}
