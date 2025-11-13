package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 我的推广人团队数量响应对象
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
@ApiModel(value = "UserSpreadPeopleTeamNumResponse对象", description = "我的推广人团队数量响应对象")
public class UserSpreadPeopleTeamNumResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "一级推广人人数")
    private Integer firstSpreadNum;

    @ApiModelProperty(value = "二级推广人人数")
    private Integer secondSpreadNum;

    @ApiModelProperty(value = "推广人总人数")
    private Integer count;
}
