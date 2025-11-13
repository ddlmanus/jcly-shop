package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.product.ProductAttribute;
import com.zbkj.service.dao.ProductAttributeDao;
import com.zbkj.service.service.ProductAttributeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author dudl
 * @description ProductAttributeServiceImpl 接口实现
 * @date 2024-11-11
 */
@Service
public class ProductAttributeServiceImpl extends ServiceImpl<ProductAttributeDao, ProductAttribute> implements ProductAttributeService {

    @Resource
    private ProductAttributeDao dao;

    /**
     * 商品变更导致的删除
     */
    @Override
    public Boolean deleteByProductUpdate(Integer proId) {
        LambdaUpdateWrapper<ProductAttribute> wrapper = Wrappers.lambdaUpdate();
        wrapper.set(ProductAttribute::getIsDel, 1);
        wrapper.eq(ProductAttribute::getProductId, proId);
        wrapper.eq(ProductAttribute::getIsDel, 0);
        return update(wrapper);
    }

    /**
     * 获取商品的规格列表
     */
    @Override
    public List<ProductAttribute> findListByProductId(Integer proId) {
        LambdaQueryWrapper<ProductAttribute> lqw = Wrappers.lambdaQuery();
        lqw.eq(ProductAttribute::getProductId, proId);
        lqw.eq(ProductAttribute::getIsDel, 0);
        lqw.orderByAsc(ProductAttribute::getSort);
        return dao.selectList(lqw);
    }

    /**
     * 批量获取商品规格列表 - 性能优化方法
     */
    @Override
    public List<ProductAttribute> findListByProductIds(List<Integer> productIds) {
        if (CollUtil.isEmpty(productIds)) {
            return CollUtil.newArrayList();
        }
        
        LambdaQueryWrapper<ProductAttribute> lqw = Wrappers.lambdaQuery();
        lqw.in(ProductAttribute::getProductId, productIds);
        lqw.eq(ProductAttribute::getIsDel, 0);
        lqw.orderByAsc(ProductAttribute::getProductId, ProductAttribute::getSort);
        return dao.selectList(lqw);
    }
}

