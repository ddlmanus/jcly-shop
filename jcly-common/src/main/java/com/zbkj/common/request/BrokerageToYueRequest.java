package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.DecimalMin;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 佣金转余额请求对象
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
@ApiModel(value = "BrokerageToYueRequest对象", description = "佣金转余额请求对象")
public class BrokerageToYueRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "转入余额金额，非全部转入时必传")
    @DecimalMin(value = "0.01", message = "转入余额金额，必须大与等于0.01元")
    private BigDecimal price;
}
