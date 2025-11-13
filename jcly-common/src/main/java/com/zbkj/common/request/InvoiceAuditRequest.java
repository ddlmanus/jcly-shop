package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 发票审核请求类
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
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "InvoiceAuditRequest对象", description = "发票审核请求")
public class InvoiceAuditRequest {

    @ApiModelProperty(value = "发票ID", required = true)
    @NotNull(message = "发票ID不能为空")
    private Integer invoiceId;

    @ApiModelProperty(value = "审核状态：1-审核通过，2-审核拒绝", required = true)
    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;

    @ApiModelProperty(value = "审核备注")
    @Size(max = 500, message = "审核备注长度不能超过500个字符")
    private String auditRemark;

    @ApiModelProperty(value = "发票号码（审核通过时填写）")
    @Size(max = 50, message = "发票号码长度不能超过50个字符")
    private String invoiceCode;

    @ApiModelProperty(value = "发票备注")
    @Size(max = 500, message = "发票备注长度不能超过500个字符")
    private String remark;

    @ApiModelProperty(value = "发票文件URL")
    @Size(max = 500, message = "发票文件URL长度不能超过500个字符")
    private String invoiceFileUrl;

    @ApiModelProperty(value = "发票文件名称")
    @Size(max = 200, message = "发票文件名称长度不能超过200个字符")
    private String invoiceFileName;
} 