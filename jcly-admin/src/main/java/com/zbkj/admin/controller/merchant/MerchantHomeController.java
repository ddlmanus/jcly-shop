package com.zbkj.admin.controller.merchant;

import com.zbkj.admin.service.AdminHomeService;
import com.zbkj.service.service.HomeService;
import com.zbkj.common.response.HomeExtendedStatisticsResponse;
import com.zbkj.common.response.HomeOperatingMerDataResponse;
import com.zbkj.common.response.HomeRateResponse;
import com.zbkj.common.response.ProductRankingResponse;
import com.zbkj.common.response.SalesStatisticsResponse;
import com.zbkj.common.response.SalesTrendChartResponse;
import com.zbkj.common.result.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 商户端主页控制器
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
@RequestMapping("api/admin/merchant/statistics/home")
@Api(tags = "商户端主页控制器")
public class MerchantHomeController {

    @Autowired
    private AdminHomeService adminHomeService;

    @PreAuthorize("hasAuthority('merchant:statistics:home:index')")
    @ApiOperation(value = "首页数据")
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public CommonResult<HomeRateResponse> indexDate() {
        return CommonResult.success(adminHomeService.indexMerchantDate());
    }

    @PreAuthorize("hasAuthority('merchant:statistics:home:operating:data')")
    @ApiOperation(value = "经营数据")
    @RequestMapping(value = "/operating/data", method = RequestMethod.GET)
    public CommonResult<HomeOperatingMerDataResponse> operatingData() {
        return CommonResult.success(adminHomeService.operatingMerchantData());
    }

    @PreAuthorize("hasAuthority('merchant:statistics:home:product:pay:ranking')")
    @ApiOperation(value = "商品支付排行榜")
    @RequestMapping(value = "/product/pay/ranking", method = RequestMethod.GET)
    public CommonResult<List<ProductRankingResponse>> productPayRanking() {
        return CommonResult.success(adminHomeService.merchantProductPayRanking());
    }

    @PreAuthorize("hasAuthority('merchant:statistics:home:product:pageview:ranking')")
    @ApiOperation(value = "商品浏览量排行榜")
    @RequestMapping(value = "/product/pageview/ranking", method = RequestMethod.GET)
    public CommonResult<List<ProductRankingResponse>> productPageviewRanking() {
        return CommonResult.success(adminHomeService.merchantProductPageviewRanking());
    }

   // @PreAuthorize("hasAuthority('merchant:statistics:home:extended')")
    @ApiOperation(value = "获取扩展统计数据")
    @RequestMapping(value = "/extended/statistics", method = RequestMethod.GET)
    public CommonResult<HomeExtendedStatisticsResponse> getExtendedStatistics() {
        return CommonResult.success(adminHomeService.getExtendedStatistics());
    }

   // @PreAuthorize("hasAuthority('merchant:statistics:home:sales:statistics')")
    @ApiOperation(value = "获取销售统计数据")
    @RequestMapping(value = "/sales/statistics", method = RequestMethod.GET)
    public CommonResult<SalesStatisticsResponse> getSalesStatistics() {
        return CommonResult.success(adminHomeService.getSalesStatistics());
    }

   // @PreAuthorize("hasAuthority('merchant:statistics:home:sales:trend:chart')")
    @ApiOperation(value = "获取销售趋势图表数据")
    @RequestMapping(value = "/sales/trend/chart", method = RequestMethod.GET)
    public CommonResult<SalesTrendChartResponse> getSalesTrendChart(
            @RequestParam(defaultValue = "day") String timeType,
            @RequestParam(defaultValue = "30") Integer days) {
        return CommonResult.success(adminHomeService.getSalesTrendChart(timeType, days));
    }

   // @PreAuthorize("hasAuthority('merchant:statistics:home:order:count:trend:chart')")
    @ApiOperation(value = "获取订单数量趋势图表数据")
    @RequestMapping(value = "/order/count/trend/chart", method = RequestMethod.GET)
    public CommonResult<SalesTrendChartResponse> getOrderCountTrendChart(
            @RequestParam(defaultValue = "day") String timeType,
            @RequestParam(defaultValue = "30") Integer days) {
        return CommonResult.success(adminHomeService.getOrderCountTrendChart(timeType, days));
    }
}



