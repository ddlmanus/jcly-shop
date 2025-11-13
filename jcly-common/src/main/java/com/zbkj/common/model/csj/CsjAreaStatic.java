package com.zbkj.common.model.csj;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_csj_area_static")
@ApiModel(value = "采食家大屏统计区域对象", description = "采食家大屏统计区域对象表")
public class CsjAreaStatic {
    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @ApiModelProperty(value = "订单总数")
    private int orderTotal;
    @ApiModelProperty(value = "用户总数")
    private int userTotal;
    @ApiModelProperty(value = "订单总金额")
    private BigDecimal orderPriceTotal;
    @ApiModelProperty(value = "区域名称")
    private String regionName;
    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "状态（0：关闭，1：开启）")
    private Boolean status;

    @ApiModelProperty(value = "是否删除 状态（0：否，1：是）")
    private Boolean isDel;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
