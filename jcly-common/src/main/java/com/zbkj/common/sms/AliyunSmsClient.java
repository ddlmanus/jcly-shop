package com.zbkj.common.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.*;
import com.aliyun.teaopenapi.models.Config;

/**
 * 阿里云短信发送客户端
 * 基于阿里云短信服务API 2017-05-25版本
 */
@Component
public class AliyunSmsClient {

    private static final Logger logger = LoggerFactory.getLogger(AliyunSmsClient.class);
    
    public Boolean sendSms(AliyunSmsConfig config, String phoneNo, String params) throws Exception {
        if (config == null) {
            throw new IllegalArgumentException("阿里云短信配置不能为空");
        }

        Config clientConfig = new Config()
                .setAccessKeyId(config.getAccessKeyId())
                .setAccessKeySecret(config.getAccessKeySecret())
                .setEndpoint(config.getEndpoint() != null ? config.getEndpoint() : "dysmsapi.aliyuncs.com");

        Client client = new Client(clientConfig);

        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setSignName(config.getSignName())
                .setTemplateCode(config.getTemplateCode())
                .setPhoneNumbers(phoneNo)
                .setTemplateParam(params);

        SendSmsResponse response = client.sendSms(sendSmsRequest);
        
        if ("OK".equals(response.getBody().getCode())) {
            return true;
        } else {
            logger.error("发送短信失败: {}", response.getBody().getMessage());
            throw new RuntimeException(response.getBody().getMessage());
        }
    }
    
    /**
     * 兼容旧版本的方法，建议使用新的配置对象方式
     */
    @Deprecated
    public Boolean sendSms(String signName, String phoneNo, String params, String templateCode, String key, String secret) throws Exception {
        AliyunSmsConfig config = new AliyunSmsConfig(key, secret, signName, templateCode);
        return sendSms(config, phoneNo, params);
    }
}