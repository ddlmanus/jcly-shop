package com.zbkj.common.response;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 秒杀时段响应对象
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
@ApiModel(value = "SeckillTimeIntervalResponse对象", description = "秒杀时段响应对象")
public class SeckillTimeIntervalResponse implements Serializable {

    private static final long serialVersionUID = 3285713110515203543L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "秒杀时段名称")
    private String name;

    @ApiModelProperty(value = "秒杀时段开始时间")
    private String startTime;

    @ApiModelProperty(value = "秒杀时段结束时间")
    private String endTime;

    @ApiModelProperty(value = "状态 0=关闭 1=开启")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
