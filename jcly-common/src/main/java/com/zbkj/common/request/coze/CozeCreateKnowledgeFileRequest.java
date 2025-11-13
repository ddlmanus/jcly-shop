package com.zbkj.common.request.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Coze 创建知识库文件请求类
 * 根据 创建知识库文件.md 文档实现
 * 
 * @author Auto Generator
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CozeCreateKnowledgeFileRequest", description = "Coze 创建知识库文件请求")
public class CozeCreateKnowledgeFileRequest {

    @JsonProperty("dataset_id")
    @ApiModelProperty(value = "知识库ID", required = true, example = "744258581358768****")
    @NotBlank(message = "知识库ID不能为空")
    private String datasetId;

    @JsonProperty("document_bases")
    @ApiModelProperty(value = "文档信息列表", required = true)
    @NotNull(message = "文档信息不能为空")
    @Size(min = 1, message = "至少需要一个文档")
    private List<DocumentBase> documentBases;

    @JsonProperty("chunk_strategy")
    @ApiModelProperty(value = "切片策略")
    private ChunkStrategy chunkStrategy;

    @JsonProperty("format_type")
    @ApiModelProperty(value = "知识库类型", example = "0")
    private Integer formatType;

    /**
     * 文档基础信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentBase {
        
        @JsonProperty("name")
        @ApiModelProperty(value = "文档名称", required = true, example = "test.txt")
        @NotBlank(message = "文档名称不能为空")
        private String name;

        @JsonProperty("source_info")
        @ApiModelProperty(value = "文档来源信息", required = true)
        @NotNull(message = "文档来源信息不能为空")
        private SourceInfo sourceInfo;

        @JsonProperty("update_rule")
        @ApiModelProperty(value = "更新规则")
        private UpdateRule updateRule;

        @JsonProperty("caption")
        @ApiModelProperty(value = "图片描述")
        private String caption;
    }

    /**
     * 文档来源信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceInfo {
        
        @JsonProperty("file_base64")
        @ApiModelProperty(value = "文件Base64编码(本地文件上传时必选)")
        private String fileBase64;

        @JsonProperty("file_type")
        @ApiModelProperty(value = "文件类型(本地文件上传时必选)", example = "txt")
        private String fileType;

        @JsonProperty("web_url")
        @ApiModelProperty(value = "网页URL")
        private String webUrl;

        @JsonProperty("document_source")
        @ApiModelProperty(value = "文档来源", example = "0")
        private Integer documentSource;

        @JsonProperty("source_file_id")
        @ApiModelProperty(value = "源文件ID")
        private String sourceFileId;
    }

    /**
     * 切片策略
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChunkStrategy {
        
        @JsonProperty("chunk_type")
        @ApiModelProperty(value = "分段设置", example = "0")
        private Integer chunkType;

        @JsonProperty("separator")
        @ApiModelProperty(value = "分段标识符")
        private String separator;

        @JsonProperty("max_tokens")
        @ApiModelProperty(value = "最大分段长度", example = "800")
        private Long maxTokens;

        @JsonProperty("remove_extra_spaces")
        @ApiModelProperty(value = "是否自动过滤连续空格", example = "true")
        private Boolean removeExtraSpaces;

        @JsonProperty("remove_urls_emails")
        @ApiModelProperty(value = "是否自动过滤URL和邮箱", example = "true")
        private Boolean removeUrlsEmails;

        @JsonProperty("caption_type")
        @ApiModelProperty(value = "图片标注方式", example = "0")
        private Integer captionType;
    }

    /**
     * 更新规则
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRule {
        
        @JsonProperty("update_type")
        @ApiModelProperty(value = "是否自动更新", example = "0")
        private Integer updateType;

        @JsonProperty("update_interval")
        @ApiModelProperty(value = "自动更新频率(小时)", example = "24")
        private Integer updateInterval;
    }
}
