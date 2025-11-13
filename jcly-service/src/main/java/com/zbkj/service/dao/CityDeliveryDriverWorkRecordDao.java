package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.order.CityDeliveryDriverWorkRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 配送员工作记录DAO接口
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
@Mapper
public interface CityDeliveryDriverWorkRecordDao extends BaseMapper<CityDeliveryDriverWorkRecord> {

    /**
     * 根据配送员ID和日期获取工作记录
     *
     * @param driverId 配送员ID
     * @param workDate 工作日期
     * @return 工作记录
     */
    CityDeliveryDriverWorkRecord getByDriverIdAndDate(@Param("driverId") Integer driverId, 
                                                      @Param("workDate") Date workDate);

    /**
     * 获取配送员指定时间范围的工作记录
     *
     * @param driverId  配送员ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 工作记录列表
     */
    List<CityDeliveryDriverWorkRecord> getByDriverIdAndDateRange(@Param("driverId") Integer driverId,
                                                                @Param("startDate") Date startDate,
                                                                @Param("endDate") Date endDate);

    /**
     * 获取配送员月度工作统计
     *
     * @param driverId 配送员ID
     * @param year     年份
     * @param month    月份
     * @return 统计数据
     */
    Map<String, Object> getDriverMonthlyStats(@Param("driverId") Integer driverId,
                                             @Param("year") Integer year,
                                             @Param("month") Integer month);

    /**
     * 获取配送员年度工作统计
     *
     * @param driverId 配送员ID
     * @param year     年份
     * @return 统计数据
     */
    Map<String, Object> getDriverYearlyStats(@Param("driverId") Integer driverId,
                                            @Param("year") Integer year);

    /**
     * 更新工作记录的订单统计
     *
     * @param driverId      配送员ID
     * @param workDate      工作日期
     * @param totalOrders   总订单数增量
     * @param completedOrders 完成订单数增量
     * @param cancelledOrders 取消订单数增量
     * @return 更新行数
     */
    int updateOrderStats(@Param("driverId") Integer driverId,
                        @Param("workDate") Date workDate,
                        @Param("totalOrders") Integer totalOrders,
                        @Param("completedOrders") Integer completedOrders,
                        @Param("cancelledOrders") Integer cancelledOrders);

    /**
     * 更新工作记录的收入统计
     *
     * @param driverId         配送员ID
     * @param workDate         工作日期
     * @param totalIncome      总收入增量
     * @param baseIncome       基础收入增量
     * @param commissionIncome 佣金收入增量
     * @param bonusIncome      奖励收入增量
     * @return 更新行数
     */
    int updateIncomeStats(@Param("driverId") Integer driverId,
                         @Param("workDate") Date workDate,
                         @Param("totalIncome") BigDecimal totalIncome,
                         @Param("baseIncome") BigDecimal baseIncome,
                         @Param("commissionIncome") BigDecimal commissionIncome,
                         @Param("bonusIncome") BigDecimal bonusIncome);

    /**
     * 更新工作记录的距离和时间统计
     *
     * @param driverId       配送员ID
     * @param workDate       工作日期
     * @param distance       配送距离增量
     * @param maxDistance    最大配送距离
     * @param avgDeliveryTime 平均配送时间
     * @return 更新行数
     */
    int updateDistanceStats(@Param("driverId") Integer driverId,
                           @Param("workDate") Date workDate,
                           @Param("distance") BigDecimal distance,
                           @Param("maxDistance") BigDecimal maxDistance,
                           @Param("avgDeliveryTime") Integer avgDeliveryTime);

    /**
     * 更新工作记录的评分统计
     *
     * @param driverId    配送员ID
     * @param workDate    工作日期
     * @param totalRating 总评分增量
     * @param ratingCount 评价次数增量
     * @return 更新行数
     */
    int updateRatingStats(@Param("driverId") Integer driverId,
                         @Param("workDate") Date workDate,
                         @Param("totalRating") BigDecimal totalRating,
                         @Param("ratingCount") Integer ratingCount);

    /**
     * 更新工作记录的异常统计
     *
     * @param driverId       配送员ID
     * @param workDate       工作日期
     * @param violationCount 违规次数增量
     * @param exceptionCount 异常次数增量
     * @param timeoutCount   超时次数增量
     * @param complaintCount 投诉次数增量
     * @return 更新行数
     */
    int updateExceptionStats(@Param("driverId") Integer driverId,
                            @Param("workDate") Date workDate,
                            @Param("violationCount") Integer violationCount,
                            @Param("exceptionCount") Integer exceptionCount,
                            @Param("timeoutCount") Integer timeoutCount,
                            @Param("complaintCount") Integer complaintCount);

    /**
     * 获取配送员最近N天的工作记录
     *
     * @param driverId 配送员ID
     * @param days     天数
     * @return 工作记录列表
     */
    List<CityDeliveryDriverWorkRecord> getRecentWorkRecords(@Param("driverId") Integer driverId,
                                                           @Param("days") Integer days);

    /**
     * 获取优秀配送员列表（按效率排序）
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param limit     限制数量
     * @return 配送员工作记录列表
     */
    List<CityDeliveryDriverWorkRecord> getTopPerformingDrivers(@Param("startDate") Date startDate,
                                                              @Param("endDate") Date endDate,
                                                              @Param("limit") Integer limit);

    /**
     * 获取配送员收入统计（按时间范围）
     *
     * @param driverId  配送员ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 收入统计
     */
    Map<String, Object> getDriverIncomeStats(@Param("driverId") Integer driverId,
                                           @Param("startDate") Date startDate,
                                           @Param("endDate") Date endDate);

    /**
     * 批量创建或更新工作记录
     *
     * @param records 工作记录列表
     * @return 影响行数
     */
    int batchInsertOrUpdate(@Param("records") List<CityDeliveryDriverWorkRecord> records);
} 