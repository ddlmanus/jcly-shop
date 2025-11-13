package com.zbkj.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.merchant.MerchantProfitSharingConfig;
import com.zbkj.service.dao.MerchantProfitSharingConfigDao;
import com.zbkj.service.service.MerchantProfitSharingConfigService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 商户分账配置表 服务实现类
 * </p>
 *
 * @author dudl
 * @since 2025-01-07
 */
@Service
public class MerchantProfitSharingConfigServiceImpl extends ServiceImpl<MerchantProfitSharingConfigDao, MerchantProfitSharingConfig> implements MerchantProfitSharingConfigService {

    /**
     * 根据商户ID获取分账配置
     *
     * @param merId 商户ID
     * @return 分账配置
     */
    @Override
    public MerchantProfitSharingConfig getByMerId(Integer merId) {
        LambdaQueryWrapper<MerchantProfitSharingConfig> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MerchantProfitSharingConfig::getMerId, merId);
        return getOne(lqw);
    }

    /**
     * 保存或更新分账配置
     *
     * @param config 分账配置
     * @return 是否成功
     */
    @Override
    public Boolean saveOrUpdateConfig(MerchantProfitSharingConfig config) {
        if (ObjectUtil.isNull(config.getMerId())) {
            throw new CrmebException("商户ID不能为空");
        }

        // 验证分账比例
        if (config.getSharingRatio().compareTo(BigDecimal.ZERO) < 0 || 
            config.getSharingRatio().compareTo(new BigDecimal("100")) > 0) {
            throw new CrmebException("分账比例必须在0-100之间");
        }

        MerchantProfitSharingConfig existConfig = getByMerId(config.getMerId());
        if (ObjectUtil.isNotNull(existConfig)) {
            // 更新
            config.setId(existConfig.getId());
            config.setUpdateTime(new Date());
            return updateById(config);
        } else {
            // 新增
            config.setCreateTime(new Date());
            config.setUpdateTime(new Date());
            return save(config);
        }
    }

    /**
     * 检查商户是否开启分账
     *
     * @param merId 商户ID
     * @return 是否开启分账
     */
    @Override
    public Boolean isProfitSharingEnabled(Integer merId) {
        MerchantProfitSharingConfig config = getByMerId(merId);
        return ObjectUtil.isNotNull(config) && Boolean.TRUE.equals(config.getIsEnabled()) && Boolean.TRUE.equals(config.getStatus());
    }

    /**
     * 启用/禁用商户分账
     *
     * @param merId     商户ID
     * @param isEnabled 是否启用
     * @return 是否成功
     */
    @Override
    public Boolean updateEnabled(Integer merId, Boolean isEnabled) {
        MerchantProfitSharingConfig config = getByMerId(merId);
        if (ObjectUtil.isNull(config)) {
            throw new CrmebException("商户分账配置不存在");
        }

        config.setIsEnabled(isEnabled);
        config.setUpdateTime(new Date());
        return updateById(config);
    }

    /**
     * 获取商户分账比例
     *
     * @param merId 商户ID
     * @return 分账比例
     */
    @Override
    public BigDecimal getSharingRatio(Integer merId) {
        MerchantProfitSharingConfig config = getByMerId(merId);
        if (ObjectUtil.isNull(config) || !Boolean.TRUE.equals(config.getIsEnabled())) {
            return BigDecimal.ZERO;
        }
        return config.getSharingRatio();
    }
} 