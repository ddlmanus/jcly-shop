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
@ApiModel(value = "CozeGetSpaceListResponse对象", description = "查看空间列表响应")
public class CozeGetSpaceListResponse extends CozeBaseResponse {
    
    @ApiModelProperty(value = "空间列表的详细信息")
    private OpenSpaceData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(value = "OpenSpaceData对象", description = "空间数据")
    public static class OpenSpaceData {
        @ApiModelProperty(value = "用户创建或加入的空间列表")
        private List<OpenSpace> workspaces;
        
        @ApiModelProperty(value = "用户加入的空间总数")
        @JsonProperty("total_count")
        private Long totalCount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(value = "OpenSpace对象", description = "空间信息")
    public static class OpenSpace {
        @ApiModelProperty(value = "空间 ID")
        private String id;
        
        @ApiModelProperty(value = "空间名称")
        private String name;
        
        @ApiModelProperty(value = "空间图标的 url 地址")
        @JsonProperty("icon_url")
        private String iconUrl;
        
        @ApiModelProperty(value = "空间所有者的扣子用户 ID")
        @JsonProperty("owner_uid")
        private String ownerUid;
        
        @ApiModelProperty(value = "用户在空间中的角色：owner（所有者），admin（管理员），member（成员）")
        @JsonProperty("role_type")
        private String roleType;
        
        @ApiModelProperty(value = "空间管理员的用户 ID 列表")
        @JsonProperty("admin_uids")
        private List<String> adminUids;
        
        @ApiModelProperty(value = "空间的描述信息")
        private String description;
        
        @ApiModelProperty(value = "企业 ID")
        @JsonProperty("enterprise_id")
        private String enterpriseId;
        
        @ApiModelProperty(value = "用户在空间中的加入状态：joined（已加入）")
        @JsonProperty("joined_status")
        private String joinedStatus;
        
        @ApiModelProperty(value = "空间类型：personal（个人空间），team（工作空间）")
        @JsonProperty("workspace_type")
        private String workspaceType;
    }
}
