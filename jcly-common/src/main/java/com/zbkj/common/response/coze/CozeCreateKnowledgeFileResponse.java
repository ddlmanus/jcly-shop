package com.zbkj.common.response.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Coze 创建知识库文件响应类
 * 根据 创建知识库文件.md 文档实现
 * 
 * @author Auto Generator
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CozeCreateKnowledgeFileResponse", description = "Coze 创建知识库文件响应")
public class CozeCreateKnowledgeFileResponse {

    @ApiModelProperty(value = "状态码", example = "0")
    private Long code;

    @ApiModelProperty(value = "状态信息", example = "Success")
    private String msg;

    @JsonProperty("document_infos")
    @ApiModelProperty(value = "文档信息列表")
    private List<DocumentInfo> documentInfos;

    @ApiModelProperty(value = "响应详情")
    private ResponseDetail detail;

    /**
     * 文档信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentInfo {
        
        @JsonProperty("char_count")
        @ApiModelProperty(value = "文件内容的总字符数量", example = "4")
        private Integer charCount;
        
        @JsonProperty("chunk_strategy")
        @ApiModelProperty(value = "分段规则")
        private ChunkStrategy chunkStrategy;
        
        @JsonProperty("create_time")
        @ApiModelProperty(value = "文件的上传时间，格式为 10 位的 Unixtime 时间戳", example = "1719907964")
        private Integer createTime;
        
        @JsonProperty("document_id")
        @ApiModelProperty(value = "文件的 ID", example = "738694205603010****")
        private String documentId;
        
        @JsonProperty("format_type")
        @ApiModelProperty(value = "文件的格式类型：0（文档），1（表格），2（照片）", example = "0")
        private Integer formatType;
        
        @JsonProperty("hit_count")
        @ApiModelProperty(value = "命中次数", example = "0")
        private Integer hitCount;
        
        @JsonProperty("name")
        @ApiModelProperty(value = "文件名称", example = "Coze.pdf")
        private String name;
        
        @JsonProperty("size")
        @ApiModelProperty(value = "文件大小（字节）", example = "1024")
        private Long size;
        
        @JsonProperty("slice_count")
        @ApiModelProperty(value = "分段数量", example = "1")
        private Integer sliceCount;
        
        @JsonProperty("source_info")
        @ApiModelProperty(value = "文件的元数据信息")
        private SourceInfo sourceInfo;
        
        @JsonProperty("status")
        @ApiModelProperty(value = "文件状态：9（处理中），0（成功），7（失败）", example = "9")
        private Integer status;
        
        @JsonProperty("type")
        @ApiModelProperty(value = "文件类型", example = "pdf")
        private String type;
        
        @JsonProperty("update_time")
        @ApiModelProperty(value = "文件的更新时间，格式为 10 位的 Unixtime 时间戳", example = "1719907964")
        private Integer updateTime;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChunkStrategy {
        @JsonProperty("chunk_type")
        @ApiModelProperty(value = "分段设置：0（自动分段），1（自定义）", example = "1")
        private Integer chunkType;
        
        @JsonProperty("max_tokens")
        @ApiModelProperty(value = "最大分段长度", example = "800")
        private Long maxTokens;
        
        @JsonProperty("remove_extra_spaces")
        @ApiModelProperty(value = "是否自动过滤连续的空格", example = "false")
        private Boolean removeExtraSpaces;
        
        @JsonProperty("remove_urls_emails")
        @ApiModelProperty(value = "是否自动过滤所有 URL 和电子邮箱地址", example = "false")
        private Boolean removeUrlsEmails;
        
        @JsonProperty("separator")
        @ApiModelProperty(value = "分段标识符", example = "#")
        private String separator;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceInfo {
        @JsonProperty("file_base64")
        @ApiModelProperty(value = "Base64编码的文件内容")
        private String fileBase64;
        
        @JsonProperty("file_type")
        @ApiModelProperty(value = "文件类型", example = "pdf")
        private String fileType;
        
        @JsonProperty("web_url")
        @ApiModelProperty(value = "在线网页URL")
        private String webUrl;
        
        @JsonProperty("document_source")
        @ApiModelProperty(value = "文档来源")
        private Integer documentSource;
    }

    /**
     * 响应详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseDetail {
        
        @ApiModelProperty(value = "日志ID", example = "20241210152726467C48D89D6DB2****")
        private String logid;
    }
}
