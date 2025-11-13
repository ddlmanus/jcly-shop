package com.zbkj.service.service;

import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import com.alibaba.fastjson.JSONObject;
import com.zbkj.common.request.SaveConfigRequest;
import com.zbkj.common.response.WeChatJsSdkConfigResponse;
import com.zbkj.common.response.WechatOpenUploadResponse;
import com.zbkj.common.response.WechatPublicShareResponse;
import com.zbkj.common.response.WxRefundVo;
import com.zbkj.common.vo.CommonSeparateConfigVo;
import com.zbkj.common.vo.WeChatMiniAuthorizeVo;
import com.zbkj.common.vo.WeChatOauthToken;
import com.zbkj.common.vo.WxRefundResponseVo;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;

/**
 * 微信公用服务
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
public interface WechatService {

    /**
     * 获取开放平台access_token
     * 通过 code 获取
     * 公众号使用
     *
     * @return 开放平台accessToken对象
     */
    WeChatOauthToken getOauth2AccessToken(String code);

    /**
     * 小程序登录凭证校验
     *
     * @return 小程序登录校验对象
     */
    WeChatMiniAuthorizeVo miniAuthCode(String code);

    /**
     * 获取微信公众号js配置参数
     *
     * @return WeChatJsSdkConfigResponse
     */
    WeChatJsSdkConfigResponse getPublicJsConfig(String url);

    /**
     * 生成小程序码
     *
     * @param jsonObject
     * @return 小程序码
     */
    String createQrCode(JSONObject jsonObject);

    /**
     * 微信公众号发送模板消息
     *
     * @param templateId 模板ID
     * @param openId 微信小程序用户openid
     * @param dataMap 数据Map
     * @return 是否发送成功
     */
    Boolean sendPublicTemplateMessage(String templateId, String openId, HashMap<String, String> dataMap);

    /**
     * 微信小程序发送订阅消息
     *
     * @param templateId 模板ID
     * @param openId 微信小程序用户openid
     * @param dataMap 数据Map
     * @return 是否发送成功
     */
    Boolean sendMiniSubscribeMessage(String templateId, String openId, HashMap<String, String> dataMap);

    /**
     * 获取微信公众号自定义菜单配置
     * （使用本自定义菜单查询接口可以获取默认菜单和全部个性化菜单信息）
     *
     * @return 公众号自定义菜单
     */
    JSONObject getPublicCustomMenu();

    /**
     * 保存微信自定义菜单
     *
     * @param data 菜单数据，具体json格式参考微信开放平台
     * @return 创建结果
     */
    Boolean createPublicCustomMenu(String data);

    /**
     * 删除微信自定义菜单
     *
     * @return 删除结果
     */
    Boolean deletePublicCustomMenu();

    /**
     * 微信开放平台上传素材
     *
     * @param file 文件
     * @param type 类型 图片（image）、语音（voice)
     * @return WechatOpenUploadResponse
     */
    WechatOpenUploadResponse openMediaUpload(MultipartFile file, String type);

    /**
     * 微信公众号分享配置
     */
    WechatPublicShareResponse getPublicShare();

    /**
     * 获取微信小程序发货开关
     */
    CommonSeparateConfigVo getShippingSwitch();

    /**
     * 更新微信小程序发货开关
     */
    Boolean updateShippingSwitch(SaveConfigRequest request);

    /**
     * 生成小程序url链接
     * @param path 通过 URL Link 进入的小程序页面路径，必须是已经发布的小程序存在的页面，不可携带 query 。path 为空时会跳转小程序主页
     * @param query 通过 URL Link 进入小程序时的query，最大1024个字符，只支持数字，大小写英文以及部分特殊字符：!#$&'()*+,/:;=?@-._~%
     * @param expireInterval 到期失效的URL Link的失效间隔天数。生成的到期失效URL Link在该间隔时间到达前有效。最长间隔天数为30天。expire_type 为 1 必填
     * @param envVersion 默认值"release"。要打开的小程序版本。正式版为 "release"，体验版为"trial"，开发版为"develop"，仅在微信外打开时生效
     * @return 生成的小程序 URL Link
     */
    String generateMiniUrlLink(String path, String query, Integer expireInterval, String envVersion);

    /**
     * 获取微信小程序用户手机号
     *
     * @param code 小程序code
     */
    WxMaPhoneNumberInfo getMiniUserPhoneNumber(String code);

    /**
     * 获取开放平台access_token
     * 通过 code 获取
     * 开放平台-网站应用使用
     *
     * @return 开放平台accessToken对象
     */
    WeChatOauthToken getOpenOauth2AccessToken(String code);

    /**
     * 获取开放平台Appid
     * @return Appid
     */
    String getOpenAppid();

    /**
     * 微信公众号生成带参数的临时二维码
     * @param sceneId 场景标识ID
     * @param expireSeconds 二维码有效时间，以秒为单位。 最大不超过2592000（即30天），此字段如果不填，则默认有效期为60秒。
     */
    WxMpQrCodeTicket createPublicQrcodeTmpTicket(Integer sceneId, Integer expireSeconds);

    /**
     * 微信公众号生成带参数的临时二维码
     * @param sceneStr 场景标识字符
     * @param expireSeconds 二维码有效时间，以秒为单位。 最大不超过2592000（即30天），此字段如果不填，则默认有效期为60秒。
     */
    WxMpQrCodeTicket createPublicQrcodeTmpTicket(String sceneStr, Integer expireSeconds);

    /**
     * 获取微信小程序AccessToken
     * @return AccessToken
     */
    String getWxMiniAccessToken(Boolean flase);
    /**
     * 微信申请退款
     *
     * @param wxRefundVo 微信申请退款对象
     * @param path       商户p12证书绝对路径
     * @return 申请退款结果对象
     */
    WxRefundResponseVo payRefund(WxRefundVo wxRefundVo, String path);
}
