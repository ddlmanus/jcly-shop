package com.zbkj.common.model.merchant;

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
 * <p>
 * 商户公告表
 * </p>
 *
 * @author SOLO Coding
 * @since 2024-01-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_merchant_announcement")
@ApiModel(value = "MerchantAnnouncement对象", description = "商户公告表")
public class MerchantAnnouncement implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "公告标题")
    private String title;

    @ApiModelProperty(value = "公告内容")
    private String content;

    @ApiModelProperty(value = "公告图片，JSON格式")
    private String images;

    @ApiModelProperty(value = "目标用户类型：1-全部用户，2-会员用户，3-关注用户")
    private Integer targetType;

    @ApiModelProperty(value = "公告状态：0-草稿，1-已发布，2-已撤回")
    private Integer status;

    @ApiModelProperty(value = "发送时间")
    private Date sendTime;

    @ApiModelProperty(value = "发布时间")
    private Date publishTime;

    @ApiModelProperty(value = "公告类型：1-系统公告，2-活动公告，3-促销公告")
    private Integer type;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "是否删除：0-否，1-是")
    private Boolean isDelete;
}