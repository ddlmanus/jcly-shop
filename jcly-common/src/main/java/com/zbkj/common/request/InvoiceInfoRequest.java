package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Email;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "InvoiceInfoRequest对象", description = "用户开票信息请求")
public class InvoiceInfoRequest {


    @ApiModelProperty(value = "发票类型：1-普通电子发票，2-专用发票", required = true)
    @NotNull(message = "发票类型不能为空")
    private Integer invoiceType;
    @ApiModelProperty(value = "发票抬头类型：1-个人，2-企业", required = true)
    @NotNull(message = "发票抬头类型不能为空")
    private Integer invoiceTitleType;
    @ApiModelProperty(value = "发票抬头", required = true)
    @NotBlank(message = "发票抬头不能为空")
    private String invoiceTitle;

    @ApiModelProperty(value = "企业税号（抬头为企业时必填）")
    private String taxNumber;


    @ApiModelProperty(value = "联系人姓名")
    private String contactName;

    @ApiModelProperty(value = "联系人邮箱", required = true)
    @NotBlank(message = "联系人邮箱不能为空")
    @Email(message = "联系人格式不正确")
    private String contactEmail;

    @ApiModelProperty(value = "联系电话")
    private String contactPhone;
    @ApiModelProperty(value = "注册地址（抬头为企业）")
    private String contactAddress;
    @ApiModelProperty(value = "开户银行（抬头为企业）")
    private String bankName;
    @ApiModelProperty(value = "银行账号（抬头为企业）")
    private String bankAccount;
    @ApiModelProperty(value = "发票内容（商品类别，商品明细）")
    private String invoiceContent;
} 