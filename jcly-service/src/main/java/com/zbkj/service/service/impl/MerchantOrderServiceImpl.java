package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.order.MerchantOrder;
import com.zbkj.common.page.CommonPage;
import com.zbkj.service.dao.MerchantOrderDao;
import com.zbkj.service.service.MerchantOrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
*  MerchantOrderServiceImpl 接口实现
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
@Service
public class MerchantOrderServiceImpl extends ServiceImpl<MerchantOrderDao, MerchantOrder> implements MerchantOrderService {

    @Resource
    private MerchantOrderDao dao;

    /**
     * 根据主订单号获取商户订单
     * @param orderNo 主订单号
     * @return List
     */
    @Override
    public List<MerchantOrder> getByOrderNo(String orderNo) {
        LambdaQueryWrapper<MerchantOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(MerchantOrder::getOrderNo, orderNo);
        return dao.selectList(lqw);
    }

    /**
     * 根据主订单号获取商户订单（支付完成进行商户拆单后可用）
     * @param orderNo 主订单号
     * @return MerchantOrder
     */
    @Override
    public MerchantOrder getOneByOrderNo(String orderNo) {
        LambdaQueryWrapper<MerchantOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(MerchantOrder::getOrderNo, orderNo);
        lqw.last(" limit 1");
        return dao.selectOne(lqw);
    }

    /**
     * 通过核销码获取订单
     * @param verifyCode 核销码
     */
    @Override
    public MerchantOrder getOneByVerifyCode(String verifyCode) {
        LambdaQueryWrapper<MerchantOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(MerchantOrder::getVerifyCode, verifyCode);
        lqw.orderByDesc(MerchantOrder::getId);
        lqw.last("limit 1");
        return dao.selectOne(lqw);
    }

    /**
     * 商户查询待核销订单
     *
     * @param verifyCode 核销订单
     * @param merId      商户id
     * @return 待核销订单
     */
    @Override
    public MerchantOrder getByVerifyCodeForMerchant(String verifyCode, Integer merId) {
        LambdaQueryWrapper<MerchantOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(MerchantOrder::getVerifyCode, verifyCode);
        lqw.eq(MerchantOrder::getMerId, merId);
        lqw.orderByDesc(MerchantOrder::getId);
        lqw.last("limit 1");
        return dao.selectOne(lqw);
    }

    @Override
    public Map<String, List<MerchantOrder>> getMapByOrderNoList(List<String> orderNoList) {
        if (CollUtil.isEmpty(orderNoList)) {
            return new HashMap<>();
        }
        LambdaQueryWrapper<MerchantOrder> lqw = Wrappers.lambdaQuery();
        lqw.in(MerchantOrder::getOrderNo, orderNoList);
        List<MerchantOrder> merchantOrderList = dao.selectList(lqw);
        if (CollUtil.isEmpty(merchantOrderList)) {
            return new HashMap<>();
        }
        return merchantOrderList.stream().collect(Collectors.groupingBy(MerchantOrder::getOrderNo));
    }

    /**
     * 根据用户ID和商户ID获取商户订单列表（分页）
     * @param uid 用户ID
     * @param merId 商户ID
     * @param status 订单状态，0表示所有状态（商户订单表无status字段，此参数保留接口兼容性）
     * @param page 页码
     * @param limit 每页数量
     * @return PageInfo<MerchantOrder>
     */
    @Override
    public PageInfo<MerchantOrder> getMerchantOrderListByUid(Integer uid, Integer merId, Integer status, Integer page, Integer limit) {
        Page<MerchantOrder> pages = PageHelper.startPage(page, limit);
        LambdaQueryWrapper<MerchantOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(MerchantOrder::getUid, uid);
        lqw.eq(MerchantOrder::getMerId, merId);
        lqw.like(MerchantOrder::getOrderNo, "SH");
        // 注：商户订单表(eb_merchant_order)无status字段，状态通过平台订单表(eb_order)管理
        // 如果需要按状态筛选，需要关联查询平台订单表
        lqw.orderByDesc(MerchantOrder::getCreateTime);
       List<MerchantOrder> list = dao.selectList(lqw);
        return CommonPage.copyPageInfo( pages, list);
    }
}

