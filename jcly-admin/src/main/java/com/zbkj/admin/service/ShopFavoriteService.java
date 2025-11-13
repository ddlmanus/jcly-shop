package com.zbkj.admin.service;

import com.zbkj.common.model.user.UserMerchantCollect;
import com.zbkj.common.request.PageParamRequest;

import java.util.List;

/**
 * 店铺收藏服务接口
 */
public interface ShopFavoriteService {

    /**
     * 获取店铺收藏列表
     * @param searchType 搜索类型
     * @param content 搜索内容
     * @param dateLimit 日期限制
     * @param pageParamRequest 分页参数
     * @return 店铺收藏列表
     */
    List<UserMerchantCollect> getList(String searchType, String content, String dateLimit, PageParamRequest pageParamRequest);
}