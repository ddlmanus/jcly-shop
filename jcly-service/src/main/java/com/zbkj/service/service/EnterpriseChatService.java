package com.zbkj.service.service;

import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.EnterpriseChatSessionRequest;
import com.zbkj.common.request.EnterpriseChatMessageRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.model.coze.EnterpriseChatSession;
import com.zbkj.common.model.coze.EnterpriseChatMessage;
import com.zbkj.common.vo.LoginFrontUserVo;


import java.util.List;
import java.util.Map;

/**
 * 企业级聊天服务接口
 * 提供完整的企业级AI聊天功能
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
public interface EnterpriseChatService {

    // ================================
    // 会话管理相关方法
    // ================================

    /**
     * 获取会话列表
     * 
     * @param cozeBotId 智能体ID（可选）
     * @param status 会话状态（可选）
     * @param pageParamRequest 分页参数
     * @return 会话列表
     */
    CommonPage<EnterpriseChatSession> getSessionList(String cozeBotId, Integer status, 
                                                   PageParamRequest pageParamRequest);

    /**
     * 创建新的聊天会话
     * 
     * @param request 会话创建请求
     * @return 创建的会话信息
     */
    EnterpriseChatSession createSession(EnterpriseChatSessionRequest request);

    /**
     * 创建新的聊天会话（指定用户）
     * 
     * @param request 会话创建请求
     * @param user 用户信息
     * @return 创建的会话信息
     */
    EnterpriseChatSession createSession(EnterpriseChatSessionRequest request, com.zbkj.common.vo.LoginUserVo user);

    /**
     * 获取会话详情
     * 
     * @param sessionId 会话ID
     * @return 会话详情
     */
    EnterpriseChatSession getSessionDetail(String sessionId);

    /**
     * 获取会话详情（指定用户）
     * 
     * @param sessionId 会话ID
     * @param user 用户信息
     * @return 会话详情
     */
    EnterpriseChatSession getSessionDetail(String sessionId, com.zbkj.common.vo.LoginUserVo user);
    /**
     * 获取会话详情（指定用户）
     *
     * @param sessionId 会话ID
     * @param user 用户信息
     * @return 会话详情
     */
    EnterpriseChatSession getFrontSessionDetail(String sessionId, LoginFrontUserVo user);

    /**
     * 更新会话信息
     * 
     * @param sessionId 会话ID
     * @param request 更新请求
     * @return 更新后的会话信息
     */
    EnterpriseChatSession updateSession(String sessionId, EnterpriseChatSessionRequest request);

    /**
     * 删除会话
     * 
     * @param sessionId 会话ID
     */
    void deleteSession(String sessionId);

    /**
     * 清空会话历史记录
     * 
     * @param sessionId 会话ID
     */
    void clearSessionHistory(String sessionId);

    // ================================
    // 消息管理相关方法
    // ================================

    // 注意：getMessageList 和 sendMessage 方法已迁移到 UnifiedChatService
    // 这些方法已被标记为废弃，请使用 UnifiedChatService 替代
    
    /**
     * @deprecated 请使用 UnifiedChatService.getSessionMessages() 替代
     */
    @Deprecated
    CommonPage<EnterpriseChatMessage> getMessageList(String sessionId, String role, 
                                                    String messageType, PageParamRequest pageParamRequest);

    /**
     * @deprecated 请使用 UnifiedChatService.sendMessage() 替代
     */
    @Deprecated
    Map<String, Object> sendMessage(EnterpriseChatMessageRequest request);

    /**
     * 获取消息详情
     * 
     * @param messageId 消息ID
     * @return 消息详情
     */
    EnterpriseChatMessage getMessageDetail(String messageId);

    /**
     * 删除消息
     * 
     * @param messageId 消息ID
     */
    void deleteMessage(String messageId);

    /**
     * 重新发送失败的消息
     * 
     * @param messageId 消息ID
     * @return 重新发送的结果
     */
    Map<String, Object> resendMessage(String messageId);

    /**
     * 保存消息到数据库
     * 
     * @param message 消息对象
     * @return 保存的消息
     */
    EnterpriseChatMessage saveMessage(EnterpriseChatMessage message);

    /**
     * 处理Coze流式响应
     * 
     * @param sessionId 会话ID
     * @param streamResponse 流式响应对象
     * @param userMessageId 用户消息ID
     * @return 处理后的AI回复消息
     */
    EnterpriseChatMessage processStreamResponse(String sessionId, 
                                              com.zbkj.common.model.coze.stream.CozeStreamResponse streamResponse, 
                                              String userMessageId);

    // ================================
    // 统计分析相关方法
    // ================================

    /**
     * 获取聊天统计信息
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param cozeBotId 智能体ID（可选）
     * @return 统计信息
     */
    Map<String, Object> getChatStatistics(String startDate, String endDate, String cozeBotId);

    /**
     * 获取热门对话主题
     * 
     * @param limit 返回数量限制
     * @return 热门主题列表
     */
    List<Map<String, Object>> getHotTopics(Integer limit);

    /**
     * 导出聊天历史记录
     * 
     * @param sessionId 会话ID
     * @param format 导出格式（json, csv, txt）
     * @return 导出结果
     */
    String exportChatHistory(String sessionId, String format);

    // ================================
    // 配置管理相关方法
    // ================================

    /**
     * 获取聊天配置
     * 
     * @return 配置信息
     */
    Map<String, Object> getChatConfig();

    /**
     * 更新聊天配置
     * 
     * @param config 配置信息
     */
    void updateChatConfig(Map<String, Object> config);

    // ================================
    // 辅助方法
    // ================================

    /**
     * 根据用户ID和智能体ID获取或创建会话
     * 
     * @param cozeBotId 智能体ID
     * @return 会话信息
     */
    EnterpriseChatSession getOrCreateUserSession(String cozeBotId);

    /**
     * 构建Coze API对话的上下文消息
     * 
     * @param sessionId 会话ID
     * @param limit 上下文消息数量限制
     * @return 上下文消息列表
     */
    List<Map<String, Object>> buildContextMessages(String sessionId, Integer limit);

    /**
     * 处理Coze API响应并保存到数据库
     * 
     * @param sessionId 会话ID
     * @param cozeResponse Coze API响应
     * @param userMessageId 用户消息ID
     * @return 处理后的AI消息
     */
    EnterpriseChatMessage processCozeResponse(String sessionId, Object cozeResponse, String userMessageId);
}
