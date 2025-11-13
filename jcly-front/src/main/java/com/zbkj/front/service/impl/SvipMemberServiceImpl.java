package com.zbkj.front.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.zbkj.common.config.CrmebConfig;
import com.zbkj.common.constants.*;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.member.PaidMemberCard;
import com.zbkj.common.model.member.PaidMemberOrder;
import com.zbkj.common.model.system.GroupConfig;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserBalanceRecord;
import com.zbkj.common.model.user.UserToken;
import com.zbkj.common.request.PaySvipOrderRequest;
import com.zbkj.common.response.*;
import com.zbkj.common.result.CommonResultCode;
import com.zbkj.common.result.MemberResultCode;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.common.utils.RequestUtil;
import com.zbkj.common.vo.*;
import com.zbkj.front.service.SvipMemberService;
import com.zbkj.service.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 会员服务实现类
 *
 * @author Han
 * @version 1.0.0
 * @Date 2024/5/14
 */
@Service
public class SvipMemberServiceImpl implements SvipMemberService {

    private Logger logger = LoggerFactory.getLogger(SvipMemberServiceImpl.class);

    @Autowired
    private UserService userService;
    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private GroupConfigService groupConfigService;
    @Autowired
    private PaidMemberCardService paidMemberCardService;
    @Autowired
    private PaidMemberOrderService paidMemberOrderService;
    @Autowired
    private WechatService wechatService;
    @Autowired
    private UserTokenService userTokenService;
    @Autowired
    private AliPayService aliPayService;
    @Autowired
    private UserBalanceRecordService userBalanceRecordService;
    @Autowired
    private CrmebConfig crmebConfig;
    @Autowired
    private WechatPayService wechatPayService;

    /**
     * 获取svip会员中心信息
     */
    @Override
    public SvipInfoResponse getSvipInfo() {
        SvipInfoResponse response = new SvipInfoResponse();
        boolean isPermanentPaidMember = false;
        Integer uid = userService.getUserId();
        if (uid > 0) {
            User user = userService.getById(uid);
            isPermanentPaidMember = user.getIsPermanentPaidMember();
            response.setNickname(user.getNickname());
            response.setAvatar(user.getAvatar());
            response.setPhone(CrmebUtil.maskMobile(user.getPhone()));
            response.setIsPaidMember(user.getIsPaidMember());
            response.setIsPermanentPaidMember(user.getIsPermanentPaidMember());
            response.setPaidMemberExpirationTime(user.getPaidMemberExpirationTime());
        }
        response.setBenefitsList(getSvipInfoBenefitsList());
        if (isPermanentPaidMember) {
            response.setCardList(new ArrayList<>());
        } else {
            response.setCardList(getSvipCardList(uid));
        }
        return response;
    }

    /**
     * 获取svip会员权益列表
     */
    @Override
    public List<SvipBenefitsExplainResponse> getSvipBenefitsList() {
        List<GroupConfig> configList = getBaseSvipBenefitsList();
        List<SvipBenefitsExplainResponse> benefitsResponseList = new ArrayList<>();
        if (CollUtil.isNotEmpty(configList)) {
            Iterator<GroupConfig> iterator = configList.iterator();
            while (iterator.hasNext()) {
                GroupConfig config = iterator.next();
                SvipBenefitsExplainResponse benefitsResponse = new SvipBenefitsExplainResponse();
                benefitsResponse.setValue(config.getValue());
                benefitsResponse.setImageUrl(config.getImageUrl());
                benefitsResponse.setExpand(config.getExpand());
                benefitsResponseList.add(benefitsResponse);
            }
        }
        return benefitsResponseList;
    }

    private List<SvipCardResponse> getSvipCardList(Integer uid) {
        Boolean isPayTrial = paidMemberOrderService.userIsPayTrial(uid);
        return paidMemberCardService.findFrontList(isPayTrial);
    }

    private List<SvipBenefitsResponse> getSvipInfoBenefitsList() {
        List<GroupConfig> configList = getBaseSvipBenefitsList();
        List<SvipBenefitsResponse> benefitsResponseList = new ArrayList<>();
        if (CollUtil.isNotEmpty(configList)) {
            Iterator<GroupConfig> iterator = configList.iterator();
            while (iterator.hasNext()) {
                GroupConfig config = iterator.next();
                SvipBenefitsResponse benefitsResponse = new SvipBenefitsResponse();
                benefitsResponse.setValue(config.getValue());
                benefitsResponse.setMessage(config.getMessage());
                benefitsResponse.setImageUrl(config.getImageUrl());
                benefitsResponseList.add(benefitsResponse);
            }
        }
        return benefitsResponseList;
    }

    private List<GroupConfig> getBaseSvipBenefitsList() {
        List<GroupConfig> configList = groupConfigService.findByTag(GroupConfigConstants.TAG_PAID_MEMBER_BENEFITS, Constants.SORT_DESC, null);
        for (int i = 0; i < configList.size(); ) {
            GroupConfig groupConfig = configList.get(i);
            if (!groupConfig.getStatus()) {
                configList.remove(i);
                continue;
            }
            i++;
        }
        return configList;
    }

    /**
     * 生成购买svip会员订单
     *
     */
    @Override
    public OrderPayResultResponse paySvipOrderCreate(PaySvipOrderRequest request) {
        User user = userService.getInfo();
        if (user.getIsPermanentPaidMember()) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "用户已经是永久会员");
        }
        PaidMemberCard card = paidMemberCardService.getByIdException(request.getCardId());
        if (!card.getStatus()) {
            throw new CrmebException(MemberResultCode.PAID_MEMBER_CARD_NOT_EXIST);
        }
        // 试用会员卡只能购买一次
        if (card.getType().equals(0) && paidMemberOrderService.userIsPayTrial(user.getId())) {
            throw new CrmebException(MemberResultCode.PAID_MEMBER_CARD_NOT_EXIST);
        }
        PaidMemberOrder order = new PaidMemberOrder();
        order.setUid(user.getId());
        order.setOrderNo(CrmebUtil.getOrderNo(OrderConstants.PAID_MEMBER_ORDER_PREFIX));
        order.setCardId(card.getId());
        order.setCardName(card.getName());
        order.setType(card.getType());
        order.setDeadlineDay(card.getDeadlineDay());
        order.setOriginalPrice(card.getOriginalPrice());
        order.setPrice(card.getPrice());
        order.setGiftBalance(BigDecimal.ZERO);
        if (card.getGiftBalance().compareTo(BigDecimal.ZERO) > 0) {
            if (card.getIsFirstChargeGive()) {
                    if (!paidMemberOrderService.userIsFirstPay(user.getId(), card.getId())) {
                        order.setGiftBalance(card.getGiftBalance());
                    }
            } else {
                order.setGiftBalance(card.getGiftBalance());
            }
        }
        order.setPayType(request.getPayType());
        order.setPayChannel(request.getPayChannel());
        order.setPaid(false);
        OrderPayResultResponse response = new OrderPayResultResponse();
        response.setPayChannel(request.getPayChannel());
        response.setPayType(request.getPayType());
        response.setOrderNo(order.getOrderNo());
        if (order.getPayType().equals(PayConstants.PAY_TYPE_YUE)) {
            if (user.getNowMoney().compareTo(order.getPrice()) < 0) {
                throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "用户余额不足");
            }
            boolean save = paidMemberOrderService.save(order);
            if (!save) {
                throw new CrmebException("生成svip订单失败!");
            }
            userYuePay(order, user.getId(), user.getNowMoney());
            Boolean yueBoolean = paidMemberOrderService.paySuccessAfter(order);
            if (!yueBoolean) {
                userService.updateNowMoney(user.getId(), order.getPrice(), Constants.OPERATION_TYPE_ADD);
            }
            response.setStatus(yueBoolean);
            return response;
        }
        if (request.getPayType().equals(PayConstants.PAY_TYPE_WE_CHAT)) {
            WxPayJsResultVo vo = wechatPayment(order.getPrice(), request.getPayChannel(), user.getId());
            order.setOutTradeNo(vo.getOutTradeNo());
            vo.setOutTradeNo(null);
            response.setJsConfig(vo);
        }
        if (request.getPayType().equals(PayConstants.PAY_TYPE_ALI_PAY)) {
            String result = aliPayService.pay(order.getOrderNo(), order.getPrice(), "svip", request.getPayChannel(), "");
            response.setAlipayRequest(result);
            order.setOutTradeNo(order.getOrderNo());
        }
        boolean save = paidMemberOrderService.save(order);
        if (!save) {
            throw new CrmebException("生成svip订单失败!");
        }
        return response;
    }

    private void userYuePay(PaidMemberOrder svipOrder, Integer userId, BigDecimal nowMoney) {
        Boolean yueBoolean = userService.updateNowMoney(userId, svipOrder.getPrice(), Constants.OPERATION_TYPE_SUBTRACT);
        if (!yueBoolean) {
            throw new CrmebException("用户余额扣除数据操作异常");
        }
        // 创建记录
        userBalanceRecordService.save(createBalanceRecord(svipOrder, nowMoney));
    }

    private UserBalanceRecord createBalanceRecord(PaidMemberOrder svipOrder, BigDecimal nowMoney) {
        UserBalanceRecord record = new UserBalanceRecord();
        record.setUid(svipOrder.getUid());
        record.setLinkId(svipOrder.getOrderNo());
        record.setLinkType(BalanceRecordConstants.BALANCE_RECORD_LINK_TYPE_SVIP);
        record.setType(BalanceRecordConstants.BALANCE_RECORD_TYPE_SUB);
        record.setAmount(svipOrder.getPrice());
        record.setBalance(nowMoney.subtract(svipOrder.getPrice()));
        record.setRemark(StrUtil.format(BalanceRecordConstants.BALANCE_RECORD_REMARK_SVIP_ORDER, svipOrder.getPrice()));
        return record;
    }

    /**
     * svip会员订单购买记录
     */
    @Override
    public List<SvipOrderRecordResponse> getSvipOrderRecord() {
        Integer userId = userService.getUserIdException();
        List<PaidMemberOrder> recordList = paidMemberOrderService.findUserSvipOrderRecord(userId);
        List<SvipOrderRecordResponse> responseList = new ArrayList<>();
        for (PaidMemberOrder record : recordList) {
            SvipOrderRecordResponse response = new SvipOrderRecordResponse();
            BeanUtils.copyProperties(record, response);
            responseList.add(response);
        }
        return responseList;
    }

    /**
     * 微信支付
     *
     * @param rechargePrice 充值金额
     * @param payChannel    支付渠道
     * @param uid           用户id
     * @return
     */
    private WxPayJsResultVo wechatPayment(BigDecimal rechargePrice, String payChannel, Integer uid) {
        // 根据订单支付类型来判断获取公众号openId还是小程序openId
        UserToken userToken = new UserToken();
        userToken.setToken("");
        if (payChannel.equals(PayConstants.PAY_CHANNEL_WECHAT_PUBLIC)) {// 公众号
            userToken = userTokenService.getTokenByUserId(uid, UserConstants.USER_TOKEN_TYPE_WECHAT);
        }
        if (payChannel.equals(PayConstants.PAY_CHANNEL_WECHAT_MINI)
                || payChannel.equals(PayConstants.PAY_CHANNEL_WECHAT_MINI_VIDEO)) {// 小程序
            userToken = userTokenService.getTokenByUserId(uid, UserConstants.USER_TOKEN_TYPE_ROUTINE);
        }
        // 从config表中获取H5/PC支付使用的是公众号还是小程序的
        String paySource = payChannel;
        if (paySource.equals(PayConstants.PAY_CHANNEL_H5) || paySource.equals(PayConstants.PAY_CHANNEL_WECHAT_NATIVE)) {
            String source = systemConfigService.getValueByKey(SysConfigConstants.WECHAT_PAY_SOURCE_H5_PC);
            if (StrUtil.isNotBlank(source) && source.equals(PayConstants.WECHAT_PAY_SOURCE_MINI)) {
                paySource = PayConstants.PAY_CHANNEL_WECHAT_MINI;
            } else {
                paySource = PayConstants.PAY_CHANNEL_WECHAT_PUBLIC;
            }
        }
        // 根据不同的版本调用对应的微信下单接口
        MyRecord myRecord;
        String outTradeNo;
        String apiDomain = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_KEY_API_URL);
        if ("V2".equals(crmebConfig.getWxPayVersion())) {
            WxPayUnifiedOrderRequest unifiedOrderRequest = buildWxPayUnifiedOrderRequest(rechargePrice, uid);
            outTradeNo = unifiedOrderRequest.getOutTradeNo();
            myRecord = wechatPayService.createOrder(unifiedOrderRequest, userToken.getToken(), payChannel, paySource, apiDomain);
        } else {
            WxPayUnifiedOrderV3Request v3Request = buildWxPayUnifiedOrderV3Request(rechargePrice, uid);
            outTradeNo = v3Request.getOutTradeNo();
            myRecord = wechatPayService.createV3Order(v3Request, userToken.getToken(), payChannel, paySource, apiDomain);
        }
        WxPayJsResultVo vo = new WxPayJsResultVo();
        vo.setOutTradeNo(outTradeNo);
        switch (payChannel) {
            case PayConstants.PAY_CHANNEL_WECHAT_MINI:
            case PayConstants.PAY_CHANNEL_WECHAT_MINI_VIDEO:
            case PayConstants.PAY_CHANNEL_WECHAT_PUBLIC:
                vo.setAppId(myRecord.getStr("appId"));
                vo.setNonceStr(myRecord.getStr("nonceStr"));
                vo.setPackages(myRecord.getStr("package"));
                vo.setSignType(myRecord.getStr("signType"));
                vo.setPaySign(myRecord.getStr("paySign"));
                vo.setTimeStamp(myRecord.getStr("timeStamp"));
                break;
            case PayConstants.PAY_CHANNEL_H5:
                vo.setMwebUrl(myRecord.getStr("mwebUrl"));
            case PayConstants.PAY_CHANNEL_WECHAT_NATIVE:
                vo.setMwebUrl(myRecord.getStr("codeUrl"));
            case PayConstants.PAY_CHANNEL_WECHAT_APP_IOS:
            case PayConstants.PAY_CHANNEL_WECHAT_APP_ANDROID:
                vo.setAppId(myRecord.getStr("appId"));
                vo.setPartnerid(myRecord.getStr("partnerId"));
                vo.setPrepayid(myRecord.getStr("prepayId"));
                vo.setPackages(myRecord.getStr("package"));
                vo.setNonceStr(myRecord.getStr("nonceStr"));
                vo.setTimeStamp(myRecord.getStr("timeStamp"));
                vo.setPaySign(myRecord.getStr("sign"));
        }
        return vo;
    }

    private WxPayUnifiedOrderRequest buildWxPayUnifiedOrderRequest(BigDecimal price, Integer uid) {
        WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
        // 因商品名称在微信侧超长更换为网站名称
        String siteName = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_KEY_SITE_NAME);
        orderRequest.setBody(siteName);

        AttachVo attachVo = new AttachVo(PayConstants.PAY_SERVICE_TYPE_SVIP, uid);
        orderRequest.setAttach(JSONObject.toJSONString(attachVo));
        orderRequest.setOutTradeNo(CrmebUtil.getOrderNo(OrderConstants.ORDER_PREFIX_WECHAT));
        orderRequest.setTotalFee(price.multiply(BigDecimal.TEN).multiply(BigDecimal.TEN).intValue());
        orderRequest.setSpbillCreateIp(RequestUtil.getClientIp());
        String domain = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_KEY_SITE_URL);
        CreateOrderH5SceneInfoVo createOrderH5SceneInfoVo = new CreateOrderH5SceneInfoVo(
                new CreateOrderH5SceneInfoDetailVo(
                        domain,
                        systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_KEY_SITE_NAME)
                )
        );
        orderRequest.setSceneInfo(JSONObject.toJSONString(createOrderH5SceneInfoVo));
        return orderRequest;
    }

    private WxPayUnifiedOrderV3Request buildWxPayUnifiedOrderV3Request(BigDecimal price, Integer uid) {
        WxPayUnifiedOrderV3Request orderRequest = new WxPayUnifiedOrderV3Request();
        // description【商品描述】不能超过127个字符。
        String siteName = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_KEY_SITE_NAME);
        orderRequest.setDescription(siteName);
        orderRequest.setOutTradeNo(CrmebUtil.getOrderNo(OrderConstants.ORDER_PREFIX_WECHAT));
        AttachVo attachVo = new AttachVo(PayConstants.PAY_SERVICE_TYPE_SVIP, uid);
        orderRequest.setAttach(JSONObject.toJSONString(attachVo));
        WxPayUnifiedOrderV3Request.Amount amount = new WxPayUnifiedOrderV3Request.Amount();
        amount.setTotal(price.multiply(BigDecimal.TEN).multiply(BigDecimal.TEN).intValue());
        orderRequest.setAmount(amount);
        return orderRequest;
    }

}
