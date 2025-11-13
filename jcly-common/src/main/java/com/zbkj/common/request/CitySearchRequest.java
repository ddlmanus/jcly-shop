package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 城市区域搜索请求对象
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
@ApiModel(value = "CitySearchRequest对象", description = "城市区域搜索请求对象")
public class CitySearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "区域类型，0-国家、1-省、2-市、3-区、4-街道", required = true)
    private Integer regionType;

    @ApiModelProperty(value = "父区域id,(获取所有省，传值1)", required = true)
    private Integer parentId;
    @ApiModelProperty(value = "区域名称", required = true)
    private String regionName;
}
