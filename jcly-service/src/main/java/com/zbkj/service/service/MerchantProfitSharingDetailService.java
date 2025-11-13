package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.merchant.MerchantProfitSharingDetail;
import com.zbkj.common.request.PageParamRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商户分账明细记录表 服务类
 * </p>
 *
 * @author dudl
 * @since 2025-01-07
 */
public interface MerchantProfitSharingDetailService extends IService<MerchantProfitSharingDetail> {

    /**
     * 分页查询分账明细记录
     *
     * @param merId            商户ID
     * @param pageParamRequest 分页参数
     * @return 分页结果
     */
    PageInfo<MerchantProfitSharingDetail> getPage(Integer merId, PageParamRequest pageParamRequest);

    /**
     * 创建分账记录
     *
     * @param orderNo       订单号
     * @param transactionId 微信支付订单号
     * @param totalAmount   订单总金额
     * @param merId         商户ID
     * @return 是否成功
     */
    Boolean createSharingRecord(String orderNo, String transactionId, BigDecimal totalAmount, Integer merId);

    /**
     * 执行分账
     *
     * @param sharingId 分账记录ID
     * @return 是否成功
     */
    Boolean executeSharing(Integer sharingId);

    /**
     * 查询需要执行分账的订单
     *
     * @return 分账记录列表
     */
    List<MerchantProfitSharingDetail> getPendingSharingList();

    /**
     * 查询需要重试的分账记录
     *
     * @return 重试记录列表
     */
    List<MerchantProfitSharingDetail> getRetryList();

    /**
     * 更新分账状态
     *
     * @param sharingId     分账记录ID
     * @param sharingStatus 分账状态
     * @param wechatResult  微信返回结果
     * @param errorCode     错误代码
     * @param errorMsg      错误信息
     * @return 是否成功
     */
    Boolean updateSharingStatus(Integer sharingId, String sharingStatus, String wechatResult, String errorCode, String errorMsg);

    /**
     * 根据订单号查询分账记录
     *
     * @param orderNo 订单号
     * @return 分账记录
     */
    MerchantProfitSharingDetail getByOrderNo(String orderNo);

    /**
     * 生成分账单号
     *
     * @return 分账单号
     */
    String generateSharingNo();

    /**
     * 重试分账
     *
     * @param sharingId 分账记录ID
     * @return 是否成功
     */
    Boolean retrySharing(Integer sharingId);

    /**
     * 获取分账统计数据
     *
     * @param merId 商户ID
     * @return 统计数据
     */
    Map<String, Object> getProfitSharingStats(Integer merId);
} 