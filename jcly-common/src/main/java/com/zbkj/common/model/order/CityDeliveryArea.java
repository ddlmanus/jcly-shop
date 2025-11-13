package com.zbkj.common.model.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 同城配送区域表
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
@TableName("eb_city_delivery_area")
@ApiModel(value = "CityDeliveryArea", description = "同城配送区域表")
public class CityDeliveryArea implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "配送区域ID", required = true)
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "区域名称", required = true, example = "朝阳区东部配送区")
    @NotBlank(message = "区域名称不能为空")
    private String areaName;

    @ApiModelProperty(value = "省份", required = true, example = "北京市")
    @NotBlank(message = "省份不能为空")
    private String province;

    @ApiModelProperty(value = "城市", required = true, example = "北京市")
    @NotBlank(message = "城市不能为空")
    private String city;

    @ApiModelProperty(value = "区县", example = "朝阳区")
    private String district;

    @ApiModelProperty(value = "地址范围描述", example = "朝阳区东部，包括国贸、CBD等区域")
    private String addressRange;

    @ApiModelProperty(value = "中心地址", example = "北京市朝阳区建国门外大街1号")
    private String centerAddress;

    @ApiModelProperty(value = "中心点经度", required = true, example = "116.456")
    @NotNull(message = "中心点经度不能为空")
    private BigDecimal centerLongitude;

    @ApiModelProperty(value = "中心点纬度", required = true, example = "39.912")
    @NotNull(message = "中心点纬度不能为空")
    private BigDecimal centerLatitude;

    @ApiModelProperty(value = "服务半径（公里）", required = true, example = "5.0")
    @NotNull(message = "服务半径不能为空")
    private BigDecimal serviceRadius;

    @ApiModelProperty(value = "边界坐标（JSON格式）", example = "[{\"lat\":39.912,\"lng\":116.456}]")
    private String boundaryCoordinates;

    @ApiModelProperty(value = "费用规则ID")
    private Integer feeRuleId;

    @ApiModelProperty(value = "是否支持即时配送：0-否，1-是", example = "1")
    private Integer supportInstantDelivery;

    @ApiModelProperty(value = "是否支持预约配送：0-否，1-是", example = "1")
    private Integer supportScheduledDelivery;

    @ApiModelProperty(value = "最大配送距离（公里）", example = "20.0")
    private BigDecimal maxDeliveryDistance;

    @ApiModelProperty(value = "服务开始时间", example = "08:00")
    private String serviceStartTime;

    @ApiModelProperty(value = "服务结束时间", example = "22:00")
    private String serviceEndTime;

    @ApiModelProperty(value = "配送时段（JSON格式）", example = "[{\"start\":\"08:00\",\"end\":\"12:00\"},{\"start\":\"14:00\",\"end\":\"18:00\"}]")
    private String deliveryTimeSlots;

    @ApiModelProperty(value = "状态：0-禁用，1-启用", required = true, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

    @ApiModelProperty(value = "更新时间", required = true)
    private Date updateTime;

    @ApiModelProperty(value = "是否删除：0-正常，1-删除", example = "0")
    private Integer isDel;
} 