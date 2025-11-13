package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.order.CityDeliveryCustomerRating;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 配送员客户评价服务接口
 * @author 荆楚粮油
 * @since 2024-01-15
 */
public interface CityDeliveryCustomerRatingService extends IService<CityDeliveryCustomerRating> {

    /**
     * 创建客户评价
     */
    boolean createRating(CityDeliveryCustomerRating rating);

    /**
     * 根据配送订单号获取评价
     */
    CityDeliveryCustomerRating getRatingByDeliveryOrderNo(String deliveryOrderNo);

    /**
     * 根据配送员ID获取评价列表
     */
    List<CityDeliveryCustomerRating> getRatingsByDriverId(Integer driverId, Date startDate, Date endDate);

    /**
     * 计算配送员平均评分
     */
    BigDecimal calculateDriverAverageRating(Integer driverId);

    /**
     * 获取配送员评价统计
     */
    Map<String, Object> getDriverRatingStats(Integer driverId);

    /**
     * 获取配送员最新评价
     */
    List<CityDeliveryCustomerRating> getLatestRatings(Integer driverId, Integer limit);

    /**
     * 更新评价审核状态
     */
    boolean updateAuditStatus(Integer ratingId, Integer auditStatus, String auditRemark, Integer auditAdminId);

    /**
     * 获取待审核评价列表
     */
    List<CityDeliveryCustomerRating> getPendingAuditRatings();

    /**
     * 计算系统自动评分
     */
    BigDecimal calculateSystemRating(String deliveryOrderNo);

    /**
     * 更新最终评分
     */
    boolean updateFinalRating(Integer ratingId);

    /**
     * 检查客户是否已评价
     */
    boolean hasCustomerRated(String deliveryOrderNo, Integer customerId);

    /**
     * 获取配送员好评率
     */
    BigDecimal getDriverGoodRatingRate(Integer driverId);

    /**
     * 获取配送员差评列表
     */
    List<CityDeliveryCustomerRating> getDriverPoorRatings(Integer driverId);

    /**
     * 批量处理评价
     */
    boolean batchProcessRatings(List<Integer> ratingIds, Integer auditStatus, String auditRemark, Integer auditAdminId);

    /**
     * 获取评价详情（包含配送信息）
     */
    Map<String, Object> getRatingDetailWithDeliveryInfo(Integer ratingId);

    /**
     * 根据多个配送订单号获取评价
     */
    List<CityDeliveryCustomerRating> getRatingsByDeliveryOrderNos(List<String> deliveryOrderNos);

    /**
     * 计算配送员各维度评分统计
     */
    Map<String, BigDecimal> getDriverRatingDimensionStats(Integer driverId);

    /**
     * 获取评价趋势数据
     */
    List<Map<String, Object>> getRatingTrendData(Integer driverId, Date startDate, Date endDate);

    /**
     * 处理投诉评价
     */
    boolean handleComplaintRating(Integer ratingId, String handlerResult);

    /**
     * 获取高质量评价（用于展示）
     */
    List<CityDeliveryCustomerRating> getHighQualityRatings(Integer driverId, Integer limit);

    /**
     * 自动创建评价记录（当订单完成时）
     */
    boolean autoCreateRatingRecord(String deliveryOrderNo);

    /**
     * 计算评价权重
     */
    BigDecimal calculateRatingWeight(Integer customerId);
} 