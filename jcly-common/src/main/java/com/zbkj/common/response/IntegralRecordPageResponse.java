package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户积分记录分页响应对象
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
@ApiModel(value = "IntegralRecordPageResponse对象", description = "用户积分记录分页响应对象")
public class IntegralRecordPageResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "记录id")
    private Integer id;

    @ApiModelProperty(value = "用户uid")
    private Integer uid;

    @ApiModelProperty(value = "关联id-orderNo,refundOrderNo,(sign,system默认为0）")
    private String linkId;

    @ApiModelProperty(value = "关联类型（order,refund,sign,system）")
    private String linkType;

    @ApiModelProperty(value = "类型：1-增加，2-扣减")
    private Integer type;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "积分")
    private Integer integral;

    @ApiModelProperty(value = "备注")
    private String mark;

    @ApiModelProperty(value = "状态：1-订单创建，2-冻结期，3-完成，4-失效（订单退款）")
    private Integer status;

    @ApiModelProperty(value = "添加时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "用户昵称")
    private String nickName;
}
