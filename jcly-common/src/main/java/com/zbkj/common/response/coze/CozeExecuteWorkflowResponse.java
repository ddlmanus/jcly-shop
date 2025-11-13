package com.zbkj.common.response.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Coze 执行工作流响应类
 * 根据 执行工作流.md 文档实现
 * 
 * @author Auto Generator
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CozeExecuteWorkflowResponse", description = "Coze 执行工作流响应")
public class CozeExecuteWorkflowResponse {

    @ApiModelProperty(value = "状态码", example = "0")
    private Long code;

    @ApiModelProperty(value = "状态信息", example = "Success")
    private String msg;

    @ApiModelProperty(value = "工作流执行结果，JSON序列化字符串", example = "{\"output\":\"北京的经度为116.4074°E，纬度为39.9042°N。\"}")
    private String data;

    @JsonProperty("execute_id")
    @ApiModelProperty(value = "异步执行的事件ID", example = "741364789030728****")
    private String executeId;

    @JsonProperty("debug_url")
    @ApiModelProperty(value = "工作流试运行调试页面", example = "https://www.coze.cn/work_flow?execute_id=741364789030728****")
    private String debugUrl;

    @ApiModelProperty(value = "资源使用情况")
    private Usage usage;

    @ApiModelProperty(value = "响应详情")
    private ResponseDetail detail;

    /**
     * 资源使用情况
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        
        @JsonProperty("input_count")
        @ApiModelProperty(value = "输入内容所消耗的Token数", example = "50")
        private Integer inputCount;

        @JsonProperty("output_count")
        @ApiModelProperty(value = "大模型输出内容所消耗的Token数", example = "100")
        private Integer outputCount;

        @JsonProperty("token_count")
        @ApiModelProperty(value = "本次API调用消耗的Token总量", example = "150")
        private Integer tokenCount;
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
