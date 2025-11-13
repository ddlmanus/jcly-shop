package com.zbkj.common.wxjava.properties;

import com.zbkj.common.config.RedisConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@Data
@ConfigurationProperties(prefix = "wx.miniapp")
public class WxMaProperties {

    /**
     * 是否使用redis存储access token
     */
    private boolean useRedis;

    /**
     * redis 配置
     */
    private RedisConfig redisConfig;

    @Data
    public static class RedisConfig {
        /**
         * redis服务器 主机地址
         */
        private String host;

        /**
         * redis服务器 端口号
         */
        private Integer port;

        /**
         * redis服务器 密码
         */
        private String password;

        /**
         * redis 服务连接超时时间
         */
        private Integer timeout;

        /**
         * redis 默认数据库
         */
        private Integer database;
    }

    private List<Config> configs;

    @Data
    public static class Config {
        /**
         * 设置微信小程序的appid
         */
        private String appid;

        /**
         * 设置微信小程序的Secret
         */
        private String secret;

        /**
         * 设置微信小程序消息服务器配置的token
         */
        private String token;

        /**
         * 设置微信小程序消息服务器配置的EncodingAESKey
         */
        private String aesKey;

        /**
         * 消息格式，XML或者JSON
         */
        private String msgDataFormat;
    }

}
