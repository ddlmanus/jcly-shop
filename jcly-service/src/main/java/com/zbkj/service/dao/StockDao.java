package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.stock.Stock;
import com.zbkj.common.response.StockResponse;

import java.util.List;
import java.util.Map;

/**
 * 商品库存表 Mapper 接口
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
public interface StockDao extends BaseMapper<Stock> {

    /**
     * 库存分页列表
     * @param map 查询参数
     */
    List<StockResponse> getStockPage(Map<String, Object> map);
} 