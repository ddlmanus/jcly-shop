package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 更新配送员请求参数
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
@ApiModel(value = "CityDeliveryDriverUpdateRequest", description = "更新配送员请求参数")
public class CityDeliveryDriverUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "配送员ID不能为空")
    @ApiModelProperty(value = "配送员ID", required = true, example = "1")
    private Integer id;

    @ApiModelProperty(value = "配送员姓名", example = "张三")
    private String name;

    @ApiModelProperty(value = "手机号", example = "13800138000")
    private String phone;

    @ApiModelProperty(value = "身份证号", example = "350102199001011234")
    private String idCard;

    @ApiModelProperty(value = "性别：1-男，2-女", example = "1")
    private Integer gender;

    @ApiModelProperty(value = "年龄", example = "28")
    private Integer age;

    @ApiModelProperty(value = "住址", example = "北京市朝阳区某某街道")
    private String address;

    @ApiModelProperty(value = "头像URL", example = "http://example.com/avatar.jpg")
    private String avatar;

    @ApiModelProperty(value = "车辆号牌", example = "京A12345")
    private String vehicleNumber;

    @ApiModelProperty(value = "车辆类型：1-摩托车，2-电动车，3-自行车，4-汽车", example = "2")
    private Integer vehicleType;

    @ApiModelProperty(value = "紧急联系人", example = "李四")
    private String emergencyContact;

    @ApiModelProperty(value = "紧急联系人电话", example = "13900139000")
    private String emergencyPhone;

    @ApiModelProperty(value = "入职日期", example = "2024-01-01")
    private Date hireDate;

    @ApiModelProperty(value = "工作区域", example = "朝阳区")
    private String workArea;

    @ApiModelProperty(value = "服务区域", example = "朝阳区,海淀区")
    private String serviceArea;

    @ApiModelProperty(value = "银行卡号", example = "6222000000000000")
    private String bankCard;

    @ApiModelProperty(value = "银行名称", example = "中国银行")
    private String bankName;

    @ApiModelProperty(value = "基础工资", example = "3000.00")
    private BigDecimal baseSalary;

    @ApiModelProperty(value = "配送佣金比例(%)", example = "80.00")
    private BigDecimal commissionRate;

    @ApiModelProperty(value = "服务半径（公里）", example = "10.00")
    private BigDecimal serviceRadius;

    @ApiModelProperty(value = "最大配送距离（公里）", example = "20.00")
    private BigDecimal maxDeliveryDistance;

    @ApiModelProperty(value = "工作时间开始", example = "08:00")
    private String workStartTime;

    @ApiModelProperty(value = "工作时间结束", example = "22:00")
    private String workEndTime;

    @ApiModelProperty(value = "是否支持夜间配送：0-不支持，1-支持", example = "1")
    private Integer supportNightDelivery;

    @ApiModelProperty(value = "状态：0-离线，1-在线，2-忙碌，3-停用", example = "1")
    private Integer status;

    @ApiModelProperty(value = "可用状态：0-不可用，1-可接单", example = "1")
    private Integer availableStatus;

    @ApiModelProperty(value = "最大订单数", example = "5")
    private Integer maxOrders;

    @ApiModelProperty(value = "认证状态：0-未认证，1-认证中，2-认证通过，3-认证失败", example = "2")
    private Integer certificationStatus;

    @ApiModelProperty(value = "认证备注", example = "资料齐全")
    private String certificationRemark;

    @ApiModelProperty(value = "认证失败原因", example = "身份证照片不清晰")
    private String certificationFailReason;

    @ApiModelProperty(value = "设备IMEI号", example = "123456789012345")
    private String deviceImei;

    @ApiModelProperty(value = "APP版本", example = "1.0.0")
    private String appVersion;

    @ApiModelProperty(value = "备注", example = "更新配送员信息")
    private String remark;
} 