package com.zbkj.common.response.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Coze获取空间成员列表响应参数
 * @author: auto-generated
 * @date: 2024/01/01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "CozeGetSpaceMembersResponse", description = "Coze获取空间成员列表响应参数")
public class CozeGetSpaceMembersResponse extends CozeBaseResponse {

    @ApiModelProperty(value = "空间成员数据")
    private OpenSpaceMemberListData data;

    @Data
    @ApiModel(value = "OpenSpaceMemberListData", description = "空间成员列表数据")
    public static class OpenSpaceMemberListData {

        @ApiModelProperty(value = "成员列表")
        private List<OpenSpaceMember> items;

        @ApiModelProperty(value = "成员总数")
        @JsonProperty("total_count")
        private Long totalCount;
    }

    @Data
    @ApiModel(value = "OpenSpaceMember", description = "空间成员信息")
    public static class OpenSpaceMember {

        @ApiModelProperty(value = "用户的扣子用户ID")
        @JsonProperty("user_id")
        private String userId;

        @ApiModelProperty(value = "该用户在空间中的角色：admin-空间管理员，member-空间成员，owner-空间所有者")
        @JsonProperty("role_type")
        private String roleType;

        @ApiModelProperty(value = "用户的昵称")
        @JsonProperty("user_nickname")
        private String userNickname;

        @ApiModelProperty(value = "用户的名称")
        @JsonProperty("user_unique_name")
        private String userUniqueName;

        @ApiModelProperty(value = "用户头像URL")
        @JsonProperty("avatar_url")
        private String avatarUrl;
    }
}
