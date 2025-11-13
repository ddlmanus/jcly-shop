package com.zbkj.common.model.member;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * 会员积分规则表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_member_integral_rule")
@ApiModel(value="MemberIntegralRule对象", description="会员积分规则表")
public class MemberIntegralRule implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "消费金额兑换积分比例，多少元=1积分")
    private BigDecimal moneyToIntegral;

    @ApiModelProperty(value = "积分抵扣金额比例，多少积分=1元")
    private BigDecimal integralToMoney;

    @ApiModelProperty(value = "积分抵扣比例，最多可抵扣订单金额的百分比")
    private Integer integralDeductionLimit;

    @ApiModelProperty(value = "积分到期规则：0=永不过期，1=按年度过期，2=按获取日期计算")
    private Integer integralExpireType;

    @ApiModelProperty(value = "积分有效期天数，从获取积分之日起计算")
    private Integer integralExpireDays;

    @ApiModelProperty(value = "积分说明")
    private String integralDescription;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}