package com.zbkj.common.model.coze;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * Coze空间成员表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_coze_space_member")
@ApiModel(value = "CozeSpaceMember对象", description = "Coze空间成员")
public class CozeSpaceMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "商户ID")
    private Integer merchantId;

    @ApiModelProperty(value = "Coze空间ID")
    private String spaceId;

    @ApiModelProperty(value = "Coze用户ID")
    private String userId;

    @ApiModelProperty(value = "用户角色：admin-空间管理员，member-空间成员，owner-空间所有者")
    private String roleType;

    @ApiModelProperty(value = "用户昵称")
    private String userNickname;

    @ApiModelProperty(value = "用户名称")
    private String userUniqueName;

    @ApiModelProperty(value = "用户头像URL")
    private String avatarUrl;

    @ApiModelProperty(value = "状态：0-禁用，1-启用")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
