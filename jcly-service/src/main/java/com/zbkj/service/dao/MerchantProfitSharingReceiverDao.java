package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.merchant.MerchantProfitSharingReceiver;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 分账接收方表 Mapper 接口
 * </p>
 *
 * @author dudl
 * @since 2025-01-07
 */
public interface MerchantProfitSharingReceiverDao extends BaseMapper<MerchantProfitSharingReceiver> {

    /**
     * 根据商户ID查询有效的接收方列表
     *
     * @param merId 商户ID
     * @return 接收方列表
     */
    List<MerchantProfitSharingReceiver> selectActiveByMerId(@Param("merId") Integer merId);

    /**
     * 根据商户ID和账户查询接收方
     *
     * @param merId   商户ID
     * @param account 账户
     * @return 接收方
     */
    MerchantProfitSharingReceiver selectByMerIdAndAccount(@Param("merId") Integer merId, @Param("account") String account);

    /**
     * 根据商户ID查询默认接收方
     *
     * @param merId 商户ID
     * @return 默认接收方
     */
    MerchantProfitSharingReceiver selectDefaultByMerId(@Param("merId") Integer merId);
} 