package com.zbkj.common.response.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Coze智能体列表响应
 * +----------------------------------------------------------------------
 * | JCLY [ JCLY赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 * +----------------------------------------------------------------------
 * | Author: dudl
 * +----------------------------------------------------------------------
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "CozeBotListResponse", description = "Coze智能体列表响应")
public class CozeBotListResponse extends CozeBaseResponse {

    @ApiModelProperty(value = "智能体列表数据")
    private BotListData data;

    @Data
    @ApiModel(value = "BotListData", description = "智能体列表数据")
    public static class BotListData {
        
        @ApiModelProperty(value = "智能体列表")
        private List<BotInfo> items;
        
        @ApiModelProperty(value = "总数")
        private Integer total;
        
        @ApiModelProperty(value = "当前页码")
        @JsonProperty("page_num")
        private Integer pageNum;
        
        @ApiModelProperty(value = "每页大小")
        @JsonProperty("page_size")
        private Integer pageSize;
        
        @ApiModelProperty(value = "总页数")
        @JsonProperty("total_pages")
        private Integer totalPages;
        
        @ApiModelProperty(value = "是否有下一页")
        @JsonProperty("has_more")
        private Boolean hasMore;
    }

    @Data
    @ApiModel(value = "BotInfo", description = "智能体信息")
    public static class BotInfo {
        
        @ApiModelProperty(value = "智能体ID")
        @JsonProperty("id")
        private String botId;
        
        @ApiModelProperty(value = "智能体名称")
        private String name;
        
        @ApiModelProperty(value = "智能体描述")
        private String description;
        
        @ApiModelProperty(value = "智能体头像URL")
        @JsonProperty("icon_url")
        private String iconUrl;
        
        @ApiModelProperty(value = "创建时间")
        @JsonProperty("create_time")
        private Long createTime;
        
        @ApiModelProperty(value = "更新时间")
        @JsonProperty("update_time")
        private Long updateTime;
        
        @ApiModelProperty(value = "发布状态")
        @JsonProperty("is_published")
        private boolean publishStatus;
        
        @ApiModelProperty(value = "版本")
        private String version;
        
        @ApiModelProperty(value = "空间ID")
        @JsonProperty("space_id") 
        private String spaceId;
        
        @ApiModelProperty(value = "模型ID")
        @JsonProperty("model_id")
        private String modelId;
        
        @ApiModelProperty(value = "提示词信息")
        @JsonProperty("prompt_info")
        private PromptInfo promptInfo;
        
        @ApiModelProperty(value = "开场白信息")
        @JsonProperty("onboarding_info")
        private OnboardingInfo onboardingInfo;
    }

    @Data
    @ApiModel(value = "PromptInfo", description = "提示词信息")
    public static class PromptInfo {
        @ApiModelProperty(value = "提示词内容")
        private String prompt;
    }

    @Data
    @ApiModel(value = "OnboardingInfo", description = "开场白信息")
    public static class OnboardingInfo {
        @ApiModelProperty(value = "开场白")
        private String prologue;
        
        @ApiModelProperty(value = "建议问题")
        @JsonProperty("suggested_questions")
        private List<String> suggestedQuestions;
    }
}
