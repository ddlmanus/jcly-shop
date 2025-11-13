package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.stock.StockOutRecord;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StockOutRequest;
import com.zbkj.common.request.StockOutRecordSearchRequest;

/**
 * 库存出库记录服务接口
 * +----------------------------------------------------------------------
 * | JCLY [ JCLY赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 * +----------------------------------------------------------------------
 * | Author: AI Assistant
 * +----------------------------------------------------------------------
 */
public interface StockOutRecordService extends IService<StockOutRecord> {

    /**
     * 出库记录分页列表
     * @param searchRequest 搜索参数
     * @param pageParamRequest 分页参数
     * @return PageInfo<StockOutRecord>
     */
    PageInfo<StockOutRecord> getStockOutRecordPage(StockOutRecordSearchRequest searchRequest, PageParamRequest pageParamRequest);

    /**
     * 出库记录分页列表（兼容原有方法）
     * @param merId 商户ID（可选，平台端查看时不传）
     * @param pageParamRequest 分页参数
     * @return PageInfo<StockOutRecord>
     */
    PageInfo<StockOutRecord> getStockOutRecordPage(Integer merId, PageParamRequest pageParamRequest);

    /**
     * 商品出库操作
     * @param request 出库请求参数
     * @param operatorId 操作员ID
     * @param operatorName 操作员姓名
     * @param merId 商户ID
     * @return Boolean
     */
    Boolean stockOut(StockOutRequest request, Integer operatorId, String operatorName, Integer merId);

    /**
     * 根据出库单号获取记录
     * @param recordNo 出库单号
     * @return StockOutRecord
     */
    StockOutRecord getByRecordNo(String recordNo);

    /**
     * 销售出库（订单出库）
     * @param productId 商品ID
     * @param sku SKU编码
     * @param quantity 出库数量
     * @param orderNo 订单号
     * @param operatorId 操作员ID
     * @param operatorName 操作员姓名
     * @param merId 商户ID
     * @return Boolean
     */
    Boolean saleStockOut(Integer productId, String sku, Integer quantity, String orderNo, 
                         Integer operatorId, String operatorName, Integer merId);
} 