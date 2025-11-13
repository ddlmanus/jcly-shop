package com.zbkj.admin.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.zbkj.admin.service.WechatMessageService;
import com.zbkj.admin.vo.MessageReplyDataVo;
import com.zbkj.common.constants.WeChatConstants;
import com.zbkj.common.model.article.Article;
import com.zbkj.common.model.wechat.WechatReply;
import com.zbkj.common.utils.RedisUtil;
import com.zbkj.service.service.ArticleService;
import com.zbkj.service.service.SystemAttachmentService;
import com.zbkj.service.service.UserTokenService;
import com.zbkj.service.service.WechatReplyService;
import lombok.Data;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutNewsMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;


/**
 * 微信公众号消息 服务实现类
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
@Data
@Service
public class WeChatMessageServiceImpl implements WechatMessageService {
    private static final Logger logger = LoggerFactory.getLogger(WeChatMessageServiceImpl.class);

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private WechatReplyService wechatReplyService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserTokenService userTokenService;

    @Autowired
    private SystemAttachmentService systemAttachmentService;

    /**
     * 消息事件处理
     * @param request HttpServletRequest request请求
     * @return  String
     */
    @Override
    public String messageEvent(HttpServletRequest request) {
        // 解析微信消息
        WxMpXmlMessage inMessage;
        try {
            inMessage = WxMpXmlMessage.fromXml(request.getInputStream());
        } catch (Exception e) {
            logger.error("解析微信公众号消息异常");
            logger.error(e.getMessage(), e);
            return "";
        }
        WechatReply wechatReply = getReplyByContent(inMessage.getMsgType(), inMessage.getEvent(), inMessage.getEventKey(), inMessage.getContent());
        logger.info("公众号消息推送——inMessage={}", JSONObject.toJSONString(inMessage));
        if (inMessage.getMsgType().equals("event") && inMessage.getEvent().equals("SCAN") && "pcLogin".equals(inMessage.getEventKey())) {
            logger.info("进入了公众号pc扫码消息推送处理");
            logger.info("进入了公众号pc扫码消息推送,用户信息FromUserName={}", inMessage.getFriendUserName());
            String content = "欢迎登录CRMEB，请确保本人操作确认。点击<a href=\"weixin://bizmsgmenu?msgmenucontent=确认登录&msgmenuid=10000\">确认登录</a>";
            redisUtil.set("pc_wechat_public_ticket_" + inMessage.getTicket(), inMessage.getFromUser(), 60*30L);
            redisUtil.set("pc_wechat_public_user_openid_" + inMessage.getFromUser(), 0, 60*30L);
            // 处理文本消息
            return WxMpXmlOutMessage.TEXT()
                    .content(content)
                    .fromUser(inMessage.getToUser())
                    .toUser(inMessage.getFromUser())
                    .build()
                    .toXml();
        }
        if (StrUtil.isNotBlank(inMessage.getBizMsgMenuId()) && inMessage.getBizMsgMenuId().equals("10000")) {
            // 标识用户pc扫码确认登录
            String fromUser = inMessage.getFromUser();
            redisUtil.set("pc_wechat_public_user_openid_" + fromUser, 1, 60*30L);
            return WxMpXmlOutMessage.TEXT()
                    .content("登录成功")
                    .fromUser(inMessage.getToUser())
                    .toUser(inMessage.getFromUser())
                    .build()
                    .toXml();
        }


        MessageReplyDataVo messageReplyDataVo = JSONObject.toJavaObject(JSONObject.parseObject(wechatReply.getData()), MessageReplyDataVo.class);
        switch (wechatReply.getType().toLowerCase()) {
            case WeChatConstants.WE_CHAT_MESSAGE_RESP_MESSAGE_TYPE_TEXT:
                // 处理文本消息
                return WxMpXmlOutMessage.TEXT()
                        .content(messageReplyDataVo.getContent())
                        .fromUser(inMessage.getToUser())
                        .toUser(inMessage.getFromUser())
                        .build()
                        .toXml();
            case WeChatConstants.WE_CHAT_MESSAGE_RESP_MESSAGE_TYPE_IMAGE:
                // 处理图片消息
                return WxMpXmlOutMessage.IMAGE()
                        .mediaId(messageReplyDataVo.getMediaId())
                        .fromUser(inMessage.getToUser())
                        .toUser(inMessage.getFromUser())
                        .build()
                        .toXml();

            case WeChatConstants.WE_CHAT_MESSAGE_RESP_MESSAGE_TYPE_VOICE:
                // 处理图片消息
                return WxMpXmlOutMessage.VOICE()
                        .mediaId(messageReplyDataVo.getMediaId())
                        .fromUser(inMessage.getToUser())
                        .toUser(inMessage.getFromUser())
                        .build()
                        .toXml();
            case WeChatConstants.WE_CHAT_MESSAGE_RESP_MESSAGE_TYPE_NEWS:
                // 处理图文消息
                Article article = articleService.getById(messageReplyDataVo.getArticleId());
                if (article == null || article.getStatus()) {
                    return "";
                }
                WxMpXmlOutNewsMessage.Item item = new WxMpXmlOutNewsMessage.Item();
                item.setTitle(article.getTitle());
                item.setDescription(article.getSynopsis());
                item.setPicUrl(article.getCover());
                return WxMpXmlOutNewsMessage.NEWS()
                        .addArticle(item)
                        .fromUser(inMessage.getToUser())
                        .toUser(inMessage.getFromUser())
                        .build()
                        .toXml();
            default:
                return "";
        }
    }

    /**
     * 处理不同的消息类型
     */
    private WechatReply getReplyByContent(String msgType, String event, String eventKey, String content) {
        WechatReply wp = new WechatReply();
        switch (msgType) {
            case WeChatConstants.WE_CHAT_MESSAGE_REQ_MESSAGE_TYPE_EVENT:
                if (StringUtils.isBlank(event)) {
                    break;
                }
                switch (event.toLowerCase()) {
                    case WeChatConstants.WE_CHAT_MESSAGE_EVENT_TYPE_UNSUBSCRIBE:
                    case WeChatConstants.WE_CHAT_MESSAGE_EVENT_TYPE_SCAN:
                    case WeChatConstants.WE_CHAT_MESSAGE_EVENT_TYPE_LOCATION:
                    case WeChatConstants.WE_CHAT_MESSAGE_EVENT_TYPE_VIEW:
                        //暂时不处理
                        break;
                    case WeChatConstants.WE_CHAT_MESSAGE_EVENT_TYPE_SUBSCRIBE:
                        wp = wechatReplyService.getVoByKeywords(WeChatConstants.WE_CHAT_MESSAGE_EVENT_TYPE_SUBSCRIBE.toLowerCase(), true);
                        break;
                    case WeChatConstants.WE_CHAT_MESSAGE_EVENT_TYPE_CLICK:
                        wp = wechatReplyService.getVoByKeywords(eventKey, true);
                        break;
                    default:
                        break;
                }
                break;
            case WeChatConstants.WE_CHAT_MESSAGE_REQ_MESSAGE_TYPE_TEXT:
                wp = wechatReplyService.getVoByKeywords(content, true);
                break;
            case WeChatConstants.WE_CHAT_MESSAGE_REQ_MESSAGE_TYPE_IMAGE:
            case WeChatConstants.WE_CHAT_MESSAGE_REQ_MESSAGE_TYPE_VOICE:
            case WeChatConstants.WE_CHAT_MESSAGE_REQ_MESSAGE_TYPE_VIDEO:
            case WeChatConstants.WE_CHAT_MESSAGE_REQ_MESSAGE_TYPE_LOCATION:
            case WeChatConstants.WE_CHAT_MESSAGE_REQ_MESSAGE_TYPE_LINK:
                break;
            default:
                break;
        }
        if (ObjectUtil.isNull(wp)) {
            //无效关键字回复
            wp = wechatReplyService.getVoByKeywords(WeChatConstants.WE_CHAT_MESSAGE_DEFAULT_CONTENT_KEY);
        }
        return wp;
    }
}
