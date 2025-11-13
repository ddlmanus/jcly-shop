package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zbkj.common.event.MessageRoutingEvent;
import com.zbkj.common.model.chat.UnifiedChatMessage;
import com.zbkj.common.model.chat.UnifiedChatSession;
import com.zbkj.common.model.service.CustomerServiceStaff;
import com.zbkj.service.dao.chat.UnifiedChatSessionDao;
import com.zbkj.service.service.UnifiedMessageRoutingService;
import com.zbkj.service.service.UnifiedChatService;
import com.zbkj.service.service.CrossAppMessageService;
import com.zbkj.service.service.CustomerServiceStaffService;
import com.zbkj.service.service.SystemAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一消息路由服务实现 - 基于事件驱动
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Service
public class UnifiedMessageRoutingServiceImpl implements UnifiedMessageRoutingService {

    // 使用Spring事件发布器来解决跨模块通信问题
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // 注入统一聊天服务，用于获取会话信息
    @Autowired
    private UnifiedChatService unifiedChatService;

    // 注入跨应用消息服务，用于跨端口通信
    @Autowired
    private CrossAppMessageService crossAppMessageService;

    // 注入Redis模板，用于发布客服消息
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 注入客服服务，用于客服分配和管理
    @Autowired
    private CustomerServiceStaffService customerServiceStaffService;

    // 注入系统管理员服务，用于获取管理员信息
    @Autowired
    private SystemAdminService systemAdminService;

    // 注入会话DAO，用于直接操作会话数据
    @Autowired
    private UnifiedChatSessionDao unifiedChatSessionDao;

    /**
     * 安全转换 Long 为 Integer，避免 Redis 序列化时产生 GenericJackson2JsonRedisSerializer 类型信息
     * @param value Long 值
     * @return Integer 值或 null
     */
    private Integer safeConvertToInteger(Long value) {
        return value != null ? value.intValue() : null;
    }

    @Override
    public void routeMessage(UnifiedChatMessage message, String sourceEndpoint) {
        try {
            log.info("路由消息: sessionId={}, messageType={}, source={}", 
                    message.getSessionId(), message.getMessageType(), sourceEndpoint);
            switch (message.getSenderType()) {
                case "USER":
                    // 用户消息 - 根据receiverType推送到商户端或平台端
                    if (!"ADMIN".equals(sourceEndpoint)) {
                        // 根据接收者类型决定推送目标
                        if ("PLATFORM".equals(message.getReceiverType())) {
                            // 用户联系平台客服，推送到平台端
                            pushMessageToPlatformCrossApp(message.getSessionId(), message);
                        } else {
                            // 用户联系商户客服，推送到商户端
                            pushUserMessageToMerchantCrossApp(message.getSessionId(), message);
                            pushUserMessageToMerchant(message.getSessionId(), message);
                        }
                    }
                    break;
                    
                case "MERCHANT":
                    // 商户客服消息 - 根据receiverType推送到用户端、平台端或其他商户端
                    if (!"FRONT".equals(sourceEndpoint)) {
                        // 根据接收者类型决定推送目标
                        if ("PLATFORM".equals(message.getReceiverType())) {
                            // 商户回复平台消息，推送到平台端
                            pushMessageToPlatformCrossApp(message.getSessionId(), message);
                        } else if ("MERCHANT".equals(message.getReceiverType())) {
                            // 商户给其他商户发消息，推送到目标商户端
                            pushMerchantMessageToMerchantCrossApp(message.getSessionId(), message);
                        } else {
                            // 商户回复用户消息，推送到小程序端用户
                            pushStaffReplyToUserCrossApp(message.getSessionId(), message);
                        }
                    }
                    break;
                    
                case "PLATFORM":
                    // 平台消息 - 需要推送给商户端或用户端
                    // 根据接收者类型确定推送目标
                    if (message.getReceiverType() != null && "MERCHANT".equals(message.getReceiverType())) {
                        // 推送给商户端
                        pushPlatformMessageToMerchantCrossApp(message.getSessionId(), message);
                    } else {
                        // 推送给用户端
                        pushStaffReplyToUserCrossApp(message.getSessionId(), message);
                    }
                    break;
                    
                case "AI":
                    // AI消息 - 推送给所有相关端
                    pushAiReplyToRelevantEndpointsCrossApp(message.getSessionId(), message);
                   // pushAiReplyToRelevantEndpoints(message.getSessionId(), message);
                    break;
                    
                case "SYSTEM":
                    // 系统消息 - 广播到所有端
                    crossAppMessageService.broadcastSystemNotification(message.getContent(), "ALL");
                  //  broadcastSystemNotification(message.getSessionId(), message.getContent());
                    break;
                    
                default:
                    log.warn("未知的发送者类型: {}", message.getSenderType());
            }
        } catch (Exception e) {
            log.error("路由消息失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendToFrontendUser(Integer userId, Map<String, Object> message) {
        try {
            // 发布事件，让 jcly-front 模块的监听器处理
            MessageRoutingEvent event = MessageRoutingEvent.createSendToUserEvent(this, userId, message);
            eventPublisher.publishEvent(event);
            log.debug("消息事件已发布给小程序用户: userId={}", userId);
        } catch (Exception e) {
            log.error("发布消息事件给小程序用户失败: userId={}, 错误: {}", userId, e.getMessage(), e);
        }
    }

    @Override
    public void sendToMerchantStaff(Integer staffId, Map<String, Object> message) {
        try {
            // 发布事件，让 jcly-admin 模块的监听器处理
            MessageRoutingEvent event = MessageRoutingEvent.createSendToStaffEvent(this, staffId, message);
            eventPublisher.publishEvent(event);
            log.debug("消息事件已发布给商户端客服: staffId={}", staffId);
        } catch (Exception e) {
            log.error("发布消息事件给商户端客服失败: staffId={}, 错误: {}", staffId, e.getMessage(), e);
        }
    }

    @Override
    public void broadcastSystemNotification(String sessionId, String notification) {
        try {
            // 发布系统通知事件
            MessageRoutingEvent event = MessageRoutingEvent.createSystemNotificationEvent(this, sessionId, notification);
            eventPublisher.publishEvent(event);
            log.debug("系统通知事件已发布: sessionId={}", sessionId);
        } catch (Exception e) {
            log.error("发布系统通知事件失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public void handleTransferToHumanNotification(String sessionId, Integer userId, Integer staffId, Integer queuePosition) {
        try {
            // 发布转人工客服通知事件
            MessageRoutingEvent event = MessageRoutingEvent.createTransferToHumanEvent(
                this, sessionId, userId, staffId, queuePosition);
            eventPublisher.publishEvent(event);
            log.info("转人工客服通知事件已发布: sessionId={}, userId={}, staffId={}", sessionId, userId, staffId);
        } catch (Exception e) {
            log.error("发布转人工客服通知事件失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void pushAiReplyToRelevantEndpoints(String sessionId, UnifiedChatMessage message) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "ai_reply");
            messageData.put("sessionId", message.getSessionId());
            messageData.put("messageId", message.getMessageId());
            messageData.put("content", message.getContent());
            messageData.put("contentType", message.getContentType());
            messageData.put("messageType", message.getMessageType());
            messageData.put("timestamp", System.currentTimeMillis());
            messageData.put("sender", "AI");

            // 获取会话信息来确定推送目标
            try {
                UnifiedChatSession session = unifiedChatService.getSession(sessionId);
                if (session != null) {
                    // 发布AI回复事件到小程序端（用户可以看到AI回复）
                    if (session.getUserId() != null) {
                        MessageRoutingEvent userEvent = MessageRoutingEvent.createSendToUserEvent(this, session.getUserId().intValue(), messageData);
                        eventPublisher.publishEvent(userEvent);
                        log.info("AI回复事件已发布到小程序端: sessionId={}, userId={}", sessionId, session.getUserId());
                    }

                    // 发布AI回复事件到商户端（客服可以看到用户与AI的对话历史）
                    if (session.getStaffId() != null) {
                        Map<String, Object> merchantMessageData = new HashMap<>(messageData);
                        merchantMessageData.put("type", "ai_conversation_update");
                        merchantMessageData.put("isHistoryMessage", true); // 标记为历史消息，供商户端显示
                        merchantMessageData.put("userId", session.getUserId());

                        MessageRoutingEvent staffEvent = MessageRoutingEvent.createSendToStaffEvent(this, session.getStaffId().intValue(), merchantMessageData);
                        eventPublisher.publishEvent(staffEvent);
                        log.info("AI回复事件已发布到商户端: sessionId={}, staffId={}", sessionId, session.getStaffId());
                    }
                } else {
                    log.warn("无法获取会话信息，跳过AI回复推送: sessionId={}", sessionId);
                }
            } catch (Exception e) {
                log.error("获取会话信息失败，使用默认推送: sessionId={}, 错误: {}", sessionId, e.getMessage());
                
                // 回退到基本推送逻辑，广播给所有端
                MessageRoutingEvent broadcastEvent = new MessageRoutingEvent(
                    this, MessageRoutingEvent.EventType.AI_REPLY_NOTIFICATION, 
                    sessionId, null, null, messageData, null);
                eventPublisher.publishEvent(broadcastEvent);
            }

            log.debug("AI回复事件已发布到相关端点: sessionId={}", sessionId);
        } catch (Exception e) {
            log.error("发布AI回复事件失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public void pushUserMessageToMerchant(String sessionId, UnifiedChatMessage message) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "user_message");
            messageData.put("sessionId", message.getSessionId());
            messageData.put("messageId", message.getMessageId());
            messageData.put("senderId", message.getSenderId());
            messageData.put("senderName", message.getSenderName());
            messageData.put("content", message.getContent());
            messageData.put("contentType", message.getContentType());
            messageData.put("messageType", message.getMessageType());
            messageData.put("timestamp", System.currentTimeMillis());
            messageData.put("isFromUser", true);

            // 获取会话信息，检查是否有分配的客服
            try {
                UnifiedChatSession session = unifiedChatService.getSession(sessionId);
                if (session != null && session.getStaffId() != null && session.getStaffId() > 0) {
                    // 如果有分配的客服，直接推送给该客服
                    log.info("推送用户消息给指定客服: sessionId={}, staffId={}, userId={}", 
                            sessionId, session.getStaffId(), message.getSenderId());
                    
                    // 增加会话上下文信息
                    messageData.put("staffId", session.getStaffId());
                    messageData.put("sessionStatus", session.getStatus());
                    messageData.put("serviceMode", session.getServiceMode());
                    
                    MessageRoutingEvent event = MessageRoutingEvent.createSendToStaffEvent(
                        this, session.getStaffId().intValue(), messageData);
                    eventPublisher.publishEvent(event);
                    
                    log.debug("用户消息已推送给指定客服: sessionId={}, staffId={}, userId={}", 
                            sessionId, session.getStaffId(), message.getSenderId());
                } else {
                    // 如果没有分配客服，使用广播方式（兜底处理）
                    log.info("会话未分配客服，使用广播方式推送用户消息: sessionId={}, userId={}", 
                            sessionId, message.getSenderId());
                    
                    MessageRoutingEvent event = new MessageRoutingEvent(
                        this, MessageRoutingEvent.EventType.USER_MESSAGE_TO_MERCHANT,
                        sessionId, null, null, messageData, null);
                    eventPublisher.publishEvent(event);
                    
                    log.debug("用户消息已广播到商户端: sessionId={}, userId={}", sessionId, message.getSenderId());
                }
            } catch (Exception e) {
                log.error("获取会话信息失败，使用广播方式: sessionId={}, 错误: {}", sessionId, e.getMessage());
                
                // 如果获取会话信息失败，使用广播作为兜底
                MessageRoutingEvent event = new MessageRoutingEvent(
                    this, MessageRoutingEvent.EventType.USER_MESSAGE_TO_MERCHANT,
                    sessionId, null, null, messageData, null);
                eventPublisher.publishEvent(event);
            }

        } catch (Exception e) {
            log.error("发布用户消息事件到商户端失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public void pushStaffReplyToUser(String sessionId, UnifiedChatMessage message) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "staff_reply");
            messageData.put("sessionId", message.getSessionId());
            messageData.put("messageId", message.getMessageId());
            messageData.put("senderId", message.getSenderId());
            messageData.put("senderName", message.getSenderName());
            messageData.put("senderAvatar", message.getSenderAvatar());
            messageData.put("content", message.getContent());
            messageData.put("contentType", message.getContentType());
            messageData.put("messageType", message.getMessageType());
            messageData.put("timestamp", System.currentTimeMillis());
            messageData.put("sender", "MERCHANT");
            messageData.put("isFromStaff", true);

            // 发布客服回复事件到小程序端
            if (message.getReceiverId() != null) {
                MessageRoutingEvent event = MessageRoutingEvent.createSendToUserEvent(
                    this, message.getReceiverId().intValue(), messageData);
                eventPublisher.publishEvent(event);
                log.info("客服回复事件已发布到小程序端: sessionId={}, receiverId={}, content={}", 
                        sessionId, message.getReceiverId(), message.getContent());
            } else {
                log.warn("接收者ID为空，无法发布客服回复事件: sessionId={}", sessionId);
            }
        } catch (Exception e) {
            log.error("发布客服回复事件到小程序端失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }

    /**
     * 跨应用推送用户消息到商户端（使用智能客服分配）
     */
    private void pushUserMessageToMerchantCrossApp(String sessionId, UnifiedChatMessage message) {
        try {
            // 1. 获取或创建会话
            UnifiedChatSession session = ensureSessionExists(sessionId, message);
            if (session == null) {
                log.error("【跨应用路由】无法获取或创建会话: sessionId={}", sessionId);
                return;
            }

            // 2. 确定目标客服ID（智能分配，如果没有则自动创建）
            Integer staffId = determineOrCreateTargetStaffId(message, session);

            if (staffId == null) {
                log.error("【跨应用路由】无法分配或创建客服，消息将被丢弃: sessionId={}", sessionId);
                return;
            }

            // 3. 确保会话已关联客服
            if (session.getStaffId() == null || !session.getStaffId().equals(staffId.longValue())) {
                session.setStaffId(staffId.longValue());
                unifiedChatSessionDao.updateById(session);
                log.info("【跨应用路由】会话已关联客服: sessionId={}, staffId={}", sessionId, staffId);
            }

            // 4. 构造推送消息
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "user_message");
            messageData.put("sessionId", message.getSessionId());
            messageData.put("messageId", message.getMessageId());
            // ✅ 修复：转换 Long 为 Integer，避免 Redis 序列化产生类型信息
            messageData.put("senderId", safeConvertToInteger(message.getSenderId()));
            messageData.put("senderName", message.getSenderName());
            messageData.put("content", message.getContent());
            messageData.put("contentType", message.getContentType());
            messageData.put("messageType", message.getMessageType());
            messageData.put("timestamp", System.currentTimeMillis());
            messageData.put("isFromUser", true);
            messageData.put("staffId", staffId);  // staffId 已经是 Integer

            // 5. 发布到Redis频道（按客服ID）- 即使客服不在线也发送
            String channel = "customer-service:" + staffId;
            redisTemplate.convertAndSend(channel, messageData);

            log.info("【跨应用路由】用户消息已路由到客服（无论在线状态）: staffId={}, messageId={}, channel={}, sessionId={}",
                    staffId, message.getMessageId(), channel, sessionId);

            // 6. 兜底：同时使用原有的跨应用服务（确保消息不丢失）
            Integer userId = message.getSenderId() != null ? message.getSenderId().intValue() : null;
            crossAppMessageService.sendUserMessageToMerchant(sessionId, userId, staffId, messageData);

        } catch (Exception e) {
            log.error("【跨应用路由】推送用户消息到商户端失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }

    /**
     * 跨应用推送客服回复到小程序端
     */
    private void pushStaffReplyToUserCrossApp(String sessionId, UnifiedChatMessage message) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "staff_reply");
            messageData.put("sessionId", message.getSessionId());
            messageData.put("messageId", message.getMessageId());
            // ✅ 修复：转换 Long 为 Integer
            messageData.put("senderId", safeConvertToInteger(message.getSenderId()));
            messageData.put("senderName", message.getSenderName());
            messageData.put("senderAvatar", message.getSenderAvatar());
            messageData.put("content", message.getContent());
            messageData.put("contentType", message.getContentType());
            messageData.put("messageType", message.getMessageType());
            messageData.put("timestamp", System.currentTimeMillis());
            messageData.put("sender", "MERCHANT");
            messageData.put("isFromStaff", true);

            Integer userId = safeConvertToInteger(message.getReceiverId());
            Integer staffId = safeConvertToInteger(message.getSenderId());

            if (userId != null) {
                // 使用跨应用消息服务推送
                crossAppMessageService.sendStaffReplyToFrontend(sessionId, userId, staffId, messageData);
                
                log.info("【跨应用路由】客服回复已通过Redis发送到小程序端: sessionId={}, userId={}, staffId={}", 
                        sessionId, userId, staffId);
            } else {
                log.warn("【跨应用路由】接收者ID为空，无法推送客服回复: sessionId={}", sessionId);
            }
        } catch (Exception e) {
            log.error("【跨应用路由】推送客服回复到小程序端失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }

    /**
     * 跨应用推送AI回复到相关端点
     */
    private void pushAiReplyToRelevantEndpointsCrossApp(String sessionId, UnifiedChatMessage message) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "ai_reply");
            messageData.put("sessionId", message.getSessionId());
            messageData.put("messageId", message.getMessageId());
            messageData.put("content", message.getContent());
            messageData.put("contentType", message.getContentType());
            messageData.put("messageType", message.getMessageType());
            messageData.put("timestamp", System.currentTimeMillis());
            messageData.put("sender", "AI");

            // 获取会话信息来确定推送目标
            try {
                UnifiedChatSession session = unifiedChatService.getSession(sessionId);
                if (session != null) {
                    // ✅ 修复：转换 Long 为 Integer
                    Integer userId = safeConvertToInteger(session.getUserId());
                    Integer staffId = safeConvertToInteger(session.getStaffId());

                    // 使用跨应用消息服务推送AI回复
                    crossAppMessageService.sendAiReplyToRelevantEndpoints(sessionId, userId, staffId, messageData);
                    
                    log.info("【跨应用路由】AI回复已通过Redis发送到相关端点: sessionId={}, userId={}, staffId={}", 
                            sessionId, userId, staffId);
                } else {
                    log.warn("【跨应用路由】无法获取会话信息，跳过AI回复推送: sessionId={}", sessionId);
                }
            } catch (Exception e) {
                log.error("【跨应用路由】获取会话信息失败，跳过AI回复推送: sessionId={}, 错误: {}", sessionId, e.getMessage());
            }
        } catch (Exception e) {
            log.error("【跨应用路由】推送AI回复失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }

    /**
     * 跨应用推送平台消息到商户端
     */
    private void pushPlatformMessageToMerchantCrossApp(String sessionId, UnifiedChatMessage message) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "platform_message");
            messageData.put("sessionId", message.getSessionId());
            messageData.put("messageId", message.getMessageId());
            // ✅ 修复：转换 Long 为 Integer
            messageData.put("senderId", safeConvertToInteger(message.getSenderId()));
            messageData.put("senderName", message.getSenderName() != null ? message.getSenderName() : "平台管理员");
            messageData.put("content", message.getContent());
            messageData.put("contentType", message.getContentType());
            messageData.put("messageType", message.getMessageType());
            messageData.put("timestamp", System.currentTimeMillis());
            messageData.put("isFromPlatform", true);

            // 获取会话信息，确定目标商户
            try {
                UnifiedChatSession session = unifiedChatService.getSession(sessionId);
                if (session != null && session.getStaffId() != null && session.getStaffId() > 0) {
                    // 推送到商户端客服
                    Integer staffId = safeConvertToInteger(session.getStaffId());
                    messageData.put("staffId", staffId);
                    crossAppMessageService.sendPlatformMessageToMerchant(sessionId, staffId, messageData);
                    log.info("【跨应用路由】平台消息已发送到商户端客服: sessionId={}, staffId={}", 
                            sessionId, session.getStaffId());
                } else {
                    log.warn("【跨应用路由】会话中没有分配商户客服，跳过平台消息推送: sessionId={}", sessionId);
                }
            } catch (Exception e) {
                log.error("【跨应用路由】获取会话信息失败: sessionId={}, 错误: {}", sessionId, e.getMessage());
            }
        } catch (Exception e) {
            log.error("【跨应用路由】推送平台消息到商户端失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }

    /**
     * 跨应用推送商户消息到其他商户端
     * 【修复】确保发送方和接收方都有客服，且使用同一个会话
     */
    private void pushMerchantMessageToMerchantCrossApp(String sessionId, UnifiedChatMessage message) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "merchant_to_merchant_message");
            messageData.put("sessionId", message.getSessionId());
            messageData.put("messageId", message.getMessageId());
            // ✅ 修复：转换 Long 为 Integer
            messageData.put("senderId", safeConvertToInteger(message.getSenderId()));
            messageData.put("senderName", message.getSenderName() != null ? message.getSenderName() : "商户客服");
            messageData.put("senderAvatar", message.getSenderAvatar());
            messageData.put("content", message.getContent());
            messageData.put("contentType", message.getContentType());
            messageData.put("messageType", message.getMessageType());
            messageData.put("timestamp", System.currentTimeMillis());
            messageData.put("isFromMerchant", true);

            // 获取目标商户客服ID（从消息的receiverId获取）
            Integer targetStaffId = safeConvertToInteger(message.getReceiverId());
            Integer sourceStaffId = safeConvertToInteger(message.getSenderId());

            if (targetStaffId == null) {
                // 如果没有指定receiverId，尝试从会话中获取
                try {
                    UnifiedChatSession session = unifiedChatService.getSession(sessionId);
                    if (session != null && session.getStaffId() != null && session.getStaffId() > 0) {
                        targetStaffId = session.getStaffId().intValue();
                    }
                } catch (Exception e) {
                    log.error("【跨应用路由】获取会话信息失败: sessionId={}, 错误: {}", sessionId, e.getMessage());
                }
            }

            if (targetStaffId != null && targetStaffId > 0 && sourceStaffId != null && sourceStaffId > 0) {
                // 【修复】确保双方都有客服：自动为发送方和接收方创建客服
                try {
                    // 1. 检查并创建发送方客服
                    com.zbkj.common.model.admin.SystemAdmin sourceAdmin = systemAdminService.getById(sourceStaffId);
                    if (sourceAdmin != null && sourceAdmin.getMerId() != null) {
                        Long sourceMerId = sourceAdmin.getMerId().longValue();
                        if (!customerServiceStaffService.isStaffAvailable(sourceStaffId)) {
                            log.info("【跨应用路由】发送方商户客服不可用，自动创建: sourceAdminId={}, merId={}", sourceStaffId, sourceMerId);
                            CustomerServiceStaff sourceStaff = sourceMerId == 0L
                                    ? customerServiceStaffService.createOrGetDefaultStaffForPlatform()
                                    : customerServiceStaffService.createOrGetDefaultStaffForMerchant(sourceMerId);
                            if (sourceStaff != null) {
                                log.info("【跨应用路由】成功为发送方创建客服 ✅: adminId={}, merId={}", sourceStaff.getAdminId(), sourceMerId);
                            }
                        }
                    }

                    // 2. 检查并创建接收方客服
                    com.zbkj.common.model.admin.SystemAdmin targetAdmin = systemAdminService.getById(targetStaffId);
                    if (targetAdmin != null && targetAdmin.getMerId() != null) {
                        Long targetMerId = targetAdmin.getMerId().longValue();
                        if (!customerServiceStaffService.isStaffAvailable(targetStaffId)) {
                            log.info("【跨应用路由】目标商户客服不可用，自动创建: targetAdminId={}, merId={}", targetStaffId, targetMerId);
                            CustomerServiceStaff targetStaff = targetMerId == 0L
                                    ? customerServiceStaffService.createOrGetDefaultStaffForPlatform()
                                    : customerServiceStaffService.createOrGetDefaultStaffForMerchant(targetMerId);
                            if (targetStaff != null) {
                                log.info("【跨应用路由】成功为目标商户创建客服 ✅: adminId={}, merId={}", targetStaff.getAdminId(), targetMerId);
                                targetStaffId = targetStaff.getAdminId();
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("【跨应用路由】检查或创建双方客服失败: sourceStaffId={}, targetStaffId={}, 错误: {}",
                            sourceStaffId, targetStaffId, e.getMessage());
                }

                // 使用专用的商户到商户消息发送方法
                crossAppMessageService.sendMerchantMessageToMerchant(sessionId, sourceStaffId, targetStaffId, messageData);
                log.info("【跨应用路由】商户消息已发送到目标商户端客服: sessionId={}, sourceStaffId={}, targetStaffId={}",
                        sessionId, sourceStaffId, targetStaffId);
            } else {
                log.warn("【跨应用路由】无法确定发送方或目标商户客服ID，跳过商户到商户消息推送: sessionId={}, sourceStaffId={}, targetStaffId={}",
                        sessionId, sourceStaffId, targetStaffId);
            }
        } catch (Exception e) {
            log.error("【跨应用路由】推送商户消息到其他商户端失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }

    /**
     * 跨应用推送消息到平台端
     */
    private void pushMessageToPlatformCrossApp(String sessionId, UnifiedChatMessage message) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "user_message_to_platform");
            messageData.put("sessionId", message.getSessionId());
            messageData.put("messageId", message.getMessageId());
            // ✅ 修复：转换 Long 为 Integer
            messageData.put("senderId", safeConvertToInteger(message.getSenderId()));
            messageData.put("senderName", message.getSenderName() != null ? message.getSenderName() : "用户" + message.getSenderId());
            messageData.put("content", message.getContent());
            messageData.put("contentType", message.getContentType());
            messageData.put("messageType", message.getMessageType());
            messageData.put("timestamp", System.currentTimeMillis());
            messageData.put("isFromUser", true);

            // 获取会话信息，确定目标平台客服
            try {
                UnifiedChatSession session = unifiedChatService.getSession(sessionId);
                if (session != null && session.getStaffId() != null) {
                    // 推送到平台端客服
                    Integer staffId = safeConvertToInteger(session.getStaffId());
                    Integer userId = safeConvertToInteger(session.getUserId());
                    messageData.put("staffId", staffId);
                    messageData.put("userId", userId);
                    crossAppMessageService.sendUserMessageToPlatformStaff(sessionId, userId, staffId, messageData);
                    log.info("【跨应用路由】用户消息已发送到平台端客服: sessionId={}, userId={}, staffId={}",
                            sessionId, userId, staffId);
                } else {
                    log.warn("【跨应用路由】会话中没有分配客服，跳过消息推送: sessionId={}", sessionId);
                }
            } catch (Exception e) {
                log.error("【跨应用路由】获取会话信息失败: sessionId={}, 错误: {}", sessionId, e.getMessage());
            }
        } catch (Exception e) {
            log.error("【跨应用路由】推送消息到平台端失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }

    // ==================== 智能客服分配相关方法 ====================

    /**
     * 确保会话存在
     * @param sessionId 会话ID
     * @param message 消息
     * @return 会话对象
     */
    private UnifiedChatSession ensureSessionExists(String sessionId, UnifiedChatMessage message) {
        try {
            // 1. 先尝试获取现有会话
            UnifiedChatSession session = unifiedChatSessionDao.selectOne(
                    new LambdaQueryWrapper<UnifiedChatSession>()
                            .eq(UnifiedChatSession::getSessionId, sessionId)
            );

            if (session != null) {
                return session;
            }

            // 2. 会话不存在，创建新会话
            log.info("【跨应用路由】会话不存在，创建新会话: sessionId={}", sessionId);

            session = new UnifiedChatSession();
            session.setSessionId(sessionId);

            // 设置用户信息
            if (message.getSenderId() != null) {
                session.setUserId(message.getSenderId());
            }

            // 根据接收者类型确定商户ID
            Long merId = 0L;  // 默认平台
            if ("MERCHANT".equals(message.getReceiverType())) {
                // 如果接收者是商户，从receiverId获取商户ID
                if (message.getReceiverId() != null) {
                    merId = message.getReceiverId();
                }
            }
            session.setMerId(merId);

            session.setUserType("USER");
            session.setSessionType(UnifiedChatSession.SESSION_TYPE_HUMAN);
            session.setCurrentServiceType(UnifiedChatSession.SERVICE_TYPE_HUMAN);
            session.setStatus(UnifiedChatSession.STATUS_ACTIVE);
            session.setTotalMessages(0);
            session.setPriority(UnifiedChatSession.PRIORITY_NORMAL);
            session.setCreateTime(new Date());
            session.setUpdateTime(new Date());

            unifiedChatSessionDao.insert(session);
            log.info("【跨应用路由】新会话已创建: sessionId={}, merId={}", sessionId, merId);

            return session;

        } catch (Exception e) {
            log.error("【跨应用路由】确保会话存在失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 确定或创建目标客服ID（智能分配）
     * 如果商户没有客服，自动创建默认客服
     *
     * @param message 消息
     * @param session 会话
     * @return 客服AdminID
     */
    private Integer determineOrCreateTargetStaffId(UnifiedChatMessage message, UnifiedChatSession session) {
        try {
            // 1. 从会话获取已分配的客服（会话中存储的是admin_id）
            if (session.getStaffId() != null && session.getStaffId() > 0) {
                Integer adminId = session.getStaffId().intValue();

                // 验证客服是否仍然可用（使用admin_id验证）
                if (customerServiceStaffService.isStaffAvailable(adminId)) {
                    log.info("【智能分配】使用会话中已分配的客服 ✅: sessionId={}, adminId={}",
                            message.getSessionId(), adminId);
                    return adminId;
                } else {
                    log.warn("【智能分配】会话中的客服不可用，需要重新分配: sessionId={}, oldAdminId={}",
                            message.getSessionId(), adminId);
                }
            }

            // 2. 如果消息明确指定了客服，使用指定的客服（这里假设指定的也是admin_id）
            if (("MERCHANT".equals(message.getReceiverType()) || "STAFF".equals(message.getReceiverType()))
                    && message.getReceiverId() != null) {
                Integer targetAdminId = message.getReceiverId().intValue();
                if (customerServiceStaffService.isStaffAvailable(targetAdminId)) {
                    log.info("【智能分配】使用消息指定的客服 ✅: messageId={}, adminId={}",
                            message.getMessageId(), targetAdminId);
                    return targetAdminId;
                } else {
                    log.warn("【智能分配】消息指定的客服不可用: messageId={}, adminId={}",
                            message.getMessageId(), targetAdminId);
                }
            }

            // 3. 自动分配或创建客服
            return assignOrCreateStaff(message, session);

        } catch (Exception e) {
            log.error("【智能分配】确定或创建目标客服失败: sessionId={}, 错误: {}",
                    message.getSessionId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 自动分配或创建客服
     * 优先尝试分配现有客服，如果没有则自动创建
     *
     * @param message 消息
     * @param session 会话
     * @return 客服AdminID
     */
    private Integer assignOrCreateStaff(UnifiedChatMessage message, UnifiedChatSession session) {
        try {
            Long merId = session.getMerId();

            if (merId == null) {
                log.warn("【智能分配】无法获取商户ID，无法分配客服: sessionId={}",
                        message.getSessionId());
                return null;
            }

            // 1. 先尝试分配现有的可用客服
            List<CustomerServiceStaff> availableStaff =
                    customerServiceStaffService.getAvailableStaff(merId);

            if (availableStaff != null && !availableStaff.isEmpty()) {
                // 选择当前会话数最少的客服（负载均衡）
                availableStaff.sort(Comparator.comparingInt(CustomerServiceStaff::getCurrentSessions));
                CustomerServiceStaff assignedStaff = availableStaff.get(0);

                // 使用staffId（表主键ID）增加会话数
                customerServiceStaffService.incrementCurrentSessions(assignedStaff.getId());

                log.info("【智能分配】分配现有客服成功 ✅: adminId={}, staffId={}, staffName={}, currentSessions={}, merId={}",
                        assignedStaff.getAdminId(), assignedStaff.getId(), assignedStaff.getStaffName(),
                        assignedStaff.getCurrentSessions(), merId);

                return assignedStaff.getAdminId();
            }

            // 2. 没有可用客服，自动创建默认客服
            log.info("【智能分配】没有可用客服，尝试自动创建默认客服: merId={}", merId);

            boolean isPlatform = merId == 0L || "PLATFORM".equals(message.getReceiverType());
            CustomerServiceStaff defaultStaff;

            if (isPlatform) {
                // 平台客服：自动创建或获取
                defaultStaff = customerServiceStaffService.getDefaultStaff(0L);
                if (defaultStaff == null) {
                    log.info("【智能分配】平台没有默认客服，自动创建");
                    defaultStaff = customerServiceStaffService.createOrGetDefaultStaffForPlatform();
                }
            } else {
                // 商户客服：自动创建或获取
                defaultStaff = customerServiceStaffService.getDefaultStaff(merId);
                if (defaultStaff == null) {
                    log.info("【智能分配】商户没有默认客服，自动创建: merId={}", merId);
                    defaultStaff = customerServiceStaffService.createOrGetDefaultStaffForMerchant(merId);
                }
            }

            if (defaultStaff != null) {
                log.info("【智能分配】自动创建或获取默认客服成功 ✅: sessionId={}, adminId={}, staffName={}, isPlatform={}",
                        session.getSessionId(), defaultStaff.getAdminId(), defaultStaff.getStaffName(), isPlatform);

                return defaultStaff.getAdminId();
            }

            log.error("【智能分配】无法创建默认客服: merId={}, isPlatform={}", merId, isPlatform);
            return null;

        } catch (Exception e) {
            log.error("【智能分配】自动分配或创建客服失败: sessionId={}, 错误: {}",
                    message.getSessionId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 确定目标客服ID（智能分配）
     * @param message 消息
     * @return 客服AdminID（注意：这里返回的是admin_id，不是主键id）
     */
    private Integer determineTargetStaffId(UnifiedChatMessage message) {
        try {
            // 1. 从会话获取已分配的客服（会话中存储的是admin_id）
            UnifiedChatSession session = unifiedChatSessionDao.selectOne(
                    new LambdaQueryWrapper<UnifiedChatSession>()
                            .eq(UnifiedChatSession::getSessionId, message.getSessionId())
            );

            if (session != null && session.getStaffId() != null && session.getStaffId() > 0) {
                // ✅ 会话中的staffId实际存储的是admin_id
                Integer adminId = session.getStaffId().intValue();

                // 验证客服是否仍然可用（使用admin_id验证）
                if (customerServiceStaffService.isStaffAvailable(adminId)) {
                    log.info("【智能分配】使用会话中已分配的客服 ✅: sessionId={}, adminId={}",
                            message.getSessionId(), adminId);
                    return adminId;
                } else {
                    log.warn("【智能分配】会话中的客服不可用，需要重新分配: sessionId={}, oldAdminId={}",
                            message.getSessionId(), adminId);
                }
            }

            // 2. 如果消息明确指定了客服，使用指定的客服（这里假设指定的也是admin_id）
            if (("MERCHANT".equals(message.getReceiverType()) || "STAFF".equals(message.getReceiverType()))
                    && message.getReceiverId() != null) {
                Integer targetAdminId = message.getReceiverId().intValue();
                if (customerServiceStaffService.isStaffAvailable(targetAdminId)) {
                    log.info("【智能分配】使用消息指定的客服 ✅: messageId={}, adminId={}",
                            message.getMessageId(), targetAdminId);
                    return targetAdminId;
                } else {
                    log.warn("【智能分配】消息指定的客服不可用: messageId={}, adminId={}",
                            message.getMessageId(), targetAdminId);
                }
            }

            // 3. 自动分配客服
            CustomerServiceStaff staff = autoAssignStaff(message, session);

            if (staff != null) {
                // ✅ 更新会话的客服ID（存储admin_id）
                if (session != null) {
                    session.setStaffId(staff.getAdminId().longValue());  // ✅ 存储admin_id
                    unifiedChatSessionDao.updateById(session);
                    log.info("【智能分配】会话已更新客服AdminID ✅: sessionId={}, adminId={}, staffName={}",
                            session.getSessionId(), staff.getAdminId(), staff.getStaffName());
                }
                return staff.getAdminId();  // ✅ 返回admin_id
            }

            log.warn("【智能分配】无法找到可用客服: sessionId={}", message.getSessionId());
            return null;

        } catch (Exception e) {
            log.error("【智能分配】确定目标客服失败: sessionId={}, 错误: {}",
                    message.getSessionId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 自动分配客服（智能选择）
     * @param message 消息
     * @param session 会话
     * @return 分配的客服
     */
    private CustomerServiceStaff autoAssignStaff(UnifiedChatMessage message, UnifiedChatSession session) {
        try {
            Long merId = session != null ? session.getMerId() : null;

            if (merId == null) {
                log.warn("【智能分配】无法获取商户ID，无法分配客服: sessionId={}",
                        message.getSessionId());
                return null;
            }

            // 获取可用客服列表（在线且未达到最大会话数）
            List<CustomerServiceStaff> availableStaff =
                    customerServiceStaffService.getAvailableStaff(merId);

            if (availableStaff == null || availableStaff.isEmpty()) {
                log.warn("【智能分配】没有可用客服，尝试使用默认客服: merId={}", merId);
                // 没有可用客服，返回默认客服
                return customerServiceStaffService.getDefaultStaff(merId);
            }

            // 选择当前会话数最少的客服（负载均衡）
            availableStaff.sort(Comparator.comparingInt(CustomerServiceStaff::getCurrentSessions));
            CustomerServiceStaff assignedStaff = availableStaff.get(0);

            // ✅ 修复：使用staffId（表主键ID）增加会话数
            customerServiceStaffService.incrementCurrentSessions(assignedStaff.getId());

            log.info("【智能分配】自动分配客服成功 ✅: adminId={}, staffId={}, staffName={}, currentSessions={}, merId={}",
                    assignedStaff.getAdminId(), assignedStaff.getId(), assignedStaff.getStaffName(),
                    assignedStaff.getCurrentSessions(), merId);

            return assignedStaff;

        } catch (Exception e) {
            log.error("【智能分配】自动分配客服失败: sessionId={}, 错误: {}",
                    message.getSessionId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 分配默认客服（兜底方案）
     * @param message 消息
     * @return 客服AdminID
     */
    private Integer assignDefaultStaff(UnifiedChatMessage message) {
        try {
            // 获取会话
            UnifiedChatSession session = unifiedChatSessionDao.selectOne(
                    new LambdaQueryWrapper<UnifiedChatSession>()
                            .eq(UnifiedChatSession::getSessionId, message.getSessionId())
            );

            if (session == null || session.getMerId() == null) {
                log.warn("【智能分配】无法获取会话或商户ID，无法分配默认客服: sessionId={}",
                        message.getSessionId());
                return null;
            }

            // 判断是否为平台客服（merId=0 或 receiverType=PLATFORM）
            boolean isPlatform = session.getMerId() == 0L || "PLATFORM".equals(message.getReceiverType());

            // 获取或自动创建默认客服
            CustomerServiceStaff defaultStaff = null;
            if (isPlatform) {
                // 平台客服：先尝试获取，没有则自动创建
                defaultStaff = customerServiceStaffService.getDefaultStaff(0L);
                if (defaultStaff == null) {
                    log.info("【智能分配】平台没有默认客服，尝试自动创建");
                    defaultStaff = customerServiceStaffService.createOrGetDefaultStaffForPlatform();
                }
            } else {
                // 商户客服：先尝试获取，没有则自动创建
                defaultStaff = customerServiceStaffService.getDefaultStaff(session.getMerId());
                if (defaultStaff == null) {
                    log.info("【智能分配】商户没有默认客服，尝试自动创建: merId={}", session.getMerId());
                    defaultStaff = customerServiceStaffService.createOrGetDefaultStaffForMerchant(session.getMerId());
                }
            }

            if (defaultStaff != null) {
                // ✅ 更新会话（存储admin_id）
                session.setStaffId(defaultStaff.getAdminId().longValue());  // ✅ 存储admin_id
                unifiedChatSessionDao.updateById(session);

                log.info("【智能分配】分配默认客服成功 ✅: sessionId={}, adminId={}, staffName={}, isPlatform={}",
                        session.getSessionId(), defaultStaff.getAdminId(), defaultStaff.getStaffName(), isPlatform);

                return defaultStaff.getAdminId();  // ✅ 返回admin_id
            }

            log.warn("【智能分配】未找到或无法创建默认客服: merId={}, isPlatform={}",
                    session.getMerId(), isPlatform);
            return null;

        } catch (Exception e) {
            log.error("【智能分配】分配默认客服失败: sessionId={}, 错误: {}",
                    message.getSessionId(), e.getMessage(), e);
            return null;
        }
    }
}