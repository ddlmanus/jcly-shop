package com.zbkj.front.controller;

import com.alibaba.fastjson.JSONArray;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.AmapService;
import com.zbkj.service.service.TencentMapService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 前端地图服务控制器
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
@Slf4j
@RestController
@RequestMapping("api/front/map")
@Api(tags = "前端地图服务接口")
@Validated
public class MapController {

    @Autowired
    private TencentMapService tencentMapService;

    @Autowired
    private AmapService amapService;

    @ApiOperation(value = "地址智能提示/搜索")
    @RequestMapping(value = "/address/suggestion", method = RequestMethod.GET)
    public CommonResult<Object> getAddressSuggestion(
            @ApiParam(value = "搜索关键词", required = true) @NotBlank(message = "搜索关键词不能为空") @RequestParam String keyword,
            @ApiParam(value = "城市名称") @RequestParam(required = false) String city,
            @ApiParam(value = "地图类型", allowableValues = "tencent,amap") @RequestParam(required = false, defaultValue = "tencent") String mapType,
            @ApiParam(value = "返回数量限制") @RequestParam(required = false, defaultValue = "10") Integer limit) {
        
        try {
            if ("amap".equalsIgnoreCase(mapType)) {
                // 使用高德地图服务
                Map<String, Object> result = amapService.addressSuggestion(keyword, city);
                log.info("高德地图地址搜索 - 关键词: {}, 城市: {}, 结果: {}", keyword, city, result);
                return CommonResult.success(result);
            } else {
                // 使用腾讯地图服务（默认）
                JSONArray suggestions = tencentMapService.getSuggestion(keyword, city);
                log.info("腾讯地图地址搜索 - 关键词: {}, 城市: {}, 结果数量: {}", keyword, city, suggestions != null ? suggestions.size() : 0);
                return CommonResult.success(suggestions);
            }
        } catch (Exception e) {
            log.error("地址搜索失败：{}", e.getMessage(), e);
            return CommonResult.failed("地址搜索失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "地址转经纬度（地理编码）")
    @RequestMapping(value = "/geocoding", method = RequestMethod.GET)
    public CommonResult<Object> geocoding(
            @ApiParam(value = "地址", required = true) @NotBlank(message = "地址不能为空") @RequestParam String address,
            @ApiParam(value = "地图类型", allowableValues = "tencent,amap") @RequestParam(required = false, defaultValue = "tencent") String mapType) {
        
        try {
            if ("amap".equalsIgnoreCase(mapType)) {
                // 使用高德地图服务
                Map<String, Object> result = amapService.geocoding(address);
                log.info("高德地图地理编码 - 地址: {}, 结果: {}", address, result);
                return CommonResult.success(result);
            } else {
                // 使用腾讯地图服务（默认）
                Map<String, BigDecimal> coordinates = tencentMapService.geocoding(address);
                if (coordinates == null || coordinates.isEmpty()) {
                    return CommonResult.failed("地址解析失败，请检查地址是否正确");
                }
                
                Map<String, Object> result = new HashMap<>();
                result.put("latitude", coordinates.get("latitude"));
                result.put("longitude", coordinates.get("longitude"));
                result.put("address", address);
                
                log.info("腾讯地图地理编码 - 地址: {}, 结果: {}", address, result);
                return CommonResult.success(result);
            }
        } catch (Exception e) {
            log.error("地理编码失败：{}", e.getMessage(), e);
            return CommonResult.failed("地理编码失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "经纬度转地址（逆地理编码）")
    @RequestMapping(value = "/reverse-geocoding", method = RequestMethod.GET)
    public CommonResult<Object> reverseGeocoding(
            @ApiParam(value = "纬度", required = true) @NotNull(message = "纬度不能为空") @RequestParam BigDecimal latitude,
            @ApiParam(value = "经度", required = true) @NotNull(message = "经度不能为空") @RequestParam BigDecimal longitude,
            @ApiParam(value = "地图类型", allowableValues = "tencent,amap") @RequestParam(required = false, defaultValue = "tencent") String mapType) {
        
        try {
            if ("amap".equalsIgnoreCase(mapType)) {
                // 使用高德地图服务
                Map<String, Object> result = amapService.reverseGeocoding(latitude.doubleValue(), longitude.doubleValue());
                log.info("高德地图逆地理编码 - 坐标: [{}, {}], 结果: {}", latitude, longitude, result);
                return CommonResult.success(result);
            } else {
                // 使用腾讯地图服务（默认）
                String address = tencentMapService.reverseGeocoding(latitude, longitude);
                
                Map<String, Object> result = new HashMap<>();
                result.put("address", address);
                result.put("latitude", latitude);
                result.put("longitude", longitude);
                
                log.info("腾讯地图逆地理编码 - 坐标: [{}, {}], 地址: {}", latitude, longitude, address);
                return CommonResult.success(result);
            }
        } catch (Exception e) {
            log.error("逆地理编码失败：{}", e.getMessage(), e);
            return CommonResult.failed("逆地理编码失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "获取地图密钥")
    @RequestMapping(value = "/key", method = RequestMethod.GET)
    public CommonResult<Map<String, String>> getMapKey(
            @ApiParam(value = "地图类型", allowableValues = "tencent,amap,all") @RequestParam(required = false, defaultValue = "all") String mapType) {
        
        try {
            Map<String, String> keys = new HashMap<>();
            
            if ("tencent".equalsIgnoreCase(mapType)) {
                String tencentKey = tencentMapService.getMapKey();
                keys.put("tencent", tencentKey);
            } else if ("amap".equalsIgnoreCase(mapType)) {
                // 暂时返回空，如果有需要可以扩展AmapService接口
                keys.put("amap", "");
            } else {
                // 返回所有密钥
                String tencentKey = tencentMapService.getMapKey();
                keys.put("tencent", tencentKey);
                keys.put("amap", "");
            }
            
            log.info("获取地图密钥 - 类型: {}, 结果: {}", mapType, keys);
            return CommonResult.success(keys);
        } catch (Exception e) {
            log.error("获取地图密钥失败：{}", e.getMessage(), e);
            return CommonResult.failed("获取地图密钥失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "计算两点间距离")
    @RequestMapping(value = "/distance", method = RequestMethod.GET)
    public CommonResult<Object> calculateDistance(
            @ApiParam(value = "起点纬度") @RequestParam(required = false) BigDecimal fromLat,
            @ApiParam(value = "起点经度") @RequestParam(required = false) BigDecimal fromLng,
            @ApiParam(value = "终点纬度") @RequestParam(required = false) BigDecimal toLat,
            @ApiParam(value = "终点经度") @RequestParam(required = false) BigDecimal toLng,
            @ApiParam(value = "起点地址") @RequestParam(required = false) String fromAddress,
            @ApiParam(value = "终点地址") @RequestParam(required = false) String toAddress,
            @ApiParam(value = "地图类型", allowableValues = "tencent,amap") @RequestParam(required = false, defaultValue = "tencent") String mapType) {
        
        try {
            if ("amap".equalsIgnoreCase(mapType)) {
                // 使用高德地图服务
                if (fromLat != null && fromLng != null && toLat != null && toLng != null) {
                    Map<String, Object> result = amapService.calculateDistance(
                        fromLat.doubleValue(), fromLng.doubleValue(), 
                        toLat.doubleValue(), toLng.doubleValue()
                    );
                    log.info("高德地图距离计算 - 坐标模式，结果: {}", result);
                    return CommonResult.success(result);
                } else {
                    return CommonResult.failed("高德地图距离计算需要提供完整的经纬度坐标");
                }
            } else {
                // 使用腾讯地图服务（默认）
                BigDecimal distance;
                Map<String, Object> result = new HashMap<>();
                
                if (fromLat != null && fromLng != null && toLat != null && toLng != null) {
                    // 使用经纬度计算
                    distance = tencentMapService.calculateDistanceByCoordinates(fromLat, fromLng, toLat, toLng);
                    result.put("fromLat", fromLat);
                    result.put("fromLng", fromLng);
                    result.put("toLat", toLat);
                    result.put("toLng", toLng);
                } else if (fromAddress != null && toAddress != null) {
                    // 使用地址计算
                    distance = tencentMapService.calculateDistance(fromAddress, toAddress);
                    result.put("fromAddress", fromAddress);
                    result.put("toAddress", toAddress);
                } else {
                    return CommonResult.failed("请提供完整的经纬度坐标或地址信息");
                }
                
                result.put("distance", distance);
                result.put("unit", "公里");
                
                log.info("腾讯地图距离计算 - 结果: {}", result);
                return CommonResult.success(result);
            }
        } catch (Exception e) {
            log.error("距离计算失败：{}", e.getMessage(), e);
            return CommonResult.failed("距离计算失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "周边搜索")
    @RequestMapping(value = "/nearby-search", method = RequestMethod.GET)
    public CommonResult<JSONArray> nearbySearch(
            @ApiParam(value = "纬度", required = true) @NotNull(message = "纬度不能为空") @RequestParam BigDecimal latitude,
            @ApiParam(value = "经度", required = true) @NotNull(message = "经度不能为空") @RequestParam BigDecimal longitude,
            @ApiParam(value = "搜索关键词") @RequestParam(required = false, defaultValue = "") String keyword,
            @ApiParam(value = "搜索半径(米)") @RequestParam(required = false, defaultValue = "1000") Integer radius) {
        
        try {
            JSONArray result = tencentMapService.searchNearby(latitude, longitude, keyword, radius);
            log.info("周边搜索 - 坐标: [{}, {}], 关键词: {}, 半径: {}米, 结果数量: {}", 
                    latitude, longitude, keyword, radius, result != null ? result.size() : 0);
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("周边搜索失败：{}", e.getMessage(), e);
            return CommonResult.failed("周边搜索失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "IP定位")
    @RequestMapping(value = "/ip-location", method = RequestMethod.GET)
    public CommonResult<Object> ipLocation(
            @ApiParam(value = "IP地址，为空时使用客户端IP") @RequestParam(required = false) String ip,
            @ApiParam(value = "地图类型", allowableValues = "tencent,amap") @RequestParam(required = false, defaultValue = "amap") String mapType) {
        
        try {
            if ("amap".equalsIgnoreCase(mapType)) {
                // 使用高德地图服务
                Map<String, Object> result = amapService.ipLocation(ip);
                log.info("高德地图IP定位 - IP: {}, 结果: {}", ip, result);
                return CommonResult.success(result);
            } else {
                // 使用腾讯地图服务
                Map<String, Object> result = tencentMapService.getLocationByIP(ip);
                log.info("腾讯地图IP定位 - IP: {}, 结果: {}", ip, result);
                return CommonResult.success(result);
            }
        } catch (Exception e) {
            log.error("IP定位失败：{}", e.getMessage(), e);
            return CommonResult.failed("IP定位失败：" + e.getMessage());
        }
    }
}