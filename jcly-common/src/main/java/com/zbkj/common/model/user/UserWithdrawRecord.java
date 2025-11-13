package com.zbkj.common.model.user;

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
 * 用户提现记录表
 * </p>
 *
 * @author System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_user_withdraw_record")
@ApiModel(value="UserWithdrawRecord对象", description="用户提现记录表")
public class UserWithdrawRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "提现记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "用户ID")
    private Integer uid;

    @ApiModelProperty(value = "提现单号")
    private String withdrawNo;

    @ApiModelProperty(value = "银行卡ID")
    private Integer bankCardId;

    @ApiModelProperty(value = "提现金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "提现前余额")
    private BigDecimal balanceBefore;

    @ApiModelProperty(value = "提现后余额")
    private BigDecimal balanceAfter;

    @ApiModelProperty(value = "提现状态 0=待审核 1=审核通过 2=提现成功 3=提现失败 4=审核拒绝")
    private Integer status;

    @ApiModelProperty(value = "提现方式 1=银行卡")
    private Integer withdrawType;

    @ApiModelProperty(value = "银联交易流水号")
    private String transactionId;

    @ApiModelProperty(value = "申请时间")
    private Date applyTime;

    @ApiModelProperty(value = "审核时间")
    private Date auditTime;

    @ApiModelProperty(value = "审核人ID")
    private Integer auditorId;

    @ApiModelProperty(value = "审核备注")
    private String auditRemark;

    @ApiModelProperty(value = "提现时间")
    private Date withdrawTime;

    @ApiModelProperty(value = "失败原因")
    private String failReason;

    @ApiModelProperty(value = "手续费")
    private BigDecimal fee;

    @ApiModelProperty(value = "实际到账金额")
    private BigDecimal actualAmount;

    @ApiModelProperty(value = "IP地址")
    private String ipAddress;

    @ApiModelProperty(value = "用户代理")
    private String userAgent;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "是否删除 0=否 1=是")
    private Boolean isDel;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
} 