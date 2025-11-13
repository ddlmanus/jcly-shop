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
 * 配送异常记录表
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
@TableName("eb_city_delivery_exception")
@ApiModel(value = "CityDeliveryException", description = "配送异常记录实体类")
public class CityDeliveryException implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "异常ID", example = "1")
    private Integer id;

    @NotBlank(message = "配送订单号不能为空")
    @ApiModelProperty(value = "配送订单号", required = true, example = "D202512010001")
    private String deliveryOrderNo;

    @ApiModelProperty(value = "配送员ID", example = "1")
    private Integer driverId;

    @NotNull(message = "异常类型不能为空")
    @ApiModelProperty(value = "异常类型：1-超时，2-路线偏移，3-配送员静止，4-距离异常", required = true, example = "1")
    private Integer exceptionType;

    @ApiModelProperty(value = "异常级别：1-轻微，2-一般，3-严重", example = "1")
    private Integer exceptionLevel;

    @NotBlank(message = "异常描述不能为空")
    @ApiModelProperty(value = "异常描述", required = true, example = "配送员超时30分钟未到达")
    private String exceptionDescription;

    @ApiModelProperty(value = "异常位置", example = "湖北省武汉市武昌区某某路")
    private String exceptionLocation;

    @ApiModelProperty(value = "异常位置经度", example = "114.305328")
    private BigDecimal longitude;

    @ApiModelProperty(value = "异常位置纬度", example = "30.593099")
    private BigDecimal latitude;

    @ApiModelProperty(value = "上报者类型：1-系统自动，2-用户，3-商户，4-配送员", example = "1")
    private Integer reporterType;

    @ApiModelProperty(value = "上报者ID", example = "1")
    private Integer reporterId;

    @ApiModelProperty(value = "证据图片（JSON格式）", example = "[\"img1.jpg\",\"img2.jpg\"]")
    private String evidenceImages;

    @ApiModelProperty(value = "证据描述", example = "配送员长时间未移动")
    private String evidenceDescription;

    @ApiModelProperty(value = "处理状态：0-待处理，1-处理中，2-已处理，3-已忽略", example = "0")
    private Integer handleStatus;

    @ApiModelProperty(value = "处理结果", example = "已联系配送员，问题已解决")
    private String handleResult;

    @ApiModelProperty(value = "处理人ID", example = "1")
    private Integer handlerId;

    @ApiModelProperty(value = "处理时间", example = "2024-01-01 10:30:00")
    private Date handleTime;

    @ApiModelProperty(value = "罚款金额", example = "50.00")
    private BigDecimal penaltyAmount;

    @ApiModelProperty(value = "赔偿金额", example = "100.00")
    private BigDecimal compensationAmount;

    @NotNull(message = "创建时间不能为空")
    @ApiModelProperty(value = "创建时间", required = true, example = "2024-01-01 10:00:00")
    private Date createTime;

    @NotNull(message = "更新时间不能为空")
    @ApiModelProperty(value = "更新时间", required = true, example = "2024-01-01 10:00:00")
    private Date updateTime;
} 