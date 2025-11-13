package com.zbkj.common.model.merchant;

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
 * 商户分账明细记录表
 * </p>
 *
 * @author dudl
 * @since 2025-01-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_merchant_profit_sharing_detail")
@ApiModel(value = "MerchantProfitSharingDetail对象", description = "商户分账明细记录表")
public class MerchantProfitSharingDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "分账单号")
    private String sharingNo;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "微信支付订单号")
    private String transactionId;

    @ApiModelProperty(value = "商户分账单号")
    private String outOrderNo;

    @ApiModelProperty(value = "分账接收方子商户号")
    private String subMchId;

    @ApiModelProperty(value = "分账金额（分）")
    private BigDecimal sharingAmount;

    @ApiModelProperty(value = "分账比例")
    private BigDecimal sharingRatio;

    @ApiModelProperty(value = "订单总金额（分）")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "分账类型")
    private String sharingType;

    @ApiModelProperty(value = "分账接收方账户")
    private String account;

    @ApiModelProperty(value = "分账接收方姓名")
    private String name;

    @ApiModelProperty(value = "与分账方的关系类型")
    private String relationType;

    @ApiModelProperty(value = "分账状态：PROCESSING-处理中，SUCCESS-分账成功，FAILED-分账失败")
    private String sharingStatus;

    @ApiModelProperty(value = "分账时间")
    private Date sharingTime;

    @ApiModelProperty(value = "微信分账返回结果")
    private String wechatResult;

    @ApiModelProperty(value = "错误代码")
    private String errorCode;

    @ApiModelProperty(value = "错误信息")
    private String errorMsg;

    @ApiModelProperty(value = "重试次数")
    private Integer retryCount;

    @ApiModelProperty(value = "最大重试次数")
    private Integer maxRetryCount;

    @ApiModelProperty(value = "下次重试时间")
    private Date nextRetryTime;

    @ApiModelProperty(value = "完成时间")
    private Date completeTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
} 