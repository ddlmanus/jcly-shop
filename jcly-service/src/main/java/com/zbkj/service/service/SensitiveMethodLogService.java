package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.record.SensitiveMethodLog;
import com.zbkj.common.request.PageParamRequest;

/**
 * SensitiveMethoyLogService 接口
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
public interface SensitiveMethodLogService extends IService<SensitiveMethodLog> {

    /**
     * 添加敏感记录
     * @param methodLog 记录信息
     */
    void addLog(SensitiveMethodLog methodLog);

    /**
     * 平台端分页列表
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    PageInfo<SensitiveMethodLog> getPlatformPageList(PageParamRequest pageParamRequest);

    /**
     * 商户端分页列表
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    PageInfo<SensitiveMethodLog> getMerchantPageList(PageParamRequest pageParamRequest);
}
