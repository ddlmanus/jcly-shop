package com.zbkj.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.model.stock.Stock;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StockSearchRequest;
import com.zbkj.common.response.StockResponse;
import com.zbkj.service.dao.StockDao;
import com.zbkj.service.service.StockService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存管理服务实现类
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
@Service
public class StockServiceImpl extends ServiceImpl<StockDao, Stock> implements StockService {

    @Resource
    private StockDao dao;

    @Override
    public PageInfo<StockResponse> getStockPage(StockSearchRequest request, PageParamRequest pageParamRequest) {
        Page<Object> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());

        Map<String, Object> map = new HashMap<>();
        if (StrUtil.isNotBlank(request.getKeywords())) {
            map.put("keywords", request.getKeywords());
        }
        if (ObjectUtil.isNotNull(request.getProductId())) {
            map.put("productId", request.getProductId());
        }
        if (ObjectUtil.isNotNull(request.getMerId())) {
            map.put("merId", request.getMerId());
        }
        if (ObjectUtil.isNotNull(request.getStockAlert())) {
            map.put("stockAlert", request.getStockAlert());
        }

        List<StockResponse> list = dao.getStockPage(map);
        return CommonPage.copyPageInfo(page, list);
    }

    @Override
    public Stock getByProductIdAndSku(Integer productId, String sku) {
        LambdaQueryWrapper<Stock> lqw = Wrappers.lambdaQuery();
        lqw.eq(Stock::getProductId, productId);
        lqw.eq(Stock::getSku, sku);
        lqw.eq(Stock::getIsDel, false);
        lqw.last(" limit 1");
        return getOne(lqw);
    }

    @Override
    public Boolean createOrUpdateStock(Integer productId, String sku, Integer merId, Integer stock, BigDecimal costPrice) {
        Stock existStock = getByProductIdAndSku(productId, sku);
        if (ObjectUtil.isNotNull(existStock)) {
            // 更新现有库存
            existStock.setStock(stock);
            existStock.setCostPrice(costPrice);
            existStock.setUpdateTime(new Date());
            return updateById(existStock);
        } else {
            // 创建新库存记录
            Stock newStock = new Stock();
            newStock.setProductId(productId);
            newStock.setSku(sku);
            newStock.setMerId(merId);
            newStock.setStock(stock);
            newStock.setFreezeStock(0);
            newStock.setCostPrice(costPrice);
            newStock.setCreateTime(new Date());
            newStock.setUpdateTime(new Date());
            newStock.setIsDel(false);
            return save(newStock);
        }
    }

    @Override
    public Boolean addStock(Integer productId, String sku, Integer quantity) {
        UpdateWrapper<Stock> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql(StrUtil.format("stock = stock + {}", quantity));
        updateWrapper.eq("product_id", productId);
        updateWrapper.eq("sku", sku);
        updateWrapper.eq("is_del", false);
        updateWrapper.set("update_time", new Date());
        
        boolean update = update(updateWrapper);
        if (!update) {
            throw new CrmebException("增加库存失败，商品ID：" + productId + "，SKU：" + sku);
        }
        return update;
    }

    @Override
    public Boolean reduceStock(Integer productId, String sku, Integer quantity) {
        UpdateWrapper<Stock> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql(StrUtil.format("stock = stock - {}", quantity));
        updateWrapper.eq("product_id", productId);
        updateWrapper.eq("sku", sku);
        updateWrapper.eq("is_del", false);
        updateWrapper.last(StrUtil.format(" and (stock - {} >= 0)", quantity));
        updateWrapper.set("update_time", new Date());
        
        boolean update = update(updateWrapper);
        if (!update) {
            throw new CrmebException("减少库存失败，库存不足，商品ID：" + productId + "，SKU：" + sku);
        }
        return update;
    }

    @Override
    public Boolean freezeStock(Integer productId, String sku, Integer quantity) {
        UpdateWrapper<Stock> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql(StrUtil.format("freeze_stock = freeze_stock + {}", quantity));
        updateWrapper.eq("product_id", productId);
        updateWrapper.eq("sku", sku);
        updateWrapper.eq("is_del", false);
        updateWrapper.last(StrUtil.format(" and (stock - freeze_stock - {} >= 0)", quantity));
        updateWrapper.set("update_time", new Date());
        
        boolean update = update(updateWrapper);
        if (!update) {
            throw new CrmebException("冻结库存失败，可用库存不足，商品ID：" + productId + "，SKU：" + sku);
        }
        return update;
    }

    @Override
    public Boolean unfreezeStock(Integer productId, String sku, Integer quantity) {
        UpdateWrapper<Stock> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql(StrUtil.format("freeze_stock = freeze_stock - {}", quantity));
        updateWrapper.eq("product_id", productId);
        updateWrapper.eq("sku", sku);
        updateWrapper.eq("is_del", false);
        updateWrapper.last(StrUtil.format(" and (freeze_stock - {} >= 0)", quantity));
        updateWrapper.set("update_time", new Date());
        
        boolean update = update(updateWrapper);
        if (!update) {
            throw new CrmebException("解冻库存失败，冻结库存不足，商品ID：" + productId + "，SKU：" + sku);
        }
        return update;
    }

    @Override
    public Boolean deleteByProductId(Integer productId) {
        LambdaUpdateWrapper<Stock> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Stock::getIsDel, true);
        updateWrapper.set(Stock::getUpdateTime, new Date());
        updateWrapper.eq(Stock::getProductId, productId);
        updateWrapper.eq(Stock::getIsDel, false);
        
        return update(updateWrapper);
    }
} 