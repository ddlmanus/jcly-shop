package com.zbkj.front.controller;


import com.zbkj.common.request.*;
import com.zbkj.common.request.PhoneQuickLoginRequest;
import com.zbkj.common.response.FrontLoginConfigResponse;
import com.zbkj.common.response.LoginResponse;
import com.zbkj.common.response.PcLoginConfigResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.utils.AesUtils;
import com.zbkj.front.service.LoginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户登陆 前端控制器
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
@Slf4j
@RestController
@RequestMapping("api/front/login")
@Api(tags = "用户 -- 登录注册")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @ApiOperation(value = "获取登录配置")
    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public CommonResult<FrontLoginConfigResponse> getLoginConfig() {
        return CommonResult.success(loginService.getLoginConfig());
    }

    @ApiOperation(value = "手机号验证码登录")
    @RequestMapping(value = "/mobile/captcha", method = RequestMethod.POST)
    public CommonResult<LoginResponse> phoneCaptchaLogin(@RequestBody @Validated LoginMobileRequest loginRequest) {
        return CommonResult.success(loginService.phoneCaptchaLogin(loginRequest));
    }

    @ApiOperation(value = "手机号密码登录")
    @RequestMapping(value = "/mobile/password", method = RequestMethod.POST)
    public CommonResult<LoginResponse> phonePasswordLogin(@RequestBody @Validated LoginPasswordRequest loginRequest) {
        return CommonResult.success(loginService.phonePasswordLogin(loginRequest));
    }

    @ApiOperation(value = "退出")
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public CommonResult<String> loginOut(HttpServletRequest request) {
        loginService.loginOut(request);
        return CommonResult.success();
    }

    @ApiOperation(value = "发送短信登录验证码")
    @RequestMapping(value = "/send/code", method = RequestMethod.POST)
    public CommonResult<String> sendCode(@RequestBody @Validated SendCodeRequest request) {
        if (loginService.sendLoginCode(request.getPhone())) {
            return CommonResult.success("发送成功");
        }
        return CommonResult.failed("发送失败");
    }

    @ApiOperation(value = "微信公众号号授权登录")
    @RequestMapping(value = "/wechat/public", method = RequestMethod.POST)
    public CommonResult<LoginResponse> wechatPublicLogin(@RequestBody @Validated WechatPublicLoginRequest request) {
        return CommonResult.success(loginService.wechatPublicLogin(request));
    }

    @ApiOperation(value = "微信登录小程序授权登录")
    @RequestMapping(value = "/wechat/routine", method = RequestMethod.POST)
    public CommonResult<LoginResponse> wechatRoutineLogin(@RequestBody @Validated RegisterThirdUserRequest request) {
        return CommonResult.success(loginService.wechatRoutineLogin(request));
    }

    @ApiOperation(value = "微信注册绑定手机号")
    @RequestMapping(value = "/wechat/register/binding/phone", method = RequestMethod.POST)
    public CommonResult<LoginResponse> wechatRegisterBindingPhone(@RequestBody @Validated WxBindingPhoneRequest request) {
        return CommonResult.success(loginService.wechatRegisterBindingPhone(request));
    }

    @ApiOperation(value = "微信登录App授权登录")
    @RequestMapping(value = "/wechat/app/login", method = RequestMethod.POST)
    public CommonResult<LoginResponse> wechatAppLogin(@RequestBody @Validated RegisterAppWxRequest request) {
        return CommonResult.success(loginService.wechatAppLogin(request));
    }

    @ApiOperation(value = "ios登录")
    @RequestMapping(value = "/ios/login", method = RequestMethod.POST)
    public CommonResult<LoginResponse> ioslogin(@RequestBody @Validated IosLoginRequest loginRequest) {
        return CommonResult.success(loginService.ioslogin(loginRequest));
    }

    @ApiOperation(value = "校验token是否有效")
    @RequestMapping(value = "/token/is/exist", method = RequestMethod.POST)
    public CommonResult<Boolean> tokenIsExist() {
        return CommonResult.success(loginService.tokenIsExist());
    }
    
    @ApiOperation(value = "手机号快速注册登录")
    @RequestMapping(value = "/mobile/quick", method = RequestMethod.POST)
    public CommonResult<LoginResponse> phoneQuickLogin(@RequestBody @Validated PhoneQuickLoginRequest loginRequest) {
        return CommonResult.success(loginService.phoneQuickLogin(loginRequest));
    }
    @ApiOperation(value = "手机号加密注册登录")
    @RequestMapping(value = "/mobile/aesquick", method = RequestMethod.POST)
    public CommonResult<LoginResponse> phoneAesQuickLogin(@RequestBody @Validated PhoneQuickAesLoginRequest loginRequest) {
        return CommonResult.success(loginService.phoneAesQuickLogin(loginRequest));
    }

    /**
     * 获取手机号加密
     */
    @ApiOperation(value = "获取手机号加密")
    @RequestMapping(value = "/mobile/aes", method = RequestMethod.GET)
    public CommonResult<String> getMobileAes() {
        //获取当前用户手机号
       return CommonResult.success(loginService.getMobileAes());
    }


    /**
     * ----------------------------------------------------------------
     * 以下为PC商城登录独有部分
     * ----------------------------------------------------------------
     */

    @ApiOperation(value = "获取PC商城登录配置")
    @RequestMapping(value = "/pc/config", method = RequestMethod.GET)
    public CommonResult<PcLoginConfigResponse> getPcLoginConfig() {
        return CommonResult.success(loginService.getPcLoginConfig());
    }

    @ApiOperation(value = "微信PC授权登录（网页应用）")
    @RequestMapping(value = "/wechat/pc", method = RequestMethod.POST)
    public CommonResult<LoginResponse> wechatPcLogin(@RequestBody @Validated WechatPublicLoginRequest request) {
        return CommonResult.success(loginService.wechatPcLogin(request));
    }

    @ApiOperation(value = "获取PC商城微信公众号用户同意登录信息")
    @RequestMapping(value = "/pc/wechat/public/agree/info", method = RequestMethod.GET)
    public CommonResult<LoginResponse> getWechatPublicAgreeInfo(@RequestParam(value = "ticket") String ticket) {
        return CommonResult.success(loginService.getWechatPublicAgreeInfo(ticket));
    }
}



