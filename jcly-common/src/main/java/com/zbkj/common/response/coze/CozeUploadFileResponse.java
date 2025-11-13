package com.zbkj.common.response.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Coze上传文件响应
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
@ApiModel(value = "CozeUploadFileResponse", description = "Coze上传文件响应")
public class CozeUploadFileResponse {

    @ApiModelProperty(value = "调用状态码")
    private Long code;

    @ApiModelProperty(value = "状态信息")
    private String msg;

    @ApiModelProperty(value = "已上传的文件信息")
    private FileData data;

    @Data
    @ApiModel(value = "FileData", description = "文件数据")
    public static class FileData {
        
        @ApiModelProperty(value = "已上传的文件ID")
        private String id;

        @ApiModelProperty(value = "文件的总字节数")
        private Long bytes;

        @ApiModelProperty(value = "文件名称")
        @JsonProperty("file_name")
        private String fileName;

        @ApiModelProperty(value = "文件的上传时间，格式为10位的Unixtime时间戳，单位为秒")
        @JsonProperty("created_at")
        private Long createdAt;
    }
}
