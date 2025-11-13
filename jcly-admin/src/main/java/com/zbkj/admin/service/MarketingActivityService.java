package com.zbkj.admin.service;

import com.zbkj.common.request.BirthdayPresentRequest;
import com.zbkj.common.request.NewPeoplePresentRequest;
import com.zbkj.common.response.BirthdayPresentResponse;
import com.zbkj.common.response.NewPeoplePresentResponse;

/**
 * 营销活动service
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
public interface MarketingActivityService {

    /**
     * 获取生日有礼配置
     */
    BirthdayPresentResponse getBirthdayPresentConfig();

    /**
     * 编辑生日有礼配置
     * @param request 请求参数
     */
    Boolean editBirthdayPresent(BirthdayPresentRequest request);

    /**
     * 获取新人礼配置
     */
    NewPeoplePresentResponse getNewPeopleConfig();

    /**
     * 编辑新人礼配置
     * @param request 请求参数
     */
    Boolean editNewPeopleConfig(NewPeoplePresentRequest request);

    /**
     * 发送生日有礼
     */
    void sendBirthdayPresent();
}
