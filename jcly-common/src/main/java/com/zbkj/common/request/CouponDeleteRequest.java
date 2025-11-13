package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 删除优惠卷请求对象
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
@ApiModel(value = "CouponDeleteRequest对象", description = "删除优惠卷请求对象")
public class CouponDeleteRequest implements Serializable {

    private static final long serialVersionUID = -7053809553211774431L;

    @ApiModelProperty(value = "优惠券ID", required = true)
    @NotNull(message = "请选择优惠券")
    private Integer id;

    @ApiModelProperty(value = "已领取优惠券是否失效，1-失效，0-不失效", required = true)
    @Range(min = 0, max = 1, message = "未知的失效状态")
    private Integer loseEfficacyStatus;

}
