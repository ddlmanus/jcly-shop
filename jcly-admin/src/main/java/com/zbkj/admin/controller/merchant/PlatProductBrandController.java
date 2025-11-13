package com.zbkj.admin.controller.merchant;

import com.zbkj.common.annotation.LogControllerAnnotation;
import com.zbkj.common.enums.MethodType;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.BrandCategorySearchRequest;
import com.zbkj.common.request.MerchantApplyProductBrandRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.ProductBrandAuditListResponse;
import com.zbkj.common.response.ProductBrandResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.ProductBrandService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 平台端商品品牌控制器
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
@RequestMapping("api/admin/merchant/plat/product/brand")
@Api(tags = "商户端商品品牌控制器")
public class PlatProductBrandController {

    @Autowired
    private ProductBrandService productBrandService;

    @PreAuthorize("hasAuthority('merchant:plat:product:brand:list')")
    @ApiOperation(value = "品牌分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<ProductBrandResponse>> getList(@Validated BrandCategorySearchRequest request, @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(productBrandService.getPageListByCategory(request, pageParamRequest)));
    }

    @PreAuthorize("hasAuthority('merchant:plat:product:brand:cache:list')")
    @ApiOperation(value = "品牌缓存列表(全部)")
    @RequestMapping(value = "/cache/list", method = RequestMethod.GET)
    public CommonResult<List<ProductBrandResponse>> getCacheAllList() {
        return CommonResult.success(productBrandService.getCacheAllList());
    }

  //  @PreAuthorize("hasAuthority('merchant:plat:product:brand:apply')")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "商户申请平台商品品牌")
    @ApiOperation(value = "商户申请平台商品品牌")
    @RequestMapping(value = "/apply", method = RequestMethod.POST)
    public CommonResult<String> applyPlatformBrand(@RequestBody @Validated MerchantApplyProductBrandRequest request) {
        if (productBrandService.merchantApplyBrand(request)) {
            return CommonResult.success("申请提交成功，等待平台审核");
        }
        return CommonResult.failed("申请提交失败");
    }

    @PreAuthorize("hasAuthority('merchant:plat:product:brand:my:list')")
    @ApiOperation(value = "获取商户自己申请的品牌列表")
    @RequestMapping(value = "/my/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<ProductBrandAuditListResponse>> getMyAppliedBrandList(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(productBrandService.getMerchantAppliedBrandList(pageParamRequest)));
    }
}



