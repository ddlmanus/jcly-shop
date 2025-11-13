package com.zbkj.service.service;

import java.util.Map;

/**
 * 跨应用消息通信服务接口
 * 用于解决商户端(20800)和小程序端(20810)之间的WebSocket消息推送问题
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
public interface CrossAppMessageService {
    
    /**
     * 发送消息到商户端
     * @param channel 消息频道
     * @param message 消息内容
     */
    void sendToMerchant(String channel, Map<String, Object> message);
    
    /**
     * 发送消息到小程序端
     * @param channel 消息频道
     * @param message 消息内容
     */
    void sendToFrontend(String channel, Map<String, Object> message);
    
    /**
     * 发送用户消息到商户端
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param staffId 客服ID（可为null）
     * @param message 消息内容
     */
    void sendUserMessageToMerchant(String sessionId, Integer userId, Integer staffId, Map<String, Object> message);
    
    /**
     * 发送客服回复到小程序端
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param staffId 客服ID
     * @param message 消息内容
     */
    void sendStaffReplyToFrontend(String sessionId, Integer userId, Integer staffId, Map<String, Object> message);
    
    /**
     * 发送AI回复到相关端点
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param staffId 客服ID（可为null）
     * @param message 消息内容
     */
    void sendAiReplyToRelevantEndpoints(String sessionId, Integer userId, Integer staffId, Map<String, Object> message);
    
    /**
     * 发送平台消息到商户端
     * @param sessionId 会话ID
     * @param staffId 客服ID
     * @param message 消息内容
     */
    void sendPlatformMessageToMerchant(String sessionId, Integer staffId, Map<String, Object> message);
    
    /**
     * 发送商户消息到平台端
     * @param sessionId 会话ID
     * @param userId 平台管理员ID
     * @param message 消息内容
     */
    void sendMerchantMessageToPlatform(String sessionId, Integer userId, Map<String, Object> message);

    /**
     * 发送用户消息到平台端客服
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param staffId 平台客服ID
     * @param message 消息内容
     */
    void sendUserMessageToPlatformStaff(String sessionId, Integer userId, Integer staffId, Map<String, Object> message);

    /**
     * 发送商户消息到其他商户端
     * @param sessionId 会话ID
     * @param sourceStaffId 发送者客服ID
     * @param targetStaffId 目标客服ID
     * @param message 消息内容
     */
    void sendMerchantMessageToMerchant(String sessionId, Integer sourceStaffId, Integer targetStaffId, Map<String, Object> message);

    /**
     * 广播系统通知
     * @param notification 通知内容
     * @param targetType 目标类型：MERCHANT, FRONTEND, ALL
     */
    void broadcastSystemNotification(String notification, String targetType);
}
