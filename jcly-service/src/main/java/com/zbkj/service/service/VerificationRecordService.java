package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.order.VerificationRecord;

/**
 * 核销记录服务
 *
 * @author dudl
 * @description VerificationRecordService 接口
 * @date 2025-03-17
 */
public interface VerificationRecordService extends IService<VerificationRecord> {

    VerificationRecord getByOrderNo(String orderNo);
}