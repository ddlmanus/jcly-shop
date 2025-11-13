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
import java.util.Date;

/**
 * 会员积分记录表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_member_integral_record")
@ApiModel(value="MemberIntegralRecord对象", description="会员积分记录表")
public class MemberIntegralRecord implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "会员ID")
    private Integer memberId;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "类型：1=增加，2=减少")
    private Integer type;

    @ApiModelProperty(value = "积分数量")
    private Integer integral;

    @ApiModelProperty(value = "变动后积分")
    private Integer balance;

    @ApiModelProperty(value = "变动说明")
    private String title;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "关联ID")
    private Integer linkId;

    @ApiModelProperty(value = "关联类型")
    private String linkType;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "备注信息（前端显示用）")
    @TableField(exist = false)
    private String description;

    @ApiModelProperty(value = "变动时间（格式化后的创建时间）")
    @TableField(exist = false)
    private String create_time;
}