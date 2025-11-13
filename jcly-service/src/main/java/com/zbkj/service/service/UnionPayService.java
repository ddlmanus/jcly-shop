package com.zbkj.service.service;

import com.zbkj.common.model.order.Order;
import com.zbkj.common.model.user.UserBankCard;
import com.zbkj.common.vo.BankCardVerifyRequestVo;
import com.zbkj.common.vo.BankCardVerifyResponseVo;

import java.util.Map;

public interface UnionPayService {
    String unionPay(Order order);
    String unionPay(Order order, UserBankCard bankCard, String smsCode);
    public String validate(Map<String, String> valideData, String encoding);
    public Map<String, String> queryOrder(String orderId, String txnTime);
    public Map<String, String> refundOrder(String orderId, String origTxnTime, java.math.BigDecimal refundAmount);
    public Map<String, String> returnOrder(String origQryId, java.math.BigDecimal returnAmount);
    public BankCardVerifyResponseVo verifyBankCard(BankCardVerifyRequestVo request);
}
