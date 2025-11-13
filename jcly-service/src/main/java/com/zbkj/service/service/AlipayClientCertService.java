package com.zbkj.service.service;

import com.alipay.api.AlipayClient;

/**
 * 支付宝证书客户端服务接口
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
public interface AlipayClientCertService {

    /**
     * 获取支付宝证书客户端
     * @return AlipayClient
     */
    AlipayClient getCertClient();

    /**
     * 检查证书配置是否完整
     * @return boolean
     */
    boolean checkCertConfig();

    /**
     * 获取应用证书序列号
     * @return String
     */
    String getAppCertSN();

    /**
     * 获取支付宝根证书序列号
     * @return String
     */
    String getAlipayRootCertSN();
}
