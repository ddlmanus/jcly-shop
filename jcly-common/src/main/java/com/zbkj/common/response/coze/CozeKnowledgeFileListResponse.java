package com.zbkj.common.response.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Coze知识库文件列表响应
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "CozeKnowledgeFileListResponse对象", description = "Coze知识库文件列表响应")
public class CozeKnowledgeFileListResponse extends CozeBaseResponse {

    @ApiModelProperty(value = "知识库文件列表")
    @JsonProperty("document_infos")
    private List<DocumentInfo> documentInfos;

    @ApiModelProperty(value = "指定知识库中的文件总数")
    private Integer total;

    @Data
    @ApiModel(value = "文档信息")
    public static class DocumentInfo {
        @ApiModelProperty(value = "文件内容的总字符数量")
        @JsonProperty("char_count")
        private Integer charCount;

        @ApiModelProperty(value = "分段规则")
        @JsonProperty("chunk_strategy")
        private ChunkStrategy chunkStrategy;

        @ApiModelProperty(value = "文件的上传时间，格式为10位的Unixtime时间戳")
        @JsonProperty("create_time")
        private Integer createTime;

        @ApiModelProperty(value = "文件的格式类型：0-文档类型，1-表格类型，2-照片类型")
        @JsonProperty("format_type")
        private Integer formatType;

        @ApiModelProperty(value = "被对话命中的次数")
        @JsonProperty("hit_count")
        private Integer hitCount;

        @ApiModelProperty(value = "文件的名称")
        private String name;

        @ApiModelProperty(value = "文件的大小，单位为字节")
        private Long size;

        @ApiModelProperty(value = "文件的分段数量")
        @JsonProperty("slice_count")
        private Integer sliceCount;

        @ApiModelProperty(value = "文件的上传方式：0-上传本地文件，1-上传在线网页")
        @JsonProperty("source_type")
        private Integer sourceType;

        @ApiModelProperty(value = "文件的处理状态：0-处理中，1-处理完毕，9-处理失败")
        private Integer status;

        @ApiModelProperty(value = "本地文件格式，即文件后缀")
        private String type;

        @ApiModelProperty(value = "在线网页自动更新的频率，单位为小时")
        @JsonProperty("update_interval")
        private Integer updateInterval;

        @ApiModelProperty(value = "文件的最近一次修改时间，格式为10位的Unixtime时间戳")
        @JsonProperty("update_time")
        private Integer updateTime;

        @ApiModelProperty(value = "在线网页是否自动更新：0-不自动更新，1-自动更新")
        @JsonProperty("update_type")
        private Integer updateType;

        @ApiModelProperty(value = "上传的本地文档的唯一标识")
        @JsonProperty("tos_uri")
        private String tosUri;

        @ApiModelProperty(value = "文件的ID")
        @JsonProperty("document_id")
        private String documentId;
        @ApiModelProperty
        @JsonProperty("web_url")
        private String webUrl;
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
