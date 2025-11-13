package com.zbkj.service.service;

import com.alibaba.fastjson.JSONArray;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 腾讯地图服务接口
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
public interface TencentMapService {

    /**
     * 地址解析（获取经纬度）
     */
    Map<String, BigDecimal> geocoding(String address);

    /**
     * 逆地址解析（根据经纬度获取地址）
     */
    String reverseGeocoding(BigDecimal latitude, BigDecimal longitude);

    /**
     * 计算两点间距离（使用地址）
     */
    BigDecimal calculateDistance(String fromAddress, String toAddress);

    /**
     * 计算两点间距离（使用坐标）
     */
    BigDecimal calculateDistanceByCoordinates(BigDecimal fromLat, BigDecimal fromLng, BigDecimal toLat, BigDecimal toLng);

    /**
     * 路径规划（使用地址）
     */
    Map<String, Object> getDirections(String fromAddress, String toAddress);

    /**
     * 路径规划（使用坐标）
     */
    Map<String, Object> getDirectionsByCoordinates(BigDecimal fromLat, BigDecimal fromLng, BigDecimal toLat, BigDecimal toLng);

    /**
     * 地点搜索
     */
    JSONArray searchPlaces(String keyword, String city, Integer pageSize);

    /**
     * 获取行政区划
     */
    JSONArray getDistrict(String keyword);

    /**
     * IP定位
     */
    Map<String, Object> getLocationByIP(String ip);

    /**
     * 坐标转换
     */
    Map<String, BigDecimal> coordinateTransform(BigDecimal latitude, BigDecimal longitude, String from, String to);

    /**
     * 判断点是否在圆形区域内
     */
    Boolean isPointInCircle(BigDecimal pointLat, BigDecimal pointLng, 
                           BigDecimal centerLat, BigDecimal centerLng, BigDecimal radius);

    /**
     * 判断点是否在多边形区域内
     */
    Boolean isPointInPolygon(BigDecimal pointLat, BigDecimal pointLng, String polygonCoordinates);

    /**
     * 获取两点间路径
     */
    Map<String, Object> getRoute(BigDecimal fromLat, BigDecimal fromLng, 
                                BigDecimal toLat, BigDecimal toLng, String mode);

    /**
     * 批量地址解析
     */
    Map<String, Map<String, BigDecimal>> batchGeocoding(String[] addresses);

    /**
     * 获取静态地图
     */
    String getStaticMap(BigDecimal centerLat, BigDecimal centerLng, Integer zoom, String size);

    /**
     * 地址智能提示
     */
    JSONArray getSuggestion(String keyword, String region);

    /**
     * 周边搜索
     */
    JSONArray searchNearby(BigDecimal latitude, BigDecimal longitude, String keyword, Integer radius);

    /**
     * 获取城市列表
     */
    JSONArray getCityList();

    /**
     * 路况信息
     */
    Map<String, Object> getTrafficInfo(BigDecimal latitude, BigDecimal longitude, Integer radius);

    /**
     * 获取配送路径优化
     */
    Map<String, Object> getOptimizedRoute(BigDecimal startLat, BigDecimal startLng, 
                                         BigDecimal[] destLats, BigDecimal[] destLngs);

    /**
     * 地理围栏检测
     */
    Boolean geoFenceCheck(BigDecimal latitude, BigDecimal longitude, String fenceId);

    /**
     * 获取地图瓦片URL
     */
    String getTileUrl(Integer z, Integer x, Integer y, String style);

    /**
     * 预估配送时间
     */
    Integer estimateDeliveryTime(String fromAddress, String toAddress);

    /**
     * 验证地址有效性
     */
    Boolean validateAddress(String address);

    /**
     * 多点路径规划（适用于配送员一次配送多个订单）
     */
    Map<String, Object> getMultiPointRoute(BigDecimal startLat, BigDecimal startLng,
                                          BigDecimal[] wayPointLats, BigDecimal[] wayPointLngs,
                                          BigDecimal endLat, BigDecimal endLng);

    /**
     * 获取最优配送顺序
     */
    int[] getOptimalDeliveryOrder(BigDecimal startLat, BigDecimal startLng,
                                 BigDecimal[] destLats, BigDecimal[] destLngs);

    /**
     * 计算多点配送总距离和时间
     */
    Map<String, Object> calculateMultiPointDelivery(BigDecimal startLat, BigDecimal startLng,
                                                   BigDecimal[] destLats, BigDecimal[] destLngs);

    /**
     * 地址标准化
     */
    String standardizeAddress(String address);

    /**
     * 获取区域边界
     */
    JSONArray getRegionBoundary(String regionName);

    /**
     * 检查配送范围（基于多边形区域）
     */
    Boolean checkDeliveryRange(BigDecimal latitude, BigDecimal longitude, String areaPolygon);

    /**
     * 获取地址详细信息
     */
    Map<String, Object> getAddressDetail(String address);

    /**
     * 根据经纬度获取详细地址信息（包含省市区街道等）
     */
    Map<String, Object> locationToAddress(BigDecimal latitude, BigDecimal longitude);

    String getMapKey();
}