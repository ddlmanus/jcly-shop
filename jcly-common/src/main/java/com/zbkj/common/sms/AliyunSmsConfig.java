package com.zbkj.common.sms;

import lombok.Data;

/**
 * 阿里云短信配置类
 */
@Data
public class AliyunSmsConfig {
    
    /**
     * AccessKey ID
     */
    private String accessKeyId;
    
    /**
     * AccessKey Secret
     */
    private String accessKeySecret;
    
    /**
     * 短信签名
     */
    private String signName;
    
    /**
     * 短信模板代码
     */
    private String templateCode;
    
    /**
     * 接入点
     */
    private String endpoint;
    
    /**
     * 区域ID
     */
    private String regionId;
    
    public AliyunSmsConfig() {}
    
    public AliyunSmsConfig(String accessKeyId, String accessKeySecret, String signName, String templateCode) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.signName = signName;
        this.templateCode = templateCode;
        this.endpoint = "dysmsapi.aliyuncs.com";
        this.regionId = "cn-qingdao";
    }
    
    public AliyunSmsConfig(String accessKeyId, String accessKeySecret, String signName, 
                          String templateCode, String endpoint, String regionId) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.signName = signName;
        this.templateCode = templateCode;
        this.endpoint = endpoint;
        this.regionId = regionId;
    }
}