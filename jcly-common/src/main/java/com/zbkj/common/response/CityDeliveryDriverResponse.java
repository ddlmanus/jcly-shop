package com.zbkj.common.response;

import com.zbkj.common.model.order.CityDeliveryDriver;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 同城配送员响应对象
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
public class CityDeliveryDriverResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String driverCode;
    private String name;
    private String phone;
    private String idCard;
    private Integer gender;
    private String genderText;
    private Integer age;
    private String address;
    private String avatar;
    private String vehicleNumber;
    private Integer vehicleType;
    private String vehicleTypeText;
    private String emergencyContact;
    private String emergencyPhone;
    private Date hireDate;
    private String workArea;
    private String serviceArea;
    private String bankCard;
    private String bankName;
    private BigDecimal baseSalary;
    private BigDecimal commissionRate;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String currentAddress;
    private BigDecimal serviceRadius;
    private Integer status;
    private String statusText;
    private Integer availableStatus;
    private String availableStatusText;
    private Integer currentOrders;
    private Integer maxOrders;
    private Integer monthlyDeliveries;
    private Integer totalDeliveries;
    private Integer completedOrders;
    private Integer completedDeliveries;
    private BigDecimal monthlyIncome;
    private BigDecimal totalIncome;
    private Integer violationCount;
    private BigDecimal rating;
    private Date lastOnlineTime;
    private Date locationUpdateTime;
    private Integer certificationStatus;
    private String certificationStatusText;
    private String certificationRemark;
    private String certificationFailReason;
    private BigDecimal maxDeliveryDistance;
    private String workStartTime;
    private String workEndTime;
    private Integer supportNightDelivery;
    private String supportNightDeliveryText;
    private String deviceImei;
    private String appVersion;
    private String remark;
    private Date createTime;
    private Date updateTime;

    // 扩展字段
    private BigDecimal completionRate;
    private BigDecimal averageRating;
    private Integer todayOrders;
    private Integer todayCompleted;
    private BigDecimal todayIncome;
    private Integer onlineHours;
    private Integer exceptionCount;
    private String distanceFromOrder;
    private BigDecimal orderMatchScore;
    private String workAreaText;
    private String certificationDocuments;
    private Date certificationDate;
    private String vehicleInfo;
    private String serviceLevel;
    private Boolean isOnline;
    private Boolean canTakeOrder;
    private Integer orderCapacity;
    private String currentLocation;
    private BigDecimal workHours;
    private BigDecimal efficiency;

    /**
     * 从实体对象转换为响应对象
     */
    public static CityDeliveryDriverResponse fromEntity(CityDeliveryDriver driver) {
        CityDeliveryDriverResponse response = new CityDeliveryDriverResponse();
        response.setId(driver.getId());
        response.setName(driver.getName());
        response.setPhone(driver.getPhone());
        response.setIdCard(driver.getIdCard());
        response.setGender(driver.getGender());
        response.setAge(driver.getAge());
        response.setAddress(driver.getAddress());
        response.setAvatar(driver.getAvatar());
        response.setVehicleNumber(driver.getVehicleNumber());
        response.setVehicleType(driver.getVehicleType());
        response.setEmergencyContact(driver.getEmergencyContact());
        response.setEmergencyPhone(driver.getEmergencyPhone());
        response.setHireDate(driver.getHireDate());
        response.setWorkArea(driver.getWorkArea());
        response.setServiceArea(driver.getServiceArea());
        response.setBankCard(driver.getBankCard());
        response.setBankName(driver.getBankName());
        response.setBaseSalary(driver.getBaseSalary());
        response.setCommissionRate(driver.getCommissionRate());
        response.setLongitude(driver.getLongitude());
        response.setLatitude(driver.getLatitude());
        response.setCurrentAddress(driver.getCurrentAddress());
        response.setServiceRadius(driver.getServiceRadius());
        response.setStatus(driver.getStatus());
        response.setAvailableStatus(driver.getAvailableStatus());
        response.setCurrentOrders(driver.getCurrentOrders());
        response.setMaxOrders(driver.getMaxOrders());
        response.setMonthlyDeliveries(driver.getMonthlyDeliveries());
        response.setTotalDeliveries(driver.getTotalDeliveries());
        response.setCompletedOrders(driver.getCompletedOrders());
        response.setCompletedDeliveries(driver.getCompletedDeliveries());
        response.setMonthlyIncome(driver.getMonthlyIncome());
        response.setTotalIncome(driver.getTotalIncome());
        response.setViolationCount(driver.getViolationCount());
        response.setRating(driver.getRating());
        response.setLastOnlineTime(driver.getLastOnlineTime());
        response.setLocationUpdateTime(driver.getLocationUpdateTime());
        response.setCertificationStatus(driver.getCertificationStatus());
        response.setCertificationRemark(driver.getCertificationRemark());
        response.setCertificationFailReason(driver.getCertificationFailReason());
        response.setMaxDeliveryDistance(driver.getMaxDeliveryDistance());
        response.setWorkStartTime(driver.getWorkStartTime());
        response.setWorkEndTime(driver.getWorkEndTime());
        response.setSupportNightDelivery(driver.getSupportNightDelivery());
        response.setDeviceImei(driver.getDeviceImei());
        response.setAppVersion(driver.getAppVersion());
        response.setRemark(driver.getRemark());
        response.setCreateTime(driver.getCreateTime());
        response.setUpdateTime(driver.getUpdateTime());
        
        // 设置文本字段
        response.setGenderText(getGenderText(driver.getGender()));
        response.setVehicleTypeText(getVehicleTypeText(driver.getVehicleType()));
        response.setStatusText(getStatusText(driver.getStatus()));
        response.setAvailableStatusText(getAvailableStatusText(driver.getAvailableStatus()));
        response.setCertificationStatusText(getCertificationStatusText(driver.getCertificationStatus()));
        response.setSupportNightDeliveryText(getSupportNightDeliveryText(driver.getSupportNightDelivery()));
        
        // 设置扩展字段
        response.setIsOnline(driver.getStatus() != null && driver.getStatus() == 1);
        response.setCanTakeOrder(driver.getAvailableStatus() != null && driver.getAvailableStatus() == 1 && 
                                (driver.getCurrentOrders() == null || driver.getCurrentOrders() < (driver.getMaxOrders() == null ? 3 : driver.getMaxOrders())));
        response.setOrderCapacity(driver.getMaxOrders() == null ? 3 : driver.getMaxOrders());
        
        return response;
    }

    /**
     * 获取性别文本
     */
    private static String getGenderText(Integer gender) {
        if (gender == null) return "未知";
        switch (gender) {
            case 1: return "男";
            case 2: return "女";
            default: return "未知";
        }
    }

    /**
     * 获取车辆类型文本
     */
    private static String getVehicleTypeText(Integer vehicleType) {
        if (vehicleType == null) return "未知";
        switch (vehicleType) {
            case 1: return "电动车";
            case 2: return "摩托车";
            case 3: return "汽车";
            default: return "未知";
        }
    }

    /**
     * 获取状态文本
     */
    private static String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "离线";
            case 1: return "在线";
            case 2: return "忙碌";
            case 3: return "停用";
            default: return "未知";
        }
    }

    /**
     * 获取可用状态文本
     */
    private static String getAvailableStatusText(Integer availableStatus) {
        if (availableStatus == null) return "未知";
        switch (availableStatus) {
            case 0: return "不可用";
            case 1: return "可接单";
            default: return "未知";
        }
    }

    /**
     * 获取认证状态文本
     */
    private static String getCertificationStatusText(Integer certificationStatus) {
        if (certificationStatus == null) return "未知";
        switch (certificationStatus) {
            case 0: return "未认证";
            case 1: return "认证中";
            case 2: return "认证通过";
            case 3: return "认证失败";
            default: return "未知";
        }
    }

    /**
     * 获取夜间配送支持文本
     */
    private static String getSupportNightDeliveryText(Integer supportNightDelivery) {
        if (supportNightDelivery == null) return "不支持";
        switch (supportNightDelivery) {
            case 0: return "不支持";
            case 1: return "支持";
            default: return "不支持";
        }
    }
} 