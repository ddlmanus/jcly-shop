package com.zbkj.service.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zbkj.common.model.page.PageLayoutNavigationHistory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 页面装修-底部导航历史记录表 服务类
 * </p>
 *
 * @author Claude
 * @since 2025-10-31
 */
public interface PageLayoutNavigationHistoryService extends IService<PageLayoutNavigationHistory> {

    /**
     * 保存导航配置为历史记录（模版）
     * @param templateName 模版名称
     * @param jsonObject 导航配置数据
     * @return 是否保存成功
     */
    Boolean saveHistoryTemplate(String templateName, JSONObject jsonObject);

    /**
     * 分页查询导航历史记录
     * @param page 页码
     * @param limit 每页条数
     * @return 历史记录分页数据
     */
    IPage<PageLayoutNavigationHistory> getHistoryPage(Integer page, Integer limit);

    /**
     * 设置默认导航配置，并更新系统配置
     * @param id 历史记录ID
     * @return 是否设置成功
     */
    Boolean setDefaultAndApply(Integer id);

    /**
     * 获取当前默认的导航配置
     * @return 默认导航配置
     */
    PageLayoutNavigationHistory getDefault();

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
    Boolean updateHistory(PageLayoutNavigationHistory history);
}
