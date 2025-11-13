package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 商品规格属性添加库存对象
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
@ApiModel(value="ProductAttrValueAddStockRequest对象", description="商品规格属性添加对象")
public class ProductAttrValueAddStockRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "ID", required = true)
    @NotNull(message = "商品规格属性ID不能为空")
    private Integer id;

    @ApiModelProperty(value = "商品规格属性添加库存", required = true)
    @NotNull(message = "添加库存不能为空")
    @Min(value = 0, message = "添加库存不能小于0")
    private Integer stock;

    @ApiModelProperty(value = "并发版本控制,前端不传值")
    private Integer version;
}
