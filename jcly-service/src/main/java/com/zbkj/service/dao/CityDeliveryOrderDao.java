package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.order.CityDeliveryOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 同城配送订单表 Mapper 接口
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
public interface CityDeliveryOrderDao extends BaseMapper<CityDeliveryOrder> {

    /**
     * 根据订单号获取配送订单
     */
    CityDeliveryOrder getOrderByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据配送订单号获取配送订单
     */
    CityDeliveryOrder getOrderByDeliveryOrderNo(@Param("deliveryOrderNo") String deliveryOrderNo);

    /**
     * 根据配送员ID获取配送订单
     */
    List<CityDeliveryOrder> getOrdersByDriverId(@Param("driverId") Integer driverId);

    /**
     * 根据商户ID获取配送订单
     */
    List<CityDeliveryOrder> getOrdersByMerId(@Param("merId") Integer merId);

    /**
     * 根据用户ID获取配送订单
     */
    List<CityDeliveryOrder> getOrdersByUserId(@Param("userId") Integer userId);

    /**
     * 根据配送状态获取配送订单
     */
    List<CityDeliveryOrder> getOrdersByStatus(@Param("status") Integer status);

    /**
     * 获取配送员当前配送订单
     */
    List<CityDeliveryOrder> getDriverCurrentOrders(@Param("driverId") Integer driverId);

    /**
     * 获取待分配的配送订单
     */
    List<CityDeliveryOrder> getPendingOrders();

    /**
     * 更新配送订单状态
     */
    int updateOrderStatus(@Param("deliveryOrderNo") String deliveryOrderNo,
                        @Param("status") Integer status,
                        @Param("updateTime") Date updateTime);

    /**
     * 分配配送员
     */
    int assignDriver(@Param("deliveryOrderNo") String deliveryOrderNo,
                   @Param("driverId") Integer driverId,
                   @Param("driverName") String driverName,
                   @Param("driverPhone") String driverPhone);

    /**
     * 更新配送时间
     */
    int updateDeliveryTime(@Param("deliveryOrderNo") String deliveryOrderNo,
                         @Param("pickupTime") Date pickupTime,
                         @Param("deliveryTime") Date deliveryTime);

    /**
     * 获取超时的配送订单
     */
    List<CityDeliveryOrder> getTimeoutOrders(@Param("timeoutMinutes") Integer timeoutMinutes);

    /**
     * 获取配送统计信息
     */
    CityDeliveryOrder getOrderWithStats(@Param("deliveryOrderNo") String deliveryOrderNo);

    /**
     * 根据时间范围获取配送订单
     */
    List<CityDeliveryOrder> getOrdersByTimeRange(@Param("startTime") Date startTime,
                                               @Param("endTime") Date endTime);

    /**
     * 获取配送员今日订单数
     */
    Integer getDriverTodayOrderCount(@Param("driverId") Integer driverId);

    /**
     * 获取商户今日配送订单
     */
    List<CityDeliveryOrder> getMerTodayOrders(@Param("merId") Integer merId);

    /**
     * 批量更新配送订单状态
     */
    int batchUpdateOrderStatus(@Param("orderIds") List<Integer> orderIds,
                             @Param("status") Integer status);

    /**
     * 根据配送类型获取订单
     */
    List<CityDeliveryOrder> getOrdersByDeliveryType(@Param("deliveryType") Integer deliveryType);
} 