package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.merchant.MerchantProfitSharingDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 商户分账明细记录表 Mapper 接口
 * </p>
 *
 * @author dudl
 * @since 2025-01-07
 */
public interface MerchantProfitSharingDetailDao extends BaseMapper<MerchantProfitSharingDetail> {

    /**
     * 查询需要重试的分账记录
     *
     * @return 需要重试的分账记录列表
     */
    List<MerchantProfitSharingDetail> selectRetryList();

    /**
     * 根据订单号查询分账记录
     *
     * @param orderNo 订单号
     * @return 分账记录
     */
    MerchantProfitSharingDetail selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据商户ID和状态查询分账记录
     *
     * @param merId         商户ID
     * @param sharingStatus 分账状态
     * @return 分账记录列表
     */
    List<MerchantProfitSharingDetail> selectByMerIdAndStatus(@Param("merId") Integer merId, @Param("sharingStatus") String sharingStatus);
} 