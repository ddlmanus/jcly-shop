package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.order.CityDeliveryDriver;
import com.zbkj.common.model.order.CityDeliveryOrder;
import com.zbkj.common.response.CityDeliveryDriverResponse;
import com.zbkj.service.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 同城配送实时追踪服务实现类
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
@Service
public class CityDeliveryTrackingServiceImpl implements CityDeliveryTrackingService {

    @Autowired
    private CityDeliveryDriverService cityDeliveryDriverService;
    
    @Autowired
    private CityDeliveryService cityDeliveryService;
    
    @Autowired
    private TencentMapService tencentMapService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String DRIVER_LOCATION_KEY = "driver:location:";
    private static final String ORDER_TRACK_KEY = "order:track:";
    private static final String DRIVER_TRACK_KEY = "driver:track:";
    private static final int LOCATION_EXPIRE_SECONDS = 300; // 5分钟过期

    /**
     * 更新配送员实时位置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDriverLocation(Integer driverId, BigDecimal longitude, BigDecimal latitude, 
                                      String address, BigDecimal speed, BigDecimal direction) {
        try {
            if (driverId == null || longitude == null || latitude == null) {
                throw new CrmebException("参数不能为空");
            }

            // 检查配送员是否存在
            CityDeliveryDriver driver = cityDeliveryDriverService.getById(driverId);
            if (driver == null) {
                throw new CrmebException("配送员不存在");
            }

            // 构建位置数据
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("driverId", driverId);
            locationData.put("longitude", longitude);
            locationData.put("latitude", latitude);
            locationData.put("address", address);
            locationData.put("speed", speed != null ? speed : BigDecimal.ZERO);
            locationData.put("direction", direction != null ? direction : BigDecimal.ZERO);
            locationData.put("updateTime", new Date());
            locationData.put("timestamp", System.currentTimeMillis());

            // 更新到Redis缓存
            String locationKey = DRIVER_LOCATION_KEY + driverId;
            redisTemplate.opsForValue().set(locationKey, locationData, LOCATION_EXPIRE_SECONDS, TimeUnit.SECONDS);

            // 更新数据库中的配送员位置
            cityDeliveryDriverService.updateDriverLocation(driverId, longitude, latitude, address);

            // 记录轨迹历史
            addDriverTrackHistory(driverId, longitude, latitude, address, speed, direction);

            // 检查是否有正在配送的订单，添加订单轨迹
            List<CityDeliveryOrder> activeOrders = getActiveOrdersByDriver(driverId);
            for (CityDeliveryOrder order : activeOrders) {
                addTrackPoint(order.getDeliveryOrderNo(), driverId, longitude, latitude, address, "transit", "配送员位置更新");
            }

            return true;
        } catch (Exception e) {
            throw new CrmebException("更新配送员位置失败：" + e.getMessage());
        }
    }

    /**
     * 获取配送员实时位置
     */
    @Override
    public Map<String, Object> getDriverRealTimeLocation(Integer driverId) {
        try {
            if (driverId == null) {
                throw new CrmebException("配送员ID不能为空");
            }

            // 先从Redis缓存获取
            String locationKey = DRIVER_LOCATION_KEY + driverId;
            Object cachedLocation = redisTemplate.opsForValue().get(locationKey);
            
            if (cachedLocation != null) {
                return (Map<String, Object>) cachedLocation;
            }

            // 缓存中没有，从数据库获取
            CityDeliveryDriver driver = cityDeliveryDriverService.getById(driverId);
            if (driver == null) {
                return new HashMap<>();
            }

            Map<String, Object> locationData = new HashMap<>();
            locationData.put("driverId", driverId);
            locationData.put("driverName", driver.getName());
            locationData.put("longitude", driver.getLongitude());
            locationData.put("latitude", driver.getLatitude());
            locationData.put("address", driver.getCurrentAddress());
            locationData.put("speed", BigDecimal.ZERO);
            locationData.put("direction", BigDecimal.ZERO);
            locationData.put("updateTime", driver.getLocationUpdateTime());
            locationData.put("status", driver.getStatus());
            locationData.put("statusText", getDriverStatusText(driver.getStatus()));

            return locationData;
        } catch (Exception e) {
            System.err.println("获取配送员实时位置失败：" + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 批量更新配送员位置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchUpdateDriverLocations(List<Map<String, Object>> locationUpdates) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failedCount = 0;
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            for (Map<String, Object> update : locationUpdates) {
                Map<String, Object> singleResult = new HashMap<>();
                
                try {
                    Integer driverId = (Integer) update.get("driverId");
                    BigDecimal longitude = (BigDecimal) update.get("longitude");
                    BigDecimal latitude = (BigDecimal) update.get("latitude");
                    String address = (String) update.get("address");
                    BigDecimal speed = (BigDecimal) update.get("speed");
                    BigDecimal direction = (BigDecimal) update.get("direction");

                    Boolean success = updateDriverLocation(driverId, longitude, latitude, address, speed, direction);
                    
                    if (success) {
                        successCount++;
                        singleResult.put("status", "success");
                        singleResult.put("driverId", driverId);
                    } else {
                        failedCount++;
                        singleResult.put("status", "failed");
                        singleResult.put("driverId", driverId);
                        singleResult.put("message", "更新失败");
                    }
                } catch (Exception e) {
                    failedCount++;
                    singleResult.put("status", "error");
                    singleResult.put("message", e.getMessage());
                }
                
                results.add(singleResult);
            }

            result.put("totalCount", locationUpdates.size());
            result.put("successCount", successCount);
            result.put("failedCount", failedCount);
            result.put("results", results);
            result.put("batchTime", new Date());

        } catch (Exception e) {
            result.put("error", "批量更新失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 添加配送轨迹点
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addTrackPoint(String deliveryOrderNo, Integer driverId, BigDecimal longitude, 
                               BigDecimal latitude, String address, String trackType, String remark) {
        try {
            if (StrUtil.isBlank(deliveryOrderNo) || driverId == null || longitude == null || latitude == null) {
                return false;
            }

            // 构建轨迹点数据
            Map<String, Object> trackPoint = new HashMap<>();
            trackPoint.put("deliveryOrderNo", deliveryOrderNo);
            trackPoint.put("driverId", driverId);
            trackPoint.put("longitude", longitude);
            trackPoint.put("latitude", latitude);
            trackPoint.put("address", address);
            trackPoint.put("trackType", trackType);
            trackPoint.put("remark", remark);
            trackPoint.put("createTime", new Date());
            trackPoint.put("timestamp", System.currentTimeMillis());

            // 添加到Redis轨迹列表
            String trackKey = ORDER_TRACK_KEY + deliveryOrderNo;
            redisTemplate.opsForList().rightPush(trackKey, trackPoint);
            
            // 设置过期时间（7天）
            redisTemplate.expire(trackKey, 7, TimeUnit.DAYS);

            return true;
        } catch (Exception e) {
            System.err.println("添加轨迹点失败：" + e.getMessage());
            return false;
        }
    }

    /**
     * 获取配送订单完整轨迹
     */
    @Override
    public List<Map<String, Object>> getOrderTrackingPath(String deliveryOrderNo) {
        try {
            if (StrUtil.isBlank(deliveryOrderNo)) {
                return new ArrayList<>();
            }

            String trackKey = ORDER_TRACK_KEY + deliveryOrderNo;
            List<Object> trackList = redisTemplate.opsForList().range(trackKey, 0, -1);

            if (CollUtil.isEmpty(trackList)) {
                return new ArrayList<>();
            }

            return trackList.stream()
                    .map(track -> (Map<String, Object>) track)
                    .sorted(Comparator.comparing(track -> (Long) track.get("timestamp")))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("获取订单轨迹失败：" + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取配送员当日轨迹
     */
    @Override
    public List<Map<String, Object>> getDriverDailyTrack(Integer driverId, String date) {
        try {
            if (driverId == null || StrUtil.isBlank(date)) {
                return new ArrayList<>();
            }

            String trackKey = DRIVER_TRACK_KEY + driverId + ":" + date;
            List<Object> trackList = redisTemplate.opsForList().range(trackKey, 0, -1);

            if (CollUtil.isEmpty(trackList)) {
                return new ArrayList<>();
            }

            return trackList.stream()
                    .map(track -> (Map<String, Object>) track)
                    .sorted(Comparator.comparing(track -> (Long) track.get("timestamp")))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("获取配送员当日轨迹失败：" + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 实时追踪配送进度
     */
    @Override
    public Map<String, Object> trackOrderProgress(String deliveryOrderNo) {
        Map<String, Object> progress = new HashMap<>();

        try {
            // 获取配送订单信息
            CityDeliveryOrder order = getOrderByDeliveryNo(deliveryOrderNo);
            if (order == null) {
                progress.put("error", "订单不存在");
                return progress;
            }

            progress.put("deliveryOrderNo", deliveryOrderNo);
            progress.put("orderStatus", order.getDeliveryStatus());
            progress.put("statusText", getOrderStatusText(order.getDeliveryStatus()));

            // 如果有配送员，获取配送员实时位置
            if (order.getDriverId() != null) {
                Map<String, Object> driverLocation = getDriverRealTimeLocation(order.getDriverId());
                progress.put("driverLocation", driverLocation);

                // 计算配送员到收货地址的距离和时间
                if (order.getDeliveryLatitude() != null && order.getDeliveryLongitude() != null) {
                    Integer eta = calculateETA(order.getDriverId(), order.getDeliveryLongitude(), order.getDeliveryLatitude());
                    progress.put("estimatedArrivalTime", eta);

                    BigDecimal distance = calculateDistance(driverLocation, order);
                    progress.put("remainingDistance", distance);
                }
            }

            // 获取配送轨迹
            List<Map<String, Object>> trackingPath = getOrderTrackingPath(deliveryOrderNo);
            progress.put("trackingPath", trackingPath);

            // 配送进度百分比
            Integer progressPercentage = calculateProgressPercentage(order.getDeliveryStatus());
            progress.put("progressPercentage", progressPercentage);

            progress.put("updateTime", new Date());

        } catch (Exception e) {
            progress.put("error", "追踪配送进度失败：" + e.getMessage());
        }

        return progress;
    }

    /**
     * 计算配送员到目的地的预计时间
     */
    @Override
    public Integer calculateETA(Integer driverId, BigDecimal targetLongitude, BigDecimal targetLatitude) {
        try {
            Map<String, Object> driverLocation = getDriverRealTimeLocation(driverId);
            if (driverLocation.isEmpty()) {
                return null;
            }

            BigDecimal driverLongitude = (BigDecimal) driverLocation.get("longitude");
            BigDecimal driverLatitude = (BigDecimal) driverLocation.get("latitude");

            if (driverLongitude == null || driverLatitude == null) {
                return null;
            }

            // 使用腾讯地图API计算路径和时间
            return tencentMapService.estimateDeliveryTime(
                driverLatitude + "," + driverLongitude,
                targetLatitude + "," + targetLongitude
            );
        } catch (Exception e) {
            System.err.println("计算ETA失败：" + e.getMessage());
            return null;
        }
    }

    /**
     * 获取区域内所有配送员实时位置
     */
    @Override
    public List<Map<String, Object>> getDriversInArea(BigDecimal centerLongitude, BigDecimal centerLatitude, BigDecimal radius) {
        List<Map<String, Object>> driversInArea = new ArrayList<>();

        try {
            // 获取所有在线配送员
            List<CityDeliveryDriverResponse> onlineDrivers = cityDeliveryDriverService.getOnlineDrivers();

            for (CityDeliveryDriverResponse driver : onlineDrivers) {
                Map<String, Object> driverLocation = getDriverRealTimeLocation(driver.getId());
                
                if (!driverLocation.isEmpty()) {
                    BigDecimal driverLon = (BigDecimal) driverLocation.get("longitude");
                    BigDecimal driverLat = (BigDecimal) driverLocation.get("latitude");

                    if (driverLon != null && driverLat != null) {
                        double distance = calculateDistanceKm(
                            centerLongitude.doubleValue(), centerLatitude.doubleValue(),
                            driverLon.doubleValue(), driverLat.doubleValue()
                        );

                        if (distance <= radius.doubleValue()) {
                            driverLocation.put("distanceFromCenter", BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP));
                            driversInArea.add(driverLocation);
                        }
                    }
                }
            }

            // 按距离排序
            driversInArea.sort(Comparator.comparing(driver -> 
                ((BigDecimal) driver.get("distanceFromCenter")).doubleValue()));

        } catch (Exception e) {
            System.err.println("获取区域内配送员失败：" + e.getMessage());
        }

        return driversInArea;
    }

    /**
     * 检测配送员是否在指定地理围栏内
     */
    @Override
    public Boolean checkDriverInGeofence(Integer driverId, List<Map<String, BigDecimal>> fencePoints) {
        try {
            Map<String, Object> driverLocation = getDriverRealTimeLocation(driverId);
            if (driverLocation.isEmpty()) {
                return false;
            }

            BigDecimal driverLon = (BigDecimal) driverLocation.get("longitude");
            BigDecimal driverLat = (BigDecimal) driverLocation.get("latitude");

            if (driverLon == null || driverLat == null) {
                return false;
            }

            return isPointInPolygon(driverLon.doubleValue(), driverLat.doubleValue(), fencePoints);
        } catch (Exception e) {
            System.err.println("检测地理围栏失败：" + e.getMessage());
            return false;
        }
    }

    /**
     * 配送异常检测
     */
    @Override
    public Map<String, Object> detectDeliveryAnomalies(String deliveryOrderNo) {
        Map<String, Object> anomalies = new HashMap<>();
        List<String> detectedAnomalies = new ArrayList<>();

        try {
            CityDeliveryOrder order = getOrderByDeliveryNo(deliveryOrderNo);
            if (order == null) {
                anomalies.put("error", "订单不存在");
                return anomalies;
            }

            // 检测1：订单超时
            if (isOrderTimeout(order)) {
                detectedAnomalies.add("订单配送超时");
            }

            // 检测2：配送员长时间未移动
            if (order.getDriverId() != null && isDriverStatic(order.getDriverId())) {
                detectedAnomalies.add("配送员长时间未移动");
            }

            // 检测3：偏离预期路径
            if (order.getDriverId() != null && isDeviatingFromRoute(order)) {
                detectedAnomalies.add("偏离预期配送路径");
            }

            // 检测4：配送距离异常
            if (isDistanceAbnormal(order)) {
                detectedAnomalies.add("配送距离异常");
            }

            anomalies.put("deliveryOrderNo", deliveryOrderNo);
            anomalies.put("anomalies", detectedAnomalies);
            anomalies.put("anomalyCount", detectedAnomalies.size());
            anomalies.put("hasAnomalies", !detectedAnomalies.isEmpty());
            anomalies.put("checkTime", new Date());

        } catch (Exception e) {
            anomalies.put("error", "异常检测失败：" + e.getMessage());
        }

        return anomalies;
    }

    /**
     * 获取配送热力图数据
     */
    @Override
    public List<Map<String, Object>> getDeliveryHeatmapData(String startTime, String endTime, String granularity) {
        List<Map<String, Object>> heatmapData = new ArrayList<>();

        try {
            // 这里应该从数据库查询配送密度数据
            // 简化实现，返回模拟数据
            String[] areas = {"市中心", "商业区", "住宅区", "工业区", "郊区"};
            
            for (String area : areas) {
                Map<String, Object> data = new HashMap<>();
                data.put("areaName", area);
                data.put("longitude", 116.4074 + Math.random() * 0.1);
                data.put("latitude", 39.9042 + Math.random() * 0.1);
                data.put("intensity", (int)(Math.random() * 100));
                data.put("orderCount", (int)(Math.random() * 50));
                heatmapData.add(data);
            }

        } catch (Exception e) {
            System.err.println("获取配送热力图数据失败：" + e.getMessage());
        }

        return heatmapData;
    }

    /**
     * 生成配送轨迹报告
     */
    @Override
    public Map<String, Object> generateTrackingReport(String deliveryOrderNo) {
        Map<String, Object> report = new HashMap<>();

        try {
            CityDeliveryOrder order = getOrderByDeliveryNo(deliveryOrderNo);
            if (order == null) {
                report.put("error", "订单不存在");
                return report;
            }

            List<Map<String, Object>> trackingPath = getOrderTrackingPath(deliveryOrderNo);

            report.put("deliveryOrderNo", deliveryOrderNo);
            report.put("orderCreateTime", order.getCreateTime());
            report.put("orderFinishTime", order.getFinishTime());
            
            if (!trackingPath.isEmpty()) {
                // 计算总配送距离
                BigDecimal totalDistance = calculateTotalDistance(trackingPath);
                report.put("totalDistance", totalDistance);

                // 计算配送时长
                long durationMinutes = calculateDeliveryDuration(order);
                report.put("deliveryDurationMinutes", durationMinutes);

                // 平均速度
                if (durationMinutes > 0) {
                    BigDecimal avgSpeed = totalDistance.divide(BigDecimal.valueOf(durationMinutes / 60.0), 2, RoundingMode.HALF_UP);
                    report.put("averageSpeed", avgSpeed);
                }

                // 轨迹点数量
                report.put("trackPointCount", trackingPath.size());

                // 停留点分析
                List<Map<String, Object>> stayPoints = analyzeStayPoints(trackingPath);
                report.put("stayPoints", stayPoints);
            }

            report.put("reportGenerateTime", new Date());

        } catch (Exception e) {
            report.put("error", "生成轨迹报告失败：" + e.getMessage());
        }

        return report;
    }

    /**
     * 配送员轨迹回放
     */
    @Override
    public List<Map<String, Object>> replayDriverTrack(Integer driverId, String startTime, String endTime) {
        try {
            // 这里应该从数据库查询指定时间范围内的轨迹数据
            // 简化实现，从Redis获取当日数据
            String today = DateUtil.format(new Date(), "yyyy-MM-dd");
            return getDriverDailyTrack(driverId, today);
        } catch (Exception e) {
            System.err.println("配送员轨迹回放失败：" + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 实时监控所有配送员状态
     */
    @Override
    public Map<String, Object> monitorAllDriversStatus() {
        Map<String, Object> monitoring = new HashMap<>();

        try {
            List<CityDeliveryDriver> allDrivers = cityDeliveryDriverService.getByStatus(null);
            
            int totalDrivers = allDrivers.size();
            int onlineDrivers = 0;
            int busyDrivers = 0;
            int idleDrivers = 0;
            int offlineDrivers = 0;

            List<Map<String, Object>> driversStatus = new ArrayList<>();

            for (CityDeliveryDriver driver : allDrivers) {
                Integer status = driver.getStatus();
                
                switch (status) {
                    case 0: offlineDrivers++; break;
                    case 1: 
                        if (driver.getCurrentOrders() != null && driver.getCurrentOrders() > 0) {
                            busyDrivers++;
                        } else {
                            idleDrivers++;
                        }
                        onlineDrivers++;
                        break;
                    case 2: busyDrivers++; break;
                }

                Map<String, Object> driverStatus = new HashMap<>();
                driverStatus.put("driverId", driver.getId());
                driverStatus.put("driverName", driver.getName());
                driverStatus.put("status", status);
                driverStatus.put("statusText", getDriverStatusText(status));
                driverStatus.put("currentOrders", driver.getCurrentOrders());
                driverStatus.put("lastLocationUpdate", driver.getLocationUpdateTime());
                
                // 获取实时位置
                Map<String, Object> location = getDriverRealTimeLocation(driver.getId());
                driverStatus.put("location", location);

                driversStatus.add(driverStatus);
            }

            monitoring.put("totalDrivers", totalDrivers);
            monitoring.put("onlineDrivers", onlineDrivers);
            monitoring.put("busyDrivers", busyDrivers);
            monitoring.put("idleDrivers", idleDrivers);
            monitoring.put("offlineDrivers", offlineDrivers);
            monitoring.put("driversStatus", driversStatus);
            monitoring.put("monitorTime", new Date());

        } catch (Exception e) {
            monitoring.put("error", "监控配送员状态失败：" + e.getMessage());
        }

        return monitoring;
    }

    /**
     * 配送距离和时间统计
     */
    @Override
    public Map<String, Object> calculateDeliveryMetrics(String deliveryOrderNo) {
        Map<String, Object> metrics = new HashMap<>();

        try {
            CityDeliveryOrder order = getOrderByDeliveryNo(deliveryOrderNo);
            if (order == null) {
                metrics.put("error", "订单不存在");
                return metrics;
            }

            List<Map<String, Object>> trackingPath = getOrderTrackingPath(deliveryOrderNo);

            metrics.put("deliveryOrderNo", deliveryOrderNo);
            
            if (!trackingPath.isEmpty()) {
                // 实际配送距离
                BigDecimal actualDistance = calculateTotalDistance(trackingPath);
                metrics.put("actualDeliveryDistance", actualDistance);

                // 预计距离（直线距离）
                BigDecimal straightDistance = calculateStraightDistance(order);
                metrics.put("straightLineDistance", straightDistance);

                // 距离偏差
                if (straightDistance.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal distanceDeviation = actualDistance.subtract(straightDistance)
                            .divide(straightDistance, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    metrics.put("distanceDeviationPercentage", distanceDeviation);
                }

                // 配送时长
                long deliveryDuration = calculateDeliveryDuration(order);
                metrics.put("deliveryDurationMinutes", deliveryDuration);

                // 预计时长
                Integer estimatedDuration = calculateEstimatedDuration(order);
                metrics.put("estimatedDurationMinutes", estimatedDuration);

                // 时间偏差
                if (estimatedDuration != null && estimatedDuration > 0) {
                    double timeDeviation = ((double) deliveryDuration - estimatedDuration) / estimatedDuration * 100;
                    metrics.put("timeDeviationPercentage", BigDecimal.valueOf(timeDeviation).setScale(2, RoundingMode.HALF_UP));
                }
            }

            metrics.put("calculationTime", new Date());

        } catch (Exception e) {
            metrics.put("error", "计算配送指标失败：" + e.getMessage());
        }

        return metrics;
    }

    /**
     * 设置地理围栏预警
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean setupGeofenceAlert(Integer driverId, String deliveryOrderNo, String alertType) {
        try {
            // 构建预警配置
            Map<String, Object> alertConfig = new HashMap<>();
            alertConfig.put("driverId", driverId);
            alertConfig.put("deliveryOrderNo", deliveryOrderNo);
            alertConfig.put("alertType", alertType);
            alertConfig.put("createTime", new Date());
            alertConfig.put("active", true);

            // 存储到Redis
            String alertKey = "geofence:alert:" + driverId + ":" + deliveryOrderNo;
            redisTemplate.opsForValue().set(alertKey, alertConfig, 24, TimeUnit.HOURS);

            return true;
        } catch (Exception e) {
            System.err.println("设置地理围栏预警失败：" + e.getMessage());
            return false;
        }
    }

    /**
     * 获取配送员位置历史记录
     */
    @Override
    public List<Map<String, Object>> getDriverLocationHistory(Integer driverId, String startTime, String endTime, Integer limit) {
        try {
            // 这里应该从数据库查询历史记录
            // 简化实现，返回当日轨迹数据
            String today = DateUtil.format(new Date(), "yyyy-MM-dd");
            List<Map<String, Object>> history = getDriverDailyTrack(driverId, today);

            if (limit != null && limit > 0 && history.size() > limit) {
                return history.subList(0, limit);
            }

            return history;
        } catch (Exception e) {
            System.err.println("获取配送员位置历史失败：" + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 实时推送配送状态更新
     */
    @Override
    public Boolean pushDeliveryStatusUpdate(String deliveryOrderNo, String status, String message, Map<String, Object> extraData) {
        try {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("deliveryOrderNo", deliveryOrderNo);
            updateData.put("status", status);
            updateData.put("message", message);
            updateData.put("extraData", extraData);
            updateData.put("timestamp", System.currentTimeMillis());
            updateData.put("updateTime", new Date());

            // 这里应该推送到WebSocket或消息队列
            // 简化实现，存储到Redis供前端轮询
            String pushKey = "push:status:" + deliveryOrderNo;
            redisTemplate.opsForValue().set(pushKey, updateData, 1, TimeUnit.HOURS);

            return true;
        } catch (Exception e) {
            System.err.println("推送配送状态更新失败：" + e.getMessage());
            return false;
        }
    }

    /**
     * 配送轨迹数据清理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> cleanupTrackingData(String beforeDate) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 这里应该清理数据库中的历史轨迹数据
            // 简化实现，只返回统计信息
            result.put("cleanupDate", beforeDate);
            result.put("deletedRecords", 0);
            result.put("cleanupTime", new Date());
            result.put("status", "completed");

        } catch (Exception e) {
            result.put("error", "清理轨迹数据失败：" + e.getMessage());
            result.put("status", "failed");
        }

        return result;
    }

    /**
     * 获取配送效率分析
     */
    @Override
    public Map<String, Object> analyzeDeliveryEfficiency(Integer driverId, String timeRange) {
        Map<String, Object> analysis = new HashMap<>();

        try {
            CityDeliveryDriver driver = cityDeliveryDriverService.getById(driverId);
            if (driver == null) {
                analysis.put("error", "配送员不存在");
                return analysis;
            }

            analysis.put("driverId", driverId);
            analysis.put("driverName", driver.getName());
            analysis.put("timeRange", timeRange);

            // 模拟效率分析数据
            analysis.put("averageDeliveryTime", 25.5); // 平均配送时间（分钟）
            analysis.put("averageSpeed", 18.2); // 平均速度（公里/小时）
            analysis.put("completionRate", 95.8); // 完成率（%）
            analysis.put("onTimeRate", 88.6); // 准时率（%）
            analysis.put("totalOrders", 156); // 总订单数
            analysis.put("totalDistance", 450.8); // 总配送距离（公里）
            analysis.put("totalWorkTime", 8.5); // 总工作时间（小时）

            // 效率评级
            String efficiencyGrade = calculateEfficiencyGrade(analysis);
            analysis.put("efficiencyGrade", efficiencyGrade);

            analysis.put("analysisTime", new Date());

        } catch (Exception e) {
            analysis.put("error", "分析配送效率失败：" + e.getMessage());
        }

        return analysis;
    }

    // ========== 私有辅助方法 ==========

    /**
     * 添加配送员轨迹历史
     */
    private void addDriverTrackHistory(Integer driverId, BigDecimal longitude, BigDecimal latitude, 
                                     String address, BigDecimal speed, BigDecimal direction) {
        try {
            String today = DateUtil.format(new Date(), "yyyy-MM-dd");
            String trackKey = DRIVER_TRACK_KEY + driverId + ":" + today;

            Map<String, Object> trackPoint = new HashMap<>();
            trackPoint.put("driverId", driverId);
            trackPoint.put("longitude", longitude);
            trackPoint.put("latitude", latitude);
            trackPoint.put("address", address);
            trackPoint.put("speed", speed);
            trackPoint.put("direction", direction);
            trackPoint.put("timestamp", System.currentTimeMillis());
            trackPoint.put("createTime", new Date());

            redisTemplate.opsForList().rightPush(trackKey, trackPoint);
            redisTemplate.expire(trackKey, 7, TimeUnit.DAYS);
        } catch (Exception e) {
            System.err.println("添加配送员轨迹历史失败：" + e.getMessage());
        }
    }

    /**
     * 获取配送员正在配送的订单
     */
    private List<CityDeliveryOrder> getActiveOrdersByDriver(Integer driverId) {
        QueryWrapper<CityDeliveryOrder> query = new QueryWrapper<>();
        query.eq("driver_id", driverId)
             .in("delivery_status", Arrays.asList(2, 3, 4)) // 已接单、取件中、配送中
             .eq("is_del", false);
        
        return cityDeliveryService.list(query);
    }

    /**
     * 根据配送单号获取订单
     */
    private CityDeliveryOrder getOrderByDeliveryNo(String deliveryOrderNo) {
        QueryWrapper<CityDeliveryOrder> query = new QueryWrapper<>();
        query.eq("delivery_order_no", deliveryOrderNo).eq("is_del", false);
        return cityDeliveryService.getOne(query);
    }

    /**
     * 获取配送员状态文本
     */
    private String getDriverStatusText(Integer status) {
        switch (status) {
            case 0: return "离线";
            case 1: return "在线";
            case 2: return "忙碌";
            case 3: return "停用";
            default: return "未知";
        }
    }

    /**
     * 获取订单状态文本
     */
    private String getOrderStatusText(Integer status) {
        switch (status) {
            case 1: return "待分配";
            case 2: return "已接单";
            case 3: return "取件中";
            case 4: return "配送中";
            case 5: return "已送达";
            case 6: return "已取消";
            case 7: return "异常";
            default: return "未知状态";
        }
    }

    /**
     * 计算配送进度百分比
     */
    private Integer calculateProgressPercentage(Integer deliveryStatus) {
        switch (deliveryStatus) {
            case 1: return 10;  // 待分配
            case 2: return 25;  // 已接单
            case 3: return 50;  // 取件中
            case 4: return 75;  // 配送中
            case 5: return 100; // 已送达
            case 6: return 0;   // 已取消
            case 7: return 0;   // 异常
            default: return 0;
        }
    }

    /**
     * 计算两点间距离
     */
    private BigDecimal calculateDistance(Map<String, Object> driverLocation, CityDeliveryOrder order) {
        try {
            BigDecimal driverLon = (BigDecimal) driverLocation.get("longitude");
            BigDecimal driverLat = (BigDecimal) driverLocation.get("latitude");

            if (driverLon == null || driverLat == null || 
                order.getDeliveryLongitude() == null || order.getDeliveryLatitude() == null) {
                return BigDecimal.ZERO;
            }

            double distance = calculateDistanceKm(
                driverLon.doubleValue(), driverLat.doubleValue(),
                order.getDeliveryLongitude().doubleValue(), order.getDeliveryLatitude().doubleValue()
            );

            return BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 计算两点间距离（公里）
     */
    private double calculateDistanceKm(double lon1, double lat1, double lon2, double lat2) {
        final double R = 6371; // 地球半径，单位：公里
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }

    /**
     * 判断点是否在多边形内
     */
    private boolean isPointInPolygon(double x, double y, List<Map<String, BigDecimal>> polygon) {
        int n = polygon.size();
        boolean inside = false;

        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = polygon.get(i).get("longitude").doubleValue();
            double yi = polygon.get(i).get("latitude").doubleValue();
            double xj = polygon.get(j).get("longitude").doubleValue();
            double yj = polygon.get(j).get("latitude").doubleValue();

            if (((yi > y) != (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi)) {
                inside = !inside;
            }
        }

        return inside;
    }

    /**
     * 检查订单是否超时
     */
    private boolean isOrderTimeout(CityDeliveryOrder order) {
        if (order.getCreateTime() == null) return false;
        
        long elapsedMinutes = (System.currentTimeMillis() - order.getCreateTime().getTime()) / (1000 * 60);
        return elapsedMinutes > 120; // 2小时超时
    }

    /**
     * 检查配送员是否长时间静止
     */
    private boolean isDriverStatic(Integer driverId) {
        try {
            Map<String, Object> location = getDriverRealTimeLocation(driverId);
            Date updateTime = (Date) location.get("updateTime");
            
            if (updateTime == null) return false;
            
            long elapsedMinutes = (System.currentTimeMillis() - updateTime.getTime()) / (1000 * 60);
            return elapsedMinutes > 30; // 30分钟未移动
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查是否偏离预期路径
     */
    private boolean isDeviatingFromRoute(CityDeliveryOrder order) {
        // 简化实现，实际需要根据预期路径计算偏离程度
        return false;
    }

    /**
     * 检查配送距离是否异常
     */
    private boolean isDistanceAbnormal(CityDeliveryOrder order) {
        try {
            if (order.getDeliveryDistance() == null) return false;
            
            // 计算直线距离
            BigDecimal straightDistance = calculateStraightDistance(order);
            if (straightDistance.compareTo(BigDecimal.ZERO) == 0) return false;
            
            // 如果实际距离超过直线距离的3倍，认为异常
            BigDecimal ratio = order.getDeliveryDistance().divide(straightDistance, 2, RoundingMode.HALF_UP);
            return ratio.compareTo(BigDecimal.valueOf(3)) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 计算直线距离
     */
    private BigDecimal calculateStraightDistance(CityDeliveryOrder order) {
        try {
            if (order.getPickupLongitude() == null || order.getPickupLatitude() == null ||
                order.getDeliveryLongitude() == null || order.getDeliveryLatitude() == null) {
                return BigDecimal.ZERO;
            }

            double distance = calculateDistanceKm(
                order.getPickupLongitude().doubleValue(), order.getPickupLatitude().doubleValue(),
                order.getDeliveryLongitude().doubleValue(), order.getDeliveryLatitude().doubleValue()
            );

            return BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 计算轨迹总距离
     */
    private BigDecimal calculateTotalDistance(List<Map<String, Object>> trackingPath) {
        if (trackingPath.size() < 2) return BigDecimal.ZERO;

        double totalDistance = 0.0;
        
        for (int i = 1; i < trackingPath.size(); i++) {
            Map<String, Object> prev = trackingPath.get(i - 1);
            Map<String, Object> curr = trackingPath.get(i);

            BigDecimal prevLon = (BigDecimal) prev.get("longitude");
            BigDecimal prevLat = (BigDecimal) prev.get("latitude");
            BigDecimal currLon = (BigDecimal) curr.get("longitude");
            BigDecimal currLat = (BigDecimal) curr.get("latitude");

            if (prevLon != null && prevLat != null && currLon != null && currLat != null) {
                double segmentDistance = calculateDistanceKm(
                    prevLon.doubleValue(), prevLat.doubleValue(),
                    currLon.doubleValue(), currLat.doubleValue()
                );
                totalDistance += segmentDistance;
            }
        }

        return BigDecimal.valueOf(totalDistance).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算配送时长
     */
    private long calculateDeliveryDuration(CityDeliveryOrder order) {
        if (order.getCreateTime() == null) return 0;
        
        Date endTime = order.getFinishTime() != null ? order.getFinishTime() : new Date();
        return (endTime.getTime() - order.getCreateTime().getTime()) / (1000 * 60);
    }

    /**
     * 计算预计配送时长
     */
    private Integer calculateEstimatedDuration(CityDeliveryOrder order) {
        // 简化实现，返回基于距离的估算
        BigDecimal distance = calculateStraightDistance(order);
        return distance.multiply(BigDecimal.valueOf(3)).intValue(); // 每公里3分钟
    }

    /**
     * 分析停留点
     */
    private List<Map<String, Object>> analyzeStayPoints(List<Map<String, Object>> trackingPath) {
        List<Map<String, Object>> stayPoints = new ArrayList<>();
        
        // 简化实现，返回空列表
        // 实际应该分析轨迹中的停留点
        
        return stayPoints;
    }

    /**
     * 计算效率评级
     */
    private String calculateEfficiencyGrade(Map<String, Object> analysis) {
        double completionRate = (Double) analysis.get("completionRate");
        double onTimeRate = (Double) analysis.get("onTimeRate");
        
        double score = (completionRate + onTimeRate) / 2;
        
        if (score >= 95) return "A+";
        else if (score >= 90) return "A";
        else if (score >= 85) return "B+";
        else if (score >= 80) return "B";
        else if (score >= 75) return "C+";
        else if (score >= 70) return "C";
        else return "D";
    }
} 