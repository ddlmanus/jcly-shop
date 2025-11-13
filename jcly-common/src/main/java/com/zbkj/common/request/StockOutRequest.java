package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 库存出库请求对象
 * +----------------------------------------------------------------------
 * | JCLY [ JCLY赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 * +----------------------------------------------------------------------
 * | Author: AI Assistant
 * +----------------------------------------------------------------------
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "StockOutRequest对象", description = "库存出库请求对象")
public class StockOutRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品ID", required = true)
    @NotNull(message = "请选择商品")
    private Integer productId;

    @ApiModelProperty(value = "SKU编码", required = true)
    @NotBlank(message = "SKU编码不能为空")
    private String sku;

    @ApiModelProperty(value = "出库数量", required = true)
    @NotNull(message = "出库数量不能为空")
    @Min(value = 1, message = "出库数量不能小于1")
    private Integer outQuantity;

    @ApiModelProperty(value = "出库类型：1=销售出库，2=损耗出库，3=调拨出库，4=其他出库", required = true)
    @NotNull(message = "请选择出库类型")
    @Range(min = 1, max = 4, message = "出库类型必须在1-4之间")
    private Integer outType;

    @ApiModelProperty(value = "关联订单号(销售出库时可填)")
    @Length(max = 32, message = "订单号不能超过32个字符")
    private String orderNo;

    @ApiModelProperty(value = "出库备注")
    @Length(max = 500, message = "出库备注不能超过500个字符")
    private String remark;
} 