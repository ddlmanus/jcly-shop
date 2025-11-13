package com.zbkj.admin.controller.publicly;

import com.zbkj.common.result.CommonResult;
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
 * 腾讯地图服务控制器
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
@RequestMapping("api/publicly/tencent/map")
@Api(tags = "腾讯地图服务控制器")
@Validated
public class TencentMapController {

    @Autowired
    private TencentMapService tencentMapService;

    @ApiOperation(value = "地址解析（获取经纬度）")
    @RequestMapping(value = "/geocoding", method = RequestMethod.GET)
    public CommonResult<Map<String, BigDecimal>> geocoding(
            @ApiParam(value = "地址", required = true) @NotBlank(message = "地址不能为空") @RequestParam String address) {
        
        try {
            Map<String, BigDecimal> result = tencentMapService.geocoding(address);
            if (result == null || result.isEmpty()) {
                return CommonResult.failed("地址解析失败，请检查地址是否正确");
            }
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("地址解析失败：{}", e.getMessage(), e);
            return CommonResult.failed("地址解析失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "逆地址解析（根据经纬度获取地址）")
    @RequestMapping(value = "/reverse-geocoding", method = RequestMethod.GET)
    public CommonResult<String> reverseGeocoding(
            @ApiParam(value = "经度", required = true) @NotNull(message = "经度不能为空") @RequestParam BigDecimal longitude,
            @ApiParam(value = "纬度", required = true) @NotNull(message = "纬度不能为空") @RequestParam BigDecimal latitude) {
        
        try {
            String address = tencentMapService.reverseGeocoding(latitude, longitude);
            return CommonResult.success(address);
        } catch (Exception e) {
            log.error("逆地址解析失败：{}", e.getMessage(), e);
            return CommonResult.failed("逆地址解析失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "计算两点间距离")
    @RequestMapping(value = "/distance", method = RequestMethod.GET)
    public CommonResult<BigDecimal> calculateDistance(
            @ApiParam(value = "起点经度") @RequestParam(required = false) BigDecimal fromLng,
            @ApiParam(value = "起点纬度") @RequestParam(required = false) BigDecimal fromLat,
            @ApiParam(value = "终点经度") @RequestParam(required = false) BigDecimal toLng,
            @ApiParam(value = "终点纬度") @RequestParam(required = false) BigDecimal toLat,
            @ApiParam(value = "起点地址") @RequestParam(required = false) String fromAddress,
            @ApiParam(value = "终点地址") @RequestParam(required = false) String toAddress) {
        
        try {
            BigDecimal distance;
            
            if (fromLng != null && fromLat != null && toLng != null && toLat != null) {
                // 使用经纬度直接计算
                distance = tencentMapService.calculateDistanceByCoordinates(fromLat, fromLng, toLat, toLng);
            } else if (fromAddress != null && toAddress != null) {
                // 使用地址计算
                distance = tencentMapService.calculateDistance(fromAddress, toAddress);
            } else {
                return CommonResult.failed("请提供完整的经纬度坐标或地址信息");
            }
            
            return CommonResult.success(distance);
        } catch (Exception e) {
            log.error("距离计算失败：{}", e.getMessage(), e);
            return CommonResult.failed("距离计算失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "地址智能提示")
    @RequestMapping(value = "/address-suggestion", method = RequestMethod.GET)
    public CommonResult<com.alibaba.fastjson.JSONArray> getAddressSuggestion(
            @ApiParam(value = "搜索关键词", required = true) @NotBlank(message = "搜索关键词不能为空") @RequestParam String keyword,
            @ApiParam(value = "城市名称") @RequestParam(required = false, defaultValue = "北京") String city,
            @ApiParam(value = "页面大小", required = false, defaultValue = "10") Integer pageSize) {
        
        try {
            com.alibaba.fastjson.JSONArray suggestions = tencentMapService.searchPlaces(keyword, city, pageSize);
            return CommonResult.success(suggestions);
        } catch (Exception e) {
            log.error("地址智能提示失败：{}", e.getMessage(), e);
            return CommonResult.failed("地址智能提示失败：" + e.getMessage());
        }
    }
    @ApiOperation(value = "根据省市区街道详细地址获取经纬度")
    @RequestMapping(value = "/geocoding/address-detail", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> geocodingByAddressDetail(
            @ApiParam(value = "省份", required = true) @NotBlank(message = "省份不能为空") @RequestParam String province,
            @ApiParam(value = "城市", required = true) @NotBlank(message = "城市不能为空") @RequestParam String city,
            @ApiParam(value = "区县", required = true) @NotBlank(message = "区县不能为空") @RequestParam String district,
            @ApiParam(value = "街道") @RequestParam(required = false) String street,
            @ApiParam(value = "详细地址", required = true) @NotBlank(message = "详细地址不能为空") @RequestParam String detail) {
        
        try {
            // 组合完整地址
            StringBuilder fullAddress = new StringBuilder();
            fullAddress.append(province);
            if (!province.endsWith("省") && !province.endsWith("市") && !province.endsWith("区")) {
                if (province.contains("内蒙古") || province.contains("广西") || province.contains("西藏") || 
                    province.contains("宁夏") || province.contains("新疆")) {
                    // 自治区
                } else if (province.contains("北京") || province.contains("天津") || 
                          province.contains("上海") || province.contains("重庆")) {
                    // 直辖市
                } else {
                    fullAddress.append("省");
                }
            }
            
            fullAddress.append(city);
            if (!city.endsWith("市") && !city.endsWith("区") && !city.endsWith("县")) {
                fullAddress.append("市");
            }
            
            fullAddress.append(district);
            if (!district.endsWith("区") && !district.endsWith("县") && !district.endsWith("市")) {
                fullAddress.append("区");
            }
            
            if (street != null && !street.trim().isEmpty()) {
                fullAddress.append(street);
                if (!street.endsWith("街道") && !street.endsWith("镇") && !street.endsWith("乡")) {
                    fullAddress.append("街道");
                }
            }
            
            fullAddress.append(detail);
            
            String completeAddress = fullAddress.toString();
            log.info("组合后的完整地址: {}", completeAddress);
            
            // 调用地址解析服务
            Map<String, BigDecimal> coordinates = tencentMapService.geocoding(completeAddress);
            if (coordinates == null || coordinates.isEmpty()) {
                return CommonResult.failed("地址解析失败，请检查地址信息是否正确");
            }
            
            // 获取地址详细信息
            Map<String, Object> addressDetail = tencentMapService.getAddressDetail(completeAddress);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("latitude", coordinates.get("latitude"));
            result.put("longitude", coordinates.get("longitude"));
            result.put("fullAddress", completeAddress);
            result.put("inputProvince", province);
            result.put("inputCity", city);
            result.put("inputDistrict", district);
            result.put("inputStreet", street);
            result.put("inputDetail", detail);
            
            // 如果获取到了详细信息，添加到结果中
            if (addressDetail != null && !addressDetail.isEmpty()) {
                result.put("standardizedProvince", addressDetail.get("province"));
                result.put("standardizedCity", addressDetail.get("city"));
                result.put("standardizedDistrict", addressDetail.get("district"));
                result.put("standardizedStreet", addressDetail.get("street"));
                result.put("adcode", addressDetail.get("adcode"));
            }
            
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("根据省市区街道详细地址获取经纬度失败：{}", e.getMessage(), e);
            return CommonResult.failed("地址解析失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "根据经纬度获取详细地址信息")
    @RequestMapping(value = "/location/to/address", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> locationToAddress(
            @ApiParam(value = "纬度", required = true) @NotNull(message = "纬度不能为空") @RequestParam BigDecimal latitude,
            @ApiParam(value = "经度", required = true) @NotNull(message = "经度不能为空") @RequestParam BigDecimal longitude) {
        
        try {
            Map<String, Object> result = tencentMapService.locationToAddress(latitude, longitude);
            if (result == null || result.isEmpty()) {
                return CommonResult.failed("坐标解析失败，请检查坐标是否正确");
            }
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("根据经纬度获取地址信息失败：{}", e.getMessage(), e);
            return CommonResult.failed("坐标解析失败：" + e.getMessage());
        }
    }

    /**
     * 获取腾讯地图key
     */
    @ApiOperation(value = "获取腾讯地图key")
    @RequestMapping(value = "/get/key", method = RequestMethod.GET)
     public CommonResult<String> getMapKey() {
      return CommonResult.success(tencentMapService.getMapKey());
    }
}