package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.invoice.InvoiceDescription;
import com.zbkj.common.request.InvoiceDescriptionRequest;

/**
 * 发票说明服务接口
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
public interface InvoiceDescriptionService extends IService<InvoiceDescription> {

    /**
     * 获取发票说明
     * @return 发票说明
     */
    InvoiceDescription getInvoiceDescription();

    /**
     * 更新发票说明
     * @param request 更新请求
     * @return 更新结果
     */
    Boolean updateInvoiceDescription(InvoiceDescriptionRequest request);
} 