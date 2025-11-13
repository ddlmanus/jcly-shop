package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 我的积分响应对象
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
@ApiModel(value = "UserMyIntegralResponse对象", description = "我的积分响应对象")
public class UserMyIntegralResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "当前积分")
    private Integer integral;

    @ApiModelProperty(value = "累计积分")
    private Integer settledIntegral;

    @ApiModelProperty(value = "累计消费积分")
    private Integer useIntegral;

    @ApiModelProperty(value = "冻结中积分")
    private Integer freezeIntegral;
}
