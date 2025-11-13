package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.record.ProductDayRecord;
import com.zbkj.common.request.ProductRankingRequest;

/**
 * ProductDayRecordService 接口
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
public interface ProductDayRecordService extends IService<ProductDayRecord> {

    /**
     * 获取商品排行榜
     * @param request 查询参数
     * @return PageInfo
     */
    PageInfo<ProductDayRecord> getRanking(ProductRankingRequest request);
}