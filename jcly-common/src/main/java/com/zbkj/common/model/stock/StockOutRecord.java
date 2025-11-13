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
 * 库存出库记录表
 * </p>
 *
 * @author AI Assistant
 * @since 2024-12-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_stock_out_record")
@ApiModel(value = "StockOutRecord对象", description = "库存出库记录表")
public class StockOutRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "出库记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "出库单号")
    private String recordNo;

    @ApiModelProperty(value = "商品ID")
    private Integer productId;

    @ApiModelProperty(value = "商品名称")
    private String productName;
    @ApiModelProperty(value = "商品图片")
    private String productImages;

    @ApiModelProperty(value = "SKU编码")
    private String sku;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "原有库存数量")
    private Integer beforeStock;

    @ApiModelProperty(value = "出库数量")
    private Integer outQuantity;

    @ApiModelProperty(value = "现有库存数量")
    private Integer afterStock;

    @ApiModelProperty(value = "出库成本价")
    private BigDecimal costPrice;

    @ApiModelProperty(value = "出库总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "出库类型：1=销售出库，2=损耗出库，3=调拨出库，4=其他出库")
    private Integer outType;

    @ApiModelProperty(value = "出库备注")
    private String remark;

    @ApiModelProperty(value = "操作员ID")
    private Integer operatorId;

    @ApiModelProperty(value = "操作员姓名")
    private String operatorName;

    @ApiModelProperty(value = "关联订单号")
    private String orderNo;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "是否删除")
    private Boolean isDel;
} 