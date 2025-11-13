package com.zbkj.common.model.invoice;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 发票申请表
 * </p>
 *
 * @author dudl
 * @since 2025-01-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_invoice")
@ApiModel(value = "Invoice对象", description = "发票申请表")
public class Invoice implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "发票ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "发票申请单号")
    private String invoiceNo;

    @ApiModelProperty(value = "关联订单号")
    private String orderNo;

    @ApiModelProperty(value = "用户ID")
    private Integer uid;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;
    @ApiModelProperty(value = "发票抬头类型：1-个人，2-企业")
    private Integer invoiceTitleType;
    @ApiModelProperty(value = "发票抬头")
    private String invoiceTitle;

    @ApiModelProperty(value = "发票类型：1-普通发票，2-专用发票")
    private Integer invoiceType;

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
    @ApiModelProperty(value = "联系地址")
    private String contactAddress;
    @ApiModelProperty(value = "开户银行")
    private String bankName;
    @ApiModelProperty(value = "银行账号")
    private String bankAccount;
    @ApiModelProperty(value = "发票内容")
    private String invoiceContent;

    @ApiModelProperty(value = "开票状态：0-待开票，1-已开票，2-开票失败")
    private Integer status;

    @ApiModelProperty(value = "发票号码")
    private String invoiceCode;

    @ApiModelProperty(value = "开票时间")
    private Date invoiceDate;

    @ApiModelProperty(value = "发票备注")
    private String remark;

    @ApiModelProperty(value = "审核状态：0-待审核，1-审核通过，2-审核拒绝")
    private Integer auditStatus;

    @ApiModelProperty(value = "审核人员ID")
    private Integer auditorId;

    @ApiModelProperty(value = "审核时间")
    private Date auditTime;

    @ApiModelProperty(value = "审核备注")
    private String auditRemark;

    @ApiModelProperty(value = "发票文件URL")
    private String invoiceFileUrl;

    @ApiModelProperty(value = "发票文件名称")
    private String invoiceFileName;

    @ApiModelProperty(value = "是否删除")
    private Boolean isDel;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
} 