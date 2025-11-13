package com.zbkj.service.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alipay.api.AlipayClient;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import com.zbkj.common.constants.AlipayConfig;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.result.PayResultCode;
import com.zbkj.common.vo.MyRecord;
import com.zbkj.service.service.AlipayClientCertService;
import com.zbkj.service.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 支付宝证书客户端服务实现
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
@Service
public class AlipayClientCertServiceImpl implements AlipayClientCertService {

    private static final Logger logger = LoggerFactory.getLogger(AlipayClientCertServiceImpl.class);

    @Autowired
    private SystemConfigService systemConfigService;

    private volatile AlipayClient certClient;

    /**
     * 获取支付宝证书客户端
     */
    @Override
    public AlipayClient getCertClient() {
        return  createCertClient();
    }

    /**
     * 创建证书客户端
     */
    private AlipayClient createCertClient() {
        try {
            MyRecord certConfig = getCertConfig();
            
            String appId = certConfig.getStr(AlipayConfig.APPID);
            String privateKey = certConfig.getStr(AlipayConfig.ALIPAY_PRIVATE_KEY_CERT);
            String appCertPath = certConfig.getStr(AlipayConfig.ALIPAY_CERT_PATH);
            String alipayCertPath = certConfig.getStr(AlipayConfig.ALIPAY_PUBLIC_CERT_PATH);
            String rootCertPath = certConfig.getStr(AlipayConfig.ALIPAY_ROOT_CERT_PATH);

            logger.info("创建支付宝证书客户端:");
            logger.info("AppId: {}", appId);
            logger.info("应用证书路径: {}", appCertPath);
            logger.info("支付宝公钥证书路径: {}", alipayCertPath);
            logger.info("支付宝根证书路径: {}", rootCertPath);

            // 验证证书文件是否存在
            validateCertFiles(appCertPath, alipayCertPath, rootCertPath);

            // 构建证书请求对象
            CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
            certAlipayRequest.setServerUrl(AlipayConfig.URL);
            certAlipayRequest.setAppId(appId);
            certAlipayRequest.setPrivateKey(privateKey);
            certAlipayRequest.setFormat(AlipayConfig.FORMAT);
            certAlipayRequest.setCharset(AlipayConfig.CHARSET);
            certAlipayRequest.setSignType(AlipayConfig.SIGNTYPE);
            certAlipayRequest.setCertPath(appCertPath);
            certAlipayRequest.setAlipayPublicCertPath(alipayCertPath);
            certAlipayRequest.setRootCertPath(rootCertPath);

            AlipayClient client = new DefaultAlipayClient(certAlipayRequest);
            
            logger.info("支付宝证书客户端创建成功");
            return client;

        } catch (Exception e) {
            logger.error("创建支付宝证书客户端失败", e);
            throw new CrmebException("支付宝证书客户端初始化失败: " + e.getMessage());
        }
    }

    /**
     * 验证证书文件
     */
    private void validateCertFiles(String appCertPath, String alipayCertPath, String rootCertPath) {
        File appCertFile = new File(appCertPath);
        File alipayCertFile = new File(alipayCertPath);
        File rootCertFile = new File(rootCertPath);

        if (!appCertFile.exists()) {
            throw new CrmebException("应用证书文件不存在: " + appCertPath);
        }
        if (!appCertFile.canRead()) {
            throw new CrmebException("应用证书文件无法读取: " + appCertPath);
        }

        if (!alipayCertFile.exists()) {
            throw new CrmebException("支付宝公钥证书文件不存在: " + alipayCertPath);
        }
        if (!alipayCertFile.canRead()) {
            throw new CrmebException("支付宝公钥证书文件无法读取: " + alipayCertPath);
        }

        if (!rootCertFile.exists()) {
            throw new CrmebException("支付宝根证书文件不存在: " + rootCertPath);
        }
        if (!rootCertFile.canRead()) {
            throw new CrmebException("支付宝根证书文件无法读取: " + rootCertPath);
        }

        logger.info("证书文件验证通过");
    }

    /**
     * 获取证书配置
     */
    private MyRecord getCertConfig() {
        List<String> configKeys = new ArrayList<>();
        configKeys.add(AlipayConfig.APPID);
        configKeys.add(AlipayConfig.RSA_PRIVATE_KEY);
        configKeys.add(AlipayConfig.ALIPAY_PRIVATE_KEY_CERT);
        configKeys.add(AlipayConfig.ALIPAY_CERT_PATH);
        configKeys.add(AlipayConfig.ALIPAY_PUBLIC_CERT_PATH);
        configKeys.add(AlipayConfig.ALIPAY_ROOT_CERT_PATH);

        MyRecord certConfig = systemConfigService.getValuesByKeyList(configKeys);

        // 验证必要配置
        if (StrUtil.isBlank(certConfig.getStr(AlipayConfig.APPID))) {
            throw new CrmebException(PayResultCode.ALI_PAY_NOT_CONFIG);
        }
        if (StrUtil.isBlank(certConfig.getStr(AlipayConfig.RSA_PRIVATE_KEY))) {
            throw new CrmebException("支付宝应用私钥未配置");
        }
        if (StrUtil.isBlank(certConfig.getStr(AlipayConfig.ALIPAY_CERT_PATH))) {
            throw new CrmebException("支付宝应用证书路径未配置");
        }
        if (StrUtil.isBlank(certConfig.getStr(AlipayConfig.ALIPAY_PUBLIC_CERT_PATH))) {
            throw new CrmebException("支付宝公钥证书路径未配置");
        }
        if (StrUtil.isBlank(certConfig.getStr(AlipayConfig.ALIPAY_ROOT_CERT_PATH))) {
            throw new CrmebException("支付宝根证书路径未配置");
        }

        return certConfig;
    }

    /**
     * 检查证书配置是否完整
     */
    @Override
    public boolean checkCertConfig() {
        try {
            getCertConfig();
            return true;
        } catch (Exception e) {
            logger.warn("证书配置检查失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取应用证书序列号
     */
    @Override
    public String getAppCertSN() {
        try {
            MyRecord certConfig = getCertConfig();
            String appCertPath = certConfig.getStr(AlipayConfig.ALIPAY_CERT_PATH);
            // 这里可以通过证书工具类获取序列号，暂时返回配置路径
            return appCertPath;
        } catch (Exception e) {
            logger.error("获取应用证书序列号失败", e);
            return "";
        }
    }

    /**
     * 获取支付宝根证书序列号
     */
    @Override
    public String getAlipayRootCertSN() {
        try {
            MyRecord certConfig = getCertConfig();
            String rootCertPath = certConfig.getStr(AlipayConfig.ALIPAY_ROOT_CERT_PATH);
            // 这里可以通过证书工具类获取序列号，暂时返回配置路径
            return rootCertPath;
        } catch (Exception e) {
            logger.error("获取支付宝根证书序列号失败", e);
            return "";
        }
    }

    /**
     * 清除客户端缓存（当证书更新时调用）
     */
    public void clearClientCache() {
        certClient = null;
        logger.info("支付宝证书客户端缓存已清除");
    }
}
