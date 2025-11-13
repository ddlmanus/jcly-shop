package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 微信开放平台上传素材响应对象
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
@ApiModel(value = "WechatOpenUploadResponse对象", description = "微信开放平台上传素材响应对象")
public class WechatOpenUploadResponse implements Serializable {

    private static final long serialVersionUID = -4585094537501770138L;

    @ApiModelProperty(value = "素材ID")
    private String mediaId;

    @ApiModelProperty(value = "素材地址")
    private String url;

    @ApiModelProperty(value = "素材名称")
    private String name;
}
