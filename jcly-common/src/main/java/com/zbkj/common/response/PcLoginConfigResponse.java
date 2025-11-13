package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * PC商城登录配置响应对象
 *
 * @author Han
 * @version 1.0.0
 * @Date 2025/5/8
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "PcLoginConfigResponse", description = "PC商城登录配置响应对象")
public class PcLoginConfigResponse implements Serializable {

    private static final long serialVersionUID = -6187813950119527605L;

    @ApiModelProperty(value = "微信扫码开关:0-关闭微信扫码，1-微信网页应用扫码，2-微信公众号扫码")
    private Integer wxScanSwitch;

    @ApiModelProperty(value = "开放平台appid,wxScanSwitch=1时有值")
    private String openAppid;

    @ApiModelProperty(value = "微信公众号二维码ticket,wxScanSwitch=2时有值")
    private String ticket;
}
