package com.zbkj.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

/**
 * 聚水潭API通用工具类
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
public class JustuitanApiUtil {

    private static final Logger logger = LoggerFactory.getLogger(JustuitanApiUtil.class);

    /**
     * 构建API基础参数
     * @param appKey 应用Key
     * @param accessToken 访问令牌
     * @return 基础参数Map
     */
    public static Map<String, Object> buildApiParams(String appKey, String accessToken) {
        Map<String, Object> params = new HashMap<>();
        params.put("app_key", appKey);
        params.put("access_token", accessToken);
        params.put("timestamp", System.currentTimeMillis() / 1000);
        params.put("charset", "utf-8");
        params.put("version", "2");
        return params;
    }

    /**
     * 构建获取access_token的请求参数
     * @param appKey 应用Key
     * @param grantType 授权类型
     * @param code 授权码
     * @param charset 字符集
     * @return 请求参数
     */
    public static MultiValueMap<String, Object> buildTokenParams(String appKey, String grantType, String code, String charset) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("app_key", appKey);
        params.add("grant_type", grantType);
        params.add("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        params.add("code", code);
        params.add("charset", charset);
        return params;
    }

    /**
     * 生成API签名
     * @param params 参数Map
     * @param appSecret 应用密钥
     * @return 签名字符串
     */
    public static String generateSign(Map<String, Object> params, String appSecret) {
        // 按照聚水潭开放平台API文档要求生成签名
        StringBuilder signStr = new StringBuilder();
        signStr.append(appSecret);
        
        // 按参数名排序并拼接
        params.entrySet().stream()
                .filter(entry -> !"sign".equals(entry.getKey()) && ObjectUtil.isNotNull(entry.getValue()))
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> signStr.append(entry.getKey()).append(entry.getValue()));
        
        // 调试日志：打印待签名字符串
        String signString = signStr.toString();
        logger.info("聚水潭API待签名字符串: {}", signString);
        
        // MD5加密并转小写（根据聚水潭签名规则文档要求）
        String sign = DigestUtil.md5Hex(signString).toLowerCase();
        logger.info("聚水潭API生成的签名: {}", sign);
        
        return sign;
    }

    /**
     * 生成MultiValueMap参数的签名
     * @param params MultiValueMap参数
     * @param appSecret 应用密钥
     * @return 签名字符串
     */
    public static String generateSign(MultiValueMap<String, Object> params, String appSecret) {
        Map<String, Object> signParams = new HashMap<>();
        params.forEach((key, values) -> {
            if (CollUtil.isNotEmpty(values)) {
                signParams.put(key, values.get(0));
            }
        });
        return generateSign(signParams, appSecret);
    }

    /**
     * 解析API响应
     * @param response 响应字符串
     * @return 是否成功
     */
    public static Boolean parseApiResponse(String response) {
        if (StrUtil.isBlank(response)) {
            logger.error("聚水潭API响应为空");
            return false;
        }
        
        try {
            JSONObject jsonResponse = JSONObject.parseObject(response);
            Integer code = jsonResponse.getInteger("code");
            
            if (code != null && code == 0) {
                logger.info("聚水潭API调用成功: {}", response);
                return true;
            } else {
                String msg = jsonResponse.getString("msg");
                logger.error("聚水潭API调用失败: code={}, msg={}, response={}", code, msg, response);
                return false;
            }
        } catch (Exception e) {
            logger.error("解析聚水潭API响应异常: {}", response, e);
            return false;
        }
    }

    /**
     * 从响应中提取access_token
     * @param response 响应字符串
     * @return access_token
     */
    public static String extractAccessToken(String response) {
        if (StrUtil.isBlank(response)) {
            logger.error("获取access_token响应为空");
            return null;
        }
        
        try {
            JSONObject jsonResponse = JSONObject.parseObject(response);
            if (jsonResponse.getInteger("code") == 0) {
                JSONObject data = jsonResponse.getJSONObject("data");
                if (data != null) {
                    String accessToken = data.getString("access_token");
                    if (StrUtil.isNotBlank(accessToken)) {
                        logger.info("成功获取access_token: {}", accessToken);
                        return accessToken;
                    }
                }
            }
            
            logger.error("获取access_token失败: {}", response);
            return null;
            
        } catch (Exception e) {
            logger.error("解析access_token响应异常: {}", response, e);
            return null;
        }
    }
}