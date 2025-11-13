package com.zbkj.front.controller;

import com.zbkj.common.model.platform.Store;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.StoreService;
import com.zbkj.service.service.TencentMapService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 小程序门店API控制器
 */
@Slf4j
@RestController
@RequestMapping("api/front/store")
@Api(tags = "小程序门店接口")
public class ApiStoreController {

    @Autowired
    private StoreService storeService;

    @Autowired
    private TencentMapService tencentMapService;

    @ApiOperation(value = "获取附近的门店")
    @RequestMapping(value = "/nearby", method = RequestMethod.GET)
    public CommonResult<List<Store>> getNearbyStores(
            @ApiParam(name = "latitude", value = "纬度", required = true) @RequestParam Double latitude,
            @ApiParam(name = "longitude", value = "经度", required = true) @RequestParam Double longitude,
            @ApiParam(name = "radius", value = "搜索半径(公里)", required = false) @RequestParam(required = false, defaultValue = "20.0") Double radius,
            @ApiParam(name = "limit", value = "返回数量限制", required = false) @RequestParam(required = false, defaultValue = "10") Integer limit) {
        List<Store> list = storeService.getNearbyStores(latitude, longitude, radius, limit);
        return CommonResult.success(list);
    }

    @ApiOperation(value = "获取门店详情")
    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
    public CommonResult<Store> getStoreDetail(@PathVariable Integer id) {
        Store store = storeService.getStoreDetail(id);
        return CommonResult.success(store);
    }

    @ApiOperation(value = "获取所有门店列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<List<Store>> getAllStores() {
        List<Store> list = storeService.getAllStores();
        return CommonResult.success(list);
    }

    @ApiOperation(value = "根据地址搜索附近门店")
    @RequestMapping(value = "/nearby-by-address", method = RequestMethod.GET)
    public CommonResult<List<Store>> getNearbyStoresByAddress(
            @ApiParam(name = "address", value = "地址", required = true) @NotBlank(message = "地址不能为空") @RequestParam String address,
            @ApiParam(name = "radius", value = "搜索半径(公里)", required = false) @RequestParam(required = false, defaultValue = "20.0") Double radius,
            @ApiParam(name = "limit", value = "返回数量限制", required = false) @RequestParam(required = false, defaultValue = "10") Integer limit) {
        
        try {
            // 1. 使用腾讯地图服务将地址转换为经纬度
            Map<String, BigDecimal> coordinates = tencentMapService.geocoding(address);
            if (coordinates == null || coordinates.isEmpty()) {
                return CommonResult.failed("地址解析失败，请检查地址是否正确");
            }
            
            BigDecimal latitude = coordinates.get("latitude");
            BigDecimal longitude = coordinates.get("longitude");
            
            if (latitude == null || longitude == null) {
                return CommonResult.failed("无法获取地址对应的经纬度坐标");
            }
            
            log.info("地址解析成功 - 地址: {}, 经度: {}, 纬度: {}", address, longitude, latitude);
            
            // 2. 根据经纬度搜索附近门店
            List<Store> nearbyStores = storeService.getNearbyStores(
                latitude.doubleValue(), 
                longitude.doubleValue(), 
                radius, 
                limit
            );
            
            log.info("根据地址搜索门店完成 - 地址: {}, 找到门店数量: {}", address, nearbyStores.size());
            return CommonResult.success(nearbyStores);
            
        } catch (Exception e) {
            log.error("根据地址搜索门店失败 - 地址: {}, 错误: {}", address, e.getMessage(), e);
            return CommonResult.failed("搜索失败：" + e.getMessage());
        }
    }
}