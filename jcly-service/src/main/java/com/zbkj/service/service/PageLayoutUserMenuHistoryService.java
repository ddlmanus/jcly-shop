package com.zbkj.service.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zbkj.common.model.page.PageLayoutUserMenuHistory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 页面装修-用户中心菜单历史记录表 服务类
 * </p>
 *
 * @author Claude
 * @since 2025-10-31
 */
public interface PageLayoutUserMenuHistoryService extends IService<PageLayoutUserMenuHistory> {

    /**
     * 保存用户中心菜单配置为历史记录（模版）
     * @param templateName 模版名称
     * @param jsonObject 菜单配置数据
     * @return 是否保存成功
     */
    Boolean saveHistoryTemplate(String templateName, JSONObject jsonObject);

    /**
     * 分页查询菜单历史记录
     * @param page 页码
     * @param limit 每页条数
     * @return 历史记录分页数据
     */
    IPage<PageLayoutUserMenuHistory> getHistoryPage(Integer page, Integer limit);

    /**
     * 设置默认菜单配置，并更新系统配置
     * @param id 历史记录ID
     * @return 是否设置成功
     */
    Boolean setDefaultAndApply(Integer id);

    /**
     * 获取当前默认的菜单配置
     * @return 默认菜单配置
     */
    PageLayoutUserMenuHistory getDefault();

    /**
     * 删除历史记录
     * @param id 历史记录ID
     * @return 是否删除成功
     */
    Boolean deleteHistory(Integer id);

    /**
     * 更新历史记录
     * @param history 历史记录对象
     * @return 是否更新成功
     */
    Boolean updateHistory(PageLayoutUserMenuHistory history);
}
