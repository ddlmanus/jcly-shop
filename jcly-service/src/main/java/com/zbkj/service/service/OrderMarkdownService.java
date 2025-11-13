package com.zbkj.service.service;

import com.zbkj.common.model.order.Order;

/**
 * <p>
 * 订单Markdown文件服务接口
 * </p>
 *
 * @author Auto Generator
 * @since 2024-01-01
 */
public interface OrderMarkdownService {

    /**
     * 生成并上传订单信息的Markdown文件到Coze知识库
     * 
     * @param order 订单信息
     * @return 上传成功返回Coze文件ID，失败返回null
     */
    String generateAndUploadOrderMarkdown(Order order);

    /**
     * 删除订单的Markdown文件
     * 
     * @param orderNo 订单号
     * @return 删除结果
     */
    Boolean deleteOrderMarkdown(String orderNo);

    /**
     * 更新订单在Coze知识库中的Markdown文件
     * 当订单状态发生变更时调用此方法同步更新知识库中的订单信息
     * 
     * @param order 更新后的订单信息
     * @return 更新后的Coze文件ID，失败返回null
     */
    String updateOrderMarkdownInKnowledge(Order order);

    /**
     * 根据订单号更新知识库中的Markdown文件
     * 
     * @param orderNo 订单号
     * @return 更新结果
     */
    Boolean updateOrderMarkdownByOrderNo(String orderNo);
}
