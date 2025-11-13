package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户优惠券响应对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="StoreCouponUserResponse对象", description="用户优惠券响应对象")
public class StoreCouponUserResponse implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "优惠券ID")
    private Integer id;

    @ApiModelProperty(value = "优惠券名称")
    private String name;

    @ApiModelProperty(value = "优惠金额")
    private BigDecimal money;

    @ApiModelProperty(value = "最低消费")
    private BigDecimal minPrice;

    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    private Date endTime;

    @ApiModelProperty(value = "使用时间")
    private Date useTime;

    @ApiModelProperty(value = "状态（0：未使用，1：已使用, 2:已失效）")
    private Integer status;

    @ApiModelProperty(value = "是否已过期")
    private Boolean isExpired;

    @ApiModelProperty(value = "是否满足使用条件")
    private Boolean isAvailable;
}