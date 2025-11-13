package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 库存入库请求对象
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
@ApiModel(value = "StockInRequest对象", description = "库存入库请求对象")
public class StockInRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品ID", required = true)
    @NotNull(message = "请选择商品")
    private Integer productId;

    @ApiModelProperty(value = "SKU编码", required = true)
    @NotBlank(message = "SKU编码不能为空")
    private String sku;

    @ApiModelProperty(value = "入库数量", required = true)
    @NotNull(message = "入库数量不能为空")
    @Min(value = 1, message = "入库数量不能小于1")
    private Integer inQuantity;

    @ApiModelProperty(value = "入库成本价", required = true)
    @NotNull(message = "入库成本价不能为空")
    @Min(value = 0, message = "入库成本价不能小于0")
    private BigDecimal costPrice;

    @ApiModelProperty(value = "供应商")
    @Length(max = 255, message = "供应商名称不能超过255个字符")
    private String supplier;

    @ApiModelProperty(value = "入库备注")
    @Length(max = 500, message = "入库备注不能超过500个字符")
    private String remark;
} 