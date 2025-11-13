package com.zbkj.common.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

/**
 * 安全工具类
 * 提供敏感信息加密解密、脱敏显示等功能
 *
 * @author System
 * @since 2024-01-01
 */
@Slf4j
public class SecurityUtils {

    /**
     * AES加密密钥，建议从配置文件读取
     */
    private static final String AES_KEY = "jcly-shop-aes-key-2024-secure";
    
    /**
     * 银行卡号脱敏显示
     *
     * @param cardNo 银行卡号
     * @return 脱敏后的银行卡号
     */
    public static String maskBankCard(String cardNo) {
        if (StrUtil.isBlank(cardNo)) {
            return "";
        }
        if (cardNo.length() < 8) {
            return cardNo;
        }
        return cardNo.substring(0, 4) + "****" + cardNo.substring(cardNo.length() - 4);
    }

    /**
     * 身份证号脱敏显示
     *
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    public static String maskIdCard(String idCard) {
        if (StrUtil.isBlank(idCard)) {
            return "";
        }
        if (idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 手机号脱敏显示
     *
     * @param mobile 手机号
     * @return 脱敏后的手机号
     */
    public static String maskMobile(String mobile) {
        if (StrUtil.isBlank(mobile)) {
            return "";
        }
        if (mobile.length() < 7) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }

    /**
     * 姓名脱敏显示
     *
     * @param name 姓名
     * @return 脱敏后的姓名
     */
    public static String maskName(String name) {
        if (StrUtil.isBlank(name)) {
            return "";
        }
        if (name.length() <= 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "**" + name.charAt(name.length() - 1);
    }

    /**
     * AES加密
     *
     * @param data 待加密数据
     * @return 加密后的数据（Base64编码）
     */
    public static String encrypt(String data) {
        if (StrUtil.isBlank(data)) {
            return data;
        }
        try {
            AES aes = SecureUtil.aes(AES_KEY.getBytes(StandardCharsets.UTF_8));
            return aes.encryptHex(data);
        } catch (Exception e) {
            log.error("AES加密失败", e);
            throw new RuntimeException("数据加密失败", e);
        }
    }

    /**
     * AES解密
     *
     * @param encryptedData 加密数据（Base64编码）
     * @return 解密后的数据
     */
    public static String decrypt(String encryptedData) {
        if (StrUtil.isBlank(encryptedData)) {
            return encryptedData;
        }
        try {
            AES aes = SecureUtil.aes(AES_KEY.getBytes(StandardCharsets.UTF_8));
            return aes.decryptStr(encryptedData);
        } catch (Exception e) {
            log.error("AES解密失败", e);
            throw new RuntimeException("数据解密失败", e);
        }
    }

    /**
     * 生成随机字符串
     *
     * @param length 长度
     * @return 随机字符串
     */
    public static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new SecureRandom();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(characters.charAt(random.nextInt(characters.length())));
        }
        return result.toString();
    }

    /**
     * 生成数字验证码
     *
     * @param length 长度
     * @return 数字验证码
     */
    public static String generateNumericCode(int length) {
        Random random = new SecureRandom();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(random.nextInt(10));
        }
        return result.toString();
    }

    /**
     * MD5加密
     *
     * @param data 待加密数据
     * @return MD5加密结果
     */
    public static String md5(String data) {
        return SecureUtil.md5(data);
    }

    /**
     * SHA256加密
     *
     * @param data 待加密数据
     * @return SHA256加密结果
     */
    public static String sha256(String data) {
        return SecureUtil.sha256(data);
    }

    /**
     * 验证签名
     *
     * @param data 原始数据
     * @param signature 签名
     * @param secret 密钥
     * @return 验证结果
     */
    public static boolean verifySignature(String data, String signature, String secret) {
        if (StrUtil.isBlank(data) || StrUtil.isBlank(signature) || StrUtil.isBlank(secret)) {
            return false;
        }
        try {
            String expectedSignature = sha256(data + secret);
            return signature.equals(expectedSignature);
        } catch (Exception e) {
            log.error("签名验证失败", e);
            return false;
        }
    }

    /**
     * 生成签名
     *
     * @param data 原始数据
     * @param secret 密钥
     * @return 签名
     */
    public static String generateSignature(String data, String secret) {
        if (StrUtil.isBlank(data) || StrUtil.isBlank(secret)) {
            return "";
        }
        return sha256(data + secret);
    }

    /**
     * 验证银行卡号格式
     *
     * @param cardNo 银行卡号
     * @return 验证结果
     */
    public static boolean isValidBankCard(String cardNo) {
        if (StrUtil.isBlank(cardNo)) {
            return false;
        }
        // 去除空格
        cardNo = cardNo.replaceAll("\\s+", "");
        // 检查长度（一般为16-19位）
        if (cardNo.length() < 16 || cardNo.length() > 19) {
            return false;
        }
        // 检查是否全为数字
        if (!cardNo.matches("\\d+")) {
            return false;
        }
        // Luhn算法验证
        return luhnCheck(cardNo);
    }

    /**
     * Luhn算法验证银行卡号
     *
     * @param cardNo 银行卡号
     * @return 验证结果
     */
    private static boolean luhnCheck(String cardNo) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNo.length() - 1; i >= 0; i--) {
            int digit = Integer.parseInt(cardNo.substring(i, i + 1));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    /**
     * 验证身份证号格式
     *
     * @param idCard 身份证号
     * @return 验证结果
     */
    public static boolean isValidIdCard(String idCard) {
        if (StrUtil.isBlank(idCard)) {
            return false;
        }
        // 18位身份证正则表达式
        String regex = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";
        return idCard.matches(regex);
    }

    /**
     * 验证手机号格式
     *
     * @param mobile 手机号
     * @return 验证结果
     */
    public static boolean isValidMobile(String mobile) {
        if (StrUtil.isBlank(mobile)) {
            return false;
        }
        // 中国手机号正则表达式
        String regex = "^1[3-9]\\d{9}$";
        return mobile.matches(regex);
    }

    /**
     * 生成交易号
     *
     * @param prefix 前缀
     * @return 交易号
     */
    public static String generateTradeNo(String prefix) {
        return prefix + System.currentTimeMillis() + generateRandomString(4);
    }

    /**
     * 获取银行卡类型
     *
     * @param cardNo 银行卡号
     * @return 1=借记卡 2=信用卡
     */
    public static int getBankCardType(String cardNo) {
        if (StrUtil.isBlank(cardNo)) {
            return 1;
        }
        // 简单的银行卡类型判断规则
        // 信用卡一般以4、5开头
        if (cardNo.startsWith("4") || cardNo.startsWith("5")) {
            return 2; // 信用卡
        }
        return 1; // 借记卡
    }

    /**
     * 检查密码强度
     *
     * @param password 密码
     * @return 强度等级 1=弱 2=中 3=强
     */
    public static int checkPasswordStrength(String password) {
        if (StrUtil.isBlank(password)) {
            return 0;
        }
        int score = 0;
        // 长度检查
        if (password.length() >= 8) {
            score++;
        }
        // 包含数字
        if (password.matches(".*\\d.*")) {
            score++;
        }
        // 包含字母
        if (password.matches(".*[a-zA-Z].*")) {
            score++;
        }
        // 包含特殊字符
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            score++;
        }
        return Math.min(score, 3);
    }
} 