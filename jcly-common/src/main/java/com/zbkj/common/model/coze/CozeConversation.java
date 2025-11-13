package com.zbkj.common.model.coze;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * Coze 会话表
 * </p>
 *
 * @author Auto Generator
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_coze_conversation")
@ApiModel(value = "CozeConversation对象", description = "Coze 会话表")
public class CozeConversation implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "用户ID")
    private Integer userId;

    @ApiModelProperty(value = "Coze会话ID")
    private String cozeConversationId;
    @ApiModelProperty(value = "会话中最新的一个上下文片段 ID")
    private String lastSectionId;

    @ApiModelProperty(value = "关联的Bot ID")
    private String cozeBotId;

    @ApiModelProperty(value = "会话名称")
    private String conversationName;
    
    @ApiModelProperty(value = "会话描述")
    private String description;

    @ApiModelProperty(value = "会话元数据(JSON格式)")
    private String metaData;

    @ApiModelProperty(value = "状态：0-已结束，1-进行中")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
