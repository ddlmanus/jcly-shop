package com.zbkj.admin.controller.merchant;

import com.zbkj.common.annotation.LogControllerAnnotation;
import com.zbkj.common.enums.MethodType;
import com.zbkj.common.request.MerchantApplyProductCategoryRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.response.ProductCategoryAuditListResponse;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.vo.ProCategoryCacheVo;
import com.zbkj.service.service.ProductCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 商户端商品分类控制器
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
@RequestMapping("api/admin/merchant/plat/product/category")
@Api(tags = "商户端商品分类控制器")
public class PlatProductCategoryController {

    @Autowired
    private ProductCategoryService productCategoryService;

   // @PreAuthorize("hasAuthority('merchant:plat:product:category:cache:tree')")
    @ApiOperation(value = "分类缓存树")
    @RequestMapping(value = "/cache/tree", method = RequestMethod.GET)
    public CommonResult<List<ProCategoryCacheVo>> getMerchantCacheTree() {
        return CommonResult.success(productCategoryService.getMerchantCacheTree());
    }

   // @PreAuthorize("hasAuthority('merchant:plat:product:category:apply')")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "商户申请平台商品分类")
    @ApiOperation(value = "商户申请平台商品分类")
    @RequestMapping(value = "/apply", method = RequestMethod.POST)
    public CommonResult<String> applyPlatformCategory(@RequestBody @Validated MerchantApplyProductCategoryRequest request) {
        if (productCategoryService.merchantApplyCategory(request)) {
            return CommonResult.success("申请提交成功，等待平台审核");
        }
        return CommonResult.failed("申请提交失败");
    }

    @PreAuthorize("hasAuthority('merchant:plat:product:category:my:list')")
    @ApiOperation(value = "获取商户自己申请的分类列表")
    @RequestMapping(value = "/my/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<ProductCategoryAuditListResponse>> getMyAppliedCategoryList(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(productCategoryService.getMerchantAppliedCategoryList(pageParamRequest)));
    }
}



