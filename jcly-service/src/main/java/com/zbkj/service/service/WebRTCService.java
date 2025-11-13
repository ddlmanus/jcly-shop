package com.zbkj.service.service;

import java.util.Map;

/**
 * WebRTC音频通话服务接口
 */
public interface WebRTCService {

    /**
     * 发起音频通话
     * @param callerId 发起者ID
     * @param calleeId 接收者ID
     * @param callerType 发起者类型
     * @param calleeType 接收者类型
     * @return 通话信息
     */
    Map<String, Object> initiateCall(Integer callerId, Integer calleeId, String callerType, String calleeType);

    /**
     * 接受音频通话
     * @param callId 通话ID
     * @param userId 用户ID
     * @return 通话信息
     */
    Map<String, Object> acceptCall(String callId, Integer userId);

    /**
     * 拒绝音频通话
     * @param callId 通话ID
     * @param userId 用户ID
     * @param reason 拒绝原因
     */
    void rejectCall(String callId, String userId, String reason);

    /**
     * 结束音频通话
     * @param callId 通话ID
     * @param userId 用户ID
     */
    void endCall(String callId, String userId);

    /**
     * 获取通话状态
     * @param callId 通话ID
     * @return 通话状态
     */
    Map<String, Object> getCallStatus(String callId);

    /**
     * 处理WebRTC信令
     * @param callId 通话ID
     * @param type 信令类型
     * @param data 信令数据
     */
    void handleSignaling(String callId, String type, Object data);

    /**
     * 检查用户是否在线
     * @param userId 用户ID
     * @param userType 用户类型
     * @return 是否在线
     */
    boolean isUserOnline(Integer userId, String userType);

    /**
     * 获取在线用户数量
     * @return 在线用户数量
     */
    int getOnlineUserCount();
}
