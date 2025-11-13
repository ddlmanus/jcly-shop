package com.zbkj.service.service;

import java.util.Map;

/**
 * 高德地图服务接口
 * 
 * @author 系统
 * @date 2025-01-14
 */
public interface AmapService {

    /**
     * 地理编码 - 地址转坐标
     * @param address 地址
     * @return 坐标信息
     */
    Map<String, Object> geocoding(String address);

    /**
     * 逆地理编码 - 坐标转地址
     * @param lat 纬度
     * @param lng 经度
     * @return 地址信息
     */
    Map<String, Object> reverseGeocoding(Double lat, Double lng);

    /**
     * 距离计算
     * @param lat1 起点纬度
     * @param lng1 起点经度
     * @param lat2 终点纬度
     * @param lng2 终点经度
     * @return 距离信息
     */
    Map<String, Object> calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2);

    /**
     * 地址建议
     * @param keyword 关键字
     * @param city 城市
     * @return 建议列表
     */
    Map<String, Object> addressSuggestion(String keyword, String city);

    /**
     * IP定位
     * @param ip IP地址，为空时使用客户端IP
     * @return 定位信息
     */
    Map<String, Object> ipLocation(String ip);
}