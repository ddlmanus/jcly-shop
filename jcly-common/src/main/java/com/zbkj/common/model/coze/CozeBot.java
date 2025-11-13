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
 * Coze 智能体表
 * </p>
 *
 * @author Auto Generator
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_coze_bot")
@ApiModel(value = "CozeBot对象", description = "Coze 智能体表")
public class CozeBot implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商户ID")
    private Integer merchantId;

    @ApiModelProperty(value = "Coze Bot ID")
    private String cozeBotId;

    @ApiModelProperty(value = "智能体名称")
    private String name;

    @ApiModelProperty(value = "智能体描述")
    private String description;

    @ApiModelProperty(value = "智能体头像")
    private String iconUrl;

    @ApiModelProperty(value = "个性化指令")
    private String instructions;

    @ApiModelProperty(value = "开场白内容")
    private String promptInfo;

    @ApiModelProperty(value = "智能体版本：1-草稿版本，2-发布版本")
    private Integer version;

    @ApiModelProperty(value = "发布状态：0-未发布，1-已发布")
    private Integer publishStatus;

    @ApiModelProperty(value = "Coze空间ID")
    private String spaceId;

    @ApiModelProperty(value = "模型名称")
    private String model;

    @ApiModelProperty(value = "智能体配置(JSON格式)")
    private String config;

    @ApiModelProperty(value = "状态：0-禁用，1-启用")
    private Integer status;

    @ApiModelProperty(value = "是否默认智能体：0-否，1-是")
    private Integer isDefault;

    @ApiModelProperty(value = "绑定的知识库ID列表(JSON格式)")
    private String knowledgeIds;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
