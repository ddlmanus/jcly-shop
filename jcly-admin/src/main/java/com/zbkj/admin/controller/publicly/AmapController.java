package com.zbkj.admin.controller.publicly;

import com.zbkj.common.annotation.LogControllerAnnotation;
import com.zbkj.common.enums.MethodType;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.utils.RequestUtil;
import com.zbkj.service.service.AmapService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 高德地图API控制器
 * 
 * @author 系统
 * @date 2025-01-14
 */
@Slf4j
@RestController
@RequestMapping("api/admin/map/amap")
@Api(tags = "高德地图API")
public class AmapController {

    @Autowired
    private AmapService amapService;

    @Value("${amap.key:}")
    private String amapKey;

    @Value("${amap.secret:}")
    private String amapSecret;

    /**
     * 获取高德地图API密钥
     */
    @ApiOperation(value = "获取高德地图API密钥")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.SELECT, description = "获取高德地图API密钥")
    @RequestMapping(value = "/key", method = RequestMethod.GET)
    public CommonResult<String> getAmapKey() {
        if (amapKey == null || amapKey.trim().isEmpty()) {
            return CommonResult.failed("高德地图API密钥未配置");
        }
        return CommonResult.success(amapKey);
    }

    /**
     * 高德地图地理编码 - 地址转坐标
     */
    @ApiOperation(value = "高德地图地理编码")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.SELECT, description = "高德地图地理编码")
    @ApiImplicitParam(name = "address", value = "地址", required = true, dataType = "String")
    @RequestMapping(value = "/geocoding", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> geocoding(@RequestParam String address) {
        try {
            Map<String, Object> result = amapService.geocoding(address);
            log.info("高德地图地理编码请求 - 地址: {}, 结果: {}", address, result);
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("高德地图地理编码失败", e);
            return CommonResult.failed("地理编码失败: " + e.getMessage());
        }
    }

    /**
     * 高德地图逆地理编码 - 坐标转地址
     */
    @ApiOperation(value = "高德地图逆地理编码")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.SELECT, description = "高德地图逆地理编码")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "lat", value = "纬度", required = true, dataType = "Double"),
        @ApiImplicitParam(name = "lng", value = "经度", required = true, dataType = "Double")
    })
    @RequestMapping(value = "/reverse-geocoding", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> reverseGeocoding(@RequestParam Double lat, @RequestParam Double lng) {
        try {
            Map<String, Object> result = amapService.reverseGeocoding(lat, lng);
            log.info("高德地图逆地理编码请求 - 坐标: {},{}, 结果: {}", lat, lng, result);
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("高德地图逆地理编码失败", e);
            return CommonResult.failed("逆地理编码失败: " + e.getMessage());
        }
    }

    /**
     * 高德地图距离计算
     */
    @ApiOperation(value = "高德地图距离计算")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.SELECT, description = "高德地图距离计算")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "lat1", value = "起点纬度", required = true, dataType = "Double"),
        @ApiImplicitParam(name = "lng1", value = "起点经度", required = true, dataType = "Double"),
        @ApiImplicitParam(name = "lat2", value = "终点纬度", required = true, dataType = "Double"),
        @ApiImplicitParam(name = "lng2", value = "终点经度", required = true, dataType = "Double")
    })
    @RequestMapping(value = "/distance", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> calculateDistance(
            @RequestParam Double lat1, @RequestParam Double lng1,
            @RequestParam Double lat2, @RequestParam Double lng2) {
        try {
            Map<String, Object> result = amapService.calculateDistance(lat1, lng1, lat2, lng2);
            log.info("高德地图距离计算请求 - 起点: {},{}, 终点: {},{}, 结果: {}", 
                    lat1, lng1, lat2, lng2, result);
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("高德地图距离计算失败", e);
            return CommonResult.failed("距离计算失败: " + e.getMessage());
        }
    }

    /**
     * 高德地图地址建议
     */
    @ApiOperation(value = "高德地图地址建议")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.SELECT, description = "高德地图地址建议")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "keyword", value = "关键字", required = true, dataType = "String"),
        @ApiImplicitParam(name = "city", value = "城市", dataType = "String")
    })
    @RequestMapping(value = "/suggestion", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> addressSuggestion(
            @RequestParam String keyword, @RequestParam(required = false) String city) {
        try {
            Map<String, Object> result = amapService.addressSuggestion(keyword, city);
            log.info("高德地图地址建议请求 - 关键字: {}, 城市: {}, 结果: {}", keyword, city, result);
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("高德地图地址建议失败", e);
            return CommonResult.failed("地址建议失败: " + e.getMessage());
        }
    }

    /**
     * 高德地图IP定位
     */
    @ApiOperation(value = "高德地图IP定位")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.SELECT, description = "高德地图IP定位")
    @ApiImplicitParam(name = "ip", value = "IP地址(可选，为空时使用客户端IP)", dataType = "String")
    @RequestMapping(value = "/ip-location", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> ipLocation(
            @RequestParam(required = false) String ip, HttpServletRequest request) {
        try {
            // 如果没有传入IP，则获取客户端IP
            if (ip == null || ip.trim().isEmpty()) {
                ip = RequestUtil.getClientIp();
            }
            
            Map<String, Object> result = amapService.ipLocation(ip);
            log.info("高德地图IP定位请求 - IP: {}, 结果: {}", ip, result);
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("高德地图IP定位失败", e);
            return CommonResult.failed("IP定位失败: " + e.getMessage());
        }
    }
} 