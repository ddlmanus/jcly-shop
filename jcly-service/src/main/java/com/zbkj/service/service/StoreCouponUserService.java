package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.store.StoreCouponUser;
import com.zbkj.common.response.StoreCouponUserResponse;

import java.util.List;

/**
 * 用户优惠券服务接口
 */
public interface StoreCouponUserService extends IService<StoreCouponUser> {

    /**
     * 获取会员优惠券列表
     * @param uid 用户ID
     * @param storeId 店铺ID
     * @param page 页码
     * @param limit 每页数量
     * @return 优惠券列表
     */
    List<StoreCouponUserResponse> getMemberCouponList(Integer uid, Integer storeId, Integer page, Integer limit);
    
    /**
     * 检查用户是否已领取过该优惠券
     * @param uid 用户ID
     * @param couponId 优惠券ID
     * @return 是否已领取
     */
    boolean checkUserHasCoupon(Integer uid, Integer couponId);
    
    /**
     * 获取用户可用的优惠券数量
     * @param uid 用户ID
     * @param storeId 店铺ID
     * @return 可用优惠券数量
     */
    int getUsableCouponCount(Integer uid, Integer storeId);
    
    /**
     * 更新过期的优惠券状态
     * @return 更新数量
     */
    int updateExpiredCoupon();
}