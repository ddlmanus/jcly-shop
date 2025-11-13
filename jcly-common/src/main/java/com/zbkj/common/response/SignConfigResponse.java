package com.zbkj.common.response;

import com.zbkj.common.model.sgin.SignConfig;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 签到配置响应对象
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
@ApiModel(value = "SignConfigResponse对象", description = "签到配置响应对象")
public class SignConfigResponse implements Serializable {

    private static final long serialVersionUID = -4585094537501770138L;

    @ApiModelProperty(value = "基础签到配置")
    private SignConfig baseSignConfig;

    @ApiModelProperty(value = "连续签到列表")
    private List<SignConfig> signConfigList;

    @ApiModelProperty(value = "签到规则说明")
    private String signRuleDescription;

}
