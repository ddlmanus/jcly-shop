package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 同城配送订单列表查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel(value = "CityDeliveryOrderListRequest", description = "同城配送订单列表查询参数")
public class CityDeliveryOrderListRequest extends PageParamRequest {

    @ApiModelProperty(value = "配送订单号")
    private String deliveryOrderNo;

    @ApiModelProperty(value = "原订单号")
    private String orderNo;

    @ApiModelProperty(value = "配送状态：0-待接单，1-已接单，2-取件中，3-配送中，4-已送达，5-配送失败，9-已取消")
    private Integer deliveryStatus;

    @ApiModelProperty(value = "配送类型：1-即时配送，2-预约配送")
    private Integer deliveryType;

    @ApiModelProperty(value = "配送员ID")
    private Integer driverId;

    @ApiModelProperty(value = "配送员姓名")
    private String driverName;

    @ApiModelProperty(value = "配送员手机号")
    private String driverPhone;

    @ApiModelProperty(value = "用户ID")
    private Integer userId;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "配送区域ID")
    private Integer areaId;

    @ApiModelProperty(value = "开始日期，格式：yyyy-MM-dd")
    private String startDate;

    @ApiModelProperty(value = "结束日期，格式：yyyy-MM-dd")
    private String endDate;

    @ApiModelProperty(value = "创建时间开始")
    private String createTimeStart;

    @ApiModelProperty(value = "创建时间结束")
    private String createTimeEnd;

    @ApiModelProperty(value = "取件地址关键词")
    private String pickupAddress;

    @ApiModelProperty(value = "收货地址关键词")
    private String deliveryAddress;

    @ApiModelProperty(value = "紧急程度：1-普通，2-加急，3-特急")
    private Integer urgencyLevel;

    @ApiModelProperty(value = "是否异常订单：0-正常，1-异常")
    private Integer isException;

    @ApiModelProperty(value = "最小配送费用")
    private String minFee;

    @ApiModelProperty(value = "最大配送费用")
    private String maxFee;

    @ApiModelProperty(value = "排序字段")
    private String sortField;

    @ApiModelProperty(value = "排序方向：asc-升序，desc-降序")
    private String sortOrder;
} 