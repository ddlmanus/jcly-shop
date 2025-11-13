package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.order.CityDeliveryDriverWorkRecord;
import com.zbkj.common.request.PageParamRequest;
import com.github.pagehelper.PageInfo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 配送员工作记录服务接口
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
public interface CityDeliveryDriverWorkRecordService extends IService<CityDeliveryDriverWorkRecord> {

    /**
     * 创建或更新配送员工作记录
     *
     * @param driverId 配送员ID
     * @param workDate 工作日期
     * @return 工作记录
     */
    CityDeliveryDriverWorkRecord createOrUpdateWorkRecord(Integer driverId, Date workDate);

    /**
     * 配送员签到
     *
     * @param driverId 配送员ID
     * @return 是否成功
     */
    Boolean driverCheckIn(Integer driverId);

    /**
     * 配送员签退
     *
     * @param driverId 配送员ID
     * @return 是否成功
     */
    Boolean driverCheckOut(Integer driverId);

    /**
     * 更新订单完成统计
     *
     * @param driverId    配送员ID
     * @param isCompleted 是否完成
     * @param income      收入
     * @param distance    配送距离
     * @param deliveryTime 配送时间（分钟）
     * @return 是否成功
     */
    Boolean updateOrderStats(Integer driverId, Boolean isCompleted, BigDecimal income, 
                           BigDecimal distance, Integer deliveryTime);

    /**
     * 更新配送员评分
     *
     * @param driverId 配送员ID
     * @param rating   评分
     * @return 是否成功
     */
    Boolean updateDriverRating(Integer driverId, BigDecimal rating);

    /**
     * 记录异常事件
     *
     * @param driverId      配送员ID
     * @param exceptionType 异常类型（1-违规，2-异常，3-超时，4-投诉）
     * @return 是否成功
     */
    Boolean recordException(Integer driverId, Integer exceptionType);

    /**
     * 获取配送员工作记录分页列表
     *
     * @param driverId         配送员ID
     * @param startDate        开始日期
     * @param endDate          结束日期
     * @param pageParamRequest 分页参数
     * @return 分页结果
     */
    PageInfo<CityDeliveryDriverWorkRecord> getWorkRecordPage(Integer driverId, String startDate, 
                                                           String endDate, PageParamRequest pageParamRequest);

    /**
     * 获取配送员收入统计
     *
     * @param driverId  配送员ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 收入统计
     */
    Map<String, Object> getDriverIncomeStats(Integer driverId, String startDate, String endDate);

    /**
     * 获取配送员工作统计
     *
     * @param driverId 配送员ID
     * @param days     统计天数
     * @return 工作统计
     */
    Map<String, Object> getDriverWorkStats(Integer driverId, Integer days);

    /**
     * 获取配送员月度统计
     *
     * @param driverId 配送员ID
     * @param year     年份
     * @param month    月份
     * @return 月度统计
     */
    Map<String, Object> getDriverMonthlyStats(Integer driverId, Integer year, Integer month);

    /**
     * 获取配送员年度统计
     *
     * @param driverId 配送员ID
     * @param year     年份
     * @return 年度统计
     */
    Map<String, Object> getDriverYearlyStats(Integer driverId, Integer year);

    /**
     * 获取优秀配送员排行榜
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param limit     限制数量
     * @return 排行榜
     */
    List<Map<String, Object>> getTopPerformingDrivers(String startDate, String endDate, Integer limit);

    /**
     * 获取配送员效率评级
     *
     * @param driverId 配送员ID
     * @param workDate 工作日期
     * @return 效率评级
     */
    String calculateEfficiencyRating(Integer driverId, Date workDate);

    /**
     * 批量生成工作记录
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 生成数量
     */
    Integer batchGenerateWorkRecords(Date startDate, Date endDate);

    /**
     * 获取配送员当日工作记录
     *
     * @param driverId 配送员ID
     * @return 工作记录
     */
    CityDeliveryDriverWorkRecord getTodayWorkRecord(Integer driverId);

    /**
     * 获取配送员最近工作记录
     *
     * @param driverId 配送员ID
     * @param days     天数
     * @return 工作记录列表
     */
    List<CityDeliveryDriverWorkRecord> getRecentWorkRecords(Integer driverId, Integer days);

    /**
     * 计算配送员工作时长
     *
     * @param checkInTime  签到时间
     * @param checkOutTime 签退时间
     * @return 工作时长（小时）
     */
    BigDecimal calculateWorkHours(Date checkInTime, Date checkOutTime);

    /**
     * 更新在线时长
     *
     * @param driverId     配送员ID
     * @param onlineMinutes 在线时长（分钟）
     * @return 是否成功
     */
    Boolean updateOnlineTime(Integer driverId, Integer onlineMinutes);

    /**
     * 生成工作报告
     *
     * @param driverId  配送员ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 工作报告
     */
    Map<String, Object> generateWorkReport(Integer driverId, String startDate, String endDate);
} 