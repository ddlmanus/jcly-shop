package com.zbkj.admin.config;

import com.zbkj.admin.listener.RedisMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis消息监听配置 - 商户端
 * 配置Redis发布订阅，监听来自小程序端的消息
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Configuration
public class RedisMessageConfig {

    @Autowired
    private RedisMessageListener redisMessageListener;

    /**
     * 专用于消息发布订阅的RedisTemplate，使用字符串序列化
     */
    @Bean("messageRedisTemplate")
    public RedisTemplate<String, String> messageRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 全部使用字符串序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis消息监听容器
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // 监听客服消息频道（使用模式匹配）
        container.addMessageListener(
            new MessageListenerAdapter(redisMessageListener),
            new PatternTopic("customer-service:*")
        );

        // 监听商户端消息频道
        container.addMessageListener(
            new MessageListenerAdapter(redisMessageListener),
            new ChannelTopic("websocket:merchant:message")
        );

        // 监听商户到商户消息频道
        container.addMessageListener(
            new MessageListenerAdapter(redisMessageListener),
            new ChannelTopic("websocket:merchant:merchant_message")
        );

        // 监听平台端消息频道
        container.addMessageListener(
            new MessageListenerAdapter(redisMessageListener),
            new ChannelTopic("websocket:platform:message")
        );

        // 监听系统通知频道
        container.addMessageListener(
            new MessageListenerAdapter(redisMessageListener),
            new ChannelTopic("websocket:system:notification")
        );

        log.info("【商户端Redis配置】消息监听容器已配置，监听频道: customer-service:*, websocket:merchant:message, websocket:merchant:merchant_message, websocket:platform:message, websocket:system:notification");

        return container;
    }
}
