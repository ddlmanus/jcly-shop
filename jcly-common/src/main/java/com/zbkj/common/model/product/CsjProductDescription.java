package com.zbkj.common.model.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 采食家商品详情表
 * </p>
 *
 * @author dudl
 * @since 2025-01-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_csj_product_description")
@ApiModel(value = "CsjProductDescription对象", description = "采食家商品详情表")
public class CsjProductDescription implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品详情id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "商品id")
    private Integer productId;

    @ApiModelProperty(value = "商品详情")
    private String description;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
