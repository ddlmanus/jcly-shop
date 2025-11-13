package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 人工客服消息响应对象
 * +----------------------------------------------------------------------
 * | JCLY [ JCLY赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 * +----------------------------------------------------------------------
 * | Author: dudl
 * +----------------------------------------------------------------------
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "HumanServiceMessageResponse对象", description = "人工客服消息响应")
public class HumanServiceMessageResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("消息ID")
    private String messageId;

    @ApiModelProperty("会话ID")
    private String sessionId;

    @ApiModelProperty("发送者ID")
    private Integer senderId;

    @ApiModelProperty("发送者类型（USER-用户, STAFF-客服, SYSTEM-系统）")
    private String senderType;

    @ApiModelProperty("发送者名称")
    private String senderName;

    @ApiModelProperty("发送者头像")
    private String senderAvatar;

    @ApiModelProperty("接收者ID")
    private Integer receiverId;

    @ApiModelProperty("接收者类型（USER-用户, STAFF-客服, MERCHANT-商户, PLATFORM-平台）")
    private String receiverType;

    @ApiModelProperty("消息类型（TEXT-文本, IMAGE-图片, FILE-文件, ORDER_LINK-订单链接, PRODUCT_LINK-商品链接, SYSTEM-系统消息）")
    private String messageType;

    @ApiModelProperty("消息内容")
    private String content;

    @ApiModelProperty("内容格式（TEXT-纯文本, HTML-富文本, JSON-结构化数据, URL-链接）")
    private String contentFormat;

    @ApiModelProperty("扩展信息（JSON格式）")
    private String extraData;

    @ApiModelProperty("是否已读")
    private Boolean isRead;

    @ApiModelProperty("消息状态（SENDING-发送中, SENT-已发送, DELIVERED-已送达, READ-已读, FAILED-发送失败）")
    private String messageStatus;

    @ApiModelProperty("发送时间")
    private Date sendTime;

    @ApiModelProperty("送达时间")
    private Date deliveredTime;

    @ApiModelProperty("阅读时间")
    private Date readTime;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("引用消息ID（回复消息时使用）")
    private String replyToMessageId;

    @ApiModelProperty("引用消息内容")
    private String replyToContent;

    // ==================== 富媒体消息扩展字段 ====================

    @ApiModelProperty("图片URL（仅图片消息）")
    private String imageUrl;

    @ApiModelProperty("图片缩略图URL（仅图片消息）")
    private String thumbnailUrl;

    @ApiModelProperty("文件名（仅文件消息）")
    private String fileName;

    @ApiModelProperty("文件大小（仅文件消息）")
    private Long fileSize;

    @ApiModelProperty("文件类型（仅文件消息）")
    private String fileType;

    @ApiModelProperty("文件下载URL（仅文件消息）")
    private String fileUrl;

    @ApiModelProperty("商品ID（仅商品链接消息）")
    private Integer productId;

    @ApiModelProperty("商品名称（仅商品链接消息）")
    private String productName;

    @ApiModelProperty("商品价格（仅商品链接消息）")
    private String productPrice;

    @ApiModelProperty("商品图片（仅商品链接消息）")
    private String productImage;

    @ApiModelProperty("商品库存（仅商品链接消息）")
    private Integer productStock;

    @ApiModelProperty("商品链接（仅商品链接消息）")
    private String productUrl;

    @ApiModelProperty("订单号（仅订单链接消息）")
    private String orderNo;

    @ApiModelProperty("订单金额（仅订单链接消息）")
    private String orderAmount;

    @ApiModelProperty("订单状态（仅订单链接消息）")
    private String orderStatus;

    @ApiModelProperty("订单创建时间（仅订单链接消息）")
    private Date orderCreateTime;

    @ApiModelProperty("订单链接（仅订单链接消息）")
    private String orderUrl;
}
