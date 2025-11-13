package com.zbkj.service.service;

import com.zbkj.common.model.coze.stream.CozeStreamResponse;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Coze流式客户端接口
 * 用于处理Coze API的SSE流式响应
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
public interface CozeStreamClient {
    
    /**
     * 发起流式对话
     * 
     * @param requestParams 请求参数
     * @param eventCallback 事件回调函数，实时接收流式事件
     * @return 完整的流式响应结果
     * @throws Exception 处理异常
     */
    CozeStreamResponse startStreamChat(Map<String, Object> requestParams, 
                                     Consumer<String> eventCallback) throws Exception;
    
    /**
     * 发起流式对话（无回调版本）
     * 
     * @param requestParams 请求参数
     * @return 完整的流式响应结果
     * @throws Exception 处理异常
     */
    CozeStreamResponse startStreamChat(Map<String, Object> requestParams) throws Exception;
}
