package com.zbkj.common.constants;

/**
 * 支付宝配置
 *  +----------------------------------------------------------------------
 *  | JCLY [ JCLY赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 *  +----------------------------------------------------------------------
 *  | Author: dudl
 *  +----------------------------------------------------------------------
 */
public class AlipayConfig {

    // 商户appid
    public static String APPID = "ali_pay_appid";

    // 私钥 pkcs8格式的
    public static String RSA_PRIVATE_KEY = "ali_pay_private_key";

    // 服务器异步通知页面路径 需http://或者https://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String notify_url = "ali_pay_notifu_url";

    // 页面跳转同步通知页面路径 需http://或者https://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问 商户可以自定义同步跳转地址
    public static String return_url = "ali_pay_return_url";

    // 用户付款中途退出返回商户网站的地址
    public static String quit_url = "ali_pay_quit_url";

    // 请求网关地址
    public static String URL = "https://openapi.alipay.com/gateway.do";

    // 编码
    public static String CHARSET = "UTF-8";

    // 返回格式
    public static String FORMAT = "json";

    // 支付宝公钥
    public static String ALIPAY_PUBLIC_KEY_2 = "ali_pay_public_key2";
    public static String ALIPAY_PUBLIC_KEY = "ali_pay_public_key";

    //支付宝证书
    /**
     * 应用证书
     */
    public  static String ALIPAY_CERT_PATH="ali_pay_cert_path";

    /**
     * 支付宝公钥
     */
    public static String ALIPAY_PUBLIC_CERT_PATH="ali_pay_alipay_public_cert_path";

    /**
     * 支付宝根证书
     */
    public static String ALIPAY_ROOT_CERT_PATH="ali_pay_root_cert_path";
    // 日志记录目录
    public static String LOG_PATH = "/log";
    // RSA2
    public static String SIGNTYPE = "RSA2";

    // 是否开启支付宝支付
    public static String ALIPAY_IS_OPEN = "ali_pay_is_open";

    // 页面跳转同步通知页面路径（充值）
    public static String recharge_return_url = "ali_pay_recharge_return_url";

    // 用户付款中途退出返回商户网站的地址（充值）
    public static String recharge_quit_url = "ali_pay_recharge_quit_url";
    // 页面跳转同步通知页面路径（付费会员）
    public static String svip_return_url = "ali_pay_svip_return_url";

    // 用户付款中途退出返回商户网站的地址（付费会员）
    public static String svip_quit_url = "ali_pay_svip_quit_url";
    
    // 是否启用证书模式
    public static String ALIPAY_USE_CERT_MODE = "ali_pay_use_cert_mode";

    public static String ALIPAY_PRIVATE_KEY_CERT = "ali_pay_private_key_cert";
}
