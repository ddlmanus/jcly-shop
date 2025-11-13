package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.merchant.MerchantProfitSharingConfig;

/**
 * <p>
 * 商户分账配置表 服务类
 * </p>
 *
 * @author dudl
 * @since 2025-01-07
 */
public interface MerchantProfitSharingConfigService extends IService<MerchantProfitSharingConfig> {

    /**
     * 根据商户ID获取分账配置
     *
     * @param merId 商户ID
     * @return 分账配置
     */
    MerchantProfitSharingConfig getByMerId(Integer merId);

    /**
     * 保存或更新分账配置
     *
     * @param config 分账配置
     * @return 是否成功
     */
    Boolean saveOrUpdateConfig(MerchantProfitSharingConfig config);

    /**
     * 检查商户是否开启分账
     *
     * @param merId 商户ID
     * @return 是否开启分账
     */
    Boolean isProfitSharingEnabled(Integer merId);

    /**
     * 启用/禁用商户分账
     *
     * @param merId     商户ID
     * @param isEnabled 是否启用
     * @return 是否成功
     */
    Boolean updateEnabled(Integer merId, Boolean isEnabled);

    /**
     * 获取商户分账比例
     *
     * @param merId 商户ID
     * @return 分账比例
     */
    java.math.BigDecimal getSharingRatio(Integer merId);
} 