package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 同城配送员请求参数
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "CityDeliveryDriverRequest", description = "同城配送员请求参数")
public class CityDeliveryDriverRequest {

    @ApiModelProperty(value = "配送员ID（编辑时必填）")
    private Integer id;

    @ApiModelProperty(value = "配送员姓名", required = true)
    @NotBlank(message = "配送员姓名不能为空")
    private String name;

    @ApiModelProperty(value = "手机号", required = true)
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @ApiModelProperty(value = "身份证号")
    private String idCard;

    @ApiModelProperty(value = "性别：1-男，2-女")
    private Integer gender;

    @ApiModelProperty(value = "年龄")
    private Integer age;

    @ApiModelProperty(value = "住址")
    private String address;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @ApiModelProperty(value = "车牌号")
    private String vehicleNumber;

    @ApiModelProperty(value = "车辆类型：1-电动车，2-摩托车，3-汽车")
    private Integer vehicleType;

    @ApiModelProperty(value = "紧急联系人")
    private String emergencyContact;

    @ApiModelProperty(value = "紧急联系人电话")
    private String emergencyPhone;

    @ApiModelProperty(value = "工作地区")
    private String workArea;

    @ApiModelProperty(value = "服务区域")
    private String serviceArea;

    @ApiModelProperty(value = "银行卡号")
    private String bankCard;

    @ApiModelProperty(value = "银行名称")
    private String bankName;

    @ApiModelProperty(value = "基础工资")
    private BigDecimal baseSalary;

    @ApiModelProperty(value = "配送佣金比例(%)")
    private BigDecimal commissionRate;

    @ApiModelProperty(value = "服务半径（公里）")
    private BigDecimal serviceRadius;

    @ApiModelProperty(value = "最大配送距离（公里）")
    private BigDecimal maxDeliveryDistance;

    @ApiModelProperty(value = "最大订单数")
    private Integer maxOrders;

    @ApiModelProperty(value = "工作时间开始")
    private String workStartTime;

    @ApiModelProperty(value = "工作时间结束")
    private String workEndTime;

    @ApiModelProperty(value = "是否支持夜间配送：0-不支持，1-支持")
    private Integer supportNightDelivery;

    @ApiModelProperty(value = "状态：0-离线，1-在线，2-忙碌，3-停用")
    private Integer status;

    @ApiModelProperty(value = "可用状态：0-不可用，1-可接单")
    private Integer availableStatus;

    @ApiModelProperty(value = "认证状态：0-未认证，1-认证中，2-认证通过，3-认证失败")
    private Integer certificationStatus;

    @ApiModelProperty(value = "设备IMEI号")
    private String deviceImei;

    @ApiModelProperty(value = "APP版本")
    private String appVersion;

    @ApiModelProperty(value = "备注")
    private String remark;
} 