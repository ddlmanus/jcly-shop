package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品品牌响应对象
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
@ApiModel(value="ProductBrandResponse对象", description="商品品牌响应对象")
public class ProductBrandResponse implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "品牌id")
    private Integer id;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "icon")
    private String icon;

    @ApiModelProperty(value = "显示状态")
    private Boolean isShow;
    @ApiModelProperty(value = "排序")
    private Integer sort;
    @ApiModelProperty(value = "是否删除")
    private Boolean isDel;
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
