package com.zbkj.service.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 地图服务缓存管理器
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
public class MapCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存前缀
    private static final String CACHE_PREFIX = "city_delivery:map:";
    private static final String GEOCODING_PREFIX = CACHE_PREFIX + "geocoding:";
    private static final String DISTANCE_PREFIX = CACHE_PREFIX + "distance:";
    private static final String ROUTE_PREFIX = CACHE_PREFIX + "route:";
    private static final String PLACE_PREFIX = CACHE_PREFIX + "place:";
    private static final String AREA_PREFIX = CACHE_PREFIX + "area:";

    // 缓存过期时间（小时）
    private static final long GEOCODING_EXPIRE = 24; // 地址解析缓存24小时
    private static final long DISTANCE_EXPIRE = 12;  // 距离计算缓存12小时
    private static final long ROUTE_EXPIRE = 6;      // 路径规划缓存6小时
    private static final long PLACE_EXPIRE = 24;     // 地点搜索缓存24小时
    private static final long AREA_EXPIRE = 168;     // 区域信息缓存7天

    /**
     * 缓存地址解析结果
     */
    public void cacheGeocodingResult(String address, Map<String, BigDecimal> result) {
        String key = GEOCODING_PREFIX + SecureUtil.md5(address.toLowerCase());
        redisTemplate.opsForValue().set(key, result, GEOCODING_EXPIRE, TimeUnit.HOURS);
    }

    /**
     * 获取缓存的地址解析结果
     */
    @SuppressWarnings("unchecked")
    public Map<String, BigDecimal> getCachedGeocodingResult(String address) {
        String key = GEOCODING_PREFIX + SecureUtil.md5(address.toLowerCase());
        return (Map<String, BigDecimal>) redisTemplate.opsForValue().get(key);
    }

    /**
     * 缓存距离计算结果
     */
    public void cacheDistanceResult(String fromAddress, String toAddress, BigDecimal distance) {
        String cacheKey = generateDistanceKey(fromAddress, toAddress);
        String key = DISTANCE_PREFIX + SecureUtil.md5(cacheKey);
        redisTemplate.opsForValue().set(key, distance, DISTANCE_EXPIRE, TimeUnit.HOURS);
    }

    /**
     * 缓存坐标距离计算结果
     */
    public void cacheDistanceByCoordinates(BigDecimal fromLat, BigDecimal fromLng, 
                                          BigDecimal toLat, BigDecimal toLng, BigDecimal distance) {
        String cacheKey = generateCoordinateDistanceKey(fromLat, fromLng, toLat, toLng);
        String key = DISTANCE_PREFIX + SecureUtil.md5(cacheKey);
        redisTemplate.opsForValue().set(key, distance, DISTANCE_EXPIRE, TimeUnit.HOURS);
    }

    /**
     * 获取缓存的距离计算结果
     */
    public BigDecimal getCachedDistanceResult(String fromAddress, String toAddress) {
        String cacheKey = generateDistanceKey(fromAddress, toAddress);
        String key = DISTANCE_PREFIX + SecureUtil.md5(cacheKey);
        return (BigDecimal) redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取缓存的坐标距离计算结果
     */
    public BigDecimal getCachedDistanceByCoordinates(BigDecimal fromLat, BigDecimal fromLng, 
                                                    BigDecimal toLat, BigDecimal toLng) {
        String cacheKey = generateCoordinateDistanceKey(fromLat, fromLng, toLat, toLng);
        String key = DISTANCE_PREFIX + SecureUtil.md5(cacheKey);
        return (BigDecimal) redisTemplate.opsForValue().get(key);
    }

    /**
     * 缓存路径规划结果
     */
    public void cacheRouteResult(String fromAddress, String toAddress, Map<String, Object> route) {
        String cacheKey = generateRouteKey(fromAddress, toAddress);
        String key = ROUTE_PREFIX + SecureUtil.md5(cacheKey);
        redisTemplate.opsForValue().set(key, route, ROUTE_EXPIRE, TimeUnit.HOURS);
    }

    /**
     * 缓存坐标路径规划结果
     */
    public void cacheRouteByCoordinates(BigDecimal fromLat, BigDecimal fromLng, 
                                       BigDecimal toLat, BigDecimal toLng, Map<String, Object> route) {
        String cacheKey = generateCoordinateRouteKey(fromLat, fromLng, toLat, toLng);
        String key = ROUTE_PREFIX + SecureUtil.md5(cacheKey);
        redisTemplate.opsForValue().set(key, route, ROUTE_EXPIRE, TimeUnit.HOURS);
    }

    /**
     * 获取缓存的路径规划结果
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCachedRouteResult(String fromAddress, String toAddress) {
        String cacheKey = generateRouteKey(fromAddress, toAddress);
        String key = ROUTE_PREFIX + SecureUtil.md5(cacheKey);
        return (Map<String, Object>) redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取缓存的坐标路径规划结果
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCachedRouteByCoordinates(BigDecimal fromLat, BigDecimal fromLng, 
                                                          BigDecimal toLat, BigDecimal toLng) {
        String cacheKey = generateCoordinateRouteKey(fromLat, fromLng, toLat, toLng);
        String key = ROUTE_PREFIX + SecureUtil.md5(cacheKey);
        return (Map<String, Object>) redisTemplate.opsForValue().get(key);
    }

    /**
     * 缓存地点搜索结果
     */
    public void cachePlaceSearchResult(String keyword, String city, JSONArray places) {
        String cacheKey = keyword + ":" + city;
        String key = PLACE_PREFIX + SecureUtil.md5(cacheKey.toLowerCase());
        redisTemplate.opsForValue().set(key, places, PLACE_EXPIRE, TimeUnit.HOURS);
    }

    /**
     * 获取缓存的地点搜索结果
     */
    public JSONArray getCachedPlaceSearchResult(String keyword, String city) {
        String cacheKey = keyword + ":" + city;
        String key = PLACE_PREFIX + SecureUtil.md5(cacheKey.toLowerCase());
        Object result = redisTemplate.opsForValue().get(key);
        return result != null ? (JSONArray) result : null;
    }

    /**
     * 缓存逆地址解析结果
     */
    public void cacheReverseGeocodingResult(BigDecimal latitude, BigDecimal longitude, String address) {
        String cacheKey = latitude + "," + longitude;
        String key = GEOCODING_PREFIX + "reverse:" + SecureUtil.md5(cacheKey);
        redisTemplate.opsForValue().set(key, address, GEOCODING_EXPIRE, TimeUnit.HOURS);
    }

    /**
     * 获取缓存的逆地址解析结果
     */
    public String getCachedReverseGeocodingResult(BigDecimal latitude, BigDecimal longitude) {
        String cacheKey = latitude + "," + longitude;
        String key = GEOCODING_PREFIX + "reverse:" + SecureUtil.md5(cacheKey);
        return (String) redisTemplate.opsForValue().get(key);
    }

    /**
     * 缓存区域检查结果
     */
    public void cacheAreaCheckResult(String address, Boolean result) {
        String key = AREA_PREFIX + "check:" + SecureUtil.md5(address.toLowerCase());
        redisTemplate.opsForValue().set(key, result, AREA_EXPIRE, TimeUnit.HOURS);
    }

    /**
     * 获取缓存的区域检查结果
     */
    public Boolean getCachedAreaCheckResult(String address) {
        String key = AREA_PREFIX + "check:" + SecureUtil.md5(address.toLowerCase());
        return (Boolean) redisTemplate.opsForValue().get(key);
    }

    /**
     * 缓存地址智能提示结果
     */
    public void cacheSuggestionResult(String keyword, String region, JSONArray suggestions) {
        String cacheKey = keyword + ":" + region;
        String key = PLACE_PREFIX + "suggestion:" + SecureUtil.md5(cacheKey.toLowerCase());
        redisTemplate.opsForValue().set(key, suggestions, PLACE_EXPIRE, TimeUnit.HOURS);
    }

    /**
     * 获取缓存的地址智能提示结果
     */
    public JSONArray getCachedSuggestionResult(String keyword, String region) {
        String cacheKey = keyword + ":" + region;
        String key = PLACE_PREFIX + "suggestion:" + SecureUtil.md5(cacheKey.toLowerCase());
        Object result = redisTemplate.opsForValue().get(key);
        return result != null ? (JSONArray) result : null;
    }

    /**
     * 清除指定地址的缓存
     */
    public void clearAddressCache(String address) {
        String pattern = CACHE_PREFIX + "*" + SecureUtil.md5(address.toLowerCase()) + "*";
        clearCacheByPattern(pattern);
    }

    /**
     * 清除所有地图缓存
     */
    public void clearAllMapCache() {
        String pattern = CACHE_PREFIX + "*";
        clearCacheByPattern(pattern);
    }

    /**
     * 清除指定模式的缓存
     */
    public void clearCacheByPattern(String pattern) {
        try {
            redisTemplate.delete(redisTemplate.keys(pattern));
        } catch (Exception e) {
            // 忽略删除失败的情况
        }
    }

    /**
     * 检查缓存是否存在
     */
    public Boolean hasCache(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置缓存过期时间
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    // ========== 私有工具方法 ==========

    /**
     * 生成距离计算缓存键
     */
    private String generateDistanceKey(String fromAddress, String toAddress) {
        return fromAddress.toLowerCase() + ":" + toAddress.toLowerCase();
    }

    /**
     * 生成坐标距离计算缓存键
     */
    private String generateCoordinateDistanceKey(BigDecimal fromLat, BigDecimal fromLng, 
                                                BigDecimal toLat, BigDecimal toLng) {
        return fromLat + "," + fromLng + ":" + toLat + "," + toLng;
    }

    /**
     * 生成路径规划缓存键
     */
    private String generateRouteKey(String fromAddress, String toAddress) {
        return fromAddress.toLowerCase() + ":" + toAddress.toLowerCase();
    }

    /**
     * 生成坐标路径规划缓存键
     */
    private String generateCoordinateRouteKey(BigDecimal fromLat, BigDecimal fromLng, 
                                             BigDecimal toLat, BigDecimal toLng) {
        return fromLat + "," + fromLng + ":" + toLat + "," + toLng;
    }
} 