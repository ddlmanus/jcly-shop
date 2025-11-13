package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 采食家商品搜索对象
 *
 * @author dudl
 * @version 1.0.0
 * @Date 2025-01-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "CsjProductSearchRequest对象", description = "采食家商品搜索对象")
public class CsjProductSearchRequest extends PageParamRequest {

    private static final long serialVersionUID = -4572464885363357010L;

    @ApiModelProperty(value = "商品名称")
    private String name;

    @ApiModelProperty(value = "商品分类")
    private String categoryName;

    @ApiModelProperty(value = "商品品牌")
    private String brandName;

    @ApiModelProperty(value = "上架状态（0：未上架，1：上架）")
    private Boolean isShow;
}
