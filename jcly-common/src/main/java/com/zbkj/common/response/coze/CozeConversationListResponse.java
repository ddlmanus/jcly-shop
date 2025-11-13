package com.zbkj.common.response.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * Coze会话列表响应
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
@ApiModel(value = "CozeConversationListResponse", description = "Coze会话列表响应")
public class CozeConversationListResponse extends CozeBaseResponse {

    @ApiModelProperty(value = "会话列表数据")
    private ConversationListData data;

    @Data
    @ApiModel(value = "ConversationListData", description = "会话列表数据")
    public static class ConversationListData {
        
        @ApiModelProperty(value = "会话列表")
        private List<ConversationInfo> conversations;
        
        @ApiModelProperty(value = "总数")
        private Integer total;
        
        @ApiModelProperty(value = "是否有更多")
        @JsonProperty("has_more")
        private Boolean hasMore;
        
        @ApiModelProperty(value = "下一页标识")
        @JsonProperty("next_cursor")
        private String nextCursor;
    }

    @Data
    @ApiModel(value = "ConversationInfo", description = "会话信息")
    public static class ConversationInfo {
        
        @ApiModelProperty(value = "会话ID")
        private String id;
        
        @ApiModelProperty(value = "会话名称")
        private String name;
        
        @ApiModelProperty(value = "智能体ID")
        @JsonProperty("bot_id")
        private String botId;
        
        @ApiModelProperty(value = "创建者ID")
        @JsonProperty("creator_id")
        private String creatorId;
        
        @ApiModelProperty(value = "创建时间")
        @JsonProperty("created_at")
        private Long createdAt;
        
        @ApiModelProperty(value = "更新时间")
        @JsonProperty("updated_at")
        private Long updatedAt;
        
        @ApiModelProperty(value = "最后片段ID")
        @JsonProperty("last_section_id")
        private String lastSectionId;
        
        @ApiModelProperty(value = "渠道ID")
        @JsonProperty("connector_id")
        private String connectorId;
        
        @ApiModelProperty(value = "附加数据")
        @JsonProperty("meta_data")
        private Map<String, String> metaData;
    }
}
