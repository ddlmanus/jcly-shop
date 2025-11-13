package com.zbkj.common.model.booking;

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
 * <p>
 * 预约订单表
 * </p>
 *
 * @author CRMEB
 * @since 2025-11-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_booking_order")
@ApiModel(value = "BookingOrder对象", description = "预约订单表")
public class BookingOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预约订单ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "关联的eb_merchant_order订单ID")
    private Integer orderId;

    @ApiModelProperty(value = "用户ID")
    private Integer userId;

    @ApiModelProperty(value = "商品ID")
    private Integer productId;

    @ApiModelProperty(value = "SKU ID")
    private Integer skuId;

    @ApiModelProperty(value = "商户ID")
    private Integer merchantId;

    @ApiModelProperty(value = "预约日期")
    private Date bookingDate;

    @ApiModelProperty(value = "预约时间段（如：09:00-10:00）")
    private String timeSlot;

    @ApiModelProperty(value = "时段价格")
    private BigDecimal timeSlotPrice;

    @ApiModelProperty(value = "服务方式：shop-到店服务，home-上门服务")
    private String serviceMode;

    @ApiModelProperty(value = "服务人员ID（员工表ID）")
    private Integer servicePersonId;

    @ApiModelProperty(value = "服务人员姓名")
    private String servicePersonName;

    @ApiModelProperty(value = "订单状态：0-待确认，1-已确认/待服务，2-服务中，3-已完成，4-已取消，5-已退款")
    private Integer status;

    @ApiModelProperty(value = "预约人姓名")
    private String userName;

    @ApiModelProperty(value = "预约人电话")
    private String userPhone;

    @ApiModelProperty(value = "服务地址（上门服务必填）")
    private String address;

    @ApiModelProperty(value = "省份")
    private String province;

    @ApiModelProperty(value = "城市")
    private String city;

    @ApiModelProperty(value = "区县")
    private String district;

    @ApiModelProperty(value = "详细地址")
    private String detail;

    @ApiModelProperty(value = "表单数据（JSON格式）")
    private String formData;

    @ApiModelProperty(value = "用户备注")
    private String remark;

    @ApiModelProperty(value = "取消原因")
    private String cancelReason;

    @ApiModelProperty(value = "退款金额")
    private BigDecimal refundAmount;

    @ApiModelProperty(value = "退款时间")
    private Date refundTime;

    @ApiModelProperty(value = "支付时间")
    private Date payTime;

    @ApiModelProperty(value = "确认时间")
    private Date confirmTime;

    @ApiModelProperty(value = "服务开始时间")
    private Date serviceStartTime;

    @ApiModelProperty(value = "服务结束时间")
    private Date serviceEndTime;

    @ApiModelProperty(value = "完成时间")
    private Date completeTime;

    @ApiModelProperty(value = "取消时间")
    private Date cancelTime;

    @ApiModelProperty(value = "是否已评价")
    private Boolean isComment;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}

