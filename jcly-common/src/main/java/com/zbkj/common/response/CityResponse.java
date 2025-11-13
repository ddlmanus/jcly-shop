package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 城市区域响应对象
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
@ApiModel(value = "CityResponse对象", description = "城市区域响应对象")
public class CityResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "区域id")
    private Integer regionId;

    @ApiModelProperty(value = "父区域id")
    private Integer parentId;

    @ApiModelProperty(value = "区域名称")
    private String regionName;

    @ApiModelProperty(value = "区域类型，0-国家、1-省、2-市、3-区、4-街道")
    private Integer regionType;

    @ApiModelProperty(value = "是否呦下级数据")
    private Boolean isChild = true;
}
