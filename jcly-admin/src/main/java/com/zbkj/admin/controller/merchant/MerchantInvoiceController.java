package com.zbkj.admin.controller.merchant;

import com.zbkj.common.annotation.LogControllerAnnotation;
import com.zbkj.common.enums.MethodType;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.InvoiceAuditRequest;
import com.zbkj.common.request.InvoiceSearchRequest;
import com.zbkj.common.request.MerchantInvoiceMergeRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.InvoiceCountResponse;
import com.zbkj.common.response.InvoiceDetailResponse;
import com.zbkj.common.response.InvoicePageResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.InvoiceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商户端发票管理控制器
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
@RequestMapping("api/admin/merchant/invoice")
@Api(tags = "商户端发票管理控制器")
public class MerchantInvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PreAuthorize("hasAuthority('merchant:invoice:list')")
    @ApiOperation(value = "发票分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<InvoicePageResponse>> getList(@Validated InvoiceSearchRequest request,
                                                                @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(invoiceService.getMerchantPage(request, pageParamRequest)));
    }

    @PreAuthorize("hasAuthority('merchant:invoice:count')")
    @ApiOperation(value = "发票数量统计")
    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public CommonResult<InvoiceCountResponse> getCount(@Validated InvoiceSearchRequest request) {
        return CommonResult.success(invoiceService.getMerchantCount(request));
    }

    @PreAuthorize("hasAuthority('merchant:invoice:detail')")
    @ApiOperation(value = "发票详情")
    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
    public CommonResult<InvoiceDetailResponse> getDetail(@PathVariable Integer id) {
        return CommonResult.success(invoiceService.getMerchantDetail(id));
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "商户端发票审核")
    @PreAuthorize("hasAuthority('merchant:invoice:audit')")
    @ApiOperation(value = "发票审核")
    @RequestMapping(value = "/audit", method = RequestMethod.POST)
    public CommonResult<String> audit(@RequestBody @Validated InvoiceAuditRequest request) {
        if (invoiceService.merchantAudit(request)) {
            return CommonResult.success("审核成功");
        }
        return CommonResult.failed("审核失败");
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "商户端合并开票")
    @PreAuthorize("hasAuthority('merchant:invoice:merge')")
    @ApiOperation(value = "合并开票")
    @RequestMapping(value = "/merge", method = RequestMethod.POST)
    public CommonResult<String> mergeInvoice(@RequestBody @Validated MerchantInvoiceMergeRequest request) {
        if (invoiceService.merchantMergeInvoice(request)) {
            return CommonResult.success("合并开票成功");
        }
        return CommonResult.failed("合并开票失败");
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.DELETE, description = "商户端删除发票")
    @PreAuthorize("hasAuthority('merchant:invoice:delete')")
    @ApiOperation(value = "删除发票")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public CommonResult<String> delete(@PathVariable Integer id) {
        if (invoiceService.merchantDelete(id)) {
            return CommonResult.success("删除成功");
        }
        return CommonResult.failed("删除失败");
    }
} 