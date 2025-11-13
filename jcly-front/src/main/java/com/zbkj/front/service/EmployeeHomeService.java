package com.zbkj.front.service;

import com.zbkj.common.response.HomeOperatingMerDataResponse;
import com.zbkj.common.response.HomeRateResponse;
import com.zbkj.common.response.ProductRankingResponse;

import java.util.List;

/**
 * 移动端商家管理 - 首页统计
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
public interface EmployeeHomeService {

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
}
