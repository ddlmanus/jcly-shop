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
 * Coze 空间实体类
 * 用于存储从Coze API获取的空间信息到本地数据库
 * 
 * @author Auto Generator
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_coze_space")
@ApiModel(value = "CozeSpace对象", description = "Coze 空间")
public class CozeSpace implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "Coze空间ID")
    private String spaceId;

    @ApiModelProperty(value = "空间名称")
    private String name;

    @ApiModelProperty(value = "空间图标URL")
    private String iconUrl;

    @ApiModelProperty(value = "空间所有者的扣子用户ID")
    private String ownerUid;

    @ApiModelProperty(value = "用户在空间中的角色：owner（所有者），admin（管理员），member（成员）")
    private String roleType;

    @ApiModelProperty(value = "空间的描述信息")
    private String description;

    @ApiModelProperty(value = "企业ID")
    private String enterpriseId;

    @ApiModelProperty(value = "用户在空间中的加入状态：joined（已加入）")
    private String joinedStatus;

    @ApiModelProperty(value = "空间类型：personal（个人空间），team（工作空间）")
    private String workspaceType;

    @ApiModelProperty(value = "状态：0=禁用，1=启用")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}