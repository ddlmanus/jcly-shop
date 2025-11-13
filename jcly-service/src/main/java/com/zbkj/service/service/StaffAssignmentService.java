package com.zbkj.service.service;

import java.util.List;
import java.util.Map;

/**
 * 客服分配服务接口
 * 智能分配客服，基于技能、工作负载、在线状态等因素
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
public interface StaffAssignmentService {

    /**
     * 智能分配客服
     * @param merchantId 商户ID
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param requiredSkills 需要的技能标签
     * @param urgency 紧急程度: low, medium, high
     * @param preferredStaffId 首选客服ID（可选）
     * @return 分配结果
     */
    Map<String, Object> assignBestStaff(Integer merchantId, Integer userId, String sessionId, 
                                       List<String> requiredSkills, String urgency, Integer preferredStaffId);

    /**
     * 根据规则分配客服
     * @param merchantId 商户ID
     * @param assignmentRule 分配规则: round_robin, least_busy, skill_based, vip_priority
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param context 上下文信息
     * @return 分配结果
     */
    Map<String, Object> assignByRule(Integer merchantId, String assignmentRule, String sessionId, 
                                    Integer userId, Map<String, Object> context);

    /**
     * 获取客服工作负载
     * @param staffId 客服ID
     * @return 工作负载信息
     */
    Map<String, Object> getStaffWorkload(Integer staffId);

    /**
     * 获取客服在线状态
     * @param staffId 客服ID
     * @return 在线状态信息
     */
    Map<String, Object> getStaffOnlineStatus(Integer staffId);

    /**
     * 获取客服技能标签
     * @param staffId 客服ID
     * @return 技能标签列表
     */
    List<String> getStaffSkills(Integer staffId);

    /**
     * 获取商户的所有客服
     * @param merchantId 商户ID
     * @param onlineOnly 是否只返回在线客服
     * @return 客服列表
     */
    List<Map<String, Object>> getMerchantStaff(Integer merchantId, boolean onlineOnly);

    /**
     * 计算客服匹配度
     * @param staffId 客服ID
     * @param requiredSkills 需要的技能
     * @param urgency 紧急程度
     * @param userLevel 用户等级
     * @return 匹配度分数 (0-100)
     */
    int calculateStaffMatchScore(Integer staffId, List<String> requiredSkills, 
                               String urgency, String userLevel);

    /**
     * 更新客服状态
     * @param staffId 客服ID
     * @param status 状态: online, offline, busy, away
     * @param maxConcurrentSessions 最大并发会话数
     * @return 更新结果
     */
    boolean updateStaffStatus(Integer staffId, String status, Integer maxConcurrentSessions);

    /**
     * 增加客服工作负载
     * @param staffId 客服ID
     * @param sessionId 会话ID
     * @return 是否成功
     */
    boolean addStaffWorkload(Integer staffId, String sessionId);

    /**
     * 减少客服工作负载
     * @param staffId 客服ID
     * @param sessionId 会话ID
     * @return 是否成功
     */
    boolean removeStaffWorkload(Integer staffId, String sessionId);

    /**
     * 获取分配统计
     * @param merchantId 商户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    Map<String, Object> getAssignmentStatistics(Integer merchantId, Long startTime, Long endTime);

    /**
     * 客服强制下线处理
     * @param staffId 客服ID
     * @param reason 下线原因
     * @return 处理结果
     */
    Map<String, Object> handleStaffForceOffline(Integer staffId, String reason);

    /**
     * 重新分配会话
     * @param sessionId 会话ID
     * @param reason 重新分配原因
     * @return 重新分配结果
     */
    Map<String, Object> reassignSession(String sessionId, String reason);

    /**
     * 检查客服是否可以接受新会话
     * @param staffId 客服ID
     * @return 是否可以接受
     */
    boolean canStaffAcceptNewSession(Integer staffId);

    /**
     * 获取客服当前处理的会话列表
     * @param staffId 客服ID
     * @return 会话列表
     */
    List<String> getStaffActiveSessions(Integer staffId);

    /**
     * 设置客服技能标签
     * @param staffId 客服ID
     * @param skills 技能标签列表
     * @return 设置结果
     */
    boolean setStaffSkills(Integer staffId, List<String> skills);

    /**
     * 获取等待分配的会话队列
     * @param merchantId 商户ID
     * @return 等待队列
     */
    List<Map<String, Object>> getPendingAssignmentQueue(Integer merchantId);
}
