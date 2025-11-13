package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.stock.Stock;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StockSearchRequest;
import com.zbkj.common.response.StockResponse;

/**
 * 库存管理服务接口
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
public interface StockService extends IService<Stock> {

    /**
     * 库存分页列表
     * @param request 查询参数
     * @param pageParamRequest 分页参数
     * @return PageInfo<StockResponse>
     */
    PageInfo<StockResponse> getStockPage(StockSearchRequest request, PageParamRequest pageParamRequest);

    /**
     * 根据商品ID和SKU获取库存信息
     * @param productId 商品ID
     * @param sku SKU编码
     * @return Stock
     */
    Stock getByProductIdAndSku(Integer productId, String sku);

    /**
     * 创建或更新库存
     * @param productId 商品ID
     * @param sku SKU编码
     * @param merId 商户ID
     * @param stock 库存数量
     * @param costPrice 成本价
     * @return Boolean
     */
    Boolean createOrUpdateStock(Integer productId, String sku, Integer merId, Integer stock, java.math.BigDecimal costPrice);

    /**
     * 增加库存
     * @param productId 商品ID
     * @param sku SKU编码
     * @param quantity 数量
     * @return Boolean
     */
    Boolean addStock(Integer productId, String sku, Integer quantity);

    /**
     * 减少库存
     * @param productId 商品ID
     * @param sku SKU编码
     * @param quantity 数量
     * @return Boolean
     */
    Boolean reduceStock(Integer productId, String sku, Integer quantity);

    /**
     * 冻结库存
     * @param productId 商品ID
     * @param sku SKU编码
     * @param quantity 数量
     * @return Boolean
     */
    Boolean freezeStock(Integer productId, String sku, Integer quantity);

    /**
     * 解冻库存
     * @param productId 商品ID
     * @param sku SKU编码
     * @param quantity 数量
     * @return Boolean
     */
    Boolean unfreezeStock(Integer productId, String sku, Integer quantity);

    /**
     * 根据商品ID删除所有库存记录
     * @param productId 商品ID
     * @return Boolean
     */
    Boolean deleteByProductId(Integer productId);
} 