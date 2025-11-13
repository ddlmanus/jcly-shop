package com.zbkj.service.service;

import com.zbkj.common.response.PlantFormScanResponse;

/**
 * 采食家平台数据同步服务
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
public interface CaiShiJiaPlatformService {

    /**
     * 同步采食家平台大屏数据
     * @return PlantFormScanResponse 大屏统计数据
     */
    PlantFormScanResponse syncPlatformData();

    /**
     * 登录采食家平台获取Token
     * @param account 账户
     * @param password 密码
     * @return Token字符串
     */
    String loginToPlatform(String account, String password);

    /**
     * 同步并保存采食家平台数据到数据库
     * @return 是否成功
     */
    boolean syncAndSaveData();
}