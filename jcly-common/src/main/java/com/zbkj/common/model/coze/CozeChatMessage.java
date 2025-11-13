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
 * Coze 聊天消息实体类
 * 用于存储用户与智能体的聊天消息记录
 * 
 * @author Auto Generator
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_coze_chat_message")
@ApiModel(value = "CozeChatMessage对象", description = "Coze 聊天消息")
public class CozeChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "会话ID")
    private Integer sessionId;

    @ApiModelProperty(value = "用户ID")
    private Integer userId;

    @ApiModelProperty(value = "Coze消息ID")
    private String cozeMessageId;

    @ApiModelProperty(value = "Coze聊天ID")
    private String cozeChatId;

    @ApiModelProperty(value = "智能体ID")
    private String cozeBotId;

    @ApiModelProperty(value = "消息角色：user（用户），assistant（助手）")
    private String role;

    @ApiModelProperty(value = "消息内容")
    private String content;

    @ApiModelProperty(value = "消息内容类型：text（文本），object_string（对象字符串）")
    private String contentType;

    @ApiModelProperty(value = "消息类型：text（文本），image（图片），file（文件）")
    private String type;

    @ApiModelProperty(value = "消息状态：1-启用，0-禁用")
    private Integer status;

    @ApiModelProperty(value = "Coze创建时间（时间戳）")
    private Long cozeCreatedAt;

    @ApiModelProperty(value = "Coze更新时间（时间戳）")
    private Long cozeUpdatedAt;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
