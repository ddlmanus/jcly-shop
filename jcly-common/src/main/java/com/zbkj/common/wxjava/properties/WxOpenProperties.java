package com.zbkj.common.wxjava.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 类的详细说明
 *
 * @author Han
 * @version 1.0.0
 * @Date 2025/4/11
 */
@Data
@ConfigurationProperties(prefix = "wx.open")
public class WxOpenProperties {

    /**
     * 是否使用redis存储access token
     */
    private boolean useRedis;

    /**
     * redis 配置
     */
    private WxOpenProperties.RedisConfig redisConfig;

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

    /**
     * 设置微信小程序的appid
     */
    private String componentAppId;

    /**
     * 设置微信小程序的Secret
     */
    private String componentSecret;

    /**
     * 设置微信小程序消息服务器配置的token
     */
    private String componentToken;

    /**
     * 设置微信小程序消息服务器配置的EncodingAESKey
     */
    private String componentAesKey;
}
