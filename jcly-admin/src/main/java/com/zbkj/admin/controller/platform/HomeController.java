package com.zbkj.admin.controller.platform;

import com.zbkj.service.service.HomeService;
import com.zbkj.common.response.*;
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
 * 平台端主页控制器
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
@RequestMapping("api/admin/platform/statistics/home")
@Api(tags = "平台端主页控制器")
public class HomeController {

    @Autowired
    private HomeService homeService;

   // @PreAuthorize("hasAuthority('platform:statistics:home:index')")
    @ApiOperation(value = "首页数据")
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public CommonResult<PlatformHomeRateResponse> indexDate() {
        return CommonResult.success(homeService.indexPlatformDate());
    }

  //  @PreAuthorize("hasAuthority('platform:statistics:home:operating:data')")
    @ApiOperation(value = "经营数据")
    @RequestMapping(value = "/operating/data", method = RequestMethod.GET)
    public CommonResult<HomeOperatingDataResponse> operatingData() {
        return CommonResult.success(homeService.operatingPlatformData());
    }

 //   @PreAuthorize("hasAuthority('platform:statistics:home:user:channel')")
    @ApiOperation(value = "用户渠道数据")
    @RequestMapping(value = "/channel", method = RequestMethod.GET)
    public CommonResult<List<UserChannelDataResponse>> getChannelData() {
        return CommonResult.success(homeService.getUserChannelData());
    }

  //  @PreAuthorize("hasAuthority('platform:statistics:home:dashboard:overview')")
    @ApiOperation(value = "仪表板概览数据")
    @RequestMapping(value = "/dashboard/overview", method = RequestMethod.GET)
    public CommonResult<PlatformDashboardOverviewResponse> getDashboardOverview() {
        return CommonResult.success(homeService.getPlatformDashboardOverview());
    }

  //  @PreAuthorize("hasAuthority('platform:statistics:home:revenue')")
    @ApiOperation(value = "平台收益汇总数据")
    @RequestMapping(value = "/revenue", method = RequestMethod.GET)
    public CommonResult<PlatformRevenueResponse> getPlatformRevenue() {
        return CommonResult.success(homeService.getPlatformRevenue());
    }

   // @PreAuthorize("hasAuthority('platform:statistics:home:product:sales:ranking')")
    @ApiOperation(value = "商品销量排行榜前10")
    @RequestMapping(value = "/product/sales/ranking", method = RequestMethod.GET)
    public CommonResult<List<ProductSalesRankingResponse>> getProductSalesRanking() {
        return CommonResult.success(homeService.getPlatformProductSalesRanking());
    }

   // @PreAuthorize("hasAuthority('platform:statistics:home:merchant:sales:ranking')")
    @ApiOperation(value = "店铺成交金额排行榜前10")
    @RequestMapping(value = "/merchant/sales/ranking", method = RequestMethod.GET)
    public CommonResult<List<MerchantSalesRankingResponse>> getMerchantSalesRanking() {
        return CommonResult.success(homeService.getPlatformMerchantSalesRanking());
    }

  //  @PreAuthorize("hasAuthority('platform:statistics:home:supplier:ranking')")
    @ApiOperation(value = "供应商供货排行榜前10")
    @RequestMapping(value = "/supplier/ranking", method = RequestMethod.GET)
    public CommonResult<List<SupplierRankingResponse>> getSupplierRanking() {
        return CommonResult.success(homeService.getPlatformSupplierRanking());
    }

    @ApiOperation(value = "用户报表数据")
    @RequestMapping(value = "/report/user", method = RequestMethod.GET)
    public CommonResult<UserReportResponse> getUserReport(@RequestParam(value = "timeType", required = false, defaultValue = "today") String timeType) {
        return CommonResult.success(homeService.getUserReport(timeType));
    }

    @ApiOperation(value = "订单报表数据")
    @RequestMapping(value = "/report/order", method = RequestMethod.GET)
    public CommonResult<OrderReportResponse> getOrderReport(@RequestParam(value = "timeType", required = false, defaultValue = "week") String timeType) {
        return CommonResult.success(homeService.getOrderReport(timeType));
    }

    @ApiOperation(value = "商品报表数据")
    @RequestMapping(value = "/report/product", method = RequestMethod.GET)
    public CommonResult<ProductReportResponse> getProductReport(
            @RequestParam(value = "timeType", required = false, defaultValue = "week") String timeType,
            @RequestParam(value = "merchantId", required = false) Integer merchantId,
            @RequestParam(value = "productStatus", required = false) Integer productStatus) {
        return CommonResult.success(homeService.getProductReport(timeType, merchantId, productStatus));
    }

    @ApiOperation(value = "获取商户列表")
    @RequestMapping(value = "/merchants", method = RequestMethod.GET)
    public CommonResult<List<com.zbkj.common.model.merchant.Merchant>> getMerchantList() {
        return CommonResult.success(homeService.getMerchantList());
    }
}



