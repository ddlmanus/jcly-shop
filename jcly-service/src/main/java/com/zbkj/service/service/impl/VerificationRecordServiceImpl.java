package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.order.VerificationRecord;
import com.zbkj.service.dao.VerificationRecordDao;
import com.zbkj.service.service.VerificationRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 核销记录服务实现类
* @author dudl
* @description VerificationRecordServiceImpl 接口实现
* @date 2025-03-17
*/
@Service
public class VerificationRecordServiceImpl extends ServiceImpl<VerificationRecordDao, VerificationRecord> implements VerificationRecordService {

    @Resource
    private VerificationRecordDao dao;


    @Override
    public VerificationRecord getByOrderNo(String orderNo) {
        LambdaQueryWrapper<VerificationRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(VerificationRecord::getOrderNo, orderNo);
        lqw.last(" limit 1");
        return dao.selectOne(lqw);
    }
}

