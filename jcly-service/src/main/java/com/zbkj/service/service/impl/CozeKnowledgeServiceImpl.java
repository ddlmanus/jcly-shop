package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeKnowledge;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.coze.CozeKnowledgeListResponse;
import com.zbkj.service.dao.CozeKnowledgeDao;
import com.zbkj.service.service.CozeKnowledgeService;
import com.zbkj.service.service.CozeKnowledgeFileService;
import com.zbkj.service.service.CozeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * CozeKnowledge 知识库服务实现类
 * </p>
 *
 * @author Auto Generator
 * @since 2024-01-01
 */
@Slf4j
@Service
public class CozeKnowledgeServiceImpl extends ServiceImpl<CozeKnowledgeDao, CozeKnowledge> implements CozeKnowledgeService {

    @Autowired
    private CozeService cozeService;

    @Autowired
    private CozeKnowledgeFileService cozeKnowledgeFileService;

    @Override
    public CozeKnowledge getByCozeKnowledgeId(String cozeKnowledgeId,Integer merchantId) {
        LambdaQueryWrapper<CozeKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeKnowledge::getCozeKnowledgeId, cozeKnowledgeId);
        wrapper.eq(CozeKnowledge::getMerchantId, merchantId);
        wrapper.eq(CozeKnowledge::getStatus, 1);
        return getOne(wrapper);
    }

    @Override
    public PageInfo<CozeKnowledge> getByMerchantId(Integer merchantId, PageParamRequest pageParamRequest) {
        Page<CozeKnowledge> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<CozeKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeKnowledge::getMerchantId, merchantId);
        wrapper.eq(CozeKnowledge::getStatus, 1);
        wrapper.orderByDesc(CozeKnowledge::getCreateTime);
        List<CozeKnowledge> cozeKnowledges = this.baseMapper.selectList(wrapper);
        return CommonPage.copyPageInfo(page,cozeKnowledges);
    }

    @Override
    public CozeKnowledge saveOrUpdateKnowledge(CozeKnowledge cozeKnowledge) {
        Date now = new Date();
        
        if (cozeKnowledge.getId() == null) {
            // 新增
            cozeKnowledge.setCreateTime(now);
            cozeKnowledge.setUpdateTime(now);
            if (cozeKnowledge.getStatus() == null) {
                cozeKnowledge.setStatus(1);
            }
        } else {
            // 更新
            cozeKnowledge.setUpdateTime(now);
        }
        
        saveOrUpdate(cozeKnowledge);
        return cozeKnowledge;
    }

    @Override
    public Boolean deleteByKnowledgeId(String cozeKnowledgeId, Integer merchantId) {
        LambdaQueryWrapper<CozeKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeKnowledge::getCozeKnowledgeId, cozeKnowledgeId);
        wrapper.eq(CozeKnowledge::getMerchantId, merchantId);
        
        CozeKnowledge cozeKnowledge = getOne(wrapper);
        if (cozeKnowledge == null) {
            return false;
        }
        
        // 软删除，设置状态为0
        cozeKnowledge.setStatus(0);
        cozeKnowledge.setUpdateTime(new Date());
        return updateById(cozeKnowledge);
    }

    @Override
    public List<CozeKnowledge> getAllByMerchantId(Integer merchantId) {
        LambdaQueryWrapper<CozeKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeKnowledge::getMerchantId, merchantId);
        wrapper.eq(CozeKnowledge::getStatus, 1);
        wrapper.orderByDesc(CozeKnowledge::getCreateTime);
        return list(wrapper);
    }

    @Override
    public Boolean syncKnowledgeListFromCoze(Integer merchantId, String spaceId) {
        try {
            log.info("开始同步知识库列表，商户ID：{}，空间ID：{}", merchantId, spaceId);
            
            // 获取Coze平台的知识库列表（分页获取所有数据）
            int pageNum = 1;
            int pageSize = 100;
            boolean hasMore = true;
            List<String> syncedKnowledgeIds = new java.util.ArrayList<>();
            
            while (hasMore) {
                CozeKnowledgeListResponse response = getKnowledgeListFromCoze(spaceId, null, null, pageNum, pageSize);
                
                if (response != null && response.getData() != null && !CollectionUtils.isEmpty(response.getData().getDatasetList())) {
                    // 保存到本地数据库
                    Boolean saveResult = saveKnowledgeFromCozeResponse(merchantId, response.getData().getDatasetList());
                    if (!saveResult) {
                        log.error("保存知识库数据失败，商户ID：{}，页码：{}", merchantId, pageNum);
                        return false;
                    }
                    
                    // 收集知识库ID，用于后续同步文件
                    for (CozeKnowledgeListResponse.Dataset dataset : response.getData().getDatasetList()) {
                        syncedKnowledgeIds.add(dataset.getDatasetId());
                    }
                    
                    // 判断是否还有更多数据
                    hasMore = response.getData().getDatasetList().size() >= pageSize;
                    pageNum++;
                } else {
                    hasMore = false;
                }
            }
            
            log.info("知识库列表同步完成，商户ID：{}，开始同步知识库文件", merchantId);
            
            // 同步每个知识库的文件信息
            int successCount = 0;
            int totalCount = syncedKnowledgeIds.size();
            
            for (String knowledgeId : syncedKnowledgeIds) {
                try {
                    Boolean fileSyncResult = cozeKnowledgeFileService.syncKnowledgeFileListFromCoze(merchantId, knowledgeId);
                    if (fileSyncResult) {
                        successCount++;
                        log.info("知识库文件同步成功，知识库ID：{}", knowledgeId);
                    } else {
                        log.warn("知识库文件同步失败，知识库ID：{}", knowledgeId);
                    }
                } catch (Exception e) {
                    log.error("同步知识库文件时出现异常，知识库ID：{}，错误：{}", knowledgeId, e.getMessage(), e);
                }
            }
            
            log.info("知识库和文件同步完成，商户ID：{}，知识库总数：{}，文件同步成功数：{}", merchantId, totalCount, successCount);
            return true;
        } catch (Exception e) {
            log.error("同步知识库列表失败，商户ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public CozeKnowledgeListResponse getKnowledgeListFromCoze(String spaceId, String name, Integer formatType, Integer pageNum, Integer pageSize) {
        try {
            return cozeService.getKnowledgeListTyped(spaceId, name, formatType, pageNum, pageSize);
        } catch (Exception e) {
            log.error("调用Coze API获取知识库列表失败，空间ID：{}，错误信息：{}", spaceId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Boolean saveKnowledgeFromCozeResponse(Integer merchantId, List<CozeKnowledgeListResponse.Dataset> datasets) {
        if (CollectionUtils.isEmpty(datasets)) {
            return true;
        }
        
        try {
            Date now = new Date();
            
            for (CozeKnowledgeListResponse.Dataset dataset : datasets) {
                // 检查本地是否已存在该知识库
                CozeKnowledge existingKnowledge = getByCozeKnowledgeId(dataset.getDatasetId(),merchantId);
                
                CozeKnowledge knowledge;
                if (existingKnowledge != null) {
                    // 更新现有知识库
                    knowledge = existingKnowledge;
                } else {
                    // 创建新知识库
                    knowledge = new CozeKnowledge();
                    knowledge.setMerchantId(merchantId);
                    knowledge.setCozeKnowledgeId(dataset.getDatasetId());
                    knowledge.setCreateTime(now);
                    knowledge.setStatus(1);
                }
                
                // 更新知识库信息
                knowledge.setName(dataset.getName());
                knowledge.setDescription(dataset.getDescription());
                knowledge.setIconUri(dataset.getIconUri());
                knowledge.setIconUrl(dataset.getIconUrl());
                knowledge.setSpaceId(dataset.getSpaceId());
                knowledge.setDocCount(dataset.getDocCount());
                knowledge.setHitCount(dataset.getHitCount());
                knowledge.setSliceCount(dataset.getSliceCount());
                knowledge.setAllFileSize(dataset.getAllFileSize());
                knowledge.setBotUsedCount(dataset.getBotUsedCount());
                knowledge.setFormatType(dataset.getFormatType());
                knowledge.setDatasetType(dataset.getDatasetType());
                knowledge.setCreatorId(dataset.getCreatorId());
                knowledge.setCreatorName(dataset.getCreatorName());
                knowledge.setAvatarUrl(dataset.getAvatarUrl());
                knowledge.setCanEdit(dataset.getCanEdit());
                
                // 处理JSON字段
                if (dataset.getChunkStrategy() != null) {
                    knowledge.setChunkStrategy(com.alibaba.fastjson.JSON.toJSONString(dataset.getChunkStrategy()));
                }
                if (!CollectionUtils.isEmpty(dataset.getFileList())) {
                    knowledge.setFileList(com.alibaba.fastjson.JSON.toJSONString(dataset.getFileList()));
                }
                if (!CollectionUtils.isEmpty(dataset.getFailedFileList())) {
                    knowledge.setFailedFileList(com.alibaba.fastjson.JSON.toJSONString(dataset.getFailedFileList()));
                }
                if (!CollectionUtils.isEmpty(dataset.getProcessingFileList())) {
                    knowledge.setProcessingFileList(com.alibaba.fastjson.JSON.toJSONString(dataset.getProcessingFileList()));
                }
                if (!CollectionUtils.isEmpty(dataset.getProcessingFileIdList())) {
                    knowledge.setProcessingFileIdList(com.alibaba.fastjson.JSON.toJSONString(dataset.getProcessingFileIdList()));
                }
                
                knowledge.setCozeCreateTime(dataset.getCreateTime());
                knowledge.setCozeUpdateTime(dataset.getUpdateTime());
                knowledge.setSyncTime(now);
                knowledge.setUpdateTime(now);
                
                // 保存或更新
                saveOrUpdate(knowledge);
            }
            
            return true;
        } catch (Exception e) {
            log.error("保存知识库数据失败，商户ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return false;
        }
    }
}
