package com.zbkj.admin.service.impl;

import com.zbkj.admin.service.AdminHomeService;
import com.zbkj.common.model.admin.SystemAdmin;
import com.zbkj.common.response.HomeExtendedStatisticsResponse;
import com.zbkj.common.response.HomeOperatingMerDataResponse;
import com.zbkj.common.response.HomeRateResponse;
import com.zbkj.common.response.ProductRankingResponse;
import com.zbkj.common.response.SalesStatisticsResponse;
import com.zbkj.common.response.SalesTrendChartResponse;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.service.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户表 服务实现类
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
@Service
public class AdminHomeServiceImpl implements AdminHomeService {

    @Autowired
    private HomeService homeService;

    /**
     * 首页数据
     * @return HomeRateResponse
     */
    @Override
    public HomeRateResponse indexMerchantDate() {
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        return homeService.indexMerchantDate(systemAdmin);
    }

    /**
     * 经营数据：
     * @return HomeOperatingMerDataResponse
     */
    @Override
    public HomeOperatingMerDataResponse operatingMerchantData() {
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        return homeService.operatingMerchantData(systemAdmin);
    }

    /**
     * 商户端商品支付排行榜
     */
    @Override
    public List<ProductRankingResponse> merchantProductPayRanking() {
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        return homeService.merchantProductPayRanking(systemAdmin);
    }

    /**
     * 商品浏览量排行榜
     */
    @Override
    public List<ProductRankingResponse> merchantProductPageviewRanking() {
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        return homeService.merchantProductPageviewRanking(systemAdmin);
    }

    /**
     * 获取扩展统计数据
     */
    @Override
    public HomeExtendedStatisticsResponse getExtendedStatistics() {
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        return homeService.getExtendedStatistics(systemAdmin);
    }

    /**
     * 获取销售统计数据
     */
    @Override
    public SalesStatisticsResponse getSalesStatistics() {
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        return homeService.getSalesStatistics(systemAdmin);
    }

    /**
     * 获取销售趋势图表数据
     */
    @Override
    public SalesTrendChartResponse getSalesTrendChart(String timeType, Integer days) {
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        return homeService.getSalesTrendChart(systemAdmin, timeType, days);
    }

    /**
     * 获取订单数量趋势图表数据
     */
    @Override
    public SalesTrendChartResponse getOrderCountTrendChart(String timeType, Integer days) {
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        return homeService.getOrderCountTrendChart(systemAdmin, timeType, days);
    }

}
