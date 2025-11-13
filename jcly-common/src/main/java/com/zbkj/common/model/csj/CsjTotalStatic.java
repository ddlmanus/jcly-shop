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
@TableName("eb_csj_total_static")
@ApiModel(value = "采食家大屏总统计对象", description = "采食家大屏总统计对象表")
public class CsjTotalStatic {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "销售总额")
    private BigDecimal yearSales;

    @ApiModelProperty(value = "全部用户数")
    private Integer userNum;

    @ApiModelProperty(value = "认证用户数")
    private Integer userPassNum;

    @ApiModelProperty(value = "全部商户数")
    private Integer merchantNum;

    @ApiModelProperty(value = "累计新增用户数")
    private Integer newAllUser;

    @ApiModelProperty(value = "统计日期")
    private Date statisticsDate;

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