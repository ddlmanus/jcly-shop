package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 微信公众号分享响应对象
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
@ApiModel(value = "WechatPublicShareResponse对象", description = "微信公众号分享响应对象")
public class WechatPublicShareResponse implements Serializable {

    private static final long serialVersionUID = -4585094537501770138L;

    @ApiModelProperty(value = "分享图片")
    private String image;

    @ApiModelProperty(value = "分享标题")
    private String title;

    @ApiModelProperty(value = "分享简介")
    private String synopsis;
}
