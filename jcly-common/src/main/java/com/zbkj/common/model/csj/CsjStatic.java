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
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_csj_static")
@ApiModel(value = "采食家大屏统计对象", description = "采食家大屏统计对象表")
public class CsjStatic {
    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @ApiModelProperty(value = "年份")
    private String year;
    @ApiModelProperty(value = "月份")
    private String month;
    @ApiModelProperty(value = "今日销售额")
    private BigDecimal sales;
    @ApiModelProperty(value = "销售总额")
    private BigDecimal yearSales;

    @ApiModelProperty(value = "昨日销售额")
    private BigDecimal yesterdaySales;

    @ApiModelProperty(value = "今日访问量")
    private Integer pageviews;

    @ApiModelProperty(value = "昨日访问量")
    private Integer yesterdayPageviews;

    @ApiModelProperty(value = "今日订单量")
    private Integer orderNum;

    @ApiModelProperty(value = "今日完成订单量")
    private Integer finishOrderNum;

    @ApiModelProperty(value = "昨日订单量")
    private Integer yesterdayOrderNum;

    @ApiModelProperty(value = "今日新增用户数")
    private Integer todayNewUserNum;

    @ApiModelProperty(value = "昨日新增用户数")
    private Integer yesterdayNewUserNum;

    @ApiModelProperty(value = "今日新增商户数")
    private Integer todayNewMerchantNum;

    @ApiModelProperty(value = "昨日新增商户数")
    private Integer yesterdayNewMerchantNum;
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
