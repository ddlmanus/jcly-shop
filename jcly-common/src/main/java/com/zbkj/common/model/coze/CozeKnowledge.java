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
 * Coze 知识库表
 * </p>
 *
 * @author Auto Generator
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_coze_knowledge")
@ApiModel(value = "CozeKnowledge对象", description = "Coze 知识库表")
public class CozeKnowledge implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商户ID")
    private Integer merchantId;

    @ApiModelProperty(value = "Coze知识库ID")
    private String cozeKnowledgeId;

    @ApiModelProperty(value = "知识库名称")
    private String name;

    @ApiModelProperty(value = "知识库描述")
    private String description;

    @ApiModelProperty(value = "知识库图标URI")
    private String iconUri;

    @ApiModelProperty(value = "知识库图标URL")
    private String iconUrl;

    @ApiModelProperty(value = "Coze空间ID")
    private String spaceId;

    @ApiModelProperty(value = "知识库中的文件数量")
    private Integer docCount;

    @ApiModelProperty(value = "知识库命中总次数")
    private Integer hitCount;

    @ApiModelProperty(value = "知识库分段总数")
    private Integer sliceCount;

    @ApiModelProperty(value = "知识库中已存文件的总大小")
    private Long allFileSize;

    @ApiModelProperty(value = "知识库已绑定的智能体数量")
    private Integer botUsedCount;

    @ApiModelProperty(value = "知识库类型：0-文本类型，1-表格类型，2-图片类型")
    private Integer formatType;

    @ApiModelProperty(value = "知识库来源：0-coze知识库，1-火山知识库")
    private Integer datasetType;

    @ApiModelProperty(value = "知识库创建者的扣子ID")
    private String creatorId;

    @ApiModelProperty(value = "知识库创建者的用户名")
    private String creatorName;

    @ApiModelProperty(value = "知识库创建者的头像URL")
    private String avatarUrl;

    @ApiModelProperty(value = "当前用户是否为该知识库的所有者")
    private Boolean canEdit;

    @ApiModelProperty(value = "知识库的切片规则(JSON格式)")
    private String chunkStrategy;

    @ApiModelProperty(value = "知识库中的文件列表(JSON格式)")
    private String fileList;

    @ApiModelProperty(value = "处理失败的文件列表(JSON格式)")
    private String failedFileList;

    @ApiModelProperty(value = "处理中的文件名列表(JSON格式)")
    private String processingFileList;

    @ApiModelProperty(value = "处理中的文件ID列表(JSON格式)")
    private String processingFileIdList;

    @ApiModelProperty(value = "Coze平台创建时间戳")
    private Integer cozeCreateTime;

    @ApiModelProperty(value = "Coze平台更新时间戳")
    private Integer cozeUpdateTime;

    @ApiModelProperty(value = "最后同步时间")
    private Date syncTime;

    @ApiModelProperty(value = "状态：0-禁用，1-启用")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
