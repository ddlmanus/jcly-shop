package com.zbkj.admin.service;

import javax.servlet.http.HttpServletRequest;

/**
 * 微信公众号消息 服务类
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
public interface WechatMessageService {
//    String init(HttpServletRequest request);

    String messageEvent(HttpServletRequest request);
}
