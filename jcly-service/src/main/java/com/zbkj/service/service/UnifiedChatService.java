package com.zbkj.service.service;

import com.zbkj.common.model.chat.UnifiedChatMessage;
import com.zbkj.common.model.chat.UnifiedChatSession;
import com.zbkj.common.request.chat.SendMessageRequest;
import com.zbkj.common.response.chat.MessageResponse;
import com.zbkj.common.vo.LoginFrontUserVo;
import com.zbkj.common.vo.MyRecord;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * 统一聊天服务接口
 * @author AI Assistant
 * @since 2025-01-09
 */
public interface UnifiedChatService {

    /**
     * 创建或获取会话
     * @param userId 用户ID
     * @param userType 用户类型
     * @param merId 商户ID
     * @param sessionType 会话类型：AI, HUMAN, MIXED
     * @param cozeBotId Coze智能体ID（AI会话时需要）
     * @return 会话信息
     */
    UnifiedChatSession createOrGetSession(Long userId, String userType, Long merId, String sessionType, String cozeBotId);

    /**
     * 发送消息
     * @param request 发送消息请求
     * @return 消息响应
     */
    MessageResponse sendMessage(SendMessageRequest request);

    /**
     * 获取会话消息列表
     * @param sessionId 会话ID
     * @param page 页码
     * @param size 每页大小
     * @return 消息列表
     */
    List<MessageResponse> getSessionMessages(String sessionId, Integer page, Integer size);

    /**
     * 获取会话信息
     * @param sessionId 会话ID
     * @return 会话信息
     */
    UnifiedChatSession getSession(String sessionId);

    /**
     * 更新会话状态
     * @param sessionId 会话ID
     * @param status 新状态
     */
    void updateSessionStatus(String sessionId, String status);

    /**
     * 转接到人工客服
     * @param sessionId 会话ID
     * @param transferReason 转接原因
     * @param priority 优先级
     * @return 更新后的会话信息
     */
    UnifiedChatSession transferToHumanService(String sessionId, String transferReason, String priority);

    /**
     * 分配客服
     * @param sessionId 会话ID
     * @param staffId 客服ID
     * @return 更新后的会话信息
     */
    UnifiedChatSession assignStaff(String sessionId, Long staffId);

    /**
     * 结束会话
     * @param sessionId 会话ID
     * @param reason 结束原因
     */
    void endSession(String sessionId, String reason);

    /**
     * 标记消息已读
     * @param messageId 消息ID
     * @param readerId 阅读者ID
     * @param readerType 阅读者类型
     */
    void markMessageAsRead(String messageId, Long readerId, String readerType);

    /**
     * 获取用户的活跃会话列表
     * @param userId 用户ID
     * @param userType 用户类型
     * @param merId 商户ID
     * @return 会话列表
     */
    List<UnifiedChatSession> getUserActiveSessions(Long userId, String userType, Long merId);

    /**
     * 获取客服的活跃会话列表
     * @param staffId 客服ID
     * @param merId 商户ID
     * @return 会话列表
     */
    List<UnifiedChatSession> getStaffActiveSessions(Long staffId, Long merId);

    /**
     * 获取等待队列
     * @param merId 商户ID
     * @return 等待中的会话列表
     */
    List<UnifiedChatSession> getWaitingQueue(Long merId);

    /**
     * 处理AI回复
     * @param message 用户消息
     * @return AI回复消息
     */
    MessageResponse processAiReply(UnifiedChatMessage message);

    /**
     * 根据Coze会话ID获取会话
     * @param cozeConversationId Coze会话ID
     * @return 会话信息
     */
    UnifiedChatSession getSessionByCozeConversationId(String cozeConversationId);

    /**
     * 保存会话
     * @param session 会话信息
     * @return 保存的会话信息
     */
    UnifiedChatSession saveSession(UnifiedChatSession session);

    /**
     * 更新会话
     * @param session 会话信息
     * @return 更新的会话信息
     */
    UnifiedChatSession updateSession(UnifiedChatSession session);

    /**
     * 生成会话ID
     * @return 会话ID
     */
    String generateSessionId();

    /**
     * 根据Coze消息ID获取消息
     * @param cozeMessageId Coze消息ID
     * @return 消息信息
     */
    UnifiedChatMessage getMessageByCozeMessageId(String cozeMessageId);

    /**
     * 保存消息
     * @param message 消息信息
     * @return 保存的消息信息
     */
    UnifiedChatMessage saveMessage(UnifiedChatMessage message);

    /**
     * 更新消息
     * @param message 消息信息
     * @return 更新的消息信息
     */
    UnifiedChatMessage updateMessage(UnifiedChatMessage message);

    /**
     * 生成消息ID
     * @return 消息ID
     */
    String generateMessageId();

    /**
     * 更新会话最后消息信息
     * @param session 会话信息
     * @param message 最后消息
     */
    void updateSessionLastMessage(UnifiedChatSession session, UnifiedChatMessage message);

    /**
     * 基于联系人创建或获取会话
     * @param contactId 联系人ID
     * @param contactType 联系人类型
     * @param merId 商户ID
     * @param staffId 客服ID
     * @param sessionType 会话类型
     * @return 会话信息
     */
    UnifiedChatSession createOrGetSessionByContact(Long contactId, String contactType, Long merId, Long staffId, String sessionType);

    /**
     * 获取与当前用户相关的联系人列表（包含最新消息）
     * @param merId 商户ID
     * @param currentUserId 当前用户ID
     * @param page 页码
     * @param size 页大小
     * @return 联系人列表
     */
    List<Map<String, Object>> getContactListWithMessages(Long merId, Long currentUserId, Integer page, Integer size);

    /**
     * 标记会话消息为已读
     * @param sessionId 会话ID
     */
    void markSessionMessagesAsRead(String sessionId);

    /**
     * 标记单条消息为已读（重载方法）
     * @param messageId 消息ID
     */
    void markSingleMessageAsRead(String messageId);

    /**
     * 获取消息已读状态
     * @param messageId 消息ID
     * @return 已读状态信息
     */
    Map<String, Object> getMessageReadStatus(String messageId);

    /**
     * 消息管理页面 - 获取消息列表（支持搜索过滤）
     * @param sessionId 会话ID（可选）
     * @param messageType 消息类型（可选）
     * @param role 角色（可选）
     * @param content 消息内容关键词（可选）
     * @param senderType 发送者类型（可选）
     * @param status 消息状态（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 分页消息列表
     */
    com.github.pagehelper.PageInfo<MessageResponse> getMessagesForManagement(
        String sessionId, String messageType, String role, String content, 
        String senderType, String status, Integer page, Integer size);

    /**
     * 会话管理页面 - 获取会话列表（支持搜索过滤）
     * @param merId 商户ID
     * @param sessionType 会话类型（可选）
     * @param status 会话状态（可选）
     * @param userId 用户ID（可选）
     * @param cozeBotId Coze智能体ID（可选）
     * @param sessionId 会话ID（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 分页会话列表
     */
    com.github.pagehelper.PageInfo<UnifiedChatSession> getSessionsForManagement(
        Long merId, String sessionType, String status, Long userId, 
        String cozeBotId, String sessionId, Integer page, Integer size);

    /**
     * 根据类型获取可添加的用户列表
     * @param userType 用户类型：USER-用户，MERCHANT-商户，PLATFORM-平台
     * @param keyword 搜索关键词（可选）
     * @return 用户列表
     */
    List<Map<String, Object>> getAvailableUsersByType(String userType, String keyword);
    
    UnifiedChatSession getOrCreateSession(@NotBlank(message = "会话ID不能为空") String sessionId, LoginFrontUserVo finalUserContext);

    UnifiedChatSession createOrGetUserSession(long userId,Integer merId, @NotBlank(message = "智能体ID不能为空") String cozeBotId);

    /**
     * 人工客服消息管理 - 获取消息列表（排除AI助手消息）
     * @param merId 商户ID
     * @param sessionId 会话ID（可选）
     * @param messageType 消息类型（可选）
     * @param role 角色（可选）
     * @param content 消息内容关键词（可选）
     * @param senderType 发送者类型（可选）
     * @param status 消息状态（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 分页消息列表
     */
    com.github.pagehelper.PageInfo<com.zbkj.common.response.chat.MessageResponse> getHumanServiceMessages(
        Long merId, String sessionId, String messageType, String role, String content, 
        String senderType, String status, Integer page, Integer size);

    /**
     * 根据消息ID获取消息详情
     * @param messageId 消息ID
     * @return 消息详情
     */
    com.zbkj.common.model.chat.UnifiedChatMessage getMessageById(String messageId);

    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @return 操作结果
     */
    boolean markMessageAsRead(String messageId);

    /**
     * 批量删除消息
     * @param messageIds 消息ID列表
     * @return 操作结果
     */
    boolean batchDeleteMessages(java.util.List<String> messageIds);

    /**
     * 人工客服会话管理 - 获取会话列表
     * @param merId 商户ID
     * @param sessionType 会话类型（可选）
     * @param status 会话状态（可选）
     * @param userId 用户ID（可选）
     * @param sessionId 会话ID（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 分页会话列表
     */
    com.github.pagehelper.PageInfo<com.zbkj.common.model.chat.UnifiedChatSession> getHumanServiceSessions(
        Long merId, String sessionType, String status, Long userId, String sessionId, Integer page, Integer size);

    /**
     * 获取指定会话的消息列表
     * @param merId 商户ID
     * @param sessionId 会话ID
     * @param messageType 消息类型（可选）
     * @param role 角色（可选）
     * @param senderType 发送者类型（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 分页消息列表
     */
    com.github.pagehelper.PageInfo<com.zbkj.common.response.chat.MessageResponse> getSessionMessages(
        Long merId, String sessionId, String messageType, String role, String senderType, Integer page, Integer size);

    boolean clearMessages(String sessionId);

    /**
     * 获取未读消息数量
     * @param merId 商户ID
     * @return 未读消息数量
     */
    int getUnreadMessageCount(Long merId);

    /**
     * 获取用户未读消息数量
     * @param userId 用户ID
     * @return 未读消息数量
     */
    public int getUserUnreadMessageCount(Long userId);
}