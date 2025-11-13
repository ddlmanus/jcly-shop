package com.zbkj.common.response.system;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 磁盘状态信息相应对象
 *
 * @author dudl
 * @version 1.0.0
 * @Date 2025/3/10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "DiskInfoResponse", description = "磁盘状态信息相应对象")
public class DiskInfoResponse implements Serializable {

    private static final long serialVersionUID = -1741229453820569828L;

    @ApiModelProperty(value = "盘符路径")
    private String driveLetterPath;

    @ApiModelProperty(value = "文件系统")
    private String fileSystem;

    @ApiModelProperty(value = "盘符类型")
    private String driveLetterType;

    @ApiModelProperty(value = "总大小")
    private String totalSize;

    @ApiModelProperty(value = "可用大小")
    private String usableSize;

    @ApiModelProperty(value = "已用大小")
    private String usedSize;

    @ApiModelProperty(value = "已用百分比")
    private String usedRate;

}
