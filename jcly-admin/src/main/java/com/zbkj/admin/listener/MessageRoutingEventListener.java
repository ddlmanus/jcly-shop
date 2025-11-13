package com.zbkj.admin.listener;

import com.zbkj.common.event.MessageRoutingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 消息路由事件监听器 - 商户端
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Component
public class MessageRoutingEventListener {

    @Autowired(required = false)
    private com.zbkj.admin.websocket.HumanServiceWebSocketHandler adminWebSocketHandler;

    @EventListener
    public void handleMessageRoutingEvent(MessageRoutingEvent event) {
        log.info("【商户端】收到消息路由事件: eventType={}, sessionId={}, targetUserId={}, targetStaffId={}", 
                event.getEventType(), event.getSessionId(), event.getTargetUserId(), event.getTargetStaffId());
        
        try {
            switch (event.getEventType()) {
                case SEND_TO_MERCHANT_STAFF:
                    log.info("【商户端】处理发送给指定客服事件");
                    handleSendToMerchantStaff(event);
                    break;
                case BROADCAST_SYSTEM_NOTIFICATION:
                    log.info("【商户端】处理系统通知广播事件");
                    handleBroadcastSystemNotification(event);
                    break;
                case TRANSFER_TO_HUMAN_NOTIFICATION:
                    log.info("【商户端】处理转人工客服通知事件");
                    handleTransferToHumanNotification(event);
                    break;
                case AI_REPLY_NOTIFICATION:
                    log.info("【商户端】处理AI回复通知事件");
                    handleAiReplyNotification(event);
                    break;
                case USER_MESSAGE_TO_MERCHANT:
                    log.info("【商户端】处理用户消息到商户事件");
                    handleUserMessageToMerchant(event);
                    break;
                default:
                    log.warn("【商户端】未知的事件类型: {}", event.getEventType());
                    break;
            }
        } catch (Exception e) {
            log.error("【商户端】处理消息路由事件失败: eventType={}, 错误: {}", event.getEventType(), e.getMessage(), e);
        }
    }

    private void handleSendToMerchantStaff(MessageRoutingEvent event) {
        log.info("【商户端】开始处理发送给指定客服: staffId={}, WebSocketHandler是否可用={}", 
                event.getTargetStaffId(), adminWebSocketHandler != null);
        
        if (adminWebSocketHandler != null && event.getTargetStaffId() != null) {
            log.info("【商户端】调用WebSocketHandler发送消息给客服: staffId={}, 消息内容={}", 
                    event.getTargetStaffId(), event.getMessageData());
            adminWebSocketHandler.sendMessageToUser(event.getTargetStaffId(), event.getMessageData());
            log.info("【商户端】消息已发送给商户端客服: staffId={}", event.getTargetStaffId());
        } else {
            log.error("【商户端】无法发送消息 - WebSocketHandler可用={}, 目标客服ID={}", 
                    adminWebSocketHandler != null, event.getTargetStaffId());
        }
    }

    private void handleBroadcastSystemNotification(MessageRoutingEvent event) {
        if (adminWebSocketHandler != null) {
            adminWebSocketHandler.broadcastToAllUsers(event.getMessageData());
            log.debug("系统通知已广播到商户端: sessionId={}", event.getSessionId());
        } else {
            log.warn("管理端WebSocket处理器不可用，无法广播系统通知");
        }
    }

    private void handleTransferToHumanNotification(MessageRoutingEvent event) {
        // 处理转人工客服通知
        if (event.getTargetStaffId() != null && event.getTargetStaffId() > 0) {
            // 通知被分配的客服
            java.util.Map<String, Object> staffNotification = new java.util.HashMap<>();
            staffNotification.put("type", "new_session_assigned");
            staffNotification.put("sessionId", event.getSessionId());
            staffNotification.put("userId", event.getTargetUserId());
            staffNotification.put("timestamp", System.currentTimeMillis());
            staffNotification.put("message", "新的客服会话已分配给您");

            if (adminWebSocketHandler != null) {
                adminWebSocketHandler.sendMessageToUser(event.getTargetStaffId(), staffNotification);
                log.info("转人工客服通知已发送给商户端客服: staffId={}, sessionId={}", 
                        event.getTargetStaffId(), event.getSessionId());
            }
        }
    }

    private void handleAiReplyNotification(MessageRoutingEvent event) {
        if (adminWebSocketHandler != null) {
            // AI消息广播到商户端，供客服查看对话历史
            adminWebSocketHandler.broadcastToAllUsers(event.getMessageData());
            log.debug("AI回复已广播到商户端: sessionId={}", event.getSessionId());
        }
    }

    private void handleUserMessageToMerchant(MessageRoutingEvent event) {
        log.info("【商户端】开始处理用户消息到商户: sessionId={}, WebSocketHandler是否可用={}", 
                event.getSessionId(), adminWebSocketHandler != null);
        
        if (adminWebSocketHandler != null) {
            try {
                // 从事件数据中获取会话ID
                String sessionId = event.getSessionId();
                log.info("【商户端】会话ID={}, 消息数据={}", sessionId, event.getMessageData());
                
                if (sessionId != null) {
                    // 使用会话特定的广播方法，只推送给该会话的参与者（客服）
                    log.info("【商户端】调用broadcastMessageToSession推送消息");
                    adminWebSocketHandler.broadcastMessageToSession(sessionId, event.getMessageData());
                    log.info("【商户端】用户消息已推送给会话参与者: sessionId={}", sessionId);
                } else {
                    // 如果没有会话ID，作为兜底方案广播到所有用户
                    log.info("【商户端】会话ID为空，使用广播方案");
                    adminWebSocketHandler.broadcastToAllUsers(event.getMessageData());
                    log.info("【商户端】用户消息已广播到所有商户端用户（兜底方案）");
                }
            } catch (Exception e) {
                log.error("【商户端】推送用户消息到商户端失败: sessionId={}, 错误: {}", event.getSessionId(), e.getMessage(), e);
                // 如果特定推送失败，尝试广播作为兜底
                try {
                    log.info("【商户端】尝试使用广播作为兜底方案");
                    adminWebSocketHandler.broadcastToAllUsers(event.getMessageData());
                    log.info("【商户端】使用广播作为兜底方案推送用户消息成功");
                } catch (Exception ex) {
                    log.error("【商户端】广播用户消息也失败: {}", ex.getMessage(), ex);
                }
            }
        } else {
            log.error("【商户端】WebSocketHandler不可用，无法处理用户消息");
        }
    }
}