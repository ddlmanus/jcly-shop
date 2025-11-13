package com.zbkj.common.utils;
/**
 * @author 李明飞
 * @date 2025/9/5 14:35
 */

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;

/**
 * AES加密工具类（三项目共用，密钥从配置中心获取）
 */
@Component
public class AesUtils {
    // AES 密钥（16 字节）：请确保密钥足够随机和安全
    private static final String KEY = "zhongxun!@$123f1";
    // 初始向量（IV，16 字节）：实际应用中应随机生成
    private static final String IV = "abcdefghijklmnop";
    // 指定加密算法和模式
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    /**
     * 对给定的明文进行 AES 加密，并返回 Base64 编码后的密文字符串。
     *
     * @param plainText 明文字符串
     * @return Base64 编码后的密文字符串，如果加密过程中发生异常则返回 null
     */
    public static String encrypt(String plainText) {
        try {
            // 获取 Cipher 实例，指定使用 AES/CBC/PKCS5Padding 模式
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            // 构造密钥规范
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes("UTF-8"), "AES");
            // 构造初始向量规范
            IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes("UTF-8"));
            // 初始化 Cipher 为加密模式
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            // 对明文进行加密
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
            // 使用 Base64 对密文字节数组进行编码，返回字符串
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            // 捕获异常并打印错误信息
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 对给定的 Base64 编码密文进行 AES 解密，还原出原始明文字符串。
     *
     * @param cipherText Base64 编码后的密文字符串
     * @return 解密后的明文字符串，如果解密过程中发生异常则返回 null
     */
    public static String decrypt(String cipherText) {
        try {
            // 获取 Cipher 实例，指定使用 AES/CBC/PKCS5Padding 模式
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            // 构造密钥规范
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes("UTF-8"), "AES");
            // 构造初始向量规范
            IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes("UTF-8"));
            // 初始化 Cipher 为解密模式
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            // 将 Base64 编码的密文转换为字节数组
            byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
            // 对密文进行解密
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);

            String str = URLEncoder.encode("1223+++EEEE");
            String str2 = URLDecoder.decode(str);
            // 将解密后的字节数组转换为字符串
            return new String(decryptedBytes, "UTF-8");
        } catch (Exception e) {
            // 捕获异常并打印错误信息
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 主函数，演示 AES 加密和解密的完整流程。
     *
     * @param args 命令行参数（未使用）
     */
    public static void main2(String[] args) {

        // 定义待加密的明文
        String plainText = "Hello, AES Encryption!";
        System.out.println("原始明文: " + plainText);

        // 对明文进行加密
        String encryptedText = encrypt(plainText);
        System.out.println("加密后的密文 (Base64 编码): " + encryptedText);

        // 对密文进行解密
        String decryptedText = decrypt(encryptedText);
        System.out.println("解密后的明文: " + decryptedText);
    }

    public static void main(String[] args) {
        String str = URLEncoder.encode("1223+++EEEE");
        String str2 = URLDecoder.decode(str);
        System.out.println(str);
        System.out.println(str2);
    }
}
