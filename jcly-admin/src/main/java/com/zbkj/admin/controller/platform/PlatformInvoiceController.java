package com.zbkj.admin.controller.platform;

import com.zbkj.common.annotation.LogControllerAnnotation;
import com.zbkj.common.enums.MethodType;
import com.zbkj.common.model.invoice.InvoiceDescription;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.InvoiceAuditRequest;
import com.zbkj.common.request.InvoiceDescriptionRequest;
import com.zbkj.common.request.InvoiceSearchRequest;
import com.zbkj.common.response.InvoiceCountResponse;
import com.zbkj.common.response.InvoiceDetailResponse;
import com.zbkj.common.response.InvoicePageResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.service.service.InvoiceDescriptionService;
import com.zbkj.service.service.InvoiceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 平台端发票管理控制器
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
@RequestMapping("api/admin/platform/invoice")
@Api(tags = "平台端发票管理控制器")
public class PlatformInvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceDescriptionService invoiceDescriptionService;

    @PreAuthorize("hasAuthority('platform:invoice:list')")
    @ApiOperation(value = "发票分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<InvoicePageResponse>> getList(@Validated InvoiceSearchRequest request) {
        return CommonResult.success(CommonPage.restPage(invoiceService.getPageList(request)));
    }

    @PreAuthorize("hasAuthority('platform:invoice:count')")
    @ApiOperation(value = "获取发票各状态数量统计")
    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public CommonResult<InvoiceCountResponse> getInvoiceCount(@Validated InvoiceSearchRequest request) {
        return CommonResult.success(invoiceService.getInvoiceCount(request));
    }

    @PreAuthorize("hasAuthority('platform:invoice:detail')")
    @ApiOperation(value = "获取发票详情")
    @RequestMapping(value = "/detail/{invoiceId}", method = RequestMethod.GET)
    public CommonResult<InvoiceDetailResponse> getInvoiceDetail(@PathVariable("invoiceId") Integer invoiceId) {
        return CommonResult.success(invoiceService.getInvoiceDetail(invoiceId));
    }

    @PreAuthorize("hasAuthority('platform:invoice:detail:by:no')")
    @ApiOperation(value = "根据发票申请单号获取发票详情")
    @RequestMapping(value = "/detail/by/no/{invoiceNo}", method = RequestMethod.GET)
    public CommonResult<InvoiceDetailResponse> getInvoiceDetailByNo(@PathVariable("invoiceNo") String invoiceNo) {
        return CommonResult.success(invoiceService.getInvoiceDetailByNo(invoiceNo));
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "发票审核")
    @PreAuthorize("hasAuthority('platform:invoice:audit')")
    @ApiOperation(value = "发票审核")
    @RequestMapping(value = "/audit", method = RequestMethod.POST)
    public CommonResult<String> auditInvoice(@RequestBody @Validated InvoiceAuditRequest request) {
        Integer auditorId = SecurityUtil.getLoginUserVo().getUser().getId();
        if (invoiceService.auditInvoice(request, auditorId)) {
            return CommonResult.success("发票审核成功");
        }
        return CommonResult.failed("发票审核失败");
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.DELETE, description = "删除发票")
    @PreAuthorize("hasAuthority('platform:invoice:delete')")
    @ApiOperation(value = "删除发票")
    @RequestMapping(value = "/delete/{invoiceId}", method = RequestMethod.POST)
    public CommonResult<String> deleteInvoice(@PathVariable("invoiceId") Integer invoiceId) {
        if (invoiceService.deleteInvoice(invoiceId)) {
            return CommonResult.success("删除发票成功");
        }
        return CommonResult.failed("删除发票失败");
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.DELETE, description = "批量删除发票")
    @PreAuthorize("hasAuthority('platform:invoice:batch:delete')")
    @ApiOperation(value = "批量删除发票")
    @RequestMapping(value = "/batch/delete", method = RequestMethod.POST)
    public CommonResult<String> batchDeleteInvoice(@RequestParam("invoiceIds") String invoiceIds) {
        if (invoiceService.batchDeleteInvoice(invoiceIds)) {
            return CommonResult.success("批量删除发票成功");
        }
        return CommonResult.failed("批量删除发票失败");
    }

    // ====== 发票说明管理 ======

    @PreAuthorize("hasAuthority('platform:invoice:description:get')")
    @ApiOperation(value = "获取发票说明")
    @RequestMapping(value = "/description/get", method = RequestMethod.GET)
    public CommonResult<InvoiceDescription> getInvoiceDescription() {
        return CommonResult.success(invoiceDescriptionService.getInvoiceDescription());
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "更新发票说明")
    @PreAuthorize("hasAuthority('platform:invoice:description:update')")
    @ApiOperation(value = "更新发票说明")
    @RequestMapping(value = "/description/update", method = RequestMethod.POST)
    public CommonResult<String> updateInvoiceDescription(@RequestBody @Validated InvoiceDescriptionRequest request) {
        if (invoiceDescriptionService.updateInvoiceDescription(request)) {
            return CommonResult.success("更新发票说明成功");
        }
        return CommonResult.failed("更新发票说明失败");
    }
} 