package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.constants.Constants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.product.Product;
import com.zbkj.common.model.product.ProductAttrValue;
import com.zbkj.service.dao.ProductAttrValueDao;
import com.zbkj.service.service.JustuitanErpService;
import com.zbkj.service.service.ProductAttrValueService;
import com.zbkj.service.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * ProductAttrValueServiceImpl 接口实现
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
@Service
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValue>
        implements ProductAttrValueService {

    private static final Logger logger = LoggerFactory.getLogger(ProductAttrValueServiceImpl.class);

    @Resource
    private ProductAttrValueDao dao;
    
    @Autowired
    private JustuitanErpService justuitanErpService;
    
    @Autowired
    private ProductService productService;

    /**
     * 根据商品id和attrId获取规格属性
     * @param productId 商品id
     * @param attrId 属性id
     * @param type 商品类型
     * @param marketingType 营销类型
     * @return 商品属性集合
     */
    @Override
    public ProductAttrValue getByProductIdAndAttrId(Integer productId, Integer attrId, Integer type, Integer marketingType) {
        LambdaQueryWrapper<ProductAttrValue> lqw = Wrappers.lambdaQuery();
        lqw.eq(ProductAttrValue::getProductId, productId);
        lqw.eq(ProductAttrValue::getType, type);
        lqw.eq(ProductAttrValue::getMarketingType, marketingType);
        lqw.eq(ProductAttrValue::getId, attrId);
        lqw.eq(ProductAttrValue::getIsDel, false);
        lqw.last(" limit 1");
        return dao.selectOne(lqw);
    }

    /**
     * 根据id、类型查询
     *
     * @param id   ID
     * @param type 类型
     * @return ProductAttrValue
     */
    @Override
    public ProductAttrValue getByIdAndProductIdAndType(Integer id, Integer productId, Integer type) {
        LambdaQueryWrapper<ProductAttrValue> lqw = Wrappers.lambdaQuery();
        lqw.eq(ProductAttrValue::getId, id);
        lqw.eq(ProductAttrValue::getProductId, productId);
        lqw.eq(ProductAttrValue::getType, type);
        lqw.eq(ProductAttrValue::getIsDel, false);
        lqw.last(" limit 1");
        return dao.selectOne(lqw);
    }

    /**
     * 根据id、类型查询
     * @param id ID
     * @param type 类型
     * @param marketingType 营销类型
     * @return ProductAttrValue
     */
    @Override
    public ProductAttrValue getByIdAndProductIdAndType(Integer id, Integer productId, Integer type, Integer marketingType) {
        LambdaQueryWrapper<ProductAttrValue> lqw = Wrappers.lambdaQuery();
        lqw.eq(ProductAttrValue::getId, id);
        lqw.eq(ProductAttrValue::getProductId, productId);
        lqw.eq(ProductAttrValue::getType, type);
        lqw.eq(ProductAttrValue::getMarketingType, marketingType);
        lqw.eq(ProductAttrValue::getIsDel, false);
        lqw.last(" limit 1");
        return dao.selectOne(lqw);
    }

    /**
     * 添加(退货)/扣减库存
     *
     * @param id            商品属性规格id
     * @param num           数量
     * @param operationType 类型：add—添加，sub—扣减，refund-退款添加库存
     * @param type          活动类型 0=商品
     * @return Boolean
     */
    @Override
    public Boolean operationStock(Integer id, Integer num, String operationType, Integer type, Integer version) {
        UpdateWrapper<ProductAttrValue> updateWrapper = new UpdateWrapper<>();
        if (operationType.equals(Constants.OPERATION_TYPE_QUICK_ADD)) {
            updateWrapper.setSql(StrUtil.format("stock = stock + {}", num));
//            if (type > 0 && type < 5) {
//                updateWrapper.setSql(StrUtil.format("quota = quota + {}", num));
//            }
        }
        if (operationType.equals(Constants.OPERATION_TYPE_ADD)) {
            updateWrapper.setSql(StrUtil.format("stock = stock + {}", num));
            updateWrapper.setSql(StrUtil.format("sales = sales - {}", num));
//            if (type > 0 && type < 5) {
//                updateWrapper.setSql(StrUtil.format("quota = quota + {}", num));
//            }
        }
        if (operationType.equals(Constants.OPERATION_TYPE_SUBTRACT)) {
            updateWrapper.setSql(StrUtil.format("stock = stock - {}", num));
            updateWrapper.setSql(StrUtil.format("sales = sales + {}", num));
//            if (type > 0 && type < 4) {
//                updateWrapper.setSql(StrUtil.format("quota = quota - {}", num));
//                // 扣减时加乐观锁保证库存不为负
//                updateWrapper.last(StrUtil.format("and (quota - {} >= 0)", num));
//            } else {// 普通商品或者视频号商品
//                // 扣减时加乐观锁保证库存不为负
//                updateWrapper.last(StrUtil.format("and (stock - {} >= 0)", num));
//            }
            updateWrapper.last(StrUtil.format("and (stock - {} >= 0)", num));
        }
        if (operationType.equals(Constants.OPERATION_TYPE_ACTIVITY_CREATE)) {
            updateWrapper.setSql(StrUtil.format("stock = stock - {}", num));
            // 扣减时加乐观锁保证库存不为负
            updateWrapper.last(StrUtil.format("and (stock - {} >= 0)", num));
        }
        if (operationType.equals(Constants.OPERATION_TYPE_DELETE)) {
            updateWrapper.setSql(StrUtil.format("stock = stock - {}", num));
            updateWrapper.last(StrUtil.format("and (stock - {} >= 0)", num));
        }

        updateWrapper.setSql("version = version + 1");
        updateWrapper.eq("id", id);
        updateWrapper.eq("type", type);
        updateWrapper.eq("version", version);
        boolean update = update(updateWrapper);
        if (!update) {
            throw new CrmebException("更新商品attrValue失败，attrValueId = " + id);
        }
        
        // 库存更新成功后，同步到聚水潭（仅针对手动添加/扣减库存操作）
        if (update && (operationType.equals(Constants.OPERATION_TYPE_ADD) 
                || operationType.equals(Constants.OPERATION_TYPE_QUICK_ADD))) {
            syncInventoryToJst(id, num, operationType, type, null);
        }
        
        return update;
    }

    /**
     * 添加/扣减库存
     * @param id 商品属性规格id
     * @param num 数量
     * @param operationType 类型：add—添加，sub—扣减
     * @param type 基础类型：0=普通商品,1-积分商品,2-虚拟商品,4=视频号,5-云盘商品,6-卡密商品
     * @param marketingType 营销类型 1=秒杀
     */
    @Override
    public Boolean operationStock(Integer id, Integer num, String operationType, Integer type, Integer marketingType, Integer version) {
        UpdateWrapper<ProductAttrValue> updateWrapper = new UpdateWrapper<>();
        if (operationType.equals(Constants.OPERATION_TYPE_QUICK_ADD)) {
            updateWrapper.setSql(StrUtil.format("stock = stock + {}", num));
            if (type > 0 && type < 5) {
                updateWrapper.setSql(StrUtil.format("quota = quota + {}", num));
            }
        }
        if (operationType.equals(Constants.OPERATION_TYPE_ADD)) {
            updateWrapper.setSql(StrUtil.format("stock = stock + {}", num));
            updateWrapper.setSql(StrUtil.format("sales = sales - {}", num));
            if (marketingType > 0) {
                updateWrapper.setSql(StrUtil.format("quota = quota + {}", num));
            }
        }
        if (operationType.equals(Constants.OPERATION_TYPE_SUBTRACT)) {
            updateWrapper.setSql(StrUtil.format("stock = stock - {}", num));
            updateWrapper.setSql(StrUtil.format("sales = sales + {}", num));
            if (marketingType > 0) {
                updateWrapper.setSql(StrUtil.format("quota = quota - {}", num));
                // 扣减时加乐观锁保证库存不为负
                updateWrapper.last(StrUtil.format("and (quota - {} >= 0)", num));
            } else {// 普通商品或者视频号商品
                // 扣减时加乐观锁保证库存不为负
                updateWrapper.last(StrUtil.format("and (stock - {} >= 0)", num));
            }
        }
        if (operationType.equals(Constants.OPERATION_TYPE_ACTIVITY_CREATE)) {
            updateWrapper.setSql(StrUtil.format("stock = stock - {}", num));
            // 扣减时加乐观锁保证库存不为负
            updateWrapper.last(StrUtil.format("and (stock - {} >= 0)", num));
        }
        if (operationType.equals(Constants.OPERATION_TYPE_DELETE)) {
            updateWrapper.setSql(StrUtil.format("stock = stock - {}", num));
            updateWrapper.last(StrUtil.format("and (stock - {} >= 0)", num));
        }

        updateWrapper.setSql("version = version + 1");
        updateWrapper.eq("id", id);
        updateWrapper.eq("type", type);
        updateWrapper.eq("marketing_type", marketingType);
        updateWrapper.eq("version", version);
        boolean update = update(updateWrapper);
        if (!update) {
            throw new CrmebException("更新商品attrValue失败，attrValueId = " + id);
        }
        
        // 库存更新成功后，同步到聚水潭（仅针对手动添加/扣减库存操作，且为普通商品）
        if (update && marketingType == 0 
                && (operationType.equals(Constants.OPERATION_TYPE_ADD) 
                    || operationType.equals(Constants.OPERATION_TYPE_QUICK_ADD))) {
            syncInventoryToJst(id, num, operationType, type, marketingType);
        }
        
        return update;
    }
    
    /**
     * 同步库存到聚水潭
     * @param id SKU ID
     * @param num 数量
     * @param operationType 操作类型
     * @param type 商品类型
     * @param marketingType 营销类型
     */
    private void syncInventoryToJst(Integer id, Integer num, String operationType, Integer type, Integer marketingType) {
        try {
            // 获取更新后的商品规格信息
            ProductAttrValue attrValue = getById(id);
            if (attrValue == null) {
                logger.warn("商品规格不存在，无法同步库存到聚水潭，SKU ID: {}", id);
                return;
            }
            
            // 获取商品信息
            Product product = productService.getById(attrValue.getProductId());
            if (product == null) {
                logger.warn("商品不存在，无法同步库存到聚水潭，商品ID: {}", attrValue.getProductId());
                return;
            }
            
            // 检查是否为自营店商品
            if (!justuitanErpService.isSelfOperatedStore(product.getMerId())) {
                logger.debug("商品不属于自营店，跳过库存同步到聚水潭: productId={}, merId={}", 
                    product.getId(), product.getMerId());
                return;
            }
            
            // 同步库存到聚水潭（使用更新后的实际库存）
            Boolean syncResult = justuitanErpService.syncInventory(
                attrValue.getProductId(), 
                id, 
                attrValue.getStock(), 
                "add"  // 聚水潭使用盘点方式，直接设置为最新库存
            );
            
            if (syncResult) {
                logger.info("库存修改同步到聚水潭成功，商品ID: {}, SKU ID: {}, 新库存: {}, 操作类型: {}", 
                    attrValue.getProductId(), id, attrValue.getStock(), operationType);
            } else {
                logger.warn("库存修改同步到聚水潭失败，商品ID: {}, SKU ID: {}, 新库存: {}", 
                    attrValue.getProductId(), id, attrValue.getStock());
            }
            
        } catch (Exception e) {
            // 不抛出异常，避免影响主流程
            logger.error("同步库存到聚水潭异常，SKU ID: {}, 错误信息: {}", id, e.getMessage(), e);
        }
    }

    /**
     * 删除商品规格属性值
     * @param productId 商品id
     * @param type 商品类型
     * @param marketingType 营销类型
     * @return Boolean
     */
    @Override
    public Boolean deleteByProductIdAndType(Integer productId, Integer type, Integer marketingType) {
        LambdaUpdateWrapper<ProductAttrValue> luw = Wrappers.lambdaUpdate();
        luw.set(ProductAttrValue::getIsDel, true);
        luw.eq(ProductAttrValue::getProductId, productId);
        luw.eq(ProductAttrValue::getType, type);
        luw.eq(ProductAttrValue::getMarketingType, marketingType);
        return update(luw);
    }

    /**
     * 批量删除商品sku
     * @param productIds 商品id
     * @param marketingType 营销类型
     * @return Boolean
     */
    @Override
    public Boolean batchDeleteByProductIdAndMarketingType(List<Integer> productIds, Integer marketingType) {
        LambdaUpdateWrapper<ProductAttrValue> luw = Wrappers.lambdaUpdate();
        luw.set(ProductAttrValue::getIsDel, true);
        luw.in(ProductAttrValue::getProductId, productIds);
        luw.eq(ProductAttrValue::getMarketingType, marketingType);
        return update(luw);
    }

    /**
     * 获取商品规格列表
     * @param productId 商品id
     * @param type 商品类型
     * @param marketingType 营销类型
     *  @param showSwitch 显示开关，true-只查询开启显示的sku
     * @return List
     */
    @Override
    public List<ProductAttrValue> getListByProductIdAndType(Integer productId, Integer type, Integer marketingType, Boolean showSwitch) {
        LambdaQueryWrapper<ProductAttrValue> lqw = Wrappers.lambdaQuery();
        lqw.eq(ProductAttrValue::getProductId, productId);
        lqw.eq(ProductAttrValue::getType, type);
        lqw.eq(ProductAttrValue::getMarketingType, marketingType);
        lqw.eq(ProductAttrValue::getIsDel, false);
        if (showSwitch) {
            lqw.eq(ProductAttrValue::getIsShow, 1);
        }
        return dao.selectList(lqw);
    }

    /**
     * 根据商品id和attrIdList获取列表集合
     * @param productId 商品id
     * @param attrIdList 属性idList
     * @param type 商品类型
     * @param marketingType 营销类型
     * @return 商品属性集合
     */
    @Override
    public List<ProductAttrValue> getByProductIdAndAttrIdList(Integer productId, List<Integer> attrIdList, Integer type, Integer marketingType) {
        LambdaQueryWrapper<ProductAttrValue> lqw = Wrappers.lambdaQuery();
        lqw.eq(ProductAttrValue::getProductId, productId);
        lqw.eq(ProductAttrValue::getType, type);
        lqw.eq(ProductAttrValue::getMarketingType, marketingType);
        lqw.in(ProductAttrValue::getId, attrIdList);
        lqw.eq(ProductAttrValue::getIsDel, false);
        return dao.selectList(lqw);
    }

    /**
     * 更新商品佣金
     * @param proId 商品ID
     * @param type 商品类型
     * @param marketingType 营销类型
     * @param brokerage 一级佣金
     * @param brokerageTwo 二级佣金
     */
    @Override
    public Boolean updateBrokerageByProductId(Integer proId, Integer type, Integer marketingType, Integer brokerage, Integer brokerageTwo) {
        LambdaUpdateWrapper<ProductAttrValue> wrapper = Wrappers.lambdaUpdate();
        wrapper.set(ProductAttrValue::getBrokerage, brokerage);
        wrapper.set(ProductAttrValue::getBrokerageTwo, brokerageTwo);
        wrapper.eq(ProductAttrValue::getProductId, proId);
        wrapper.eq(ProductAttrValue::getType, type);
        wrapper.eq(ProductAttrValue::getMarketingType, marketingType);
        return update(wrapper);
    }

    @Override
    public Boolean deleteByMasterIdListAndMarktingType(List<Integer> skuIdList, Integer marketingType) {
        LambdaUpdateWrapper<ProductAttrValue> luw = Wrappers.lambdaUpdate();
        luw.set(ProductAttrValue::getIsDel, true);
        luw.in(ProductAttrValue::getMasterId, skuIdList);
        luw.eq(ProductAttrValue::getMarketingType, marketingType);
        return update(luw);
    }

    @Override
    public List<ProductAttrValue> getAll() {
        LambdaQueryWrapper<ProductAttrValue> lqw = Wrappers.lambdaQuery();
        lqw.eq(ProductAttrValue::getIsDel, false);
        return dao.selectList(lqw);
    }

    /**
     * 根据商品ID和SKU获取商品规格信息
     * @param productId 商品ID
     * @param sku SKU编码
     * @return ProductAttrValue
     */
    @Override
    public ProductAttrValue getByProductIdAndSku(Integer productId, String sku) {
        LambdaQueryWrapper<ProductAttrValue> lqw = Wrappers.lambdaQuery();
        lqw.eq(ProductAttrValue::getProductId, productId);
        lqw.eq(ProductAttrValue::getSku, sku);
        lqw.eq(ProductAttrValue::getIsDel, false);
        lqw.last(" limit 1");
        return getOne(lqw);
    }

    /**
     * 批量获取商品SKU属性 - 性能优化方法
     */
    @Override
    public List<ProductAttrValue> batchGetListByProductIds(List<Integer> productIds, Integer marketingType, Boolean showSwitch) {
        if (CollUtil.isEmpty(productIds)) {
            return CollUtil.newArrayList();
        }
        
        LambdaQueryWrapper<ProductAttrValue> lqw = Wrappers.lambdaQuery();
        lqw.in(ProductAttrValue::getProductId, productIds);
        lqw.eq(ProductAttrValue::getMarketingType, marketingType);
        lqw.eq(ProductAttrValue::getIsDel, false);
        if (showSwitch) {
            lqw.eq(ProductAttrValue::getIsShow, true);
        }
        lqw.orderByAsc(ProductAttrValue::getProductId, ProductAttrValue::getId);
        return list(lqw);
    }

    @Override
    public List<ProductAttrValue> getListByProductId(Integer productId) {
        LambdaQueryWrapper<ProductAttrValue> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ProductAttrValue::getProductId, productId);
        return dao.selectList(lqw);
    }
}

