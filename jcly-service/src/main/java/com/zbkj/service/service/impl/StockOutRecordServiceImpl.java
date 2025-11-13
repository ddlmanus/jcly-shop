package com.zbkj.service.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.constants.Constants;
import com.zbkj.common.model.product.Product;
import com.zbkj.common.model.product.ProductAttrValue;
import com.zbkj.common.model.stock.Stock;
import com.zbkj.common.model.stock.StockOutRecord;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StockOutRequest;
import com.zbkj.common.request.StockOutRecordSearchRequest;
import com.zbkj.service.dao.StockOutRecordDao;
import com.zbkj.service.service.ProductAttrValueService;
import com.zbkj.service.service.ProductService;
import com.zbkj.service.service.StockOutRecordService;
import com.zbkj.service.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 库存出库记录服务实现类
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
public class StockOutRecordServiceImpl extends ServiceImpl<StockOutRecordDao, StockOutRecord> implements StockOutRecordService {

    @Resource
    private StockOutRecordDao dao;

    @Autowired
    private StockService stockService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Override
    public PageInfo<StockOutRecord> getStockOutRecordPage(StockOutRecordSearchRequest searchRequest, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());

        LambdaQueryWrapper<StockOutRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(StockOutRecord::getIsDel, false);
        
        // 商户ID过滤
        if (ObjectUtil.isNotNull(searchRequest.getMerId())) {
            lqw.eq(StockOutRecord::getMerId, searchRequest.getMerId());
        }
        
        // 商品名称模糊搜索
        if (StrUtil.isNotBlank(searchRequest.getProductName())) {
            lqw.like(StockOutRecord::getProductName, searchRequest.getProductName());
        }
        
        // 出库单号搜索
        if (StrUtil.isNotBlank(searchRequest.getRecordNo())) {
            lqw.like(StockOutRecord::getRecordNo, searchRequest.getRecordNo());
        }
        
        // 操作员姓名搜索
        if (StrUtil.isNotBlank(searchRequest.getOperatorName())) {
            lqw.like(StockOutRecord::getOperatorName, searchRequest.getOperatorName());
        }
        
        // 出库类型搜索
        if (ObjectUtil.isNotNull(searchRequest.getOutType())) {
            lqw.eq(StockOutRecord::getOutType, searchRequest.getOutType());
        }
        
        // 关联订单号搜索
        if (StrUtil.isNotBlank(searchRequest.getOrderNo())) {
            lqw.like(StockOutRecord::getOrderNo, searchRequest.getOrderNo());
        }
        
        // 时间范围搜索
        if (StrUtil.isNotBlank(searchRequest.getStartDate())) {
            lqw.ge(StockOutRecord::getCreateTime, searchRequest.getStartDate());
        }
        if (StrUtil.isNotBlank(searchRequest.getEndDate())) {
            lqw.le(StockOutRecord::getCreateTime, searchRequest.getEndDate());
        }
        
        lqw.orderByDesc(StockOutRecord::getCreateTime);

        return PageInfo.of(list(lqw));
    }

    @Override
    public PageInfo<StockOutRecord> getStockOutRecordPage(Integer merId, PageParamRequest pageParamRequest) {
        StockOutRecordSearchRequest searchRequest = new StockOutRecordSearchRequest();
        searchRequest.setMerId(merId);
        return getStockOutRecordPage(searchRequest, pageParamRequest);
    }

    /**
     * 更新商品表的总库存
     * 商品总库存 = 该商品所有规格库存的总和
     */
    private void updateProductTotalStock(Integer productId) {
        // 获取该商品的所有规格
        LambdaQueryWrapper<ProductAttrValue> lqw = Wrappers.lambdaQuery();
        lqw.eq(ProductAttrValue::getProductId, productId);
        lqw.eq(ProductAttrValue::getIsDel, false);
        List<ProductAttrValue> attrValueList = productAttrValueService.list(lqw);

        // 计算总库存
        Integer totalStock = attrValueList.stream().mapToInt(ProductAttrValue::getStock).sum();

        // 直接更新商品表的总库存
        Product updateProduct = new Product();
        updateProduct.setId(productId);
        updateProduct.setStock(totalStock);
        updateProduct.setUpdateTime(new Date());
        boolean directUpdate = productService.updateById(updateProduct);
        if (!directUpdate) {
            throw new RuntimeException("更新商品总库存失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean stockOut(StockOutRequest request, Integer operatorId, String operatorName, Integer merId) {
        // 获取商品信息
        Product product = productService.getById(request.getProductId());
        if (ObjectUtil.isNull(product)) {
            throw new RuntimeException("商品不存在");
        }

        // 获取商品规格信息
        ProductAttrValue productAttrValue = getProductAttrValueBySku(request.getProductId(), request.getSku());
        if (ObjectUtil.isNull(productAttrValue)) {
            throw new RuntimeException("商品规格不存在，SKU：" + request.getSku());
        }

        // 获取库存信息（优先从库存管理表获取）
        Stock stock = stockService.getByProductIdAndSku(request.getProductId(), request.getSku());
        Integer beforeStock = 0;
        BigDecimal costPrice = BigDecimal.ZERO;
        
        if (ObjectUtil.isNotNull(stock)) {
            beforeStock = stock.getStock();
            costPrice = stock.getCostPrice();
        } else {
            // 如果库存管理表中没有记录，则使用商品规格表的库存
            beforeStock = productAttrValue.getStock();
            costPrice = productAttrValue.getCost();
        }

        // 检查库存是否充足
        if (beforeStock < request.getOutQuantity()) {
            throw new RuntimeException("库存不足，当前库存：" + beforeStock);
        }

        // 记录原有库存和计算现有库存
        Integer afterStock = beforeStock - request.getOutQuantity();

        // 生成出库单号
        String recordNo = "OUT" + DateUtil.format(new Date(), "yyyyMMdd") + IdUtil.createSnowflake(1, 1).nextIdStr();

        // 计算总金额
        BigDecimal totalAmount = costPrice.multiply(new BigDecimal(request.getOutQuantity()));

        // 创建出库记录
        StockOutRecord record = new StockOutRecord();
        record.setRecordNo(recordNo);
        record.setProductId(request.getProductId());
        record.setProductName(product.getName());
        record.setProductImages(product.getImage()); // 设置商品图片
        record.setSku(request.getSku());
        record.setMerId(merId);
        record.setBeforeStock(beforeStock);
        record.setOutQuantity(request.getOutQuantity());
        record.setAfterStock(afterStock);
        record.setCostPrice(costPrice);
        record.setTotalAmount(totalAmount);
        record.setOutType(request.getOutType());
        record.setOrderNo(request.getOrderNo());
        record.setRemark(request.getRemark());
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        record.setIsDel(false);

        // 保存出库记录
        boolean saveRecord = save(record);
        if (!saveRecord) {
            throw new RuntimeException("保存出库记录失败");
        }

        // 1. 更新库存管理表（eb_stock）
        boolean updateStock;
        if (ObjectUtil.isNotNull(stock)) {
            updateStock = stockService.reduceStock(request.getProductId(), request.getSku(), request.getOutQuantity());
        } else {
            // 如果库存管理表中没有记录，创建记录后再扣减
            boolean createStock = stockService.createOrUpdateStock(
                    request.getProductId(), 
                    request.getSku(), 
                    merId, 
                    afterStock, 
                    costPrice
            );
            updateStock = createStock;
        }
        
        if (!updateStock) {
            throw new RuntimeException("更新库存管理表失败");
        }

        // 2. 同时更新商品规格表（eb_product_attr_value）的库存
        boolean updateAttrValueStock = productAttrValueService.operationStock(
                productAttrValue.getId(),
                request.getOutQuantity(),
                Constants.OPERATION_TYPE_SUBTRACT,  // 扣减库存
                productAttrValue.getType(),
                productAttrValue.getVersion()
        );

        if (!updateAttrValueStock) {
            throw new RuntimeException("更新商品规格库存失败");
        }

        // 3. 重新计算并更新商品表（eb_product）的总库存
        updateProductTotalStock(request.getProductId());

        return true;
    }

    @Override
    public StockOutRecord getByRecordNo(String recordNo) {
        LambdaQueryWrapper<StockOutRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(StockOutRecord::getRecordNo, recordNo);
        lqw.eq(StockOutRecord::getIsDel, false);
        lqw.last(" limit 1");
        return getOne(lqw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saleStockOut(Integer productId, String sku, Integer quantity, String orderNo, 
                                Integer operatorId, String operatorName, Integer merId) {
        // 获取商品信息
        Product product = productService.getById(productId);
        if (ObjectUtil.isNull(product)) {
            throw new RuntimeException("商品不存在");
        }

        // 获取商品规格信息
        ProductAttrValue productAttrValue = getProductAttrValueBySku(productId, sku);
        if (ObjectUtil.isNull(productAttrValue)) {
            throw new RuntimeException("商品规格不存在，SKU：" + sku);
        }

        // 获取库存信息（优先从库存管理表获取）
        Stock stock = stockService.getByProductIdAndSku(productId, sku);
        Integer beforeStock = 0;
        BigDecimal costPrice = BigDecimal.ZERO;
        
        if (ObjectUtil.isNotNull(stock)) {
            beforeStock = stock.getStock();
            costPrice = stock.getCostPrice();
        } else {
            // 如果库存管理表中没有记录，则使用商品规格表的库存
            beforeStock = productAttrValue.getStock();
            costPrice = productAttrValue.getCost();
        }

        // 记录原有库存和计算现有库存
        Integer afterStock = beforeStock - quantity;

        // 生成出库单号
        String recordNo = "SALE" + DateUtil.format(new Date(), "yyyyMMdd") + IdUtil.createSnowflake(1, 1).nextIdStr();

        // 计算总金额
        BigDecimal totalAmount = costPrice.multiply(new BigDecimal(quantity));

        // 创建销售出库记录
        StockOutRecord record = new StockOutRecord();
        record.setRecordNo(recordNo);
        record.setProductId(productId);
        record.setProductName(product.getName());
        record.setProductImages(product.getImage()); // 设置商品图片
        record.setSku(sku);
        record.setMerId(merId);
        record.setBeforeStock(beforeStock);
        record.setOutQuantity(quantity);
        record.setAfterStock(afterStock);
        record.setCostPrice(costPrice);
        record.setTotalAmount(totalAmount);
        record.setOutType(1); // 1=销售出库
        record.setRemark("订单销售出库");
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setOrderNo(orderNo);
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        record.setIsDel(false);

        // 保存出库记录
        boolean saveRecord = save(record);
        if (!saveRecord) {
            throw new RuntimeException("保存销售出库记录失败");
        }

        // 1. 减少库存管理表（eb_stock）的库存
        if (ObjectUtil.isNotNull(stock)) {
            boolean reduceStock = stockService.reduceStock(productId, sku, quantity);
            if (!reduceStock) {
                throw new RuntimeException("减少库存管理表库存失败");
            }
        } else {
            // 如果库存管理表中没有记录，创建新记录
            boolean createStock = stockService.createOrUpdateStock(
                    productId,
                    sku,
                    merId,
                    afterStock,
                    costPrice
            );
            if (!createStock) {
                throw new RuntimeException("创建库存管理记录失败");
            }
        }

        // 2. 同时减少商品规格表（eb_product_attr_value）的库存
        boolean updateAttrValueStock = productAttrValueService.operationStock(
                productAttrValue.getId(),
                quantity,
                Constants.OPERATION_TYPE_SUBTRACT,  // 减少库存
                productAttrValue.getType(),
                productAttrValue.getVersion()
        );

        if (!updateAttrValueStock) {
            throw new RuntimeException("减少商品规格库存失败");
        }

        // 3. 重新计算并更新商品表（eb_product）的总库存
        updateProductTotalStock(productId);

        return true;
    }

    /**
     * 根据商品ID和SKU获取商品规格信息
     * @param productId 商品ID
     * @param sku SKU编码
     * @return ProductAttrValue
     */
    private ProductAttrValue getProductAttrValueBySku(Integer productId, String sku) {
        LambdaQueryWrapper<ProductAttrValue> lqw = Wrappers.lambdaQuery();
        lqw.eq(ProductAttrValue::getProductId, productId);
        lqw.eq(ProductAttrValue::getSku, sku);
        lqw.eq(ProductAttrValue::getIsDel, false);
        lqw.last(" limit 1");
        return productAttrValueService.getOne(lqw);
    }
} 