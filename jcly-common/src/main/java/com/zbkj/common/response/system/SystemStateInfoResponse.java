package com.zbkj.common.response.system;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 系统状态信息响应对象
 *
 * @author dudl
 * @version 1.0.0
 * @Date 2025/3/10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SystemStateInfoResponse", description = "系统状态信息响应对象")
public class SystemStateInfoResponse implements Serializable {

    private static final long serialVersionUID = 7142753574987565616L;

    @ApiModelProperty(value = "CPU信息部分")
    private CpuInfoResponse cpuInfo;

    @ApiModelProperty(value = "内存信息部分")
    private MemoryInfoResponse memoryInfo;

    @ApiModelProperty(value = "服务器信息部分")
    private ServerInfoResponse serverInfo;

    @ApiModelProperty(value = "java虚拟机信息部分")
    private JvmInfoResponse jvmInfo;

    @ApiModelProperty(value = "磁盘状态信息部分")
    private List<DiskInfoResponse> diskInfoList;
}
