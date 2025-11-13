package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.order.CityDeliveryOrder;
import com.zbkj.common.response.CityDeliveryOrderResponse;
import com.zbkj.common.request.CityDeliveryOrderRequest;
import com.zbkj.common.request.CityDeliveryOrderListRequest;
import com.github.pagehelper.PageInfo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 同城配送服务接口
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
public interface CityDeliveryService  extends IService<CityDeliveryOrder> {

    /**
     * 创建同城配送订单
     */
    CityDeliveryOrder createDeliveryOrder(String orderNo, Integer merId, Integer uid,
                                         String pickupAddress, String pickupContact, String pickupPhone,
                                         String deliveryAddress, String deliveryContact, String deliveryPhone,
                                         Integer deliveryType, String scheduledTime, 
                                         BigDecimal longitude, BigDecimal latitude, String deliveryRemark);

    /**
     * 根据订单号获取配送信息
     */
    CityDeliveryOrderResponse getDeliveryOrderByOrderNo(String orderNo);

    /**
     * 根据配送订单号获取配送信息
     */
    CityDeliveryOrderResponse getDeliveryOrderByDeliveryNo(String deliveryOrderNo);

    /**
     * 更新配送状态
     */
    Boolean updateDeliveryStatus(String deliveryOrderNo, Integer status, String remark);

    /**
     * 分配配送员
     */
    Boolean assignDriver(String deliveryOrderNo, Integer driverId, String driverName, String driverPhone);

    /**
     * 自动分配配送员
     */
    Boolean autoAssignDriver(String deliveryOrderNo);

    /**
     * 添加配送轨迹
     */
    Boolean addDeliveryTrack(String deliveryOrderNo, BigDecimal longitude, BigDecimal latitude, String address, String status);

    /**
     * 取消配送订单
     */
    Boolean cancelDeliveryOrder(String deliveryOrderNo, String cancelReason);

    /**
     * 计算配送费用
     */
    BigDecimal calculateDeliveryFee(String pickupAddress, String deliveryAddress, Integer deliveryType);

    /**
     * 获取可用配送员
     */
    List<Integer> getAvailableDrivers(String pickupAddress, BigDecimal serviceRadius);

    /**
     * 获取配送员配送列表
     */
    List<CityDeliveryOrderResponse> getDriverDeliveryList(Integer driverId, Integer status);

    /**
     * 获取商户配送订单列表
     */
    List<CityDeliveryOrderResponse> getMerchantDeliveryList(Integer merId, Integer status);

    /**
     * 获取配送订单轨迹
     */
    List<Map<String, Object>> getDeliveryTrack(String deliveryOrderNo);

    /**
     * 配送员接单
     */
    Boolean driverAcceptOrder(String deliveryOrderNo, Integer driverId);

    /**
     * 配送员开始取件
     */
    Boolean driverStartPickup(String deliveryOrderNo, Integer driverId);

    /**
     * 配送员确认取件
     */
    Boolean driverConfirmPickup(String deliveryOrderNo, Integer driverId, String pickupCode);

    /**
     * 配送员开始配送
     */
    Boolean driverStartDelivery(String deliveryOrderNo, Integer driverId);

    /**
     * 配送员完成配送
     */
    Boolean driverCompleteDelivery(String deliveryOrderNo, Integer driverId, String deliveryCode);

    /**
     * 配送异常上报
     */
    Boolean reportDeliveryException(String deliveryOrderNo, Integer driverId, Integer exceptionType, String description);

    /**
     * 获取配送统计
     */
    Map<String, Object> getDeliveryStats(Integer merId);

    /**
     * 生成配送订单号
     */
    String generateDeliveryOrderNo();

    /**
     * 检查地址是否在配送范围内
     */
    Boolean checkAddressInRange(String address);

    /**
     * 获取配送时间段
     */
    List<Map<String, String>> getDeliveryTimeSlots(String address);

    /**
     * 预估配送时间
     */
    String estimateDeliveryTime(String pickupAddress, String deliveryAddress, Integer deliveryType);

    /**
     * 获取配送进度
     */
    Map<String, Object> getDeliveryProgress(String deliveryOrderNo);

    /**
     * 重新分配配送员
     */
    Boolean reassignDriver(String deliveryOrderNo, Integer newDriverId);

    /**
     * 获取超时订单
     */
    List<CityDeliveryOrderResponse> getTimeoutOrders();

    /**
     * 处理超时订单
     */
    Boolean handleTimeoutOrder(String deliveryOrderNo);

    /**
     * 配送员评价
     */
    Boolean rateDriver(String deliveryOrderNo, Integer userId, Integer rating, String comment);

    /**
     * 获取配送费用明细
     */
    Map<String, Object> getDeliveryFeeDetail(String pickupAddress, String deliveryAddress, Integer deliveryType);

    /**
     * 批量更新配送状态
     */
    Boolean batchUpdateDeliveryStatus(List<String> deliveryOrderNos, Integer status);

    /**
     * 获取配送区域信息
     */
    Map<String, Object> getDeliveryAreaInfo(String address);

    /**
     * 配送员位置更新
     */
    Boolean updateDriverLocation(Integer driverId, BigDecimal longitude, BigDecimal latitude, String address);

    /**
     * 获取配送员实时位置
     */
    Map<String, Object> getDriverLocation(Integer driverId);

    /**
     * 获取配送热力图数据
     */
    List<Map<String, Object>> getDeliveryHeatmapData(String startDate, String endDate);

    /**
     * 获取配送效率统计
     */
    Map<String, Object> getDeliveryEfficiencyStats(String startDate, String endDate);

    /**
     * 配送路径优化
     */
    List<Map<String, Object>> optimizeDeliveryRoute(Integer driverId, List<String> deliveryOrderNos);

    /**
     * 根据请求对象创建配送订单
     */
    CityDeliveryOrder createDeliveryOrderFromRequest(CityDeliveryOrderRequest request, Integer merId);

    /**
     * 分页获取配送订单列表
     */
    PageInfo<CityDeliveryOrderResponse> getDeliveryOrderPage(CityDeliveryOrderListRequest request);

    /**
     * 获取订单统计信息
     */
    Map<String, Object> getOrderStatistics(Integer merId, String startDate, String endDate);
} 