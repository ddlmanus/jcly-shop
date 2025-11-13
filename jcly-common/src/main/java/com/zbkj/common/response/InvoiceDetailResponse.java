package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 发票详情响应类
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
@ApiModel(value = "InvoiceDetailResponse对象", description = "发票详情响应")
public class InvoiceDetailResponse {

    @ApiModelProperty(value = "发票ID")
    private Integer id;

    @ApiModelProperty(value = "发票申请单号")
    private String invoiceNo;

    @ApiModelProperty(value = "关联订单号")
    private String orderNo;

    @ApiModelProperty(value = "用户ID")
    private Integer uid;

    @ApiModelProperty(value = "用户账号")
    private String userAccount;

    @ApiModelProperty(value = "用户昵称")
    private String nickname;

    @ApiModelProperty(value = "用户手机号")
    private String userPhone;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal orderAmount;

    @ApiModelProperty(value = "订单状态")
    private Integer orderStatus;

    @ApiModelProperty(value = "订单状态文本")
    private String orderStatusText;

    @ApiModelProperty(value = "发票抬头")
    private String invoiceTitle;

    @ApiModelProperty(value = "发票类型：1-普通发票，2-专用发票")
    private Integer invoiceType;

    @ApiModelProperty(value = "发票类型文本")
    private String invoiceTypeText;

    @ApiModelProperty(value = "发票抬头类型：1-个人，2-企业")
    private Integer invoiceTitleType;

    @ApiModelProperty(value = "发票抬头类型文本")
    private String invoiceTitleTypeText;

    @ApiModelProperty(value = "发票金额")
    private BigDecimal invoiceAmount;

    @ApiModelProperty(value = "企业税号")
    private String taxNumber;

    @ApiModelProperty(value = "联系邮箱")
    private String contactEmail;

    @ApiModelProperty(value = "联系人姓名")
    private String contactName;

    @ApiModelProperty(value = "联系电话")
    private String contactPhone;

    @ApiModelProperty(value = "开票状态：0-待开票，1-已开票，2-开票失败")
    private Integer status;

    @ApiModelProperty(value = "开票状态文本")
    private String statusText;

    @ApiModelProperty(value = "发票号码")
    private String invoiceCode;

    @ApiModelProperty(value = "开票时间")
    private Date invoiceDate;

    @ApiModelProperty(value = "发票备注")
    private String remark;

    @ApiModelProperty(value = "审核状态：0-待审核，1-审核通过，2-审核拒绝")
    private Integer auditStatus;

    @ApiModelProperty(value = "审核状态文本")
    private String auditStatusText;

    @ApiModelProperty(value = "审核人员ID")
    private Integer auditorId;

    @ApiModelProperty(value = "审核人员姓名")
    private String auditorName;

    @ApiModelProperty(value = "审核时间")
    private Date auditTime;

    @ApiModelProperty(value = "审核备注")
    private String auditRemark;

    @ApiModelProperty(value = "发票文件URL")
    private String invoiceFileUrl;

    @ApiModelProperty(value = "发票文件名称")
    private String invoiceFileName;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    // 订单相关信息
    @ApiModelProperty(value = "订单商品信息")
    private String orderProductInfo;

    @ApiModelProperty(value = "收货地址")
    private String shippingAddress;

    @ApiModelProperty(value = "支付时间")
    private Date payTime;

    @ApiModelProperty(value = "支付方式")
    private String payType;
} 