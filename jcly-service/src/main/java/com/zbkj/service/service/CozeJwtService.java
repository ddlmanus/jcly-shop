package com.zbkj.service.service;

/**
 * Coze JWT Service
 * 用于生成JWT token并获取Coze API访问令牌
 */
public interface CozeJwtService {
    
    /**
     * 获取Coze API访问令牌
     * @return 访问令牌
     */
    String getAccessToken();
    
    /**
     * 生成JWT token
     * @return JWT字符串
     */
    String generateJwt();
    
    /**
     * 刷新访问令牌（如果需要）
     * @return 新的访问令牌
     */
    String refreshAccessToken();
    
    /**
     * 检查访问令牌是否有效
     * @return true表示有效，false表示无效
     */
    boolean isAccessTokenValid();
}
