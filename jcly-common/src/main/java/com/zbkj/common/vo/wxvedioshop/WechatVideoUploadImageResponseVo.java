package com.zbkj.common.vo.wxvedioshop;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
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
@EqualsAndHashCode(callSuper = true)
@Data
public class WechatVideoUploadImageResponseVo extends BaseResultResponseVo {

    private WechatVideoUploadImageInfoVo img_info;
}
