package com.zbkj.service.service;

import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.*;
import com.zbkj.common.response.*;
import com.zbkj.common.model.service.*;
import com.zbkj.common.vo.LoginFrontUserVo;
import com.zbkj.common.vo.LoginUserVo;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


import java.util.List;
import java.util.Map;

/**
 * 人工客服服务接口
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
public interface HumanServiceService {

    /**
     * 转接到人工客服
     */
    Map<String, Object> transferToHuman(TransferToHumanRequest request);

    /**
     * 获取会话详情
     */
    HumanServiceSession getSessionDetail(String sessionId);

    /**
     * 接受会话（客服接单）
     */
    void acceptSession(String sessionId);

    /**
     * 结束会话
     */
    void endSession(String sessionId);

    /**
     * 获取客服工作台数据
     */
    Map<String, Object> getWorkplaceData();

    /**
     * 获取快捷回复模板
     */
    List<QuickReplyTemplate> getQuickReplyTemplates(String category);

    /**
     * 获取快捷回复模板（分页）
     */
    com.github.pagehelper.PageInfo<QuickReplyTemplate> getQuickReplyTemplatesWithPage(String category, String keyword, Integer page, Integer size);

    /**
     * 获取客服统计数据
     */
    Map<String, Object> getServiceStatistics(String startDate, String endDate);

    /**
     * 客服分配算法
     */
    CustomerServiceStaff assignStaff(Integer merId, String serviceLevel, String requiredSkills);

    /**
     * 检查是否需要转人工
     */
    boolean shouldTransferToHuman(String content);

    /**
     * 发送系统消息
     */
    void sendSystemMessage(String sessionId, String content);

    /**
     * 更新会话优先级
     */
    void updateSessionPriority(String sessionId, String priority);

    /**
     * 获取等待队列信息
     */
    Map<String, Object> getQueueInfo(Integer merId);

    // ==================== 用户端专用方法 ====================

    /**
     * 获取用户当前会话
     */
    HumanServiceSessionResponse getUserCurrentSession();

    /**
     * 获取会话详情（用户端）
     */
    Map<String, Object> getUserSessionDetail(String sessionId);

    /**
     * 发送消息（用户端）
     */
    HumanServiceMessageResponse sendUserMessage(HumanServiceMessageRequest request);

    /**
     * 获取消息列表（用户端）
     */
    CommonPage<HumanServiceMessageResponse> getUserMessageList(Map<String, Object> params);

    /**
     * 上传图片
     */
    Map<String, Object> uploadImage(MultipartFile file);

    /**
     * 上传文件
     */
    Map<String, Object> uploadFile(MultipartFile file);

    /**
     * 发送商品消息
     */
    HumanServiceMessageResponse sendProductMessage(String sessionId, Integer productId);

    /**
     * 发送订单消息
     */
    HumanServiceMessageResponse sendOrderMessage(String sessionId, String orderNo);

    /**
     * 获取在线状态
     */
    Map<String, Object> getOnlineStatus();

    /**
     * 获取队列信息（用户端）
     */
    Map<String, Object> getUserQueueInfo();

    /**
     * 评价客服服务
     */
    void rateService(String sessionId, Integer rating, String comment);

    /**
     * 获取常见问题
     */
    Map<String, Object> getFAQ();

    /**
     * 获取快捷问题
     */
    Map<String, Object> getQuickQuestions();

    // ==================== 平台端专用方法 ====================

    /**
     * 获取平台会话列表
     */
    CommonPage<HumanServiceSession> getPlatformSessionList(String sessionStatus, Integer staffId, String userType, Integer merId, PageParamRequest pageParamRequest);

    /**
     * 转接会话
     */
    void transferSession(String sessionId, Integer targetStaffId, String reason);

    /**
     * 获取平台工作台数据
     */
    Map<String, Object> getPlatformWorkplaceData();

    /**
     * 获取平台统计数据
     */
    Map<String, Object> getPlatformServiceStatistics(String startDate, String endDate);

    /**
     * 获取平台队列信息
     */
    Map<String, Object> getPlatformQueueInfo();

    /**
     * 获取平台客服配置
     */
    CustomerServiceConfig getPlatformServiceConfig();

    /**
     * 更新平台客服配置
     */
    void updatePlatformServiceConfig(CustomerServiceConfig config);

    /**
     * 获取平台快捷回复模板
     */
    List<QuickReplyTemplate> getPlatformQuickReplyTemplates(String category);

    /**
     * 添加平台快捷回复模板
     */
    void addPlatformQuickReplyTemplate(QuickReplyTemplate template);

    /**
     * 更新平台快捷回复模板
     */
    void updatePlatformQuickReplyTemplate(QuickReplyTemplate template);

    /**
     * 删除平台快捷回复模板
     */
    void deletePlatformQuickReplyTemplate(Long templateId);

    /**
     * 发起平台与商户的聊天
     */
    Map<String, Object> startPlatformMerchantChat(Integer merchantId, String message);

    /**
     * 发起平台与用户的聊天
     */
    Map<String, Object> startPlatformUserChat(Integer userId, String message);

    /**
     * 获取可聊天的商户列表
     */
    CommonPage<Map<String, Object>> getChatableMerchants(String keyword, PageParamRequest pageParamRequest);

    /**
     * 获取可聊天的用户列表
     */
    CommonPage<Map<String, Object>> getChatableUsers(String keyword, PageParamRequest pageParamRequest);

    // ==================== 控制器新增接口所需方法 ====================

    /**
     * 获取用户资料
     */
    Map<String, Object> getUserProfile(Integer userId, String userType);

    /**
     * 更新会话设置
     */
    void updateSessionSettings(String sessionId, Map<String, Object> settings);

    /**
     * 转接会话（重载方法）
     */
    void transferSession(String sessionId, Map<String, Object> transferRequest);

    /**
     * 分配客服（重载方法）
     */
    void assignStaff(String sessionId, Integer staffId);

    /**
     * 添加快捷回复模板
     */
    void addQuickReplyTemplate(QuickReplyTemplate template);

    /**
     * 更新快捷回复模板
     */
    void updateQuickReplyTemplate(QuickReplyTemplate template);

    /**
     * 删除快捷回复模板
     */
    void deleteQuickReplyTemplate(Long templateId);

    /**
     * 发送商品消息（重载方法）
     */
    HumanServiceMessage sendProductMessage(Map<String, Object> request);

    /**
     * 发送订单消息（重载方法）
     */
    HumanServiceMessage sendOrderMessage(Map<String, Object> request);

    /**
     * 获取等待队列信息（重载方法）
     */
    Map<String, Object> getQueueInfo();

    /**
     * 获取客服配置
     */
    CustomerServiceConfig getServiceConfig();

    /**
     * 更新客服配置
     */
    void updateServiceConfig(CustomerServiceConfig config);
    
    /**
     * 处理小程序消息（非流式，通过WebSocket推送AI回复）
     */
    Map<String, Object> handleMessageForMiniProgram(EnterpriseChatMessageRequest request, LoginFrontUserVo currentUser, String cozeBotId);

    /**
     * 获取当前用户按商户分组的对话列表
     * @param userId 用户ID
     * @return 按商户分组的对话列表，包含商户信息和对话消息
     */
    List<Map<String, Object>> getUserConversationsGroupedByMerchant(Integer userId);
}
