package com.zbkj.common.model.merchant;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 门店配送范围实体类
 * 
 * @author 系统
 * @date 2025-01-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("eb_merchant_store_delivery_area")
@ApiModel(value = "MerchantStoreDeliveryArea", description = "门店配送范围")
public class MerchantStoreDeliveryArea implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "门店ID", required = true)
    @NotNull(message = "门店ID不能为空")
    @TableField("store_id")
    private Integer storeId;

    @ApiModelProperty(value = "配送区域名称", required = true)
    @NotBlank(message = "配送区域名称不能为空")
    @TableField("area_name")
    private String areaName;

    @ApiModelProperty(value = "区域类型：CIRCLE-圆形，POLYGON-多边形")
    @TableField("area_type")
    private String areaType;

    @ApiModelProperty(value = "中心点纬度（圆形区域）")
    @TableField("center_latitude")
    private BigDecimal centerLatitude;

    @ApiModelProperty(value = "中心点经度（圆形区域）")
    @TableField("center_longitude")
    private BigDecimal centerLongitude;

    @ApiModelProperty(value = "配送半径（千米）")
    @TableField("radius")
    private BigDecimal radius;

    @ApiModelProperty(value = "多边形坐标点，JSON格式")
    @TableField("polygon_points")
    private String polygonPoints;

    @ApiModelProperty(value = "配送费用")
    @TableField("delivery_fee")
    private BigDecimal deliveryFee;

    @ApiModelProperty(value = "免配送费金额")
    @TableField("free_delivery_amount")
    private BigDecimal freeDeliveryAmount;

    @ApiModelProperty(value = "是否启用：0-禁用，1-启用")
    @TableField("is_enabled")
    private Integer isEnabled;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // 非数据库字段
    @ApiModelProperty(value = "门店名称")
    @TableField(exist = false)
    private String storeName;

    @ApiModelProperty(value = "区域类型名称")
    @TableField(exist = false)
    private String areaTypeName;

    @ApiModelProperty(value = "状态名称")
    @TableField(exist = false)
    private String statusName;

    /**
     * 获取区域类型名称
     */
    public String getAreaTypeName() {
        if (areaType == null) {
            return "";
        }
        switch (areaType) {
            case "CIRCLE": return "圆形区域";
            case "POLYGON": return "多边形区域";
            default: return "";
        }
    }

    /**
     * 获取状态名称
     */
    public String getStatusName() {
        if (isEnabled == null) {
            return "";
        }
        return isEnabled == 1 ? "启用" : "禁用";
    }

    /**
     * 检查是否需要配送费
     */
    public boolean needDeliveryFee(BigDecimal orderAmount) {
        if (deliveryFee == null || deliveryFee.compareTo(BigDecimal.ZERO) == 0) {
            return false; // 配送费为0，不需要配送费
        }
        if (freeDeliveryAmount != null && orderAmount != null) {
            return orderAmount.compareTo(freeDeliveryAmount) < 0; // 订单金额小于免配送费金额
        }
        return true; // 需要配送费
    }

    /**
     * 计算实际配送费
     */
    public BigDecimal calculateDeliveryFee(BigDecimal orderAmount) {
        if (!needDeliveryFee(orderAmount)) {
            return BigDecimal.ZERO;
        }
        return deliveryFee != null ? deliveryFee : BigDecimal.ZERO;
    }
} 