package com.zbkj.admin.service;

import com.zbkj.common.model.record.SensitiveMethodLog;

/**
 * 行为service
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
public interface ActionService {

    /**
     * 添加敏感记录
     *
     * @param methodLog 记录信息
     */
    void addSensitiveLog(SensitiveMethodLog methodLog);
}
