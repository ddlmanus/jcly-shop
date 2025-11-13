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
 * 配送轨迹表
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
@TableName("eb_city_delivery_track")
@ApiModel(value = "CityDeliveryTrack", description = "配送轨迹实体类")
public class CityDeliveryTrack implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "轨迹ID", example = "1")
    private Integer id;

    @NotBlank(message = "配送订单号不能为空")
    @ApiModelProperty(value = "配送订单号", example = "D202512010001", required = true)
    private String deliveryOrderNo;

    @ApiModelProperty(value = "配送员ID", example = "1")
    private Integer driverId;

    @ApiModelProperty(value = "经度", example = "114.305328")
    private BigDecimal longitude;

    @ApiModelProperty(value = "纬度", example = "30.593099")
    private BigDecimal latitude;

    @ApiModelProperty(value = "地址", example = "湖北省武汉市武昌区某某路某某号")
    private String address;

    @NotBlank(message = "状态描述不能为空")
    @ApiModelProperty(value = "状态描述", example = "配送员已取件", required = true)
    private String status;

    @ApiModelProperty(value = "状态码：0-待接单，1-已接单，2-取件中，3-配送中，4-已送达，5-配送失败，9-已取消", example = "2")
    private Integer statusCode;

    @ApiModelProperty(value = "轨迹类型：1-位置轨迹，2-状态变更", example = "1")
    private Integer trackType;

    @ApiModelProperty(value = "距离起点距离（公里）", example = "2.5")
    private BigDecimal distanceFromStart;

    @ApiModelProperty(value = "当前速度（km/h）", example = "25.5")
    private BigDecimal speed;

    @ApiModelProperty(value = "行驶方向", example = "东北")
    private String direction;

    @ApiModelProperty(value = "备注", example = "正在前往取件地点")
    private String remark;

    @NotNull(message = "创建时间不能为空")
    @ApiModelProperty(value = "创建时间", example = "2024-01-01 10:30:00", required = true)
    private Date createTime;
} 