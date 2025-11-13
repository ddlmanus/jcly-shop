package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.merchant.MerchantProfitSharingApply;
import com.zbkj.common.request.PageParamRequest;

import java.util.Map;

/**
 * <p>
 * 商户分账申请记录表 服务类
 * </p>
 *
 * @author dudl
 * @since 2025-01-07
 */
public interface MerchantProfitSharingApplyService extends IService<MerchantProfitSharingApply> {

    /**
     * 分页查询分账申请记录
     *
     * @param merId             商户ID
     * @param pageParamRequest  分页参数
     * @return 分页结果
     */
    PageInfo<MerchantProfitSharingApply> getPage(Integer merId, PageParamRequest pageParamRequest);

    /**
     * 提交分账申请
     *
     * @param apply 申请信息
     * @return 是否成功
     */
    Boolean submitApply(MerchantProfitSharingApply apply);

    /**
     * 审核分账申请
     *
     * @param applyId     申请ID
     * @param applyStatus 申请状态
     * @param auditRemark 审核备注
     * @param auditorId   审核人ID
     * @return 是否成功
     */
    Boolean auditApply(Integer applyId, String applyStatus, String auditRemark, Integer auditorId);

    /**
     * 根据申请号获取申请记录
     *
     * @param applyNo 申请号
     * @return 申请记录
     */
    MerchantProfitSharingApply getByApplyNo(String applyNo);

    /**
     * 生成申请编号
     *
     * @return 申请编号
     */
    String generateApplyNo();

    /**
     * 获取分账申请统计数据
     *
     * @param merId 商户ID
     * @return 统计数据
     */
    Map<String, Object> getApplyStats(Integer merId);
} 