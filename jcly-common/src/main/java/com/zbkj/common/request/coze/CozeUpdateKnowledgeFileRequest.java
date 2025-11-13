package com.zbkj.common.request.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * Coze 修改知识库文件请求类
 * 根据 修改知识库文件.md 文档实现
 * 
 * @author Auto Generator
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CozeUpdateKnowledgeFileRequest", description = "Coze 修改知识库文件请求")
public class CozeUpdateKnowledgeFileRequest {

    @JsonProperty("document_id")
    @ApiModelProperty(value = "待修改的知识库文件ID", required = true, example = "738694205603010****")
    @NotBlank(message = "文件ID不能为空")
    private String documentId;

    @JsonProperty("document_name")
    @ApiModelProperty(value = "知识库文件的新名称", example = "cozeoverview")
    private String documentName;

    @JsonProperty("update_rule")
    @ApiModelProperty(value = "在线网页的更新配置")
    private UpdateRule updateRule;

    /**
     * 更新规则
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRule {
        
        @JsonProperty("update_type")
        @ApiModelProperty(value = "在线网页是否自动更新", example = "1", notes = "0：（默认）不自动更新，1：自动更新")
        private Integer updateType;

        @JsonProperty("update_interval")
        @ApiModelProperty(value = "在线网页自动更新的频率", example = "24", notes = "单位为小时，最小值为 24")
        private Integer updateInterval;
    }
}
