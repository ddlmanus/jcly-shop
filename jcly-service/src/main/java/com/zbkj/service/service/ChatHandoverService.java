package com.zbkj.service.service;

import java.util.List;
import java.util.Map;

/**
 * AI转人工客服服务接口
 * 处理AI与人工客服之间的无缝切换
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
public interface ChatHandoverService {

    /**
     * 请求转人工客服
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param reason 转人工原因
     * @param urgency 紧急程度: low, medium, high
     * @return 转接结果
     */
    Map<String, Object> requestHandoverToHuman(String sessionId, Integer userId, String reason, String urgency);

    /**
     * 自动判断是否需要转人工
     * @param userMessage 用户消息内容
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 是否需要转人工及原因
     */
    Map<String, Object> shouldHandoverToHuman(String userMessage, String sessionId, Integer userId);

    /**
     * 分配客服
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param merchantId 商户ID
     * @param skillTags 技能标签
     * @param urgency 紧急程度
     * @return 分配结果
     */
    Map<String, Object> assignStaff(String sessionId, Integer userId, Integer merchantId, 
                                   List<String> skillTags, String urgency);

    /**
     * 客服接受转接
     * @param sessionId 会话ID
     * @param staffId 客服ID
     * @return 接受结果
     */
    Map<String, Object> acceptHandover(String sessionId, Integer staffId);

    /**
     * 客服拒绝转接
     * @param sessionId 会话ID
     * @param staffId 客服ID
     * @param reason 拒绝原因
     * @return 拒绝结果
     */
    Map<String, Object> rejectHandover(String sessionId, Integer staffId, String reason);

    /**
     * 完成转接，切换到人工模式
     * @param sessionId 会话ID
     * @param staffId 客服ID
     * @return 完成结果
     */
    Map<String, Object> completeHandover(String sessionId, Integer staffId);

    /**
     * 结束人工客服，切换回AI模式
     * @param sessionId 会话ID
     * @param staffId 客服ID
     * @param summary 服务总结
     * @return 结束结果
     */
    Map<String, Object> endHumanService(String sessionId, Integer staffId, String summary);

    /**
     * 获取会话的转接状态
     * @param sessionId 会话ID
     * @return 转接状态信息
     */
    Map<String, Object> getHandoverStatus(String sessionId);

    /**
     * 获取客服工作负载
     * @param staffId 客服ID
     * @return 工作负载信息
     */
    Map<String, Object> getStaffWorkload(Integer staffId);

    /**
     * 获取可用客服列表
     * @param merchantId 商户ID
     * @param skillTags 技能标签
     * @param urgency 紧急程度
     * @return 可用客服列表
     */
    List<Map<String, Object>> getAvailableStaff(Integer merchantId, List<String> skillTags, String urgency);

    /**
     * 更新会话服务模式
     * @param sessionId 会话ID
     * @param serviceMode AI或HUMAN
     * @param staffId 客服ID（人工模式时必填）
     * @return 更新结果
     */
    boolean updateSessionServiceMode(String sessionId, String serviceMode, Integer staffId);

    /**
     * 发送转接通知
     * @param sessionId 会话ID
     * @param notificationType 通知类型
     * @param targetUserId 目标用户ID
     * @param message 通知消息
     */
    void sendHandoverNotification(String sessionId, String notificationType, Integer targetUserId, String message);

    /**
     * 记录转接历史
     * @param sessionId 会话ID
     * @param fromMode 来源模式
     * @param toMode 目标模式
     * @param staffId 客服ID
     * @param reason 转接原因
     * @param duration 持续时间
     */
    void recordHandoverHistory(String sessionId, String fromMode, String toMode, 
                              Integer staffId, String reason, Long duration);

    /**
     * 获取转接统计数据
     * @param merchantId 商户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    Map<String, Object> getHandoverStatistics(Integer merchantId, Long startTime, Long endTime);
}
