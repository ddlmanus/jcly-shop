package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 会员信息响应
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "MemberInfoResponse对象", description = "会员信息响应")
public class MemberInfoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "会员ID")
    private Integer id;

    @ApiModelProperty(value = "用户ID")
    private Integer uid;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "会员等级ID")
    private Integer levelId;

    @ApiModelProperty(value = "会员等级名称")
    private String levelName;

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

    @ApiModelProperty(value = "等级图标")
    private String levelIcon;

    @ApiModelProperty(value = "等级折扣")
    private BigDecimal levelDiscount;

    @ApiModelProperty(value = "等级说明")
    private String levelDescription;

    @ApiModelProperty(value = "距离下一等级所需积分")
    private Integer nextLevelNeedIntegral;

    @ApiModelProperty(value = "下一等级名称")
    private String nextLevelName;
}