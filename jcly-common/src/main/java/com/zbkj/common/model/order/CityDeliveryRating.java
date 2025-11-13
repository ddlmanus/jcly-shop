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
import java.util.Date;

/**
 * 配送评价表
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
@TableName("eb_city_delivery_rating")
@ApiModel(value = "CityDeliveryRating", description = "配送评价实体类")
public class CityDeliveryRating implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "评价ID", example = "1")
    private Integer id;

    @NotBlank(message = "配送订单号不能为空")
    @ApiModelProperty(value = "配送订单号", required = true, example = "D202512010001")
    private String deliveryOrderNo;

    @NotBlank(message = "订单号不能为空")
    @ApiModelProperty(value = "原订单号", required = true, example = "2025120100001")
    private String orderNo;

    @NotNull(message = "配送员ID不能为空")
    @ApiModelProperty(value = "配送员ID", required = true, example = "1")
    private Integer driverId;

    @NotNull(message = "用户ID不能为空")
    @ApiModelProperty(value = "用户ID", required = true, example = "1")
    private Integer userId;

    @NotNull(message = "商户ID不能为空")
    @ApiModelProperty(value = "商户ID", required = true, example = "1")
    private Integer merId;

    @ApiModelProperty(value = "评价类型：1-用户评价配送员，2-商户评价配送员", example = "1")
    private Integer ratingType;

    @NotNull(message = "服务态度评分不能为空")
    @ApiModelProperty(value = "服务态度评分（1-5分）", required = true, example = "5")
    private Integer serviceRating;

    @NotNull(message = "配送速度评分不能为空")
    @ApiModelProperty(value = "配送速度评分（1-5分）", required = true, example = "5")
    private Integer speedRating;

    @NotNull(message = "货物安全评分不能为空")
    @ApiModelProperty(value = "货物安全评分（1-5分）", required = true, example = "5")
    private Integer safetyRating;

    @NotNull(message = "总体评分不能为空")
    @ApiModelProperty(value = "总体评分（1-5分）", required = true, example = "5")
    private Integer overallRating;

    @ApiModelProperty(value = "评价内容", example = "配送员服务态度很好，送货及时")
    private String comment;

    @ApiModelProperty(value = "配送员回复", example = "谢谢您的好评，我们会继续努力")
    private String reply;

    @ApiModelProperty(value = "回复时间", example = "2024-01-01 15:00:00")
    private Date replyTime;

    @ApiModelProperty(value = "评价图片（JSON格式）", example = "[\"img1.jpg\",\"img2.jpg\"]")
    private String images;

    @ApiModelProperty(value = "是否匿名评价：0-否，1-是", example = "0")
    private Integer isAnonymous;

    @ApiModelProperty(value = "是否默认评价：0-否，1-是", example = "0")
    private Integer isDefault;

    @NotNull(message = "创建时间不能为空")
    @ApiModelProperty(value = "创建时间", required = true, example = "2024-01-01 14:00:00")
    private Date createTime;

    @NotNull(message = "更新时间不能为空")
    @ApiModelProperty(value = "更新时间", required = true, example = "2024-01-01 14:00:00")
    private Date updateTime;
} 