package com.zbkj.front.websocket;

import com.zbkj.common.token.FrontTokenComponent;
import com.zbkj.common.utils.RedisUtil;
import com.zbkj.common.utils.RequestUtil;
import com.zbkj.common.vo.LoginFrontUserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;

/**
 * WebSocket握手拦截器 - 前端模块
 * 负责连接前的安全验证和用户信息提取
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Component
public class WebSocketInterceptor implements HandshakeInterceptor {

    @Autowired
    private FrontTokenComponent frontTokenComponent;
    
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        try {
            URI uri = request.getURI();
            String query = uri.getQuery();
            log.info("WebSocket握手请求: {}", uri);
            
            // 提取参数
            String token = extractParameter(query, "token");
            String userType = extractParameter(query, "userType");
            String sessionId = extractParameter(query, "sessionId");
            
            if (token == null || token.trim().isEmpty()) {
                log.warn("WebSocket握手失败: 缺少token参数");
                return false;
            }
            
            // 验证token并获取用户信息
            LoginFrontUserVo userVo = validateToken(token);
            if (userVo == null || userVo.getUser() == null) {
                log.warn("WebSocket握手失败: token验证失败 - {}", token.substring(0, Math.min(10, token.length())));
                return false;
            }
            
            // 将用户信息存储到WebSocket会话属性中
            attributes.put("userId", userVo.getUser().getId());
            attributes.put("userName", userVo.getUser().getNickname() != null ? userVo.getUser().getNickname() : "用户");
            attributes.put("userType", userType != null ? userType : "USER");
            attributes.put("token", token);
            attributes.put("chatSessionId", sessionId != null ? sessionId : "");
            
            log.info("WebSocket握手成功: userId={}, userName={}, sessionId={}", 
                    userVo.getUser().getId(), userVo.getUser().getNickname(), sessionId);
            
            return true;
            
        } catch (Exception e) {
            log.error("WebSocket握手异常: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket握手后异常: {}", exception.getMessage(), exception);
        } else {
            log.debug("WebSocket握手完成");
        }
    }

    /**
     * 从查询字符串中提取参数
     */
    private String extractParameter(String query, String paramName) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2 && paramName.equals(keyValue[0])) {
                try {
                    return java.net.URLDecoder.decode(keyValue[1], "UTF-8");
                } catch (Exception e) {
                    log.warn("参数解码失败: {}", pair);
                    return keyValue[1];
                }
            }
        }
        return null;
    }

    /**
     * 验证token并获取用户信息
     */
    private LoginFrontUserVo validateToken(String token) {
        try {
            // 转换token格式为Redis key格式
            String redisTokenKey = convertTokenToRedisKey(token);
            log.info("原始token: {}, Redis key: {}", token, redisTokenKey);
            
            // 使用FrontTokenComponent验证token
            Boolean isValid = frontTokenComponent.checkToken(redisTokenKey);
            if (!isValid) {
                log.warn("Token验证失败: {}", token.substring(0, Math.min(10, token.length())));
                return null;
            }
            log.info("Token验证成功: {}", isValid);
            
            // WebSocket握手时，直接验证token即可，用户信息可以在连接建立后再获取
            // 为了简化流程，我们创建一个临时的用户对象，包含从token中可以解析的基本信息
            LoginFrontUserVo tempUserVo = new LoginFrontUserVo();
            
            // 尝试从Redis中获取用户信息（token验证成功说明Redis中有对应的用户数据）
            try {
                // 尝试从Redis获取完整的用户信息
                Object userDataFromRedis = redisUtil.get(redisTokenKey);
                if (userDataFromRedis != null && userDataFromRedis instanceof Integer) {
                    // Redis中存储的是用户ID
                    Integer userId = (Integer) userDataFromRedis;
                    com.zbkj.common.model.user.User user = new com.zbkj.common.model.user.User();
                    user.setId(userId);
                    user.setNickname("用户" + userId); // 设置默认昵称
                    tempUserVo.setUser(user);
                    log.info("从Redis获取用户ID: {}", userId);
                } else {
                    // 从token中提取用户ID作为备用方案
                    String userId = extractUserIdFromToken(token);
                    if (userId != null) {
                        com.zbkj.common.model.user.User user = new com.zbkj.common.model.user.User();
                        user.setId(Integer.valueOf(userId));
                        user.setNickname("用户" + userId); // 设置默认昵称
                        tempUserVo.setUser(user);
                        log.info("从token提取用户ID: {}", userId);
                    }
                }
            } catch (Exception e) {
                log.debug("从Redis获取用户信息失败，但token有效，继续处理: {}", e.getMessage());
                // 创建默认用户对象
                com.zbkj.common.model.user.User user = new com.zbkj.common.model.user.User();
                user.setId(0); // 临时ID，后续会从token中获取
                user.setNickname("默认用户"); // 设置默认昵称
                tempUserVo.setUser(user);
            }
            
            return tempUserVo;
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从token中提取用户ID
     */
    private String extractUserIdFromToken(String token) {
        try {
            // token格式通常是 "user:normal:userIdString"
            if (token.startsWith("user:normal:")) {
                return token.substring("user:normal:".length());
            }
            return null;
        } catch (Exception e) {
            log.warn("提取用户ID失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 将前端token转换为Redis key格式
     */
    private String convertTokenToRedisKey(String token) {
        try {
            // 前端token格式: "user:normal:7513f2b5ef764f3c9c6981bc1e2e2614"
            // Redis key格式: "TOKEN:USER:NORMAL:7513f2b5ef764f3c9c6981bc1e2e2614"
            if (token.startsWith("user:normal:")) {
                String uuid = token.substring("user:normal:".length());
                return "TOKEN:USER:NORMAL:" + uuid;
            }
            // 如果已经是Redis key格式，直接返回
            if (token.startsWith("TOKEN:USER:NORMAL:")) {
                return token;
            }
            // 其他情况返回原token
            return token;
        } catch (Exception e) {
            log.warn("Token格式转换失败: {}", e.getMessage());
            return token;
        }
    }
}
