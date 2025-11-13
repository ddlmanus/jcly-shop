package com.zbkj.common.response;

import com.zbkj.common.model.coupon.Coupon;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 新人礼响应对象
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
@ApiModel(value = "NewPeoplePresentResponse对象", description = "新人礼响应对象")
public class NewPeoplePresentResponse implements Serializable {

    private static final long serialVersionUID = -4585094537501770138L;

    @ApiModelProperty(value = "新人礼开关")
    private Boolean newPeopleSwitch;

    @ApiModelProperty(value = "新人礼优惠券列表")
    private List<Coupon> couponList;

}
