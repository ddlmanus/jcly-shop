package com.zbkj.common.constants;

/**
 * 阿里云短信配置常量
 */
public class AliyunSmsConst {
    /** 
     * AccessKey ID - 建议从系统配置中读取 
     * 可以在eb_system_config表中配置 aliyun_sms_access_key_id
     */
    public static final String secretId = "LTAI5t6T4DjQmhVtuVSH5gNb";
    
    /** 
     * AccessKey Secret - 建议从系统配置中读取
     * 可以在eb_system_config表中配置 aliyun_sms_access_key_secret
     */
    public static final String secretKey = "g3YKRPxU0NXGQ0gMjVLq6CQe4lfps5";
    
    /** 
     * 短信应用ID - 暂未使用，阿里云短信不需要此参数
     * 这是腾讯云短信的参数，保留做兼容 
     */
    public static final String sdkAppId = "20056465613";
    
    /** 
     * 短信签名名称 - 建议从系统配置中读取
     * 可以在eb_system_config表中配置 aliyun_sms_sign_name
     */
    public static final String SignName = "中讯志远武汉科技";
    
    /** 阿里云短信服务地域节点 */
    public static final String ENDPOINT = "dysmsapi.aliyuncs.com";
    
    /** 短信发送成功的返回码 */
    public static final String SUCCESS_CODE = "OK";
}
