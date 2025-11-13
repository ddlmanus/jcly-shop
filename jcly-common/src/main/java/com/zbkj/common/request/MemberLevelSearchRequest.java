package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 会员等级搜索请求对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MemberLevelSearchRequest对象", description="会员等级搜索请求对象")
public class MemberLevelSearchRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "页码")
    private int page = 1;

    @ApiModelProperty(value = "每页数量")
    private int limit = 10;

    @ApiModelProperty(value = "等级名称")
    private String levelName;
    @ApiModelProperty(value = "商户ID")
    private Integer merId;
    @ApiModelProperty(value = "状态   ")
    private Boolean status;
}