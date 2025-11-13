package com.zbkj.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.chat.UnifiedChatMessage;
import com.zbkj.common.model.service.HumanServiceMessage;
import com.zbkj.common.model.service.CustomerServiceStaff;
import com.zbkj.common.model.admin.SystemAdmin;
import com.zbkj.common.model.user.User;
import com.zbkj.common.token.FrontTokenComponent;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.common.utils.RedisUtil;
import com.zbkj.common.vo.LoginUserVo;
import com.zbkj.common.vo.LoginFrontUserVo;
import com.zbkj.service.dao.HumanServiceMessageDao;
import com.zbkj.service.dao.CustomerServiceStaffDao;
import com.zbkj.service.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 人工客服WebSocket服务实现
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Service
public class HumanServiceWebSocketServiceImpl implements HumanServiceWebSocketService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private HumanServiceMessageDao humanServiceMessageDao;

    @Autowired
    private CustomerServiceStaffDao customerServiceStaffDao;

        @Autowired
    private SystemAdminService systemAdminService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UnifiedChatService unifiedChatService;
    
    @Autowired
    private UnifiedMessageRoutingService messageRoutingService;
    
    @Autowired
    private FrontTokenComponent frontTokenComponent;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    // WebSocket处理器的静态引用，用于推送消息
    private static final String WEBSOCKET_HANDLER_BEAN = "humanServiceWebSocketHandler";

    // Redis key前缀
    private static final String ONLINE_STATUS_KEY = "human_service:online_status:";
    private static final String HEARTBEAT_KEY = "human_service:heartbeat:";
    private static final String OFFLINE_MESSAGE_KEY = "human_service:offline_message:";

    @Override
    public Map<String, Object> validateTokenAndGetUser(String token, String userType) {
        if (!StringUtils.hasText(token)) {
            throw new CrmebException("Token不能为空");
        }

        Map<String, Object> userInfo = new HashMap<>();
        
        try {
            switch (userType) {
                case "MERCHANT":
                case "PLATFORM":
                    // 验证管理员token - 使用TokenComponent（不是FrontTokenComponent）
                    LoginUserVo loginUser = null;
                    
                    // 构造完整的token key用于Redis查询
                    String tokenKey = null;
                    if (token.startsWith("merchant")) {
                        // 商户管理员token
                        String uuid = token.substring("merchant".length());
                        tokenKey = "TOKEN:ADMIN:MERCHANT:" + uuid;
                    } else if (token.startsWith("platform")) {
                        // 平台管理员token
                        String uuid = token.substring("platform".length());
                        tokenKey = "TOKEN:ADMIN:PLATFORM:" + uuid;
                    } else {
                        throw new CrmebException("无效的token格式");
                    }
                    
                    // 从Redis获取登录用户信息
                    loginUser = redisUtil.get(tokenKey);
                    if (loginUser == null || loginUser.getUser() == null) {
                        throw new CrmebException("Token已过期或无效");
                    }
                    
                    SystemAdmin admin = loginUser.getUser();
                    userInfo.put("userId", admin.getId().toString());
                    userInfo.put("userName", admin.getRealName() != null ? admin.getRealName() : admin.getAccount());
                    userInfo.put("userType", "PLATFORM".equals(userType) ? "PLATFORM" : "MERCHANT");
                    userInfo.put("avatar", admin.getHeaderImage() != null ? admin.getHeaderImage() : "");
                    userInfo.put("merId", admin.getMerId().toString());
                    break;
                    
                case "USER":
                    // 验证用户token - 通过FrontTokenComponent验证
                    if (!frontTokenComponent.checkToken(token)) {
                        throw new CrmebException("无效的用户token");
                    }
                    
                    // 从Redis获取用户ID
                    Integer userId = redisUtil.get(token);
                    if (userId == null) {
                        throw new CrmebException("无效的用户token");
                    }
                    
                    // 获取用户信息
                    User user = userService.getById(userId);
                    if (user == null) {
                        throw new CrmebException("用户不存在");
                    }
                    
                    userInfo.put("userId", user.getId().toString());
                    userInfo.put("userName", user.getNickname());
                    userInfo.put("userType", "USER");
                    userInfo.put("avatar", user.getAvatar());
                    break;
                    
                default:
                    throw new CrmebException("不支持的用户类型: " + userType + "，仅支持 MERCHANT（商户管理员）、USER（小程序用户）");
            }
            
            log.info("用户token验证成功，用户ID: {}, 类型: {}", userInfo.get("userId"), userType);
            return userInfo;
            
        } catch (Exception e) {
            log.error("验证用户token失败: {}", e.getMessage(), e);
            throw new CrmebException("token验证失败: " + e.getMessage());
        }
    }

    @Override
    public void updateUserOnlineStatus(Integer userId, String userType) {
        String key = ONLINE_STATUS_KEY + userType + ":" + userId;
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("userId", userId);
        statusData.put("userType", userType);
        statusData.put("status", "ONLINE");
        statusData.put("loginTime", System.currentTimeMillis());
        
        // 设置在线状态，30分钟过期
        redisTemplate.opsForValue().set(key, statusData, 30, TimeUnit.MINUTES);
        
        // 如果是客服，更新数据库状态
        if ("MERCHANT".equals(userType)) {
            try {
                customerServiceStaffDao.updateOnlineStatus(userId, CustomerServiceStaff.ONLINE_STATUS_ONLINE);
            } catch (Exception e) {
                log.error("更新客服在线状态失败: {}", e.getMessage(), e);
            }
        }
        
        log.info("更新用户在线状态，用户ID: {}, 类型: {}", userId, userType);
    }

    @Override
    public void updateUserOfflineStatus(Integer userId) {
        // 清除所有类型的在线状态
        String[] userTypes = {"MERCHANT", "USER"};
        for (String userType : userTypes) {
            String key = ONLINE_STATUS_KEY + userType + ":" + userId;
            redisTemplate.delete(key);
        }
        
        // 如果是客服，更新数据库状态
        try {
            customerServiceStaffDao.updateOnlineStatus(userId, 
                CustomerServiceStaff.ONLINE_STATUS_OFFLINE);
        } catch (Exception e) {
            // 可能不是客服，忽略错误
            log.debug("尝试更新客服离线状态失败，可能不是客服用户: {}", userId);
        }
        
        log.info("更新用户离线状态，用户ID: {}", userId);
    }

    @Override
    public void sendOfflineMessages(Integer userId, String userType) {
        String key = OFFLINE_MESSAGE_KEY + userType + ":" + userId;
        List<Object> offlineMessages = redisTemplate.opsForList().range(key, 0, -1);
        
        if (offlineMessages != null && !offlineMessages.isEmpty()) {
            log.info("发送离线消息，用户ID: {}, 消息数量: {}", userId, offlineMessages.size());
            
            try {
                // 通过WebSocket发送离线消息
                sendWebSocketMessage(userId, userType, createOfflineMessagesNotification(offlineMessages));
                
                // 清除已发送的离线消息
                redisTemplate.delete(key);
                
                log.info("离线消息发送成功，用户ID: {}, 消息数量: {}", userId, offlineMessages.size());
                
            } catch (Exception e) {
                log.error("发送离线消息失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
                // 发送失败不删除离线消息，等待下次重试
            }
        }
    }

    @Override
    public void updateHeartbeat(Integer userId) {
        String key = HEARTBEAT_KEY + userId;
        redisTemplate.opsForValue().set(key, System.currentTimeMillis(), 5, TimeUnit.MINUTES);
    }

    @Override
    public Map<String, Object> sendChatMessage(Integer senderId, String senderType, String sessionId,
                                              String content, Integer receiverId, String receiverType) {
        
        // 创建消息记录
        HumanServiceMessage message = new HumanServiceMessage();
        message.setMessageId(CrmebUtil.getUuid());
        message.setSessionId(sessionId);
        message.setSenderId(senderId.longValue());
        message.setSenderType(senderType);
        message.setReceiverId(receiverId.longValue());
        message.setReceiverType(receiverType);
        message.setMessageType(HumanServiceMessage.MESSAGE_TYPE_TEXT);
        message.setContent(content);
        message.setContentFormat(HumanServiceMessage.CONTENT_FORMAT_TEXT);
        message.setIsRead(false);
        message.setIsSystemMessage(false);
        message.setStatus(HumanServiceMessage.STATUS_SENT);
        message.setCreateTime(new Date());
        message.setUpdateTime(new Date());

        int result = humanServiceMessageDao.insert(message);
        if (result <= 0) {
            throw new CrmebException("保存聊天消息失败");
        }

        // 检查接收者是否在线
        boolean receiverOnline = isUserOnline(receiverId, receiverType);
        if (!receiverOnline) {
            // 存储离线消息
            storeOfflineMessage(receiverId, receiverType, message);
        }

        Map<String, Object> result_map = new HashMap<>();
        result_map.put("message", message);
        result_map.put("receiverOnline", receiverOnline);
        
        return result_map;
    }

    @Override
    public void markMessageAsRead(String messageId, Integer userId) {
        try {
            HumanServiceMessage message = humanServiceMessageDao.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HumanServiceMessage>()
                    .eq(HumanServiceMessage::getMessageId, messageId)
            );
            
            if (message != null && !message.getIsRead()) {
                message.setIsRead(true);
                message.setReadTime(new Date());
                message.setUpdateTime(new Date());
                humanServiceMessageDao.updateById(message);
                
                log.info("标记消息已读，消息ID: {}, 用户ID: {}", messageId, userId);
            }
        } catch (Exception e) {
            log.error("标记消息已读失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void updateStaffStatus(Integer staffId, String status) {
        try {
            customerServiceStaffDao.updateOnlineStatus(staffId, status);
            log.info("更新客服状态，客服ID: {}, 状态: {}", staffId, status);
        } catch (Exception e) {
            log.error("更新客服状态失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void pushNewSessionNotification(Integer staffId, String sessionId) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "NEW_SESSION");
        notification.put("staffId", staffId);
        notification.put("sessionId", sessionId);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("title", "新会话通知");
        notification.put("content", "您有一个新的客服会话，请及时处理");
        
        // 检查客服是否在线
        boolean staffOnline = isUserOnline(staffId, "MERCHANT");
        if (staffOnline) {
            try {
                // 通过WebSocket实时推送
                sendWebSocketMessage(staffId, "MERCHANT", notification);
                log.info("推送新会话通知成功，客服ID: {}, 会话ID: {}", staffId, sessionId);
            } catch (Exception e) {
                log.error("推送新会话通知失败，客服ID: {}, 会话ID: {}, 错误: {}", 
                         staffId, sessionId, e.getMessage(), e);
                // 推送失败时存储为离线消息
                storeOfflineMessage(staffId, "MERCHANT", notification);
            }
        } else {
            // 存储离线通知
            storeOfflineMessage(staffId, "MERCHANT", notification);
            log.info("客服离线，存储新会话通知为离线消息，客服ID: {}, 会话ID: {}", staffId, sessionId);
        }
    }

    @Override
    public void pushSystemNotification(Integer userId, String userType, String title, String content) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "SYSTEM_NOTIFICATION");
        notification.put("userId", userId);
        notification.put("userType", userType);
        notification.put("title", title);
        notification.put("content", content);
        notification.put("timestamp", System.currentTimeMillis());
        
        boolean userOnline = isUserOnline(userId, userType);
        if (userOnline) {
            try {
                // 通过WebSocket实时推送
                sendWebSocketMessage(userId, userType, notification);
                log.info("推送系统通知成功，用户ID: {}, 标题: {}", userId, title);
            } catch (Exception e) {
                log.error("推送系统通知失败，用户ID: {}, 标题: {}, 错误: {}", 
                         userId, title, e.getMessage(), e);
                // 推送失败时存储为离线消息
                storeOfflineMessage(userId, userType, notification);
            }
        } else {
            // 存储离线通知
            storeOfflineMessage(userId, userType, notification);
            log.info("用户离线，存储系统通知为离线消息，用户ID: {}, 标题: {}", userId, title);
        }
    }

    @Override
    public boolean isUserOnline(Integer userId, String userType) {
        String key = ONLINE_STATUS_KEY + userType + ":" + userId;
        return redisTemplate.hasKey(key);
    }

    /**
     * 处理不同的消息类型
     * @param userId
     * @param userType
     * @param message
     */
    @Override
    public void handleUserMessage(Integer userId, String userType, String message) {
        try {
            // 解析消息JSON
            Map<String, Object> messageData = JSON.parseObject(message, Map.class);
            String messageType = (String) messageData.get("type");
            
            log.debug("处理用户消息，用户ID: {}, 类型: {}, 消息类型: {}", userId, userType, messageType);
            
            switch (messageType) {
                case "heartbeat":
                    // 更新心跳
                    updateHeartbeat(userId);
                    break;
                    
                case "chat_message":
                    // 处理聊天消息
                    handleChatMessageFromUser(userId, userType, messageData);
                    break;
                    
                case "typing_status":
                    // 处理正在输入状态
                    handleTypingStatus(userId, userType, messageData);
                    break;
                    
                case "read_receipt":
                    // 处理消息已读回执
                    handleReadReceipt(userId, messageData);
                    break;
                    
                case "status_change":
                    // 处理状态变更（仅客服）
                    if ("MERCHANT".equals(userType)) {
                        handleStaffStatusChange(userId, messageData);
                    }
                    break;
                    
                default:
                    log.warn("未知的消息类型: {}, 用户ID: {}", messageType, userId);
            }
            
        } catch (Exception e) {
            log.error("处理用户消息失败，用户ID: {}, 消息: {}, 错误: {}", userId, message, e.getMessage(), e);
        }
    }

    /**
     * 存储离线消息
     */
    private void storeOfflineMessage(Integer userId, String userType, Object message) {
        String key = OFFLINE_MESSAGE_KEY + userType + ":" + userId;
        redisTemplate.opsForList().rightPush(key, message);
        // 设置离线消息7天过期
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
        
        log.info("存储离线消息，用户ID: {}, 类型: {}", userId, userType);
    }

    /**
     * 通过WebSocket发送消息的通用方法
     */
    private void sendWebSocketMessage(Integer userId, String userType, Object message) {
        try {
            if ("MERCHANT".equals(userType)||"PLATFORM".equals(userType)) {
                // 发送给管理端WebSocket Handler
                sendToAdminWebSocket(userId, message);
            } else {
                // 发送给用户端WebSocket Handler (USER, MERCHANT)
                sendToFrontWebSocket(userId, message);
            }
        } catch (Exception e) {
            log.error("发送WebSocket消息失败，用户ID: {}, 用户类型: {}, 错误: {}", 
                     userId, userType, e.getMessage(), e);
            throw new CrmebException("发送WebSocket消息失败: " + e.getMessage());
        }
    }

    /**
     * 发送消息到管理端WebSocket
     */
    private void sendToAdminWebSocket(Integer userId, Object message) {
        try {
            // 获取管理端WebSocket处理器
            Object handler = applicationContext.getBean(WEBSOCKET_HANDLER_BEAN);
            if (handler != null) {
                // 通过反射调用sendMessageToUser方法
                java.lang.reflect.Method method = handler.getClass().getMethod("sendMessageToUser", Integer.class, Map.class);
                method.invoke(handler, Integer.valueOf(userId), (Map<String, Object>) message);
            }
        } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            log.warn("发送消息到管理端WebSocket失败，用户ID: {}, 错误: {}", userId, e.getMessage());
        } catch (Exception e) {
            log.warn("发送消息到管理端WebSocket出现异常，用户ID: {}, 错误: {}", userId, e.getMessage());
        }
    }

    /**
     * 发送消息到用户端WebSocket
     */
    private void sendToFrontWebSocket(Integer userId, Object message) {
        try {
            // 使用静态方法调用前端WebSocket控制器
            java.lang.reflect.Method method = Class.forName("com.zbkj.front.controller.HumanServiceWebSocketController")
                .getMethod("sendMessageToUser", Integer.class, Object.class);
            method.invoke(null, userId, message);
            log.debug("成功发送消息到用户端WebSocket: userId={}", userId);
        } catch (ClassNotFoundException e) {
            // 前端WebSocket控制器不在当前classpath中，这是正常的（可能在不同的服务中）
            log.debug("前端WebSocket控制器不可用，跳过用户端发送: userId={}", userId);
        } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            log.warn("发送消息到用户端WebSocket失败，用户ID: {}, 错误: {}", userId, e.getMessage());
        } catch (Exception e) {
            log.warn("发送消息到用户端WebSocket出现异常，用户ID: {}, 错误: {}", userId, e.getMessage());
        }
    }

    /**
     * 创建离线消息通知
     */
    private Map<String, Object> createOfflineMessagesNotification(List<Object> offlineMessages) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "OFFLINE_MESSAGES");
        notification.put("messages", offlineMessages);
        notification.put("count", offlineMessages.size());
        notification.put("timestamp", System.currentTimeMillis());
        return notification;
    }

    /**
     * 处理来自用户的聊天消息
     */
    private void handleChatMessageFromUser(Integer userId, String userType, Map<String, Object> messageData) {
        try {
            String sessionId = (String) messageData.get("sessionId");
            String content = (String) messageData.get("content");
            Integer receiverId = (Integer) messageData.get("receiverId");
            String receiverType = (String) messageData.get("receiverType");
            
            if (sessionId != null && content != null && receiverId != null && receiverType != null) {
                // 处理聊天消息
                Map<String, Object> result = sendChatMessage(
                    Integer.valueOf(userId), userType, sessionId, content, receiverId, receiverType);
                
                // 通知接收方有新消息
                sendWebSocketMessage(receiverId, receiverType,
                    createNewMessageNotification(result));
            }
        } catch (Exception e) {
            log.error("处理聊天消息失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * 处理正在输入状态
     */
    private void handleTypingStatus(Integer userId, String userType, Map<String, Object> messageData) {
        try {
            String sessionId = (String) messageData.get("sessionId");
            Integer receiverId = (Integer) messageData.get("receiverId");
            String receiverType = (String) messageData.get("receiverType");
            Boolean isTyping = (Boolean) messageData.get("isTyping");
            
            if (receiverId != null && receiverType != null) {
                Map<String, Object> typingNotification = new HashMap<>();
                typingNotification.put("type", "TYPING_STATUS");
                typingNotification.put("sessionId", sessionId);
                typingNotification.put("senderId", userId);
                typingNotification.put("senderType", userType);
                typingNotification.put("isTyping", isTyping);
                typingNotification.put("timestamp", System.currentTimeMillis());
                
                sendWebSocketMessage(receiverId, receiverType, typingNotification);
            }
        } catch (Exception e) {
            log.error("处理正在输入状态失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * 处理消息已读回执
     */
    private void handleReadReceipt(Integer userId, Map<String, Object> messageData) {
        try {
            String messageId = (String) messageData.get("messageId");
            if (messageId != null) {
                markMessageAsRead(messageId, userId);
            }
        } catch (Exception e) {
            log.error("处理消息已读回执失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * 处理客服状态变更
     */
    private void handleStaffStatusChange(Integer userId, Map<String, Object> messageData) {
        try {
            String newStatus = (String) messageData.get("status");
            if (newStatus != null) {
                updateStaffStatus(userId, newStatus);
                
                // 广播状态变更通知
                Map<String, Object> statusNotification = new HashMap<>();
                statusNotification.put("type", "MERCHANT_STATUS_CHANGE");
                statusNotification.put("staffId", userId);
                statusNotification.put("status", newStatus);
                statusNotification.put("timestamp", System.currentTimeMillis());
                
                // 这里可以广播给所有相关用户
                log.info("客服状态变更，客服ID: {}, 新状态: {}", userId, newStatus);
            }
        } catch (Exception e) {
            log.error("处理客服状态变更失败，客服ID: {}, 错误: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * 创建新消息通知
     */
    private Map<String, Object> createNewMessageNotification(Map<String, Object> messageResult) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "NEW_MESSAGE");
        notification.put("data", messageResult);
        notification.put("timestamp", System.currentTimeMillis());
        return notification;
    }

    @Override
    public void sendMessageToUser(Integer userId, Map<String, Object> message) {
        try {
            log.debug("发送消息给用户: userId={}, 消息类型: {}", userId, message.get("type"));
            
            // 尝试发送到管理端WebSocket
            try {
                sendToAdminWebSocket(userId, message);
                log.debug("消息已发送到管理端WebSocket: userId={}", userId);
            } catch (Exception e) {
                log.warn("发送到管理端WebSocket失败: userId={}, 错误: {}", userId, e.getMessage());
            }
            
            // 尝试发送到用户端WebSocket
            try {
                sendToFrontWebSocket(userId, message);
                log.debug("消息已发送到用户端WebSocket: userId={}", userId);
            } catch (Exception e) {
                log.warn("发送到用户端WebSocket失败: userId={}, 错误: {}", userId, e.getMessage());
            }
            
        } catch (Exception e) {
            log.error("发送消息给用户失败: userId={}, 错误: {}", userId, e.getMessage(), e);
        }
    }
    
    @Override
    public String generateMessageId() {
        return unifiedChatService.generateMessageId();
    }
    
    @Override
    public UnifiedChatMessage saveMessage(UnifiedChatMessage message) {
        return unifiedChatService.saveMessage(message);
    }
    
    @Override
    public void notifyMerchantOfNewMessage(UnifiedChatMessage message) {
        try {
            // 使用消息路由服务通知商户端
            messageRoutingService.routeMessage(message,"USER");
            log.info("已通知商户端新消息: sessionId={}, messageId={}", message.getSessionId(), message.getMessageId());
        } catch (Exception e) {
            log.error("通知商户端新消息失败: sessionId={}, messageId={}, 错误: {}", 
                     message.getSessionId(), message.getMessageId(), e.getMessage(), e);
        }
    }
}
