package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.coupon.CouponProduct;

import java.util.List;

/**
 * CouponProductService 接口
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
public interface CouponProductService extends IService<CouponProduct> {

    /**
     * 通过优惠券id查询列表
     * @param cid 优惠券ID
     * @return List
     */
    List<CouponProduct> findByCid(Integer cid);

    /**
     * 通过优惠券id删除
     * @param cid 优惠券ID
     * @return Boolean
     */
    Boolean deleteByCid(Integer cid);
}
