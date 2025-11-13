package com.zbkj.front.service;


import com.zbkj.common.request.IosBindingPhoneRequest;

/**
 * IOS服务类
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
public interface IosService {

    /**
     * ios绑定手机号（登录后）
     *
     * @param request 请求对象
     * @return 是否绑定
     */
    Boolean bindingPhone(IosBindingPhoneRequest request);
}
