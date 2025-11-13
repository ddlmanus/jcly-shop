package com.zbkj.service.service;

import com.zbkj.common.model.chat.UnifiedChatMessage;

import java.util.Map;

/**
 * 统一消息路由服务
 * 负责在小程序端和商户端之间路由消息
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
public interface UnifiedMessageRoutingService {

    /**
     * 路由消息到所有相关端
     * @param message 统一聊天消息
     * @param sourceEndpoint 消息来源端点：FRONT（小程序端）, ADMIN（商户端）
     */
    void routeMessage(UnifiedChatMessage message, String sourceEndpoint);

    /**
     * 发送消息给小程序端用户
     * @param userId 用户ID
     * @param message 消息内容
     */
    void sendToFrontendUser(Integer userId, Map<String, Object> message);

    /**
     * 发送消息给商户端客服
     * @param staffId 客服ID
     * @param message 消息内容
     */
    void sendToMerchantStaff(Integer staffId, Map<String, Object> message);

    /**
     * 发送系统通知到所有相关端
     * @param sessionId 会话ID
     * @param notification 通知内容
     */
    void broadcastSystemNotification(String sessionId, String notification);

    /**
     * 处理转人工客服通知
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param staffId 客服ID（如果已分配）
     * @param queuePosition 排队位置（如果未分配客服）
     */
    void handleTransferToHumanNotification(String sessionId, Integer userId, Integer staffId, Integer queuePosition);

    /**
     * 推送AI回复到相关端
     * @param sessionId 会话ID
     * @param message AI消息
     */
    void pushAiReplyToRelevantEndpoints(String sessionId, UnifiedChatMessage message);

    /**
     * 推送用户消息到商户端
     * @param sessionId 会话ID
     * @param message 用户消息
     */
    void pushUserMessageToMerchant(String sessionId, UnifiedChatMessage message);

    /**
     * 推送客服回复到小程序端
     * @param sessionId 会话ID
     * @param message 客服消息
     */
    void pushStaffReplyToUser(String sessionId, UnifiedChatMessage message);
}