package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 会员发送消息请求对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MemberSendMessageRequest对象", description="会员发送消息请求对象")
public class MemberSendMessageRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "消息内容(富文本)", required = true)
    @NotBlank(message = "消息内容不能为空")
    private String content;

    @ApiModelProperty(value = "消息类型：1-店铺公告，2-优惠通知，3-其他", required = true)
    @NotNull(message = "消息类型不能为空")
    private Integer messageType;

    @ApiModelProperty(value = "会员ID列表", required = true)
    @NotEmpty(message = "会员ID列表不能为空")
    private List<Integer> memberIds;
}
