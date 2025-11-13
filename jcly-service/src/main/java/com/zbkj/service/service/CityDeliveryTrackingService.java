package com.zbkj.service.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 同城配送实时追踪服务接口
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
public interface CityDeliveryTrackingService {

    /**
     * 更新配送员实时位置
     * 
     * @param driverId 配送员ID
     * @param longitude 经度
     * @param latitude 纬度
     * @param address 地址
     * @param speed 速度（km/h）
     * @param direction 方向角度
     * @return 是否更新成功
     */
    Boolean updateDriverLocation(Integer driverId, BigDecimal longitude, BigDecimal latitude, 
                               String address, BigDecimal speed, BigDecimal direction);

    /**
     * 获取配送员实时位置
     * 
     * @param driverId 配送员ID
     * @return 位置信息
     */
    Map<String, Object> getDriverRealTimeLocation(Integer driverId);

    /**
     * 批量更新配送员位置
     * 
     * @param locationUpdates 位置更新数据列表
     * @return 批量更新结果
     */
    Map<String, Object> batchUpdateDriverLocations(List<Map<String, Object>> locationUpdates);

    /**
     * 添加配送轨迹点
     * 
     * @param deliveryOrderNo 配送单号
     * @param driverId 配送员ID
     * @param longitude 经度
     * @param latitude 纬度
     * @param address 地址
     * @param trackType 轨迹类型（pickup/delivery/transit）
     * @param remark 备注
     * @return 是否添加成功
     */
    Boolean addTrackPoint(String deliveryOrderNo, Integer driverId, BigDecimal longitude, 
                        BigDecimal latitude, String address, String trackType, String remark);

    /**
     * 获取配送订单完整轨迹
     * 
     * @param deliveryOrderNo 配送单号
     * @return 轨迹点列表
     */
    List<Map<String, Object>> getOrderTrackingPath(String deliveryOrderNo);

    /**
     * 获取配送员当日轨迹
     * 
     * @param driverId 配送员ID
     * @param date 日期（yyyy-MM-dd）
     * @return 当日轨迹列表
     */
    List<Map<String, Object>> getDriverDailyTrack(Integer driverId, String date);

    /**
     * 实时追踪配送进度
     * 
     * @param deliveryOrderNo 配送单号
     * @return 配送进度信息
     */
    Map<String, Object> trackOrderProgress(String deliveryOrderNo);

    /**
     * 计算配送员到目的地的预计时间
     * 
     * @param driverId 配送员ID
     * @param targetLongitude 目标经度
     * @param targetLatitude 目标纬度
     * @return 预计到达时间（分钟）
     */
    Integer calculateETA(Integer driverId, BigDecimal targetLongitude, BigDecimal targetLatitude);

    /**
     * 获取区域内所有配送员实时位置
     * 
     * @param centerLongitude 中心点经度
     * @param centerLatitude 中心点纬度
     * @param radius 半径（公里）
     * @return 配送员位置列表
     */
    List<Map<String, Object>> getDriversInArea(BigDecimal centerLongitude, BigDecimal centerLatitude, BigDecimal radius);

    /**
     * 检测配送员是否在指定地理围栏内
     * 
     * @param driverId 配送员ID
     * @param fencePoints 围栏坐标点列表
     * @return 是否在围栏内
     */
    Boolean checkDriverInGeofence(Integer driverId, List<Map<String, BigDecimal>> fencePoints);

    /**
     * 配送异常检测
     * 
     * @param deliveryOrderNo 配送单号
     * @return 异常检测结果
     */
    Map<String, Object> detectDeliveryAnomalies(String deliveryOrderNo);

    /**
     * 获取配送热力图数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param granularity 时间粒度（hour/day）
     * @return 热力图数据
     */
    List<Map<String, Object>> getDeliveryHeatmapData(String startTime, String endTime, String granularity);

    /**
     * 生成配送轨迹报告
     * 
     * @param deliveryOrderNo 配送单号
     * @return 轨迹分析报告
     */
    Map<String, Object> generateTrackingReport(String deliveryOrderNo);

    /**
     * 配送员轨迹回放
     * 
     * @param driverId 配送员ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 轨迹回放数据
     */
    List<Map<String, Object>> replayDriverTrack(Integer driverId, String startTime, String endTime);

    /**
     * 实时监控所有配送员状态
     * 
     * @return 配送员状态监控数据
     */
    Map<String, Object> monitorAllDriversStatus();

    /**
     * 配送距离和时间统计
     * 
     * @param deliveryOrderNo 配送单号
     * @return 距离和时间统计
     */
    Map<String, Object> calculateDeliveryMetrics(String deliveryOrderNo);

    /**
     * 设置地理围栏预警
     * 
     * @param driverId 配送员ID
     * @param deliveryOrderNo 配送单号
     * @param alertType 预警类型
     * @return 设置结果
     */
    Boolean setupGeofenceAlert(Integer driverId, String deliveryOrderNo, String alertType);

    /**
     * 获取配送员位置历史记录
     * 
     * @param driverId 配送员ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 记录数量限制
     * @return 位置历史记录
     */
    List<Map<String, Object>> getDriverLocationHistory(Integer driverId, String startTime, String endTime, Integer limit);

    /**
     * 实时推送配送状态更新
     * 
     * @param deliveryOrderNo 配送单号
     * @param status 状态
     * @param message 消息
     * @param extraData 额外数据
     * @return 推送结果
     */
    Boolean pushDeliveryStatusUpdate(String deliveryOrderNo, String status, String message, Map<String, Object> extraData);

    /**
     * 配送轨迹数据清理
     * 
     * @param beforeDate 清理此日期之前的数据
     * @return 清理结果
     */
    Map<String, Object> cleanupTrackingData(String beforeDate);

    /**
     * 获取配送效率分析
     * 
     * @param driverId 配送员ID
     * @param timeRange 时间范围
     * @return 效率分析数据
     */
    Map<String, Object> analyzeDeliveryEfficiency(Integer driverId, String timeRange);
} 