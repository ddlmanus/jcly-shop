package com.zbkj.common.model.store;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 优惠券表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_store_coupon")
@ApiModel(value="StoreCoupon对象", description="优惠券表")
public class StoreCoupon implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "优惠券ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "优惠券名称")
    private String name;

    @ApiModelProperty(value = "优惠金额")
    private BigDecimal money;

    @ApiModelProperty(value = "最低消费")
    private BigDecimal minPrice;

    @ApiModelProperty(value = "发行数量")
    private Integer num;

    @ApiModelProperty(value = "已领取数量")
    private Integer receiveNum;

    @ApiModelProperty(value = "已使用数量")
    private Integer useNum;

    @ApiModelProperty(value = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    @ApiModelProperty(value = "使用类型：0-全场通用，1-商品分类，2-商品")
    private Integer useType;

    @ApiModelProperty(value = "主键ID（分类ID或商品ID）")
    private String primaryKey;

    @ApiModelProperty(value = "是否可重复领取：0-否，1-是")
    private Integer isRepeated;

    @ApiModelProperty(value = "状态：0-禁用，1-启用")
    private Integer status;

    @ApiModelProperty(value = "是否删除")
    private Boolean isDel;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}