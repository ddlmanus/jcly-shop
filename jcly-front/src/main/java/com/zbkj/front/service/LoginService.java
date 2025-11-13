package com.zbkj.front.service;

import com.zbkj.common.request.*;
import com.zbkj.common.response.FrontLoginConfigResponse;
import com.zbkj.common.response.LoginResponse;
import com.zbkj.common.response.PcLoginConfigResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户登录服务
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
public interface LoginService {

    /**
     * 获取登录配置
     */
    FrontLoginConfigResponse getLoginConfig();

    /**
     * 手机号验证码登录
     */
    LoginResponse phoneCaptchaLogin(LoginMobileRequest loginRequest);

    /**
     * 手机号密码登录
     */
    LoginResponse phonePasswordLogin(LoginPasswordRequest loginRequest);

    /**
     * 退出登录
     */
    void loginOut(HttpServletRequest request);

    /**
     * 发送短信登录验证码
     */
    boolean sendLoginCode(String phone);

    /**
     * 微信公众号授权登录
     */
    LoginResponse wechatPublicLogin(WechatPublicLoginRequest request);

    /**
     * 微信小程序授权登录
     */
    LoginResponse wechatRoutineLogin(RegisterThirdUserRequest request);

    /**
     * 微信注册绑定手机号
     */
    LoginResponse wechatRegisterBindingPhone(WxBindingPhoneRequest request);

    /**
     * 微信App授权登录
     */
    LoginResponse wechatAppLogin(RegisterAppWxRequest request);

    /**
     * ios登录
     */
    LoginResponse ioslogin(IosLoginRequest loginRequest);

    /**
     * 检测token是否有效
     */
    Boolean tokenIsExist();

    /**
     * 获取PC登录配置
     */
    PcLoginConfigResponse getPcLoginConfig();

    /**
     * 微信PC授权登录
     */
    LoginResponse wechatPcLogin(WechatPublicLoginRequest request);

    /**
     * 获取PC商城微信公众号用户同意登录信息
     */
    LoginResponse getWechatPublicAgreeInfo(String ticket);
    
    /**
     * 手机号快速注册登录
     */
    LoginResponse phoneQuickLogin(PhoneQuickLoginRequest loginRequest);

    String getMobileAes();

    LoginResponse phoneAesQuickLogin(PhoneQuickAesLoginRequest loginRequest);
}