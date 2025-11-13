package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.product.CsjProductDescription;

/**
 * <p>
 * 采食家商品详情Service接口
 * </p>
 *
 * @author dudl
 * @since 2025-01-16
 */
public interface CsjProductDescriptionService extends IService<CsjProductDescription> {

    /**
     * 根据商品ID获取商品详情
     *
     * @param productId 商品ID
     * @return CsjProductDescription
     */
    CsjProductDescription getByProductId(Integer productId);

    /**
     * 根据商品ID删除商品详情
     *
     * @param productId 商品ID
     * @return Boolean
     */
    Boolean deleteByProductId(Integer productId);
}
