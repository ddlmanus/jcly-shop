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
 * 用户充值记录表
 * </p>
 *
 * @author System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_user_recharge_record")
@ApiModel(value="UserRechargeRecord对象", description="用户充值记录表")
public class UserRechargeRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "充值记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "用户ID")
    private Integer uid;

    @ApiModelProperty(value = "充值单号")
    private String rechargeNo;

    @ApiModelProperty(value = "银行卡ID")
    private Integer bankCardId;

    @ApiModelProperty(value = "充值金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "充值前余额")
    private BigDecimal balanceBefore;

    @ApiModelProperty(value = "充值后余额")
    private BigDecimal balanceAfter;

    @ApiModelProperty(value = "充值状态 0=待支付 1=充值成功 2=充值失败")
    private Integer status;

    @ApiModelProperty(value = "充值方式 1=银行卡")
    private Integer rechargeType;

    @ApiModelProperty(value = "银联交易流水号")
    private String transactionId;

    @ApiModelProperty(value = "充值时间")
    private Date rechargeTime;

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