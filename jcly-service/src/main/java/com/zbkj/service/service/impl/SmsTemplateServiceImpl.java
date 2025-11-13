package com.zbkj.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.sms.SmsTemplate;
import com.zbkj.common.result.SystemConfigResultCode;
import com.zbkj.service.dao.SmsTemplateDao;
import com.zbkj.service.service.SmsTemplateService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * SmsTemplateServiceImpl 接口实现
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
@Service
public class SmsTemplateServiceImpl extends ServiceImpl<SmsTemplateDao, SmsTemplate> implements SmsTemplateService {

    @Resource
    private SmsTemplateDao dao;

    /**
     * 获取详情
     * @param id 模板id
     * @return SmsTemplate
     */
    @Override
    public SmsTemplate getDetail(Integer id) {
        SmsTemplate smsTemplate = getById(id);
        if (ObjectUtil.isNull(smsTemplate)) {
            throw new CrmebException(SystemConfigResultCode.SMS_TEMPLATE_NOT_EXIST);
        }
        return smsTemplate;
    }
}

