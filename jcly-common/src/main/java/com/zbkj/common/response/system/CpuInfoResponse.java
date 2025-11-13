package com.zbkj.common.response.system;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * CPU信息相应对象
 *
 * @author dudl
 * @version 1.0.0
 * @Date 2025/3/10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "CpuInfoResponse", description = "CPU信息相应对象")
public class CpuInfoResponse implements Serializable {

    private static final long serialVersionUID = -1741229453820569828L;

    @ApiModelProperty(value = "核心数")
    private Integer coreNum;

    @ApiModelProperty(value = "用户使用率")
    private String userUsageRate;

    @ApiModelProperty(value = "系统使用率")
    private String systemUsageRate;

    @ApiModelProperty(value = "当前空闲率")
    private String idleRate;
}
