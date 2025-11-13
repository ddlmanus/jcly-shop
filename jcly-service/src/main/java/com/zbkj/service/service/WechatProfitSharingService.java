package com.zbkj.service.service;

import com.zbkj.common.model.merchant.MerchantProfitSharingDetail;
import com.zbkj.common.model.merchant.MerchantProfitSharingReceiver;

import java.util.Map;

/**
 * <p>
 * 微信分账API调用服务
 * </p>
 *
 * @author dudl
 * @since 2025-01-07
 */
public interface WechatProfitSharingService {

    /**
     * 添加分账接收方
     *
     * @param receiver 接收方信息
     * @return 微信接口返回结果
     */
    Map<String, Object> addReceiver(MerchantProfitSharingReceiver receiver);

    /**
     * 删除分账接收方
     *
     * @param receiver 接收方信息
     * @return 微信接口返回结果
     */
    Map<String, Object> removeReceiver(MerchantProfitSharingReceiver receiver);

    /**
     * 请求单次分账
     *
     * @param detail 分账明细
     * @return 微信接口返回结果
     */
    Map<String, Object> profitSharing(MerchantProfitSharingDetail detail);

    /**
     * 查询分账结果
     *
     * @param outOrderNo    商户分账单号
     * @param transactionId 微信支付订单号
     * @return 微信接口返回结果
     */
    Map<String, Object> queryProfitSharing(String outOrderNo, String transactionId);

    /**
     * 完结分账
     *
     * @param transactionId 微信支付订单号
     * @param outOrderNo    商户分账单号
     * @param description   分账描述
     * @return 微信接口返回结果
     */
    Map<String, Object> finishProfitSharing(String transactionId, String outOrderNo, String description);

    /**
     * 分账回退
     *
     * @param outOrderNo     原分账单号
     * @param outReturnNo    商户回退单号
     * @param returnAccount  回退接收方账户
     * @param returnAmount   回退金额
     * @param description    回退描述
     * @return 微信接口返回结果
     */
    Map<String, Object> profitSharingReturn(String outOrderNo, String outReturnNo, String returnAccount, Integer returnAmount, String description);

    /**
     * 回退结果查询
     *
     * @param outOrderNo  原分账单号
     * @param outReturnNo 商户回退单号
     * @return 微信接口返回结果
     */
    Map<String, Object> queryProfitSharingReturn(String outOrderNo, String outReturnNo);
} 