package com.zbkj.service.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserRealNameAuth;
import com.zbkj.common.result.CommonResultCode;
import com.zbkj.common.utils.SecurityUtils;
import com.zbkj.common.vo.MyRecord;
import com.zbkj.service.dao.UserRealNameAuthDao;
import com.zbkj.service.service.UserRealNameAuthService;
import com.zbkj.service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户实名认证服务实现
 *
 * @author System
 * @since 2024-01-01
 */
@Slf4j
@Service
public class UserRealNameAuthServiceImpl extends ServiceImpl<UserRealNameAuthDao, UserRealNameAuth> implements UserRealNameAuthService {

    @Resource
    private UserRealNameAuthDao userRealNameAuthDao;

    @Autowired
    private UserService userService;

    @Value("${ocr.api.url:}")
    private String ocrApiUrl;

    @Value("${ocr.api.key:}")
    private String ocrApiKey;

    @Value("${ocr.api.secret:}")
    private String ocrApiSecret;

    @Value("${face.recognition.api.url:}")
    private String faceApiUrl;

    @Value("${face.recognition.api.key:}")
    private String faceApiKey;

    @Value("${face.recognition.api.secret:}")
    private String faceApiSecret;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyRecord performOcrRecognition(Integer uid, String idCardFrontImage, String idCardBackImage) {
        MyRecord result = new MyRecord();
        try {
            // 参数验证
            if (ObjectUtil.isNull(uid) || StrUtil.isBlank(idCardFrontImage) || StrUtil.isBlank(idCardBackImage)) {
                return result.set("success", false).set("message", "参数不能为空");
            }

            // 验证用户是否存在
            User user = userService.getById(uid);
            if (ObjectUtil.isNull(user)) {
                return result.set("success", false).set("message", "用户不存在");
            }

            // 调用OCR识别API
            MyRecord ocrResult = callOcrApi(idCardFrontImage, idCardBackImage);
            
            if (ocrResult.getBoolean("success")) {
                // 保存OCR结果
                UserRealNameAuth authRecord = getUserRealNameAuth(uid);
                if (ObjectUtil.isNull(authRecord)) {
                    authRecord = new UserRealNameAuth();
                    authRecord.setUid(uid);
                    authRecord.setAuthType(1); // OCR认证
                    authRecord.setAuthStatus(1); // 认证中
                    authRecord.setCreateTime(new Date());
                }
                
                authRecord.setRealName(ocrResult.getStr("realName"));
                authRecord.setIdCard(SecurityUtils.encrypt(ocrResult.getStr("idCard")));
                authRecord.setGender(ocrResult.getInt("gender"));
                authRecord.setBirthday(ocrResult.getStr("birthday"));
                authRecord.setNation(ocrResult.getStr("nation"));
                authRecord.setAddress(ocrResult.getStr("address"));
                authRecord.setIssuingAuthority(ocrResult.getStr("issuingAuthority"));
                authRecord.setValidPeriod(ocrResult.getStr("validPeriod"));
                authRecord.setIdCardFrontImage(idCardFrontImage);
                authRecord.setIdCardBackImage(idCardBackImage);
                authRecord.setOcrResult(JSONUtil.toJsonStr(ocrResult));
                authRecord.setConfidenceScore(ocrResult.getBigDecimal("confidenceScore"));
                authRecord.setUpdateTime(new Date());
                
                saveOrUpdate(authRecord);
                
                result.set("success", true)
                      .set("message", "OCR识别成功")
                      .set("authId", authRecord.getId())
                      .set("realName", ocrResult.getStr("realName"))
                      .set("idCard", SecurityUtils.maskIdCard(ocrResult.getStr("idCard")))
                      .set("gender", ocrResult.getInt("gender"))
                      .set("birthday", ocrResult.getStr("birthday"))
                      .set("confidenceScore", ocrResult.getBigDecimal("confidenceScore"));
            } else {
                result.set("success", false)
                      .set("message", "OCR识别失败：" + ocrResult.getStr("message"));
            }

        } catch (Exception e) {
            log.error("OCR识别异常", e);
            result.set("success", false).set("message", "OCR识别失败：" + e.getMessage());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyRecord performFaceRecognition(Integer uid, String faceImage) {
        MyRecord result = new MyRecord();
        try {
            // 参数验证
            if (ObjectUtil.isNull(uid) || StrUtil.isBlank(faceImage)) {
                return result.set("success", false).set("message", "参数不能为空");
            }

            // 验证用户是否存在
            User user = userService.getById(uid);
            if (ObjectUtil.isNull(user)) {
                return result.set("success", false).set("message", "用户不存在");
            }

            // 获取用户OCR认证信息
            UserRealNameAuth authRecord = getUserRealNameAuth(uid);
            if (ObjectUtil.isNull(authRecord)) {
                return result.set("success", false).set("message", "请先完成身份证OCR认证");
            }

            // 调用人脸识别API
            MyRecord faceResult = callFaceRecognitionApi(faceImage, authRecord.getIdCardFrontImage());
            
            if (faceResult.getBoolean("success")) {
                // 更新人脸识别结果
                authRecord.setFaceImage(faceImage);
                authRecord.setFaceResult(JSONUtil.toJsonStr(faceResult));
                authRecord.setAuthType(authRecord.getAuthType() == 1 ? 3 : 2); // 设置为组合认证或人脸认证
                authRecord.setUpdateTime(new Date());
                
                // 如果人脸识别成功且置信度高，直接设置为认证成功
                BigDecimal confidenceScore = faceResult.getBigDecimal("confidenceScore");
                if (confidenceScore.compareTo(new BigDecimal("0.9")) > 0) {
                    authRecord.setAuthStatus(2); // 认证成功
                    authRecord.setAuthTime(new Date());
                }
                
                updateById(authRecord);
                
                result.set("success", true)
                      .set("message", "人脸识别成功")
                      .set("authId", authRecord.getId())
                      .set("confidenceScore", confidenceScore)
                      .set("authStatus", authRecord.getAuthStatus());
            } else {
                result.set("success", false)
                      .set("message", "人脸识别失败：" + faceResult.getStr("message"));
            }

        } catch (Exception e) {
            log.error("人脸识别异常", e);
            result.set("success", false).set("message", "人脸识别失败：" + e.getMessage());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyRecord performLivenessDetection(Integer uid, String faceVideo) {
        MyRecord result = new MyRecord();
        try {
            // 参数验证
            if (ObjectUtil.isNull(uid) || StrUtil.isBlank(faceVideo)) {
                return result.set("success", false).set("message", "参数不能为空");
            }

            // 验证用户是否存在
            User user = userService.getById(uid);
            if (ObjectUtil.isNull(user)) {
                return result.set("success", false).set("message", "用户不存在");
            }

            // 获取用户认证信息
            UserRealNameAuth authRecord = getUserRealNameAuth(uid);
            if (ObjectUtil.isNull(authRecord)) {
                return result.set("success", false).set("message", "请先完成身份证OCR认证");
            }

            // 调用活体检测API
            MyRecord livenessResult = callLivenessDetectionApi(faceVideo);
            
            if (livenessResult.getBoolean("success")) {
                // 更新活体检测结果
                authRecord.setFaceVideo(faceVideo);
                authRecord.setLivenessResult(JSONUtil.toJsonStr(livenessResult));
                authRecord.setUpdateTime(new Date());
                
                // 如果活体检测成功，设置为认证成功
                if (livenessResult.getBoolean("isLive")) {
                    authRecord.setAuthStatus(2); // 认证成功
                    authRecord.setAuthTime(new Date());
                }
                
                updateById(authRecord);
                
                result.set("success", true)
                      .set("message", "活体检测成功")
                      .set("authId", authRecord.getId())
                      .set("isLive", livenessResult.getBoolean("isLive"))
                      .set("confidenceScore", livenessResult.getBigDecimal("confidenceScore"))
                      .set("authStatus", authRecord.getAuthStatus());
            } else {
                result.set("success", false)
                      .set("message", "活体检测失败：" + livenessResult.getStr("message"));
            }

        } catch (Exception e) {
            log.error("活体检测异常", e);
            result.set("success", false).set("message", "活体检测失败：" + e.getMessage());
        }
        return result;
    }

    @Override
    public MyRecord getUserAuthStatus(Integer uid) {
        MyRecord result = new MyRecord();
        try {
            UserRealNameAuth authRecord = getUserRealNameAuth(uid);
            
            if (ObjectUtil.isNull(authRecord)) {
                result.set("success", true)
                      .set("authStatus", 0)
                      .set("authStatusName", "未认证")
                      .set("isAuth", false);
            } else {
                String authStatusName = "";
                switch (authRecord.getAuthStatus()) {
                    case 0:
                        authStatusName = "待认证";
                        break;
                    case 1:
                        authStatusName = "认证中";
                        break;
                    case 2:
                        authStatusName = "认证成功";
                        break;
                    case 3:
                        authStatusName = "认证失败";
                        break;
                }
                
                result.set("success", true)
                      .set("authId", authRecord.getId())
                      .set("authType", authRecord.getAuthType())
                      .set("authStatus", authRecord.getAuthStatus())
                      .set("authStatusName", authStatusName)
                      .set("isAuth", authRecord.getAuthStatus() == 2)
                      .set("realName", SecurityUtils.maskName(authRecord.getRealName()))
                      .set("idCard", SecurityUtils.maskIdCard(SecurityUtils.decrypt(authRecord.getIdCard())))
                      .set("authTime", authRecord.getAuthTime())
                      .set("failReason", authRecord.getFailReason());
            }

        } catch (Exception e) {
            log.error("获取用户认证状态异常", e);
            result.set("success", false).set("message", "获取认证状态失败：" + e.getMessage());
        }
        return result;
    }

    @Override
    public boolean isUserRealNameAuth(Integer uid) {
        try {
            UserRealNameAuth authRecord = getUserRealNameAuth(uid);
            return ObjectUtil.isNotNull(authRecord) && authRecord.getAuthStatus() == 2;
        } catch (Exception e) {
            log.error("检查用户实名认证状态异常", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyRecord submitRealNameAuth(Integer uid, Integer authType, String idCardFrontImage, 
                                      String idCardBackImage, String faceImage, String faceVideo) {
        MyRecord result = new MyRecord();
        try {
            // 参数验证
            if (ObjectUtil.isNull(uid) || ObjectUtil.isNull(authType)) {
                return result.set("success", false).set("message", "参数不能为空");
            }

            // 验证用户是否存在
            User user = userService.getById(uid);
            if (ObjectUtil.isNull(user)) {
                return result.set("success", false).set("message", "用户不存在");
            }

            // 检查是否已认证
            if (isUserRealNameAuth(uid)) {
                return result.set("success", false).set("message", "您已完成实名认证");
            }

            // 根据认证类型处理
            switch (authType) {
                case 1: // 仅OCR认证
                    if (StrUtil.isBlank(idCardFrontImage) || StrUtil.isBlank(idCardBackImage)) {
                        return result.set("success", false).set("message", "身份证照片不能为空");
                    }
                    return performOcrRecognition(uid, idCardFrontImage, idCardBackImage);
                    
                case 2: // 仅人脸认证
                    if (StrUtil.isBlank(faceImage)) {
                        return result.set("success", false).set("message", "人脸照片不能为空");
                    }
                    return performFaceRecognition(uid, faceImage);
                    
                case 3: // OCR+人脸认证
                    if (StrUtil.isBlank(idCardFrontImage) || StrUtil.isBlank(idCardBackImage) || StrUtil.isBlank(faceImage)) {
                        return result.set("success", false).set("message", "身份证照片和人脸照片不能为空");
                    }
                    // 先进行OCR认证
                    MyRecord ocrResult = performOcrRecognition(uid, idCardFrontImage, idCardBackImage);
                    if (ocrResult.getBoolean("success")) {
                        // 再进行人脸认证
                        return performFaceRecognition(uid, faceImage);
                    }
                    return ocrResult;
                    
                default:
                    return result.set("success", false).set("message", "不支持的认证类型");
            }

        } catch (Exception e) {
            log.error("提交实名认证异常", e);
            result.set("success", false).set("message", "提交实名认证失败：" + e.getMessage());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyRecord auditRealNameAuth(Integer authId, Integer auditorId, Integer authStatus, String auditRemark) {
        MyRecord result = new MyRecord();
        try {
            // 参数验证
            if (ObjectUtil.isNull(authId) || ObjectUtil.isNull(auditorId) || ObjectUtil.isNull(authStatus)) {
                return result.set("success", false).set("message", "参数不能为空");
            }

            // 获取认证记录
            UserRealNameAuth authRecord = getById(authId);
            if (ObjectUtil.isNull(authRecord)) {
                return result.set("success", false).set("message", "认证记录不存在");
            }

            // 更新审核信息
            authRecord.setAuthStatus(authStatus);
            authRecord.setAuditorId(auditorId);
            authRecord.setAuditTime(new Date());
            authRecord.setAuditRemark(auditRemark);
            authRecord.setUpdateTime(new Date());
            
            if (authStatus == 3) { // 认证失败
                authRecord.setFailReason(auditRemark);
            } else if (authStatus == 2) { // 认证成功
                authRecord.setAuthTime(new Date());
            }
            
            boolean updated = updateById(authRecord);
            if (updated) {
                result.set("success", true)
                      .set("message", "审核成功")
                      .set("authId", authId)
                      .set("authStatus", authStatus);
            } else {
                result.set("success", false).set("message", "审核失败");
            }

        } catch (Exception e) {
            log.error("审核实名认证异常", e);
            result.set("success", false).set("message", "审核失败：" + e.getMessage());
        }
        return result;
    }

    @Override
    public UserRealNameAuth getUserRealNameAuth(Integer uid) {
        try {
            LambdaQueryWrapper<UserRealNameAuth> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(UserRealNameAuth::getUid, uid);
            wrapper.eq(UserRealNameAuth::getIsDel, false);
            wrapper.orderByDesc(UserRealNameAuth::getCreateTime);
            wrapper.last("LIMIT 1");
            
            return getOne(wrapper);
        } catch (Exception e) {
            log.error("获取用户实名认证信息异常", e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyRecord reAuth(Integer uid) {
        MyRecord result = new MyRecord();
        try {
            // 参数验证
            if (ObjectUtil.isNull(uid)) {
                return result.set("success", false).set("message", "参数不能为空");
            }

            // 获取当前认证记录
            UserRealNameAuth authRecord = getUserRealNameAuth(uid);
            if (ObjectUtil.isNull(authRecord)) {
                return result.set("success", false).set("message", "没有找到认证记录");
            }

            // 只有认证失败的才能重新认证
            if (authRecord.getAuthStatus() != 3) {
                return result.set("success", false).set("message", "当前状态不允许重新认证");
            }

            // 重置认证状态
            authRecord.setAuthStatus(0); // 待认证
            authRecord.setFailReason(null);
            authRecord.setAuditRemark(null);
            authRecord.setUpdateTime(new Date());
            
            boolean updated = updateById(authRecord);
            if (updated) {
                result.set("success", true)
                      .set("message", "重新认证成功，请重新提交认证材料")
                      .set("authId", authRecord.getId());
            } else {
                result.set("success", false).set("message", "重新认证失败");
            }

        } catch (Exception e) {
            log.error("重新认证异常", e);
            result.set("success", false).set("message", "重新认证失败：" + e.getMessage());
        }
        return result;
    }

    @Override
    public UserRealNameAuth getUserAuthDetail(Object o) {
        return null;
    }

    /**
     * 调用OCR识别API
     */
    private MyRecord callOcrApi(String idCardFrontImage, String idCardBackImage) {
        MyRecord result = new MyRecord();
        try {
            // 如果没有配置OCR API，返回模拟结果
            if (StrUtil.isBlank(ocrApiUrl)) {
                log.warn("未配置OCR API，返回模拟结果");
                return result.set("success", true)
                           .set("realName", "张三")
                           .set("idCard", "110101199001011234")
                           .set("gender", 1)
                           .set("birthday", "1990-01-01")
                           .set("nation", "汉")
                           .set("address", "北京市东城区")
                           .set("issuingAuthority", "北京市公安局东城分局")
                           .set("validPeriod", "2020-01-01至2030-01-01")
                           .set("confidenceScore", new BigDecimal("0.95"));
            }

            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("idCardFrontImage", idCardFrontImage);
            params.put("idCardBackImage", idCardBackImage);
            params.put("timestamp", System.currentTimeMillis());
            params.put("apiKey", ocrApiKey);

            // 生成签名
            String signData = JSONUtil.toJsonStr(params);
            String signature = SecurityUtils.generateSignature(signData, ocrApiSecret);
            params.put("signature", signature);

            // 发送HTTP请求
            HttpResponse response = HttpRequest.post(ocrApiUrl)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(params))
                    .timeout(30000)
                    .execute();

            if (response.isOk()) {
                String responseBody = response.body();
                JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
                
                if (jsonResponse.getBool("success", false)) {
                    JSONObject data = jsonResponse.getJSONObject("data");
                    result.set("success", true)
                          .set("realName", data.getStr("realName"))
                          .set("idCard", data.getStr("idCard"))
                          .set("gender", data.getInt("gender"))
                          .set("birthday", data.getStr("birthday"))
                          .set("nation", data.getStr("nation"))
                          .set("address", data.getStr("address"))
                          .set("issuingAuthority", data.getStr("issuingAuthority"))
                          .set("validPeriod", data.getStr("validPeriod"))
                          .set("confidenceScore", data.getBigDecimal("confidenceScore"));
                } else {
                    result.set("success", false)
                          .set("message", jsonResponse.getStr("message"));
                }
            } else {
                result.set("success", false)
                      .set("message", "OCR接口调用失败");
            }

        } catch (Exception e) {
            log.error("调用OCR API异常", e);
            result.set("success", false)
                  .set("message", "OCR服务暂时不可用");
        }
        return result;
    }

    /**
     * 调用人脸识别API
     */
    private MyRecord callFaceRecognitionApi(String faceImage, String idCardImage) {
        MyRecord result = new MyRecord();
        try {
            // 如果没有配置人脸识别API，返回模拟结果
            if (StrUtil.isBlank(faceApiUrl)) {
                log.warn("未配置人脸识别API，返回模拟结果");
                return result.set("success", true)
                           .set("confidenceScore", new BigDecimal("0.92"))
                           .set("isMatch", true);
            }

            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("faceImage", faceImage);
            params.put("idCardImage", idCardImage);
            params.put("timestamp", System.currentTimeMillis());
            params.put("apiKey", faceApiKey);

            // 生成签名
            String signData = JSONUtil.toJsonStr(params);
            String signature = SecurityUtils.generateSignature(signData, faceApiSecret);
            params.put("signature", signature);

            // 发送HTTP请求
            HttpResponse response = HttpRequest.post(faceApiUrl)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(params))
                    .timeout(30000)
                    .execute();

            if (response.isOk()) {
                String responseBody = response.body();
                JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
                
                if (jsonResponse.getBool("success", false)) {
                    JSONObject data = jsonResponse.getJSONObject("data");
                    result.set("success", true)
                          .set("confidenceScore", data.getBigDecimal("confidenceScore"))
                          .set("isMatch", data.getBool("isMatch"));
                } else {
                    result.set("success", false)
                          .set("message", jsonResponse.getStr("message"));
                }
            } else {
                result.set("success", false)
                      .set("message", "人脸识别接口调用失败");
            }

        } catch (Exception e) {
            log.error("调用人脸识别API异常", e);
            result.set("success", false)
                  .set("message", "人脸识别服务暂时不可用");
        }
        return result;
    }

    /**
     * 调用活体检测API
     */
    private MyRecord callLivenessDetectionApi(String faceVideo) {
        MyRecord result = new MyRecord();
        try {
            // 如果没有配置活体检测API，返回模拟结果
            if (StrUtil.isBlank(faceApiUrl)) {
                log.warn("未配置活体检测API，返回模拟结果");
                return result.set("success", true)
                           .set("isLive", true)
                           .set("confidenceScore", new BigDecimal("0.88"));
            }

            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("faceVideo", faceVideo);
            params.put("timestamp", System.currentTimeMillis());
            params.put("apiKey", faceApiKey);

            // 生成签名
            String signData = JSONUtil.toJsonStr(params);
            String signature = SecurityUtils.generateSignature(signData, faceApiSecret);
            params.put("signature", signature);

            // 发送HTTP请求
            HttpResponse response = HttpRequest.post(faceApiUrl + "/liveness")
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(params))
                    .timeout(30000)
                    .execute();

            if (response.isOk()) {
                String responseBody = response.body();
                JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
                
                if (jsonResponse.getBool("success", false)) {
                    JSONObject data = jsonResponse.getJSONObject("data");
                    result.set("success", true)
                          .set("isLive", data.getBool("isLive"))
                          .set("confidenceScore", data.getBigDecimal("confidenceScore"));
                } else {
                    result.set("success", false)
                          .set("message", jsonResponse.getStr("message"));
                }
            } else {
                result.set("success", false)
                      .set("message", "活体检测接口调用失败");
            }

        } catch (Exception e) {
            log.error("调用活体检测API异常", e);
            result.set("success", false)
                  .set("message", "活体检测服务暂时不可用");
        }
        return result;
    }
} 