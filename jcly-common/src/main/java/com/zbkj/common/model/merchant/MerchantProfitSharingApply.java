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
 * 商户分账申请记录表
 * </p>
 *
 * @author dudl
 * @since 2025-01-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_merchant_profit_sharing_apply")
@ApiModel(value = "MerchantProfitSharingApply对象", description = "商户分账申请记录表")
public class MerchantProfitSharingApply implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "申请单号")
    private String applyNo;

    @ApiModelProperty(value = "子商户号")
    private String subMchId;

    @ApiModelProperty(value = "申请分账比例")
    private BigDecimal sharingRatio;

    @ApiModelProperty(value = "分账接收方账户")
    private String account;

    @ApiModelProperty(value = "分账接收方姓名")
    private String name;

    @ApiModelProperty(value = "与分账方的关系类型")
    private String relationType;

    @ApiModelProperty(value = "自定义的分账关系")
    private String customRelation;

    @ApiModelProperty(value = "申请理由")
    private String applyReason;

    @ApiModelProperty(value = "申请状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝")
    private String applyStatus;

    @ApiModelProperty(value = "审核备注")
    private String auditRemark;

    @ApiModelProperty(value = "审核时间")
    private Date auditTime;

    @ApiModelProperty(value = "审核人ID")
    private Integer auditorId;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
} 