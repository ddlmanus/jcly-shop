package com.zbkj.admin.service.impl;

import com.zbkj.admin.service.ActionService;
import com.zbkj.common.model.record.SensitiveMethodLog;
import com.zbkj.service.service.SensitiveMethodLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 行为service实现类
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
@Service
public class ActionServiceImpl implements ActionService {

    @Autowired
    private SensitiveMethodLogService sensitiveMethodLogService;

    /**
     * 添加敏感记录
     * @param methodLog 记录信息
     */
    @Async
    @Override
    public void addSensitiveLog(SensitiveMethodLog methodLog) {
        sensitiveMethodLogService.addLog(methodLog);
    }
}
