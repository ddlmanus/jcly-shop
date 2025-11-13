package com.zbkj.common.model.member;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * 会员等级表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_member_level")
@ApiModel(value="MemberLevel对象", description="会员等级表")
public class MemberLevel implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "等级ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "等级名称")
    private String levelName;

    @ApiModelProperty(value = "等级名称（前端兼容字段）")
    @TableField(exist = false)
    private String level_name;

    @ApiModelProperty(value = "等级图标")
    private String icon;

    @ApiModelProperty(value = "所需积分")
    private Integer minIntegral;

    @ApiModelProperty(value = "折扣率")
    private BigDecimal discount;

    @ApiModelProperty(value = "等级说明")
    private String description;

    @ApiModelProperty(value = "状态：0=禁用，1=启用")
    private Integer status;

    @ApiModelProperty(value = "会员数量")
    private Integer memberCount;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "是否删除")
    private Boolean isDel;
    @ApiModelProperty(value = "是否为默认等级")
    private Boolean isDefault;
}