package com.zbkj.common.model.coze;

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
 * Coze 聊天会话实体类
 * 用于存储用户与智能体的聊天会话信息
 * 
 * @author Auto Generator
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_coze_chat_session")
@ApiModel(value = "CozeChatSession对象", description = "Coze 聊天会话")
public class CozeChatSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "用户ID")
    private Integer userId;

    @ApiModelProperty(value = "智能体ID")
    private String cozeBotId;

    @ApiModelProperty(value = "Coze会话ID")
    private String cozeSessionId;

    @ApiModelProperty(value = "会话名称")
    private String sessionName;

    @ApiModelProperty(value = "最后一条消息内容")
    private String lastMessage;

    @ApiModelProperty(value = "最后消息时间")
    private Date lastMessageTime;

    @ApiModelProperty(value = "消息总数")
    private Integer messageCount;

    @ApiModelProperty(value = "会话状态：1-活跃，0-已结束")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
