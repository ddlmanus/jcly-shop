package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 会员积分变更请求对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MemberIntegralRequest对象", description="会员积分变更请求对象")
public class MemberIntegralRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "会员ID", required = true)
    @NotNull(message = "会员ID不能为空")
    private List<Integer> memberIds;

    @ApiModelProperty(value = "类型：1增加，2减少", required = true)
    @NotNull(message = "类型不能为空")
    private Integer type;

    @ApiModelProperty(value = "积分数量", required = true)
    @NotNull(message = "积分数量不能为空")
    @Min(value = 1, message = "积分数量必须大于0")
    private Integer integral;

    @ApiModelProperty(value = "标题", required = true)
    @NotBlank(message = "标题不能为空")
    private String title;

    @ApiModelProperty(value = "备注")
    private String remark;
}