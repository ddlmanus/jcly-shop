package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.product.ProductCoupon;

import java.util.List;

/**
 * StoreProductCouponService 接口
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
public interface ProductCouponService extends IService<ProductCoupon> {
    /**
     * 根据产品id删除 优惠券关联信息
     *
     * @param productId 产品id
     */
    Boolean deleteByProductId(Integer productId);

    /**
     * 根据商品id获取已关联优惠券信息
     *
     * @param productId 商品id
     * @return 已关联优惠券
     */
    List<ProductCoupon> getListByProductId(Integer productId);
}
