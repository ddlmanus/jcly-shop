package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.seckill.SeckillActivity;
import com.zbkj.common.request.SeckillActivitySearchRequest;

import java.util.List;

/**
*  SeckillActivityService 接口
*  +----------------------------------------------------------------------
*  | JCLY [ JCLY赋能开发者，助力企业发展 ]
*  +----------------------------------------------------------------------
*  | Copyright (c) 2016~2020 https://www.ddlmanus.xyz All rights reserved.
*  +----------------------------------------------------------------------
*  | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
*  +----------------------------------------------------------------------
*  | Author: dudl
*  +----------------------------------------------------------------------
*/
public interface SeckillActivityService extends IService<SeckillActivity> {

    /**
     * 秒杀活动分页列表
     * @param request 查询参数
     * @param isMerchant 是否商户
     */
    PageInfo<SeckillActivity> getActivityPage(SeckillActivitySearchRequest request, Boolean isMerchant);

    /**
     * 获取秒杀活动
     * @param acvitityIdList 秒杀活动ID列表
     * @param isOpen 是否开启
     */
    List<SeckillActivity> findByIdListAndOpen(List<Integer> acvitityIdList, Boolean isOpen);
}