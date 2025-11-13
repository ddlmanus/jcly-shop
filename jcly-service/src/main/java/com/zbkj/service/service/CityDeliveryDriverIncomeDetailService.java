package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.order.CityDeliveryDriverIncomeDetail;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 配送员收入明细服务接口
 * @author 荆楚粮油
 * @since 2024-01-15
 */
public interface CityDeliveryDriverIncomeDetailService extends IService<CityDeliveryDriverIncomeDetail> {

    /**
     * 创建收入明细记录
     */
    boolean createIncomeDetail(CityDeliveryDriverIncomeDetail incomeDetail);

    /**
     * 根据配送订单号获取收入明细
     */
    CityDeliveryDriverIncomeDetail getByDeliveryOrderNo(String deliveryOrderNo);

    /**
     * 根据配送员ID获取收入明细列表
     */
    List<CityDeliveryDriverIncomeDetail> getByDriverId(Integer driverId, Date startDate, Date endDate);

    /**
     * 计算配送员总收入
     */
    BigDecimal calculateTotalIncome(Integer driverId, Date startDate, Date endDate);

    /**
     * 计算配送员月收入
     */
    BigDecimal calculateMonthlyIncome(Integer driverId, Integer year, Integer month);

    /**
     * 获取配送员收入统计
     */
    Map<String, Object> getIncomeStats(Integer driverId);

    /**
     * 批量结算配送员收入
     */
    boolean batchSettlement(List<Integer> incomeDetailIds);

    /**
     * 更新结算状态
     */
    boolean updateSettlementStatus(Integer id, Integer status);

    /**
     * 获取未结算收入明细
     */
    List<CityDeliveryDriverIncomeDetail> getUnsettledIncomes(Integer driverId);

    /**
     * 根据配送员和日期范围获取收入明细
     */
    List<CityDeliveryDriverIncomeDetail> getIncomeDetailsByDateRange(Integer driverId, Date startDate, Date endDate);

    /**
     * 计算配送员日收入
     */
    BigDecimal calculateDailyIncome(Integer driverId, Date date);

    /**
     * 获取配送员收入趋势数据
     */
    List<Map<String, Object>> getIncomeTrend(Integer driverId, Date startDate, Date endDate);

    /**
     * 更新客户评分和评价
     */
    boolean updateCustomerRating(String deliveryOrderNo, BigDecimal rating, String comment);

    /**
     * 计算奖励和扣除
     */
    Map<String, BigDecimal> calculateBonusAndDeduction(CityDeliveryDriverIncomeDetail incomeDetail);

    /**
     * 自动计算收入明细
     */
    boolean autoCalculateIncomeDetail(String deliveryOrderNo);

    /**
     * 获取配送员排行榜数据
     */
    List<Map<String, Object>> getDriverRankingData(Date startDate, Date endDate, Integer limit);

    /**
     * 计算平台收入分成
     */
    BigDecimal calculatePlatformIncome(BigDecimal totalFee, BigDecimal driverIncome);

    /**
     * 获取收入明细统计报表
     */
    Map<String, Object> getIncomeStatisticsReport(Date startDate, Date endDate);
} 