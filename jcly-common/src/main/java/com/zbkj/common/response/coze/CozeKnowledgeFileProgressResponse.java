package com.zbkj.common.response.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Coze查看知识库文件上传进度响应
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "CozeKnowledgeFileProgressResponse对象", description = "Coze查看知识库文件上传进度响应")
public class CozeKnowledgeFileProgressResponse extends CozeBaseResponse {

    @ApiModelProperty(value = "文件上传进度数据")
    private GetDocumentProgressOpenApiData data;

    @Data
    @ApiModel(value = "文件上传进度数据")
    public static class GetDocumentProgressOpenApiData {
        @ApiModelProperty(value = "文件进度详情列表")
        private List<DocumentProgress> data;
    }

    @Data
    @ApiModel(value = "文件进度详情")
    public static class DocumentProgress {
        @ApiModelProperty(value = "文件地址")
        private String url;

        @ApiModelProperty(value = "文件大小(字节)")
        private Long size;

        @ApiModelProperty(value = "文件类型")
        private String type;

        @ApiModelProperty(value = "处理状态：0-处理中，1-处理完毕，9-处理失败")
        private Integer status;

        @ApiModelProperty(value = "上传进度(百分比)")
        private Integer progress;

        @ApiModelProperty(value = "文件ID")
        @JsonProperty("document_id")
        private String documentId;

        @ApiModelProperty(value = "是否自动更新：0-不自动更新，1-自动更新")
        @JsonProperty("update_type")
        private Integer updateType;

        @ApiModelProperty(value = "文件名称")
        @JsonProperty("document_name")
        private String documentName;

        @ApiModelProperty(value = "预期剩余时间(秒)")
        @JsonProperty("remaining_time")
        private Long remainingTime;

        @ApiModelProperty(value = "失败状态的详细描述")
        @JsonProperty("status_descript")
        private String statusDescript;

        @ApiModelProperty(value = "自动更新频率(小时)")
        @JsonProperty("update_interval")
        private Integer updateInterval;
    }
}
