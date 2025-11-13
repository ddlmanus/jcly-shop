package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 人工客服会话响应对象
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
@ApiModel(value = "HumanServiceSessionResponse对象", description = "人工客服会话响应")
public class HumanServiceSessionResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("会话ID")
    private String sessionId;

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("用户类型（USER-普通用户, MERCHANT-商户, PLATFORM-平台）")
    private String userType;

    @ApiModelProperty("用户名称")
    private String userName;

    @ApiModelProperty("用户头像")
    private String userAvatar;

    @ApiModelProperty("商户ID")
    private Integer merId;

    @ApiModelProperty("商户名称")
    private String merchantName;

    @ApiModelProperty("客服ID")
    private Integer staffId;

    @ApiModelProperty("客服名称")
    private String staffName;

    @ApiModelProperty("客服头像")
    private String staffAvatar;

    @ApiModelProperty("客服工号")
    private String staffNo;

    @ApiModelProperty("会话状态（WAITING-等待中, ACTIVE-进行中, ENDED-已结束）")
    private String sessionStatus;

    @ApiModelProperty("会话优先级（LOW-低, NORMAL-普通, HIGH-高, URGENT-紧急）")
    private String priority;

    @ApiModelProperty("服务等级")
    private String serviceLevel;

    @ApiModelProperty("会话标签")
    private String tags;

    @ApiModelProperty("会话备注")
    private String remarks;

    @ApiModelProperty("最后消息内容")
    private String lastMessage;

    @ApiModelProperty("最后消息时间")
    private Date lastMessageTime;

    @ApiModelProperty("未读消息数")
    private Integer unreadCount;

    @ApiModelProperty("等待开始时间")
    private Date waitStartTime;

    @ApiModelProperty("服务开始时间")
    private Date serviceStartTime;

    @ApiModelProperty("服务结束时间")
    private Date serviceEndTime;

    @ApiModelProperty("会话创建时间")
    private Date createTime;

    @ApiModelProperty("评分（1-5星）")
    private Integer rating;

    @ApiModelProperty("评价内容")
    private String ratingComment;

    @ApiModelProperty("客服是否在线")
    private Boolean staffOnline;

    @ApiModelProperty("预计等待时间（分钟）")
    private Integer estimatedWaitTime;

    @ApiModelProperty("会话持续时间（分钟）")
    private Integer sessionDuration;
}
