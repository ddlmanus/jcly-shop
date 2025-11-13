package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeWorkflow;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.service.dao.CozeWorkflowDao;
import com.zbkj.service.service.CozeWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * CozeWorkflow 工作流服务实现类
 * </p>
 *
 * @author Auto Generator
 * @since 2024-01-01
 */
@Slf4j
@Service
public class CozeWorkflowServiceImpl extends ServiceImpl<CozeWorkflowDao, CozeWorkflow> implements CozeWorkflowService {

    @Override
    public PageInfo<CozeWorkflow> getByMerchantId(Integer merchantId, PageParamRequest pageParamRequest) {
        com.github.pagehelper.Page<Object> objects = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<CozeWorkflow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeWorkflow::getMerchantId, merchantId);
        wrapper.eq(CozeWorkflow::getStatus, 1);
        wrapper.orderByDesc(CozeWorkflow::getCreateTime);
        List<CozeWorkflow> cozeWorkflows = this.baseMapper.selectList(wrapper);
        return CommonPage.copyPageInfo(objects,cozeWorkflows);
    }

    @Override
    public CozeWorkflow getByCozeWorkflowId(String cozeWorkflowId,Integer merchantId) {
        LambdaQueryWrapper<CozeWorkflow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeWorkflow::getCozeWorkflowId, cozeWorkflowId);
        wrapper.eq(CozeWorkflow::getMerchantId, merchantId);
        wrapper.eq(CozeWorkflow::getStatus, 1);
        return getOne(wrapper);
    }

    @Override
    public Boolean saveOrUpdateWorkflow(CozeWorkflow cozeWorkflow) {
        try {
            if (cozeWorkflow.getId() != null) {
                // 更新
                cozeWorkflow.setUpdateTime(new Date());
                return updateById(cozeWorkflow);
            } else {
                // 新增，先检查是否已存在相同的Coze工作流ID
                CozeWorkflow existing = getByCozeWorkflowId(cozeWorkflow.getCozeWorkflowId(), cozeWorkflow.getMerchantId());
                if (existing != null) {
                    // 更新现有记录
                    existing.setName(cozeWorkflow.getName());
                    existing.setDescription(cozeWorkflow.getDescription());
                    existing.setIconUrl(cozeWorkflow.getIconUrl());
                    existing.setWorkflowMode(cozeWorkflow.getWorkflowMode());
                    existing.setAppId(cozeWorkflow.getAppId());
                    existing.setPublishStatus(cozeWorkflow.getPublishStatus());
                    existing.setCreatorId(cozeWorkflow.getCreatorId());
                    existing.setCreatorName(cozeWorkflow.getCreatorName());
                    existing.setUpdateTime(new Date());
                    return updateById(existing);
                } else {
                    // 新增
                    cozeWorkflow.setCreateTime(new Date());
                    cozeWorkflow.setUpdateTime(new Date());
                    return save(cozeWorkflow);
                }
            }
        } catch (Exception e) {
            log.error("保存或更新工作流失败", e);
            return false;
        }
    }

    @Override
    public Boolean deleteByCozeWorkflowId(String cozeWorkflowId, Integer merchantId) {
        LambdaQueryWrapper<CozeWorkflow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeWorkflow::getCozeWorkflowId, cozeWorkflowId);
        wrapper.eq(CozeWorkflow::getMerchantId, merchantId);
        
        CozeWorkflow workflow = getOne(wrapper);
        if (workflow == null) {
            return false;
        }
        
        // 软删除
        workflow.setStatus(0);
        workflow.setUpdateTime(new Date());
        return updateById(workflow);
    }

    @Override
    public List<CozeWorkflow> getAllByMerchantId(Integer merchantId) {
        LambdaQueryWrapper<CozeWorkflow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CozeWorkflow::getMerchantId, merchantId);
        wrapper.eq(CozeWorkflow::getStatus, 1);
        wrapper.orderByDesc(CozeWorkflow::getCreateTime);
        return list(wrapper);
    }
}
