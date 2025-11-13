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
 * 会员表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_member")
@ApiModel(value="Member对象", description="会员表")
public class Member implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "会员ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "用户ID")
    private Integer uid;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "会员等级ID")
    private Integer levelId;

    @ApiModelProperty(value = "会员等级名称")
    private String levelName;

    @ApiModelProperty(value = "会员等级名称（前端兼容字段）")
    @TableField(exist = false)
    private String level_name;

    @ApiModelProperty(value = "会员昵称")
    private String nickname;

    @ApiModelProperty(value = "会员手机号")
    private String phone;

    @ApiModelProperty(value = "会员头像")
    private String avatar;

    @ApiModelProperty(value = "会员积分")
    private Integer integral;

    @ApiModelProperty(value = "累计获得积分")
    private Integer totalIntegral;

    @ApiModelProperty(value = "累计消费金额")
    private BigDecimal totalConsume;

    @ApiModelProperty(value = "订单总数")
    private Integer totalOrderCount;

    @ApiModelProperty(value = "最后登录时间")
    private Date lastLoginTime;

    @ApiModelProperty(value = "登录次数")
    private Integer loginCount;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "是否删除")
    private Boolean isDel;
}