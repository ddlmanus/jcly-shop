package com.zbkj.common.response.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CozeGetConversationListResponse对象", description = "查看会话列表响应")
public class CozeGetConversationListResponse extends CozeBaseResponse {
    
    @ApiModelProperty(value = "分页的会话列表信息")
    private ConversationListData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(value = "ConversationListData对象", description = "会话列表数据")
    public static class ConversationListData {
        @ApiModelProperty(value = "是否还有更多会话未在本次请求中返回")
        @JsonProperty("has_more")
        private Boolean hasMore;
        
        @ApiModelProperty(value = "会话的详细信息列表")
        private List<ConversationInfo> conversations;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(value = "ConversationInfo对象", description = "会话信息")
    public static class ConversationInfo {
        @ApiModelProperty(value = "会话的唯一标识")
        private String id;
        
        @ApiModelProperty(value = "会话的创建时间，格式为 10 位的 Unixtime 时间戳，单位为秒")
        @JsonProperty("created_at")
        private Long createdAt;
        
        @ApiModelProperty(value = "创建会话时的附加信息")
        @JsonProperty("meta_data")
        private Object metaData;
        
        @ApiModelProperty(value = "会话中最新的一个上下文片段 ID")
        @JsonProperty("last_section_id")
        private String lastSectionId;
    }
}
