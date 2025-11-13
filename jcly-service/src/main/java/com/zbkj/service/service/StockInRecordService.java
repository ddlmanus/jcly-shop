package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.stock.StockInRecord;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StockInRequest;
import com.zbkj.common.request.StockInRecordSearchRequest;

/**
 * 库存入库记录服务接口
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
public interface StockInRecordService extends IService<StockInRecord> {

    /**
     * 入库记录分页列表
     * @param searchRequest 搜索参数
     * @param pageParamRequest 分页参数
     * @return PageInfo<StockInRecord>
     */
    PageInfo<StockInRecord> getStockInRecordPage(StockInRecordSearchRequest searchRequest, PageParamRequest pageParamRequest);

    /**
     * 入库记录分页列表（兼容原有方法）
     * @param merId 商户ID（可选，平台端查看时不传）
     * @param pageParamRequest 分页参数
     * @return PageInfo<StockInRecord>
     */
    PageInfo<StockInRecord> getStockInRecordPage(Integer merId, PageParamRequest pageParamRequest);

    /**
     * 商品入库操作
     * @param request 入库请求参数
     * @param operatorId 操作员ID
     * @param operatorName 操作员姓名
     * @param merId 商户ID
     * @return Boolean
     */
    Boolean stockIn(StockInRequest request, Integer operatorId, String operatorName, Integer merId);

    /**
     * 根据入库单号获取记录
     * @param recordNo 入库单号
     * @return StockInRecord
     */
    StockInRecord getByRecordNo(String recordNo);
} 