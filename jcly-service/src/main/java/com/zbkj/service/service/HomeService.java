package com.zbkj.service.service;

import com.zbkj.common.model.admin.SystemAdmin;
import com.zbkj.common.response.*;
import com.zbkj.common.vo.MyRecord;

import java.util.List;
import java.util.Map;

/**
 * 首页统计
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
public interface HomeService {

    /**
     * 首页数据
     * @return HomeRateResponse
     */
    HomeRateResponse indexMerchantDate(SystemAdmin systemAdmin);

    /**
     * 经营数据
     * @return HomeOperatingMerDataResponse
     */
    HomeOperatingMerDataResponse operatingMerchantData(SystemAdmin systemAdmin);

    /**
     * 平台端首页数据
     * @return PlatformHomeRateResponse
     */
    PlatformHomeRateResponse indexPlatformDate();

    /**
     * 平台端首页经营数据
     */
    HomeOperatingDataResponse operatingPlatformData();

    /**
     * 平台端首页获取用户渠道数据
     */
    List<UserChannelDataResponse> getUserChannelData();

    /**
     * 商户端商品支付排行榜
     */
    List<ProductRankingResponse> merchantProductPayRanking(SystemAdmin systemAdmin);

    /**
     * 商品浏览量排行榜
     */
    List<ProductRankingResponse> merchantProductPageviewRanking(SystemAdmin systemAdmin);

    /**
     * 平台仪表板概览数据
     */
    PlatformDashboardOverviewResponse getPlatformDashboardOverview();

    /**
     * 平台收益汇总数据
     */
    PlatformRevenueResponse getPlatformRevenue();

    /**
     * 平台商品销量排行榜前10
     */
    List<ProductSalesRankingResponse> getPlatformProductSalesRanking();

    /**
     * 平台店铺成交金额排行榜前10
     */
    List<MerchantSalesRankingResponse> getPlatformMerchantSalesRanking();

    /**
     * 平台供应商供货排行榜前10
     */
    List<SupplierRankingResponse> getPlatformSupplierRanking();

    /**
     * 获取用户报表数据
     * @param timeType 时间类型：today-今日, yesterday-昨日, thisWeek-本周, thisMonth-本月
     */
    UserReportResponse getUserReport(String timeType);

    /**
     * 获取订单报表数据
     * @param timeType 时间类型：week-按周, month-按月, year-按年
     */
    OrderReportResponse getOrderReport(String timeType);

    /**
     * 获取商品报表数据
     * @param timeType 时间类型：week-按周, month-按月, year-按年
     * @param merchantId 商户ID，null表示所有商户
     * @param productStatus 商品状态：0-下架, 1-上架, null表示所有状态
     */
    ProductReportResponse getProductReport(String timeType, Integer merchantId, Integer productStatus);

    /**
     * 获取商户列表
     */
    List<com.zbkj.common.model.merchant.Merchant> getMerchantList();

    /**
     * 获取扩展统计数据（订单转化率、会员数量、库存周转率、商品评价数量）
     * @param systemAdmin 当前登录的商户管理员
     * @return HomeExtendedStatisticsResponse
     */
    HomeExtendedStatisticsResponse getExtendedStatistics(SystemAdmin systemAdmin);

    /**
     * 获取销售统计数据（总销售额、按月按年销售额、订单量统计）
     * @param systemAdmin 当前登录的商户管理员
     * @return SalesStatisticsResponse
     */
    SalesStatisticsResponse getSalesStatistics(SystemAdmin systemAdmin);

    /**
     * 获取销售趋势图表数据
     * @param systemAdmin 当前登录的商户管理员
     * @param timeType 时间类型：day/month/year
     * @param days 天数（当timeType为day时使用）
     * @return SalesTrendChartResponse
     */
    SalesTrendChartResponse getSalesTrendChart(SystemAdmin systemAdmin, String timeType, Integer days);

    /**
     * 获取订单数量趋势图表数据
     * @param systemAdmin 当前登录的商户管理员
     * @param timeType 时间类型：day/month/year
     * @param days 天数（当timeType为day时使用）
     * @return SalesTrendChartResponse
     */
    SalesTrendChartResponse getOrderCountTrendChart(SystemAdmin systemAdmin, String timeType, Integer days);

    /**
     * 大屏数据
     * @return
     */
    PlantFormScanResponse indexScanDate();

    List<PlatformHomeAreaResponse> indexArea();
}
