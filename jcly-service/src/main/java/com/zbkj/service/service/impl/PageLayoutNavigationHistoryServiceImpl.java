package com.zbkj.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.page.PageLayoutNavigationHistory;
import com.zbkj.service.dao.page.PageLayoutNavigationHistoryDao;
import com.zbkj.service.service.PageLayoutNavigationHistoryService;
import com.zbkj.service.service.PageLayoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 页面装修-底部导航历史记录表 服务实现类
 * </p>
 *
 * @author Claude
 * @since 2025-10-31
 */
@Service
public class PageLayoutNavigationHistoryServiceImpl extends ServiceImpl<PageLayoutNavigationHistoryDao, PageLayoutNavigationHistory>
        implements PageLayoutNavigationHistoryService {

    @Autowired
    private PageLayoutService pageLayoutService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveHistoryTemplate(String templateName, JSONObject jsonObject) {
        // 将当前保存的导航配置保存为历史记录（模版）
        PageLayoutNavigationHistory history = new PageLayoutNavigationHistory();
        history.setTemplateName(templateName);
        history.setNavigationData(JSONObject.toJSONString(jsonObject.getJSONArray("bottomNavigationList")));
        history.setIsDefault(0);
        history.setCenterButtonIndex(jsonObject.getString("centerButtonIndex"));
        history.setTabBarStyleMode(jsonObject.getString("tabBarStyleMode"));
        history.setIsCustom(jsonObject.getString("isCustom"));
        return save(history);
    }

    @Override
    public IPage<PageLayoutNavigationHistory> getHistoryPage(Integer page, Integer limit) {
        LambdaQueryWrapper<PageLayoutNavigationHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(PageLayoutNavigationHistory::getCreateTime);
        return baseMapper.selectPage(new Page<>(page, limit), queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean setDefaultAndApply(Integer id) {
        // 1. 获取指定的历史记录
        PageLayoutNavigationHistory history = baseMapper.selectById(id);
        if (history == null) {
            return false;
        }
        JSONObject navigationData = JSONObject.parseObject(history.getNavigationData());
        // 2. 构建导航数据JSON对象
        JSONObject navigationJson = new JSONObject();
        navigationJson.put("bottomNavigationList", navigationData.get("bottomNavigationList"));
        navigationJson.put("centerButtonIndex", history.getCenterButtonIndex());
        navigationJson.put("tabBarStyleMode", history.getTabBarStyleMode());
        navigationJson.put("isCustom", history.getIsCustom());

        // 3. 调用bottomNavigationSave更新系统配置
        pageLayoutService.bottomNavigationSave(navigationJson);

        // 4. 更新历史记录：将其他的is_default改为0，当前的改为1
        LambdaQueryWrapper<PageLayoutNavigationHistory> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(PageLayoutNavigationHistory::getIsDefault, 1);
        PageLayoutNavigationHistory updateHistory = new PageLayoutNavigationHistory();
        updateHistory.setIsDefault(0);
        baseMapper.update(updateHistory, updateWrapper);

        // 5. 设置当前记录为默认
        history.setIsDefault(1);
        return baseMapper.updateById(history) > 0;
    }

    @Override
    public PageLayoutNavigationHistory getDefault() {
        LambdaQueryWrapper<PageLayoutNavigationHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PageLayoutNavigationHistory::getIsDefault, 1);
        queryWrapper.orderByDesc(PageLayoutNavigationHistory::getCreateTime);
        queryWrapper.last("limit 1");
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public Boolean deleteHistory(Integer id) {
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public Boolean updateHistory(PageLayoutNavigationHistory history) {
        PageLayoutNavigationHistory history1 = this.getById(history.getId());
        //判断 如果该模板为默认，则需要更新配置
        if (history1.getIsDefault() == 1) {
            JSONObject navigationData = JSONObject.parseObject(history.getNavigationData());
            // 2. 构建导航数据JSON对象
            JSONObject navigationJson = new JSONObject();
            navigationJson.put("bottomNavigationList", navigationData.get("bottomNavigationList"));
            navigationJson.put("centerButtonIndex", history.getCenterButtonIndex());
            navigationJson.put("tabBarStyleMode", history.getTabBarStyleMode());
            navigationJson.put("isCustom", history.getIsCustom());

            // 3. 调用bottomNavigationSave更新系统配置
            pageLayoutService.bottomNavigationSave(navigationJson);
        }
        return baseMapper.updateById(history) > 0;
    }
}
