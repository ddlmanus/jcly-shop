package com.zbkj.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.invoice.InvoiceDescription;
import com.zbkj.common.request.InvoiceDescriptionRequest;
import com.zbkj.service.dao.InvoiceDescriptionDao;
import com.zbkj.service.service.InvoiceDescriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 发票说明服务实现类
 * +----------------------------------------------------------------------
 * | JCLY [ JCLY赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 * +----------------------------------------------------------------------
 * | Author: dudl
 * +----------------------------------------------------------------------
 */
@Slf4j
@Service
public class InvoiceDescriptionServiceImpl extends ServiceImpl<InvoiceDescriptionDao, InvoiceDescription> implements InvoiceDescriptionService {

    /**
     * 获取发票说明
     */
    @Override
    public InvoiceDescription getInvoiceDescription() {
        LambdaQueryWrapper<InvoiceDescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvoiceDescription::getIsShow, true);
        wrapper.orderByAsc(InvoiceDescription::getSort);
        wrapper.last("LIMIT 1");
        
        InvoiceDescription description = getOne(wrapper);
        if (ObjectUtil.isNull(description)) {
            // 如果没有记录，创建默认记录
            description = new InvoiceDescription();
            description.setTitle("发票说明");
            description.setContent("<p>这是发票说明</p><p>请在此编辑发票相关说明内容...</p>");
            description.setIsShow(true);
            description.setSort(0);
            description.setCreateTime(new Date());
            description.setUpdateTime(new Date());
            save(description);
        }
        
        return description;
    }

    /**
     * 更新发票说明
     */
    @Override
    public Boolean updateInvoiceDescription(InvoiceDescriptionRequest request) {
        InvoiceDescription description;
        
        if (ObjectUtil.isNotNull(request.getId()) && request.getId() > 0) {
            // 更新现有记录
            description = getById(request.getId());
            if (ObjectUtil.isNull(description)) {
                throw new RuntimeException("发票说明记录不存在");
            }
        } else {
            // 获取第一条记录或创建新记录
            description = getInvoiceDescription();
        }
        
        // 更新字段
        description.setTitle(request.getTitle());
        description.setContent(request.getContent());
        description.setIsShow(request.getIsShow());
        if (ObjectUtil.isNotNull(request.getSort())) {
            description.setSort(request.getSort());
        }
        description.setUpdateTime(new Date());
        
        return updateById(description);
    }
} 