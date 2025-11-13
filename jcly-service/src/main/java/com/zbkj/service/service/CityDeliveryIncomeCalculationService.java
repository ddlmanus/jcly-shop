package com.zbkj.service.service;

import com.zbkj.common.model.order.CityDeliveryDriverIncomeDetail;
import com.zbkj.common.model.order.CityDeliveryOrder;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 配送员收入计算服务接口
 * +----------------------------------------------------------------------
 * | JCLY [ JCLY赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 * +----------------------------------------------------------------------
 * | Author: dudl
 * +----------------------------------------------------------------------
 */
public interface CityDeliveryIncomeCalculationService {

    /**
     * 计算配送员收入明细
     * 
     * @param deliveryOrder 配送订单
     * @return 收入明细
     */
    CityDeliveryDriverIncomeDetail calculateDriverIncome(CityDeliveryOrder deliveryOrder);

    /**
     * 计算基础配送费
     * 
     * @param distance 配送距离（公里）
     * @param deliveryType 配送类型（1-即时，2-预约）
     * @param areaId 配送区域ID
     * @return 基础配送费
     */
    BigDecimal calculateBaseFee(BigDecimal distance, Integer deliveryType, Integer areaId);

    /**
     * 计算距离费用
     * 
     * @param distance 配送距离（公里）
     * @param feeRuleId 费用规则ID
     * @return 距离费用
     */
    BigDecimal calculateDistanceFee(BigDecimal distance, Integer feeRuleId);

    /**
     * 计算时间费用（夜间、节假日等）
     * 
     * @param deliveryOrder 配送订单
     * @return 时间费用
     */
    BigDecimal calculateTimeFee(CityDeliveryOrder deliveryOrder);

    /**
     * 计算加急费用
     * 
     * @param deliveryOrder 配送订单
     * @return 加急费用
     */
    BigDecimal calculateUrgentFee(CityDeliveryOrder deliveryOrder);

    /**
     * 计算配送员基础收入
     * 
     * @param deliveryFee 配送费用
     * @param feeRuleId 费用规则ID
     * @return 基础收入
     */
    BigDecimal calculateBaseIncome(BigDecimal deliveryFee, Integer feeRuleId);

    /**
     * 计算配送员佣金收入
     * 
     * @param deliveryFee 配送费用
     * @param commissionRate 佣金比例
     * @return 佣金收入
     */
    BigDecimal calculateCommissionIncome(BigDecimal deliveryFee, BigDecimal commissionRate);

    /**
     * 计算距离奖励
     * 
     * @param distance 配送距离
     * @param driverId 配送员ID
     * @return 距离奖励
     */
    BigDecimal calculateDistanceBonus(BigDecimal distance, Integer driverId);

    /**
     * 计算时效奖励
     * 
     * @param deliveryOrder 配送订单
     * @param actualDuration 实际配送时长（分钟）
     * @return 时效奖励
     */
    BigDecimal calculateTimeBonus(CityDeliveryOrder deliveryOrder, Integer actualDuration);

    /**
     * 计算评分奖励
     * 
     * @param customerRating 客户评分
     * @param deliveryFee 配送费用
     * @return 评分奖励
     */
    BigDecimal calculateRatingBonus(BigDecimal customerRating, BigDecimal deliveryFee);

    /**
     * 计算平台收入
     * 
     * @param deliveryFee 配送费用
     * @param driverIncome 配送员收入
     * @return 平台收入
     */
    BigDecimal calculatePlatformIncome(BigDecimal deliveryFee, BigDecimal driverIncome);

    /**
     * 计算商户承担费用
     * 
     * @param deliveryOrder 配送订单
     * @return 商户承担费用
     */
    BigDecimal calculateMerchantCost(CityDeliveryOrder deliveryOrder);

    /**
     * 计算用户支付费用
     * 
     * @param deliveryOrder 配送订单
     * @return 用户支付费用
     */
    BigDecimal calculateUserPayment(CityDeliveryOrder deliveryOrder);

    /**
     * 计算补贴金额
     * 
     * @param deliveryOrder 配送订单
     * @param driverIncome 配送员收入
     * @return 补贴金额
     */
    BigDecimal calculateSubsidyAmount(CityDeliveryOrder deliveryOrder, BigDecimal driverIncome);

    /**
     * 获取配送员佣金比例
     * 
     * @param driverId 配送员ID
     * @param areaId 配送区域ID
     * @return 佣金比例
     */
    BigDecimal getDriverCommissionRate(Integer driverId, Integer areaId);

    /**
     * 检查是否为夜间配送
     * 
     * @param deliveryOrder 配送订单
     * @return 是否夜间配送
     */
    Boolean isNightDelivery(CityDeliveryOrder deliveryOrder);

    /**
     * 检查是否为节假日配送
     * 
     * @param deliveryOrder 配送订单
     * @return 是否节假日配送
     */
    Boolean isHolidayDelivery(CityDeliveryOrder deliveryOrder);

    /**
     * 检查是否为恶劣天气配送
     * 
     * @param deliveryOrder 配送订单
     * @return 是否恶劣天气配送
     */
    Boolean isBadWeatherDelivery(CityDeliveryOrder deliveryOrder);

    /**
     * 获取配送费用明细
     * 
     * @param deliveryOrder 配送订单
     * @return 费用明细
     */
    Map<String, BigDecimal> getDeliveryFeeBreakdown(CityDeliveryOrder deliveryOrder);

    /**
     * 获取配送员收入明细
     * 
     * @param deliveryOrder 配送订单
     * @return 收入明细
     */
    Map<String, BigDecimal> getDriverIncomeBreakdown(CityDeliveryOrder deliveryOrder);

    /**
     * 验证收入计算结果
     * 
     * @param incomeDetail 收入明细
     * @return 验证结果
     */
    Boolean validateIncomeCalculation(CityDeliveryDriverIncomeDetail incomeDetail);

    /**
     * 重新计算收入明细
     * 
     * @param deliveryOrderNo 配送订单号
     * @return 重新计算的收入明细
     */
    CityDeliveryDriverIncomeDetail recalculateIncome(String deliveryOrderNo);

    /**
     * 批量计算收入明细
     * 
     * @param deliveryOrderNos 配送订单号列表
     * @return 计算成功数量
     */
    Integer batchCalculateIncome(java.util.List<String> deliveryOrderNos);
} 