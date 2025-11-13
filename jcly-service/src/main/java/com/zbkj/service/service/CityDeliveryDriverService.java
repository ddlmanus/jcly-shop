package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.order.CityDeliveryDriver;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.CityDeliveryDriverResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 同城配送员服务接口
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
public interface CityDeliveryDriverService extends IService<CityDeliveryDriver> {

    /**
     * 获取配送员分页列表
     */
    List<CityDeliveryDriverResponse> getList(Map<String, Object> params, PageParamRequest pageParamRequest);

    /**
     * 获取配送员列表（简单版本，用于分页）
     */
    List<CityDeliveryDriver> getList(PageParamRequest pageParamRequest);

    /**
     * 新增配送员
     */
    Boolean create(CityDeliveryDriver driver);

    /**
     * 更新配送员信息
     */
    Boolean updateDriver(CityDeliveryDriver driver);

    /**
     * 删除配送员
     */
    Boolean delete(Integer driverId);

    /**
     * 根据ID获取配送员详情
     */
    CityDeliveryDriverResponse getDriverDetail(Integer driverId);

    /**
     * 更新配送员状态
     */
    Boolean updateStatus(Integer driverId, Integer status);

    /**
     * 批量更新配送员状态
     */
    Boolean batchUpdateStatus(List<Integer> driverIds, Integer status);

    /**
     * 配送员认证审核
     */
    Boolean certification(Integer driverId, Integer certificationStatus, String remark);

    /**
     * 根据位置获取附近的配送员
     */
    List<CityDeliveryDriverResponse> getNearbyDrivers(BigDecimal longitude, BigDecimal latitude, BigDecimal radius);

    /**
     * 获取在线配送员列表
     */
    List<CityDeliveryDriverResponse> getOnlineDrivers();

    /**
     * 获取可用配送员列表
     */
    List<CityDeliveryDriverResponse> getAvailableDrivers();

    /**
     * 根据区域获取配送员
     */
    List<CityDeliveryDriverResponse> getDriversByArea(String workArea);

    /**
     * 更新配送员位置
     */
    Boolean updateDriverLocation(Integer driverId, BigDecimal longitude, BigDecimal latitude, String address);

    /**
     * 获取配送员工作统计
     */
    Map<String, Object> getDriverWorkStats(Integer driverId);

    /**
     * 根据手机号获取配送员
     */
    CityDeliveryDriver getDriverByPhone(String phone);

    /**
     * 根据配送员编号获取配送员
     */
    CityDeliveryDriver getDriverByCode(String driverCode);

    /**
     * 生成配送员编号
     */
    String generateDriverCode();

    /**
     * 更新配送员订单数
     */
    Boolean updateDriverOrderCount(Integer driverId, Integer increment);

    /**
     * 更新配送员收入
     */
    Boolean updateDriverIncome(Integer driverId, BigDecimal income);

    /**
     * 获取配送员评分
     */
    BigDecimal getDriverRating(Integer driverId);

    /**
     * 更新配送员评分
     */
    Boolean updateDriverRating(Integer driverId, BigDecimal rating);

    /**
     * 检查配送员是否可以接单
     */
    Boolean checkDriverCanTakeOrder(Integer driverId);

    /**
     * 获取配送员当前订单数
     */
    Integer getDriverCurrentOrderCount(Integer driverId);

    /**
     * 根据配送能力获取最佳配送员
     */
    CityDeliveryDriver getBestDriver(BigDecimal pickupLongitude, BigDecimal pickupLatitude, BigDecimal deliveryLongitude, BigDecimal deliveryLatitude);

    /**
     * 配送员上线
     */
    Boolean driverOnline(Integer driverId);

    /**
     * 配送员下线
     */
    Boolean driverOffline(Integer driverId);

    /**
     * 获取配送员今日工作统计
     */
    Map<String, Object> getDriverTodayStats(Integer driverId);

    /**
     * 获取配送员月度工作统计
     */
    Map<String, Object> getDriverMonthStats(Integer driverId);

    /**
     * 根据位置获取附近可用配送员ID列表
     */
    List<Integer> getAvailableDriversNearby(BigDecimal longitude, BigDecimal latitude, BigDecimal radius);

    /**
     * 增加配送员当前订单数
     */
    Boolean incrementCurrentOrders(Integer driverId);

    /**
     * 减少配送员当前订单数
     */
    Boolean decrementCurrentOrders(Integer driverId);

    /**
     * 根据多个条件获取可用配送员
     */
    List<CityDeliveryDriverResponse> getAvailableDriversWithConditions(Map<String, Object> conditions);

    /**
     * 批量更新配送员位置
     */
    Boolean batchUpdateDriverLocation(List<Map<String, Object>> locationData);

    /**
     * 获取配送员实时状态
     */
    Map<String, Object> getDriverRealtimeStatus(Integer driverId);

    /**
     * 配送员签到
     */
    Boolean driverCheckIn(Integer driverId, BigDecimal longitude, BigDecimal latitude);

    /**
     * 配送员签退
     */
    Boolean driverCheckOut(Integer driverId);

    /**
     * 获取配送员配送范围内的区域
     */
    List<Integer> getDriverCoverageAreas(Integer driverId);

    /**
     * 根据状态获取配送员列表
     */
    List<CityDeliveryDriver> getByStatus(Integer status);

    /**
     * 获取忙碌的配送员列表
     */
    List<CityDeliveryDriver> getBusyDrivers();

    /**
     * 获取离线的配送员列表
     */
    List<CityDeliveryDriver> getOfflineDrivers();
} 