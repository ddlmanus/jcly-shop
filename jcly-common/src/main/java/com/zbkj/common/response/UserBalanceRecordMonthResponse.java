package com.zbkj.common.response;

import com.zbkj.common.model.user.UserBalanceRecord;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 用户余额账单记录月份响应对象
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
@ApiModel(value = "UserBalanceRecordMonthResponse对象", description = "用户余额账单记录月份响应对象")
public class UserBalanceRecordMonthResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "月份")
    private String month;

    @ApiModelProperty(value = "数据")
    private List<UserBalanceRecord> list;
}
