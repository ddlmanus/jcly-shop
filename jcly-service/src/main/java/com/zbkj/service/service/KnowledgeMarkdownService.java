package com.zbkj.service.service;

import com.zbkj.common.model.coze.CozeKnowledgeFile;

/**
 * 知识库Markdown文件生成和上传服务
 * 
 * @author Auto Generator
 * @since 2024-01-01
 */
public interface KnowledgeMarkdownService {

    /**
     * 生成优惠券MD文件并上传到Coze知识库
     * 
     * @param couponId 优惠券ID
     * @param knowledgeId Coze知识库ID
     * @param merchantId 商户ID
     * @return 上传结果
     */
    CozeKnowledgeFile generateAndUploadCouponMarkdown(Integer couponId, String knowledgeId, Integer merchantId);

    /**
     * 生成拼团活动MD文件并上传到Coze知识库
     * 
     * @param activityId 拼团活动ID
     * @param knowledgeId Coze知识库ID
     * @param merchantId 商户ID
     * @return 上传结果
     */
    CozeKnowledgeFile generateAndUploadGroupBuyMarkdown(Integer activityId, String knowledgeId, Integer merchantId);

    /**
     * 生成秒杀活动MD文件并上传到Coze知识库
     * 
     * @param activityId 秒杀活动ID
     * @param knowledgeId Coze知识库ID
     * @param merchantId 商户ID
     * @return 上传结果
     */
    CozeKnowledgeFile generateAndUploadSeckillMarkdown(Integer activityId, String knowledgeId, Integer merchantId);

    /**
     * 生成积分商品MD文件并上传到Coze知识库
     * 
     * @param productId 积分商品ID
     * @param knowledgeId Coze知识库ID
     * @param merchantId 商户ID
     * @return 上传结果
     */
    CozeKnowledgeFile generateAndUploadIntegralProductMarkdown(Integer productId, String knowledgeId, Integer merchantId);

    /**
     * 通用方法：生成MD文件内容并上传到Coze知识库
     * 
     * @param content MD文件内容
     * @param fileName 文件名
     * @param knowledgeId Coze知识库ID
     * @param merchantId 商户ID
     * @return 上传结果
     */
    CozeKnowledgeFile uploadMarkdownToKnowledge(String content, String fileName, String knowledgeId, Integer merchantId);
}