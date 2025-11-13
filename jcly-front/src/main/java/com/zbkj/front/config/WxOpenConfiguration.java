package com.zbkj.front.config;

import cn.hutool.core.util.StrUtil;
import com.zbkj.common.wxjava.properties.WxOpenProperties;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxRuntimeException;
import me.chanjar.weixin.open.api.WxOpenService;
import me.chanjar.weixin.open.api.impl.WxOpenInRedisConfigStorage;
import me.chanjar.weixin.open.api.impl.WxOpenServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 微信开放平台配置类
 *
 * @author Han
 * @version 1.0.0
 * @Date 2025/4/11
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(WxOpenProperties.class)
public class WxOpenConfiguration {

    private final WxOpenProperties properties;

    private volatile static JedisPool pool;

    @Autowired
    public WxOpenConfiguration(WxOpenProperties properties) {
        this.properties = properties;
    }

    @Bean
    public WxOpenService wxOpenService() {
        if (StrUtil.isBlank(this.properties.getComponentAppId())) {
//            throw new WxRuntimeException("大哥，拜托先看下项目首页的说明（readme文件），添加下相关配置，注意别配错了！");
            return new WxOpenServiceImpl();
        }
        WxOpenInRedisConfigStorage wxOpenInRedisConfigStorage = new WxOpenInRedisConfigStorage(getJedisPool());
        wxOpenInRedisConfigStorage.setWxOpenInfo(properties.getComponentAppId(),
                properties.getComponentSecret(),
                properties.getComponentToken(),
                properties.getComponentAesKey());

        WxOpenService wxOpenService = new WxOpenServiceImpl();
        wxOpenService.setWxOpenConfigStorage(wxOpenInRedisConfigStorage);
        return wxOpenService;
    }

    private JedisPool getJedisPool() {
        if (pool == null) {
            synchronized (WxOpenConfiguration.class) {
                if (pool == null) {
                    JedisPoolConfig poolConfig = new JedisPoolConfig();
                    pool = new JedisPool(poolConfig, properties.getRedisConfig().getHost(),
                            properties.getRedisConfig().getPort(),
                            properties.getRedisConfig().getTimeout(),
                            properties.getRedisConfig().getPassword(),
                            properties.getRedisConfig().getDatabase());
                }
            }
        }
        return pool;
    }
}
