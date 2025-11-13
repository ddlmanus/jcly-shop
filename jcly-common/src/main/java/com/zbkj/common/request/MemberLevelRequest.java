package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员等级请求对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MemberLevelRequest对象", description="会员等级请求对象")
public class MemberLevelRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "等级ID")
    private Integer id;

    @ApiModelProperty(value = "等级名称", required = true)
    @NotBlank(message = "等级名称不能为空")
    @Size(min = 2, max = 20, message = "等级名称长度在2-20个字符之间")
    private String levelName;

    @ApiModelProperty(value = "等级图标", required = true)
    @NotBlank(message = "等级图标不能为空")
    private String icon;

    @ApiModelProperty(value = "所需积分", required = true)
    @NotNull(message = "所需积分不能为空")
    @Min(value = 0, message = "所需积分必须大于等于0")
    private Integer minIntegral;

    @ApiModelProperty(value = "折扣率(0-100)", required = true)
    @NotNull(message = "折扣率不能为空")
    @DecimalMin(value = "0", message = "折扣率必须大于等于0")
    @DecimalMax(value = "100", message = "折扣率必须小于等于100")
    private BigDecimal discount;

    @ApiModelProperty(value = "等级说明")
    private String description;

    @ApiModelProperty(value = "状态(0-禁用，1-启用)", required = true)
    @NotNull(message = "状态不能为空")
    private Integer status;
    @ApiModelProperty(value = "商户ID")
    private Integer merId;
}