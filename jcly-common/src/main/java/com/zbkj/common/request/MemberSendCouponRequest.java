package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 会员发送优惠券请求对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MemberSendCouponRequest对象", description="会员发送优惠券请求对象")
public class MemberSendCouponRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "优惠券ID列表", required = true)
    @NotEmpty(message = "优惠券ID列表不能为空")
    private List<Integer> couponIds;

    @ApiModelProperty(value = "会员ID列表", required = true)
    @NotEmpty(message = "会员ID列表不能为空")
    private List<Integer> memberIds;
}