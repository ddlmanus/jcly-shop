package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 会员消息分页查询请求对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MemberMessagePageRequest对象", description="会员消息分页查询请求对象")
public class MemberMessagePageRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "页码", required = true, example = "1")
    private Integer page = 1;

    @ApiModelProperty(value = "每页数量", required = true, example = "20")
    private Integer limit = 20;

    @ApiModelProperty(value = "会员ID")
    private Integer memberId;

    @ApiModelProperty(value = "消息类型：1-店铺公告，2-优惠通知，3-其他")
    private Integer messageType;

    @ApiModelProperty(value = "是否已读：0-未读，1-已读")
    private Boolean isRead;

    @ApiModelProperty(value = "关键词搜索")
    private String keywords;

    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @ApiModelProperty(value = "结束时间")
    private String endTime;
}
