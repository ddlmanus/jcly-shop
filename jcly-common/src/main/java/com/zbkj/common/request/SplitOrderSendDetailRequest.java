package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 拆单发货详情请求对象
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
@ApiModel(value = "SplitOrderSendDetailRequest对象", description = "拆单发货详情请求对象")
public class SplitOrderSendDetailRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单详情id")
    @NotNull(message = "订单详情id不能为空")
    private Integer orderDetailId;

    @ApiModelProperty(value = "发货数量")
    @NotNull(message = "订单详情发货数量不能为空")
    private Integer num;
}
