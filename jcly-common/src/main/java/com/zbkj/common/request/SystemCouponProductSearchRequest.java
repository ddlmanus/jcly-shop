package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 优惠券商品搜索请求对象(系统优惠券)
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
@ApiModel(value = "SystemCouponProductSearchRequest对象", description = "优惠券商品搜索请求对象(系统优惠券)")
public class SystemCouponProductSearchRequest extends PageParamRequest implements Serializable {

    private static final long serialVersionUID = 3481659942630712958L;

    @ApiModelProperty(value = "优惠券id")
    @NotNull(message = "优惠券id不能为空")
    private Integer couponId;

    @ApiModelProperty(value = "搜索关键字")
    private String keyword;
}
