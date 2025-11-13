package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.wechat.video.PayComponentProductSkuAttr;
import com.zbkj.service.dao.PayComponentProductSkuAttrDao;
import com.zbkj.service.service.PayComponentProductSkuAttrService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 *
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
public class PayComponentProductSkuAttrServiceImpl extends ServiceImpl<PayComponentProductSkuAttrDao, PayComponentProductSkuAttr> implements PayComponentProductSkuAttrService {

    @Resource
    private PayComponentProductSkuAttrDao dao;

}

