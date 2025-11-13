package com.zbkj.common.wxjava;

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.zbkj.common.wxjava.properties.WxPayProperties;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 类的详细说明
 *
 * @author dudl
 * @version 1.0.0
 * @Date 2025/3/31
 */
public class WxPayServiceFactory {
    private final ConcurrentHashMap<String, WxPayService> services = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, WxPayProperties> configStore = new ConcurrentHashMap<>();

    // 注册配置
    public void registerConfig(String appId, WxPayProperties config) {
        configStore.put(appId, config);
    }

    // 获取支付服务
    public WxPayService getService(String key) {
        return services.computeIfAbsent(key, k -> {
            // 在这里创建并返回新的WxPayService实例
            WxPayProperties config = configStore.get(key);
            if (config == null) {
                throw new IllegalArgumentException("未找到微信支付对应的配置: " + key);
            }
            WxPayConfig payConfig = new WxPayConfig();
            payConfig.setAppId(StringUtils.trimToNull(config.getAppId()));//V3商户模式需要
            payConfig.setMchId(StringUtils.trimToNull(config.getMchId()));//V3商户模式需要
            payConfig.setMchKey(StringUtils.trimToNull(config.getMchKey()));
            payConfig.setSubAppId(StringUtils.trimToNull(config.getSubAppId()));
            payConfig.setSubMchId(StringUtils.trimToNull(config.getSubMchId()));
            payConfig.setKeyPath(StringUtils.trimToNull(config.getKeyPath()));
            payConfig.setApiV3Key(StringUtils.trimToNull(config.getApiV3Key()));//V3商户模式需要
            payConfig.setCertSerialNo(StringUtils.trimToNull(config.getCertSerialNo()));//V3商户模式需要
            payConfig.setPrivateCertPath(StringUtils.trimToNull(config.getPrivateCertPath()));//V3商户模式需要
            payConfig.setPrivateKeyPath(StringUtils.trimToNull(config.getPrivateKeyPath()));//V3商户模式需要
            payConfig.setPublicKeyPath(StringUtils.trimToNull(config.getPublicKeyPath()));//V3商户模式需要
            payConfig.setPublicKeyId(StringUtils.trimToNull(config.getPublicKeyId()));//V3商户模式需要
            // 可以指定是否使用沙箱环境
            payConfig.setUseSandboxEnv(false);
            WxPayServiceImpl wxPayService = new WxPayServiceImpl();
            wxPayService.setConfig(payConfig);
            return wxPayService;
        });
    }

    public String getPaySourceByAppid(String appid) {
        String paySource = "";
        for (String key : configStore.keySet()) {
            WxPayProperties wxPayProperties = configStore.get(key);
            if (wxPayProperties.getAppId().equals(appid)) {
                paySource = key;
                break;
            }
        }
        return paySource;
    }
}
