package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zbkj.common.model.product.Product;
import com.zbkj.common.model.product.ProductAttrValue;
import com.zbkj.common.model.stock.Stock;
import com.zbkj.common.model.stock.StockInRecord;
import com.zbkj.service.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 库存一致性检查和修复服务
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
public class StockConsistencyServiceImpl implements StockConsistencyService {

    private static final Logger logger = LoggerFactory.getLogger(StockConsistencyServiceImpl.class);

    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductAttrValueService productAttrValueService;
    
    @Autowired
    private StockService stockService;
    
    @Autowired
    private StockInRecordService stockInRecordService;

    /**
     * 库存一致性检查结果
     */
    public static class ConsistencyCheckResult {
        private boolean isConsistent = true;
        private List<String> issues = new ArrayList<>();
        private int checkedProducts = 0;
        private int checkedSkus = 0;
        private int inconsistentSkus = 0;
        private int missingStockRecords = 0;
        private int repairedItems = 0;

        // Getters and setters
        public boolean isConsistent() { return isConsistent; }
        public void setConsistent(boolean consistent) { isConsistent = consistent; }
        
        public List<String> getIssues() { return issues; }
        public void addIssue(String issue) { 
            this.issues.add(issue); 
            this.isConsistent = false;
        }
        
        public int getCheckedProducts() { return checkedProducts; }
        public void setCheckedProducts(int checkedProducts) { this.checkedProducts = checkedProducts; }
        
        public int getCheckedSkus() { return checkedSkus; }
        public void setCheckedSkus(int checkedSkus) { this.checkedSkus = checkedSkus; }
        
        public int getInconsistentSkus() { return inconsistentSkus; }
        public void setInconsistentSkus(int inconsistentSkus) { this.inconsistentSkus = inconsistentSkus; }
        
        public int getMissingStockRecords() { return missingStockRecords; }
        public void setMissingStockRecords(int missingStockRecords) { this.missingStockRecords = missingStockRecords; }
        
        public int getRepairedItems() { return repairedItems; }
        public void setRepairedItems(int repairedItems) { this.repairedItems = repairedItems; }

        @Override
        public String toString() {
            return String.format("库存一致性检查结果 - 检查商品:%d, 检查SKU:%d, 不一致SKU:%d, 缺失库存记录:%d, 修复项:%d, 一致性:%s", 
                checkedProducts, checkedSkus, inconsistentSkus, missingStockRecords, repairedItems, isConsistent ? "正常" : "异常");
        }
    }

    @Override
    public ConsistencyCheckResult checkStockConsistency(Integer merId, boolean autoRepair) {
        ConsistencyCheckResult result = new ConsistencyCheckResult();
        
        try {
            logger.info("开始库存一致性检查，商户ID={}，自动修复={}", merId, autoRepair);
            
            // 1. 获取所有商品
            LambdaQueryWrapper<Product> productQuery = Wrappers.lambdaQuery();
            if (ObjectUtil.isNotNull(merId)) {
                productQuery.eq(Product::getMerId, merId);
            }
            productQuery.eq(Product::getIsDel, false);
            List<Product> products = productService.list(productQuery);
            
            result.setCheckedProducts(products.size());
            logger.info("找到{}个商品需要检查", products.size());
            
            for (Product product : products) {
                checkProductStockConsistency(product, result, autoRepair);
            }
            
            logger.info("库存一致性检查完成：{}", result);
            
        } catch (Exception e) {
            logger.error("库存一致性检查异常", e);
            result.addIssue("检查过程异常：" + e.getMessage());
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsistencyCheckResult repairStockInconsistency(Integer merId) {
        return checkStockConsistency(merId, true);
    }

    /**
     * 检查单个商品的库存一致性
     */
    private void checkProductStockConsistency(Product product, ConsistencyCheckResult result, boolean autoRepair) {
        try {
            // 获取商品的所有SKU
            LambdaQueryWrapper<ProductAttrValue> skuQuery = Wrappers.lambdaQuery();
            skuQuery.eq(ProductAttrValue::getProductId, product.getId());
            skuQuery.eq(ProductAttrValue::getIsDel, false);
            List<ProductAttrValue> skus = productAttrValueService.list(skuQuery);
            
            if (CollUtil.isEmpty(skus)) {
                result.addIssue("商品[" + product.getName() + "]没有找到SKU记录");
                return;
            }
            
            result.setCheckedSkus(result.getCheckedSkus() + skus.size());
            
            // 计算SKU库存总和
            Integer calculatedTotalStock = skus.stream().mapToInt(ProductAttrValue::getStock).sum();
            
            // 检查商品总库存是否一致
            if (!product.getStock().equals(calculatedTotalStock)) {
                String issue = String.format("商品[%s]总库存不一致：商品表=%d，SKU总和=%d", 
                    product.getName(), product.getStock(), calculatedTotalStock);
                result.addIssue(issue);
                result.setInconsistentSkus(result.getInconsistentSkus() + 1);
                
                if (autoRepair) {
                    // 修复商品总库存
                    Product updateProduct = new Product();
                    updateProduct.setId(product.getId());
                    updateProduct.setStock(calculatedTotalStock);
                    updateProduct.setUpdateTime(new Date());
                    productService.updateById(updateProduct);
                    result.setRepairedItems(result.getRepairedItems() + 1);
                    logger.info("已修复商品[{}]总库存：{} → {}", product.getName(), product.getStock(), calculatedTotalStock);
                }
            }
            
            // 检查每个SKU的库存记录
            for (ProductAttrValue sku : skus) {
                checkSkuStockConsistency(product, sku, result, autoRepair);
            }
            
        } catch (Exception e) {
            result.addIssue("检查商品[" + product.getName() + "]时异常：" + e.getMessage());
            logger.error("检查商品[{}]库存一致性异常", product.getName(), e);
        }
    }

    /**
     * 检查单个SKU的库存一致性
     */
    private void checkSkuStockConsistency(Product product, ProductAttrValue sku, ConsistencyCheckResult result, boolean autoRepair) {
        try {
            // 检查库存管理表中是否存在对应记录
            Stock stockRecord = stockService.getByProductIdAndSku(product.getId(), sku.getSku());
            
            if (ObjectUtil.isNull(stockRecord)) {
                // 缺失库存管理记录
                String issue = String.format("SKU[%s]缺失库存管理记录，商品：%s", sku.getSku(), product.getName());
                result.addIssue(issue);
                result.setMissingStockRecords(result.getMissingStockRecords() + 1);
                
                if (autoRepair && sku.getStock() > 0) {
                    // 创建库存记录
                    boolean created = stockService.createOrUpdateStock(
                        product.getId(), 
                        sku.getSku(), 
                        product.getMerId(), 
                        sku.getStock(), 
                        sku.getCost()
                    );
                    
                    if (created) {
                        // 创建初始入库记录
                        StockInRecord record = new StockInRecord();
                        record.setRecordNo("REPAIR" + DateUtil.format(new Date(), "yyyyMMdd") + IdUtil.createSnowflake(1, 1).nextIdStr());
                        record.setProductId(product.getId());
                        record.setProductName(product.getName());
                        record.setProductImages(product.getImage());
                        record.setSku(sku.getSku());
                        record.setMerId(product.getMerId());
                        record.setBeforeStock(0);
                        record.setInQuantity(sku.getStock());
                        record.setAfterStock(sku.getStock());
                        record.setCostPrice(sku.getCost());
                        record.setTotalAmount(sku.getCost().multiply(new BigDecimal(sku.getStock())));
                        record.setSupplier("系统修复");
                        record.setRemark("库存一致性修复 - 创建缺失的库存记录");
                        record.setOperatorId(0); // 系统操作
                        record.setOperatorName("系统自动修复");
                        record.setCreateTime(new Date());
                        record.setUpdateTime(new Date());
                        record.setIsDel(false);
                        stockInRecordService.save(record);
                        
                        result.setRepairedItems(result.getRepairedItems() + 1);
                        logger.info("已修复SKU[{}]库存记录，商品：{}，库存：{}", sku.getSku(), product.getName(), sku.getStock());
                    }
                }
            } else {
                // 检查库存数量是否一致
                if (!stockRecord.getStock().equals(sku.getStock())) {
                    String issue = String.format("SKU[%s]库存不一致：库存表=%d，规格表=%d，商品：%s", 
                        sku.getSku(), stockRecord.getStock(), sku.getStock(), product.getName());
                    result.addIssue(issue);
                    result.setInconsistentSkus(result.getInconsistentSkus() + 1);
                    
                    if (autoRepair) {
                        // 以商品规格表的库存为准，更新库存管理表
                        boolean updated = stockService.createOrUpdateStock(
                            product.getId(), 
                            sku.getSku(), 
                            product.getMerId(), 
                            sku.getStock(), 
                            sku.getCost()
                        );
                        
                        if (updated) {
                            result.setRepairedItems(result.getRepairedItems() + 1);
                            logger.info("已修复SKU[{}]库存一致性，商品：{}，库存：{} → {}", 
                                sku.getSku(), product.getName(), stockRecord.getStock(), sku.getStock());
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            result.addIssue("检查SKU[" + sku.getSku() + "]时异常：" + e.getMessage());
            logger.error("检查SKU[{}]库存一致性异常，商品：{}", sku.getSku(), product.getName(), e);
        }
    }
}