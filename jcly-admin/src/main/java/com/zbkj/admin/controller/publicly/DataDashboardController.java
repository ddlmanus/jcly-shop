package com.zbkj.admin.controller.publicly;

import com.alibaba.fastjson.JSONObject;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.DataDashboardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 数据看板控制器
 * 提供荆楚粮油云数据平台的统计分析接口
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
//@Slf4j
//@RestController
//@RequestMapping("api/public/dashboard")
//@Api(tags = "数据看板接口控制器")
@Slf4j
@RestController
@RequestMapping("api/publicly/dashboard")
@Api(tags = "数据看板接口控制器")
@Validated
public class DataDashboardController {

    @Autowired
    private DataDashboardService dataDashboardService;

    // =================================================================================
    // 顶部统计卡片接口
    // =================================================================================

    @ApiOperation(value = "获取顶部统计数据")
    @PostMapping("/overview")
    public CommonResult<JSONObject> getOverviewData(@RequestBody JSONObject params) {
        
        // 获取当前年月作为默认值
        Integer year = params.getInteger("year");
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        
        Integer month = params.getInteger("month");
        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }
        
        JSONObject overviewData = dataDashboardService.getOverviewData(year, month);
        return CommonResult.success(overviewData);
    }

    // =================================================================================
    // 图表数据接口
    // =================================================================================

    @ApiOperation(value = "获取销售额/订单量全年统计")
    @PostMapping("/sales/yearly")
    public CommonResult<JSONObject> getYearlySalesData(@RequestBody JSONObject params) {
        
        // 获取年份，默认当前年份
        Integer year = params.getInteger("year");
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        
        // 获取数据类型，默认销售额
        String dataType = params.getString("dataType");
        if (dataType == null) {
            dataType = "sales";
        }
        
        JSONObject salesData = dataDashboardService.getYearlySalesData(year, dataType);
        return CommonResult.success(salesData);
    }

    @ApiOperation(value = "获取新增用户统计")
    @PostMapping("/users/yearly")
    public CommonResult<JSONObject> getYearlyUserData(@RequestBody JSONObject params) {
        
        // 获取年份，默认当前年份
        Integer year = params.getInteger("year");
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        
        JSONObject userData = dataDashboardService.getYearlyUserData(year);
        return CommonResult.success(userData);
    }

    @ApiOperation(value = "获取地区销售数据分布")
    @PostMapping("/regions/sales")
    public CommonResult<JSONObject> getRegionSalesData(@RequestBody JSONObject params) {
        
        // 获取年份，默认当前年份
        Integer year = params.getInteger("year");
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        
        // 获取月份，默认当前月份
        Integer month = params.getInteger("month");
        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }
        
        // 获取省份代码（可选，用于查询省下的市或市下的区）
        String regionCode = params.getString("regionCode");
        
        // 获取统计类型：sales（销售额）、orders（订单量）、users（用户量）、newUsers（新增用户量）、merchants（商户数）
        String statisticsType = params.getString("statisticsType");
        if (statisticsType == null) {
            statisticsType = "sales"; // 默认统计销售额
        }
        
        JSONObject regionData = dataDashboardService.getRegionSalesData(year, month, regionCode, statisticsType);
        return CommonResult.success(regionData);
    }

    // =================================================================================
    // 排行榜数据接口
    // =================================================================================

    @ApiOperation(value = "获取商品分类销量排行")
    @PostMapping("/ranking/categories")
    public CommonResult<JSONObject> getCategoryRanking(@RequestBody JSONObject params) {
        
        // 获取年份，默认当前年份
        Integer year = params.getInteger("year");
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        
        // 获取排行数量，默认10
        Integer limit = params.getInteger("limit");
        if (limit == null) {
            limit = 10;
        }
        
        JSONObject categoryRanking = dataDashboardService.getCategoryRanking(year, limit);
        return CommonResult.success(categoryRanking);
    }

    @ApiOperation(value = "获取分销商销量/销售额排行")
    @PostMapping("/ranking/distributors")
    public CommonResult<JSONObject> getDistributorRanking(@RequestBody JSONObject params) {
        
        // 获取年份，默认当前年份
        Integer year = params.getInteger("year");
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        
        // 获取月份，可选
        Integer month = params.getInteger("month");
        
        // 获取排行数量，可选（不传则返回所有商户）
        Integer limit = params.getInteger("limit");
        
        // 获取数据类型，默认销售额
        String dataType = params.getString("dataType");
        if (dataType == null) {
            dataType = "sales";
        }
        
        JSONObject distributorRanking = dataDashboardService.getDistributorRanking(year, month, limit, dataType);
        return CommonResult.success(distributorRanking);
    }

    // =================================================================================
    // 下拉选项接口
    // =================================================================================

    @ApiOperation(value = "获取省份列表（用于下拉选择）")
    @GetMapping("/provinces")
    public CommonResult<List<Map<String, Object>>> getAvailableProvinces() {
        
        List<Map<String, Object>> provinces = dataDashboardService.getAvailableProvinces();
        return CommonResult.success(provinces);
    }

}