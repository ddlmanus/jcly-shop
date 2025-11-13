package com.zbkj.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.order.CityDeliveryOrder;
import com.zbkj.common.response.CityDeliveryOrderResponse;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 配送订单服务接口
 * @author 荆楚粮油
 * @since 2024-01-15
 */
public interface CityDeliveryOrderService extends IService<CityDeliveryOrder> {

    /**
     * 根据订单号获取配送订单
     */
    CityDeliveryOrder getByOrderNo(String orderNo);

    /**
     * 根据配送订单号获取配送订单
     */
    CityDeliveryOrder getByDeliveryOrderNo(String deliveryOrderNo);

    /**
     * 根据配送员ID获取配送订单列表
     */
    List<CityDeliveryOrder> getByDriverId(Integer driverId);

    /**
     * 根据商户ID获取配送订单列表
     */
    List<CityDeliveryOrder> getByMerId(Integer merId);

    /**
     * 根据用户ID获取配送订单列表
     */
    List<CityDeliveryOrder> getByUserId(Integer userId);

    /**
     * 根据配送状态获取配送订单列表
     */
    List<CityDeliveryOrder> getByStatus(Integer status);

    /**
     * 获取配送员当前配送订单
     */
    List<CityDeliveryOrder> getDriverCurrentOrders(Integer driverId);

    /**
     * 获取待分配的配送订单
     */
    List<CityDeliveryOrder> getPendingOrders();

    /**
     * 更新配送订单状态
     */
    boolean updateOrderStatus(String deliveryOrderNo, Integer status, Date updateTime);

    /**
     * 分配配送员
     */
    boolean assignDriver(String deliveryOrderNo, Integer driverId, String driverName, String driverPhone);

    /**
     * 更新配送时间
     */
    boolean updateDeliveryTime(String deliveryOrderNo, Date pickupTime, Date deliveryTime);

    /**
     * 获取超时订单
     */
    List<CityDeliveryOrder> getTimeoutOrders();

    /**
     * 获取配送统计
     */
    Map<String, Object> getDeliveryStatistics(Integer merId);

    /**
     * 根据条件查询配送订单
     */
    List<CityDeliveryOrder> queryByConditions(Map<String, Object> conditions);

    /**
     * 批量更新配送状态
     */
    boolean batchUpdateStatus(List<String> deliveryOrderNos, Integer status);

    /**
     * 获取配送订单响应对象
     */
    CityDeliveryOrderResponse convertToResponse(CityDeliveryOrder order);

    /**
     * 根据日期范围获取配送订单
     */
    List<CityDeliveryOrder> getByDateRange(Date startDate, Date endDate);

    /**
     * 获取配送员今日订单
     */
    List<CityDeliveryOrder> getDriverTodayOrders(Integer driverId);

    /**
     * 统计配送员订单数量
     */
    long countDriverOrders(Integer driverId, Integer status);

    /**
     * 获取配送订单详情
     */
    Map<String, Object> getOrderDetail(String deliveryOrderNo);

    /**
     * 取消配送订单
     */
    boolean cancelOrder(String deliveryOrderNo, String cancelReason);

    /**
     * 异常处理
     */
    boolean handleException(String deliveryOrderNo, String exceptionReason);

    List<CityDeliveryOrder> getList(Page<CityDeliveryOrder> pageRequest, LambdaQueryWrapper<CityDeliveryOrder> wrapper);
} 