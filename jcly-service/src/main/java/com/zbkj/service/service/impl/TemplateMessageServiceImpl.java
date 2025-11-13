package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.constants.WeChatConstants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.system.SystemNotification;
import com.zbkj.common.model.template.TemplateMessage;
import com.zbkj.common.result.CommonResultCode;
import com.zbkj.service.dao.TemplateMessageDao;
import com.zbkj.service.service.SystemNotificationService;
import com.zbkj.service.service.TemplateMessageService;
import com.zbkj.service.service.WechatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * TemplateMessageServiceImpl 接口实现
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
public class TemplateMessageServiceImpl extends ServiceImpl<TemplateMessageDao, TemplateMessage> implements TemplateMessageService {

    private static final Logger logger = LoggerFactory.getLogger(TemplateMessageServiceImpl.class);

    @Resource
    private TemplateMessageDao dao;

    @Autowired
    private WechatService wechatService;

    @Autowired
    private SystemNotificationService systemNotificationService;

    @Autowired
    private com.zbkj.common.utils.RestTemplateUtil restTemplateUtil;

    /**
     * 发送模板消息
     *
     * @param templateId 模板消息编号
     * @param temMap     内容Map
     * @param openId     微信用户openid
     */
    @Override
    public void pushTemplateMessage(Integer templateId, HashMap<String, String> temMap, String openId) {
        TemplateMessage templateMessage = getById(templateId);
        if (ObjectUtil.isNull(templateMessage) || StrUtil.isBlank(templateMessage.getContent())) {
            return;
        }
        wechatService.sendPublicTemplateMessage(templateMessage.getTempId(), openId, temMap);
    }

    /**
     * 发送小程序订阅消息
     *
     * @param templateId 模板消息编号
     * @param temMap     内容Map
     * @param openId     微信用户openId
     */
    @Override
    public void pushMiniTemplateMessage(Integer templateId, HashMap<String, String> temMap, String openId) {
        TemplateMessage templateMessage = getById(templateId);
        if (ObjectUtil.isNull(templateMessage) || StrUtil.isBlank(templateMessage.getContent())) {
            return;
        }
        wechatService.sendMiniSubscribeMessage(templateMessage.getTempId(), openId, temMap);
    }

    /**
     * 公众号模板消息同步
     *
     * @return Boolean
     */
    @Override
    public Boolean whcbqhnSync() {
        List<SystemNotification> notificationList = systemNotificationService.getListByWechat("public");
        List<Integer> wechatIdList = notificationList.stream().map(SystemNotification::getWechatId).collect(Collectors.toList());
        List<TemplateMessage> templateMessageList = getListByIdList(wechatIdList);
        if (CollUtil.isEmpty(templateMessageList)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "请先添加公众号模板消息");
        }

        // TODO
        return true;
    }

    /**
     * 小程序订阅消息同步
     *
     * @return Boolean
     */
    @Override
    public Boolean routineSync() {
        try {
            // 1. 获取微信小程序AccessToken
            String miniAccessToken =  wechatService.getWxMiniAccessToken(false);
            if (StrUtil.isBlank(miniAccessToken)) {
                throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "获取小程序AccessToken失败");
            }

            // 2. 调用微信API获取小程序订阅消息模板列表
            String url = StrUtil.format(WeChatConstants.WECHAT_MINI_GET_ALL_PRIVATE_TEMPLATE_URL, miniAccessToken);
            String response = restTemplateUtil.postStringData(url, "{}");
            
            if (StrUtil.isBlank(response)) {
                throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "微信接口返回数据为空");
            }

            JSONObject responseJson = JSONObject.parseObject(response);
            
            // 3. 检查微信接口返回结果
            if (responseJson.containsKey("errcode") && !responseJson.getString("errcode").equals("0")) {
                String errorMsg = responseJson.getString("errmsg");
                logger.error("微信小程序获取订阅消息模板失败: errcode={}, errmsg={}", 
                    responseJson.getString("errcode"), errorMsg);
                throw new CrmebException(CommonResultCode.VALIDATE_FAILED, 
                    "微信接口调用失败: " + errorMsg);
            }

            // 4. 解析微信返回的模板数据
            if (!responseJson.containsKey("data")) {
                logger.info("微信小程序暂无订阅消息模板");
                return true;
            }

            JSONArray templateArray = responseJson.getJSONArray("data");
            if (CollUtil.isEmpty(templateArray)) {
                logger.info("微信小程序订阅消息模板列表为空");
                return true;
            }

            // 5. 获取本地已配置的通知设置
            List<SystemNotification> notificationList = systemNotificationService.getListByWechat(WeChatConstants.WECHAT_MINI_APPID);
            List<Integer> routineIdList = notificationList.stream()
                .map(SystemNotification::getRoutineId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            // 6. 获取本地已存在的模板消息
            List<TemplateMessage> existingTemplates = CollUtil.isEmpty(routineIdList) ? 
                new ArrayList<>() : getListByIdList(routineIdList);
            
            // 创建模板ID到模板对象的映射，便于查找
            Map<String, TemplateMessage> existingTemplateMap = existingTemplates.stream()
                .collect(Collectors.toMap(TemplateMessage::getTempId, t -> t, (existing, replacement) -> existing));

            // 7. 同步微信模板到本地数据库
            int syncCount = 0;
            for (int i = 0; i < templateArray.size(); i++) {
                JSONObject wechatTemplate = templateArray.getJSONObject(i);
                String priTmplId = wechatTemplate.getString("priTmplId"); // 模板ID
                String title = wechatTemplate.getString("title"); // 模板标题
                String content = wechatTemplate.getString("content"); // 模板内容
                String example = wechatTemplate.getString("example"); // 模板示例
                Integer type = wechatTemplate.getInteger("type"); // 模板类型

                if (StrUtil.isBlank(priTmplId)) {
                    continue;
                }

                TemplateMessage templateMessage;
                if (existingTemplateMap.containsKey(priTmplId)) {
                    // 更新已存在的模板
                    templateMessage = existingTemplateMap.get(priTmplId);
                    templateMessage.setName(title);
                    templateMessage.setContent(StrUtil.isNotBlank(content) ? content : example);
                    templateMessage.setUpdateTime(new Date());
                    updateById(templateMessage);
                    logger.info("更新小程序订阅消息模板: {}", title);
                } else {
                    // 创建新的模板记录
                    templateMessage = new TemplateMessage();
                    templateMessage.setType(false); // 0=订阅消息
                    templateMessage.setTempKey(priTmplId);
                    templateMessage.setName(title);
                    templateMessage.setContent(StrUtil.isNotBlank(content) ? content : example);
                    templateMessage.setTempId(priTmplId);
                    templateMessage.setStatus(1); // 启用状态
                    templateMessage.setCreateTime(new Date());
                    templateMessage.setUpdateTime(new Date());
                    save(templateMessage);
                    logger.info("新增小程序订阅消息模板: {}", title);
                }
                syncCount++;
            }

            logger.info("小程序订阅消息同步完成，共同步{}个模板", syncCount);
            return true;

        } catch (Exception e) {
            logger.error("小程序订阅消息同步失败", e);
            if (e instanceof CrmebException) {
                throw e;
            }
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "同步失败: " + e.getMessage());
        }
    }

    /**
     * 通过模板编号获取列表
     *
     * @param idList 模板编号列表
     * @return List
     */
    private List<TemplateMessage> getListByIdList(List<Integer> idList) {
        LambdaQueryWrapper<TemplateMessage> lqw = Wrappers.lambdaQuery();
        lqw.in(TemplateMessage::getId, idList);
        return dao.selectList(lqw);
    }

    /**
     * 查询单条数据
     *
     * @param id Integer id
     */
    @Override
    public TemplateMessage infoException(Integer id) {
        TemplateMessage message = getById(id);
        if (ObjectUtil.isNull(message)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "模板不存在");
        }
        return message;
    }

    /**
     * 获取模板列表
     *
     * @param tidList id数组
     * @return List
     */
    @Override
    public List<TemplateMessage> getByIdList(List<Integer> tidList) {
        LambdaQueryWrapper<TemplateMessage> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(TemplateMessage::getId, tidList);
        return dao.selectList(lambdaQueryWrapper);
    }
}