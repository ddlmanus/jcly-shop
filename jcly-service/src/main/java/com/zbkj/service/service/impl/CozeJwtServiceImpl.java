package com.zbkj.service.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zbkj.service.service.CozeJwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coze JWT Service Implementation
 * 严格按照Coze JWT授权文档实现的鉴权服务
 * 
 * 授权流程:
 * 1. 生成JWT（JSON Web Token）
 * 2. 通过JWT获取OAuth Access Token
 * 3. 使用Access Token调用Coze API
 */
@Service
public class CozeJwtServiceImpl implements CozeJwtService {
    
    private static final Logger logger = LoggerFactory.getLogger(CozeJwtServiceImpl.class);
    
    // JWT令牌过期时间（15分钟，900秒）- 按照Coze文档建议
    private static final int JWT_EXPIRATION_SECONDS = 900;
    // 访问令牌提前刷新时间（提前5分钟）
    private static final int TOKEN_REFRESH_BUFFER_SECONDS = 300;
    // 访问令牌默认有效期（24小时）- 按照Coze文档最大值
    private static final int ACCESS_TOKEN_DURATION_SECONDS = 86399;
    // OAuth Token获取端点
    private static final String OAUTH_TOKEN_ENDPOINT = "/api/permission/oauth2/token";
    
    @Value("${coze.app.id}")
    private String appId;
    
    @Value("${coze.app.public-key-fingerprint}")
    private String publicKeyFingerprint;
    
    @Value("${coze.app.private-key}")
    private String privateKeyContent;
    
    @Value("${coze.api.endpoint:https://api.coze.cn}")
    private String apiEndpoint;
    
    private PrivateKey privateKey;
    private final Map<String, Object> tokenCache = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        try {
            this.privateKey = parsePrivateKey(privateKeyContent);
            logger.info("Coze JWT Service initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Coze JWT Service", e);
            throw new RuntimeException("Failed to initialize Coze JWT Service", e);
        }
    }
    
    @Override
    public String getAccessToken() {
        // 检查缓存的访问令牌是否有效
        if (isAccessTokenValid()) {
            return (String) tokenCache.get("access_token");
        }
        
        // 刷新访问令牌
        return refreshAccessToken();
    }
    
    @Override
    public String generateJwt() {
        try {
            long currentTime = System.currentTimeMillis() / 1000;
            
            // 按照Coze JWT文档构建Header
            Map<String, Object> headers = new HashMap<>();
            headers.put("alg", "RS256");          // 固定为RS256
            headers.put("typ", "JWT");            // 固定为JWT
            headers.put("kid", publicKeyFingerprint); // OAuth应用的公钥指纹
            
            // 按照Coze JWT文档构建Payload
            Map<String, Object> claims = new HashMap<>();
            claims.put("iss", appId);                        // OAuth应用的ID
            claims.put("aud", "api.coze.cn");               // 扣子API的Endpoint（不包含协议）
            claims.put("iat", currentTime);                 // JWT开始生效的时间
            claims.put("exp", currentTime + JWT_EXPIRATION_SECONDS); // JWT过期时间
            claims.put("jti", UUID.randomUUID().toString().replace("-", "")); // 随机字符串，防止重放攻击
            
            // 使用私钥签名生成JWT
            String jwt = Jwts.builder()
                    .setHeader(headers)
                    .setClaims(claims)
                    .signWith(SignatureAlgorithm.RS256, privateKey)
                    .compact();
            
            logger.debug("Generated JWT successfully");
            return jwt;
            
        } catch (Exception e) {
            logger.error("Failed to generate JWT", e);
            throw new RuntimeException("Failed to generate JWT", e);
        }
    }
    
    @Override
    public String refreshAccessToken() {
        try {
            // 生成新的JWT
            String jwt = generateJwt();
            
            // 构建请求参数
            JSONObject requestBody = new JSONObject();
            requestBody.put("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
            requestBody.put("duration_seconds", ACCESS_TOKEN_DURATION_SECONDS);
            
            // 调用Coze OAuth Token API
            HttpResponse response = HttpRequest.post(apiEndpoint + OAUTH_TOKEN_ENDPOINT)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwt)
                    .body(requestBody.toString())
                    .timeout(30000)
                    .execute();
            
            if (!response.isOk()) {
                logger.error("Failed to get access token, status: {}, body: {}", 
                           response.getStatus(), response.body());
                throw new RuntimeException("Failed to get access token from Coze API");
            }
            
            JSONObject responseBody = JSONUtil.parseObj(response.body());
            String accessToken = responseBody.getStr("access_token");
            Long expiresIn = responseBody.getLong("expires_in");
            
            if (StrUtil.isBlank(accessToken) || expiresIn == null) {
                logger.error("Invalid response from Coze OAuth API: {}", response.body());
                throw new RuntimeException("Invalid response from Coze OAuth API");
            }
            
            // 缓存访问令牌和过期时间
            tokenCache.put("access_token", accessToken);
            tokenCache.put("expires_at", expiresIn);
            
            logger.info("Successfully refreshed Coze access token, expires at: {}", 
                       new Date(expiresIn * 1000));
            
            return accessToken;
            
        } catch (Exception e) {
            logger.error("Failed to refresh access token", e);
            throw new RuntimeException("Failed to refresh access token", e);
        }
    }
    
    @Override
    public boolean isAccessTokenValid() {
        String accessToken = (String) tokenCache.get("access_token");
        Long expiresAt = (Long) tokenCache.get("expires_at");
        
        if (StrUtil.isBlank(accessToken) || expiresAt == null) {
            return false;
        }
        
        // 检查是否还有足够的有效时间（提前5分钟刷新）
        long currentTime = System.currentTimeMillis() / 1000;
        return currentTime < (expiresAt - TOKEN_REFRESH_BUFFER_SECONDS);
    }
    
    /**
     * 解析私钥字符串为PrivateKey对象
     */
    private PrivateKey parsePrivateKey(String privateKeyContent) throws Exception {
        if (StrUtil.isBlank(privateKeyContent)) {
            throw new IllegalArgumentException("Private key content cannot be empty");
        }
        
        // 清理私钥内容，移除PEM格式的头尾标记和换行符
        String cleanedKey = privateKeyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        
        // Base64解码
        byte[] keyBytes = Base64.getDecoder().decode(cleanedKey);
        
        // 生成私钥对象
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        
        return keyFactory.generatePrivate(keySpec);
    }
}