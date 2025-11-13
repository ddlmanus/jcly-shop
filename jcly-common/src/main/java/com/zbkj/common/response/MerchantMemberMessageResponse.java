package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 商户会员消息响应对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MerchantMemberMessageResponse对象", description="商户会员消息响应对象")
public class MerchantMemberMessageResponse implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "消息ID")
    private Integer id;

    @ApiModelProperty(value = "商户ID")
    private Integer merId;

    @ApiModelProperty(value = "用户ID")
    private Integer uid;

    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @ApiModelProperty(value = "商户头像")
    private String merchantAvatar;

    @ApiModelProperty(value = "消息内容(富文本)")
    private String content;

    @ApiModelProperty(value = "消息类型：1-店铺公告，2-优惠通知，3-其他")
    private Integer messageType;

    @ApiModelProperty(value = "消息类型名称")
    private String messageTypeName;

    @ApiModelProperty(value = "是否已读：0-未读，1-已读")
    private Boolean isRead;

    @ApiModelProperty(value = "会员昵称")
    private String memberNickname;

    @ApiModelProperty(value = "会员头像")
    private String memberAvatar;

    @ApiModelProperty(value = "会员手机号")
    private String memberPhone;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
