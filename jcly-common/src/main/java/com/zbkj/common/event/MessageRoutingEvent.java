package com.zbkj.common.event;

import lombok.Data;
import org.springframework.context.ApplicationEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息路由事件 - 用于跨模块消息传递
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
public class MessageRoutingEvent extends ApplicationEvent {

    /**
     * 事件类型
     */
    public enum EventType {
        SEND_TO_FRONTEND_USER,      // 发送给小程序用户
        SEND_TO_MERCHANT_STAFF,     // 发送给商户客服
        BROADCAST_SYSTEM_NOTIFICATION, // 广播系统通知
        TRANSFER_TO_HUMAN_NOTIFICATION, // 转人工客服通知
        AI_REPLY_NOTIFICATION,      // AI回复通知
        USER_MESSAGE_TO_MERCHANT,   // 用户消息发送给商户
        STAFF_REPLY_TO_USER        // 客服回复发送给用户
    }

    private final EventType eventType;
    private final String sessionId;
    private final Integer targetUserId;
    private final Integer targetStaffId;
    private final Map<String, Object> messageData;
    private final String sourceEndpoint;

    public MessageRoutingEvent(Object source, EventType eventType, String sessionId, 
                              Integer targetUserId, Integer targetStaffId, 
                              Map<String, Object> messageData, String sourceEndpoint) {
        super(source);
        this.eventType = eventType;
        this.sessionId = sessionId;
        this.targetUserId = targetUserId;
        this.targetStaffId = targetStaffId;
        this.messageData = messageData;
        this.sourceEndpoint = sourceEndpoint;
    }

    // 创建发送给用户的事件
    public static MessageRoutingEvent createSendToUserEvent(Object source, Integer userId, Map<String, Object> message) {
        return new MessageRoutingEvent(source, EventType.SEND_TO_FRONTEND_USER, null, userId, null, message, null);
    }

    // 创建发送给客服的事件
    public static MessageRoutingEvent createSendToStaffEvent(Object source, Integer staffId, Map<String, Object> message) {
        return new MessageRoutingEvent(source, EventType.SEND_TO_MERCHANT_STAFF, null, null, staffId, message, null);
    }

    // 创建系统通知事件
    public static MessageRoutingEvent createSystemNotificationEvent(Object source, String sessionId, String notification) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "system_notification");
        data.put("sessionId", sessionId);
        data.put("content", notification);
        data.put("timestamp", System.currentTimeMillis());
        return new MessageRoutingEvent(source, EventType.BROADCAST_SYSTEM_NOTIFICATION, sessionId, null, null, data, null);
    }

    // 创建转人工客服通知事件
    public static MessageRoutingEvent createTransferToHumanEvent(Object source, String sessionId, 
                                                                Integer userId, Integer staffId, Integer queuePosition) {
        Map<String, Object> data = new HashMap<>();
        data.put("sessionId", sessionId);
        data.put("userId", userId);
        data.put("staffId", staffId != null ? staffId : 0);
        data.put("queuePosition", queuePosition != null ? queuePosition : 0);
        return new MessageRoutingEvent(source, EventType.TRANSFER_TO_HUMAN_NOTIFICATION, sessionId, userId, staffId, data, null);
    }
}