package com.zbkj.admin.service;


import com.zbkj.admin.vo.ValidateCode;

/**
 * ValidateCodeService 接口
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
public interface ValidateCodeService {

    /**
     * 获取图片验证码
     * @return CommonResult
     */
    ValidateCode get();
}
