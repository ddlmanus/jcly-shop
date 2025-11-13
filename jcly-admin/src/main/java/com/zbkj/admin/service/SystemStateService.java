package com.zbkj.admin.service;

import com.zbkj.common.response.system.SystemStateInfoResponse;

/**
 * 系统状态服务
 *
 * @author dudl
 * @version 1.0.0
 * @Date 2025/3/10
 */
public interface SystemStateService {

    /**
     * 获取系统状态
     */
    SystemStateInfoResponse getIngo();
}
