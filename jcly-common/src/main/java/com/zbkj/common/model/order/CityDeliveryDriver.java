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
 * 同城配送员表
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
@TableName("eb_city_delivery_driver")
@ApiModel(value = "CityDeliveryDriver", description = "同城配送员表")
public class CityDeliveryDriver implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "配送员ID", required = true)
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "配送员姓名", required = true, example = "张三")
    private String name;

    @ApiModelProperty(value = "手机号", required = true, example = "13800138000")
    private String phone;

    @ApiModelProperty(value = "身份证号", example = "350102199001011234")
    private String idCard;

    @ApiModelProperty(value = "性别：1-男，2-女", example = "1")
    private Integer gender;

    @ApiModelProperty(value = "年龄", example = "28")
    private Integer age;

    @ApiModelProperty(value = "住址")
    private String address;

    @ApiModelProperty(value = "头像URL")
    private String avatar;

    @ApiModelProperty(value = "车辆号牌", example = "京A12345")
    private String vehicleNumber;

    @ApiModelProperty(value = "车辆类型：1-摩托车，2-电动车，3-自行车，4-汽车", example = "2")
    private Integer vehicleType;

    @ApiModelProperty(value = "紧急联系人", example = "李四")
    private String emergencyContact;

    @ApiModelProperty(value = "紧急联系人电话", example = "13900139000")
    private String emergencyPhone;

    @ApiModelProperty(value = "入职日期")
    private Date hireDate;

    @ApiModelProperty(value = "工作区域", example = "朝阳区")
    private String workArea;

    @ApiModelProperty(value = "服务区域", example = "朝阳区,海淀区")
    private String serviceArea;

    @ApiModelProperty(value = "银行卡号")
    private String bankCard;

    @ApiModelProperty(value = "银行名称", example = "中国建设银行")
    private String bankName;

    @ApiModelProperty(value = "基本工资（元）", example = "3000.00")
    private BigDecimal baseSalary;

    @ApiModelProperty(value = "佣金比例（%）", example = "0.15")
    private BigDecimal commissionRate;

    @ApiModelProperty(value = "注册位置经度")
    private BigDecimal longitude;

    @ApiModelProperty(value = "注册位置纬度")
    private BigDecimal latitude;

    @ApiModelProperty(value = "当前位置经度")
    private BigDecimal currentLongitude;

    @ApiModelProperty(value = "当前位置纬度")
    private BigDecimal currentLatitude;

    @ApiModelProperty(value = "当前地址")
    private String currentAddress;

    @ApiModelProperty(value = "服务半径（公里）", example = "5.0")
    private BigDecimal serviceRadius;

    @ApiModelProperty(value = "状态：0-待审核，1-正常，2-暂停，3-封禁", required = true, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @ApiModelProperty(value = "可用状态：0-离线，1-在线空闲，2-在线忙碌", example = "1")
    private Integer availableStatus;

    @ApiModelProperty(value = "当前订单数", example = "2")
    private Integer currentOrders;

    @ApiModelProperty(value = "最大订单数", example = "5")
    private Integer maxOrders;

    @ApiModelProperty(value = "本月配送数", example = "150")
    private Integer monthlyDeliveries;

    @ApiModelProperty(value = "总配送数", example = "3500")
    private Integer totalDeliveries;

    @ApiModelProperty(value = "已完成订单数", example = "3450")
    private Integer completedOrders;

    @ApiModelProperty(value = "已完成配送数", example = "3450")
    private Integer completedDeliveries;

    @ApiModelProperty(value = "本月收入（元）", example = "5500.00")
    private BigDecimal monthlyIncome;

    @ApiModelProperty(value = "总收入（元）", example = "125000.00")
    private BigDecimal totalIncome;

    @ApiModelProperty(value = "总收入（元）备用字段", example = "125000.00")
    private BigDecimal totalEarnings;

    @ApiModelProperty(value = "违规次数", example = "2")
    private Integer violationCount;

    @ApiModelProperty(value = "评分（1-5分）", example = "4.8")
    private BigDecimal rating;

    @ApiModelProperty(value = "最后在线时间")
    private Date lastOnlineTime;

    @ApiModelProperty(value = "位置更新时间")
    private Date locationUpdateTime;

    @ApiModelProperty(value = "认证状态：0-未认证，1-认证通过，2-认证失败", example = "1")
    private Integer certificationStatus;

    @ApiModelProperty(value = "认证备注")
    private String certificationRemark;

    @ApiModelProperty(value = "认证失败原因")
    private String certificationFailReason;

    @ApiModelProperty(value = "最大配送距离（公里）", example = "20.0")
    private BigDecimal maxDeliveryDistance;

    @ApiModelProperty(value = "工作开始时间", example = "08:00")
    private String workStartTime;

    @ApiModelProperty(value = "工作结束时间", example = "22:00")
    private String workEndTime;

    @ApiModelProperty(value = "是否支持夜间配送：0-否，1-是", example = "1")
    private Integer supportNightDelivery;

    @ApiModelProperty(value = "设备IMEI号")
    private String deviceImei;

    @ApiModelProperty(value = "APP版本号", example = "1.0.0")
    private String appVersion;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建时间", required = true)
    @NotNull(message = "创建时间不能为空")
    private Date createTime;

    @ApiModelProperty(value = "更新时间", required = true)
    @NotNull(message = "更新时间不能为空")
    private Date updateTime;

    @ApiModelProperty(value = "是否删除：0-正常，1-删除", example = "0")
    private Integer isDel;
} 