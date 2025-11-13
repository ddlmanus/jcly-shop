package com.zbkj.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.product.CsjProductDescription;
import com.zbkj.service.dao.CsjProductDescriptionDao;
import com.zbkj.service.service.CsjProductDescriptionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 采食家商品详情Service实现类
 * </p>
 *
 * @author dudl
 * @since 2025-01-16
 */
@Service
public class CsjProductDescriptionServiceImpl extends ServiceImpl<CsjProductDescriptionDao, CsjProductDescription> implements CsjProductDescriptionService {

    @Resource
    private CsjProductDescriptionDao dao;

    /**
     * 根据商品ID获取商品详情
     *
     * @param productId 商品ID
     * @return CsjProductDescription
     */
    @Override
    public CsjProductDescription getByProductId(Integer productId) {
        LambdaQueryWrapper<CsjProductDescription> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CsjProductDescription::getProductId, productId);
        return dao.selectOne(lqw);
    }

    /**
     * 根据商品ID删除商品详情
     *
     * @param productId 商品ID
     * @return Boolean
     */
    @Override
    public Boolean deleteByProductId(Integer productId) {
        LambdaQueryWrapper<CsjProductDescription> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CsjProductDescription::getProductId, productId);
        CsjProductDescription description = dao.selectOne(lqw);
        if (ObjectUtil.isNotNull(description)) {
            return removeById(description.getId());
        }
        return true;
    }
}
