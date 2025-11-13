package com.zbkj.common.request;

import com.zbkj.common.annotation.StringContains;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 我的优惠券请求对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MyCouponRequest对象", description="我的优惠券请求对象")
public class MyCouponRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "用户ID")
    private Integer uid;

    @ApiModelProperty(value = "类型，usable-可用，unusable-不可用,all-全部", required = true)
    @NotNull(message = "请选择优惠券类型")
    @StringContains(limitValues = {"usable", "unusable", "all"}, message = "请选择正确的优惠券类型")
    private String type;

    @ApiModelProperty(value = "页码")
    private Integer page = 1;

    @ApiModelProperty(value = "每页数量")
    private Integer limit = 10;
}