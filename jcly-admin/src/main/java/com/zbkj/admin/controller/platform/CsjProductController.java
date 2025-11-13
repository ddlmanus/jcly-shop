package com.zbkj.admin.controller.platform;

import com.zbkj.common.annotation.LogControllerAnnotation;
import com.zbkj.common.enums.MethodType;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.CsjProductAddRequest;
import com.zbkj.common.request.CsjProductSearchRequest;
import com.zbkj.common.response.CsjProductInfoResponse;
import com.zbkj.common.response.CsjProductListResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.CsjProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 采食家商品控制器
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
@RequestMapping("api/admin/platform/csj/product")
@Api(tags = "采食家商品控制器")
public class CsjProductController {

    @Autowired
    private CsjProductService csjProductService;

  //  @PreAuthorize("hasAuthority('platform:csj:product:page:list')")
    @ApiOperation(value = "采食家商品分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<CsjProductListResponse>> getList(@Validated CsjProductSearchRequest request) {
        return CommonResult.success(CommonPage.restPage(csjProductService.getList(request)));
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "新增采食家商品")
 //   @PreAuthorize("hasAuthority('platform:csj:product:add')")
    @ApiOperation(value = "新增采食家商品")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public CommonResult<String> add(@RequestBody @Validated CsjProductAddRequest request) {
        if (csjProductService.save(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "编辑采食家商品")
   // @PreAuthorize("hasAuthority('platform:csj:product:update')")
    @ApiOperation(value = "编辑采食家商品")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestBody @Validated CsjProductAddRequest request) {
        if (csjProductService.updateProduct(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

   // @PreAuthorize("hasAuthority('platform:csj:product:info')")
    @ApiOperation(value = "采食家商品详情")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    public CommonResult<CsjProductInfoResponse> info(@PathVariable Integer id) {
        return CommonResult.success(csjProductService.getInfo(id));
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.DELETE, description = "删除采食家商品")
  //  @PreAuthorize("hasAuthority('platform:csj:product:delete')")
    @ApiOperation(value = "删除采食家商品")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public CommonResult<String> delete(@PathVariable Integer id) {
        if (csjProductService.deleteProduct(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "采食家商品上架")
   // @PreAuthorize("hasAuthority('platform:csj:product:on:shelf')")
    @ApiOperation(value = "采食家商品上架")
    @RequestMapping(value = "/on-shelf/{id}", method = RequestMethod.POST)
    public CommonResult<String> putOnShelf(@PathVariable Integer id) {
        if (csjProductService.putOnShelf(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "采食家商品下架")
   // @PreAuthorize("hasAuthority('platform:csj:product:off:shelf')")
    @ApiOperation(value = "采食家商品下架")
    @RequestMapping(value = "/off-shelf/{id}", method = RequestMethod.POST)
    public CommonResult<String> offShelf(@PathVariable Integer id) {
        if (csjProductService.offShelf(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }
}
