package com.zbkj.admin.listener;

import com.alibaba.fastjson.JSON;
import com.zbkj.admin.websocket.HumanServiceWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis消息监听器 - 商户端
 * 监听来自小程序端的跨应用消息
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Component
public class RedisMessageListener implements MessageListener {

    @Autowired(required = false)
    private HumanServiceWebSocketHandler adminWebSocketHandler;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String messageBody = new String(message.getBody());

            log.info("【商户端Redis监听】收到跨应用消息: channel={}, message={}", channel, messageBody);

            // 优先处理客服消息路由（客服专用频道）
            if (channel.startsWith("customer-service:")) {
                String staffIdStr = channel.substring("customer-service:".length());
                try {
                    Integer staffId = Integer.valueOf(staffIdStr);
                    handleCustomerServiceMessage(staffId, messageBody);
                    return;
                } catch (NumberFormatException e) {
                    log.error("【商户端Redis监听】无效的客服ID: {}", staffIdStr);
                }
            }

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
                        log.info("【商户端Redis监听】解析GenericJackson2格式成功");
                    } else {
                        throw new RuntimeException("数组格式不正确");
                    }
                } else if (messageBody.startsWith("\"") && messageBody.endsWith("\"")) {
                    // 处理双重序列化的JSON字符串
                    String actualJson = messageBody.substring(1, messageBody.length() - 1);
                    actualJson = actualJson.replace("\\\"", "\"");
                    log.info("【商户端Redis监听】处理双重序列化JSON");
                    messageData = JSON.parseObject(actualJson, Map.class);
                } else {
                    // 直接解析为JSON对象
                    messageData = JSON.parseObject(messageBody, Map.class);
                }
            } catch (Exception e) {
                log.warn("【商户端Redis监听】JSON解析失败: {}", e.getMessage());
                log.error("【商户端Redis监听】原始消息: {}", messageBody);
                return;
            }

            String messageType = (String) messageData.get("type");

            // 根据消息类型处理
            switch (messageType) {
                case "user_message_to_merchant":
                case "user_message":  // 新增：支持新的消息类型
                    handleUserMessageToMerchant(messageData);
                    break;
                case "user_message_to_platform":
                    handleUserMessageToPlatform(messageData);
                    break;
                case "platform_message":
                    handlePlatformMessageToMerchant(messageData);
                    break;
                case "merchant_message":
                    handleMerchantMessageToPlatform(messageData);
                    break;
                case "merchant_to_merchant_message":
                    handleMerchantToMerchantMessage(messageData);
                    break;
                case "ai_reply_to_merchant":
                    handleAiReplyToMerchant(messageData);
                    break;
                case "system_notification":
                    handleSystemNotification(messageData);
                    break;
                default:
                    log.warn("【商户端Redis监听】未知的消息类型: {}", messageType);
            }

        } catch (Exception e) {
            log.error("【商户端Redis监听】处理跨应用消息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理用户消息到商户端
     */
    private void handleUserMessageToMerchant(Map<String, Object> messageData) {
        try {
            String sessionId = (String) messageData.get("sessionId");
            Integer userId = (Integer) messageData.get("userId");
            Integer staffId = (Integer) messageData.get("staffId");
            Map<String, Object> originalMessage = (Map<String, Object>) messageData.get("messageData");

            log.info("【商户端Redis监听】处理用户消息: sessionId={}, userId={}, staffId={}",
                    sessionId, userId, staffId);

            if (adminWebSocketHandler != null) {
                if (staffId != null && staffId > 0) {
                    // 使用新的sendMessageToStaff方法，按客服ID查找会话
                    log.info("【商户端Redis监听】发送给指定客服: staffId={}", staffId);
                    adminWebSocketHandler.sendMessageToStaff(staffId, originalMessage);
                } else {
                    // 广播给所有商户端用户
                    log.info("【商户端Redis监听】广播给所有商户端用户");
                    adminWebSocketHandler.broadcastToAllUsers(originalMessage);
                }

                log.info("【商户端Redis监听】用户消息已推送到WebSocket: sessionId={}", sessionId);
            } else {
                log.error("【商户端Redis监听】WebSocketHandler不可用，无法推送消息");
            }

        } catch (Exception e) {
            log.error("【商户端Redis监听】处理用户消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理用户消息到平台端客服
     */
    private void handleUserMessageToPlatform(Map<String, Object> messageData) {
        try {
            String sessionId = (String) messageData.get("sessionId");
            Object userIdObj = messageData.get("userId");
            Object staffIdObj = messageData.get("staffId");
            Map<String, Object> originalMessage = (Map<String, Object>) messageData.get("messageData");

            log.info("【平台��Redis监听】处理用户消息到平台客服: sessionId={}, userId={}, staffId={}, content={}",
                    sessionId, userIdObj, staffIdObj, originalMessage != null ? originalMessage.get("content") : null);

            if (adminWebSocketHandler != null) {
                // 构建WebSocket消息格式
                Map<String, Object> wsMessage = new HashMap<>();
                wsMessage.put("type", "user_message");
                wsMessage.put("sessionId", sessionId);
                wsMessage.put("messageId", originalMessage != null ? originalMessage.get("messageId") : null);
                wsMessage.put("senderId", originalMessage != null ? originalMessage.get("senderId") : userIdObj);
                wsMessage.put("senderName", originalMessage != null ? originalMessage.get("senderName") : "用户" + userIdObj);
                wsMessage.put("content", originalMessage != null ? originalMessage.get("content") : null);
                wsMessage.put("contentType", originalMessage != null ? originalMessage.get("contentType") : "text");
                wsMessage.put("messageType", originalMessage != null ? originalMessage.get("messageType") : "user");
                wsMessage.put("timestamp", originalMessage != null ? originalMessage.get("timestamp") : System.currentTimeMillis());
                wsMessage.put("isFromUser", true);

                if (staffIdObj != null) {
                    // 发送给指定平台客服
                    Integer staffId = null;
                    if (staffIdObj instanceof Integer) {
                        staffId = (Integer) staffIdObj;
                    } else if (staffIdObj instanceof String) {
                        staffId = Integer.valueOf((String) staffIdObj);
                    }

                    if (staffId != null) {
                        log.info("【平台端Redis监听】发送用户消息给指定平台客服: staffId={}", staffId);
                        adminWebSocketHandler.sendMessageToStaff(staffId, wsMessage);
                    } else {
                        log.warn("【平台端Redis监听】目标客服ID格式错误: {}", staffIdObj);
                        adminWebSocketHandler.broadcastToAllUsers(wsMessage);
                    }
                } else {
                    // 广播给所有平台端用户
                    log.info("【平台端Redis监听】广播用户消息给所有平台端用户");
                    adminWebSocketHandler.broadcastToAllUsers(wsMessage);
                }

                log.info("【平台端Redis监听】用户消息已推送到WebSocket: sessionId={}", sessionId);
            } else {
                log.error("【平台端Redis监听】WebSocketHandler不可用，无法推送用户消息");
            }

        } catch (Exception e) {
            log.error("【平台端Redis监听】处理用户消息到平台客服失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理平台消息到商户端
     */
    private void handlePlatformMessageToMerchant(Map<String, Object> messageData) {
        try {
            String sessionId = (String) messageData.get("sessionId");
            Object senderIdObj = messageData.get("senderId");
            Object targetStaffIdObj = messageData.get("targetStaffId");
            String senderName = (String) messageData.get("senderName");
            String content = (String) messageData.get("content");
            
            log.info("【商户端Redis监听】处理平台消息: sessionId={}, senderId={}, targetStaffId={}, content={}", 
                    sessionId, senderIdObj, targetStaffIdObj, content);
            
            if (adminWebSocketHandler != null) {
                // 构建WebSocket消息格式
                Map<String, Object> wsMessage = new HashMap<>();
                wsMessage.put("type", "platform_message");
                wsMessage.put("sessionId", sessionId);
                wsMessage.put("messageId", messageData.get("messageId"));
                wsMessage.put("senderId", senderIdObj);
                wsMessage.put("senderName", senderName != null ? senderName : "平台管理员");
                wsMessage.put("senderAvatar", messageData.get("senderAvatar"));
                wsMessage.put("content", content);
                wsMessage.put("contentType", messageData.get("contentType"));
                wsMessage.put("messageType", messageData.get("messageType"));
                wsMessage.put("timestamp", messageData.get("timestamp"));
                wsMessage.put("isFromPlatform", true);
                
                if (targetStaffIdObj != null) {
                    // 发送给指定客服
                    Integer targetStaffId = null;
                    if (targetStaffIdObj instanceof Integer) {
                        targetStaffId = (Integer) targetStaffIdObj;
                    } else if (targetStaffIdObj instanceof String) {
                        targetStaffId = Integer.valueOf((String) targetStaffIdObj);
                    }
                    
                    if (targetStaffId != null) {
                        log.info("【商户端Redis监听】发送平台消息给指定客服: staffId={}", targetStaffId);
                        adminWebSocketHandler.sendMessageToStaff(targetStaffId, wsMessage);
                    } else {
                        log.warn("【商户端Redis监听】目标客服ID格式错误: {}", targetStaffIdObj);
                        adminWebSocketHandler.broadcastToAllUsers(wsMessage);
                    }
                } else {
                    // 广播给所有商户端用户
                    log.info("【商户端Redis监听】广播平台消息给所有商户端用户");
                    adminWebSocketHandler.broadcastToAllUsers(wsMessage);
                }
                
                log.info("【商户端Redis监听】平台消息已推送到WebSocket: sessionId={}", sessionId);
            } else {
                log.error("【商户端Redis监听】WebSocketHandler不可用，无法推送平台消息");
            }
            
        } catch (Exception e) {
            log.error("【商户端Redis监听】处理平台消息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理商户消息到平台端
     */
    private void handleMerchantMessageToPlatform(Map<String, Object> messageData) {
        try {
            String sessionId = (String) messageData.get("sessionId");
            Object senderIdObj = messageData.get("senderId");
            Object targetUserIdObj = messageData.get("targetUserId");
            String senderName = (String) messageData.get("senderName");
            String content = (String) messageData.get("content");

            log.info("【平台端Redis监听】处理商户消息: sessionId={}, senderId={}, targetUserId={}, content={}",
                    sessionId, senderIdObj, targetUserIdObj, content);

            if (adminWebSocketHandler != null) {
                // 构建WebSocket消息格式
                Map<String, Object> wsMessage = new HashMap<>();
                wsMessage.put("type", "merchant_message");
                wsMessage.put("sessionId", sessionId);
                wsMessage.put("messageId", messageData.get("messageId"));
                wsMessage.put("senderId", senderIdObj);
                wsMessage.put("senderName", senderName != null ? senderName : "商户客服");
                wsMessage.put("senderAvatar", messageData.get("senderAvatar"));
                wsMessage.put("content", content);
                wsMessage.put("contentType", messageData.get("contentType"));
                wsMessage.put("messageType", messageData.get("messageType"));
                wsMessage.put("timestamp", messageData.get("timestamp"));
                wsMessage.put("isFromMerchant", true);

                if (targetUserIdObj != null) {
                    // 发送给指定平台用户
                    Integer targetUserId = null;
                    if (targetUserIdObj instanceof Integer) {
                        targetUserId = (Integer) targetUserIdObj;
                    } else if (targetUserIdObj instanceof String) {
                        targetUserId = Integer.valueOf((String) targetUserIdObj);
                    }

                    if (targetUserId != null) {
                        log.info("【平台端Redis监听】发送商户消息给指定平台用户: userId={}", targetUserId);
                        adminWebSocketHandler.sendMessageToUser(targetUserId, wsMessage);
                    } else {
                        log.warn("【平台端Redis监听】目标用户ID格式错误: {}", targetUserIdObj);
                        adminWebSocketHandler.broadcastToAllUsers(wsMessage);
                    }
                } else {
                    // 广播给所有平台端用户
                    log.info("【平台端Redis监听】广播商户消息给所有平台端用户");
                    adminWebSocketHandler.broadcastToAllUsers(wsMessage);
                }

                log.info("【平台端Redis监听】商户消息已推送到WebSocket: sessionId={}", sessionId);
            } else {
                log.error("【平台端Redis监听】WebSocketHandler不可用，无法推送商户消息");
            }

        } catch (Exception e) {
            log.error("【平台端Redis监听】处理商户消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理商户到商户的消息
     */
    private void handleMerchantToMerchantMessage(Map<String, Object> messageData) {
        try {
            String sessionId = (String) messageData.get("sessionId");
            Object sourceStaffIdObj = messageData.get("sourceStaffId");
            Object targetStaffIdObj = messageData.get("targetStaffId");
            Map<String, Object> originalMessage = (Map<String, Object>) messageData.get("messageData");

            log.info("【商户端Redis监听】处理商户到商户消息: sessionId={}, sourceStaffId={}, targetStaffId={}",
                    sessionId, sourceStaffIdObj, targetStaffIdObj);

            if (adminWebSocketHandler != null) {
                // 构建WebSocket消息格式
                Map<String, Object> wsMessage = new HashMap<>();
                wsMessage.put("type", "merchant_to_merchant_message");
                wsMessage.put("sessionId", sessionId);
                wsMessage.put("messageId", originalMessage.get("messageId"));
                wsMessage.put("senderId", originalMessage.get("senderId"));
                wsMessage.put("senderName", originalMessage.get("senderName"));
                wsMessage.put("senderAvatar", originalMessage.get("senderAvatar"));
                wsMessage.put("content", originalMessage.get("content"));
                wsMessage.put("contentType", originalMessage.get("contentType"));
                wsMessage.put("messageType", originalMessage.get("messageType"));
                wsMessage.put("timestamp", originalMessage.get("timestamp"));
                wsMessage.put("isFromMerchant", true);

                // 发送给目标商户客服
                Integer targetStaffId = null;
                if (targetStaffIdObj instanceof Integer) {
                    targetStaffId = (Integer) targetStaffIdObj;
                } else if (targetStaffIdObj instanceof String) {
                    targetStaffId = Integer.valueOf((String) targetStaffIdObj);
                }

                if (targetStaffId != null && targetStaffId > 0) {
                    log.info("【商户端Redis监听】发送商户消息给目标客服: targetStaffId={}", targetStaffId);
                    adminWebSocketHandler.sendMessageToStaff(targetStaffId, wsMessage);
                    log.info("【商户端Redis监听】商户到商户消息已推送到WebSocket: sessionId={}, targetStaffId={}",
                            sessionId, targetStaffId);
                } else {
                    log.warn("【商户端Redis监听】目标客服ID无效，跳过商户到商户消息推送: targetStaffId={}", targetStaffIdObj);
                }
            } else {
                log.error("【商户端Redis监听】WebSocketHandler不可用，无法推送商户到商户消息");
            }

        } catch (Exception e) {
            log.error("【商户端Redis监听】处理商户到商户消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理AI回复到商户端
     */
    private void handleAiReplyToMerchant(Map<String, Object> messageData) {
        try {
            String sessionId = (String) messageData.get("sessionId");
            Integer userId = (Integer) messageData.get("userId");
            Integer staffId = (Integer) messageData.get("staffId");
            Map<String, Object> originalMessage = (Map<String, Object>) messageData.get("messageData");
            
            log.info("【商户端Redis监听】处理AI回复: sessionId={}, userId={}, staffId={}", 
                    sessionId, userId, staffId);
            
            if (adminWebSocketHandler != null) {
                // AI回复广播到商户端，供客服查看对话历史
                adminWebSocketHandler.broadcastToAllUsers(originalMessage);
                log.info("【商户端Redis监听】AI回复已广播到商户端: sessionId={}", sessionId);
            } else {
                log.error("【商户端Redis监听】WebSocketHandler不可用，无法推送AI回复");
            }
            
        } catch (Exception e) {
            log.error("【商户端Redis监听】处理AI回复失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理系统通知
     */
    private void handleSystemNotification(Map<String, Object> messageData) {
        try {
            String notification = (String) messageData.get("notification");
            String targetType = (String) messageData.get("targetType");

            log.info("【商户端Redis监听】处理系统通知: targetType={}, notification={}",
                    targetType, notification);

            if (adminWebSocketHandler != null) {
                // 构建系统通知消息
                Map<String, Object> notificationMessage = new HashMap<>();
                notificationMessage.put("type", "system_notification");
                notificationMessage.put("notification", notification);
                notificationMessage.put("targetType", targetType);
                notificationMessage.put("timestamp", System.currentTimeMillis());

                adminWebSocketHandler.broadcastToAllUsers(notificationMessage);
                log.info("【商户端Redis监听】系统通知已广播到商户端: {}", notification);
            } else {
                log.error("【商户端Redis监听】WebSocketHandler不可用，无法推送系统通知");
            }

        } catch (Exception e) {
            log.error("【商户端Redis监听】处理系统通知失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理客服消息（通过客服专用频道）
     * @param staffId 客服ID
     * @param messageBody 消息内容
     */
    private void handleCustomerServiceMessage(Integer staffId, String messageBody) {
        try {
            log.info("【商户端Redis监听】处理客服消息: staffId={}, messageBody={}", staffId, messageBody);

            // 解析消息
            Map<String, Object> messageData;
            try {
                // 尝试解析GenericJackson2JsonRedisSerializer格式
                if (messageBody.startsWith("[")) {
                    com.alibaba.fastjson.JSONArray jsonArray = JSON.parseArray(messageBody);
                    if (jsonArray.size() >= 2) {
                        Object dataObject = jsonArray.get(1);
                        if (dataObject instanceof com.alibaba.fastjson.JSONObject) {
                            messageData = ((com.alibaba.fastjson.JSONObject) dataObject).getInnerMap();
                        } else {
                            messageData = (Map<String, Object>) dataObject;
                        }
                    } else {
                        messageData = JSON.parseObject(messageBody, Map.class);
                    }
                } else if (messageBody.startsWith("\"") && messageBody.endsWith("\"")) {
                    // 处理双重序列化的JSON字符串
                    String actualJson = messageBody.substring(1, messageBody.length() - 1);
                    actualJson = actualJson.replace("\\\"", "\"");
                    messageData = JSON.parseObject(actualJson, Map.class);
                } else {
                    // 直接解析为JSON对象
                    messageData = JSON.parseObject(messageBody, Map.class);
                }
            } catch (Exception e) {
                log.error("【商户端Redis监听】解析客服消息JSON失败: {}", e.getMessage());
                return;
            }

            // 通过WebSocket推送给指定客服
            if (adminWebSocketHandler != null) {
                adminWebSocketHandler.sendMessageToStaff(staffId, messageData);

                log.info("【商户端Redis监听】客服消息已推送: staffId={}, messageId={}, sessionId={}",
                        staffId, messageData.get("messageId"), messageData.get("sessionId"));
            } else {
                log.error("【商户端Redis监听】WebSocketHandler不可用，无法推送客服消息: staffId={}", staffId);
            }

        } catch (Exception e) {
            log.error("【商户端Redis监听】处理客服消息失败: staffId={}, error={}", staffId, e.getMessage(), e);
        }
    }
}
