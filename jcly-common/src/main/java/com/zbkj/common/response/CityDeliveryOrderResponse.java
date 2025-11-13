package com.zbkj.common.response;

import com.zbkj.common.model.order.CityDeliveryOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 配送订单响应类
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
@ApiModel(value = "CityDeliveryOrderResponse", description = "配送订单响应类")
public class CityDeliveryOrderResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "配送订单ID", example = "1")
    private Integer id;
    
    @ApiModelProperty(value = "配送订单号", example = "D202512010001")
    private String deliveryOrderNo;
    
    @ApiModelProperty(value = "原订单号", example = "2025120100001")
    private String orderNo;
    
    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merId;
    
    @ApiModelProperty(value = "用户ID", example = "1")
    private Integer uid;
    
    @ApiModelProperty(value = "配送员ID", example = "1")
    private Integer driverId;
    
    @ApiModelProperty(value = "配送员姓名", example = "张三")
    private String driverName;
    
    @ApiModelProperty(value = "配送员手机号", example = "13800138000")
    private String driverPhone;
    
    @ApiModelProperty(value = "取件地址", example = "北京市朝阳区建国门外大街1号")
    private String pickupAddress;
    
    @ApiModelProperty(value = "取件地址经度", example = "116.456")
    private BigDecimal pickupLongitude;
    
    @ApiModelProperty(value = "取件地址纬度", example = "39.912")
    private BigDecimal pickupLatitude;
    
    @ApiModelProperty(value = "取件联系人", example = "李四")
    private String pickupContact;
    
    @ApiModelProperty(value = "取件联系电话", example = "13900139000")
    private String pickupPhone;
    
    @ApiModelProperty(value = "送货地址", example = "北京市朝阳区东三环中路55号")
    private String deliveryAddress;
    
    @ApiModelProperty(value = "送货地址经度", example = "116.468")
    private BigDecimal deliveryLongitude;
    
    @ApiModelProperty(value = "送货地址纬度", example = "39.918")
    private BigDecimal deliveryLatitude;
    
    @ApiModelProperty(value = "收货联系人", example = "王五")
    private String deliveryContact;
    
    @ApiModelProperty(value = "收货联系电话", example = "13700137000")
    private String deliveryPhone;
    
    @ApiModelProperty(value = "配送距离（公里）", example = "5.5")
    private BigDecimal distance;
    
    @ApiModelProperty(value = "配送费用（元）", example = "15.00")
    private BigDecimal deliveryFee;
    
    @ApiModelProperty(value = "实际费用（元）", example = "15.00")
    private BigDecimal actualFee;
    
    @ApiModelProperty(value = "配送员佣金（元）", example = "10.00")
    private BigDecimal driverCommission;
    
    @ApiModelProperty(value = "平台抽佣（元）", example = "3.00")
    private BigDecimal platformCommission;
    
    @ApiModelProperty(value = "配送类型：1-即时配送，2-预约配送", example = "1")
    private Integer deliveryType;
    
    @ApiModelProperty(value = "配送状态：0-待派单，1-已派单，2-已接单，3-取件中，4-配送中，5-已完成，6-已取消", example = "0")
    private Integer deliveryStatus;
    
    @ApiModelProperty(value = "配送状态文本", example = "待派单")
    private String deliveryStatusText;
    
    @ApiModelProperty(value = "配送类型文本", example = "即时配送")
    private String deliveryTypeText;
    
    @ApiModelProperty(value = "预计送达时间", example = "30分钟内")
    private String estimatedTime;
    
    @ApiModelProperty(value = "预计取件时间", example = "2024-01-01 10:00:00")
    private Date estimatedPickupTime;
    
    @ApiModelProperty(value = "预计送达时间", example = "2024-01-01 10:30:00")
    private Date estimatedDeliveryTime;
    
    @ApiModelProperty(value = "实际取件时间", example = "2024-01-01 10:05:00")
    private Date actualPickupTime;
    
    @ApiModelProperty(value = "实际送达时间", example = "2024-01-01 10:25:00")
    private Date actualDeliveryTime;
    
    @ApiModelProperty(value = "配送备注", example = "请按门铃")
    private String deliveryRemark;
    
    @ApiModelProperty(value = "取消原因", example = "用户取消")
    private String cancelReason;
    
    @ApiModelProperty(value = "异常原因", example = "配送员联系不上")
    private String exceptionReason;
    
    @ApiModelProperty(value = "客户评分（1-5分）", example = "5")
    private Integer customerRating;
    
    @ApiModelProperty(value = "客户反馈", example = "配送员服务态度很好")
    private String customerFeedback;
    
    @ApiModelProperty(value = "创建时间", example = "2024-01-01 09:00:00")
    private Date createTime;
    
    @ApiModelProperty(value = "更新时间", example = "2024-01-01 10:25:00")
    private Date updateTime;
    
    @ApiModelProperty(value = "配送状态描述", example = "配送完成")
    private String deliveryStatusDesc;
    
    @ApiModelProperty(value = "配送轨迹列表")
    private List<CityDeliveryOrderResponse.DeliveryTrackData> deliveryTrackList;

    // 扩展字段
    @ApiModelProperty(value = "商户名称", example = "荆楚粮油商城")
    private String merName;
    
    @ApiModelProperty(value = "用户名称", example = "张三")
    private String userName;
    
    @ApiModelProperty(value = "用户手机号", example = "13800138000")
    private String userPhone;
    
    @ApiModelProperty(value = "订单信息", example = "商品订单")
    private String orderInfo;
    
    @ApiModelProperty(value = "轨迹列表", example = "[]")
    private String trackList;
    
    @ApiModelProperty(value = "区域名称", example = "朝阳区东部配送区")
    private String areaName;
    
    @ApiModelProperty(value = "费用规则名称", example = "默认距离费用规则")
    private String feeRuleName;
    
    @ApiModelProperty(value = "订单状态文本", example = "已完成")
    private String orderStatusText;
    
    @ApiModelProperty(value = "配送员头像", example = "http://example.com/avatar.jpg")
    private String driverAvatar;
    
    @ApiModelProperty(value = "配送员评分", example = "4.8")
    private BigDecimal driverRating;

    /**
     * 根据配送订单构造响应
     */
    public static CityDeliveryOrderResponse fromOrder(CityDeliveryOrder order) {
        CityDeliveryOrderResponse response = new CityDeliveryOrderResponse();
        response.setId(order.getId());
        response.setDeliveryOrderNo(order.getDeliveryOrderNo());
        response.setOrderNo(order.getOrderNo());
        response.setMerId(order.getMerId());
        response.setUid(order.getUid());
        response.setDriverId(order.getDriverId());
        response.setDriverName(order.getDriverName());
        response.setDriverPhone(order.getDriverPhone());
        response.setPickupAddress(order.getPickupAddress());
        response.setPickupLongitude(order.getPickupLongitude());
        response.setPickupLatitude(order.getPickupLatitude());
        response.setPickupContact(order.getPickupContact());
        response.setPickupPhone(order.getPickupPhone());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setDeliveryLongitude(order.getDeliveryLongitude());
        response.setDeliveryLatitude(order.getDeliveryLatitude());
        response.setDeliveryContact(order.getDeliveryContact());
        response.setDeliveryPhone(order.getDeliveryPhone());
        response.setDistance(order.getDistance());
        response.setDeliveryFee(order.getDeliveryFee());
        response.setActualFee(order.getActualFee());
        response.setDriverCommission(order.getDriverCommission());
        response.setPlatformCommission(order.getPlatformCommission());
        response.setDeliveryType(order.getDeliveryType());
        response.setDeliveryStatus(order.getDeliveryStatus());
        response.setEstimatedTime(order.getEstimatedTime());
        response.setEstimatedPickupTime(order.getEstimatedPickupTime());
        response.setEstimatedDeliveryTime(order.getEstimatedDeliveryTime());
        response.setActualPickupTime(order.getActualPickupTime());
        response.setActualDeliveryTime(order.getActualDeliveryTime());
        response.setDeliveryRemark(order.getDeliveryRemark());
        response.setCancelReason(order.getCancelReason());
        response.setExceptionReason(order.getExceptionReason());
        response.setCustomerRating(order.getCustomerRating());
        response.setCustomerFeedback(order.getCustomerFeedback());
        response.setCreateTime(order.getCreateTime());
        response.setUpdateTime(order.getUpdateTime());
        
        // 设置状态文本
        response.setDeliveryStatusText(getDeliveryStatusText(order.getDeliveryStatus()));
        response.setDeliveryTypeText(getDeliveryTypeText(order.getDeliveryType()));
        
        return response;
    }

    /**
     * 获取配送状态文本
     */
    private static String getDeliveryStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待派单";
            case 1: return "已派单";
            case 2: return "已接单";
            case 3: return "取件中";
            case 4: return "配送中";
            case 5: return "已完成";
            case 6: return "已取消";
            case 7: return "配送失败";
            case 8: return "异常";
            default: return "未知";
        }
    }

    /**
     * 获取配送类型文本
     */
    private static String getDeliveryTypeText(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 1: return "即时配送";
            case 2: return "预约配送";
            default: return "未知";
        }
    }

    /**
     * 获取紧急程度文本
     */
    private static String getUrgencyLevelText(Integer level) {
        if (level == null) return "普通";
        switch (level) {
            case 1: return "普通";
            case 2: return "加急";
            case 3: return "特急";
            default: return "普通";
        }
    }

    /**
     * 配送跟踪数据内部类
     */
    @Data
    @Accessors(chain = true)
    @ApiModel(value = "DeliveryTrackData", description = "配送跟踪数据")
    public static class DeliveryTrackData implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @ApiModelProperty(value = "时间", example = "2024-01-01 10:00:00")
        private String time;
        
        @ApiModelProperty(value = "地址", example = "北京市朝阳区建国门外大街1号")
        private String location;
        
        @ApiModelProperty(value = "状态", example = "已取件")
        private String status;
        
        @ApiModelProperty(value = "描述", example = "配送员已取件，正在前往目的地")
        private String description;
        
        @ApiModelProperty(value = "经度", example = "116.456")
        private BigDecimal longitude;
        
        @ApiModelProperty(value = "纬度", example = "39.912")
        private BigDecimal latitude;
        
        @ApiModelProperty(value = "轨迹时间", example = "2024-01-01 10:00:00")
        private Date trackTime;
        
        @ApiModelProperty(value = "轨迹类型", example = "status_change")
        private String trackType;
        
        @ApiModelProperty(value = "轨迹信息", example = "配送状态变更")
        private String trackInfo;
        
        @ApiModelProperty(value = "备注", example = "配送员正常取件")
        private String remark;
    }
} 