package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 更新配送区域请求参数
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
@ApiModel(value = "CityDeliveryAreaUpdateRequest", description = "更新配送区域请求参数")
public class CityDeliveryAreaUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "区域ID不能为空")
    @ApiModelProperty(value = "区域ID", required = true, example = "1")
    private Integer id;

    @ApiModelProperty(value = "区域名称", example = "朝阳区东部配送区")
    private String areaName;

    @ApiModelProperty(value = "省份", example = "北京市")
    private String province;

    @ApiModelProperty(value = "城市", example = "北京市")
    private String city;

    @ApiModelProperty(value = "区县", example = "朝阳区")
    private String district;

    @ApiModelProperty(value = "地址范围描述", example = "朝阳区东部，包括国贸、CBD等区域")
    private String addressRange;

    @ApiModelProperty(value = "中心地址", example = "北京市朝阳区建国门外大街1号")
    private String centerAddress;

    @ApiModelProperty(value = "中心点经度", example = "116.456")
    private BigDecimal centerLongitude;

    @ApiModelProperty(value = "中心点纬度", example = "39.912")
    private BigDecimal centerLatitude;

    @ApiModelProperty(value = "服务半径（公里）", example = "5.0")
    private BigDecimal serviceRadius;

    @ApiModelProperty(value = "边界坐标（JSON格式）", example = "[{\"lat\":39.912,\"lng\":116.456}]")
    private String boundaryCoordinates;

    @ApiModelProperty(value = "费用规则ID", example = "1")
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

    @ApiModelProperty(value = "状态：0-禁用，1-启用", example = "1")
    private Integer status;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;

    @ApiModelProperty(value = "备注", example = "更新配送区域")
    private String remark;
} 