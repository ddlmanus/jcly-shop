package com.zbkj.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 聚水潭ERP配置类
 * 
 * @author 系统
 * @since 2024-01-01
 */
@Component
@ConfigurationProperties(prefix = "jst.erp")
public class JustuitanErpConfig {
    
    /**
     * 同步配置
     */
    private Sync sync = new Sync();
    
    /**
     * API配置
     */
    private Api api = new Api();
    
    /**
     * 认证配置
     */
    private Auth auth = new Auth();
    
    public Sync getSync() {
        return sync;
    }
    
    public void setSync(Sync sync) {
        this.sync = sync;
    }
    
    public Api getApi() {
        return api;
    }
    
    public void setApi(Api api) {
        this.api = api;
    }
    
    public Auth getAuth() {
        return auth;
    }
    
    public void setAuth(Auth auth) {
        this.auth = auth;
    }
    
    /**
     * 同步配置
     */
    public static class Sync {
        /**
         * 是否启用聚水潭ERP同步
         */
        private Boolean enabled = true;
        
        public Boolean getEnabled() {
            return enabled;
        }
        
        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    /**
     * API配置
     */
    public static class Api {
        /**
         * 聚水潭API地址
         */
        private String url = "https://openapi.jushuitan.com";
        
        /**
         * 接口版本
         */
        private String version = "v1";
        
        /**
         * 请求超时时间(秒)
         */
        private Integer timeout = 30;
        
        /**
         * 重试次数
         */
        private Integer retry = 3;
        
        /**
         * 重试间隔(秒)
         */
        private Integer retryInterval = 5;
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public String getVersion() {
            return version;
        }
        
        public void setVersion(String version) {
            this.version = version;
        }
        
        public Integer getTimeout() {
            return timeout;
        }
        
        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }
        
        public Integer getRetry() {
            return retry;
        }
        
        public void setRetry(Integer retry) {
            this.retry = retry;
        }
        
        public Integer getRetryInterval() {
            return retryInterval;
        }
        
        public void setRetryInterval(Integer retryInterval) {
            this.retryInterval = retryInterval;
        }
    }
    
    /**
     * 认证配置
     */
    public static class Auth {
        /**
         * 应用密钥
         */
        private String appKey;
        
        /**
         * 应用秘钥
         */
        private String appSecret;

        /**
         * 应用access_token
         */
        private String accessToken;
        
        /**
         * 授权类型
         */
        private String grantType = "authorization_code";
        
        /**
         * 授权码
         */
        private String code= "123456";
        
        /**
         * 字符集
         */
        private String charset = "utf-8";
        
        /**
         * 认证接口URL
         */
        private String tokenUrl = "https://openapi.jushuitan.com/openWeb/auth/getInitToken";

        public String getAccessToken() {
            return accessToken;
        }
        
        public String getAppKey() {
            return appKey;
        }
        
        public void setAppKey(String appKey) {
            this.appKey = appKey;
        }
        
        public String getAppSecret() {
            return appSecret;
        }
        
        public void setAppSecret(String appSecret) {
            this.appSecret = appSecret;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
        
        public String getGrantType() {
            return grantType;
        }
        
        public void setGrantType(String grantType) {
            this.grantType = grantType;
        }
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public String getCharset() {
            return charset;
        }
        
        public void setCharset(String charset) {
            this.charset = charset;
        }
        
        public String getTokenUrl() {
            return tokenUrl;
        }
        
        public void setTokenUrl(String tokenUrl) {
            this.tokenUrl = tokenUrl;
        }
    }
}