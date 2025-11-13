package com.zbkj.common.response.coze;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Coze API基础响应参数
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
@ApiModel(value = "CozeBaseResponse", description = "Coze API基础响应参数")
public class CozeBaseResponse {

    @ApiModelProperty(value = "调用状态码")
    private Long code;

    @ApiModelProperty(value = "状态信息")
    private String msg;

    @ApiModelProperty(value = "响应详情信息")
    private ResponseDetail detail;

    @Data
    @ApiModel(value = "ResponseDetail", description = "响应详情")
    public static class ResponseDetail {
        @ApiModelProperty(value = "本次请求的日志ID")
        private String logid;
    }
}
