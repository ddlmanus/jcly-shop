package com.zbkj.admin.service;

import com.zbkj.common.response.HomeExtendedStatisticsResponse;
import com.zbkj.common.response.HomeOperatingMerDataResponse;
import com.zbkj.common.response.HomeRateResponse;
import com.zbkj.common.response.ProductRankingResponse;
import com.zbkj.common.response.SalesStatisticsResponse;
import com.zbkj.common.response.SalesTrendChartResponse;

import java.util.List;

/**
 * 管理端 - 首页统计
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
public interface AdminHomeService {

    /**
     * 首页数据
     * @return HomeRateResponse
     */
    HomeRateResponse indexMerchantDate();

    /**
     * 经营数据
     * @return HomeOperatingMerDataResponse
     */
    HomeOperatingMerDataResponse operatingMerchantData();

    /**
     * 商户端商品支付排行榜
     */
    List<ProductRankingResponse> merchantProductPayRanking();

    /**
     * 商品浏览量排行榜
     */
    List<ProductRankingResponse> merchantProductPageviewRanking();

    /**
     * 获取扩展统计数据
     * @return HomeExtendedStatisticsResponse
     */
    HomeExtendedStatisticsResponse getExtendedStatistics();

    /**
     * 获取销售统计数据
     * @return SalesStatisticsResponse
     */
    SalesStatisticsResponse getSalesStatistics();

    /**
     * 获取销售趋势图表数据
     * @param timeType 时间类型：day/month/year
     * @param days 天数（当timeType为day时使用）
     * @return SalesTrendChartResponse
     */
    SalesTrendChartResponse getSalesTrendChart(String timeType, Integer days);

    /**
     * 获取订单数量趋势图表数据
     * @param timeType 时间类型：day/month/year
     * @param days 天数（当timeType为day时使用）
     * @return SalesTrendChartResponse
     */
    SalesTrendChartResponse getOrderCountTrendChart(String timeType, Integer days);
}
