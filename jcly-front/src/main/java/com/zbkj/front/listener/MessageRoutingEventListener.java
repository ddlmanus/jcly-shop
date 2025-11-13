package com.zbkj.front.listener;

import com.zbkj.common.event.MessageRoutingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 消息路由事件监听器 - 小程序端
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Component
public class MessageRoutingEventListener {

    @Autowired(required = false)
    private com.zbkj.front.websocket.HumanServiceWebSocketHandler frontWebSocketHandler;

    @EventListener
    public void handleMessageRoutingEvent(MessageRoutingEvent event) {
        try {
            switch (event.getEventType()) {
                case SEND_TO_FRONTEND_USER:
                    handleSendToFrontendUser(event);
                    break;
                case BROADCAST_SYSTEM_NOTIFICATION:
                    handleBroadcastSystemNotification(event);
                    break;
                case TRANSFER_TO_HUMAN_NOTIFICATION:
                    handleTransferToHumanNotification(event);
                    break;
                case AI_REPLY_NOTIFICATION:
                    handleAiReplyNotification(event);
                    break;
                case STAFF_REPLY_TO_USER:
                    handleStaffReplyToUser(event);
                    break;
                default:
                    // 其他事件类型不处理
                    break;
            }
        } catch (Exception e) {
            log.error("处理消息路由事件失败: eventType={}, 错误: {}", event.getEventType(), e.getMessage(), e);
        }
    }

    private void handleSendToFrontendUser(MessageRoutingEvent event) {
        if (frontWebSocketHandler != null && event.getTargetUserId() != null) {
            frontWebSocketHandler.sendMessageToUser(event.getTargetUserId(), event.getMessageData());
            log.debug("消息已发送给小程序用户: userId={}", event.getTargetUserId());
        } else {
            log.warn("前端WebSocket处理器不可用或目标用户ID为空");
        }
    }

    private void handleBroadcastSystemNotification(MessageRoutingEvent event) {
        if (frontWebSocketHandler != null) {
            // 使用新添加的广播方法
            @SuppressWarnings("unchecked")
            Map<String, Object> messageData = (Map<String, Object>) event.getMessageData();
            String notification = (String) messageData.get("content");
            
            frontWebSocketHandler.broadcastSystemNotification(notification, "USER");
            log.debug("系统通知已广播到小程序端: sessionId={}", event.getSessionId());
        } else {
            log.warn("前端WebSocket处理器不可用，无法广播系统通知");
        }
    }

    private void handleTransferToHumanNotification(MessageRoutingEvent event) {
        // 处理转人工客服通知
        if (event.getTargetUserId() != null) {
            java.util.Map<String, Object> userNotification = new java.util.HashMap<>();
            userNotification.put("type", "transfer_to_human");
            userNotification.put("sessionId", event.getSessionId());
            userNotification.put("staffId", event.getTargetStaffId());
            
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> eventData = (java.util.Map<String, Object>) event.getMessageData();
            Integer queuePosition = (Integer) eventData.get("queuePosition");
            userNotification.put("queuePosition", queuePosition);
            userNotification.put("timestamp", System.currentTimeMillis());

            if (event.getTargetStaffId() != null && event.getTargetStaffId() > 0) {
                userNotification.put("message", "您的对话已转接到人工客服，请稍等...");
            } else {
                userNotification.put("message", String.format("您的对话已转接到人工客服，当前排队位置：第%d位", queuePosition != null ? queuePosition : 1));
            }

            if (frontWebSocketHandler != null) {
                frontWebSocketHandler.sendMessageToUser(event.getTargetUserId(), userNotification);
                log.info("转人工客服通知已发送给小程序用户: userId={}, sessionId={}", 
                        event.getTargetUserId(), event.getSessionId());
            }
        }
    }

    private void handleAiReplyNotification(MessageRoutingEvent event) {
        if (frontWebSocketHandler != null && event.getTargetUserId() != null) {
            // AI回复发送给对应用户
            frontWebSocketHandler.sendMessageToUser(event.getTargetUserId(), event.getMessageData());
            log.debug("AI回复已发送给小程序用户: userId={}, sessionId={}", event.getTargetUserId(), event.getSessionId());
        } else if (frontWebSocketHandler != null && event.getSessionId() != null) {
            // 如果没有指定用户，使用会话ID推送
            frontWebSocketHandler.sendAiReplyToSession(event.getSessionId(), event.getMessageData());
            log.debug("AI回复已通过会话推送到小程序端: sessionId={}", event.getSessionId());
        } else {
            // 最后兜底，广播给所有用户
            if (frontWebSocketHandler != null) {
                frontWebSocketHandler.broadcastToAllUsers(event.getMessageData());
                log.debug("AI回复已广播到小程序端所有用户: sessionId={}", event.getSessionId());
            } else {
                log.warn("前端WebSocket处理器不可用，无法推送AI回复");
            }
        }
    }

    private void handleStaffReplyToUser(MessageRoutingEvent event) {
        if (frontWebSocketHandler != null && event.getTargetUserId() != null) {
            frontWebSocketHandler.sendMessageToUser(event.getTargetUserId(), event.getMessageData());
            log.debug("客服回复已发送给小程序用户: userId={}, sessionId={}", event.getTargetUserId(), event.getSessionId());
        }
    }
}