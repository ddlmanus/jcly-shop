package com.zbkj.common.response.system;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 服务器信息相应对象
 *
 * @author dudl
 * @version 1.0.0
 * @Date 2025/3/10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "ServerInfoResponse", description = "服务器信息相应对象")
public class ServerInfoResponse implements Serializable {

    private static final long serialVersionUID = -1741229453820569828L;

    @ApiModelProperty(value = "服务器名称")
    private String serverName;

    @ApiModelProperty(value = "服务器IP")
    private String serverIp;

    @ApiModelProperty(value = "操作系统")
    private String operatingSystem;

    @ApiModelProperty(value = "系统架构")
    private String systemArchitecture;

}
