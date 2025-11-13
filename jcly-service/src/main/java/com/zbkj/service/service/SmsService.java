package com.zbkj.service.service;

import com.zbkj.common.model.admin.SystemAdmin;

import java.math.BigDecimal;
import java.util.List;

/**
 * SmsService 接口
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
public interface SmsService {

    /**
     * 发送公共验证码
     *
     * @param phone 手机号
     * @param scenario 场景值
     */
    Boolean sendCommonCode(String phone, String scenario);

    /**
     * 发送短信
     *
     * @param mobile 手机号
     * @param content 短信内容
     * @return 发送结果
     */
    com.zbkj.common.vo.MyRecord sendSms(String mobile, String content);

    /**
     * 发送支付成功短信
     *
     * @param phone    手机号
     * @param orderNo  订单编号
     * @param payPrice 支付金额
     * @return Boolean
     */
    Boolean sendPaySuccess(String phone, String orderNo, BigDecimal payPrice, String name);

    /**
     * 发送订单发货提醒短信
     *
     * @param phone     手机号
     * @param nickName  用户昵称
     * @param storeName 商品名称
     * @param orderNo   订单编号
     */
    Boolean sendOrderDeliverNotice(String phone, String nickName, String storeName, String orderNo);

    /**
     * 发送订单拆分发货提醒短信
     *
     * @param phone     手机号
     * @param storeName 商品名称
     * @param orderNo   订单编号
     */
    Boolean sendOrderSplitDeliverNotice(String phone, String storeName, String orderNo);

    /**
     * 发送入驻审核通过通知短信
     * @param phone 手机号
     * @param date 日期，yyyy-MM-dd
     * @param merName 商户名
     * @param merPhone 商户店长手机号
     * @param pwd 商户店长密码
     * @param siteName 站点名称
     * @return Boolean
     */
    Boolean sendMerchantAuditSuccessNotice(String phone, String date, String merName, String merPhone, String pwd, String siteName);

    /**
     * 发送入驻审核未通过通知短信
     * @param phone 手机号
     * @param date 日期，yyyy-MM-dd
     * @param merName 商户名
     * @param siteName 站点名称
     * @return Boolean
     */
    Boolean sendMerchantFileSuccessNotice(String phone, String date, String merName, String siteName);

    /**
     * 发送生日礼短信
     * @param phone 手机号
     * @param name 祝福内容
     * @return Boolean
     */
    Boolean sendBirthdayPresent(String phone, String name);

    /**
     * 发送下单成功提醒（商户）
     * @param adminList 商户管理员列表
     * @param orderNo 订单号
     */
    Boolean sendPaySuccessToMerchant(List<SystemAdmin> adminList, String orderNo);

    /**
     * 校验手机号验证码
     * @param scenario 场景值
     * @param phone 手机号
     * @param code 验证码
     * @return 是否通过校验
     */
    Boolean checkValidateCode(String scenario, String phone, String code);

    /**
     * 商户入驻提醒（平台）
     * @param adminList 管理员列表
     */
    Boolean sendMerchantSettledApply(List<SystemAdmin> adminList, String merName);
}
