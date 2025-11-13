package com.zbkj.common.model.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 配送员认证资料表
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
@TableName("eb_city_delivery_driver_certification")
public class CityDeliveryDriverCertification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @NotNull(message = "配送员ID不能为空")
    private Integer driverId;

    @NotNull(message = "认证类型不能为空")
    private Integer certificationType;

    @NotBlank(message = "证件类型不能为空")
    private String documentType;

    private String documentNumber;

    private String documentImageFront;

    private String documentImageBack;

    private String documentImageHold;

    private Date expireDate;

    private Integer status;

    private String auditRemark;

    private Date auditTime;

    private Integer auditorId;

    private Date submitTime;

    @NotNull(message = "创建时间不能为空")
    private Date createTime;

    @NotNull(message = "更新时间不能为空")
    private Date updateTime;
} 