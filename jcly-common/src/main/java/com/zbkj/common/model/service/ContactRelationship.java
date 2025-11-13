package com.zbkj.common.model.service;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * 联系人关系表
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_contact_relationship")
@ApiModel(value = "ContactRelationship对象", description = "联系人关系表")
public class ContactRelationship implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "拥有者ID（商户或管理员）")
    @TableField("owner_id")
    private Integer ownerId;

    @ApiModelProperty(value = "拥有者类型：MERCHANT-商户，ADMIN-管理员")
    @TableField("owner_type")
    private String ownerType;

    @ApiModelProperty(value = "联系人ID")
    @TableField("contact_id")
    private Integer contactId;

    @ApiModelProperty(value = "联系人类型：USER-用户，MERCHANT-商户，PLATFORM-平台")
    @TableField("contact_type")
    private String contactType;

    @ApiModelProperty(value = "联系人名称")
    @TableField("contact_name")
    private String contactName;

    @ApiModelProperty(value = "联系人头像")
    @TableField("contact_avatar")
    private String contactAvatar;

    @ApiModelProperty(value = "联系人电话")
    @TableField("contact_phone")
    private String contactPhone;

    @ApiModelProperty(value = "备注信息")
    @TableField("notes")
    private String notes;

    @ApiModelProperty(value = "分组名称")
    @TableField("group_name")
    private String groupName;

    @ApiModelProperty(value = "是否置顶")
    @TableField("is_pinned")
    private Boolean isPinned;

    @ApiModelProperty(value = "最后联系时间")
    @TableField("last_contact_time")
    private Date lastContactTime;

    @ApiModelProperty(value = "状态：0-已删除，1-正常")
    @TableField("status")
    private Boolean status;

    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField("update_time")
    private Date updateTime;

    // 拥有者类型常量
    public static final String OWNER_TYPE_MERCHANT = "MERCHANT";
    public static final String OWNER_TYPE_ADMIN = "ADMIN";
    public static final String OWNER_TYPE_PLATFORM = "PLATFORM";

    // 联系人类型常量
    public static final String CONTACT_TYPE_USER = "USER";
    public static final String CONTACT_TYPE_MERCHANT = "MERCHANT";
    public static final String CONTACT_TYPE_PLATFORM = "PLATFORM";

    // 默认分组
    public static final String DEFAULT_GROUP = "DEFAULT";

    // 状态常量
    public static final Boolean STATUS_DELETED = false;
    public static final Boolean STATUS_NORMAL = true;
}
