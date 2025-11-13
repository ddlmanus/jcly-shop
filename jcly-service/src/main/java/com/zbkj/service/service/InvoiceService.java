package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.invoice.Invoice;
import com.zbkj.common.request.InvoiceAuditRequest;
import com.zbkj.common.request.InvoiceSearchRequest;
import com.zbkj.common.request.MerchantInvoiceMergeRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.InvoiceCountResponse;
import com.zbkj.common.response.InvoiceDetailResponse;
import com.zbkj.common.response.InvoicePageResponse;

/**
 * 发票服务接口
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
public interface InvoiceService extends IService<Invoice> {

    /**
     * 发票分页列表
     * @param request 搜索条件
     * @return 分页列表
     */
    PageInfo<InvoicePageResponse> getPageList(InvoiceSearchRequest request);

    /**
     * 获取发票各状态数量统计
     * @param request 搜索条件
     * @return 统计结果
     */
    InvoiceCountResponse getInvoiceCount(InvoiceSearchRequest request);

    /**
     * 获取发票详情
     * @param invoiceId 发票ID
     * @return 发票详情
     */
    InvoiceDetailResponse getInvoiceDetail(Integer invoiceId);

    /**
     * 根据发票申请单号获取发票详情
     * @param invoiceNo 发票申请单号
     * @return 发票详情
     */
    InvoiceDetailResponse getInvoiceDetailByNo(String invoiceNo);

    /**
     * 发票审核
     * @param request 审核请求
     * @param auditorId 审核人员ID
     * @return 审核结果
     */
    Boolean auditInvoice(InvoiceAuditRequest request, Integer auditorId);

    /**
     * 根据订单号获取发票信息
     * @param orderNo 订单号
     * @return 发票信息
     */
    Invoice getByOrderNo(String orderNo);

    /**
     * 根据发票申请单号获取发票信息
     * @param invoiceNo 发票申请单号
     * @return 发票信息
     */
    Invoice getByInvoiceNo(String invoiceNo);

    /**
     * 生成发票申请单号
     * @return 发票申请单号
     */
    String generateInvoiceNo();

    /**
     * 删除发票（逻辑删除）
     * @param invoiceId 发票ID
     * @return 删除结果
     */
    Boolean deleteInvoice(Integer invoiceId);

    /**
     * 批量删除发票（逻辑删除）
     * @param invoiceIds 发票ID列表
     * @return 删除结果
     */
    Boolean batchDeleteInvoice(String invoiceIds);

    /**
     * 商户端发票分页列表
     */
    PageInfo<InvoicePageResponse> getMerchantPage(InvoiceSearchRequest request, PageParamRequest pageParamRequest);

    /**
     * 商户端发票数量统计
     */
    InvoiceCountResponse getMerchantCount(InvoiceSearchRequest request);

    /**
     * 商户端发票详情
     */
    InvoiceDetailResponse getMerchantDetail(Integer id);

    /**
     * 商户端发票审核
     */
    Boolean merchantAudit(InvoiceAuditRequest request);

    /**
     * 商户端合并开票
     */
    Boolean merchantMergeInvoice(MerchantInvoiceMergeRequest request);

    /**
     * 商户端删除发票
     */
    Boolean merchantDelete(Integer id);
} 