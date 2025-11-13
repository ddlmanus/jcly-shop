package com.zbkj.common.model.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 配送员客户评价表
 * @author 荆楚粮油
 * @since 2024-01-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_city_delivery_customer_rating")
@ApiModel(value="CityDeliveryCustomerRating对象", description="配送员客户评价表")
public class CityDeliveryCustomerRating implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "评价ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "配送订单号")
    private String deliveryOrderNo;

    @ApiModelProperty(value = "原订单号")
    private String orderNo;

    @ApiModelProperty(value = "配送员ID")
    private Integer driverId;

    @ApiModelProperty(value = "配送员姓名")
    private String driverName;

    @ApiModelProperty(value = "客户ID")
    private Integer customerId;

    @ApiModelProperty(value = "客户姓名")
    private String customerName;

    @ApiModelProperty(value = "客户手机号")
    private String customerPhone;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "商户名称")
    private String merName;

    @ApiModelProperty(value = "总体评分（1-5星）")
    private BigDecimal overallRating;

    @ApiModelProperty(value = "服务态度评分")
    private BigDecimal serviceRating;

    @ApiModelProperty(value = "配送速度评分")
    private BigDecimal speedRating;

    @ApiModelProperty(value = "配送质量评分")
    private BigDecimal qualityRating;

    @ApiModelProperty(value = "评价内容")
    private String comment;

    @ApiModelProperty(value = "评价标签（JSON格式）")
    private String commentTags;

    @ApiModelProperty(value = "评价图片（JSON格式）")
    private String ratingImages;

    @ApiModelProperty(value = "是否匿名评价：0-否，1-是")
    private Boolean isAnonymous;

    @ApiModelProperty(value = "配送状态：4-已送达")
    private Integer deliveryStatus;

    @ApiModelProperty(value = "配送开始时间")
    private Date deliveryStartTime;

    @ApiModelProperty(value = "配送结束时间")
    private Date deliveryEndTime;

    @ApiModelProperty(value = "配送时长（分钟）")
    private Integer deliveryDuration;

    @ApiModelProperty(value = "配送距离（公里）")
    private BigDecimal deliveryDistance;

    @ApiModelProperty(value = "配送费用")
    private BigDecimal deliveryFee;

    @ApiModelProperty(value = "是否准时送达：0-否，1-是")
    private Boolean onTimeDelivery;

    @ApiModelProperty(value = "是否提前送达：0-否，1-是")
    private Boolean earlyDelivery;

    @ApiModelProperty(value = "是否延迟送达：0-否，1-是")
    private Boolean lateDelivery;

    @ApiModelProperty(value = "温度是否合适：0-否，1-是")
    private Boolean temperatureAppropriate;

    @ApiModelProperty(value = "包装是否完整：0-否，1-是")
    private Boolean packagingIntact;

    @ApiModelProperty(value = "配送员仪表：0-不佳，1-良好")
    private Boolean driverAppearance;

    @ApiModelProperty(value = "配送员礼貌：0-不佳，1-良好")
    private Boolean driverPoliteness;

    @ApiModelProperty(value = "沟通质量：0-不佳，1-良好")
    private Boolean communicationQuality;

    @ApiModelProperty(value = "问题解决：0-不佳，1-良好")
    private Boolean problemResolution;

    @ApiModelProperty(value = "是否推荐：0-否，1-是")
    private Boolean wouldRecommend;

    @ApiModelProperty(value = "系统自动评分（基于配送表现）")
    private BigDecimal systemRating;

    @ApiModelProperty(value = "最终评分（客户评分+系统评分的加权平均）")
    private BigDecimal finalRating;

    @ApiModelProperty(value = "评分权重（基于客户等级等因素）")
    private BigDecimal ratingWeight;

    @ApiModelProperty(value = "是否有投诉：0-否，1-是")
    private Boolean hasComplaint;

    @ApiModelProperty(value = "投诉原因")
    private String complaintReason;

    @ApiModelProperty(value = "投诉是否已处理：0-否，1-是")
    private Boolean complaintHandled;

    @ApiModelProperty(value = "管理员回复")
    private String adminReply;

    @ApiModelProperty(value = "管理员回复时间")
    private Date adminReplyTime;

    @ApiModelProperty(value = "有用评价统计")
    private Integer helpfulCount;

    @ApiModelProperty(value = "评价状态：0-隐藏，1-显示，2-待审核")
    private Integer status;

    @ApiModelProperty(value = "审核状态：0-待审核，1-审核通过，2-审核拒绝")
    private Integer auditStatus;

    @ApiModelProperty(value = "审核管理员ID")
    private Integer auditAdminId;

    @ApiModelProperty(value = "审核时间")
    private Date auditTime;

    @ApiModelProperty(value = "审核备注")
    private String auditRemark;

    @ApiModelProperty(value = "评价IP地址")
    private String ipAddress;

    @ApiModelProperty(value = "用户代理信息")
    private String userAgent;

    @ApiModelProperty(value = "是否删除（0：未删除；1：已删除）")
    private Boolean isDel;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
} 