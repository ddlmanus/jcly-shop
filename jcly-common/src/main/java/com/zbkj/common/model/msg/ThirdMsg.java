package com.zbkj.common.model.msg;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_third_msg")
@ApiModel(value = "第三方数据推送对象", description = "第三方数据推送对象表")
public class ThirdMsg {
    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "消息id")
    private String id;

    @ApiModelProperty(value = "消息类型")
    private String eventType;

    @ApiModelProperty(value = "推送方式")
    private String actionType;

    @ApiModelProperty(value = "推送数据")
    private String msgContent;

    @ApiModelProperty(value = "推送时间")
    private String createTime;
}
