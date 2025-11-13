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
 * 公告发送记录表
 * </p>
 *
 * @author SOLO Coding
 * @since 2024-01-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_announcement_send_record")
@ApiModel(value = "AnnouncementSendRecord对象", description = "公告发送记录表")
public class AnnouncementSendRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "公告ID")
    private Integer announcementId;
    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "用户ID")
    private Integer userId;

    @ApiModelProperty(value = "发送状态：0-待发送，1-发送成功，2-发送失败")
    private Integer sendStatus;

    @ApiModelProperty(value = "发送时间")
    private Date sendTime;

    @ApiModelProperty(value = "阅读时间")
    private Date readTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}