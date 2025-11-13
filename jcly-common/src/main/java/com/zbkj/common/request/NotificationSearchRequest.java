package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;

/**
 * 系统通知查询对象
 *  +----------------------------------------------------------------------
 *  | JCLY [ JCLY赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 *  +----------------------------------------------------------------------
 *  | Author: dudl
 *  +----------------------------------------------------------------------
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="NotificationSearchRequest对象", description="系统通知查询对象")
public class NotificationSearchRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "发送类型（1：通知会员，2：通知平台）")
    @Range(min = 1, max = 2, message = "未知的发送类型")
    private Integer sendType;

}
