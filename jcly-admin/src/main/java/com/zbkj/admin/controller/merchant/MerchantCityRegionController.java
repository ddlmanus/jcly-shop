package com.zbkj.admin.controller.merchant;

import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.CityRegionService;
import com.zbkj.common.vo.CityVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商户端城市区域控制器
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
@RequestMapping("api/admin/city")
@Api(tags = "商户端城市区域控制器")
public class MerchantCityRegionController {

    @Autowired
    private CityRegionService cityRegionService;

    @ApiOperation(value = "获取省份列表")
    @RequestMapping(value = "/province", method = RequestMethod.GET)
    public CommonResult<List<CityVo>> getProvinceList() {
        try {
            List<CityVo> provinceList = cityRegionService.getProvinceList();
            return CommonResult.success(provinceList);
        } catch (Exception e) {
            log.error("获取省份列表失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取省份列表失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "根据省份ID获取城市列表")
    @RequestMapping(value = "/city/{provinceId}", method = RequestMethod.GET)
    public CommonResult<List<CityVo>> getCityList(
            @ApiParam(value = "省份ID", required = true) @PathVariable Integer provinceId) {
        try {
            List<CityVo> cityList = cityRegionService.getCityListByProvinceId(provinceId);
            return CommonResult.success(cityList);
        } catch (Exception e) {
            log.error("获取城市列表失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取城市列表失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "根据城市ID获取区县列表")
    @RequestMapping(value = "/district/{cityId}", method = RequestMethod.GET)
    public CommonResult<List<CityVo>> getDistrictList(
            @ApiParam(value = "城市ID", required = true) @PathVariable Integer cityId) {
        try {
            List<CityVo> districtList = cityRegionService.getDistrictListByCityId(cityId);
            return CommonResult.success(districtList);
        } catch (Exception e) {
            log.error("获取区县列表失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取区县列表失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "根据区县ID获取街道列表")
    @RequestMapping(value = "/street/{districtId}", method = RequestMethod.GET)
    public CommonResult<List<CityVo>> getStreetList(
            @ApiParam(value = "区县ID", required = true) @PathVariable Integer districtId) {
        try {
            List<CityVo> streetList = cityRegionService.getStreetListByDistrictId(districtId);
            return CommonResult.success(streetList);
        } catch (Exception e) {
            log.error("获取街道列表失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取街道列表失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "获取城市树形结构")
    @RequestMapping(value = "/tree", method = RequestMethod.GET)
    public CommonResult<List<CityVo>> getCityTree() {
        try {
            List<CityVo> cityTree = cityRegionService.getRegionListTree();
            return CommonResult.success(cityTree);
        } catch (Exception e) {
            log.error("获取城市树形结构失败: {}", e.getMessage(), e);
            return CommonResult.failed("获取城市树形结构失败: " + e.getMessage());
        }
    }
}



