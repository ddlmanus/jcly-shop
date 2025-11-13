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
 * Coze 知识库文件表
 * </p>
 *
 * @author Auto Generator
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_coze_knowledge_file")
@ApiModel(value = "CozeKnowledgeFile对象", description = "Coze 知识库文件表")
public class CozeKnowledgeFile implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商户ID")
    private Integer merchantId;

    @ApiModelProperty(value = "Coze知识库ID")
    private String cozeKnowledgeId;

    @ApiModelProperty(value = "Coze文件ID")
    private String cozeFileId;

    @ApiModelProperty(value = "文件名称")
    private String fileName;

    @ApiModelProperty(value = "文件大小(字节)")
    private Long fileSize;

    @ApiModelProperty(value = "文件类型")
    private String fileType;

    @ApiModelProperty(value = "文件内容的总字符数量")
    private Integer charCount;

    @ApiModelProperty(value = "文件的分段数量")
    private Integer sliceCount;

    @ApiModelProperty(value = "被对话命中的次数")
    private Integer hitCount;

    @ApiModelProperty(value = "文件的格式类型：0-文档类型，1-表格类型，2-照片类型")
    private Integer formatType;

    @ApiModelProperty(value = "文件的上传方式：0-上传本地文件，1-上传在线网页")
    private Integer sourceType;

    @ApiModelProperty(value = "在线网页自动更新的频率(小时)")
    private Integer updateInterval;

    @ApiModelProperty(value = "在线网页是否自动更新：0-不自动更新，1-自动更新")
    private Integer updateType;

    @ApiModelProperty(value = "上传的本地文档的唯一标识")
    private String tosUri;

    @ApiModelProperty(value = "分段规则(JSON格式)")
    private String chunkStrategy;

    @ApiModelProperty(value = "Coze平台创建时间戳")
    private Integer cozeCreateTime;

    @ApiModelProperty(value = "Coze平台更新时间戳")
    private Integer cozeUpdateTime;

    @ApiModelProperty(value = "最后同步时间")
    private Date syncTime;

    @ApiModelProperty(value = "本地文件路径")
    private String localFilePath;

    @ApiModelProperty(value = "Coze文件URL")
    private String cozeFileUrl;

    @ApiModelProperty(value = "上传状态：0-上传中，1-成功，2-失败")
    private Integer uploadStatus;

    @ApiModelProperty(value = "处理状态：0-待处理，1-处理中，2-处理成功，3-处理失败")
    private Integer processStatus;

    @ApiModelProperty(value = "处理进度(0-100)")
    private Integer processProgress;

    @ApiModelProperty(value = "错误信息")
    private String errorMessage;

    @ApiModelProperty(value = "状态：0-禁用，1-启用")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
