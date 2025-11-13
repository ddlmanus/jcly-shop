package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 发票搜索请求类
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
@ApiModel(value = "InvoiceSearchRequest对象", description = "发票搜索请求")
public class InvoiceSearchRequest {

    @ApiModelProperty(value = "订单号/发票申请单号")
    private String keywords;

    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "用户ID")
    private Integer uid;

    @ApiModelProperty(value = "用户账号/手机号")
    private String userKeywords;

    @ApiModelProperty(value = "发票抬头")
    private String invoiceTitle;

    @ApiModelProperty(value = "发票类型：1-普通发票，2-专用发票")
    @Min(value = 1, message = "发票类型值不能小于1")
    @Max(value = 2, message = "发票类型值不能大于2")
    private Integer invoiceType;

    @ApiModelProperty(value = "发票抬头类型：1-个人，2-企业")
    @Min(value = 1, message = "发票抬头类型值不能小于1")
    @Max(value = 2, message = "发票抬头类型值不能大于2")
    private Integer invoiceTitleType;

    @ApiModelProperty(value = "开票状态：0-待开票，1-已开票，2-开票失败")
    @Min(value = 0, message = "开票状态值不能小于0")
    @Max(value = 2, message = "开票状态值不能大于2")
    private Integer status;

    @ApiModelProperty(value = "审核状态：0-待审核，1-审核通过，2-审核拒绝")
    @Min(value = 0, message = "审核状态值不能小于0")
    @Max(value = 2, message = "审核状态值不能大于2")
    private Integer auditStatus;

    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @ApiModelProperty(value = "结束时间")
    private String endTime;

    @ApiModelProperty(value = "订单状态过滤（可选）")
    private Integer orderStatus;

    private PageParamRequest pageParamRequest = new PageParamRequest();

    public PageParamRequest getPageParamRequest() {
        return pageParamRequest;
    }

    public void setPageParamRequest(PageParamRequest pageParamRequest) {
        this.pageParamRequest = pageParamRequest;
    }
} 