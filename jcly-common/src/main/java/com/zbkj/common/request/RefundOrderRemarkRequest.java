package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 退款订单备注请求对象
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
@ApiModel(value = "RefundOrderRemarkRequest对象", description = "退款订单备注请求对象")
public class RefundOrderRemarkRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "退款订单号")
    @NotEmpty(message = "退款订单号不能为空")
    private String refundOrderNo;

    @ApiModelProperty(value = "备注内容")
    @NotEmpty(message = "备注内容不能为空")
    private String remark;

}
