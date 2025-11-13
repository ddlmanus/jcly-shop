package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.merchant.MerchantInfo;

/**
*  MerchantInfoService 接口
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
public interface MerchantInfoService extends IService<MerchantInfo> {

    /**
     * 通过商户id获取
     * @param merId 商户id
     * @return MerchantInfo
     */
    MerchantInfo getByMerId(Integer merId);
}
