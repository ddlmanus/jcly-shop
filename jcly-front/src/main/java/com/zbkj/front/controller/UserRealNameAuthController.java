package com.zbkj.front.controller;

import com.zbkj.common.model.user.UserRealNameAuth;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.vo.MyRecord;
import com.zbkj.service.service.UserRealNameAuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 用户实名认证控制器
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
@RequestMapping("api/front/user/real-name-auth")
@Api(tags = "用户实名认证")
@Validated
public class UserRealNameAuthController {

    @Autowired
    private UserRealNameAuthService userRealNameAuthService;

    @ApiOperation(value = "获取用户实名认证状态")
    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getUserAuthStatus() {
        MyRecord result = userRealNameAuthService.getUserAuthStatus(null);
        return CommonResult.success(result);
    }

    @ApiOperation(value = "身份证OCR识别")
    @RequestMapping(value = "/ocr", method = RequestMethod.POST)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "idCardFrontImage", value = "身份证正面图片", required = true),
        @ApiImplicitParam(name = "idCardBackImage", value = "身份证反面图片", required = true)
    })
    public CommonResult<Map<String, Object>> performOcrRecognition(@RequestParam @NotNull String idCardFrontImage,
                                                       @RequestParam @NotNull String idCardBackImage) {
        MyRecord result = userRealNameAuthService.performOcrRecognition(null, idCardFrontImage, idCardBackImage);
        if (result.getBoolean("success")) {
            return CommonResult.success(result);
        } else {
            return CommonResult.failed(result.getStr("message"));
        }
    }

    @ApiOperation(value = "人脸识别")
    @RequestMapping(value = "/face", method = RequestMethod.POST)
    @ApiImplicitParam(name = "faceImage", value = "人脸图片", required = true)
    public CommonResult<Map<String, Object>> performFaceRecognition(@RequestParam @NotNull String faceImage) {
        MyRecord result = userRealNameAuthService.performFaceRecognition(null, faceImage);
        if (result.getBoolean("success")) {
            return CommonResult.success(result);
        } else {
            return CommonResult.failed(result.getStr("message"));
        }
    }

    @ApiOperation(value = "活体检测")
    @RequestMapping(value = "/liveness", method = RequestMethod.POST)
    @ApiImplicitParam(name = "faceVideo", value = "人脸视频", required = true)
    public CommonResult<Map<String, Object>> performLivenessDetection(@RequestParam @NotNull String faceVideo) {
        MyRecord result = userRealNameAuthService.performLivenessDetection(null, faceVideo);
        if (result.getBoolean("success")) {
            return CommonResult.success(result);
        } else {
            return CommonResult.failed(result.getStr("message"));
        }
    }

    @ApiOperation(value = "提交实名认证")
    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "authType", value = "认证类型 1=OCR 2=人脸 3=OCR+人脸", required = true),
        @ApiImplicitParam(name = "idCardFrontImage", value = "身份证正面图片"),
        @ApiImplicitParam(name = "idCardBackImage", value = "身份证反面图片"),
        @ApiImplicitParam(name = "faceImage", value = "人脸图片"),
        @ApiImplicitParam(name = "faceVideo", value = "人脸视频")
    })
    public CommonResult<Map<String, Object>> submitRealNameAuth(@RequestParam @NotNull Integer authType,
                                                    @RequestParam(required = false) String idCardFrontImage,
                                                    @RequestParam(required = false) String idCardBackImage,
                                                    @RequestParam(required = false) String faceImage,
                                                    @RequestParam(required = false) String faceVideo) {
        MyRecord result = userRealNameAuthService.submitRealNameAuth(null, authType, idCardFrontImage, 
                                                                     idCardBackImage, faceImage, faceVideo);
        if (result.getBoolean("success")) {
            return CommonResult.success(result);
        } else {
            return CommonResult.failed(result.getStr("message"));
        }
    }

    @ApiOperation(value = "获取用户实名认证详情")
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public CommonResult<UserRealNameAuth> getUserRealNameAuth() {
        UserRealNameAuth authRecord = userRealNameAuthService.getUserRealNameAuth(null);
        return CommonResult.success(authRecord);
    }

    @ApiOperation(value = "重新认证")
    @RequestMapping(value = "/re-auth", method = RequestMethod.POST)
    public CommonResult<Map<String, Object>> reAuth() {
        MyRecord result = userRealNameAuthService.reAuth(null);
        if (result.getBoolean("success")) {
            return CommonResult.success(result);
        } else {
            return CommonResult.failed(result.getStr("message"));
        }
    }

    @ApiOperation(value = "检查用户是否已实名认证")
    @RequestMapping(value = "/check", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> checkUserRealNameAuth() {
        boolean isAuth = userRealNameAuthService.isUserRealNameAuth(null);
        MyRecord result = new MyRecord();
        result.set("isAuth", isAuth);
        result.set("message", isAuth ? "已实名认证" : "未实名认证");
        return CommonResult.success(result);
    }
} 