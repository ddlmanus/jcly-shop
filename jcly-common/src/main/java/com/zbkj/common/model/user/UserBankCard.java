package com.zbkj.common.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * 用户银行卡信息表
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
@TableName("eb_user_bank_card")
@ApiModel(value = "UserBankCard对象", description = "用户银行卡信息表")
public class UserBankCard implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "用户ID")
    private Integer uid;

    @ApiModelProperty(value = "银行卡号（加密存储）")
    private String cardNo;

    @ApiModelProperty(value = "银行卡号后4位（明文显示用）")
    private String cardNoLast4;

    @ApiModelProperty(value = "持卡人姓名（加密存储）")
    private String cardholderName;

    @ApiModelProperty(value = "身份证号（加密存储）")
    private String idCard;

    @ApiModelProperty(value = "手机号（加密存储）")
    private String mobile;

    @ApiModelProperty(value = "银行名称")
    private String bankName;

    @ApiModelProperty(value = "卡类型：1-借记卡，2-信用卡")
    private String cardType;

    @ApiModelProperty(value = "验证类型：3-三要素，4-四要素，6-六要素")
    private String verifyType;

    @ApiModelProperty(value = "CVN2码（6要素验证时使用，加密存储）")
    private String cvn2;

    @ApiModelProperty(value = "有效期（6要素验证时使用，格式YYMM，加密存储）")
    private String expired;

    @ApiModelProperty(value = "是否为默认卡：0-否，1-是")
    private Integer isDefault;

    @ApiModelProperty(value = "状态：0-禁用，1-启用")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "备注")
    private String remark;
}