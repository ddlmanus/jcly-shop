package com.zbkj.common.vo;

import lombok.Data;

/**
 * 短信发送api第三方参数实体类
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
public class SendSmsVo {

    // 待发送短信手机号
    private String mobile;

    // 模版id
    private Integer template;

    // 发送参数
    private String param;

    private String content;

}
