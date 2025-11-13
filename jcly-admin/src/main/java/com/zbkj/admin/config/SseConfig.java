package com.zbkj.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * SSE (Server-Sent Events) 相关配置
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Configuration
public class SseConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/admin/merchant/enterprise-chat/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Content-Type", "Cache-Control", "X-Requested-With")
                .maxAge(3600);
        
        // 为SSE专门配置，支持本地开发和线上环境
        registry.addMapping("/api/admin/merchant/enterprise-chat/message/stream-get")
                .allowedOrigins(
                    "http://localhost:9527", "http://127.0.0.1:9527", "http://localhost:3000", // 前端开发端口
                    "http://localhost:20800", "http://127.0.0.1:20800", // 后端端口
                    "https://shop.jclyyun.com" // 线上环境
                )
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .exposedHeaders("Content-Type", "Cache-Control", "X-Requested-With")
                .maxAge(3600);
        
        // 为SSE测试接口配置
        registry.addMapping("/api/admin/merchant/enterprise-chat/test/sse")
                .allowedOrigins(
                    "http://localhost:8080", "http://127.0.0.1:8080", "http://localhost:3000",
                    "http://localhost:20800", "http://127.0.0.1:20800",
                    "https://shop.jclyyun.com"
                )
                .allowedMethods("GET", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .exposedHeaders("Content-Type", "Cache-Control", "X-Requested-With")
                .maxAge(3600);
    }
    
    @Override
    public void configureAsyncSupport(@NonNull AsyncSupportConfigurer configurer) {
        // 设置异步请求超时时间为1小时 (3600秒)
        configurer.setDefaultTimeout(3600000L);
    }
}
