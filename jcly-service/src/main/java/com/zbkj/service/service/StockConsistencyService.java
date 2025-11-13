package com.zbkj.service.service;

import com.zbkj.service.service.impl.StockConsistencyServiceImpl.ConsistencyCheckResult;

/**
 * 库存一致性检查服务接口
 * +----------------------------------------------------------------------
 * | JCLY [ JCLY赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 * +----------------------------------------------------------------------
 * | Author: AI Assistant
 * +----------------------------------------------------------------------
 */
public interface StockConsistencyService {

    /**
     * 检查库存一致性
     * @param merId 商户ID，为null时检查所有商户
     * @param autoRepair 是否自动修复
     * @return 检查结果
     */
    ConsistencyCheckResult checkStockConsistency(Integer merId, boolean autoRepair);

    /**
     * 修复库存不一致问题
     * @param merId 商户ID，为null时修复所有商户
     * @return 修复结果
     */
    ConsistencyCheckResult repairStockInconsistency(Integer merId);
}