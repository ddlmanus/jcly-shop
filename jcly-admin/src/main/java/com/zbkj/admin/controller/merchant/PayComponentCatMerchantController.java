package com.zbkj.admin.controller.merchant;

import com.zbkj.common.result.CommonResult;
import com.zbkj.common.vo.wxvedioshop.cat_brand.CatItem;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 组件类目表 前端控制器
 *  +----------------------------------------------------------------------
 *  | JCLY [ JCLY赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 *  +----------------------------------------------------------------------
 *  | Author: dudl
 *  +----------------------------------------------------------------------
 */
@Slf4j
@RestController
@RequestMapping("api/admin/merchant/pay/component/cat")
@Api(tags = "04小程序 自定义交易组件—类目") //配合swagger使用
public class PayComponentCatMerchantController {

//    @Autowired
//    private PayComponentCatService payComponentCatService;

    /**
     * 获取类目列表
     */
    @PreAuthorize("hasAuthority('merchant:pay:component:cat:list')")
    @ApiOperation(value = "获取类目列表")
    @RequestMapping(value = "/get/list", method = RequestMethod.GET)
    public CommonResult<List<CatItem>> getList() {
//        return CommonResult.success(payComponentCatService.getTreeList());
        return CommonResult.success();
    }


    /**
     * 获取类目(测试用，前端不调用)
     */
    @ApiOperation(value = "获取类目(测试用，前端不调用)")
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public CommonResult<Object> sendUserCode() {
//        payComponentCatService.autoUpdate();
        return CommonResult.success();
    }
}



