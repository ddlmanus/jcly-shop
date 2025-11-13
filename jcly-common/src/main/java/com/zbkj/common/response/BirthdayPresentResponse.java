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
 * 生日有礼响应对象
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
@ApiModel(value = "BirthdayPresentResponse对象", description = "生日有礼响应对象")
public class BirthdayPresentResponse implements Serializable {

    private static final long serialVersionUID = -4585094537501770138L;

    @ApiModelProperty(value = "生日有礼开关")
    private Boolean birthdaySwitch;

    @ApiModelProperty(value = "生日有礼优惠券列表")
    private List<Coupon> couponList;

}
