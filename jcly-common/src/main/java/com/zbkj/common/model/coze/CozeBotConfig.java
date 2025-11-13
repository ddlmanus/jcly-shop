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
 * Coze 智能体配置表
 * </p>
 *
 * @author Auto Generator
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_coze_bot_config")
@ApiModel(value = "CozeBotConfig对象", description = "Coze 智能体配置表")
public class CozeBotConfig implements Serializable {

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

    @ApiModelProperty(value = "智能体模式：0-单智能体，1-多智能体")
    private Integer botMode;

    @ApiModelProperty(value = "模型配置信息(JSON格式)")
    private String modelInfo;

    @ApiModelProperty(value = "知识库配置信息(JSON格式)")
    private String knowledge;

    @ApiModelProperty(value = "文件夹ID")
    private String folderId;

    @ApiModelProperty(value = "版本号")
    private String version;

    @ApiModelProperty(value = "音色数据列表(JSON格式)")
    private String voiceDataList;

    @ApiModelProperty(value = "插件信息列表(JSON格式)")
    private String pluginInfoList;

    @ApiModelProperty(value = "快捷指令列表(JSON格式)")
    private String shortcutCommands;

    @ApiModelProperty(value = "拥有者用户ID")
    private String ownerUserId;

    @ApiModelProperty(value = "提示词信息(JSON格式)")
    private String promptInfo;

    @ApiModelProperty(value = "开场白信息(JSON格式)")
    private String onboardingInfo;

    @ApiModelProperty(value = "工作流信息列表(JSON格式)")
    private String workflowInfoList;

    @ApiModelProperty(value = "音色信息列表(JSON格式)")
    private String voiceInfoList;

    @ApiModelProperty(value = "默认用户输入类型")
    private String defaultUserInputType;

    @ApiModelProperty(value = "背景图片信息(JSON格式)")
    private String backgroundImageInfo;

    @ApiModelProperty(value = "智能体头像URL")
    private String iconUrl;

    @ApiModelProperty(value = "回复建议配置(JSON格式)")
    private String suggestReplyInfo;

    @ApiModelProperty(value = "变量列表(JSON格式)")
    private String variables;

    @ApiModelProperty(value = "Coze创建时间（时间戳）")
    private Long cozeCreateTime;

    @ApiModelProperty(value = "Coze更新时间（时间戳）")
    private Long cozeUpdateTime;

    @ApiModelProperty(value = "完整配置JSON")
    private String fullConfigJson;

    @ApiModelProperty(value = "状态：0-禁用，1-启用")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
