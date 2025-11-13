package com.zbkj.service.service;

import com.zbkj.common.model.chat.UnifiedChatMessage;
import io.swagger.models.auth.In;

import java.util.Map;

/**
 * 人工客服WebSocket服务接口
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
public interface HumanServiceWebSocketService {

    /**
     * 验证token并获取用户信息
     */
    Map<String, Object> validateTokenAndGetUser(String token, String userType);

    /**
     * 更新用户在线状态
     */
    void updateUserOnlineStatus(Integer userId, String userType);

    /**
     * 更新用户离线状态
     */
    void updateUserOfflineStatus(Integer userId);

    /**
     * 发送离线消息
     */
    void sendOfflineMessages(Integer userId, String userType);

    /**
     * 更新心跳
     */
    void updateHeartbeat(Integer userId);

    /**
     * 发送聊天消息
     */
    Map<String, Object> sendChatMessage(Integer senderId, String senderType, String sessionId,
                                        String content, Integer receiverId, String receiverType);

    /**
     * 标记消息为已读
     */
    void markMessageAsRead(String messageId, Integer userId);

    /**
     * 更新客服状态
     */
    void updateStaffStatus(Integer staffId, String status);

    /**
     * 推送新会话通知
     */
    void pushNewSessionNotification(Integer staffId, String sessionId);

    /**
     * 推送系统通知
     */
    void pushSystemNotification(Integer userId, String userType, String title, String content);

    /**
     * 获取用户在线状态
     */
    boolean isUserOnline(Integer userId, String userType);

    /**
     * 处理不同的消息类型
     * @param userId
     * @param userType
     * @param string
     */
    void handleUserMessage(Integer userId, String userType, String string);

    /**
     * 发送消息给指定用户
     * @param userId 用户ID
     * @param message 消息内容
     */
    void sendMessageToUser(Integer userId, Map<String, Object> message);
    
    /**
     * 生成消息ID
     * @return 消息ID
     */
    String generateMessageId();
    
    /**
     * 保存消息到数据库
     * @param message 消息对象
     * @return 保存的消息
     */
    UnifiedChatMessage saveMessage(UnifiedChatMessage message);
    
    /**
     * 通知商户端有新消息
     * @param message 消息对象
     */
    void notifyMerchantOfNewMessage(UnifiedChatMessage message);
}
