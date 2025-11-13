package com.zbkj.admin.controller.platform;

import com.zbkj.common.annotation.LogControllerAnnotation;
import com.zbkj.common.enums.MethodType;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.ProductBrandAuditRequest;
import com.zbkj.common.request.ProductBrandAuditListRequest;
import com.zbkj.common.request.ProductBrandBatchAuditRequest;
import com.zbkj.common.request.ProductBrandRequest;
import com.zbkj.common.response.ProductBrandListResponse;
import com.zbkj.common.response.ProductBrandResponse;
import com.zbkj.common.response.ProductBrandAuditListResponse;
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
@RequestMapping("api/admin/platform/product/brand")
@Api(tags = "平台端商品品牌控制器")
public class ProductBrandController {

    @Autowired
    private ProductBrandService productBrandService;

    @PreAuthorize("hasAuthority('platform:product:brand:list')")
    @ApiOperation(value = "品牌分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<ProductBrandListResponse>> getList(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(productBrandService.getAdminPage(pageParamRequest)));
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "新增品牌")
    @PreAuthorize("hasAuthority('platform:product:brand:add')")
    @ApiOperation(value = "新增品牌")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public CommonResult<String> add(@RequestBody @Validated ProductBrandRequest request) {
        if (productBrandService.add(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.DELETE, description = "删除品牌")
    @PreAuthorize("hasAuthority('platform:product:brand:delete')")
    @ApiOperation(value = "删除品牌")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public CommonResult<String> delete(@PathVariable(value = "id") Integer id) {
        if (productBrandService.delete(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改品牌")
    @PreAuthorize("hasAuthority('platform:product:brand:update')")
    @ApiOperation(value = "修改品牌")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestBody @Validated ProductBrandRequest request) {
        if (productBrandService.edit(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改品牌显示状态")
    @PreAuthorize("hasAuthority('platform:product:brand:show:status')")
    @ApiOperation(value = "修改品牌显示状态")
    @RequestMapping(value = "/update/show/{id}", method = RequestMethod.POST)
    public CommonResult<Object> updateShowStatus(@PathVariable(value = "id") Integer id) {
        if (productBrandService.updateShowStatus(id)) {
            return CommonResult.success("修改成功");
        }
        return CommonResult.failed("修改失败");
    }

    @PreAuthorize("hasAuthority('platform:product:brand:cache:list')")
    @ApiOperation(value = "品牌缓存列表(全部)")
    @RequestMapping(value = "/cache/list", method = RequestMethod.GET)
    public CommonResult<List<ProductBrandResponse>> getCacheAllList() {
        return CommonResult.success(productBrandService.getCacheAllList());
    }

    @PreAuthorize("hasAuthority('platform:product:brand:audit')")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "审核商户申请的商品品牌")
    @ApiOperation(value = "审核商户申请的商品品牌")
    @RequestMapping(value = "/audit", method = RequestMethod.POST)
    public CommonResult<String> auditBrand(@RequestBody @Validated ProductBrandAuditRequest request) {
        if (productBrandService.auditMerchantBrand(request)) {
            return CommonResult.success("审核成功");
        }
        return CommonResult.failed("审核失败");
    }

    @PreAuthorize("hasAuthority('platform:product:brand:audit:list')")
    @ApiOperation(value = "商品品牌审核列表")
    @RequestMapping(value = "/audit/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<ProductBrandAuditListResponse>> getAuditList(
            @Validated ProductBrandAuditListRequest request) {
        return CommonResult.success(CommonPage.restPage(productBrandService.getAuditList(request)));
    }

  //  @PreAuthorize("hasAuthority('platform:product:brand:batch:audit')")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "批量审核商户申请的商品品牌")
    @ApiOperation(value = "批量审核商户申请的商品品牌")
    @RequestMapping(value = "/batch/audit", method = RequestMethod.POST)
    public CommonResult<String> batchAuditBrand(@RequestBody @Validated ProductBrandBatchAuditRequest request) {
        if (productBrandService.batchAuditMerchantBrand(request)) {
            return CommonResult.success("批量审核成功");
        }
        return CommonResult.failed("批量审核失败");
    }
}



