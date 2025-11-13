package com.zbkj.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Desc 腾讯云短信模版枚举类型
 * @author Mintimate
 */
@Getter
@AllArgsConstructor
public enum SmsTemplateEnum {
    /**
     * 短信登录
     */
    LOGIN("SMS_471840005","短信登录模版"),
    /**
     * 短信注册
     */
    REGISTER("SMS_474875116","短信注册模板"),
    /**
     * 密码重置
     */
    RESEAT_PASSWORD("SMS_474895110","密码重置模板"),
    /**
     * 修改注册手机号
     */
    REGISTER_MOBLE("SMS_471760018","修改注册手机号"),

    ALISMS_LOGIN("SMS_474850109","短信验证"),
    
    /**
     * 支付成功通知
     */
    PAY_SUCCESS("SMS_491430573","支付成功通知"),
    
    /**
     * 发货通知
     */
    DELIVER_NOTICE("SMS_491320648","发货通知"),
    
    /**
     * 拆分发货通知
     */
    SPLIT_DELIVER("SMS_SPLIT_DELIVER_TEMPLATE","拆分发货通知"),
    
    /**
     * 审核成功通知
     */
    AUDIT_SUCCESS("SMS_AUDIT_SUCCESS_TEMPLATE","审核成功通知"),
    
    /**
     * 审核失败通知
     */
    AUDIT_FAIL("SMS_AUDIT_FAIL_TEMPLATE","审核失败通知"),
    
    /**
     * 生日祝福
     */
    BIRTHDAY("SMS_BIRTHDAY_TEMPLATE","生日祝福"),
    
    /**
     * 商户支付成功提醒
     */
    MERCHANT_PAY("SMS_MERCHANT_PAY_TEMPLATE","商户支付成功提醒"),
    
    /**
     * 商户入驻申请提醒
     */
    MERCHANT_APPLY("SMS_MERCHANT_APPLY_TEMPLATE","商户入驻申请提醒");
    
    private final String TemplateID;
    private final String TemplateDesc;
}