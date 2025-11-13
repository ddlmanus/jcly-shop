package com.zbkj.common.response.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Coze 创建知识库响应类
 * 根据 创建知识库.md 文档实现
 * 
 * @author Auto Generator
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "CozeCreateKnowledgeResponse", description = "Coze 创建知识库响应")
public class CozeCreateKnowledgeResponse extends CozeBaseResponse {

    @ApiModelProperty(value = "返回内容")
    private CreateDatasetOpenApiData data;

    /**
     * 创建知识库返回数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDatasetOpenApiData {
        
        @JsonProperty("dataset_id")
        @ApiModelProperty(value = "新知识库的 ID", example = "744668935865830****")
        private String datasetId;
    }
}
