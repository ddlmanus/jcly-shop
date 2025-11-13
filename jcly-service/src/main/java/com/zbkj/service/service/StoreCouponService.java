package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.store.StoreCoupon;

/**
 * 优惠券服务接口
 */
public interface StoreCouponService extends IService<StoreCoupon> {

    /**
     * 获取可用的优惠券
     * @param id 优惠券ID
     * @return 优惠券信息
     */
    StoreCoupon getEnabledCoupon(Integer id);
    
    /**
     * 增加优惠券领取数量
     * @param id 优惠券ID
     * @return 是否成功
     */
    boolean incReceiveNum(Integer id);
    
    /**
     * 增加优惠券使用数量
     * @param id 优惠券ID
     * @return 是否成功
     */
    boolean incUseNum(Integer id);
}