package com.zbkj.admin.service;

import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.order.OrderDetail;
import com.zbkj.common.request.*;
import com.zbkj.common.response.MerchantOrderPageResponse;
import com.zbkj.common.response.OrderAdminDetailResponse;
import com.zbkj.common.response.OrderCountItemResponse;
import com.zbkj.common.response.OrderInvoiceResponse;
import com.zbkj.common.response.OrderOverviewStatisticsResponse;
import com.zbkj.common.response.OrderStatisticsResponse;
import com.zbkj.common.vo.LogisticsResultVo;

import java.util.List;

/**
*  OrderService 接口
*  +----------------------------------------------------------------------
*  | JCLY [ JCLY赋能开发者，助力企业发展 ]
*  +----------------------------------------------------------------------
*  | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
*  +----------------------------------------------------------------------
*  | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
*  +----------------------------------------------------------------------
*  | Author: dudl
*  +----------------------------------------------------------------------
*/
public interface MerchantOrderManagerService {

    /**
     * 商户端后台分页列表
     * @param request 查询参数
     * @return PageInfo
     */
    PageInfo<MerchantOrderPageResponse> getMerchantAdminPage(OrderSearchRequest request);

    /**
     * 获取商户端订单各状态数量
     */
    OrderCountItemResponse getMerchantOrderStatusNum(OrderSearchRequest request);

    /**
     * 订单详情（PC）
     * @param orderNo 订单编号
     * @return OrderAdminDetailResponse
     */
    OrderAdminDetailResponse adminDetail(String orderNo);

    /**
     * 发货
     * @param request 发货参数
     * @return Boolean
     */
    Boolean send(OrderSendRequest request);

    /**
     * 小票打印
     * @param orderNo 订单编号
     * @return 打印结果
     */
    void printReceipt(String orderNo);

    /**
     * 商户删除订单
     * @param orderNo 订单编号
     * @return Boolean
     */
    Boolean merchantDeleteByOrderNo(String orderNo);

    /**
     * 商户备注订单
     * @param request 备注参数
     * @return Boolean
     */
    Boolean merchantMark(OrderRemarkRequest request);

    /**
     * 获取订单快递信息(商户端)
     * @param invoiceId 发货单ID
     * @return LogisticsResultVo
     */
    LogisticsResultVo getLogisticsInfoByMerchant(Integer invoiceId);

    /**
     * 核销码核销订单
     * @param verifyCode 核销码
     * @return 核销结果
     */
    Boolean verificationOrderByCode(String verifyCode);

    /**
     * 订单细节详情列表
     * @param orderNo 订单号
     * @return 订单细节详情列表
     */
    List<OrderDetail> getDetailList(String orderNo);

    /**
     * 获取订单发货单列表(商户端)
     * @param orderNo 订单号
     * @return 发货单列表
     */
    List<OrderInvoiceResponse> getInvoiceListByMerchant(String orderNo);

    /**
    /**
     * 商户直接退款
     */
    Boolean directRefund(MerchantOrderDirectRefundRequest request);

    /**
     * 修改发货单配送信息
     */
    Boolean updateInvoice(OrderInvoiceUpdateRequest request);

    /**
     * 获取订单统计数据
     * @param days 统计天数
     * @return 订单统计数据
     */
    OrderStatisticsResponse getOrderStatistics(Integer days);

    /**
     * 获取订单概览统计数据
     * @param request 搜索参数
     * @return 订单概览统计数据
     */
    OrderOverviewStatisticsResponse getOrderOverviewStatistics(OrderSearchRequest request);
}
