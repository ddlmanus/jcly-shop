package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * 拒绝收货请求对象
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
@ApiModel(value = "RejectReceivingRequest对象", description = "拒绝收货请求对象")
public class RejectReceivingRequest {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "退款订单号", required = true)
    @NotBlank(message = "退款订单号不能为空")
    private String refundOrderNo;

    @ApiModelProperty(value = "拒绝原因", required = true)
    @NotBlank(message = "请填写拒绝收货原因")
    private String reason;
}
