package com.zbkj.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.page.PageLayoutUserMenuHistory;
import com.zbkj.service.dao.page.PageLayoutUserMenuHistoryDao;
import com.zbkj.service.service.PageLayoutUserMenuHistoryService;
import com.zbkj.service.service.PageLayoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 页面装修-用户中心菜单历史记录表 服务实现类
 * </p>
 *
 * @author Claude
 * @since 2025-10-31
 */
@Service
public class PageLayoutUserMenuHistoryServiceImpl extends ServiceImpl<PageLayoutUserMenuHistoryDao, PageLayoutUserMenuHistory>
        implements PageLayoutUserMenuHistoryService {

    @Autowired
    private PageLayoutService pageLayoutService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveHistoryTemplate(String templateName, JSONObject jsonObject) {
        // 将当前保存的菜单配置保存为历史记录（模版）
        PageLayoutUserMenuHistory history = new PageLayoutUserMenuHistory();
        history.setTemplateName(templateName);
        // 保存整个配置对象，包括 userMenu 和 userBanner
        history.setMenuData(JSONObject.toJSONString(jsonObject));
        history.setIsDefault(0);
        history.setUserMenuLayoutMode(jsonObject.getString("userMenuLayoutMode"));
        return save(history);
    }

    @Override
    public IPage<PageLayoutUserMenuHistory> getHistoryPage(Integer page, Integer limit) {
        LambdaQueryWrapper<PageLayoutUserMenuHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(PageLayoutUserMenuHistory::getCreateTime);
        return baseMapper.selectPage(new Page<>(page, limit), queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean setDefaultAndApply(Integer id) {
        // 1. 获取指定的历史记录
        PageLayoutUserMenuHistory history = baseMapper.selectById(id);
        if (history == null) {
            return false;
        }

        // 2. 构建菜单数据JSON对象
        JSONObject menuJson = new JSONObject();
        JSONObject userMenuJson = JSONObject.parseObject(history.getMenuData());
        menuJson.put("userMenu",userMenuJson.get("userMenu"));
        menuJson.put("userMenuLayoutMode", history.getUserMenuLayoutMode());

        // 3. 调用userMenuSave更新系统配置
        pageLayoutService.userMenuSave(menuJson);

        // 4. 更新历史记录：将其他的is_default改为0，当前的改为1
        LambdaQueryWrapper<PageLayoutUserMenuHistory> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(PageLayoutUserMenuHistory::getIsDefault, 1);
        PageLayoutUserMenuHistory updateHistory = new PageLayoutUserMenuHistory();
        updateHistory.setIsDefault(0);
        baseMapper.update(updateHistory, updateWrapper);

        // 5. 设置当前记录为默认
        history.setIsDefault(1);
        return baseMapper.updateById(history) > 0;
    }

    @Override
    public PageLayoutUserMenuHistory getDefault() {
        LambdaQueryWrapper<PageLayoutUserMenuHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PageLayoutUserMenuHistory::getIsDefault, 1);
        queryWrapper.orderByDesc(PageLayoutUserMenuHistory::getCreateTime);
        queryWrapper.last("limit 1");
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public Boolean deleteHistory(Integer id) {
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public Boolean updateHistory(PageLayoutUserMenuHistory history) {
        PageLayoutUserMenuHistory pageLayoutUserMenuHistory = this.getById(history.getId());
        if(pageLayoutUserMenuHistory.getIsDefault()==1){
            // 2. 构建菜单数据JSON对象
            JSONObject menuJson = new JSONObject();
            JSONObject userMenuJson = JSONObject.parseObject(history.getMenuData());
            menuJson.put("userMenu",userMenuJson.get("userMenu"));
            menuJson.put("userMenuLayoutMode", history.getUserMenuLayoutMode());

            // 3. 调用userMenuSave更新系统配置
            pageLayoutService.userMenuSave(menuJson);

        }
        return baseMapper.updateById(history) > 0;
    }
}
