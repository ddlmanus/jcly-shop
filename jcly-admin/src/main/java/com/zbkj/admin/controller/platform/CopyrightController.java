package com.zbkj.admin.controller.platform;

import com.zbkj.admin.copyright.CopyrightInfoResponse;
import com.zbkj.admin.copyright.CopyrightUpdateInfoRequest;
import com.zbkj.admin.service.CopyrightService;
import com.zbkj.common.result.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * 版权控制器
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
@RequestMapping("api/admin/platform/copyright")
@Api(tags = "版权控制器")
public class CopyrightController {

    @Autowired
    private CopyrightService copyrightService;

    @ApiOperation(value = "获取版权信息")
    @RequestMapping(value = "/get/info", method = RequestMethod.GET)
    public CommonResult<CopyrightInfoResponse> getInfo() {
        return CommonResult.success(copyrightService.getInfo());
    }

    @PreAuthorize("hasAuthority('platform:copyright:update:company:info')")
    @ApiOperation(value = "编辑公司版权信息")
    @RequestMapping(value = "/update/company/info", method = RequestMethod.POST)
    public CommonResult<Object> updateCompanyInfo(@RequestBody @Validated CopyrightUpdateInfoRequest request) {
        if (copyrightService.updateCompanyInfo(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }
}
