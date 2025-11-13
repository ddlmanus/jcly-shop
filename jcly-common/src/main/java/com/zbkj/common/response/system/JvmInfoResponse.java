package com.zbkj.common.response.system;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * java虚拟机信息相应对象
 *
 * @author dudl
 * @version 1.0.0
 * @Date 2025/3/10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "JvmInfoResponse", description = "java虚拟机信息相应对象")
public class JvmInfoResponse implements Serializable {

    private static final long serialVersionUID = -1741229453820569828L;

    @ApiModelProperty(value = "java名称")
    private String javaName;

    @ApiModelProperty(value = "java版本")
    private String javaVersion;

    @ApiModelProperty(value = "启动时间")
    private String startTime;

    @ApiModelProperty(value = "运行时长")
    private String runningTime;

    @ApiModelProperty(value = "安装路径")
    private String installationPath;

    @ApiModelProperty(value = "项目路径")
    private String projectPath;

    @ApiModelProperty(value = "运行参数")
    private String operatingParameters;

}
