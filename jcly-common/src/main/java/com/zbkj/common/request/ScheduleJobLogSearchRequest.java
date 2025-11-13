package com.zbkj.common.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 定时任务日志搜索请求参数
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
@ApiModel(value = "ScheduleJobLogSearchRequest对象", description = "定时任务日志搜索请求参数")
public class ScheduleJobLogSearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "任务id")
    private Integer jobId;

    @ApiModelProperty(value = "spring bean名称")
    private String beanName;

    @ApiModelProperty(value = "方法名")
    private String methodName;

    @ApiModelProperty(value = "任务状态    1：成功    0：失败")
    private Integer status;

}
