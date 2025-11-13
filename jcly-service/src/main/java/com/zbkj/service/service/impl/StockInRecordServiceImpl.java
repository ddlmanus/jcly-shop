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
import com.zbkj.common.model.stock.StockInRecord;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StockInRequest;
import com.zbkj.common.request.StockInRecordSearchRequest;
import com.zbkj.service.dao.StockInRecordDao;
import com.zbkj.service.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * StockInRecordServiceImpl 实现类
 */
@Slf4j
@Service
public class StockInRecordServiceImpl extends ServiceImpl<StockInRecordDao, StockInRecord> implements StockInRecordService {

    @Resource
    private StockInRecordDao dao;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private StockService stockService;

    @Autowired
    private JustuitanErpService justuitanErpService;

    /**
     * 根据商品ID和SKU获取商品规格信息
     */
    private ProductAttrValue getProductAttrValueBySku(Integer productId, String sku) {
        LambdaQueryWrapper<ProductAttrValue> lqw = Wrappers.lambdaQuery();
        lqw.eq(ProductAttrValue::getProductId, productId);
        lqw.eq(ProductAttrValue::getSku, sku);
        lqw.eq(ProductAttrValue::getIsDel, false);
        lqw.last(" limit 1");
        return productAttrValueService.getOne(lqw);
    }
    @Override
    public PageInfo<StockInRecord> getStockInRecordPage(StockInRecordSearchRequest searchRequest, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());

        LambdaQueryWrapper<StockInRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(StockInRecord::getIsDel, false);
        
        // 商户ID过滤
        if (ObjectUtil.isNotNull(searchRequest.getMerId())) {
            lqw.eq(StockInRecord::getMerId, searchRequest.getMerId());
        }
        
        // 商品名称搜索
        if (StrUtil.isNotBlank(searchRequest.getProductName())) {
            lqw.like(StockInRecord::getProductName, searchRequest.getProductName());
        }
        
        // 入库单号搜索
        if (StrUtil.isNotBlank(searchRequest.getRecordNo())) {
            lqw.like(StockInRecord::getRecordNo, searchRequest.getRecordNo());
        }
        
        // 操作员姓名搜索
        if (StrUtil.isNotBlank(searchRequest.getOperatorName())) {
            lqw.like(StockInRecord::getOperatorName, searchRequest.getOperatorName());
        }
        
        // 供应商搜索
        if (StrUtil.isNotBlank(searchRequest.getSupplier())) {
            lqw.like(StockInRecord::getSupplier, searchRequest.getSupplier());
        }
        
        // 时间范围搜索
        if (StrUtil.isNotBlank(searchRequest.getStartDate())) {
            lqw.ge(StockInRecord::getCreateTime, searchRequest.getStartDate());
        }
        if (StrUtil.isNotBlank(searchRequest.getEndDate())) {
            lqw.le(StockInRecord::getCreateTime, searchRequest.getEndDate());
        }
        
        lqw.orderByDesc(StockInRecord::getCreateTime);

        return PageInfo.of(list(lqw));
    }

    @Override
    public PageInfo<StockInRecord> getStockInRecordPage(Integer merId, PageParamRequest pageParamRequest) {
        StockInRecordSearchRequest searchRequest = new StockInRecordSearchRequest();
        searchRequest.setMerId(merId);
        return getStockInRecordPage(searchRequest, pageParamRequest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean stockIn(StockInRequest request, Integer operatorId, String operatorName, Integer merId) {
        // 获取商品信息
        Product product = productService.getById(request.getProductId());
        if (ObjectUtil.isNull(product)) {
            throw new RuntimeException("商品不存在");
        }

        // 获取商品规格信息（从商品规格表获取当前库存）
        ProductAttrValue productAttrValue = getProductAttrValueBySku(request.getProductId(), request.getSku());
        if (ObjectUtil.isNull(productAttrValue)) {
            throw new RuntimeException("商品规格不存在，SKU：" + request.getSku());
        }

        // 获取当前库存信息（优先从库存管理表获取，如果不存在则从规格表获取）
        Integer beforeStock = 0;
        Stock existingStock = stockService.getByProductIdAndSku(request.getProductId(), request.getSku());
        if (ObjectUtil.isNotNull(existingStock)) {
            beforeStock = existingStock.getStock();
        } else {
            // 如果库存管理表中没有记录，则使用商品规格表的库存
            beforeStock = productAttrValue.getStock();
        }

        // 计算现有库存数量
        Integer afterStock = beforeStock + request.getInQuantity();

        // 生成入库单号
        String recordNo = "IN" + DateUtil.format(new Date(), "yyyyMMdd") + IdUtil.createSnowflake(1, 1).nextIdStr();

        // 计算总金额
        BigDecimal totalAmount = request.getCostPrice().multiply(new BigDecimal(request.getInQuantity()));

        // 创建入库记录
        StockInRecord record = new StockInRecord();
        record.setRecordNo(recordNo);
        record.setProductId(request.getProductId());
        record.setProductName(product.getName());
        record.setProductImages(product.getImage()); // 设置商品主图片
        record.setSku(request.getSku());
        record.setMerId(merId);
        record.setBeforeStock(beforeStock);
        record.setInQuantity(request.getInQuantity());
        record.setAfterStock(afterStock);
        record.setCostPrice(request.getCostPrice());
        record.setTotalAmount(totalAmount);
        record.setSupplier(request.getSupplier());
        record.setRemark(request.getRemark());
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        record.setIsDel(false);

        // 保存入库记录
        boolean saveRecord = save(record);
        if (!saveRecord) {
            throw new RuntimeException("保存入库记录失败");
        }

        // 1. 更新库存管理表（eb_stock）
        boolean updateStock = stockService.createOrUpdateStock(
                request.getProductId(), 
                request.getSku(), 
                merId, 
                afterStock, 
                request.getCostPrice()
        );
        
        if (!updateStock) {
            throw new RuntimeException("更新库存管理表失败");
        }

        // 2. 同时更新商品规格表（eb_product_attr_value）的库存
        boolean updateAttrValueStock = productAttrValueService.operationStock(
                productAttrValue.getId(),
                request.getInQuantity(),
                Constants.OPERATION_TYPE_QUICK_ADD,  // 快速添加库存
                productAttrValue.getType(),
                productAttrValue.getVersion()
        );

        if (!updateAttrValueStock) {
            throw new RuntimeException("更新商品规格库存失败");
        }

        // 3. 重新计算并更新商品表（eb_product）的总库存
        updateProductTotalStock(request.getProductId());

        // 4. 同步库存到聚水潭ERP（仅自营店）
        try {
            justuitanErpService.syncInventory(request.getProductId(), productAttrValue.getId(), afterStock, "add");
            log.info("快速添加库存同步到聚水潭成功，商品ID: {}, SKU: {}, 库存: {}",
                    request.getProductId(), request.getSku(), afterStock);
        } catch (Exception e) {
            log.error("快速添加库存同步到聚水潭失败，商品ID: {}, SKU: {}",
                    request.getProductId(), request.getSku(), e);
            // 不影响主要业务流程，只记录错误日志
        }

        return true;
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
    public StockInRecord getByRecordNo(String recordNo) {
        LambdaQueryWrapper<StockInRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(StockInRecord::getRecordNo, recordNo);
        lqw.eq(StockInRecord::getIsDel, false);
        lqw.last(" limit 1");
        return getOne(lqw);
    }
} 