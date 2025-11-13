package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.order.MerchantOrder;

import java.util.List;
import java.util.Map;

/**
*  MerchantOrderService 接口
*  +----------------------------------------------------------------------
*  | JCLY [ JCLY赋能开发者，助力企业发展 ]
*  +----------------------------------------------------------------------
*  | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
*  +----------------------------------------------------------------------
*  | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
*  +----------------------------------------------------------------------
*  | Author: dudl
*  +----------------------------------------------------------------------
*/
public interface MerchantOrderService extends IService<MerchantOrder> {

    /**
     * 根据主订单号获取商户订单
     * @param orderNo 主订单号
     * @return List
     */
    List<MerchantOrder> getByOrderNo(String orderNo);

    /**
     * 根据主订单号获取商户订单（支付完成进行商户拆单后可用）
     * @param orderNo 主订单号
     * @return MerchantOrder
     */
    MerchantOrder getOneByOrderNo(String orderNo);

    /**
     * 通过核销码获取订单
     * @param verifyCode 核销码
     */
    MerchantOrder getOneByVerifyCode(String verifyCode);


    /**
     * 商户查询待核销订单
     * @param verifyCode 核销订单
     * @param merId 商户id
     * @return 待核销订单
     */
    MerchantOrder getByVerifyCodeForMerchant(String verifyCode, Integer merId);

    Map<String, List<MerchantOrder>> getMapByOrderNoList(List<String> orderNoList);

    /**
     * 根据用户ID和商户ID获取商户订单列表（分页）
     * @param uid 用户ID
     * @param merId 商户ID
     * @param status 订单状态，0表示所有状态
     * @param page 页码
     * @param limit 每页数量
     * @return PageInfo<MerchantOrder>
     */
    PageInfo<MerchantOrder> getMerchantOrderListByUid(Integer uid, Integer merId, Integer status, Integer page, Integer limit);
}
