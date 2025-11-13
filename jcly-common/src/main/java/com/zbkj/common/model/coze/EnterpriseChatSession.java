package com.zbkj.common.model.coze;

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
 * 企业聊天会话表
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_coze_enterprise_chat_session")
@ApiModel(value = "EnterpriseChatSession对象", description = "企业聊天会话表")
public class EnterpriseChatSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "会话唯一标识")
    @TableField("session_id")
    private String sessionId;

    @ApiModelProperty(value = "用户ID（商户用户）")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "商户ID")
    @TableField("mer_id")
    private Long merId;

    @ApiModelProperty(value = "Coze智能体ID")
    @TableField("coze_bot_id")
    private String cozeBotId;

    @ApiModelProperty(value = "Coze会话ID")
    @TableField("coze_conversation_id")
    private String cozeConversationId;

    @ApiModelProperty(value = "会话标题")
    @TableField("session_title")
    private String sessionTitle;

    @ApiModelProperty(value = "会话状态：1-活跃 2-已结束 3-已删除")
    @TableField("status")
    private Integer status;

    @ApiModelProperty(value = "总消息数量")
    @TableField("total_messages")
    private Integer totalMessages;

    @ApiModelProperty(value = "最后消息时间")
    @TableField("last_message_time")
    private Date lastMessageTime;

    @ApiModelProperty(value = "最后一条消息内容")
    @TableField("last_message_content")
    private String lastMessageContent;

    @ApiModelProperty(value = "会话上下文信息（JSON）")
    @TableField("session_context")
    private String sessionContext;

    @ApiModelProperty(value = "扩展元数据（JSON）")
    @TableField("meta_data")
    private String metaData;

    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField("update_time")
    private Date updateTime;

    // 会话状态常量
    public static final int STATUS_ACTIVE = 1;      // 活跃
    public static final int STATUS_ENDED = 2;       // 已结束
    public static final int STATUS_DELETED = 3;     // 已删除
}
