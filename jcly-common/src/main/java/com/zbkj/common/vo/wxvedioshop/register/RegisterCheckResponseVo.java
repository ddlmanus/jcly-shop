package com.zbkj.common.vo.wxvedioshop.register;

import com.zbkj.common.vo.wxvedioshop.BaseResultResponseVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 获取接入状态 response
 *  +----------------------------------------------------------------------
 *  | JCLY [ JCLY赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 *  +----------------------------------------------------------------------
 *  | Author: dudl
 *  +----------------------------------------------------------------------
 */
@Data
public class RegisterCheckResponseVo  extends BaseResultResponseVo {

    @ApiModelProperty(value = "获取申请状态 对象")
    private RegisterCheckDataItemnVo data;
}
