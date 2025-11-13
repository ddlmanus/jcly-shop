package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.zbkj.common.constants.*;
import com.zbkj.common.enums.SmsTemplateEnum;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.admin.SystemAdmin;
import com.zbkj.common.model.sms.SmsTemplate;
import com.zbkj.common.model.system.SystemNotification;
import com.zbkj.common.result.CommonResultCode;
import com.zbkj.common.result.OnePassResultCode;
import com.zbkj.common.result.SystemConfigResultCode;
import com.zbkj.common.sms.AliyunSmsClient;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.common.utils.RedisUtil;
import com.zbkj.common.utils.RestTemplateUtil;
import com.zbkj.common.utils.ValidateFormUtil;
import com.zbkj.common.vo.OnePassApplicationInfoVo;
import com.zbkj.common.vo.OnePassUserInfoVo;
import com.zbkj.common.vo.OnePassUserSmsVo;
import com.zbkj.common.vo.SendSmsVo;
import com.zbkj.service.service.*;
import com.zbkj.common.sms.AliyunSmsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 行为service实现类
 * +----------------------------------------------------------------------
 * | JCLY [ JCLY赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 * +----------------------------------------------------------------------
 * | Author: dudl
 * +----------------------------------------------------------------------
 */
@Service
public class OnePassSmsServiceImpl implements OnePassSmsService, SmsService {

    private static final Logger logger = LoggerFactory.getLogger(OnePassSmsServiceImpl.class);

    @Autowired
    private OnePassService onePassService;
    @Autowired
    private RestTemplateUtil restTemplateUtil;
    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private SmsTemplateService smsTemplateService;
    @Autowired
    private SystemNotificationService systemNotificationService;
    @Autowired
    private AliyunSmsClient aliyunSmsClient;

    /**
     * 获取阿里云短信配置
     * @param templateType 模板类型：login-登录, register-注册, notice-通知
     * @return 阿里云短信配置
     */
    private AliyunSmsConfig getAliyunSmsConfig(String templateType) {
        String accessKeyId = systemConfigService.getValueByKey(SmsConstants.SMS_CONFIG_KEY_ALIYUN_KEY);
        String accessKeySecret = systemConfigService.getValueByKey(SmsConstants.SMS_CONFIG_KEY_ALIYUN_SECRET);
        String signName = systemConfigService.getValueByKey(SmsConstants.SMS_CONFIG_KEY_ALIYUN_SIGN_NAME);
        String endpoint = systemConfigService.getValueByKey(SmsConstants.SMS_CONFIG_KEY_ALIYUN_ENDPOINT);
        String regionId = systemConfigService.getValueByKey(SmsConstants.SMS_CONFIG_KEY_ALIYUN_REGION_ID);
        
        String templateCode;
        switch (templateType) {
            case "login":
                templateCode = systemConfigService.getValueByKey(SmsConstants.SMS_CONFIG_KEY_ALIYUN_TEMPLATE_LOGIN);
                break;
            case "register":
                templateCode = systemConfigService.getValueByKey(SmsConstants.SMS_CONFIG_KEY_ALIYUN_TEMPLATE_REGISTER);
                break;
            case "notice":
                templateCode = systemConfigService.getValueByKey(SmsConstants.SMS_CONFIG_KEY_ALIYUN_TEMPLATE_NOTICE);
                break;
            default:
                templateCode = systemConfigService.getValueByKey(SmsConstants.SMS_CONFIG_KEY_ALIYUN_TEMPLATE_LOGIN);
        }
        
        if (StrUtil.isBlank(accessKeyId) || StrUtil.isBlank(accessKeySecret) || 
            StrUtil.isBlank(signName) || StrUtil.isBlank(templateCode)) {
            logger.error("阿里云短信配置不完整");
            throw new CrmebException("短信配置不完整，请联系后台管理员");
        }
        
        return new AliyunSmsConfig(accessKeyId, accessKeySecret, signName, templateCode, endpoint, regionId);
    }

    /**
     * 发短信之前的校验 - 1号通相关已注释
     */
    private void beforeSendMessage() {
        // 注释掉1号通相关校验，使用阿里云短信不需要这些校验
        /*
        OnePassUserInfoVo userInfoVo = onePassService.info();
        OnePassUserSmsVo userSmsVo = userInfoVo.getSms();
        Integer open = userSmsVo.getOpen();
        if (!open.equals(1)) {
            logger.error("发送短信请先开通一号通账号服务");
            throw new CrmebException(OnePassResultCode.SMS_NOT_OPEN);
        }
        if (userSmsVo.getNum() <= 0) {
            logger.error("一号通账号服务余量不足");
            throw new CrmebException(OnePassResultCode.SMS_SHORTAGE_IN_NUMBER);
        }
        */
    }

    /**
     * 发送公共验证码
     *
     * @param phone 手机号
     * @param scenario 场景值
     * @return Boolean
     */
    @Override
    public Boolean sendCommonCode(String phone, String scenario) {
        ValidateFormUtil.isPhone(phone, "手机号码错误");
        beforeSendCommonCodeCheck(phone, scenario);
        
        try {
            // 获取短信验证码过期时间
            String codeExpireStr = systemConfigService.getValueByKey(SmsConstants.CONFIG_KEY_SMS_CODE_EXPIRE);
            if (StrUtil.isBlank(codeExpireStr) || Integer.parseInt(codeExpireStr) == 0) {
                codeExpireStr = Constants.NUM_FIVE + "";// 默认5分钟过期
            }
            String code = RandomUtil.randomNumbers(6);
            
            // 获取阿里云配置
            AliyunSmsConfig config = getAliyunSmsConfig("login");
            
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", code);
            String jsonString = JSONObject.toJSONString(jsonObject);
            
            Boolean b = aliyunSmsClient.sendSms(config, phone, jsonString);
            if (!b) {
                logger.error("发送短信失败: 手机号={}", phone);
                throw new CrmebException("发送短信失败，请稍后重试");
            }
            // 将验证码存入redis
            redisUtil.set(StrUtil.format(SmsConstants.SMS_VERIFICATION_CODE_PHONE, scenario, phone), code, Long.valueOf(codeExpireStr), TimeUnit.MINUTES);
            redisUtil.set(StrUtil.format(SmsConstants.SMS_VERIFICATION_PHONE_NUM, scenario, phone), 1, 60L);
            return true;

        } catch (CrmebException e) {
            throw e;
        } catch (Exception e) {
            logger.error("发送短信异常: 手机号={}, 异常信息={}", phone, e.getMessage(), e);
            throw new CrmebException("发送短信失败，请稍后重试");
        }
    }

    /**
     * 发送短信
     *
     * @param mobile 手机号
     * @param content 短信内容
     * @return 发送结果
     */
    @Override
    public com.zbkj.common.vo.MyRecord sendSms(String mobile, String content) {
        com.zbkj.common.vo.MyRecord result = new com.zbkj.common.vo.MyRecord();
        try {
            ValidateFormUtil.isPhone(mobile, "手机号码错误");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", content);
            String jsonString = JSONObject.toJSONString(jsonObject);
            
            AliyunSmsConfig config = getAliyunSmsConfig("register");
            Boolean sendResult = aliyunSmsClient.sendSms(config, mobile, jsonString);
            logger.info("发送短信: mobile={}, content={}", mobile, content);
            if (sendResult) {
                result.set("success", true).set("message", "短信发送成功");
            } else {
                result.set("success", false).set("message", "短信发送失败");
            }
            
        } catch (Exception e) {
            logger.error("发送短信异常", e);
            result.set("success", false).set("message", "短信发送失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 发送公共验证码前校验
     * @param phone 手机号
     * @param scenario 场景值
     */
    private void beforeSendCommonCodeCheck(String phone, String scenario) {
        String key = StrUtil.format(SmsConstants.SMS_VERIFICATION_PHONE_NUM, scenario, phone);
        if (redisUtil.exists(key)) {
            throw new CrmebException("您的短信发送过于频繁，请稍后再试");
        }
    }

    /**
     * 发送支付成功短信
     *
     * @param phone    手机号
     * @param orderNo  订单编号
     * @param payPrice 支付金额
     * @return Boolean
     */
    @Override
    public Boolean sendPaySuccess(String phone, String orderNo, BigDecimal payPrice, String ninkname) {
        try {
            // 注释掉1号通相关校验和发送
            // beforeSendMessage();
            // Integer tempId = getSmsTempId(NotifyConstants.PAY_SUCCESS_MARK);

            // 使用阿里云短信发送支付成功通知
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("nickname", ninkname);
            String jsonString = JSONObject.toJSONString(jsonObject);
            
            AliyunSmsConfig config = getAliyunSmsConfig("notice");
            return aliyunSmsClient.sendSms(config, phone, jsonString);
            
            // 注释掉1号通发送方式
            // return push(phone, tempId, map);
        } catch (Exception e) {
            logger.error("发送短信失败，{}", e.getMessage());
            return false;
        }
    }

    /**
     * 发送订单发货提醒短信
     *
     * @param phone     手机号
     * @param nickName  用户昵称
     * @param storeName 商品名称
     * @param orderNo   订单编号
     */
    @Override
    public Boolean sendOrderDeliverNotice(String phone, String nickName, String storeName, String orderNo) {
        try {
            // 注释掉1号通相关校验和发送
            // beforeSendMessage();
            // Integer tempId = getSmsTempId(NotifyConstants.DELIVER_GOODS_MARK);
            
            // 使用阿里云短信发送发货通知
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("nickname", nickName);
            String jsonString = JSONObject.toJSONString(jsonObject);
            
            AliyunSmsConfig config = getAliyunSmsConfig("notice");
            return aliyunSmsClient.sendSms(config, phone, jsonString);
            
            // 注释掉1号通发送方式
            // return push(phone, tempId, map);
        } catch (Exception e) {
            logger.error("发送短信失败，{}", e.getMessage());
            return false;
        }
    }

    /**
     * 发送订单拆分发货提醒短信
     *
     * @param phone     手机号
     * @param storeName 商品名称
     * @param orderNo   订单编号
     */
    @Override
    public Boolean sendOrderSplitDeliverNotice(String phone, String storeName, String orderNo) {
        try {
            // 注释掉1号通相关校验和发送
            // beforeSendMessage();
            // Integer tempId = getSmsTempId(NotifyConstants.DELIVER_SPLIT_GOODS_MARK);
            
            // 使用阿里云短信发送拆分发货通知
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("store_name", storeName);
            jsonObject.put("order_no", orderNo);
            String jsonString = JSONObject.toJSONString(jsonObject);
            
            AliyunSmsConfig config = getAliyunSmsConfig("notice");
            return aliyunSmsClient.sendSms(config, phone, jsonString);
            
            // 注释掉1号通发送方式
            // return push(phone, tempId, map);
        } catch (Exception e) {
            logger.error("发送短信失败，{}", e.getMessage());
            return false;
        }
    }

    /**
     * 发送入驻审核通过通知短信
     * @param phone 手机号
     * @param date 日期，yyyy-MM-dd
     * @param merName 商户名
     * @param merPhone 商户店长手机号
     * @param pwd 商户店长密码
     * @param siteName 站点名称
     * @return Boolean
     */
    @Override
    public Boolean sendMerchantAuditSuccessNotice(String phone, String date, String merName, String merPhone, String pwd, String siteName) {
        try {
            // 注释掉1号通相关校验和发送
            // beforeSendMessage();
            // Integer tempId = getSmsTempId(NotifyConstants.AUDIT_SUCCESS_MARK);
            
            // 使用阿里云短信发送审核成功通知
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("date", date);
            jsonObject.put("mer", merName);
            jsonObject.put("phone", merPhone);
            jsonObject.put("pwd", pwd);
            jsonObject.put("site_name", siteName);
            String jsonString = JSONObject.toJSONString(jsonObject);
            
            AliyunSmsConfig config = getAliyunSmsConfig("notice");
            return aliyunSmsClient.sendSms(config, phone, jsonString);
            
            // 注释掉1号通发送方式
            // return push(phone, tempId, map);
        } catch (Exception e) {
            logger.error("发送短信失败，{}", e.getMessage());
            return false;
        }
    }

    /**
     * 发送入驻审核未通过通知短信
     * @param phone 手机号
     * @param date 日期，yyyy-MM-dd
     * @param merName 商户名
     * @param siteName 站点名称
     * @return Boolean
     */
    @Override
    public Boolean sendMerchantFileSuccessNotice(String phone, String date, String merName, String siteName) {
        try {
            // 注释掉1号通相关校验和发送
            // beforeSendMessage();
            // Integer tempId = getSmsTempId(NotifyConstants.AUDIT_FAIL_MARK);
            
            // 使用阿里云短信发送审核失败通知
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("date", date);
            jsonObject.put("mer", merName);
            jsonObject.put("site", siteName);
            String jsonString = JSONObject.toJSONString(jsonObject);
            
            AliyunSmsConfig config = getAliyunSmsConfig("notice");
            return aliyunSmsClient.sendSms(config, phone, jsonString);
            
            // 注释掉1号通发送方式
            // return push(phone, tempId, map);
        } catch (Exception e) {
            logger.error("发送短信失败，{}", e.getMessage());
            return false;
        }
    }

    /**
     * 发送生日礼短信
     * @param phone 手机号
     * @param name 祝福内容
     * @return Boolean
     */
    @Override
    public Boolean sendBirthdayPresent(String phone, String name) {
        try {
            // 注释掉1号通相关校验和发送
            // beforeSendMessage();
            // Integer tempId = getSmsTempId(NotifyConstants.BIRTHDAY_PRESENT_MARK);
            
            // 使用阿里云短信发送生日祝福
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", name);
            String jsonString = JSONObject.toJSONString(jsonObject);
            
            AliyunSmsConfig config = getAliyunSmsConfig("notice");
            return aliyunSmsClient.sendSms(config, phone, jsonString);
            
            // 注释掉1号通发送方式
            // return push(phone, tempId, map);
        } catch (Exception e) {
            logger.error("发送短信失败，{}", e.getMessage());
            return false;
        }
    }

    /**
     * 发送下单成功提醒（商户）
     * @param adminList 商户管理员列表
     * @param orderNo 订单号
     */
    @Override
    public Boolean sendPaySuccessToMerchant(List<SystemAdmin> adminList, String orderNo) {
        try {
            // 注释掉1号通相关校验和发送
            // beforeSendMessage();
            // Integer tempId = getSmsTempId(NotifyConstants.MERCHANT_PAY_SUCCESS_REMINDER);
            
            // 使用阿里云短信发送商户支付成功提醒
            for (SystemAdmin admin : adminList) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code", orderNo);
                String jsonString = JSONObject.toJSONString(jsonObject);
                
                AliyunSmsConfig config = getAliyunSmsConfig("notice");
                Boolean result = aliyunSmsClient.sendSms(config, admin.getPhone(), jsonString);
                if (!result) {
                    return false;
                }
            }
            return true;
            
            // 注释掉1号通发送方式
            /*
            for (SystemAdmin admin : adminList) {
                HashMap<String, Object> map = CollUtil.newHashMap();
                map.put("admin_name", admin.getRealName());
                map.put("order_id", orderNo);
                Boolean push = push(admin.getPhone(), tempId, map);
                if (!push) {
                    return false;
                }
            }
            */
        } catch (Exception e) {
            logger.error("发送短信失败，{}", e.getMessage());
            return false;
        }
    }

    /**
     * 校验手机号验证码
     * @param scenario 场景值
     * @param phone 手机号
     * @param code 验证码
     * @return 是否通过校验
     */
    @Override
    public Boolean checkValidateCode(String scenario, String phone, String code) {
        String redisKey = StrUtil.format(SmsConstants.SMS_VERIFICATION_CODE_PHONE, scenario, phone);
        Object validateCode = redisUtil.get(redisKey);
        if (ObjectUtil.isNull(validateCode)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "验证码已过期");
        }
        if (!validateCode.toString().equals(code)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "验证码错误");
        }
        //删除验证码
        redisUtil.delete(redisKey);
        return true;
    }

    /**
     * 商户入驻提醒（平台）
     * @param adminList 管理员列表
     */
    @Override
    public Boolean sendMerchantSettledApply(List<SystemAdmin> adminList, String merName) {
        try {
            // 注释掉1号通相关校验和发送
            // beforeSendMessage();
            // Integer tempId = getSmsTempId(NotifyConstants.MERCHANT_SETTLED_APPLY);
            
            // 使用阿里云短信发送商户入驻申请提醒
            for (SystemAdmin admin : adminList) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("mer_name", merName);
                String jsonString = JSONObject.toJSONString(jsonObject);
                
                AliyunSmsConfig config = getAliyunSmsConfig("notice");
                Boolean result = aliyunSmsClient.sendSms(config, admin.getPhone(), jsonString);
                if (!result) {
                    return false;
                }
            }
            return true;
            
            // 注释掉1号通发送方式
            /*
            for (SystemAdmin admin : adminList) {
                HashMap<String, Object> map = CollUtil.newHashMap();
                map.put("mer_name", merName);
                Boolean push = push(admin.getPhone(), tempId, map);
                if (!push) {
                    return false;
                }
            }
            */
        } catch (Exception e) {
            logger.error("发送短信失败，{}", e.getMessage());
            return false;
        }
    }
    /**
     * 获取短信模板ID
     * @param mark 消息通知标识
     * @return tempId
     */
    private Integer getSmsTempId(String mark) {
        SystemNotification notification = systemNotificationService.getByMark(mark);
        if (ObjectUtil.isNull(notification)) {
            throw new CrmebException(SystemConfigResultCode.NOTIFICATION_NOT_EXIST);
        }
        if (!notification.getIsSms().equals(NotifyConstants.SWITCH_OPEN)) {
            throw new CrmebException(SystemConfigResultCode.NOTIFICATION_SMS_CLOSE.setMessage(notification.getDescription() + "未配置短信相关或已关闭"));
        }
        SmsTemplate smsTemplate = smsTemplateService.getDetail(notification.getSmsId());
        return Integer.valueOf(smsTemplate.getTempId());
    }

    /**
     * 组装发送对象 - 1号通相关已注释
     *
     * @param phone     手机号
     * @param msgTempId 模板id
     * @param mapPram   参数map
     */
    private Boolean push(String phone, Integer msgTempId, HashMap<String, Object> mapPram) {
        // 注释掉1号通发送方式，改用阿里云短信
        /*
        if (StrUtil.isBlank(phone) || msgTempId <= 0) {
            return false;
        }
        SendSmsVo smsVo = new SendSmsVo();
        smsVo.setMobile(phone);
        smsVo.setTemplate(msgTempId);
        smsVo.setParam(JSONObject.toJSONString(mapPram));
        return sendMessage(smsVo);
        */
        return false;
    }

    /**
     * 发送短信 - 1号通相关已注释
     *
     * @param sendSmsVo 短信参数
     */
    private Boolean sendMessage(SendSmsVo sendSmsVo) {
        // 注释掉1号通发送逻辑，改用阿里云短信
        /*
        String result;
        try {
            String token = getOnePassToken();
            HashMap<String, String> header = onePassUtil.getCommonHeader(token);

            Map<String, Object> map = JSONObject.parseObject(sendSmsVo.getParam());
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.add("phone", sendSmsVo.getMobile());
            param.add("temp_id", sendSmsVo.getTemplate());
            map.forEach((key, value) -> param.add(StrUtil.format(SmsConstants.SMS_COMMON_PARAM_FORMAT, key), value));
            result = restTemplateUtil.postFromUrlencoded(OnePassConstants.ONE_PASS_API_URL + OnePassConstants.ONE_PASS_API_SEND_URI_V2, param, header);
            checkResult(result);
        } catch (Exception e) {
            //接口请求异常，需要重新发送
            e.printStackTrace();
            logger.error("发送短信失败,{}", e.getMessage());
            return false;
        }
        return true;
        */
        return false;
    }

    /**
     * 检测结构请求返回的数据 - 1号通相关已注释
     *
     * @param result 接口返回的结果
     * @return JSONObject
     */
    private JSONObject checkResult(String result) {
        // 注释掉1号通结果检测逻辑
        /*
        if (StrUtil.isBlank(result)) {
            throw new CrmebException("短信平台接口异常，没任何数据返回！");
        }
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(result);
        } catch (Exception e) {
            throw new CrmebException("短信平台接口异常！");
        }
        if (SmsConstants.SMS_ERROR_CODE.equals(jsonObject.getInteger("status"))) {
            throw new CrmebException("短信平台接口" + jsonObject.getString("msg"));
        }
        return jsonObject;
        */
        return null;
    }
}
