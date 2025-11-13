package com.zbkj.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zbkj.common.constants.SysConfigConstants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.service.service.SystemConfigService;
import com.zbkj.service.service.TencentMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 腾讯地图服务实现类
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
public class TencentMapServiceImpl implements TencentMapService {

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private MapCacheService mapCacheService;

    // 腾讯地图API接口地址
    private static final String GEOCODING_URL = "https://apis.map.qq.com/ws/geocoder/v1/";
    private static final String DISTANCE_URL = "https://apis.map.qq.com/ws/distance/v1/";
    private static final String DIRECTION_URL = "https://apis.map.qq.com/ws/direction/v1/driving/";
    private static final String IP_LOCATION_URL = "https://apis.map.qq.com/ws/location/v1/ip";

    /**
     * 地址解析（地址转坐标）
     */
    @Override
    public Map<String, BigDecimal> geocoding(String address) {
        try {
            if (StrUtil.isBlank(address)) {
                throw new CrmebException("地址不能为空");
            }

            // 先从缓存中获取
            Map<String, BigDecimal> cachedResult = mapCacheService.getCachedGeocodingResult(address);
            if (cachedResult != null) {
                return cachedResult;
            }

            String key = getMapKey();
            Map<String, Object> params = new HashMap<>();
            params.put("address", address);
            params.put("key", key);

            String response = HttpUtil.get(GEOCODING_URL, params);
            JSONObject result = JSONObject.parseObject(response);

            if (result.getInteger("status") != 0) {
                throw new CrmebException("地址解析失败：" + result.getString("message"));
            }

            JSONObject location = result.getJSONObject("result").getJSONObject("location");
            Map<String, BigDecimal> coordinates = new HashMap<>();
            coordinates.put("latitude", BigDecimal.valueOf(location.getDoubleValue("lat")));
            coordinates.put("longitude", BigDecimal.valueOf(location.getDoubleValue("lng")));

            // 缓存结果
            mapCacheService.cacheGeocodingResult(address, coordinates);

            return coordinates;
        } catch (Exception e) {
            throw new CrmebException("地址解析失败：" + e.getMessage());
        }
    }

    /**
     * 逆地址解析（坐标转地址）
     */
    @Override
    public String reverseGeocoding(BigDecimal latitude, BigDecimal longitude) {
        try {
            if (ObjectUtil.isNull(latitude) || ObjectUtil.isNull(longitude)) {
                throw new CrmebException("坐标不能为空");
            }

            // 先从缓存中获取
            String cachedResult = mapCacheService.getCachedReverseGeocodingResult(latitude, longitude);
            if (cachedResult != null) {
                return cachedResult;
            }

            String key = getMapKey();
            Map<String, Object> params = new HashMap<>();
            params.put("location", latitude + "," + longitude);
            params.put("key", key);

            String response = HttpUtil.get(GEOCODING_URL, params);
            JSONObject result = JSONObject.parseObject(response);

            if (result.getInteger("status") != 0) {
                throw new CrmebException("坐标解析失败：" + result.getString("message"));
            }

            String address = result.getJSONObject("result").getString("address");
            
            // 缓存结果
            mapCacheService.cacheReverseGeocodingResult(latitude, longitude, address);

            return address;
        } catch (Exception e) {
            throw new CrmebException("坐标解析失败：" + e.getMessage());
        }
    }

    /**
     * 计算两地址间距离
     */
    @Override
    public BigDecimal calculateDistance(String fromAddress, String toAddress) {
        try {
            if (StrUtil.isBlank(fromAddress) || StrUtil.isBlank(toAddress)) {
                throw new CrmebException("地址不能为空");
            }

            // 先解析地址为坐标
            Map<String, BigDecimal> fromLocation = geocoding(fromAddress);
            Map<String, BigDecimal> toLocation = geocoding(toAddress);

            return calculateDistanceByCoordinates(
                fromLocation.get("latitude"), fromLocation.get("longitude"),
                toLocation.get("latitude"), toLocation.get("longitude")
            );
        } catch (Exception e) {
            throw new CrmebException("计算距离失败：" + e.getMessage());
        }
    }

    /**
     * 计算两坐标间距离
     */
    @Override
    public BigDecimal calculateDistanceByCoordinates(BigDecimal fromLat, BigDecimal fromLng, 
                                                   BigDecimal toLat, BigDecimal toLng) {
        try {
            if (ObjectUtil.isNull(fromLat) || ObjectUtil.isNull(fromLng) || 
                ObjectUtil.isNull(toLat) || ObjectUtil.isNull(toLng)) {
                throw new CrmebException("坐标不能为空");
            }

            // 先从缓存中获取
            BigDecimal cachedResult = mapCacheService.getCachedDistanceByCoordinates(fromLat, fromLng, toLat, toLng);
            if (cachedResult != null) {
                return cachedResult;
            }

            String key = getMapKey();
            Map<String, Object> params = new HashMap<>();
            params.put("mode", "driving"); // 驾车距离
            params.put("from", fromLat + "," + fromLng);
            params.put("to", toLat + "," + toLng);
            params.put("key", key);

            String response = HttpUtil.get(DISTANCE_URL, params);
            JSONObject result = JSONObject.parseObject(response);

            BigDecimal distance;
            if (result.getInteger("status") != 0) {
                // 如果API调用失败，使用直线距离计算
                distance = calculateStraightDistance(fromLat, fromLng, toLat, toLng);
            } else {
                JSONArray elements = result.getJSONObject("result").getJSONArray("elements");
                if (elements.size() > 0) {
                    JSONObject element = elements.getJSONObject(0);
                    Integer distanceMeters = element.getInteger("distance"); // 米
                    distance = BigDecimal.valueOf(distanceMeters).divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
                } else {
                    // 如果没有返回距离，使用直线距离
                    distance = calculateStraightDistance(fromLat, fromLng, toLat, toLng);
                }
            }

            // 缓存结果
            mapCacheService.cacheDistanceByCoordinates(fromLat, fromLng, toLat, toLng, distance);

            return distance;
        } catch (Exception e) {
            // 计算失败时使用直线距离
            return calculateStraightDistance(fromLat, fromLng, toLat, toLng);
        }
    }

    /**
     * 路径规划
     */
    @Override
    public Map<String, Object> getDirections(String fromAddress, String toAddress) {
        try {
            if (StrUtil.isBlank(fromAddress) || StrUtil.isBlank(toAddress)) {
                throw new CrmebException("地址不能为空");
            }

            // 先解析地址为坐标
            Map<String, BigDecimal> fromLocation = geocoding(fromAddress);
            Map<String, BigDecimal> toLocation = geocoding(toAddress);

            return getDirectionsByCoordinates(
                fromLocation.get("latitude"), fromLocation.get("longitude"),
                toLocation.get("latitude"), toLocation.get("longitude")
            );
        } catch (Exception e) {
            throw new CrmebException("路径规划失败：" + e.getMessage());
        }
    }

    /**
     * 坐标路径规划
     */
    @Override
    public Map<String, Object> getDirectionsByCoordinates(BigDecimal fromLat, BigDecimal fromLng, 
                                                        BigDecimal toLat, BigDecimal toLng) {
        try {
            if (ObjectUtil.isNull(fromLat) || ObjectUtil.isNull(fromLng) || 
                ObjectUtil.isNull(toLat) || ObjectUtil.isNull(toLng)) {
                throw new CrmebException("坐标不能为空");
            }

            String key = getMapKey();
            Map<String, Object> params = new HashMap<>();
            params.put("from", fromLat + "," + fromLng);
            params.put("to", toLat + "," + toLng);
            params.put("key", key);

            String response = HttpUtil.get(DIRECTION_URL, params);
            JSONObject result = JSONObject.parseObject(response);

            if (result.getInteger("status") != 0) {
                throw new CrmebException("路径规划失败：" + result.getString("message"));
            }

            JSONObject route = result.getJSONObject("result").getJSONArray("routes").getJSONObject(0);
            
            Map<String, Object> directions = new HashMap<>();
            directions.put("distance", route.getInteger("distance")); // 米
            directions.put("duration", route.getInteger("duration")); // 秒
            directions.put("polyline", route.getString("polyline")); // 路径坐标串
            
            // 解析路段信息
            JSONArray steps = route.getJSONArray("steps");
            directions.put("steps", steps);

            return directions;
        } catch (Exception e) {
            throw new CrmebException("路径规划失败：" + e.getMessage());
        }
    }

    /**
     * IP定位
     */
    @Override
    public Map<String, Object> getLocationByIP(String ip) {
        try {
            String key = getMapKey();
            Map<String, Object> params = new HashMap<>();
            if (StrUtil.isNotBlank(ip)) {
                params.put("ip", ip);
            }
            params.put("key", key);

            String response = HttpUtil.get(IP_LOCATION_URL, params);
            JSONObject result = JSONObject.parseObject(response);

            if (result.getInteger("status") != 0) {
                throw new CrmebException("IP定位失败：" + result.getString("message"));
            }

            JSONObject location = result.getJSONObject("result");
            Map<String, Object> ipLocation = new HashMap<>();
            ipLocation.put("country", location.getString("nation"));
            ipLocation.put("province", location.getString("province"));
            ipLocation.put("city", location.getString("city"));
            ipLocation.put("district", location.getString("district"));
            
            if (location.containsKey("location")) {
                JSONObject coordinates = location.getJSONObject("location");
                ipLocation.put("latitude", coordinates.getBigDecimal("lat"));
                ipLocation.put("longitude", coordinates.getBigDecimal("lng"));
            }

            return ipLocation;
        } catch (Exception e) {
            throw new CrmebException("IP定位失败：" + e.getMessage());
        }
    }

    /**
     * 预估配送时间
     */
    @Override
    public Integer estimateDeliveryTime(String fromAddress, String toAddress) {
        try {
            Map<String, Object> directions = getDirections(fromAddress, toAddress);
            Integer duration = (Integer) directions.get("duration"); // 秒
            
            // 考虑配送员取件和送达的额外时间（各5分钟）
            Integer extraTime = 10 * 60; // 10分钟
            
            return duration + extraTime;
        } catch (Exception e) {
            // 如果获取失败，返回默认时间（30分钟）
            return 30 * 60;
        }
    }

    /**
     * 获取交通状况
     */
    @Override
    public Map<String, Object> getTrafficInfo(BigDecimal latitude, BigDecimal longitude, Integer radius) {
        Map<String, Object> trafficInfo = new HashMap<>();
        
        try {
            // 腾讯地图没有直接的交通状况API，这里模拟返回
            // 实际项目中可以使用其他API或根据历史数据分析
            
            trafficInfo.put("status", "normal"); // normal, busy, congested
            trafficInfo.put("description", "交通状况良好");
            trafficInfo.put("speedKmh", 40); // 平均速度
            trafficInfo.put("delayMinutes", 0); // 延迟时间
            
        } catch (Exception e) {
            System.err.println("获取交通状况失败：" + e.getMessage());
            
            // 返回默认状况
            trafficInfo.put("status", "unknown");
            trafficInfo.put("description", "交通状况未知");
            trafficInfo.put("speedKmh", 30);
            trafficInfo.put("delayMinutes", 5);
        }
        
        return trafficInfo;
    }

    /**
     * 地址关键字搜索
     */
    @Override
    public JSONArray searchPlaces(String keyword, String city, Integer pageSize) {
        try {
            String key = getMapKey();
            Map<String, Object> params = new HashMap<>();
            params.put("keyword", keyword);
            params.put("boundary", "region(" + city + ",0)");
            params.put("page_size", pageSize);
            params.put("key", key);

            String searchUrl = "https://apis.map.qq.com/ws/place/v1/search";
            String response = HttpUtil.get(searchUrl, params);
            JSONObject result = JSONObject.parseObject(response);

            if (result.getInteger("status") != 0) {
                throw new CrmebException("地址搜索失败：" + result.getString("message"));
            }

            return result.getJSONArray("data");
        } catch (Exception e) {
            System.err.println("地址搜索失败：" + e.getMessage());
            return new JSONArray();
        }
    }

    /**
     * 验证地址有效性
     */
    @Override
    public Boolean validateAddress(String address) {
        try {
            Map<String, BigDecimal> location = geocoding(address);
            return ObjectUtil.isNotNull(location) && 
                   ObjectUtil.isNotNull(location.get("latitude")) && 
                   ObjectUtil.isNotNull(location.get("longitude"));
        } catch (Exception e) {
            return false;
        }
    }

    // ========== 私有方法 ==========

    /**
     * 获取腾讯地图API Key
     */
    @Override
    public String getMapKey() {
        String key = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_SITE_TENG_XUN_MAP_KEY);
        if (StrUtil.isBlank(key)) {
            throw new CrmebException("腾讯地图API Key未配置");
        }
        return key;
    }

    /**
     * 获取行政区划
     */
    @Override
    public JSONArray getDistrict(String keyword) {
        try {
            String key = getMapKey();
            Map<String, Object> params = new HashMap<>();
            params.put("keyword", keyword);
            params.put("key", key);

            String districtUrl = "https://apis.map.qq.com/ws/district/v1/list";
            String response = HttpUtil.get(districtUrl, params);
            JSONObject result = JSONObject.parseObject(response);

            if (result.getInteger("status") != 0) {
                throw new CrmebException("行政区划查询失败：" + result.getString("message"));
            }

            return result.getJSONArray("result");
        } catch (Exception e) {
            System.err.println("行政区划查询失败：" + e.getMessage());
            return new JSONArray();
        }
    }

    /**
     * 坐标转换
     */
    @Override
    public Map<String, BigDecimal> coordinateTransform(BigDecimal latitude, BigDecimal longitude, String from, String to) {
        try {
            String key = getMapKey();
            Map<String, Object> params = new HashMap<>();
            params.put("locations", latitude + "," + longitude);
            params.put("type", from.toUpperCase() + "_TO_" + to.toUpperCase());
            params.put("key", key);

            String transformUrl = "https://apis.map.qq.com/ws/coord/v1/translate";
            String response = HttpUtil.get(transformUrl, params);
            JSONObject result = JSONObject.parseObject(response);

            if (result.getInteger("status") != 0) {
                throw new CrmebException("坐标转换失败：" + result.getString("message"));
            }

            JSONObject location = result.getJSONArray("locations").getJSONObject(0);
            Map<String, BigDecimal> coordinates = new HashMap<>();
            coordinates.put("latitude", BigDecimal.valueOf(location.getDoubleValue("lat")));
            coordinates.put("longitude", BigDecimal.valueOf(location.getDoubleValue("lng")));

            return coordinates;
        } catch (Exception e) {
            throw new CrmebException("坐标转换失败：" + e.getMessage());
        }
    }

    /**
     * 判断点是否在圆形区域内
     */
    @Override
    public Boolean isPointInCircle(BigDecimal pointLat, BigDecimal pointLng, 
                                  BigDecimal centerLat, BigDecimal centerLng, BigDecimal radius) {
        try {
            BigDecimal distance = calculateStraightDistance(pointLat, pointLng, centerLat, centerLng);
            return distance.compareTo(radius) <= 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断点是否在多边形区域内
     */
    @Override
    public Boolean isPointInPolygon(BigDecimal pointLat, BigDecimal pointLng, String polygonCoordinates) {
        try {
            // 解析多边形坐标字符串
            String[] coordinates = polygonCoordinates.split(";");
            if (coordinates.length < 3) {
                return false; // 至少需要3个点构成多边形
            }

            // 转换为点数组
            double[][] polygon = new double[coordinates.length][2];
            for (int i = 0; i < coordinates.length; i++) {
                String[] coord = coordinates[i].split(",");
                polygon[i][0] = Double.parseDouble(coord[1]); // latitude
                polygon[i][1] = Double.parseDouble(coord[0]); // longitude
            }

            // 使用射线算法判断点是否在多边形内
            double x = pointLat.doubleValue();
            double y = pointLng.doubleValue();
            
            boolean inside = false;
            int j = polygon.length - 1;
            
            for (int i = 0; i < polygon.length; i++) {
                if ((polygon[i][1] > y) != (polygon[j][1] > y) &&
                    (x < (polygon[j][0] - polygon[i][0]) * (y - polygon[i][1]) / (polygon[j][1] - polygon[i][1]) + polygon[i][0])) {
                    inside = !inside;
                }
                j = i;
            }
            
            return inside;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取两点间路径
     */
    @Override
    public Map<String, Object> getRoute(BigDecimal fromLat, BigDecimal fromLng, 
                                       BigDecimal toLat, BigDecimal toLng, String mode) {
        try {
            String key = getMapKey();
            Map<String, Object> params = new HashMap<>();
            params.put("from", fromLat + "," + fromLng);
            params.put("to", toLat + "," + toLng);
            params.put("key", key);

            String routeUrl;
            switch (mode.toLowerCase()) {
                case "walking":
                    routeUrl = "https://apis.map.qq.com/ws/direction/v1/walking/";
                    break;
                case "bicycling":
                    routeUrl = "https://apis.map.qq.com/ws/direction/v1/bicycling/";
                    break;
                default:
                    routeUrl = "https://apis.map.qq.com/ws/direction/v1/driving/";
            }

            String response = HttpUtil.get(routeUrl, params);
            JSONObject result = JSONObject.parseObject(response);

            if (result.getInteger("status") != 0) {
                throw new CrmebException("路径查询失败：" + result.getString("message"));
            }

            JSONObject route = result.getJSONObject("result").getJSONArray("routes").getJSONObject(0);
            
            Map<String, Object> routeInfo = new HashMap<>();
            routeInfo.put("distance", route.getInteger("distance"));
            routeInfo.put("duration", route.getInteger("duration"));
            routeInfo.put("polyline", route.getString("polyline"));
            routeInfo.put("steps", route.getJSONArray("steps"));

            return routeInfo;
        } catch (Exception e) {
            throw new CrmebException("路径查询失败：" + e.getMessage());
        }
    }

    /**
     * 批量地址解析
     */
    @Override
    public Map<String, Map<String, BigDecimal>> batchGeocoding(String[] addresses) {
        Map<String, Map<String, BigDecimal>> results = new HashMap<>();
        
        for (String address : addresses) {
            try {
                Map<String, BigDecimal> location = geocoding(address);
                results.put(address, location);
            } catch (Exception e) {
                System.err.println("地址解析失败：" + address + " - " + e.getMessage());
                results.put(address, null);
            }
        }
        
        return results;
    }

    /**
     * 获取静态地图
     */
    @Override
    public String getStaticMap(BigDecimal centerLat, BigDecimal centerLng, Integer zoom, String size) {
        try {
            String key = getMapKey();
            return String.format("https://apis.map.qq.com/ws/staticmap/v2/?center=%s,%s&zoom=%d&size=%s&key=%s",
                    centerLat, centerLng, zoom, size, key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 地址智能提示
     */
    @Override
    public JSONArray getSuggestion(String keyword, String region) {
        try {
            String key = getMapKey();
            Map<String, Object> params = new HashMap<>();
            params.put("keyword", keyword);
            params.put("region", region);
            params.put("key", key);

            String suggestionUrl = "https://apis.map.qq.com/ws/place/v1/suggestion";
            String response = HttpUtil.get(suggestionUrl, params);
            JSONObject result = JSONObject.parseObject(response);

            if (result.getInteger("status") != 0) {
                return new JSONArray();
            }

            return result.getJSONArray("data");
        } catch (Exception e) {
            System.err.println("地址智能提示失败：" + e.getMessage());
            return new JSONArray();
        }
    }

    /**
     * 周边搜索
     */
    @Override
    public JSONArray searchNearby(BigDecimal latitude, BigDecimal longitude, String keyword, Integer radius) {
        try {
            String key = getMapKey();
            Map<String, Object> params = new HashMap<>();
            params.put("keyword", keyword);
            params.put("boundary", "nearby(" + latitude + "," + longitude + "," + radius + ")");
            params.put("key", key);

            String searchUrl = "https://apis.map.qq.com/ws/place/v1/search";
            String response = HttpUtil.get(searchUrl, params);
            JSONObject result = JSONObject.parseObject(response);

            if (result.getInteger("status") != 0) {
                return new JSONArray();
            }

            return result.getJSONArray("data");
        } catch (Exception e) {
            System.err.println("周边搜索失败：" + e.getMessage());
            return new JSONArray();
        }
    }

    /**
     * 获取城市列表
     */
    @Override
    public JSONArray getCityList() {
        try {
            String key = getMapKey();
            Map<String, Object> params = new HashMap<>();
            params.put("key", key);

            String cityUrl = "https://apis.map.qq.com/ws/district/v1/list";
            String response = HttpUtil.get(cityUrl, params);
            JSONObject result = JSONObject.parseObject(response);

            if (result.getInteger("status") != 0) {
                return new JSONArray();
            }

            return result.getJSONArray("result");
        } catch (Exception e) {
            System.err.println("获取城市列表失败：" + e.getMessage());
            return new JSONArray();
        }
    }

    /**
     * 获取配送路径优化
     */
    @Override
    public Map<String, Object> getOptimizedRoute(BigDecimal startLat, BigDecimal startLng, 
                                                BigDecimal[] destLats, BigDecimal[] destLngs) {
        try {
            // 构建目的地坐标字符串
            StringBuilder waypoints = new StringBuilder();
            for (int i = 0; i < destLats.length; i++) {
                if (i > 0) waypoints.append(";");
                waypoints.append(destLats[i]).append(",").append(destLngs[i]);
            }

            String key = getMapKey();
            Map<String, Object> params = new HashMap<>();
            params.put("from", startLat + "," + startLng);
            params.put("to", destLats[destLats.length - 1] + "," + destLngs[destLngs.length - 1]);
            params.put("waypoints", waypoints.toString());
            params.put("key", key);

            String routeUrl = "https://apis.map.qq.com/ws/direction/v1/driving/";
            String response = HttpUtil.get(routeUrl, params);
            JSONObject result = JSONObject.parseObject(response);

            if (result.getInteger("status") != 0) {
                // 如果优化路径失败，返回基本信息
                Map<String, Object> basicResult = new HashMap<>();
                basicResult.put("optimized", false);
                basicResult.put("totalDistance", 0);
                basicResult.put("totalDuration", 0);
                return basicResult;
            }

            JSONObject route = result.getJSONObject("result").getJSONArray("routes").getJSONObject(0);
            
            Map<String, Object> optimizedRoute = new HashMap<>();
            optimizedRoute.put("optimized", true);
            optimizedRoute.put("totalDistance", route.getInteger("distance"));
            optimizedRoute.put("totalDuration", route.getInteger("duration"));
            optimizedRoute.put("polyline", route.getString("polyline"));
            optimizedRoute.put("waypoints", waypoints.toString());

            return optimizedRoute;
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("optimized", false);
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }

    /**
     * 地理围栏检测
     */
    @Override
    public Boolean geoFenceCheck(BigDecimal latitude, BigDecimal longitude, String fenceId) {
        try {
            // 这里需要根据fenceId获取围栏数据
            // 实际实现中应该从数据库或缓存中获取围栏信息
            // 这里返回模拟结果
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取地图瓦片URL
     */
    @Override
    public String getTileUrl(Integer z, Integer x, Integer y, String style) {
        try {
            String key = getMapKey();
            style = style != null ? style : "1"; // 默认样式
            return String.format("https://rt%d.map.gtimg.com/tile?z=%d&x=%d&y=%d&type=%s&styleid=%s&version=117",
                    (x + y) % 4, z, x, y, "vector", style);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 多点路径规划（适用于配送员一次配送多个订单）
     */
    @Override
    public Map<String, Object> getMultiPointRoute(BigDecimal startLat, BigDecimal startLng,
                                                 BigDecimal[] wayPointLats, BigDecimal[] wayPointLngs,
                                                 BigDecimal endLat, BigDecimal endLng) {
        try {
            // 构建途经点字符串
            StringBuilder waypoints = new StringBuilder();
            for (int i = 0; i < wayPointLats.length; i++) {
                if (i > 0) waypoints.append(";");
                waypoints.append(wayPointLats[i]).append(",").append(wayPointLngs[i]);
            }

            String key = getMapKey();
            Map<String, Object> params = new HashMap<>();
            params.put("from", startLat + "," + startLng);
            params.put("to", endLat + "," + endLng);
            params.put("waypoints", waypoints.toString());
            params.put("key", key);

            String response = HttpUtil.get(DIRECTION_URL, params);
            JSONObject result = JSONObject.parseObject(response);

            if (result.getInteger("status") != 0) {
                throw new CrmebException("多点路径规划失败：" + result.getString("message"));
            }

            JSONObject route = result.getJSONObject("result").getJSONArray("routes").getJSONObject(0);
            
            Map<String, Object> multiRoute = new HashMap<>();
            multiRoute.put("totalDistance", route.getInteger("distance"));
            multiRoute.put("totalDuration", route.getInteger("duration"));
            multiRoute.put("polyline", route.getString("polyline"));
            multiRoute.put("waypointCount", wayPointLats.length);
            multiRoute.put("steps", route.getJSONArray("steps"));

            return multiRoute;
        } catch (Exception e) {
            throw new CrmebException("多点路径规划失败：" + e.getMessage());
        }
    }

    /**
     * 获取最优配送顺序
     */
    @Override
    public int[] getOptimalDeliveryOrder(BigDecimal startLat, BigDecimal startLng,
                                        BigDecimal[] destLats, BigDecimal[] destLngs) {
        try {
            // 简化的最近邻算法实现最优配送顺序
            int n = destLats.length;
            boolean[] visited = new boolean[n];
            int[] order = new int[n];
            
            BigDecimal currentLat = startLat;
            BigDecimal currentLng = startLng;
            
            for (int i = 0; i < n; i++) {
                int nearest = -1;
                BigDecimal minDistance = BigDecimal.valueOf(Double.MAX_VALUE);
                
                for (int j = 0; j < n; j++) {
                    if (!visited[j]) {
                        BigDecimal distance = calculateStraightDistance(currentLat, currentLng, destLats[j], destLngs[j]);
                        if (distance.compareTo(minDistance) < 0) {
                            minDistance = distance;
                            nearest = j;
                        }
                    }
                }
                
                if (nearest != -1) {
                    visited[nearest] = true;
                    order[i] = nearest;
                    currentLat = destLats[nearest];
                    currentLng = destLngs[nearest];
                }
            }
            
            return order;
        } catch (Exception e) {
            // 如果优化失败，返回原始顺序
            int[] defaultOrder = new int[destLats.length];
            for (int i = 0; i < destLats.length; i++) {
                defaultOrder[i] = i;
            }
            return defaultOrder;
        }
    }

    /**
     * 计算多点配送总距离和时间
     */
    @Override
    public Map<String, Object> calculateMultiPointDelivery(BigDecimal startLat, BigDecimal startLng,
                                                          BigDecimal[] destLats, BigDecimal[] destLngs) {
        try {
            // 获取最优配送顺序
            int[] optimalOrder = getOptimalDeliveryOrder(startLat, startLng, destLats, destLngs);
            
            BigDecimal totalDistance = BigDecimal.ZERO;
            Integer totalDuration = 0;
            
            BigDecimal currentLat = startLat;
            BigDecimal currentLng = startLng;
            
            // 计算按最优顺序的总距离和时间
            for (int index : optimalOrder) {
                BigDecimal distance = calculateDistanceByCoordinates(currentLat, currentLng, destLats[index], destLngs[index]);
                totalDistance = totalDistance.add(distance);
                
                // 估算时间（平均速度30km/h）
                totalDuration += (int) (distance.doubleValue() / 30 * 60); // 分钟
                
                currentLat = destLats[index];
                currentLng = destLngs[index];
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalDistance", totalDistance);
            result.put("totalDuration", totalDuration);
            result.put("optimalOrder", optimalOrder);
            result.put("pointCount", destLats.length);
            
            return result;
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            errorResult.put("totalDistance", BigDecimal.ZERO);
            errorResult.put("totalDuration", 0);
            return errorResult;
        }
    }

    /**
     * 地址标准化
     */
    @Override
    public String standardizeAddress(String address) {
        try {
            // 通过地址解析和逆解析来标准化地址
            Map<String, BigDecimal> location = geocoding(address);
            if (location != null && location.containsKey("latitude") && location.containsKey("longitude")) {
                return reverseGeocoding(location.get("latitude"), location.get("longitude"));
            }
            return address;
        } catch (Exception e) {
            return address; // 标准化失败返回原地址
        }
    }

    /**
     * 获取区域边界
     */
    @Override
    public JSONArray getRegionBoundary(String regionName) {
        try {
            String key = getMapKey();
            Map<String, Object> params = new HashMap<>();
            params.put("keyword", regionName);
            params.put("get_polygon", 1); // 获取边界多边形
            params.put("key", key);

            String boundaryUrl = "https://apis.map.qq.com/ws/district/v1/getchildren";
            String response = HttpUtil.get(boundaryUrl, params);
            JSONObject result = JSONObject.parseObject(response);

            if (result.getInteger("status") != 0) {
                return new JSONArray();
            }

            return result.getJSONArray("result");
        } catch (Exception e) {
            System.err.println("获取区域边界失败：" + e.getMessage());
            return new JSONArray();
        }
    }

    /**
     * 检查配送范围（基于多边形区域）
     */
    @Override
    public Boolean checkDeliveryRange(BigDecimal latitude, BigDecimal longitude, String areaPolygon) {
        try {
            return isPointInPolygon(latitude, longitude, areaPolygon);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取地址详细信息
     */
    @Override
    public Map<String, Object> getAddressDetail(String address) {
        try {
            // 先进行地址解析获取坐标
            Map<String, BigDecimal> location = geocoding(address);
            if (location == null) {
                return null;
            }

            String key = getMapKey();
            Map<String, Object> params = new HashMap<>();
            params.put("address", address);
            params.put("key", key);

            String response = HttpUtil.get(GEOCODING_URL, params);
            JSONObject result = JSONObject.parseObject(response);

            if (result.getInteger("status") != 0) {
                return null;
            }

            JSONObject adInfo = result.getJSONObject("result").getJSONObject("ad_info");
            
            Map<String, Object> addressDetail = new HashMap<>();
            addressDetail.put("latitude", location.get("latitude"));
            addressDetail.put("longitude", location.get("longitude"));
            addressDetail.put("province", adInfo.getString("province"));
            addressDetail.put("city", adInfo.getString("city"));
            addressDetail.put("district", adInfo.getString("district"));
            addressDetail.put("street", adInfo.getString("street"));
            addressDetail.put("streetNumber", adInfo.getString("street_number"));
            addressDetail.put("adcode", adInfo.getString("adcode"));
            
            return addressDetail;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据经纬度获取详细地址信息（包含省市区街道等）
     */
    @Override
    public Map<String, Object> locationToAddress(BigDecimal latitude, BigDecimal longitude) {
        try {
            if (ObjectUtil.isNull(latitude) || ObjectUtil.isNull(longitude)) {
                throw new CrmebException("坐标不能为空");
            }

            String key = getMapKey();
            Map<String, Object> params = new HashMap<>();
            params.put("location", latitude + "," + longitude);
            params.put("get_poi", 1); // 获取POI信息
            params.put("key", key);

            String response = HttpUtil.get(GEOCODING_URL, params);
            JSONObject result = JSONObject.parseObject(response);

            if (result.getInteger("status") != 0) {
                throw new CrmebException("坐标解析失败：" + result.getString("message"));
            }

            JSONObject resultData = result.getJSONObject("result");
            JSONObject adInfo = resultData.getJSONObject("ad_info");
            
            Map<String, Object> addressDetail = new HashMap<>();
            addressDetail.put("latitude", latitude);
            addressDetail.put("longitude", longitude);
            addressDetail.put("address", resultData.getString("address"));
            addressDetail.put("formattedAddress", resultData.getString("formatted_addresses"));
            
            // 行政区划信息
            if (adInfo != null) {
                addressDetail.put("province", adInfo.getString("province"));
                addressDetail.put("city", adInfo.getString("city"));
                addressDetail.put("district", adInfo.getString("district"));
                addressDetail.put("street", adInfo.getString("street"));
                addressDetail.put("streetNumber", adInfo.getString("street_number"));
                addressDetail.put("adcode", adInfo.getString("adcode"));
                addressDetail.put("cityCode", adInfo.getString("city_code"));
                addressDetail.put("nation", adInfo.getString("nation"));
                addressDetail.put("nationCode", adInfo.getString("nation_code"));
            }
            
            // POI信息
            JSONArray pois = resultData.getJSONArray("pois");
            if (pois != null && !pois.isEmpty()) {
                addressDetail.put("pois", pois);
            }
            
            // 地址组件
            JSONObject addressComponents = resultData.getJSONObject("address_component");
            if (addressComponents != null) {
                addressDetail.put("addressComponents", addressComponents);
            }
            
            return addressDetail;
        } catch (Exception e) {
            throw new CrmebException("坐标解析失败：" + e.getMessage());
        }
    }

    /**
     * 计算直线距离（单位：公里）
     */
    private BigDecimal calculateStraightDistance(BigDecimal lat1, BigDecimal lng1, 
                                               BigDecimal lat2, BigDecimal lng2) {
        final double R = 6371; // 地球半径，单位：公里
        
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLng = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1.doubleValue())) * 
                   Math.cos(Math.toRadians(lat2.doubleValue())) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        double distance = R * c;
        return BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
    }
}