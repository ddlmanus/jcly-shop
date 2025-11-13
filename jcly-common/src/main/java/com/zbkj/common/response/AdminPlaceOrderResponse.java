package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 管理员代客下单响应对象
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
@ApiModel(value = "AdminPlaceOrderResponse对象", description = "管理员代客下单响应对象")
public class AdminPlaceOrderResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "预下单号（如果未立即支付）")
    private String preOrderNo;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal orderAmount;

    @ApiModelProperty(value = "实际支付金额")
    private BigDecimal payPrice;

    @ApiModelProperty(value = "用户ID")
    private Integer userId;

    @ApiModelProperty(value = "用户昵称")
    private String userNickname;

    @ApiModelProperty(value = "是否已支付")
    private Boolean isPaid;

    @ApiModelProperty(value = "支付方式")
    private String payType;

    @ApiModelProperty(value = "订单状态")
    private Integer status;

    @ApiModelProperty(value = "代客下单管理员ID")
    private Integer adminPlacerId;

    @ApiModelProperty(value = "代客下单管理员姓名")
    private String adminPlacerName;

    @ApiModelProperty(value = "管理员备注")
    private String adminRemark;

    @ApiModelProperty(value = "创建时间")
    private String createTime;
}
