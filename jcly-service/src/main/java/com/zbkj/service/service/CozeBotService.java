package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeBot;
import com.zbkj.common.request.PageParamRequest;

import java.util.List;

/**
 * <p>
 * CozeBot 智能体服务类
 * </p>
 *
 * @author Auto Generator
 * @since 2024-01-01
 */
public interface CozeBotService extends IService<CozeBot> {

    /**
     * 根据Coze Bot ID查找
     */
    CozeBot getByCozeBotId(String cozeBotId,Integer merId);

    /**
     * 根据商户ID获取智能体列表
     */
    PageInfo<CozeBot> getByMerchantId(Integer merchantId, PageParamRequest pageParamRequest);

    /**
     * 保存或更新智能体
     */
    CozeBot saveOrUpdateBot(CozeBot cozeBot);

    /**
     * 删除智能体
     */
    Boolean deleteByBotId(String cozeBotId, Integer merchantId);

    /**
     * 获取商户的所有智能体
     */
    List<CozeBot> getAllByMerchantId(Integer merchantId);

    /**
     * 设置默认智能体
     * @param cozeBotId 智能体ID
     * @param merchantId 商户ID
     * @return 设置结果
     */
    Boolean setDefaultBot(String cozeBotId, Integer merchantId);

    /**
     * 获取商户的默认智能体
     * @param merchantId 商户ID
     * @return 默认智能体
     */
    CozeBot getDefaultBot(Integer merchantId);

    CozeBot getBotByBotId(String botId);
}
