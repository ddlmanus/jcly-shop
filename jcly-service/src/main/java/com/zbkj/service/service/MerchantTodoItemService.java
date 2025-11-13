package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.merchant.MerchantTodoItem;
import com.zbkj.common.request.PageParamRequest;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商户待办事项表 服务类
 * </p>
 *
 * @author SOLO Coding
 * @since 2024-01-05
 */
public interface MerchantTodoItemService extends IService<MerchantTodoItem> {

    /**
     * 分页获取待办事项列表
     *
     * @param merId            商户ID
     * @param type             事项类型
     * @param priority         优先级
     * @param status           状态
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    PageInfo<MerchantTodoItem> getPage(Integer merId, String type, Integer priority, Integer status, PageParamRequest pageParamRequest);

    /**
     * 创建待办事项
     *
     * @param todoItem 待办事项信息
     * @return Boolean
     */
    Boolean create(MerchantTodoItem todoItem);

    /**
     * 标记事项为已完成
     *
     * @param id         事项ID
     * @param operatorId 操作人ID
     * @return Boolean
     */
    Boolean complete(Integer id, Integer operatorId);

    /**
     * 批量标记事项为已完成
     *
     * @param ids        事项ID列表
     * @param operatorId 操作人ID
     * @return Boolean
     */
    Boolean batchComplete(List<Integer> ids, Integer operatorId);

    /**
     * 获取商户待办事项统计
     *
     * @param merId 商户ID
     * @return Map
     */
    Map<String, Object> getTodoStatistics(Integer merId);

    /**
     * 获取商户今日已办事项数量
     *
     * @param merId 商户ID
     * @return Integer
     */
    Integer getTodayCompletedCount(Integer merId);

    /**
     * 根据类型获取待办事项数量
     *
     * @param merId 商户ID
     * @param type  事项类型
     * @return Integer
     */
    Integer getCountByType(Integer merId, String type);

    /**
     * 根据ID获取待办事项详情
     *
     * @param id 事项ID
     * @return MerchantTodoItem
     */
    MerchantTodoItem getDetailById(Integer id);

    /**
     * 自动创建订单相关待办事项
     *
     * @param merId   商户ID
     * @param orderId 订单ID
     * @param type    事项类型
     * @return Boolean
     */
    Boolean createOrderTodoItem(Integer merId, String orderId, String type);

    /**
     * 自动创建商品相关待办事项
     *
     * @param merId     商户ID
     * @param productId 商品ID
     * @param type      事项类型
     * @return Boolean
     */
    Boolean createProductTodoItem(Integer merId, Integer productId, String type);
}