package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.user.UserRealNameAuth;
import com.zbkj.common.vo.MyRecord;

/**
 * 用户实名认证服务接口
 *
 * @author System
 * @since 2024-01-01
 */
public interface UserRealNameAuthService extends IService<UserRealNameAuth> {

    /**
     * 身份证OCR识别
     *
     * @param uid              用户ID
     * @param idCardFrontImage 身份证正面图片
     * @param idCardBackImage  身份证反面图片
     * @return 识别结果
     */
    MyRecord performOcrRecognition(Integer uid, String idCardFrontImage, String idCardBackImage);

    /**
     * 人脸识别
     *
     * @param uid       用户ID
     * @param faceImage 人脸图片
     * @return 识别结果
     */
    MyRecord performFaceRecognition(Integer uid, String faceImage);

    /**
     * 活体检测
     *
     * @param uid       用户ID
     * @param faceVideo 人脸视频
     * @return 检测结果
     */
    MyRecord performLivenessDetection(Integer uid, String faceVideo);

    /**
     * 获取用户实名认证状态
     *
     * @param uid 用户ID
     * @return 认证状态
     */
    MyRecord getUserAuthStatus(Integer uid);

    /**
     * 检查用户是否已实名认证
     *
     * @param uid 用户ID
     * @return 是否已认证
     */
    boolean isUserRealNameAuth(Integer uid);

    /**
     * 提交实名认证
     *
     * @param uid              用户ID
     * @param authType         认证类型
     * @param idCardFrontImage 身份证正面图片
     * @param idCardBackImage  身份证反面图片
     * @param faceImage        人脸图片
     * @param faceVideo        人脸视频
     * @return 提交结果
     */
    MyRecord submitRealNameAuth(Integer uid, Integer authType, String idCardFrontImage, 
                               String idCardBackImage, String faceImage, String faceVideo);

    /**
     * 审核实名认证
     *
     * @param authId      认证ID
     * @param auditorId   审核人ID
     * @param authStatus  认证状态
     * @param auditRemark 审核备注
     * @return 审核结果
     */
    MyRecord auditRealNameAuth(Integer authId, Integer auditorId, Integer authStatus, String auditRemark);

    /**
     * 获取用户实名认证信息
     *
     * @param uid 用户ID
     * @return 认证信息
     */
    UserRealNameAuth getUserRealNameAuth(Integer uid);

    /**
     * 重新认证
     *
     * @param uid 用户ID
     * @return 重新认证结果
     */
    MyRecord reAuth(Integer uid);

    UserRealNameAuth getUserAuthDetail(Object o);
} 