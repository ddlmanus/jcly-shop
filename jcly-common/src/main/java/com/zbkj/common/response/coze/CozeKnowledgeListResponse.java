package com.zbkj.common.response.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Coze知识库列表响应
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "CozeKnowledgeListResponse对象", description = "Coze知识库列表响应")
public class CozeKnowledgeListResponse extends CozeBaseResponse {

    @ApiModelProperty(value = "知识库列表数据")
    private ListDatasetOpenApiData data;

    @Data
    @ApiModel(value = "知识库列表数据")
    public static class ListDatasetOpenApiData {
        @ApiModelProperty(value = "空间中的知识库总数量")
        @JsonProperty("total_count")
        private Integer totalCount;

        @ApiModelProperty(value = "知识库详情列表")
        @JsonProperty("dataset_list")
        private List<Dataset> datasetList;
    }

    @Data
    @ApiModel(value = "知识库详情")
    public static class Dataset {
        @ApiModelProperty(value = "知识库名称")
        private String name;

        @ApiModelProperty(value = "知识库状态，1：启用中，3：未启用")
        private Integer status;

        @ApiModelProperty(value = "当前用户是否为该知识库的所有者")
        @JsonProperty("can_edit")
        private Boolean canEdit;

        @ApiModelProperty(value = "知识库图标的URI")
        @JsonProperty("icon_uri")
        private String iconUri;

        @ApiModelProperty(value = "知识库图标的URL")
        @JsonProperty("icon_url")
        private String iconUrl;

        @ApiModelProperty(value = "知识库所在空间的空间ID")
        @JsonProperty("space_id")
        private String spaceId;

        @ApiModelProperty(value = "知识库中的文件数量")
        @JsonProperty("doc_count")
        private Integer docCount;

        @ApiModelProperty(value = "知识库中的文件列表")
        @JsonProperty("file_list")
        private List<String> fileList;

        @ApiModelProperty(value = "知识库命中总次数")
        @JsonProperty("hit_count")
        private Integer hitCount;

        @ApiModelProperty(value = "知识库创建者的头像URL")
        @JsonProperty("avatar_url")
        private String avatarUrl;

        @ApiModelProperty(value = "知识库创建者的扣子ID")
        @JsonProperty("creator_id")
        private String creatorId;

        @ApiModelProperty(value = "知识库ID")
        @JsonProperty("dataset_id")
        private String datasetId;

        @ApiModelProperty(value = "知识库创建时间，秒级时间戳")
        @JsonProperty("create_time")
        private Integer createTime;

        @ApiModelProperty(value = "知识库描述信息")
        private String description;

        @ApiModelProperty(value = "知识库类型，0: 文本类型，1：表格类型，2: 图片类型")
        @JsonProperty("format_type")
        private Integer formatType;

        @ApiModelProperty(value = "知识库分段总数")
        @JsonProperty("slice_count")
        private Integer sliceCount;

        @ApiModelProperty(value = "知识库的更新时间，秒级时间戳")
        @JsonProperty("update_time")
        private Integer updateTime;

        @ApiModelProperty(value = "知识库创建者的用户名")
        @JsonProperty("creator_name")
        private String creatorName;

        @ApiModelProperty(value = "知识库来源：0=coze知识库 1=火山知识库")
        @JsonProperty("dataset_type")
        private Integer datasetType;

        @ApiModelProperty(value = "知识库中已存文件的总大小")
        @JsonProperty("all_file_size")
        private Long allFileSize;

        @ApiModelProperty(value = "知识库已绑定的智能体数量")
        @JsonProperty("bot_used_count")
        private Integer botUsedCount;

        @ApiModelProperty(value = "知识库的切片规则")
        @JsonProperty("chunk_strategy")
        private ChunkStrategy chunkStrategy;

        @ApiModelProperty(value = "处理失败的文件列表")
        @JsonProperty("failed_file_list")
        private List<String> failedFileList;

        @ApiModelProperty(value = "处理中的文件名")
        @JsonProperty("processing_file_list")
        private List<String> processingFileList;

        @ApiModelProperty(value = "处理中的文件ID")
        @JsonProperty("processing_file_id_list")
        private List<String> processingFileIdList;
    }

    @Data
    @ApiModel(value = "切片规则")
    public static class ChunkStrategy {
        @ApiModelProperty(value = "分段设置，0：自动分段与清洗，1：自定义")
        @JsonProperty("chunk_type")
        private Integer chunkType;

        @ApiModelProperty(value = "分段标识符")
        private String separator;

        @ApiModelProperty(value = "最大分段长度，取值范围为100~2000")
        @JsonProperty("max_tokens")
        private Long maxTokens;

        @ApiModelProperty(value = "是否自动过滤连续的空格、换行符和制表符")
        @JsonProperty("remove_extra_spaces")
        private Boolean removeExtraSpaces;

        @ApiModelProperty(value = "是否自动过滤所有URL和电子邮箱地址")
        @JsonProperty("remove_urls_emails")
        private Boolean removeUrlsEmails;

        @ApiModelProperty(value = "图片知识库的标注方式：0-系统自动标注，1-手工标注")
        @JsonProperty("caption_type")
        private Integer captionType;
    }
}
