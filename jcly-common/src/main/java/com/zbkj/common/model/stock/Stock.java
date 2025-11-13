package com.zbkj.common.model.stock;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 商品库存表
 * </p>
 *
 * @author AI Assistant
 * @since 2024-12-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_stock")
@ApiModel(value = "Stock对象", description = "商品库存表")
public class Stock implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "库存ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "商品ID")
    private Integer productId;

    @ApiModelProperty(value = "SKU编码")
    private String sku;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "当前库存")
    private Integer stock;

    @ApiModelProperty(value = "冻结库存")
    private Integer freezeStock;

    @ApiModelProperty(value = "成本价")
    private BigDecimal costPrice;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "是否删除")
    private Boolean isDel;
} 