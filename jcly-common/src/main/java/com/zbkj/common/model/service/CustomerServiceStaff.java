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
import java.math.BigDecimal;
import java.util.Date;

/**
 * 人工客服员工表
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_customer_service_staff")
@ApiModel(value = "CustomerServiceStaff对象", description = "人工客服员工表")
public class CustomerServiceStaff implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "管理员ID")
    @TableField("admin_id")
    private Integer adminId;

    @ApiModelProperty(value = "商户ID")
    @TableField("mer_id")
    private Integer merId;

    @ApiModelProperty(value = "客服工号")
    @TableField("staff_no")
    private String staffNo;

    @ApiModelProperty(value = "客服姓名")
    @TableField("staff_name")
    private String staffName;

    @ApiModelProperty(value = "账号")
    @TableField("account")
    private String account;

    @ApiModelProperty(value = "手机号")
    @TableField("phone")
    private String phone;

    @ApiModelProperty(value = "邮箱")
    @TableField("email")
    private String email;

    @ApiModelProperty(value = "客服头像")
    @TableField("avatar")
    private String avatar;

    @ApiModelProperty(value = "服务等级：JUNIOR-初级，STANDARD-标准，SENIOR-高级，EXPERT-专家")
    @TableField("service_level")
    private String serviceLevel;

    @ApiModelProperty(value = "技能标签，JSON格式")
    @TableField("skill_tags")
    private String skillTags;

    @ApiModelProperty(value = "最大并发会话数")
    @TableField("max_concurrent_sessions")
    private Integer maxConcurrentSessions;

    @ApiModelProperty(value = "当前会话数")
    @TableField("current_sessions")
    private Integer currentSessions;

    @ApiModelProperty(value = "在线状态：ONLINE-在线，BUSY-忙碌，AWAY-离开，OFFLINE-离线")
    @TableField("online_status")
    private String onlineStatus;

    @ApiModelProperty(value = "最后在线时间")
    @TableField("last_online_time")
    private Date lastOnlineTime;

    @ApiModelProperty(value = "总服务会话数")
    @TableField("total_served_sessions")
    private Integer totalServedSessions;

    @ApiModelProperty(value = "平均响应时间（秒）")
    @TableField("average_response_time")
    private Integer averageResponseTime;

    @ApiModelProperty(value = "客户满意度评分")
    @TableField("satisfaction_rating")
    private BigDecimal satisfactionRating;

    @ApiModelProperty(value = "状态：0-禁用，1-启用")
    @TableField("status")
    private Boolean status;

    @ApiModelProperty(value = "是否默认客服：0-否，1-是")
    @TableField("is_default")
    private Boolean isDefault;

    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField("update_time")
    private Date updateTime;

    // 服务等级常量
    public static final String SERVICE_LEVEL_JUNIOR = "JUNIOR";
    public static final String SERVICE_LEVEL_STANDARD = "STANDARD";
    public static final String SERVICE_LEVEL_SENIOR = "SENIOR";
    public static final String SERVICE_LEVEL_EXPERT = "EXPERT";

    // 在线状态常量
    public static final String ONLINE_STATUS_ONLINE = "ONLINE";
    public static final String ONLINE_STATUS_BUSY = "BUSY";
    public static final String ONLINE_STATUS_AWAY = "AWAY";
    public static final String ONLINE_STATUS_OFFLINE = "OFFLINE";

    // 状态常量
    public static final Boolean STATUS_DISABLED = false;
    public static final Boolean STATUS_ENABLED = true;
}
