package com.zbkj.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 挑选商品Vo对象
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
@ApiModel(value="ChooseProductVo", description="挑选商品Vo对象")
public class ChooseProductVo implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "商品id")
    private Integer id;

    @ApiModelProperty(value = "商品图片")
    private String image;

    @ApiModelProperty(value = "商品名称")
    private String name;

    @ApiModelProperty(value = "是否回收站")
    private Boolean isRecycle;

    @ApiModelProperty(value = "状态（0：未上架，1：上架）")
    private Boolean isShow;

    @ApiModelProperty(value = "是否删除")
    private Boolean isDel;
}
