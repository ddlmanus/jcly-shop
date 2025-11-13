package com.zbkj.service.task;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zbkj.common.model.coze.CozeBot;
import com.zbkj.common.model.coze.CozeSpace;
import com.zbkj.common.model.merchant.Merchant;
import com.zbkj.common.response.coze.CozeBotListResponse;
import com.zbkj.service.service.CozeBotService;
import com.zbkj.service.service.CozeService;
import com.zbkj.service.service.CozeSpaceService;
import com.zbkj.service.service.MerchantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Coze智能体同步定时任务
 * 定时同步商户的Coze空间和智能体信息到本地数据库
 *
 * @author AI Assistant
 * @since 2025-10-28
 */
@Component("CozeSyncTask")
public class CozeSyncTask {

    private static final Logger logger = LoggerFactory.getLogger(CozeSyncTask.class);

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private CozeSpaceService cozeSpaceService;

    @Autowired
    private CozeService cozeService;

    @Autowired
    private CozeBotService cozeBotService;

    /**
     * 同步所有商户的Coze空间和智能体
     * 每天凌晨2点执行一次
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncAllMerchantCozeData() {
        logger.info("================== 开始同步Coze空间和智能体 ==================");
        long startTime = System.currentTimeMillis();

        try {
            // 获取所有启用状态的商户
            LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Merchant::getIsDel, false)
                   .eq(Merchant::getIsSwitch, true); // 只同步启用的商户

            List<Merchant> merchantList = merchantService.list(wrapper);

            if (CollUtil.isEmpty(merchantList)) {
                logger.info("没有需要同步的商户");
                return;
            }

            logger.info("共有{}个商户需要同步Coze数据", merchantList.size());

            int successCount = 0;
            int failCount = 0;

            // 遍历所有商户，依次同步
            for (Merchant merchant : merchantList) {
                try {
                    logger.info("---------- 开始同步商户 [{}] 的Coze数据 ----------", merchant.getName());

                    // 1. 同步空间
                    boolean spaceResult = syncMerchantSpaces(merchant.getId());

                    if (spaceResult) {
                        // 2. 同步智能体
                        boolean botResult = syncMerchantBots(merchant.getId());

                        if (botResult) {
                            // 3. 确保有默认智能体
                            ensureDefaultBot(merchant.getId());

                            successCount++;
                            logger.info("商户 [{}] 的Coze数据同步成功", merchant.getName());
                        } else {
                            failCount++;
                            logger.warn("商户 [{}] 的智能体同步失败", merchant.getName());
                        }
                    } else {
                        failCount++;
                        logger.warn("商户 [{}] 的空间同步失败", merchant.getName());
                    }

                } catch (Exception e) {
                    failCount++;
                    logger.error("同步商户 [{}] 的Coze数据失败: {}", merchant.getName(), e.getMessage(), e);
                }
            }

            long endTime = System.currentTimeMillis();
            logger.info("================== Coze数据同步完成 ==================");
            logger.info("总商户数: {}, 成功: {}, 失败: {}, 耗时: {}ms",
                    merchantList.size(), successCount, failCount, (endTime - startTime));

        } catch (Exception e) {
            logger.error("同步Coze数据失败", e);
        }
    }

    /**
     * 同步商户的Coze空间
     * @param merId 商户ID
     * @return 是否成功
     */
    private boolean syncMerchantSpaces(Integer merId) {
        try {
            logger.info("开始同步商户 [{}] 的Coze空间", merId);

            // 调用CozeSpaceService的同步方法
            Boolean result = cozeSpaceService.syncCozeSpaces(merId);

            if (result != null && result) {
                List<CozeSpace> spaces = cozeSpaceService.getByMerId(merId);
                logger.info("商户 [{}] 成功同步{}个Coze空间", merId, spaces != null ? spaces.size() : 0);
                return true;
            } else {
                logger.warn("商户 [{}] 的空间同步返回失败", merId);
                return false;
            }

        } catch (Exception e) {
            logger.error("同步商户 [{}] 的Coze空间失败: {}", merId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 同步商户所有空间下的智能体
     * @param merId 商户ID
     * @return 是否成功
     */
    private boolean syncMerchantBots(Integer merId) {
        try {
            logger.info("开始同步商户 [{}] 的智能体", merId);

            // 获取商户的所有空间
            List<CozeSpace> spaces = cozeSpaceService.getByMerId(merId);

            if (CollUtil.isEmpty(spaces)) {
                logger.warn("商户 [{}] 没有Coze空间，跳过智能体同步", merId);
                return true; // 没有空间不算失败
            }

            int totalSynced = 0;

            // 遍历每个空间，同步智能体
            for (CozeSpace space : spaces) {
                try {
                    logger.info("同步空间 [{}] 下的智能体", space.getName());

                    // 调用Coze API获取智能体列表
                    CozeBotListResponse response = cozeService.getBotList(space.getSpaceId(), 1, 50);

                    if (response != null && response.getData() != null && response.getData().getItems() != null) {
                        List<CozeBotListResponse.BotInfo> botList = response.getData().getItems();

                        for (CozeBotListResponse.BotInfo botInfo : botList) {
                            try {
                                // 保存或更新智能体到本地数据库
                                saveOrUpdateBot(botInfo, merId, space.getSpaceId());
                                totalSynced++;
                            } catch (Exception e) {
                                logger.error("保存智能体 [{}] 失败: {}", botInfo.getName(), e.getMessage());
                            }
                        }

                        logger.info("空间 [{}] 成功同步{}个智能体", space.getName(), botList.size());
                    }

                } catch (Exception e) {
                    logger.error("同步空间 [{}] 的智能体失败: {}", space.getName(), e.getMessage(), e);
                }
            }

            logger.info("商户 [{}] 共同步{}个智能体", merId, totalSynced);
            return totalSynced > 0; // 至少同步一个智能体才算成功

        } catch (Exception e) {
            logger.error("同步商户 [{}] 的智能体失败: {}", merId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 保存或更新智能体到本地数据库
     * 注意：更新时不会修改用户已设置的 isDefault 和 status 字段，保护用户的配置
     *
     * @param botInfo Coze API返回的智能体信息
     * @param merId 商户ID
     * @param spaceId 空间ID
     */
    private void saveOrUpdateBot(CozeBotListResponse.BotInfo botInfo, Integer merId, String spaceId) {
        // 检查是否已存在
        CozeBot existingBot = cozeBotService.getByCozeBotId(botInfo.getBotId(), merId);

        if (existingBot != null) {
            // 更新现有智能体（只更新基本信息，不修改用户设置的 isDefault 和 status）
            existingBot.setName(botInfo.getName());
            existingBot.setDescription(botInfo.getDescription());
            existingBot.setIconUrl(botInfo.getIconUrl());
            existingBot.setVersion(botInfo.getVersion() != null ? parseVersion(botInfo.getVersion()) : 1);
            existingBot.setPublishStatus(botInfo.isPublishStatus() ? 1 : 0);
            existingBot.setUpdateTime(new Date());
            // 注意：不修改 isDefault 和 status，保留用户的配置

            cozeBotService.updateById(existingBot);
            logger.debug("更新智能体: {} (保留用户配置)", botInfo.getName());
        } else {
            // 创建新智能体
            CozeBot newBot = new CozeBot();
            newBot.setMerchantId(merId);
            newBot.setCozeBotId(botInfo.getBotId());
            newBot.setName(botInfo.getName());
            newBot.setDescription(botInfo.getDescription());
            newBot.setIconUrl(botInfo.getIconUrl());
            newBot.setVersion(botInfo.getVersion() != null ? parseVersion(botInfo.getVersion()) : 1);
            newBot.setPublishStatus(botInfo.isPublishStatus() ? 1 : 0);
            newBot.setSpaceId(spaceId);
            newBot.setStatus(1); // 默认启用
            newBot.setIsDefault(0); // 默认不是默认智能体
            newBot.setCreateTime(new Date());
            newBot.setUpdateTime(new Date());

            cozeBotService.save(newBot);
            logger.debug("创建智能体: {}", botInfo.getName());
        }
    }

    /**
     * 解析版本字符串为整数
     * @param version 版本字符串
     * @return 版本整数
     */
    private Integer parseVersion(String version) {
        try {
            return Integer.parseInt(version);
        } catch (NumberFormatException e) {
            logger.warn("无法解析版本号: {}, 使用默认值1", version);
            return 1;
        }
    }

    /**
     * 确保商户有默认智能体
     * 注意：只有在商户完全没有默认智能体时才会自动设置，不会覆盖用户已选择的默认智能体
     *
     * @param merId 商户ID
     */
    private void ensureDefaultBot(Integer merId) {
        try {
            // 检查是否已有默认智能体（如果已有，直接返回，不做任何修改）
            CozeBot defaultBot = cozeBotService.getDefaultBot(merId);

            if (defaultBot != null) {
                logger.debug("商户 [{}] 已有默认智能体: {}，保留用户设置", merId, defaultBot.getName());
                return; // 重要：如果已有默认智能体，不做任何修改
            }

            // 获取商户的所有智能体
            List<CozeBot> allBots = cozeBotService.getAllByMerchantId(merId);

            if (CollUtil.isEmpty(allBots)) {
                logger.warn("商户 [{}] 没有任何智能体，无法设置默认", merId);
                return;
            }

            // 优先选择已发布的智能体
            CozeBot selectedBot = null;
            for (CozeBot bot : allBots) {
                if (bot.getPublishStatus() != null && bot.getPublishStatus() == 1) {
                    selectedBot = bot;
                    break;
                }
            }

            // 如果没有已发布的，选择第一个
            if (selectedBot == null) {
                selectedBot = allBots.get(0);
            }

            // 设置为默认智能体
            cozeBotService.setDefaultBot(selectedBot.getCozeBotId(), merId);
            logger.info("商户 [{}] 设置默认智能体: {}", merId, selectedBot.getName());

        } catch (Exception e) {
            logger.error("确保商户 [{}] 的默认智能体失败: {}", merId, e.getMessage(), e);
        }
    }

    /**
     * 手动触发同步（用于测试或手动执行）
     * 可通过定时任务管理界面调用
     */
    public void manualSync() {
        logger.info("手动触发Coze数据同步");
        syncAllMerchantCozeData();
    }
}
