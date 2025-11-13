package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeKnowledge;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.coze.CozeKnowledgeListResponse;

import java.util.List;

/**
 * <p>
 * CozeKnowledge 知识库服务类
 * </p>
 *
 * @author Auto Generator
 * @since 2024-01-01
 */
public interface CozeKnowledgeService extends IService<CozeKnowledge> {

    /**
     * 根据Coze Knowledge ID查找
     */
    CozeKnowledge getByCozeKnowledgeId(String cozeKnowledgeId, Integer merchantId);

    /**
     * 根据商户ID获取知识库列表
     */
    PageInfo<CozeKnowledge> getByMerchantId(Integer merchantId, PageParamRequest pageParamRequest);

    /**
     * 保存或更新知识库
     */
    CozeKnowledge saveOrUpdateKnowledge(CozeKnowledge cozeKnowledge);

    /**
     * 删除知识库
     */
    Boolean deleteByKnowledgeId(String cozeKnowledgeId, Integer merchantId);

    /**
     * 获取商户的所有知识库
     */
    List<CozeKnowledge> getAllByMerchantId(Integer merchantId);

    /**
     * 从Coze平台同步知识库列表到本地数据库
     * @param merchantId 商户ID
     * @param spaceId 空间ID
     * @return 同步结果
     */
    Boolean syncKnowledgeListFromCoze(Integer merchantId, String spaceId);

    /**
     * 调用Coze API获取知识库列表
     * @param spaceId 空间ID
     * @param name 知识库名称(可选)
     * @param formatType 知识库类型(可选)
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return Coze API响应
     */
    CozeKnowledgeListResponse getKnowledgeListFromCoze(String spaceId, String name, Integer formatType, Integer pageNum, Integer pageSize);

    /**
     * 将Coze API响应的知识库数据保存到本地数据库
     * @param merchantId 商户ID
     * @param datasets 知识库数据列表
     * @return 保存结果
     */
    Boolean saveKnowledgeFromCozeResponse(Integer merchantId, List<CozeKnowledgeListResponse.Dataset> datasets);
}
