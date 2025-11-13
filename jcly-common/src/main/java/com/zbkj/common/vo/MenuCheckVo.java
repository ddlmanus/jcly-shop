package com.zbkj.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 菜单待选中Vo对象
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
@ApiModel(value="MenuCheckVo对象", description="菜单待选中Vo对象")
public class MenuCheckVo implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "菜单ID")
    private Integer id;

    @ApiModelProperty(value = "父级ID")
    private Integer pid;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "icon")
    private String icon;

    @ApiModelProperty(value = "是否选中")
    private Boolean checked;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "子对象列表")
    private List<MenuCheckVo> childList;
}
