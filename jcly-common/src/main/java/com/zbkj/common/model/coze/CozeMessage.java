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
 * <p>
 * Coze 消息表
 * </p>
 *
 * @author Auto Generator
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_coze_message")
@ApiModel(value = "CozeMessage对象", description = "Coze 消息表")
public class CozeMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "用户ID")
    private Integer userId;

    @ApiModelProperty(value = "Coze消息ID")
    private String cozeMessageId;

    @ApiModelProperty(value = "Coze会话ID")
    private String cozeConversationId;

    @ApiModelProperty(value = "关联的Bot ID")
    private String cozeBotId;

    @ApiModelProperty(value = "消息类型：question-用户提问，answer-机器人回答，function_call-函数调用，tool_output-工具输出")
    private String messageType;

    @ApiModelProperty(value = "消息内容")
    private String content;

    @ApiModelProperty(value = "消息内容类型：text-文本，object_string-对象字符串，card-卡片")
    private String contentType;

    @ApiModelProperty(value = "消息状态：completed-已完成，created-已创建，in_progress-进行中，requires_action-需要行动，failed-失败")
    private String status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
