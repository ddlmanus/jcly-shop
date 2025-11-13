package com.zbkj.service.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMessage;
import cn.binarywang.wx.miniapp.bean.urllink.GenerateUrlLinkRequest;
import cn.binarywang.wx.miniapp.constant.WxMaConstants;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;
import com.zbkj.common.constants.OrderConstants;
import com.zbkj.common.constants.SysConfigConstants;
import com.zbkj.common.constants.TaskConstants;
import com.zbkj.common.constants.WeChatConstants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.order.RefundOrder;
import com.zbkj.common.model.wechat.WechatExceptions;
import com.zbkj.common.request.SaveConfigRequest;
import com.zbkj.common.response.WeChatJsSdkConfigResponse;
import com.zbkj.common.response.WechatOpenUploadResponse;
import com.zbkj.common.response.WechatPublicShareResponse;
import com.zbkj.common.response.WxRefundVo;
import com.zbkj.common.result.CommonResultCode;
import com.zbkj.common.result.WechatResultCode;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.common.utils.RedisUtil;
import com.zbkj.common.utils.RestTemplateUtil;
import com.zbkj.common.utils.XmlUtil;
import com.zbkj.common.vo.CommonSeparateConfigVo;
import com.zbkj.common.vo.WeChatMiniAuthorizeVo;
import com.zbkj.common.vo.WeChatOauthToken;
import com.zbkj.common.vo.WxRefundResponseVo;
import com.zbkj.common.wxjava.properties.WxOpenProperties;
import com.zbkj.service.service.RefundOrderService;
import com.zbkj.service.service.SystemConfigService;
import com.zbkj.service.service.WechatExceptionsService;
import com.zbkj.service.service.WechatService;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.enums.TicketType;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.util.fs.FileUtils;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.material.WxMpMaterial;
import me.chanjar.weixin.mp.bean.material.WxMpMaterialUploadResult;
import me.chanjar.weixin.mp.bean.menu.WxMpMenu;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import me.chanjar.weixin.open.api.WxOpenService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 微信公用服务实现类
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
public class WechatServiceImpl implements WechatService {

    private static final Logger logger = LoggerFactory.getLogger(WechatServiceImpl.class);

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RestTemplateUtil restTemplateUtil;
    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private WechatExceptionsService wechatExceptionsService;
    @Autowired
    private WxMaService wxMaService;
    @Autowired
    private WxMpService wxMpService;
    @Autowired
    private WxOpenService wxOpenService;
    @Autowired
    private WxOpenProperties wxOpenProperties;
    @Autowired
    private RefundOrderService refundOrderService;

    /**
     * 获取开放平台access_token
     * 通过 code 获取
     * 公众号使用
     *
     * @return 开放平台accessToken对象
     */
    @Override
    public WeChatOauthToken getOauth2AccessToken(String code) {
        WxOAuth2AccessToken accessToken;
        try {
            accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
        } catch (WxErrorException e) {
            logger.error("微信公众号获取授权信息失败！微信获取开放平台access_token异常!");
            logger.error(e.getMessage(), e);
            throw new CrmebException("微信公众号获取授权信息失败！");
        }
        WeChatOauthToken weChatOauthToken = new WeChatOauthToken();
        weChatOauthToken.setAccessToken(accessToken.getAccessToken());
        weChatOauthToken.setExpiresIn(String.valueOf(accessToken.getExpiresIn()));
        weChatOauthToken.setRefreshToken(accessToken.getRefreshToken());
        weChatOauthToken.setOpenId(accessToken.getOpenId());
        weChatOauthToken.setScope(accessToken.getScope());
        return weChatOauthToken;
    }

    /**
     * 小程序登录凭证校验
     *
     * @return 小程序登录校验对象
     */
    @Override
    public WeChatMiniAuthorizeVo miniAuthCode(String code) {
        WxMaJscode2SessionResult jscode2SessionResult;
        try {
            jscode2SessionResult = wxMaService.getUserService().getSessionInfo(code);
        } catch (WxErrorException e) {
            logger.error("微信小程序获取授权信息失败！");
            logger.error(e.getMessage(), e);
            throw new CrmebException("微信小程序获取授权信息失败！");
        }
        WeChatMiniAuthorizeVo miniAuthorizeVo = new WeChatMiniAuthorizeVo();
        miniAuthorizeVo.setOpenId(jscode2SessionResult.getOpenid());
        miniAuthorizeVo.setUnionId(jscode2SessionResult.getUnionid());
        miniAuthorizeVo.setSessionKey(jscode2SessionResult.getSessionKey());
        return miniAuthorizeVo;
    }

    /**
     * 获取微信公众号js配置参数
     *
     * @return WeChatJsSdkConfigResponse
     */
    @Override
    public WeChatJsSdkConfigResponse getPublicJsConfig(String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new CrmebException("url无法解析！");
        }

        String appId = wxMpService.getWxMpConfigStorage().getAppId();
        if (StrUtil.isBlank(appId)) {
            throw new CrmebException(WechatResultCode.PUBLIC_APPID_NOT_CONFIG);
        }
        String ticket = getJsApiTicket();
        String nonceStr = CrmebUtil.getUuid();
        Long timestamp = DateUtil.currentSeconds();
        String signature = getJsSDKSignature(nonceStr, ticket, timestamp, url);

        WeChatJsSdkConfigResponse response = new WeChatJsSdkConfigResponse();
        response.setUrl(url);
        response.setAppId(appId);
        response.setNonceStr(nonceStr);
        response.setTimestamp(timestamp);
        response.setSignature(signature);
        response.setJsApiList(CrmebUtil.stringToArrayStr(WeChatConstants.PUBLIC_API_JS_API_SDK_LIST));
        return response;
    }

    /**
     * 生成小程序码
     *
     * @param jsonObject  微信端参数
     * @return 小程序码
     */
    @Override
    public String createQrCode(JSONObject jsonObject) {
        String miniAccessToken = getWxMiniAccessToken();
        String url = StrUtil.format(WeChatConstants.WECHAT_MINI_QRCODE_UNLIMITED_URL, miniAccessToken);
        logger.info("微信小程序码生成参数:{}", jsonObject);
        byte[] bytes = restTemplateUtil.postJsonDataAndReturnBuffer(url, jsonObject);
        String response = new String(bytes);
        if (StringUtils.contains(response, "errcode")) {
            logger.error("微信生成小程序码异常" + response);
            JSONObject data = JSONObject.parseObject(response);
            // 保存到微信异常表
            wxExceptionDispose(data, "微信小程序生成小程序码异常");
            if (data.getString("errcode").equals("40001")) {
                miniAccessToken = getWxMiniAccessToken(true);
                url = StrUtil.format(WeChatConstants.WECHAT_MINI_QRCODE_UNLIMITED_URL, miniAccessToken);
                bytes = restTemplateUtil.postJsonDataAndReturnBuffer(url, jsonObject);
                response = new String(bytes);
                if (StringUtils.contains(response, "errcode")) {
                    logger.error("微信生成小程序码重试异常" + response);
                    JSONObject data2 = JSONObject.parseObject(response);
                    // 保存到微信异常表
                    wxExceptionDispose(data2, "微信小程序重试生成小程序码异常");
                } else {
                    try {
                        return CrmebUtil.getBase64Image(Base64.encodeBase64String(bytes));
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new CrmebException("微信小程序码转换Base64异常");
                    }
                }
            }
            throw new CrmebException(WechatResultCode.ROUTINE_CREATE_QRCODE_EXCEPTION, "微信生成二维码异常");
        }
        try {
            return CrmebUtil.getBase64Image(Base64.encodeBase64String(bytes));
        } catch (Exception e) {
            e.printStackTrace();
            throw new CrmebException("微信小程序码转换Base64异常");
        }
    }

    /**
     * 微信公众号发送模板消息
     *
     * @param templateId 模板ID
     * @param openId 微信小程序用户openid
     * @param dataMap 数据Map
     * @return 是否发送成功
     */
    @Override
    public Boolean sendPublicTemplateMessage(String templateId, String openId, HashMap<String, String> dataMap) {
        WxMpTemplateMessage mpTemplateMessage = WxMpTemplateMessage.builder()
                .toUser(openId)
                .templateId(templateId)
                .build();
        dataMap.forEach((key, value) -> mpTemplateMessage.addData(new WxMpTemplateData(key, value)));
        try {
            wxMpService.getTemplateMsgService().sendTemplateMsg(mpTemplateMessage);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logger.error("微信公众号发送模板消息异常,templateId={},openId={}", templateId, openId);
        }
        return Boolean.TRUE;
    }

    /**
     * 微信小程序发送订阅消息
     * @param templateId 模板ID
     * @param openId 微信小程序用户openid
     * @param dataMap 数据Map
     */
    @Override
    public Boolean sendMiniSubscribeMessage(String templateId, String openId, HashMap<String, String> dataMap) {
        WxMaSubscribeMessage message = new WxMaSubscribeMessage();
        message.setTemplateId(templateId);
        message.setToUser(openId);
        message.setLang(WxMaConstants.MiniProgramLang.ZH_CN);
        message.setMiniprogramState(WxMaConstants.MiniProgramState.FORMAL);
        dataMap.forEach((key, value) -> message.addData(new WxMaSubscribeMessage.MsgData(key, value)));
        try {
            wxMaService.getMsgService().sendSubscribeMsg(message);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logger.error("微信小程序发送订阅消息异常,templateId={},openId={}", templateId, openId);
        }
        return Boolean.TRUE;
    }

    /**
     * 获取微信公众号自定义菜单配置
     * （使用本自定义菜单查询接口可以获取默认菜单和全部个性化菜单信息）
     *
     * @return 公众号自定义菜单
     */
    @Override
    public JSONObject getPublicCustomMenu() {
        JSONObject jsonObject = redisUtil.get(WeChatConstants.REDIS_PUBLIC_MENU_KEY);
        if (ObjectUtil.isNotNull(jsonObject) && StrUtil.isNotBlank(jsonObject.toString())) {
            return jsonObject;
        }
        try {
            WxMpMenu menu = wxMpService.getMenuService().menuGet();
            if (ObjectUtil.isNull(menu)) {
                throw new CrmebException("微信平台接口异常，没任何数据返回！");
            }
            String menuJson = menu.toJson();
            jsonObject = JSONObject.parseObject(menuJson);
        } catch (Exception e) {
            logger.error("获取微信公众号自定义菜单配置,微信平台接口异常");
            logger.error(e.getMessage(), e);
            throw new CrmebException("获取微信公众号自定义菜单配置,微信平台接口异常！");
        }
        redisUtil.set(WeChatConstants.REDIS_PUBLIC_MENU_KEY, jsonObject);
        return jsonObject;
    }

    /**
     * 创建微信自定义菜单
     *
     * @param data 菜单json字符串 创建数据格式从微信菜单接口中获取 编辑后通过此接口提交给微信
     * @return 创建结果
     */
    @Override
    public Boolean createPublicCustomMenu(String data) {
        String menuId;
        try {
            menuId = wxMpService.getMenuService().menuCreate(data);
        } catch (Exception e) {
            logger.error("微信公众号创建自定义菜单异常");
            logger.error(e.getMessage(), e);
            throw new CrmebException("微信公众号创建自定义菜单异常！");
        }
        if (StrUtil.isNotBlank(menuId)) {
            throw new CrmebException("微信平台接口异常，没任何数据返回！");
        }
        // 清除历史缓存
        if (redisUtil.exists(WeChatConstants.REDIS_PUBLIC_MENU_KEY)) {
            redisUtil.delete(WeChatConstants.REDIS_PUBLIC_MENU_KEY);
        }
        return Boolean.TRUE;
    }

    /**
     * 删除微信自定义菜单
     *
     * @return 删除结果
     */
    @Override
    public Boolean deletePublicCustomMenu() {
        try {
            wxMpService.getMenuService().menuDelete();
        } catch (Exception e) {
            logger.error("微信公众号删除自定义菜单异常");
            logger.error(e.getMessage(), e);
            throw new CrmebException("微信公众号删除自定义菜单异常！");
        }
        // 清除历史缓存
        if (redisUtil.exists(WeChatConstants.REDIS_PUBLIC_MENU_KEY)) {
            redisUtil.delete(WeChatConstants.REDIS_PUBLIC_MENU_KEY);
        }
        return Boolean.TRUE;
    }

    /**
     * 微信公众平台上传素材
     *
     * @param file 文件
     * @param type 类型 图片（image）、语音（voice)
     * @return WechatOpenUploadResponse
     */
    @Override
    public WechatOpenUploadResponse openMediaUpload(MultipartFile file, String type) {
        if (StrUtil.isBlank(type)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "请选择素材类型");
        }
        if (!"image".equals(type) && !"voice".equals(type)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "未知的素材类型");
        }
        String[] split = Objects.requireNonNull(file.getOriginalFilename()).split("\\.");
        String suffixName = split[split.length - 1];
        isValidPic(file.getSize(), suffixName, type);

        File tempFile;
        try {
            InputStream inputStream = file.getResource().getInputStream();
            tempFile = FileUtils.createTmpFile(inputStream, UUID.randomUUID().toString(), suffixName);
        } catch (Exception e) {
            logger.error("获取文件输入流失败");
            throw new CrmebException(e.getMessage());
        }
        WxMpMaterial wxMaterial = new WxMpMaterial();
        wxMaterial.setFile(tempFile);
        wxMaterial.setName(file.getOriginalFilename());
        WxMpMaterialUploadResult res;
        try {
            res = wxMpService.getMaterialService().materialFileUpload(type, wxMaterial);
        } catch (WxErrorException e) {
            logger.error("微信公众平台上传素材失败");
            logger.error(e.getMessage(), e);
            throw new CrmebException("微信公众平台上传素材失败");
        }
        if (StrUtil.isBlank(res.getMediaId())) {
            throw new CrmebException("微信公众平台上传素材失败");
        }
        if (WxConsts.MediaFileType.IMAGE.equals(type)
                || WxConsts.MediaFileType.THUMB.equals(type)) {
            if (StrUtil.isBlank(res.getUrl())) {
                throw new CrmebException("微信公众平台上传素材失败");
            }
        }

        WechatOpenUploadResponse response = new WechatOpenUploadResponse();
        response.setMediaId(res.getMediaId());
        response.setUrl(res.getUrl());
        response.setName(file.getOriginalFilename().replace(suffixName, ""));
        return response;
    }

    /**
     * 微信公众号分享配置
     */
    @Override
    public WechatPublicShareResponse getPublicShare() {
        WechatPublicShareResponse response = new WechatPublicShareResponse();
        response.setImage(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_ADMIN_WECHAT_SHARE_IMAGE));
        response.setTitle(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_ADMIN_WECHAT_SHARE_TITLE));
        response.setSynopsis(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_ADMIN_WECHAT_SHARE_SYNOPSIS));
        return response;
    }

    /**
     * 获取微信小程序发货开关
     */
    @Override
    public CommonSeparateConfigVo getShippingSwitch() {
        String value = systemConfigService.getValueByKey(WeChatConstants.CONFIG_WECHAT_ROUTINE_SHIPPING_SWITCH);
        CommonSeparateConfigVo vo = new CommonSeparateConfigVo();
        vo.setKey(WeChatConstants.CONFIG_WECHAT_ROUTINE_SHIPPING_SWITCH);
        vo.setValue(StrUtil.isNotBlank(value) ? value : "0");
        return vo;
    }

    /**
     * 更新微信小程序发货开关
     */
    @Override
    public Boolean updateShippingSwitch(SaveConfigRequest request) {
        String value = StrUtil.isNotBlank(request.getValue()) ? request.getValue() : "0";
        return systemConfigService.updateOrSaveValueByName(WeChatConstants.CONFIG_WECHAT_ROUTINE_SHIPPING_SWITCH, value);
    }

    /**
     * 生成小程序url链接
     * @param path 通过 URL Link 进入的小程序页面路径，必须是已经发布的小程序存在的页面，不可携带 query 。path 为空时会跳转小程序主页
     * @param query 通过 URL Link 进入小程序时的query，最大1024个字符，只支持数字，大小写英文以及部分特殊字符：!#$&'()*+,/:;=?@-._~%
     * @param expireInterval 到期失效的URL Link的失效间隔天数。生成的到期失效URL Link在该间隔时间到达前有效。最长间隔天数为30天。expire_type 为 1 必填
     * @param envVersion 默认值"release"。要打开的小程序版本。正式版为 "release"，体验版为"trial"，开发版为"develop"，仅在微信外打开时生效
     * @return 生成的小程序 URL Link
     */
    @Override
    public String generateMiniUrlLink(String path, String query, Integer expireInterval, String envVersion) {
        GenerateUrlLinkRequest urlLinkRequest = new GenerateUrlLinkRequest();
        urlLinkRequest.setPath(path);
        if (StrUtil.isNotBlank(query)) {
            urlLinkRequest.setQuery(query);
        }
        urlLinkRequest.setExpireType(1);
        urlLinkRequest.setExpireInterval(expireInterval);
        String urlLink;
        try {
            urlLink = wxMaService.getLinkService().generateUrlLink(urlLinkRequest);
        } catch (WxErrorException e) {
            logger.error("微信小程序生成加密URLLink异常！");
            // 保存到微信异常表
            throw new CrmebException("微信小程序生成加密URLLink异常！");
        }
        return urlLink;
    }

    /**
     * 获取微信小程序用户手机号
     *
     * @param code 小程序code
     */
    @Override
    public WxMaPhoneNumberInfo getMiniUserPhoneNumber(String code) {
        WxMaPhoneNumberInfo phoneNumber;
        try {
            phoneNumber = wxMaService.getUserService().getPhoneNumber(code);
        } catch (WxErrorException e) {
            logger.error("获取微信小程序用户手机号失败！");
            logger.error(e.getMessage(), e);
            throw new CrmebException("获取微信小程序用户手机号失败！");
        }
        return phoneNumber;
    }

    @Override
    public WeChatOauthToken getOpenOauth2AccessToken(String code) {
        String url = StrUtil.format(WeChatConstants.WECHAT_OAUTH2_ACCESS_TOKEN_URL, wxOpenProperties.getComponentAppId(), wxOpenProperties.getComponentSecret(), code);
        JSONObject data = restTemplateUtil.getData(url);
        if (ObjectUtil.isNull(data)) {
            throw new CrmebException("微信开放平台接口异常，没任何数据返回！");
        }
        if (data.containsKey("errcode") && !data.getString("errcode").equals("0")) {
            if (data.containsKey("errmsg")) {
                // 保存到微信异常表
                wxExceptionDispose(data, "微信开放平台获取accessToken异常");
                throw new CrmebException("微信接口调用失败：" + data.getString("errcode") + data.getString("errmsg"));
            }
        }
        WeChatOauthToken weChatOauthToken = new WeChatOauthToken();
        weChatOauthToken.setAccessToken(data.getString("access_token"));
        weChatOauthToken.setExpiresIn(String.valueOf(data.getInteger("expires_in")));
        weChatOauthToken.setRefreshToken(data.getString("refresh_token"));
        weChatOauthToken.setOpenId(data.getString("openid"));
        weChatOauthToken.setScope(data.getString("scope"));
        return weChatOauthToken;
    }

    /**
     * 获取开放平台Appid
     * @return Appid
     */
    @Override
    public String getOpenAppid() {
        return Optional.ofNullable(wxOpenService.getWxOpenConfigStorage().getComponentAppId()).orElse("");
    }

    /**
     * 微信公众号生成带参数的临时二维码
     * @param sceneId 场景标识ID
     * @param expireSeconds 二维码有效时间，以秒为单位。 最大不超过2592000（即30天），此字段如果不填，则默认有效期为60秒。
     */
    @Override
    public WxMpQrCodeTicket createPublicQrcodeTmpTicket(Integer sceneId, Integer expireSeconds) {
        WxMpQrCodeTicket wxMpQrCodeTicket;
        try {
            wxMpQrCodeTicket = wxMpService.getQrcodeService().qrCodeCreateTmpTicket(sceneId, expireSeconds);
        } catch (WxErrorException e) {
            logger.error("生成公众号带参数临时二维码失败！sceneId={}", sceneId);
            throw new RuntimeException(e);
        }
        return wxMpQrCodeTicket;
    }

    /**
     * 微信公众号生成带参数的临时二维码
     * @param sceneStr 场景标识字符
     * @param expireSeconds 二维码有效时间，以秒为单位。 最大不超过2592000（即30天），此字段如果不填，则默认有效期为60秒。
     */
    @Override
    public WxMpQrCodeTicket createPublicQrcodeTmpTicket(String sceneStr, Integer expireSeconds) {
        WxMpQrCodeTicket wxMpQrCodeTicket;
        try {
            wxMpQrCodeTicket = wxMpService.getQrcodeService().qrCodeCreateTmpTicket(sceneStr, expireSeconds);
        } catch (WxErrorException e) {
            logger.error("生成公众号带参数临时二维码失败！sceneStr={}", sceneStr);
            throw new RuntimeException(e);
        }
        return wxMpQrCodeTicket;
    }

    /**
     * 获取JS-SDK的签名
     *
     * @param nonceStr  随机字符串
     * @param ticket    ticket
     * @param timestamp 时间戳
     * @param url       url
     * @return 签名
     */
    private String getJsSDKSignature(String nonceStr, String ticket, Long timestamp, String url) {
        //注意这里参数名必须全部小写，且必须有序
        String paramString = StrUtil.format("jsapi_ticket={}&noncestr={}&timestamp={}&url={}", ticket, nonceStr, timestamp, url);
        return SecureUtil.sha1(paramString);
    }

    /**
     * 获取JS-SDK的ticket
     * 用于计算签名
     *
     * @return ticket
     */
    private String getJsApiTicket() {
        boolean exists = redisUtil.exists(WeChatConstants.REDIS_PUBLIC_JS_API_TICKET);
        if (exists) {
            Object ticket = redisUtil.get(WeChatConstants.REDIS_PUBLIC_JS_API_TICKET);
            return ticket.toString();
        }
        String ticket;
        try {
            ticket = wxMpService.getTicket(TicketType.JSAPI);
        } catch (Exception e) {
            logger.error("微信获取JS-SDK的ticket异常");
            logger.error(e.getMessage(), e);
            throw new CrmebException("微信获取JS-SDK的ticket失败");
        }
        redisUtil.set(WeChatConstants.REDIS_PUBLIC_JS_API_TICKET, ticket, WeChatConstants.REDIS_PUBLIC_JS_API_TICKET_EXPRESS, TimeUnit.SECONDS);
        return ticket;
    }

    /**
     * 微信异常处理
     *
     * @param jsonObject 微信返回数据
     * @param remark     备注
     */
    private void wxExceptionDispose(JSONObject jsonObject, String remark) {
        WechatExceptions wechatExceptions = new WechatExceptions();
        wechatExceptions.setErrcode(jsonObject.getString("errcode"));
        wechatExceptions.setErrmsg(StrUtil.isNotBlank(jsonObject.getString("errmsg")) ? jsonObject.getString("errmsg") : "");
        wechatExceptions.setData(jsonObject.toJSONString());
        wechatExceptions.setRemark(remark);
        wechatExceptions.setCreateTime(DateUtil.date());
        wechatExceptions.setUpdateTime(DateUtil.date());
        wechatExceptionsService.save(wechatExceptions);
    }

    /**
     * 是否符合微信规范
     *
     * @param size       long 文件大小
     * @param suffixName String 后缀名
     */
    private void isValidPic(long size, String suffixName, String type) {
        JSONObject config = getMediaUploadConfig();
        long supportSize = config.getJSONObject(type).getLong("size");
        if (supportSize < size) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "文件大小不能超过" + supportSize);
        }
        String supportNameSuffix = config.getJSONObject(type).getString("suffix");
        List<String> suffixNameList = CrmebUtil.stringToArrayStr(supportNameSuffix);
        if (!suffixNameList.contains(suffixName)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "文件格式必须是" + supportSize);
        }
    }

    /**
     * 获取素材上传配置
     */
    private JSONObject getMediaUploadConfig() {
        String data = StrUtil.format("{image:{size:\"{}\", suffix: \"{}\"}, voice:{size:\"{}\", suffix: \"{}\"}}",
                WeChatConstants.OPEN_MEDIA_UPLOAD_IMAGE_MAX_SIZE, WeChatConstants.OPEN_MEDIA_UPLOAD_IMAGE_SUFFIX_NAME,
                WeChatConstants.OPEN_MEDIA_UPLOAD_VOICE_MAX_SIZE, WeChatConstants.OPEN_MEDIA_UPLOAD_VOICE_SUFFIX_NAME);
        return JSONObject.parseObject(data);
    }

    private String getWxMiniAccessToken() {
        String accessToken;
        try {
            accessToken = wxMaService.getAccessToken();
        } catch (WxErrorException e) {
            logger.error("获取微信accessToken失败！");
            logger.error(e.getMessage(), e);
            throw new CrmebException("获取微信accessToken失败！");
        }
        return accessToken;
    }

    @Override
    public String getWxMiniAccessToken(Boolean forceRefresh) {
        String accessToken;
        try {
            accessToken = wxMaService.getAccessToken(forceRefresh);
        } catch (WxErrorException e) {
            logger.error("获取微信accessToken失败！");
            logger.error(e.getMessage(), e);
            throw new CrmebException("获取微信accessToken失败！");
        }
        return accessToken;
    }
    /**
     * 微信申请退款
     *
     * @param wxRefundVo 微信申请退款对象
     * @param path       商户p12证书绝对路径
     * @return 申请退款结果对象
     */
    @Override
    public WxRefundResponseVo payRefund(WxRefundVo wxRefundVo, String path) {
        String xmlStr = XmlUtil.objectToXml(wxRefundVo);
        String url = WeChatConstants.PAY_API_URL + WeChatConstants.PAY_REFUND_API_URI_WECHAT;
        HashMap<String, Object> map = CollUtil.newHashMap();
        String xml = "";
        try {
            xml = restTemplateUtil.postWXRefundXml(url, xmlStr, wxRefundVo.getMch_id(), path);
            map = XmlUtil.xmlToMap(xml);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CrmebException("xmlToMap错误，xml = " + xml);
        }
        if (null == map) {
            throw new CrmebException("微信无信息返回，微信申请退款失败！");
        }

        WxRefundResponseVo responseVo = CrmebUtil.mapToObj(map, WxRefundResponseVo.class);
        if (responseVo.getReturnCode().equalsIgnoreCase("FAIL")) {
            wxPayExceptionDispose(map, "微信申请退款异常1");
            throw new CrmebException("微信申请退款失败1！" + responseVo.getReturnMsg());
        }

        if (responseVo.getResultCode().equalsIgnoreCase("FAIL")) {
            wxPayExceptionDispose(map, "微信申请退款业务异常");
            throw new CrmebException("微信申请退款失败2！" + responseVo.getErrCodeDes());
        }
        System.out.println("================微信申请退款结束========================="+responseVo);
        return responseVo;
    }
    /**
     * 微信支付异常处理
     *
     * @param map    微信返回数据
     * @param remark 备注
     */
    private void wxPayExceptionDispose(HashMap<String, Object> map, String remark) {
        WechatExceptions wechatExceptions = new WechatExceptions();
        String returnCode = (String) map.get("return_code");
        if (returnCode.equalsIgnoreCase("FAIL")) {
            wechatExceptions.setErrcode("-100");
            wechatExceptions.setErrmsg(map.get("return_msg").toString());
        } else {
            wechatExceptions.setErrcode(map.get("err_code").toString());
            wechatExceptions.setErrmsg(map.get("err_code_des").toString());
        }
        wechatExceptions.setData(JSONObject.toJSONString(map));
        wechatExceptions.setRemark(remark);
        wechatExceptions.setCreateTime(DateUtil.date());
        wechatExceptions.setUpdateTime(DateUtil.date());
        wechatExceptionsService.save(wechatExceptions);
    }

}

