package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.coze.CozeBotConfig;
import com.zbkj.service.dao.CozeBotConfigDao;
import com.zbkj.service.service.CozeBotConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Coze 智能体配置表 服务实现类
 * </p>
 *
 * @author Auto Generator
 * @since 2024-01-01
 */
@Slf4j
@Service
public class CozeBotConfigServiceImpl extends ServiceImpl<CozeBotConfigDao, CozeBotConfig> implements CozeBotConfigService {

    @Override
    public CozeBotConfig getByCozeBotId(String cozeBotId) {
        LambdaQueryWrapper<CozeBotConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeBotConfig::getCozeBotId, cozeBotId);
        wrapper.eq(CozeBotConfig::getStatus, 1);
        wrapper.orderByDesc(CozeBotConfig::getUpdateTime);
        wrapper.last("LIMIT 1");
        return getOne(wrapper);
    }

    @Override
    public Boolean saveOrUpdateBotConfig(CozeBotConfig botConfig) {
        try {
            // 检查是否已存在
            CozeBotConfig existingConfig = getByCozeBotId(botConfig.getCozeBotId());
            
            if (existingConfig != null) {
                // 更新现有配置
                botConfig.setId(existingConfig.getId());
                botConfig.setCreateTime(existingConfig.getCreateTime());
                return updateById(botConfig);
            } else {
                // 创建新配置
                return save(botConfig);
            }
        } catch (Exception e) {
            log.error("保存智能体配置失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Boolean deleteByCozeBotId(String cozeBotId, Integer merchantId) {
        try {
            LambdaQueryWrapper<CozeBotConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CozeBotConfig::getCozeBotId, cozeBotId);
            wrapper.eq(CozeBotConfig::getMerchantId, merchantId);
            return remove(wrapper);
        } catch (Exception e) {
            log.error("删除智能体配置失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
