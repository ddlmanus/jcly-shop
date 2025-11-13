package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.coze.CozeBotConfig;

/**
 * <p>
 * Coze 智能体配置表 服务类
 * </p>
 *
 * @author Auto Generator
 * @since 2024-01-01
 */
public interface CozeBotConfigService extends IService<CozeBotConfig> {

    /**
     * 根据智能体ID获取配置
     * @param cozeBotId 智能体ID
     * @return 智能体配置
     */
    CozeBotConfig getByCozeBotId(String cozeBotId);

    /**
     * 保存或更新智能体配置
     * @param botConfig 智能体配置
     * @return 是否成功
     */
    Boolean saveOrUpdateBotConfig(CozeBotConfig botConfig);

    /**
     * 根据智能体ID删除配置
     * @param cozeBotId 智能体ID
     * @param merchantId 商户ID
     * @return 是否成功
     */
    Boolean deleteByCozeBotId(String cozeBotId, Integer merchantId);
}
