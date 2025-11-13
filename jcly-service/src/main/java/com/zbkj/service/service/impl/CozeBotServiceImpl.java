package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeBot;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.service.dao.CozeBotDao;
import com.zbkj.service.service.CozeBotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * CozeBot 智能体服务实现类
 * </p>
 *
 * @author Auto Generator
 * @since 2024-01-01
 */
@Slf4j
@Service
public class CozeBotServiceImpl extends ServiceImpl<CozeBotDao, CozeBot> implements CozeBotService {

    @Override
    public CozeBot getByCozeBotId(String cozeBotId, Integer merchantId) {
        LambdaQueryWrapper<CozeBot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeBot::getMerchantId, merchantId);
        wrapper.eq(CozeBot::getCozeBotId, cozeBotId);
        wrapper.eq(CozeBot::getStatus, 1);
        return getOne(wrapper);
    }

    @Override
    public PageInfo<CozeBot> getByMerchantId(Integer merchantId, PageParamRequest pageParamRequest) {
        Page<CozeBot> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<CozeBot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeBot::getMerchantId, merchantId);
        wrapper.eq(CozeBot::getStatus, 1);
        wrapper.orderByDesc(CozeBot::getCreateTime);
        List<CozeBot> cozeBots = this.baseMapper.selectList(wrapper);
        return CommonPage.copyPageInfo(page,cozeBots);
    }

    @Override
    public CozeBot saveOrUpdateBot(CozeBot cozeBot) {
        Date now = new Date();
        
        if (cozeBot.getId() == null) {
            // 新增
            cozeBot.setCreateTime(now);
            cozeBot.setUpdateTime(now);
            if (cozeBot.getStatus() == null) {
                cozeBot.setStatus(1);
            }
        } else {
            // 更新
            cozeBot.setUpdateTime(now);
        }
        
        saveOrUpdate(cozeBot);
        return cozeBot;
    }

    @Override
    public Boolean deleteByBotId(String cozeBotId, Integer merchantId) {
        LambdaQueryWrapper<CozeBot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeBot::getCozeBotId, cozeBotId);
        wrapper.eq(CozeBot::getMerchantId, merchantId);
        
        CozeBot cozeBot = getOne(wrapper);
        if (cozeBot == null) {
            return false;
        }
        
        // 软删除，设置状态为0
        cozeBot.setStatus(0);
        cozeBot.setUpdateTime(new Date());
        return updateById(cozeBot);
    }

    @Override
    public List<CozeBot> getAllByMerchantId(Integer merchantId) {
        LambdaQueryWrapper<CozeBot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeBot::getMerchantId, merchantId);
        wrapper.eq(CozeBot::getStatus, 1);
        wrapper.orderByDesc(CozeBot::getCreateTime);
        return list(wrapper);
    }

    @Override
    public Boolean setDefaultBot(String cozeBotId, Integer merchantId) {
        // 先将该商户下所有机器人设置为非默认
        LambdaQueryWrapper<CozeBot> allWrapper = new LambdaQueryWrapper<>();
        allWrapper.eq(CozeBot::getMerchantId, merchantId);
        allWrapper.eq(CozeBot::getStatus, 1);
        allWrapper.eq(CozeBot::getIsDefault, 0);
        List<CozeBot> allBots = list(allWrapper);
        for (CozeBot bot : allBots) {
            bot.setIsDefault(0);
            bot.setUpdateTime(new Date());
            updateById(bot);
        }
        
        // 再将指定的机器人设置为默认
        LambdaQueryWrapper<CozeBot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeBot::getCozeBotId, cozeBotId);
        wrapper.eq(CozeBot::getMerchantId, merchantId);
        CozeBot cozeBot = getOne(wrapper);
        if (cozeBot != null) {
            cozeBot.setIsDefault(1);
            cozeBot.setUpdateTime(new Date());
            return updateById(cozeBot);
        }
        return false;
    }

    @Override
    public CozeBot getDefaultBot(Integer merchantId) {
        LambdaQueryWrapper<CozeBot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeBot::getMerchantId, merchantId);
        wrapper.eq(CozeBot::getIsDefault, 1);
        CozeBot cozeBot = getOne(wrapper);
        return cozeBot;
    }

    @Override
    public CozeBot getBotByBotId(String botId) {
        LambdaQueryWrapper<CozeBot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeBot::getCozeBotId, botId);
        return getOne(wrapper);
    }
}
