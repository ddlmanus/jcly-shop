package com.zbkj.common.model.chat;

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
 * 企业级聊天消息模型
 * 支持多种消息类型和富媒体内容
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_enterprise_message")
@ApiModel(value = "EnterpriseMessage对象", description = "企业级聊天消息")
public class EnterpriseMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "消息ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "消息唯一标识")
    private String messageId;

    @ApiModelProperty(value = "会话ID")
    private String sessionId;

    @ApiModelProperty(value = "发送者ID")
    private Integer senderId;

    @ApiModelProperty(value = "发送者类型: USER-用户, STAFF-客服, SYSTEM-系统, AI-智能助手")
    private String senderType;

    @ApiModelProperty(value = "发送者名称")
    private String senderName;

    @ApiModelProperty(value = "发送者头像")
    private String senderAvatar;

    @ApiModelProperty(value = "接收者ID")
    private Integer receiverId;

    @ApiModelProperty(value = "接收者类型")
    private String receiverType;

    @ApiModelProperty(value = "消息类型: text, image, file, product_card, order_card等")
    private String messageType;

    @ApiModelProperty(value = "内容类型: text, json, html等")
    private String contentType;

    @ApiModelProperty(value = "消息内容")
    private String content;

    @ApiModelProperty(value = "扩展数据(JSON格式)")
    private String extData;

    @ApiModelProperty(value = "附件信息(JSON格式)")
    private String attachments;

    @ApiModelProperty(value = "消息状态: 0-发送中, 1-已发送, 2-已送达, 3-已读, 4-发送失败")
    private Integer status;

    @ApiModelProperty(value = "是否为系统消息: 0-否, 1-是")
    private Boolean isSystemMessage;

    @ApiModelProperty(value = "是否已删除: 0-否, 1-是")
    private Boolean isDeleted;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "消息发送时间")
    private Date sendTime;

    @ApiModelProperty(value = "消息读取时间")
    private Date readTime;

    @ApiModelProperty(value = "优先级: 0-普通, 1-重要, 2-紧急")
    private Integer priority;

    @ApiModelProperty(value = "消息标签")
    private String tags;

    @ApiModelProperty(value = "引用消息ID")
    private String replyToMessageId;

    @ApiModelProperty(value = "消息来源: web, mobile, api等")
    private String source;

    @ApiModelProperty(value = "IP地址")
    private String ipAddress;

    @ApiModelProperty(value = "用户代理")
    private String userAgent;
}
