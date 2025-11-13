package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 会员等级状态更新请求对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MemberLevelStatusRequest对象", description="会员等级状态更新请求对象")
public class MemberLevelStatusRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "等级ID", required = true)
    @NotNull(message = "等级ID不能为空")
    private Integer id;

    @ApiModelProperty(value = "状态(0-禁用，1-启用)", required = true)
    @NotNull(message = "状态不能为空")
    private Integer status;
}