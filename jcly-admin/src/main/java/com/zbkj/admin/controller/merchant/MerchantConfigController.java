package com.zbkj.admin.controller.merchant;

import com.zbkj.admin.service.PcShoppingService;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.vo.MerchantPcShoppingConfigVo;
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
 * 配置表 前端控制器
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
@RequestMapping("api/admin/merchant/config")
@Api(tags = "商户端设置")
public class MerchantConfigController {

    @Autowired
    private PcShoppingService pcShoppingService;

    @PreAuthorize("hasAuthority('merchant:config:pc:shopping:get')")
    @ApiOperation(value = "获取商户PC商城设置")
    @RequestMapping(value = "/get/pc/shopping/config", method = RequestMethod.GET)
    public CommonResult<MerchantPcShoppingConfigVo> getPcShoppingConfig() {
        return CommonResult.success(pcShoppingService.getMerchantPcShoppingConfig());
    }


    @PreAuthorize("hasAuthority('merchant:config:pc:shopping:save')")
    @ApiOperation(value = "编辑商户PC商城设置")
    @RequestMapping(value = "/save/pc/shopping/config", method = RequestMethod.POST)
    public CommonResult<Object> updatePcShoppingConfig(@RequestBody @Validated MerchantPcShoppingConfigVo voRequest) {
        if (pcShoppingService.updateMerchantPcShoppingConfig(voRequest)) {
            return CommonResult.success().setMessage("编辑成功");
        }
        return CommonResult.failed().setMessage("编辑失败");
    }
}



