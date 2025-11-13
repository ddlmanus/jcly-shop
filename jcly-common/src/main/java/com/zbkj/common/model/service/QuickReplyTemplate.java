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
 * 快捷回复模板表
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_quick_reply_template")
@ApiModel(value = "QuickReplyTemplate对象", description = "快捷回复模板表")
public class QuickReplyTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商户ID")
    @TableField("mer_id")
    private Integer merId;

    @ApiModelProperty(value = "客服ID，为空表示通用模板")
    @TableField("staff_id")
    private Integer staffId;

    @ApiModelProperty(value = "分类")
    @TableField("category")
    private String category;

    @ApiModelProperty(value = "模板标题")
    @TableField("title")
    private String title;

    @ApiModelProperty(value = "模板内容")
    @TableField("content")
    private String content;

    @ApiModelProperty(value = "快捷键")
    @TableField("shortcut_key")
    private String shortcutKey;

    @ApiModelProperty(value = "使用次数")
    @TableField("usage_count")
    private Integer usageCount;

    @ApiModelProperty(value = "排序")
    @TableField("sort_order")
    private Integer sortOrder;

    @ApiModelProperty(value = "状态：0-禁用，1-启用")
    @TableField("status")
    private Boolean status;

    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField("update_time")
    private Date updateTime;

    // 分类常量
    public static final String CATEGORY_GREETING = "问候语";
    public static final String CATEGORY_COMMON = "常用语";
    public static final String CATEGORY_ENDING = "结束语";
    public static final String CATEGORY_PRODUCT = "商品相关";
    public static final String CATEGORY_ORDER = "订单相关";
    public static final String CATEGORY_AFTER_SALE = "售后相关";

    // 状态常量
    public static final Boolean STATUS_DISABLED = false;
    public static final Boolean STATUS_ENABLED = true;
}
