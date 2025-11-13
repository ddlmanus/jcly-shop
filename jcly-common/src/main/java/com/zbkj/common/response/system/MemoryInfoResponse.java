package com.zbkj.common.response.system;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 内存信息相应对象
 *
 * @author dudl
 * @version 1.0.0
 * @Date 2025/3/10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "MemoryInfoResponse", description = "内存信息相应对象")
public class MemoryInfoResponse implements Serializable {

    private static final long serialVersionUID = -1741229453820569828L;

    @ApiModelProperty(value = "内存-总内存")
    private String memoryTotalMemory;

    @ApiModelProperty(value = "内存-已用内存")
    private String memoryUsedMemory;

    @ApiModelProperty(value = "内存-剩余内存")
    private String memoryRemainingMemory;

    @ApiModelProperty(value = "内存-使用率")
    private String memoryUsageRate;

    @ApiModelProperty(value = "JVM-总内存")
    private String jvmTotalMemory;

    @ApiModelProperty(value = "JVM-已用内存")
    private String jvmUsedMemory;

    @ApiModelProperty(value = "JVM-剩余内存")
    private String jvmRemainingMemory;

    @ApiModelProperty(value = "JVM-使用率")
    private String jvmUsageRate;
}
