package com.zbkj.admin.controller.merchant;

import com.zbkj.admin.service.CopyrightService;
import com.zbkj.common.result.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("api/admin/merchant/copyright")
@Api(tags = "商户端版权控制器")
public class MerchantCopyrightController {

    @Autowired
    private CopyrightService copyrightService;

    @PreAuthorize("hasAuthority('merchant:copyright:get:company:info')")
    @ApiOperation(value = "获取商户版权信息")
    @RequestMapping(value = "/get/company/info", method = RequestMethod.GET)
    public CommonResult<Object> getCompanyInfo() {
        return CommonResult.success(copyrightService.getCompanyInfo());
    }

}
