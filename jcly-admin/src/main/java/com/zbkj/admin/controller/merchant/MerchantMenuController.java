package com.zbkj.admin.controller.merchant;

import com.zbkj.common.result.CommonResult;
import com.zbkj.common.vo.MenuCheckVo;
import com.zbkj.service.service.SystemMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 商户端菜单控制器
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
@RequestMapping("api/admin/merchant/menu")
@Api(tags = "商户端菜单控制器")
public class MerchantMenuController {

    @Autowired
    private SystemMenuService systemMenuService;

    /**
     * 菜单缓存树
     */
    @PreAuthorize("hasAuthority('merchant:menu:cache:tree')")
    @ApiOperation(value = "菜单缓存树")
    @RequestMapping(value = "/cache/tree", method = RequestMethod.GET)
    public CommonResult<List<MenuCheckVo>> getMerchantMenuCacheTree() {
        return CommonResult.success(systemMenuService.getMenuCacheList());
    }
}



