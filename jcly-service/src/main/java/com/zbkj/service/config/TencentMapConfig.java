package com.zbkj.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯地图配置类
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
@Data
@Configuration
@ConfigurationProperties(prefix = "tencent.map")
public class TencentMapConfig {

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * Secret密钥
     */
    private String secretKey;

    /**
     * 是否启用缓存
     */
    private Boolean cacheEnabled = true;

    /**
     * 缓存过期时间（小时）
     */
    private Integer cacheExpireHours = 24;

    /**
     * API调用重试次数
     */
    private Integer retryTimes = 3;

    /**
     * API调用超时时间（毫秒）
     */
    private Integer timeoutMs = 5000;

    /**
     * 默认请求并发数限制
     */
    private Integer maxConcurrentRequests = 100;

    /**
     * 是否启用API调用日志
     */
    private Boolean logEnabled = true;

    /**
     * API基础URL配置
     */
    public static class ApiUrls {
        /**
         * 地址解析URL
         */
        private String geocodingUrl = "https://apis.map.qq.com/ws/geocoder/v1/";

        /**
         * 距离计算URL
         */
        private String distanceUrl = "https://apis.map.qq.com/ws/distance/v1/";

        /**
         * 路径规划URL
         */
        private String directionUrl = "https://apis.map.qq.com/ws/direction/v1/driving/";

        /**
         * IP定位URL
         */
        private String ipLocationUrl = "https://apis.map.qq.com/ws/location/v1/ip";

        /**
         * 地点搜索URL
         */
        private String placeSearchUrl = "https://apis.map.qq.com/ws/place/v1/search";

        /**
         * 地址智能提示URL
         */
        private String suggestionUrl = "https://apis.map.qq.com/ws/place/v1/suggestion";

        /**
         * 行政区划URL
         */
        private String districtUrl = "https://apis.map.qq.com/ws/district/v1/list";

        /**
         * 坐标转换URL
         */
        private String coordinateTransformUrl = "https://apis.map.qq.com/ws/coord/v1/translate";

        /**
         * 静态地图URL
         */
        private String staticMapUrl = "https://apis.map.qq.com/ws/staticmap/v2/";

        // Getter methods
        public String getGeocodingUrl() { return geocodingUrl; }
        public String getDistanceUrl() { return distanceUrl; }
        public String getDirectionUrl() { return directionUrl; }
        public String getIpLocationUrl() { return ipLocationUrl; }
        public String getPlaceSearchUrl() { return placeSearchUrl; }
        public String getSuggestionUrl() { return suggestionUrl; }
        public String getDistrictUrl() { return districtUrl; }
        public String getCoordinateTransformUrl() { return coordinateTransformUrl; }
        public String getStaticMapUrl() { return staticMapUrl; }
    }

    /**
     * API URLs配置
     */
    private ApiUrls urls = new ApiUrls();

    /**
     * 限流配置
     */
    @Data
    public static class RateLimit {
        /**
         * 是否启用限流
         */
        private Boolean enabled = true;

        /**
         * 每秒最大请求数
         */
        private Integer requestsPerSecond = 50;

        /**
         * 每分钟最大请求数
         */
        private Integer requestsPerMinute = 3000;

        /**
         * 每天最大请求数
         */
        private Integer requestsPerDay = 100000;
    }

    /**
     * 限流配置
     */
    private RateLimit rateLimit = new RateLimit();

    /**
     * 重试配置
     */
    @Data
    public static class RetryConfig {
        /**
         * 是否启用重试
         */
        private Boolean enabled = true;

        /**
         * 最大重试次数
         */
        private Integer maxAttempts = 3;

        /**
         * 重试间隔（毫秒）
         */
        private Integer retryIntervalMs = 1000;

        /**
         * 需要重试的HTTP状态码
         */
        private Integer[] retryHttpCodes = {500, 502, 503, 504};

        /**
         * 需要重试的错误代码
         */
        private Integer[] retryErrorCodes = {110, 120, 121, 122, 123};
    }

    /**
     * 重试配置
     */
    private RetryConfig retry = new RetryConfig();

    /**
     * 监控配置
     */
    @Data
    public static class MonitorConfig {
        /**
         * 是否启用监控
         */
        private Boolean enabled = true;

        /**
         * 慢查询阈值（毫秒）
         */
        private Integer slowQueryThresholdMs = 3000;

        /**
         * 错误率告警阈值（百分比）
         */
        private Double errorRateThreshold = 5.0;

        /**
         * 监控数据保留天数
         */
        private Integer dataRetentionDays = 7;
    }

    /**
     * 监控配置
     */
    private MonitorConfig monitor = new MonitorConfig();

    /**
     * 获取完整的地址解析URL
     */
    public String getGeocodingUrl() {
        return urls.getGeocodingUrl();
    }

    /**
     * 获取完整的距离计算URL
     */
    public String getDistanceUrl() {
        return urls.getDistanceUrl();
    }

    /**
     * 获取完整的路径规划URL
     */
    public String getDirectionUrl() {
        return urls.getDirectionUrl();
    }

    /**
     * 获取完整的IP定位URL
     */
    public String getIpLocationUrl() {
        return urls.getIpLocationUrl();
    }

    /**
     * 获取完整的地点搜索URL
     */
    public String getPlaceSearchUrl() {
        return urls.getPlaceSearchUrl();
    }

    /**
     * 获取完整的地址智能提示URL
     */
    public String getSuggestionUrl() {
        return urls.getSuggestionUrl();
    }

    /**
     * 获取完整的行政区划URL
     */
    public String getDistrictUrl() {
        return urls.getDistrictUrl();
    }

    /**
     * 获取完整的坐标转换URL
     */
    public String getCoordinateTransformUrl() {
        return urls.getCoordinateTransformUrl();
    }

    /**
     * 获取完整的静态地图URL
     */
    public String getStaticMapUrl() {
        return urls.getStaticMapUrl();
    }

    /**
     * 验证配置是否完整
     */
    public boolean isConfigValid() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    /**
     * 获取API调用超时时间
     */
    public Integer getTimeoutMs() {
        return timeoutMs != null ? timeoutMs : 5000;
    }

    /**
     * 获取重试次数
     */
    public Integer getRetryTimes() {
        return retryTimes != null ? retryTimes : 3;
    }
} 