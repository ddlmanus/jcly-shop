package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.humanservice.HumanServiceRating;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 人工客服评价服务接口
 * @author AI Assistant
 * @since 2025-01-09
 */
public interface HumanServiceRatingService extends IService<HumanServiceRating> {

    /**
     * 用户评价客服服务
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param rating 评分（1-5）
     * @param comment 评价内容
     * @param tags 评价标签
     * @param detailScores 详细评分（服务态度、专业能力、响应速度、问题解决）
     * @param isAnonymous 是否匿名
     * @return 评价结果
     */
    boolean rateService(String sessionId, Long userId, Integer rating, String comment, 
                       String tags, Map<String, BigDecimal> detailScores, Boolean isAnonymous);

    /**
     * 获取评价管理列表
     * @param merId 商户ID
     * @param staffId 客服ID（可选）
     * @param ratingType 评价类型（可选）
     * @param status 状态（可选）
     * @param keyword 关键词搜索（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 分页评价列表
     */
    PageInfo<HumanServiceRating> getRatingManagementList(Long merId, Long staffId, 
                                                         Integer ratingType, Integer status, 
                                                         String keyword, Integer page, Integer size);

    /**
     * 批量审核评价
     * @param ratingIds 评价ID列表
     * @param status 审核状态
     * @return 审核结果
     */
    boolean batchAuditRatings(List<Long> ratingIds, Integer status);

    /**
     * 删除评价
     * @param ratingId 评价ID
     * @return 删除结果
     */
    boolean deleteRating(Long ratingId);

    /**
     * 获取评价详情
     * @param ratingId 评价ID
     * @return 评价详情
     */
    HumanServiceRating getRatingDetail(Long ratingId);

    /**
     * 获取客服评价统计
     * @param merId 商户ID
     * @param staffId 客服ID（可选）
     * @param days 统计天数
     * @return 评价统计数据
     */
    Map<String, Object> getStaffRatingStatistics(Long merId, Long staffId, Integer days);

    /**
     * 获取用户对指定会话的评价
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 评价信息，如果未评价则返回null
     */
    HumanServiceRating getUserRatingBySession(String sessionId, Long userId);

    /**
     * 检查用户是否已经评价过该会话
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 是否已评价
     */
    boolean hasUserRatedSession(String sessionId, Long userId);
}


