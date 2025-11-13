package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.bill.MerchantBill;
import com.zbkj.common.request.FundsFlowRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.FinanceStatisticsResponse;

/**
 * MerchantBillService 接口
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
public interface MerchantBillService extends IService<MerchantBill> {

    /**
     * 资金监控
     *
     * @param request          查询参数
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    PageInfo<MerchantBill> getFundsFlow(FundsFlowRequest request, PageParamRequest pageParamRequest);

    /**
     * 获取商户财务统计数据
     * @param merId 商户ID
     * @param days 统计天数
     * @return 财务统计数据
     */
    FinanceStatisticsResponse getMerchantFinanceStatistics(Integer merId, Integer days);
}