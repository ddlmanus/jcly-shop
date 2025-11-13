package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeWorkflow;
import com.zbkj.common.request.PageParamRequest;

/**
 * <p>
 * CozeWorkflow 工作流服务接口
 * </p>
 *
 * @author Auto Generator
 * @since 2024-01-01
 */
public interface CozeWorkflowService extends IService<CozeWorkflow> {

    /**
     * 根据商户ID获取工作流列表
     * @param merchantId 商户ID
     * @param pageParamRequest 分页参数
     * @return 工作流列表
     */
    PageInfo<CozeWorkflow> getByMerchantId(Integer merchantId, PageParamRequest pageParamRequest);

    /**
     * 根据Coze工作流ID获取工作流详情
     * @param cozeWorkflowId Coze工作流ID
     * @return 工作流详情
     */
    CozeWorkflow getByCozeWorkflowId(String cozeWorkflowId,Integer merId);

    /**
     * 保存或更新工作流
     * @param cozeWorkflow 工作流信息
     * @return 保存结果
     */
    Boolean saveOrUpdateWorkflow(CozeWorkflow cozeWorkflow);

    /**
     * 根据Coze工作流ID删除工作流
     * @param cozeWorkflowId Coze工作流ID
     * @param merchantId 商户ID
     * @return 删除结果
     */
    Boolean deleteByCozeWorkflowId(String cozeWorkflowId, Integer merchantId);

    /**
     * 根据商户ID获取所有工作流
     * @param merchantId 商户ID
     * @return 工作流列表
     */
    java.util.List<CozeWorkflow> getAllByMerchantId(Integer merchantId);
}
