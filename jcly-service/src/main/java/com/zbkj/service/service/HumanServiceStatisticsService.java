package com.zbkj.service.service;

import com.zbkj.common.response.humanservice.HumanServiceStatisticsResponse;

/**
 * 人工客服数据统计服务接口
 * @author AI Assistant
 * @since 2025-01-09
 */
public interface HumanServiceStatisticsService {

    /**
     * 获取人工客服综合统计数据
     * @param merId 商户ID
     * @param days 统计天数，默认7天
     * @return 统计数据
     */
    HumanServiceStatisticsResponse getComprehensiveStatistics(Long merId, Integer days);

    /**
     * 获取客服回复统计数据
     * @param merId 商户ID
     * @param days 统计天数
     * @return 客服回复统计列表
     */
    java.util.List<HumanServiceStatisticsResponse.StaffReplyStatistics> getStaffReplyStatistics(Long merId, Integer days);

    /**
     * 获取每日消息统计数据
     * @param merId 商户ID
     * @param days 统计天数
     * @return 每日消息统计列表
     */
    java.util.List<HumanServiceStatisticsResponse.DailyMessageStatistics> getDailyMessageStatistics(Long merId, Integer days);

    /**
     * 获取消息类型分布统计
     * @param merId 商户ID
     * @param days 统计天数
     * @return 消息类型统计列表
     */
    java.util.List<HumanServiceStatisticsResponse.MessageTypeStatistics> getMessageTypeStatistics(Long merId, Integer days);

    /**
     * 获取客服评价统计数据
     * @param merId 商户ID
     * @param days 统计天数
     * @return 客服评价统计列表
     */
    java.util.List<HumanServiceStatisticsResponse.StaffRatingStatistics> getStaffRatingStatistics(Long merId, Integer days);

    /**
     * 获取热门问题统计
     * @param merId 商户ID
     * @param days 统计天数
     * @param limit 返回数量限制
     * @return 热门问题统计列表
     */
    java.util.List<HumanServiceStatisticsResponse.PopularQuestionStatistics> getPopularQuestionStatistics(Long merId, Integer days, Integer limit);

    /**
     * 获取客服工作效率统计
     * @param merId 商户ID
     * @param staffId 客服ID（可选）
     * @param days 统计天数
     * @return 工作效率统计数据
     */
    java.util.Map<String, Object> getStaffEfficiencyStatistics(Long merId, Long staffId, Integer days);

    /**
     * 获取客户满意度趋势
     * @param merId 商户ID
     * @param days 统计天数
     * @return 满意度趋势数据
     */
    java.util.List<java.util.Map<String, Object>> getSatisfactionTrend(Long merId, Integer days);
}


