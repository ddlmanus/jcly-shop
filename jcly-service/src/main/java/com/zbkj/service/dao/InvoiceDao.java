package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.invoice.Invoice;
import com.zbkj.common.request.InvoiceSearchRequest;
import com.zbkj.common.response.InvoicePageResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 发票 Mapper 接口
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
public interface InvoiceDao extends BaseMapper<Invoice> {

    /**
     * 发票分页列表查询
     * @param request 搜索条件
     * @return 发票列表
     */
    List<InvoicePageResponse> selectInvoicePageList(@Param("request") InvoiceSearchRequest request);

    /**
     * 获取发票各状态数量统计
     * @param request 搜索条件
     * @return 统计结果
     */
    Integer getInvoiceCountByStatus(@Param("request") InvoiceSearchRequest request, @Param("orderStatus") Integer orderStatus);

    /**
     * 根据订单号获取发票信息
     * @param orderNo 订单号
     * @return 发票信息
     */
    Invoice getByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据发票申请单号获取发票信息
     * @param invoiceNo 发票申请单号
     * @return 发票信息
     */
    Invoice getByInvoiceNo(@Param("invoiceNo") String invoiceNo);
} 