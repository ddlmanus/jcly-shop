package com.zbkj.service.service.impl;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zbkj.common.model.coze.stream.*;
import com.zbkj.service.service.CozeJwtService;
import com.zbkj.service.service.CozeStreamClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Coze流式客户端实现
 * 处理SSE (Server-Sent Events) 流式响应
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Service
public class CozeStreamClientImpl implements CozeStreamClient {
    
    @Autowired
    private CozeJwtService cozeJwtService;
    
    @Value("${coze.api.baseUrl:https://api.coze.cn}")
    private String cozeBaseUrl;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public CozeStreamResponse startStreamChat(Map<String, Object> requestParams, 
                                            Consumer<String> eventCallback) throws Exception {
        
        log.info("开始发起Coze流式对话，参数: {}", JSONUtil.toJsonStr(requestParams));
        
        // 确保开启流式响应
        requestParams.put("stream", true);
        
        String url = buildRequestUrl(requestParams);
        String requestBody = objectMapper.writeValueAsString(requestParams);
        
        log.info("请求URL: {}", url);
        log.info("请求体: {}", requestBody);
        
        CozeStreamResponse streamResponse = new CozeStreamResponse();
        
        try {
            // 建立HTTP连接
            HttpURLConnection connection = createConnection(url, requestBody);
            
            // 检查响应状态
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                String errorMessage = "HTTP请求失败，状态码: " + responseCode;
                log.error(errorMessage);
                streamResponse.setFailed(true);
                streamResponse.setErrorMessage(errorMessage);
                return streamResponse;
            }
            
            // 读取流式响应
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                
                processStreamResponse(reader, streamResponse, eventCallback);
            }
            
            connection.disconnect();
            
        } catch (Exception e) {
            log.error("处理Coze流式响应失败", e);
            streamResponse.setFailed(true);
            streamResponse.setErrorMessage("流式响应处理失败: " + e.getMessage());
        }
        
        log.info("Coze流式对话完成，状态: {}, 消息数: {}", 
                streamResponse.getFinalStatus(), 
                streamResponse.getDeltaMessages().size());
        
        return streamResponse;
    }
    
    @Override
    public CozeStreamResponse startStreamChat(Map<String, Object> requestParams) throws Exception {
        return startStreamChat(requestParams, null);
    }
    
    /**
     * 构建请求URL
     */
    private String buildRequestUrl(Map<String, Object> requestParams) {
        String url = cozeBaseUrl + "/v3/chat";
        
        // 如果参数中包含conversation_id，添加到URL查询参数中
        if (requestParams.containsKey("conversation_id")) {
            url += "?conversation_id=" + requestParams.get("conversation_id");
            // 从body中移除conversation_id，避免重复
            requestParams.remove("conversation_id");
        }
        
        return url;
    }
    
    /**
     * 创建HTTP连接
     */
    private HttpURLConnection createConnection(String url, String requestBody) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        
        // 设置请求方法和头部
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "text/event-stream");
        connection.setRequestProperty("Cache-Control", "no-cache");
        
        // 设置认证头
        String accessToken = cozeJwtService.getAccessToken();
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        
        // 启用输入输出流
        connection.setDoOutput(true);
        connection.setDoInput(true);
        
        // 写入请求体
        try (java.io.OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(requestBody.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }
        
        return connection;
    }
    
    /**
     * 处理流式响应
     */
    private void processStreamResponse(BufferedReader reader, 
                                     CozeStreamResponse streamResponse,
                                     Consumer<String> eventCallback) throws Exception {
        
        String line;
        String currentEvent = null;
        StringBuilder dataBuilder = new StringBuilder();
        
        while ((line = reader.readLine()) != null) {
            log.debug("接收到SSE行: {}", line);
            
            if (line.isEmpty()) {
                // 空行表示一个完整的事件结束
                if (currentEvent != null && dataBuilder.length() > 0) {
                    String eventData = dataBuilder.toString();
                    processEvent(currentEvent, eventData, streamResponse, eventCallback);
                    
                    // 重置状态
                    currentEvent = null;
                    dataBuilder.setLength(0);
                }
                continue;
            }
            
            if (line.startsWith("event:")) {
                currentEvent = line.substring(6).trim();
            } else if (line.startsWith("data:")) {
                if (dataBuilder.length() > 0) {
                    dataBuilder.append("\n");
                }
                dataBuilder.append(line.substring(5).trim());
            }
        }
        
        // 处理最后一个事件（如果有）
        if (currentEvent != null && dataBuilder.length() > 0) {
            String eventData = dataBuilder.toString();
            processEvent(currentEvent, eventData, streamResponse, eventCallback);
        }
    }
    
    /**
     * 处理单个事件
     */
    private void processEvent(String eventType, String eventData, 
                            CozeStreamResponse streamResponse,
                            Consumer<String> eventCallback) {
        
        log.debug("处理事件: {}, 数据: {}", eventType, eventData);
        
        try {
            // 调用事件回调
            if (eventCallback != null) {
                eventCallback.accept(String.format("event: %s\ndata: %s\n\n", eventType, eventData));
            }
            
            switch (eventType) {
                case CozeStreamEvent.EVENT_CHAT_CREATED:
                case CozeStreamEvent.EVENT_CHAT_IN_PROGRESS:
                case CozeStreamEvent.EVENT_CHAT_COMPLETED:
                case CozeStreamEvent.EVENT_CHAT_FAILED:
                    processChatEvent(eventType, eventData, streamResponse);
                    break;
                    
                case CozeStreamEvent.EVENT_MESSAGE_DELTA:
                    processMessageDelta(eventData, streamResponse);
                    break;
                    
                case CozeStreamEvent.EVENT_MESSAGE_COMPLETED:
                    processMessageCompleted(eventData, streamResponse);
                    break;
                    
                case CozeStreamEvent.EVENT_DONE:
                    streamResponse.setCompleted(true);
                    log.info("流式响应完成");
                    break;
                    
                case CozeStreamEvent.EVENT_ERROR:
                    processErrorEvent(eventData, streamResponse);
                    break;
                    
                default:
                    log.debug("未处理的事件类型: {}", eventType);
                    break;
            }
            
        } catch (Exception e) {
            log.error("处理事件失败: {}, 数据: {}", eventType, eventData, e);
        }
    }
    
    /**
     * 处理Chat事件
     */
    private void processChatEvent(String eventType, String eventData, CozeStreamResponse streamResponse) {
        try {
            CozeChatObject chatObject = objectMapper.readValue(eventData, CozeChatObject.class);
            streamResponse.setChat(chatObject);
            streamResponse.setFinalStatus(chatObject.getStatus());
            
            if (CozeChatObject.STATUS_COMPLETED.equals(chatObject.getStatus())) {
                streamResponse.setCompleted(true);
            } else if (CozeChatObject.STATUS_FAILED.equals(chatObject.getStatus())) {
                streamResponse.setFailed(true);
                if (chatObject.getLastError() != null) {
                    streamResponse.setErrorMessage(chatObject.getLastError().getMsg());
                }
            }
            
            log.debug("Chat事件处理完成: {}, 状态: {}", eventType, chatObject.getStatus());
            
        } catch (Exception e) {
            log.error("解析Chat事件失败: {}", eventData, e);
        }
    }
    
    /**
     * 处理消息增量事件
     */
    private void processMessageDelta(String eventData, CozeStreamResponse streamResponse) {
        try {
            CozeMessageObject messageObject = objectMapper.readValue(eventData, CozeMessageObject.class);
            streamResponse.addDeltaMessage(messageObject);
            
            log.debug("消息增量处理完成: {}", messageObject.getContent());
            
        } catch (Exception e) {
            log.error("解析消息增量失败: {}", eventData, e);
        }
    }
    
    /**
     * 处理消息完成事件
     */
    private void processMessageCompleted(String eventData, CozeStreamResponse streamResponse) {
        try {
            CozeMessageObject messageObject = objectMapper.readValue(eventData, CozeMessageObject.class);
            
            // 如果是assistant的answer类型消息，设置为完整消息
            if (CozeMessageObject.ROLE_ASSISTANT.equals(messageObject.getRole()) &&
                CozeMessageObject.TYPE_ANSWER.equals(messageObject.getType())) {
                streamResponse.setCompleteMessage(messageObject);
            }
            
            log.debug("消息完成处理: {}", messageObject.getContent());
            
        } catch (Exception e) {
            log.error("解析完成消息失败: {}", eventData, e);
        }
    }
    
    /**
     * 处理错误事件
     */
    private void processErrorEvent(String eventData, CozeStreamResponse streamResponse) {
        streamResponse.setFailed(true);
        streamResponse.setErrorMessage("流式响应错误: " + eventData);
        log.error("接收到错误事件: {}", eventData);
    }
}
