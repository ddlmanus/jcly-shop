package com.zbkj.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.page.PageLayoutSplashAdHistory;
import com.zbkj.common.vo.SplashAdConfigVo;
import com.zbkj.service.dao.page.PageLayoutSplashAdHistoryDao;
import com.zbkj.service.service.PageLayoutSplashAdHistoryService;
import com.zbkj.service.service.PageLayoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 页面装修-开屏广告历史记录表 服务实现类
 * </p>
 *
 * @author Claude
 * @since 2025-10-31
 */
@Service
public class PageLayoutSplashAdHistoryServiceImpl extends ServiceImpl<PageLayoutSplashAdHistoryDao, PageLayoutSplashAdHistory>
        implements PageLayoutSplashAdHistoryService {

    @Autowired
    private PageLayoutService pageLayoutService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveHistoryTemplate(String templateName, SplashAdConfigVo configVo) {
        // 将当前保存的广告配置保存为历史记录（模版）
        PageLayoutSplashAdHistory history = new PageLayoutSplashAdHistory();
        history.setTemplateName(templateName);
        history.setAdConfigData(JSONObject.toJSONString(configVo));
        history.setIsDefault(0);
        return save(history);
    }

    @Override
    public IPage<PageLayoutSplashAdHistory> getHistoryPage(Integer page, Integer limit) {
        LambdaQueryWrapper<PageLayoutSplashAdHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(PageLayoutSplashAdHistory::getCreateTime);
        return baseMapper.selectPage(new Page<>(page, limit), queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean setDefaultAndApply(Integer id) {
        // 1. 获取指定的历史记录
        PageLayoutSplashAdHistory history = baseMapper.selectById(id);
        if (history == null) {
            return false;
        }

        // 2. 解析广告配置数据
        SplashAdConfigVo configVo = JSONObject.parseObject(history.getAdConfigData(), SplashAdConfigVo.class);

        // 3. 调用splashAdConfigSave更新系统配置
        pageLayoutService.splashAdConfigSave(configVo);

        // 4. 更新历史记录：将其他的is_default改为0，当前的改为1
        LambdaQueryWrapper<PageLayoutSplashAdHistory> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(PageLayoutSplashAdHistory::getIsDefault, 1);
        PageLayoutSplashAdHistory updateHistory = new PageLayoutSplashAdHistory();
        updateHistory.setIsDefault(0);
        baseMapper.update(updateHistory, updateWrapper);

        // 5. 设置当前记录为默认
        history.setIsDefault(1);
        return baseMapper.updateById(history) > 0;
    }

    @Override
    public PageLayoutSplashAdHistory getDefault() {
        LambdaQueryWrapper<PageLayoutSplashAdHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PageLayoutSplashAdHistory::getIsDefault, 1);
        queryWrapper.orderByDesc(PageLayoutSplashAdHistory::getCreateTime);
        queryWrapper.last("limit 1");
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public Boolean deleteHistory(Integer id) {
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public Boolean updateHistory(PageLayoutSplashAdHistory history) {
        PageLayoutSplashAdHistory h = this.getById(history.getId());
        if(h.getIsDefault()==1){
            // 2. 解析广告配置数据
            SplashAdConfigVo configVo = JSONObject.parseObject(history.getAdConfigData(), SplashAdConfigVo.class);
            // 3. 调用splashAdConfigSave更新系统配置
            pageLayoutService.splashAdConfigSave(configVo);
        }
        return baseMapper.updateById(history) > 0;
    }
}
