package com.zbkj.admin.controller.publicly;

import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公开设置控制器
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
@RequestMapping("api/publicly/config")
@Api(tags = "公开设置控制器")
public class PubliclyConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    @ApiOperation(value = "获取移动端域名")
    @RequestMapping(value = "/get/front/domain", method = RequestMethod.GET)
    public CommonResult<String> getFrontDomain() {
        return CommonResult.success(systemConfigService.getFrontDomain());
    }

    @ApiOperation(value = "获取平台当前的素材地址")
    @RequestMapping(value = "/get/admin/mediadomain", method = RequestMethod.GET)
    public CommonResult<String> getMediaDomain() {
        return CommonResult.success(systemConfigService.getMediaDomain());
    }

    @ApiOperation(value = "获取主题色")
    @RequestMapping(value = "/get/change/color", method = RequestMethod.GET)
    public CommonResult<String> getChangeColor() {
        return CommonResult.success(systemConfigService.getChangeColor().getValue());
    }
}
