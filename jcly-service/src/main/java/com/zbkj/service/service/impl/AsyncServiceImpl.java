package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.zbkj.common.config.CrmebConfig;
import com.zbkj.common.constants.*;
import com.zbkj.common.enums.GroupBuyRecordEnum;
import com.zbkj.common.model.express.ShippingTemplates;
import com.zbkj.common.model.invoice.Invoice;
import com.zbkj.common.model.merchant.Merchant;
import com.zbkj.common.model.merchant.MerchantBalanceRecord;
import com.zbkj.common.model.merchant.MerchantProductCategory;
import com.zbkj.common.model.merchant.MerchantTodoItem;
import com.zbkj.common.model.order.MerchantOrder;
import com.zbkj.common.model.order.Order;
import com.zbkj.common.model.order.OrderDetail;
import com.zbkj.common.model.order.RechargeOrder;
import com.zbkj.common.model.product.ProductBrand;
import com.zbkj.common.model.product.ProductCategory;
import com.zbkj.common.model.record.BrowseRecord;
import com.zbkj.common.model.record.UserVisitRecord;
import com.zbkj.common.model.system.SystemAttachment;
import com.zbkj.common.model.system.SystemNotification;
import com.zbkj.common.model.system.SystemUserLevel;
import com.zbkj.common.model.user.*;
import com.zbkj.common.model.wechat.WechatPayInfo;
import com.zbkj.common.request.ProductAddRequest;
import com.zbkj.common.request.ProductAttrAddRequest;
import com.zbkj.common.request.ProductAttrOptionAddRequest;
import com.zbkj.common.request.ProductAttrValueAddRequest;
import com.zbkj.common.response.JustuitanOrderUploadResult;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.common.utils.RedisUtil;
import com.zbkj.common.utils.UploadUtil;
import com.zbkj.common.vo.*;
import com.zbkj.service.service.*;
import com.zbkj.service.service.groupbuy.GroupBuyUserService;
import com.zbkj.common.exception.CrmebException;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.ClientAnchor;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 异步调用服务实现类
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
public class AsyncServiceImpl implements AsyncService {

    private final Logger logger = LoggerFactory.getLogger(AsyncServiceImpl.class);

    @Lazy
    @Autowired
    private ProductService storeProductService;
    @Autowired
    private RedisUtil redisUtil;
    @Lazy
    @Autowired
    private UserVisitRecordService userVisitRecordService;
    @Lazy
    @Autowired
    private BrowseRecordService browseRecordService;
    @Lazy
    @Autowired
    private OrderService orderService;
    @Lazy
    @Autowired
    private MerchantOrderService merchantOrderService;
    @Lazy
    @Autowired
    private OrderDetailService orderDetailService;
    @Lazy
    @Autowired
    private OrderStatusService orderStatusService;
    @Lazy
    @Autowired
    private UserService userService;
    @Lazy
    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private CrmebConfig crmebConfig;
    @Lazy
    @Autowired
    private SystemNotificationService systemNotificationService;
    @Lazy
    @Autowired
    private UserTokenService userTokenService;
    @Lazy
    @Autowired
    private TemplateMessageService templateMessageService;
    @Lazy
    @Autowired
    private UserExperienceRecordService userExperienceRecordService;
    @Lazy
    @Autowired
    private SystemUserLevelService systemUserLevelService;
    @Lazy
    @Autowired
    private UserLevelService userLevelService;
    @Lazy
    @Autowired
    private CommunityNotesService communityNotesService;
    @Lazy
    @Autowired
    private CommunityReplyService communityReplyService;
    @Lazy
    @Autowired
    private UserIntegralRecordService userIntegralRecordService;
    @Lazy
    @Autowired
    private MerchantTodoItemService merchantTodoItemService;
    @Lazy
    @Autowired
    private UserBrokerageRecordService userBrokerageRecordService;
    @Lazy
    @Autowired
    private MerchantBalanceRecordService merchantBalanceRecordService;
    @Lazy
    @Autowired
    private MerchantService merchantService;
    @Lazy
    @Autowired
    private WechatOrderShippingService wechatOrderShippingService;
    @Lazy
    @Autowired
    private PaidMemberService paidMemberService;
    @Lazy
    @Autowired
    private GroupBuyUserService groupBuyUserService;
    @Lazy
    @Autowired
    private WechatPayInfoService wechatPayInfoService;
    @Lazy
    @Autowired
    private MerchantProfitSharingDetailService profitSharingDetailService;
    @Autowired
    private JustuitanErpService justuitanErpService;
    @Autowired
    private InvoiceService invoiceService;
    @Lazy
    @Autowired
    private ProductService productService;
    @Lazy
    @Autowired
    private UploadService uploadService;
    @Lazy
    @Autowired
    private QiNiuService qiNiuService;
    @Lazy
    @Autowired
    private OssService ossService;
    @Lazy
    @Autowired
    private CosService cosService;
    @Lazy
    @Autowired
    private JdCloudService jdCloudService;

    @Lazy
    @Autowired
    private MerchantProductCategoryService merchantProductCategoryService;
    @Lazy
    @Autowired
    private ProductCategoryService productCategoryService;
    @Lazy
    @Autowired
    private SystemAttachmentService systemAttachmentService;
    @Lazy
    @Autowired
    private ShippingTemplatesService shippingTemplatesService;
    @Lazy
    @Autowired
    private ProductBrandService productBrandService;
    /**
     * 商品详情统计
     *
     * @param proId 商品id
     * @param uid   用户uid
     */
    @Async
    @Override
    public void productDetailStatistics(Integer proId, Integer uid) {
        // 商品浏览量+1
        storeProductService.addBrowse(proId);
        // 商品浏览量统计(每日/商城)
        String dateStr = DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE);
        redisUtil.incrAndCreate(RedisConstants.PRO_PAGE_VIEW_KEY + dateStr);
        // 商品浏览量统计(每日/个体)
        redisUtil.incrAndCreate(StrUtil.format(RedisConstants.PRO_PRO_PAGE_VIEW_KEY, dateStr, proId));
        if (uid.equals(0)) {
            return;
        }
        // 保存用户访问记录
        if (uid > 0) {
            UserVisitRecord visitRecord = new UserVisitRecord();
            visitRecord.setDate(DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE));
            visitRecord.setUid(uid);
            visitRecord.setVisitType(VisitRecordConstants.VISIT_TYPE_DETAIL);
            userVisitRecordService.save(visitRecord);

            BrowseRecord browseRecord = browseRecordService.getByUidAndProId(uid, proId);
            if (ObjectUtil.isNull(browseRecord)) {
                browseRecord = new BrowseRecord();
                browseRecord.setUid(uid);
                browseRecord.setProductId(proId);
                browseRecord.setDate(DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE));
                browseRecord.setCreateTime(DateUtil.date());
                browseRecordService.save(browseRecord);
            } else {
                browseRecord.setDate(DateUtil.date().toString(DateConstants.DATE_FORMAT_DATE));
                browseRecordService.myUpdate(browseRecord);
            }
        }

    }

    /**
     * 保存用户访问记录
     *
     * @param userId    用户id
     * @param visitType 访问类型
     */
    @Async
    @Override
    public void saveUserVisit(Integer userId, Integer visitType) {
        UserVisitRecord visitRecord = new UserVisitRecord();
        visitRecord.setDate(DateUtil.date().toDateStr());
        visitRecord.setUid(userId);
        visitRecord.setVisitType(visitType);
        userVisitRecordService.save(visitRecord);
    }

    /**
     * 订单支付成功拆单处理
     *
     * @param orderNo 订单号
     */
    @Async
    @Override
    public void orderPaySuccessSplit(String orderNo) {
        Order order = orderService.getByOrderNo(orderNo);
        if (ObjectUtil.isNull(order)) {
            logger.error("异步——订单支付成功拆单处理 | 订单不存在，orderNo: {}", orderNo);
            return;
        }

        List<MerchantOrder> merchantOrderList = merchantOrderService.getByOrderNo(orderNo);
        if (CollUtil.isEmpty(merchantOrderList)) {
            logger.error("异步——订单支付成功拆单处理 | 商户订单信息不存在,orderNo: {}", orderNo);
            return;
        }
        Boolean execute;
        if (merchantOrderList.size() == 1) {
            // 单商户订单
            execute = oneMerchantOrderProcessing(order, merchantOrderList.get(0));
        } else {
            execute = manyMerchantOrderProcessing(order, merchantOrderList);
        }
        if (!execute) {
            logger.error("异步——订单支付成功拆单处理 | 拆单处理失败，orderNo: {}", orderNo);
            return;
        }
        // 添加支付成功redis队列
        // 异步——订单支付成功拆单处理 | 拆单成功，加入后置处理队列
        redisUtil.lPush(TaskConstants.ORDER_TASK_PAY_SUCCESS_AFTER, order.getOrderNo());
    }

    /**
     * 访问用户个人中心记录
     *
     * @param uid 用户id
     */
    @Async
    @Override
    public void visitUserCenter(Integer uid) {
        UserVisitRecord visitRecord = new UserVisitRecord();
        visitRecord.setDate(DateUtil.date().toString("yyyy-MM-dd"));
        visitRecord.setUid(uid);
        visitRecord.setVisitType(VisitRecordConstants.VISIT_TYPE_CENTER);
        userVisitRecordService.save(visitRecord);
    }

    /**
     * 安装统计
     */
    @Async
    @Override
    public void installStatistics() {
        String isInstall = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_INSTALL_STATISTICS);
        if (StrUtil.isNotBlank(isInstall) && isInstall.equals("1")) {
            return;
        }
        String version = crmebConfig.getVersion();
        if (StrUtil.isBlank(version) || !(StrUtil.startWithIgnoreCase(version, "CRMEB"))) {
            return;
        }
        String apiUrl = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_API_URL);
        if (StrUtil.isBlank(apiUrl) || !(StrUtil.startWithIgnoreCase(apiUrl, "http"))) {
            return;
        }
        Map<String, String> map = new HashMap<>();
        map.put("host", apiUrl);
        map.put("version", version);
        map.put("https", "https");
        String result = HttpUtil.post("https://shop.crmeb.net/index.php/admin/server.upgrade_api/updatewebinfo", JSONObject.toJSONString(map));
        JSONObject jsonObject = JSONObject.parseObject(result);
        if (jsonObject.getInteger("status").equals(200)) {
            systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_INSTALL_STATISTICS, "1");
        }
    }

    /**
     * 发送充值成功通知
     *
     * @param rechargeOrder 充值订单
     * @param user          用户
     */
    @Async
    @Override
    public void sendRechargeSuccessNotification(RechargeOrder rechargeOrder, User user) {
        if (!rechargeOrder.getPayType().equals(PayConstants.PAY_TYPE_WE_CHAT)) {
            return;
        }
        SystemNotification payNotification = systemNotificationService.getByMark(NotifyConstants.RECHARGE_SUCCESS_MARK);
        UserToken userToken;
        HashMap<String, String> temMap = new HashMap<>();
        if (rechargeOrder.getPayChannel().equals(PayConstants.PAY_CHANNEL_WECHAT_PUBLIC) && user.getIsWechatPublic() && payNotification.getIsWechat().equals(1)) {
            // 公众号模板消息
            userToken = userTokenService.getTokenByUserId(user.getId(), UserConstants.USER_TOKEN_TYPE_WECHAT);
            if (ObjectUtil.isNull(userToken)) {
                return;
            }
            /**
             * {{first.DATA}}
             * 客户名称：{{keyword1.DATA}}
             * 充值单号：{{keyword2.DATA}}
             * 充值金额：{{keyword3.DATA}}
             * {{remark.DATA}}
             */
            temMap.put(Constants.WE_CHAT_TEMP_KEY_FIRST, "充值成功通知！");
            temMap.put("keyword1", user.getNickname());
            temMap.put("keyword2", rechargeOrder.getOrderNo());
            temMap.put("keyword3", rechargeOrder.getPrice().toString());
            temMap.put(Constants.WE_CHAT_TEMP_KEY_END, "欢迎下次再来！");
            templateMessageService.pushTemplateMessage(payNotification.getWechatId(), temMap, userToken.getToken());
            return;
        }
        // 小程序通知
        if (rechargeOrder.getPayChannel().equals(PayConstants.PAY_CHANNEL_WECHAT_MINI) && user.getIsWechatPublic() && payNotification.getIsRoutine().equals(1)) {
            // 公众号模板消息
            userToken = userTokenService.getTokenByUserId(user.getId(), UserConstants.USER_TOKEN_TYPE_ROUTINE);
            if (ObjectUtil.isNull(userToken)) {
                return;
            }
            /**
             * 交易单号
             * {{character_string1.DATA}}
             * 充值金额
             * {{amount3.DATA}}
             * 充值时间
             * {{date5.DATA}}
             * 赠送金额
             * {{amount6.DATA}}
             * 备注
             * {{thing7.DATA}}
             */
            temMap.put("character_string1", rechargeOrder.getOrderNo());
            temMap.put("amount3", rechargeOrder.getPrice().toString());
            temMap.put("date5", rechargeOrder.getPayTime().toString());
            temMap.put("amount6", rechargeOrder.getGivePrice().toString());
            temMap.put("thing7", "您的充值已成功！");
            templateMessageService.pushTemplateMessage(payNotification.getRoutineId(), temMap, userToken.getToken());
            return;
        }
        return;
    }

    /**
     * 社区笔记用户添加经验
     *
     * @param userId 用户ID
     * @param noteId 文章ID
     */
    @Async
    @Override
    public void noteUpExp(Integer userId, Integer noteId) {

        String levelSwitch = systemConfigService.getValueByKey(UserLevelConstants.SYSTEM_USER_LEVEL_SWITCH);
        if (!Constants.COMMON_SWITCH_OPEN.equals(levelSwitch)) {
            // 开启会员后，才进行经验添加
            return;
        }
        String noteExpStr = systemConfigService.getValueByKeyException(UserLevelConstants.SYSTEM_USER_LEVEL_COMMUNITY_NOTES_EXP);
        if (noteExpStr.equals("0")) {
            return;
        }
        String noteNum = systemConfigService.getValueByKeyException(UserLevelConstants.SYSTEM_USER_LEVEL_COMMUNITY_NOTES_NUM);
        if (noteNum.equals("0")) {
            return;
        }
        if (userExperienceRecordService.isExistNote(userId, noteId)) {
            return;
        }
        Integer noteCountToday = userExperienceRecordService.getCountByNoteToday(userId);
        if (noteCountToday >= Integer.parseInt(noteNum)) {
            return;
        }
        User user = userService.getById(userId);
        int noteExp = Integer.parseInt(noteExpStr);
        if (user.getIsPaidMember()) {
            List<PaidMemberBenefitsVo> benefitsList = paidMemberService.getBenefitsList();
            for (PaidMemberBenefitsVo b : benefitsList) {
                if (b.getStatus()) {
                    if (b.getName().equals("experienceDoubling") && b.getMultiple() > 1 && b.getChannelStr().contains("2")) {
                        noteExp = noteExp * b.getMultiple();
                    }
                }
            }
        }
        UserExperienceRecord record = new UserExperienceRecord();
        record.setUid(userId);
        record.setLinkId(noteId.toString());
        record.setLinkType(ExperienceRecordConstants.EXPERIENCE_RECORD_LINK_TYPE_NOTE);
        record.setType(ExperienceRecordConstants.EXPERIENCE_RECORD_TYPE_ADD);
        record.setTitle(ExperienceRecordConstants.EXPERIENCE_RECORD_TITLE_NOTE);
        record.setExperience(noteExp);
        record.setBalance(user.getExperience() + noteExp);
//        record.setMark(StrUtil.format("社区发布笔记奖励{}经验", noteExp));
        record.setMark(StrUtil.format("社区发布种草奖励{}经验", noteExp));
        record.setCreateTime(DateUtil.date());

        int finalNoteExp = noteExp;
        Boolean execute = transactionTemplate.execute(e -> {
            userService.updateExperience(userId, finalNoteExp, Constants.OPERATION_TYPE_ADD);
            userExperienceRecordService.save(record);
            userLevelUp(userId, user.getLevel(), user.getExperience() + finalNoteExp);
            return Boolean.TRUE;
        });
        if (!execute) {
            logger.error("用户社区发布笔记添加经验失败，userId={},noteId={}", userId, noteId);
        }
    }

    /**
     * 社区笔记点赞与取消
     *
     * @param noteId        笔记ID
     * @param operationType 操作类型：add-点赞，sub-取消
     */
    @Async
    @Override
    public void communityNoteLikeOrClean(Integer noteId, String operationType) {
        communityNotesService.operationLike(noteId, operationType);
    }

    /**
     * 社区笔记评论点赞与取消
     *
     * @param replyId       评论ID
     * @param operationType 操作类型：add-点赞，sub-取消
     */
    @Async
    @Override
    public void communityReplyLikeOrClean(Integer replyId, String operationType) {
        communityReplyService.operationLike(replyId, operationType);
    }

    /**
     * 社区笔记添加评论后置处理
     *
     * @param noteId   笔记ID
     * @param parentId 一级评论ID，0-没有
     * @param replyId  评论ID
     */
    @Async
    @Override
    public void noteAddReplyAfter(Integer noteId, Integer parentId, Integer replyId) {
        communityNotesService.operationReplyNum(noteId, 1, Constants.OPERATION_TYPE_ADD);
        if (parentId > 0) {
            communityReplyService.operationReplyNum(parentId, 1, Constants.OPERATION_TYPE_ADD);
        }
    }

    /**
     * 社区评论删除后置处理
     *
     * @param noteId       笔记ID
     * @param firstReplyId 一级笔记评论ID
     * @param replyId      评论ID
     * @param countReply   评论回复数量
     */
    @Async
    @Override
    public void communityReplyDeleteAfter(Integer noteId, Integer firstReplyId, Integer replyId, Integer countReply) {
        if (firstReplyId > 0) {
            communityNotesService.operationReplyNum(noteId, 1, Constants.OPERATION_TYPE_SUBTRACT);
            communityReplyService.operationReplyNum(firstReplyId, 1, Constants.OPERATION_TYPE_SUBTRACT);
        } else {
            communityNotesService.operationReplyNum(noteId, 1 + countReply, Constants.OPERATION_TYPE_SUBTRACT);
        }
    }

    /**
     * 用户升级
     *
     * @param userId      用户ID
     * @param userLevelId 用户等级
     * @param exp         当前经验
     */
    @Async
    @Override
    public void userLevelUp(Integer userId, Integer userLevelId, Integer exp) {
        String levelSwitch = systemConfigService.getValueByKey(UserLevelConstants.SYSTEM_USER_LEVEL_SWITCH);
        if (levelSwitch.equals(Constants.COMMON_SWITCH_CLOSE)) {
            return;
        }
        SystemUserLevel userLevel = systemUserLevelService.getByLevelId(userLevelId);
        SystemUserLevel systemLevel = systemUserLevelService.getByExp(exp);
        if (userLevel.getGrade() >= systemLevel.getGrade()) {
            return;
        }
        if (systemLevel.getExperience() > exp) {
            systemLevel = systemUserLevelService.getPreviousGrade(systemLevel.getGrade());
        }

        UserLevel level = new UserLevel();
        level.setUid(userId);
        level.setLevelId(systemLevel.getId());
        level.setGrade(systemLevel.getGrade());

        userService.updateUserLevel(userId, level.getLevelId());
        userLevelService.deleteByUserId(userId);
        userLevelService.save(level);
    }

    /**
     * 订单支付成功后冻结处理
     * @param orderNoList 订单编号列表
     */
    @Async
    @Override
    public void orderPayAfterFreezingOperation(List<String> orderNoList) {
        String merchantShareNode = systemConfigService.getValueByKey(SysConfigConstants.MERCHANT_SHARE_NODE);
        if (StrUtil.isNotBlank(merchantShareNode) && "pay".equals(merchantShareNode)) {
            String merchantShareNodeFreezeDayStr = systemConfigService.getValueByKey(SysConfigConstants.MERCHANT_SHARE_FREEZE_TIME);
            if (StrUtil.isNotBlank(merchantShareNodeFreezeDayStr)) {
                orderMerchantShareFreezingOperation(orderNoList, Integer.parseInt(merchantShareNodeFreezeDayStr));
            }
        }
        String retailStoreBrokerageShareNode = systemConfigService.getValueByKey(SysConfigConstants.RETAIL_STORE_BROKERAGE_SHARE_NODE);
        if (StrUtil.isNotBlank(retailStoreBrokerageShareNode) && "pay".equals(retailStoreBrokerageShareNode)) {
            String retailStoreBrokerageShareFreezeDayStr = systemConfigService.getValueByKey(SysConfigConstants.RETAIL_STORE_BROKERAGE_FREEZING_TIME);
            if (StrUtil.isNotBlank(retailStoreBrokerageShareFreezeDayStr)) {
                orderBrokerageShareFreezingOperation(orderNoList, Integer.parseInt(retailStoreBrokerageShareFreezeDayStr));
            }
        }
        String integralFreezeNode = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_STORE_INTEGRAL_FREEZE_NODE);
        if (StrUtil.isNotBlank(integralFreezeNode) && "pay".equals(integralFreezeNode)) {
            String integralDayStr = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_STORE_INTEGRAL_EXTRACT_TIME);
            if (StrUtil.isNotBlank(integralDayStr)) {
                orderIntegralFreezingOperation(orderNoList, Integer.parseInt(integralDayStr));
            }
        }
    }

    /**
     * 订单完成后冻结处理
     * @param orderNoList 订单编号列表
     */
    @Async
    @Override
    public void orderCompleteAfterFreezingOperation(List<String> orderNoList) {
        String merchantShareNode = systemConfigService.getValueByKey(SysConfigConstants.MERCHANT_SHARE_NODE);
        String merchantShareNodeFreezeDayStr = systemConfigService.getValueByKey(SysConfigConstants.MERCHANT_SHARE_FREEZE_TIME);
        int merchantFreezeDay = 7;
        if (StrUtil.isNotBlank(merchantShareNode) && "complete".equals(merchantShareNode) && StrUtil.isNotBlank(merchantShareNodeFreezeDayStr)) {
            merchantFreezeDay = Integer.parseInt(merchantShareNodeFreezeDayStr);
        }
        orderMerchantShareFreezingOperation(orderNoList, merchantFreezeDay);


        String retailStoreBrokerageShareNode = systemConfigService.getValueByKey(SysConfigConstants.RETAIL_STORE_BROKERAGE_SHARE_NODE);
        String retailStoreBrokerageShareFreezeDayStr = systemConfigService.getValueByKey(SysConfigConstants.RETAIL_STORE_BROKERAGE_FREEZING_TIME);
        int brokerageFreezeDay = 7;
        if (StrUtil.isNotBlank(retailStoreBrokerageShareNode) && "complete".equals(retailStoreBrokerageShareNode) && StrUtil.isNotBlank(retailStoreBrokerageShareFreezeDayStr)) {
            brokerageFreezeDay = Integer.parseInt(retailStoreBrokerageShareFreezeDayStr);
        }
        orderBrokerageShareFreezingOperation(orderNoList, brokerageFreezeDay);


        String integralFreezeNode = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_STORE_INTEGRAL_FREEZE_NODE);
        String integralDayStr = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_STORE_INTEGRAL_EXTRACT_TIME);
        int integralFreezeDay = 7;
        if (StrUtil.isNotBlank(integralFreezeNode) && "complete".equals(integralFreezeNode) && StrUtil.isNotBlank(integralDayStr)) {
            integralFreezeDay = Integer.parseInt(integralDayStr);
        }
        orderIntegralFreezingOperation(orderNoList, integralFreezeDay);
        
        // 创建商户分账记录
        orderProfitSharingCreateOperation(orderNoList);
    }

    /**
     * 微信小程序发货上传发货管理
     * @param orderNo 订单编号
     */
    @Async
    @Override
    public void wechatSendUploadShipping(String orderNo) {
        try {
            wechatOrderShippingService.uploadShippingInfo(orderNo);
        } catch (Exception e) {
            logger.error("微信小程序发货上传发货管理异常，e=", e);
        }
    }

    /**
     * 核销订单微信小程序发货上传发货管理
     * @param orderNo 订单编号
     */
    @Async
    @Override
    public void verifyOrderWechatSendUploadShipping(String orderNo) {
        try {
            wechatOrderShippingService.uploadShippingInfo(orderNo, "verify");
        } catch (Exception e) {
            logger.error("微信小程序发货上传发货管理异常，e=", e);
        }
    }

    @Async
    @Override
    public void sendJushuitanOrder(Order merchantOrder) {
      logger.info("开始推送订单到聚水潭");
      JustuitanOrderUploadResult result = justuitanErpService.uploadOrderToJst(merchantOrder);
      if (!result.isSuccess()) {
        logger.error("推送订单到聚水潭失败，失败原因：{}", result.getMessage());
      }
    }

    /**
     * 订单积分冻结处理
     * @param orderNoList 订单编号列表
     * @param freezeDay 积分冻结天数
     */
    private void orderIntegralFreezingOperation(List<String> orderNoList, Integer freezeDay) {
        Order order = orderService.getByOrderNo(orderNoList.get(0));
        List<UserIntegralRecord> recordList = new ArrayList<>();
        if (freezeDay > 0) {
            for (String orderNo : orderNoList) {
                UserIntegralRecord record = userIntegralRecordService.getByOrderNoAndType(orderNo, IntegralRecordConstants.INTEGRAL_RECORD_TYPE_ADD);
                if (ObjectUtil.isNull(record)) {
                    continue;
                }
                if (!record.getStatus().equals(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_CREATE)) {
                    continue;
                }
                // 佣金进入冻结期
                record.setStatus(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_FROZEN);
                // 计算解冻时间
                record.setFrozenTime(freezeDay);
                DateTime dateTime = DateUtil.offsetDay(new Date(), freezeDay);
                long thawTime = dateTime.getTime();
                record.setThawTime(thawTime);
                record.setUpdateTime(DateUtil.date());
                recordList.add(record);
            }
            if (CollUtil.isEmpty(recordList)) {
                return;
            }
            boolean batch = userIntegralRecordService.updateBatchById(recordList);
            if (!batch) {
                logger.error("订单积分冻结处理失败，订单号={}", order.getOrderNo());
            }
            return;
        }
        User user = userService.getById(order.getUid());
        if (ObjectUtil.isNull(user)) {
            logger.error("订单积分冻结处理失败，未找到对应的用户信息，订单编号={}", order.getOrderNo());
            return;
        }
        Integer balance = user.getIntegral();

        for (String orderNo : orderNoList) {
            UserIntegralRecord record = userIntegralRecordService.getByOrderNoAndType(orderNo, IntegralRecordConstants.INTEGRAL_RECORD_TYPE_ADD);
            if (ObjectUtil.isNull(record)) {
                continue;
            }
            if (record.getStatus().equals(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_COMPLETE)) {
                continue;
            }
            // 佣金完结期
            record.setStatus(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_COMPLETE);
            // 计算解冻时间
            long thawTime = DateUtil.current(false);
            record.setFrozenTime(freezeDay);
            record.setThawTime(thawTime);
            // 计算积分余额
            balance = balance + record.getIntegral();
            record.setBalance(balance);
            record.setUpdateTime(DateUtil.date());
            recordList.add(record);
        }
        if (CollUtil.isEmpty(recordList)) {
            return;
        }
        int integral = recordList.stream().mapToInt(UserIntegralRecord::getIntegral).sum();
        Boolean execute = transactionTemplate.execute(e -> {
            userIntegralRecordService.updateBatchById(recordList);
            userService.updateIntegral(user.getId(), integral, Constants.OPERATION_TYPE_ADD);
            return Boolean.TRUE;
        });
        if (!execute) {
            logger.error("订单积分冻结处理：直接解冻失败，订单编号={}", order.getOrderNo());
        }
    }

    /**
     * 订单佣金分账冻结处理
     * @param orderNoList 订单编号列表
     */
    private void orderBrokerageShareFreezingOperation(List<String> orderNoList, Integer freezeDay) {
        Order order = orderService.getByOrderNo(orderNoList.get(0));
        List<UserBrokerageRecord> recordList = new ArrayList<>();
        if (freezeDay > 0) {
            for (String orderNo : orderNoList) {
                List<UserBrokerageRecord> brokerageRecordList = userBrokerageRecordService.findListByLinkNoAndLinkType(orderNo, BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_ORDER);
                if (CollUtil.isEmpty(brokerageRecordList)) {
                    continue;
                }
                for (UserBrokerageRecord record : brokerageRecordList) {
                    if (!record.getStatus().equals(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_CREATE)) {
                        continue;
                    }
                    // 佣金进入冻结期
                    record.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_FROZEN);
                    // 计算解冻时间
                    record.setFrozenTime(freezeDay);
                    DateTime dateTime = DateUtil.offsetDay(new Date(), record.getFrozenTime());
                    long thawTime = dateTime.getTime();
                    record.setThawTime(thawTime);
                    record.setUpdateTime(DateUtil.date());
                    recordList.add(record);
                }
            }
            if (CollUtil.isEmpty(recordList)) {
                return;
            }
            userBrokerageRecordService.updateBatchById(recordList);
            return;
        }

        for (String orderNo : orderNoList) {
            List<UserBrokerageRecord> brokerageRecordList = userBrokerageRecordService.findListByLinkNoAndLinkType(orderNo, BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_ORDER);
            if (CollUtil.isEmpty(brokerageRecordList)) {
                continue;
            }
            for (UserBrokerageRecord record : brokerageRecordList) {
                if (!record.getStatus().equals(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_CREATE)) {
                    continue;
                }
                User user = userService.getById(record.getUid());
                if (ObjectUtil.isNull(user)) {
                    continue;
                }
                record.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_COMPLETE);
                // 计算佣金余额
                BigDecimal balance = user.getBrokeragePrice().add(record.getPrice());
                record.setBalance(balance);
                record.setUpdateTime(DateUtil.date());

                recordList.add(record);
            }
        }
        if (CollUtil.isEmpty(recordList)) {
            return;
        }
        Boolean execute = transactionTemplate.execute(e -> {
            userBrokerageRecordService.updateBatchById(recordList);
            recordList.forEach(record -> {
                userService.updateBrokerage(record.getUid(), record.getPrice(), Constants.OPERATION_TYPE_ADD);
            });
            return Boolean.TRUE;
        });
        if (!execute) {
            logger.error(StrUtil.format("佣金解冻处理—数据库出错，订单编号 = {}", order.getOrderNo()));
        }

    }

    /**
     * 订单商户分账冻结处理
     * @param orderNoList 订单编号列表
     */
    private void orderMerchantShareFreezingOperation(List<String> orderNoList, Integer freezeDay) {
        Order order = orderService.getByOrderNo(orderNoList.get(0));
        List<MerchantBalanceRecord> recordList = new ArrayList<>();
        if (freezeDay > 0) {
            for (String orderNo : orderNoList) {
                MerchantBalanceRecord record = merchantBalanceRecordService.getByLinkNo(orderNo);
                if (ObjectUtil.isNull(record)) {
                    continue;
                }
                if (!record.getStatus().equals(1)) {
                    continue;
                }
                record.setStatus(2);
                record.setFrozenTime(freezeDay);
                DateTime dateTime = DateUtil.offsetDay(new Date(), record.getFrozenTime());
                long thawTime = dateTime.getTime();
                record.setThawTime(thawTime);
                record.setUpdateTime(DateUtil.date());
                recordList.add(record);
            }
            if (CollUtil.isEmpty(recordList)) {
                return;
            }
            merchantBalanceRecordService.updateBatchById(recordList);
            return;
        }
        for (String orderNo : orderNoList) {
            MerchantBalanceRecord record = merchantBalanceRecordService.getByLinkNo(orderNo);
            if (ObjectUtil.isNull(record)) {
                continue;
            }
            if (!record.getStatus().equals(1)) {
                continue;
            }
            record.setStatus(3);
            Merchant merchant = merchantService.getById(record.getMerId());
            record.setBalance(merchant.getBalance().add(record.getAmount()));
            record.setUpdateTime(DateUtil.date());
            recordList.add(record);
        }
        if (CollUtil.isEmpty(recordList)) {
            return;
        }
        Boolean execute = transactionTemplate.execute(e -> {
            merchantBalanceRecordService.updateBatchById(recordList);
            recordList.forEach(record -> {
                merchantService.operationBalance(record.getMerId(), record.getAmount(), Constants.OPERATION_TYPE_ADD);
            });
            return Boolean.TRUE;
        });
        if (!execute) {
            logger.error(StrUtil.format("商户余额解冻处理—数据库出错，订单编号 = {}", order.getOrderNo()));
        }
    }

    /**
     * 单商户订单处理
     *
     * @param order         主订单
     * @param merchantOrder 商户订单
     */
    private Boolean oneMerchantOrderProcessing(Order order, MerchantOrder merchantOrder) {
        // 赠送积分积分处理：1.下单赠送积分
        List<OrderDetail> orderDetailList = orderDetailService.getByOrderNo(order.getOrderNo());
        User user = userService.getById(order.getUid());
        presentIntegral(merchantOrder, orderDetailList, order, user.getIsPaidMember());

        // 生成新的商户订单
        Order newOrder = new Order();
        BeanUtils.copyProperties(order, newOrder);
        MerchantOrder newMerOrder = new MerchantOrder();
        BeanUtils.copyProperties(merchantOrder, newMerOrder);
        newOrder.setId(null);
        newOrder.setOrderNo(CrmebUtil.getOrderNo(OrderConstants.ORDER_PREFIX_MERCHANT));
        newOrder.setMerId(merchantOrder.getMerId());
        newOrder.setLevel(OrderConstants.ORDER_LEVEL_MERCHANT);
        newOrder.setPlatOrderNo(order.getOrderNo());
        newOrder.setStatus(OrderConstants.ORDER_STATUS_WAIT_SHIPPING);
        newOrder.setPlatCouponPrice(merchantOrder.getPlatCouponPrice());
        newOrder.setMerCouponPrice(merchantOrder.getMerCouponPrice());
        if (merchantOrder.getShippingType().equals(OrderConstants.ORDER_SHIPPING_TYPE_PICK_UP)
            && !order.getType().equals(OrderConstants.ORDER_TYPE_PITUAN)) { // 排除拼团订单
            newOrder.setStatus(OrderConstants.ORDER_STATUS_AWAIT_VERIFICATION);
        }
        if(order.getType().equals(OrderConstants.ORDER_TYPE_PITUAN)){ // 拼团订单下单固定状态
            newOrder.setGroupBuyRecordStatus(GroupBuyRecordEnum.GROUP_BUY_RECORD_ENUM_STATUS_INIT.getCode());
        }
        newMerOrder.setId(null);
        newMerOrder.setOrderNo(newOrder.getOrderNo());
        List<OrderDetail> newOrderDetailList = orderDetailList.stream().map(e -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(e, orderDetail);
            orderDetail.setId(null);
            orderDetail.setOrderNo(newOrder.getOrderNo());
            orderDetail.setUpdateTime(DateUtil.date());
            return orderDetail;
        }).collect(Collectors.toList());

        order.setIsDel(true);
        merchantOrder.setUpdateTime(DateUtil.date());
        Boolean result = transactionTemplate.execute(e -> {
            // 订单
            Boolean delete = orderService.paySplitDelete(order.getOrderNo());
            if (!delete) {
                logger.error("支付拆单失败，订单号:{}", order.getOrderNo());
                e.setRollbackOnly();
                return Boolean.FALSE;
            }
            merchantOrderService.updateById(merchantOrder);
            //更新发票
            Invoice invoice = invoiceService.getByOrderNo(order.getOrderNo());
            if(Objects.nonNull(invoice)){
                invoice.setOrderNo(newMerOrder.getOrderNo());
                invoiceService.updateById(invoice);
            }
            if (order.getGainIntegral() > 0) {
                orderDetailList.forEach(o -> o.setUpdateTime(DateUtil.date()));
                orderDetailService.updateBatchById(orderDetailList);
            }
            orderService.save(newOrder);
            merchantOrderService.save(newMerOrder);
            orderDetailService.saveBatch(newOrderDetailList);

            if(order.getType().equals(OrderConstants.ORDER_TYPE_PITUAN)){
                // 根据当前商户订单号对应的平台单号去更新拼团购买记录的订单号为sh
                logger.info("拼团正在处理的订单号：${}:", order.getOrderNo());
                groupBuyUserService.afterPay(order.getOrderNo());
            }

            //订单日志
            orderStatusService.createLog(order.getOrderNo(), OrderStatusConstants.ORDER_STATUS_PAY_SPLIT, StrUtil.format(OrderStatusConstants.ORDER_LOG_MESSAGE_PAY_SPLIT, order.getOrderNo()));
            return Boolean.TRUE;
        });

        // 如果事务执行成功，为商户创建待办事项
        if (result) {
            try {
                createOrderTodoForMerchant(merchantOrder.getMerId(), newOrder.getOrderNo(), user);
            } catch (Exception ex) {
                logger.error("创建商户待办事项失败，商户ID:{}, 订单号:{}, 错误信息:{}",
                    merchantOrder.getMerId(), newOrder.getOrderNo(), ex.getMessage());
            }
        }

        return result;
    }

    /**
     * 多商户订单处理
     *
     * @param order             主订单
     * @param merchantOrderList 商户订单列表
     */
    private Boolean manyMerchantOrderProcessing(Order order, List<MerchantOrder> merchantOrderList) {
        List<OrderDetail> orderDetailList = orderDetailService.getByOrderNo(order.getOrderNo());
        User user = userService.getById(order.getUid());
        // 赠送积分积分处理：1.下单赠送积分
        presentIntegral(merchantOrderList, orderDetailList, order, user.getIsPaidMember());
        // 商户拆单
        List<Order> newOrderList = CollUtil.newArrayList();
        List<MerchantOrder> newMerchantOrderList = CollUtil.newArrayList();
        List<OrderDetail> newOrderDetailList = CollUtil.newArrayList();

        order.setIsDel(true);
        for (MerchantOrder merchantOrder : merchantOrderList) {
            Order newOrder = new Order();
            BeanUtils.copyProperties(order, newOrder);
            newOrder.setId(null);
            newOrder.setOrderNo(CrmebUtil.getOrderNo(OrderConstants.ORDER_PREFIX_MERCHANT));
            newOrder.setMerId(merchantOrder.getMerId());
            newOrder.setTotalNum(merchantOrder.getTotalNum());
            newOrder.setProTotalPrice(merchantOrder.getProTotalPrice());
            newOrder.setTotalPostage(merchantOrder.getTotalPostage());
            newOrder.setTotalPrice(merchantOrder.getTotalPrice());
            newOrder.setCouponPrice(merchantOrder.getCouponPrice());
            newOrder.setMerCouponPrice(merchantOrder.getMerCouponPrice());
            newOrder.setPlatCouponPrice(merchantOrder.getPlatCouponPrice());
            newOrder.setUseIntegral(merchantOrder.getUseIntegral());
            newOrder.setIntegralPrice(merchantOrder.getIntegralPrice());
            newOrder.setPayPrice(merchantOrder.getPayPrice());
            newOrder.setPayPostage(merchantOrder.getPayPostage());
            newOrder.setGainIntegral(merchantOrder.getGainIntegral());
            newOrder.setLevel(OrderConstants.ORDER_LEVEL_MERCHANT);
            newOrder.setStatus(OrderConstants.ORDER_STATUS_WAIT_SHIPPING);
            newOrder.setPlatOrderNo(order.getOrderNo());
            newOrder.setIsDel(false);
            newOrder.setStatus(OrderConstants.ORDER_STATUS_WAIT_SHIPPING);
            if (merchantOrder.getShippingType().equals(OrderConstants.ORDER_SHIPPING_TYPE_PICK_UP)) {
                newOrder.setStatus(OrderConstants.ORDER_STATUS_AWAIT_VERIFICATION);
            }
            newOrder.setSvipDiscountPrice(merchantOrder.getSvipDiscountPrice());
            MerchantOrder newMerchantOrder = new MerchantOrder();
            BeanUtils.copyProperties(merchantOrder, newMerchantOrder);
            newMerchantOrder.setId(null);
            newMerchantOrder.setOrderNo(newOrder.getOrderNo());
            List<OrderDetail> tempDetailList = orderDetailList.stream().filter(e -> e.getMerId().equals(merchantOrder.getMerId())).collect(Collectors.toList());
            tempDetailList.forEach(d -> {
                d.setId(null);
                d.setOrderNo(newOrder.getOrderNo());
            });
            newOrderList.add(newOrder);
            newMerchantOrderList.add(newMerchantOrder);
            newOrderDetailList.addAll(tempDetailList);
            //更新发票
            Invoice invoice = invoiceService.getByOrderNo(order.getOrderNo());
            if(Objects.nonNull(invoice)){
                invoice.setOrderNo(newOrder.getOrderNo());
                invoiceService.updateById(invoice);
            }
        }

        merchantOrderList.forEach(o -> o.setUpdateTime(DateUtil.date()));
        Boolean result = transactionTemplate.execute(e -> {
            // 订单
            Boolean delete = orderService.paySplitDelete(order.getOrderNo());
            if (!delete) {
                logger.error("支付拆单失败，订单号:{}", order.getOrderNo());
                e.setRollbackOnly();
                return Boolean.FALSE;
            }
            merchantOrderService.updateBatchById(merchantOrderList);
            orderService.saveBatch(newOrderList);
            merchantOrderService.saveBatch(newMerchantOrderList);
            orderDetailService.saveBatch(newOrderDetailList);
            // 订单日志
            orderStatusService.createLog(order.getOrderNo(), OrderStatusConstants.ORDER_STATUS_PAY_SPLIT, StrUtil.format(OrderStatusConstants.ORDER_LOG_MESSAGE_PAY_SPLIT, order.getOrderNo()));
            return Boolean.TRUE;
        });

        // 如果事务执行成功，为每个商户创建待办事项
        if (result) {
            for (Order newOrder : newOrderList) {
                try {
                    createOrderTodoForMerchant(newOrder.getMerId(), newOrder.getOrderNo(), user);
                } catch (Exception ex) {
                    logger.error("创建商户待办事项失败，商户ID:{}, 订单号:{}, 错误信息:{}",
                        newOrder.getMerId(), newOrder.getOrderNo(), ex.getMessage());
                }
            }
        }

        return result;
    }

    /**
     * 赠送积分处理
     */
    private void presentIntegral(MerchantOrder merchantOrder, List<OrderDetail> orderDetailList, Order order, Boolean isPaidMember) {
        //比例
        String integralRatioStr = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_INTEGRAL_RATE_ORDER_GIVE);
        // 当下单支付金额按比例赠送积分 <= 0 时，不进行计算
        if (StrUtil.isNotBlank(integralRatioStr) && order.getPayPrice().compareTo(BigDecimal.ZERO) > 0 && new BigDecimal(integralRatioStr).compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal integralBig = new BigDecimal(integralRatioStr);
            int giveIntegral = merchantOrder.getPayPrice().divide(integralBig, 0, BigDecimal.ROUND_DOWN).intValue();
            if (isPaidMember) {
                List<PaidMemberBenefitsVo> benefitsList = paidMemberService.getBenefitsList();
                for (PaidMemberBenefitsVo b : benefitsList) {
                    if (b.getStatus()) {
                        if (b.getName().equals("integralDoubling") && b.getMultiple() > 1 && b.getChannelStr().contains("2")) {
                            giveIntegral = giveIntegral * b.getMultiple();
                        }
                    }
                }
            }
            merchantOrder.setGainIntegral(giveIntegral);
            order.setGainIntegral(giveIntegral);
            if (giveIntegral > 0) {
                // 订单详情
                for (int i = 0; i < orderDetailList.size(); i++) {
                    OrderDetail orderDetail = orderDetailList.get(i);
                    if (orderDetailList.size() == (i + 1)) {
                        orderDetail.setGainIntegral(giveIntegral);
                        break;
                    }
                    BigDecimal ratio = orderDetail.getPayPrice().divide(merchantOrder.getPayPrice(), 10, BigDecimal.ROUND_HALF_UP);
                    int integral = new BigDecimal(Integer.toString(merchantOrder.getGainIntegral())).multiply(ratio).setScale(0, BigDecimal.ROUND_DOWN).intValue();
                    orderDetail.setGainIntegral(integral);
                    giveIntegral = giveIntegral - integral;
                }
            }
        }
    }

    /**
     * 赠送积分处理
     */
    private void presentIntegral(List<MerchantOrder> merchantOrderList, List<OrderDetail> orderDetailList, Order order, Boolean isPaidMember) {
        int integral = 0;
        //比例
        String integralRatioStr = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_INTEGRAL_RATE_ORDER_GIVE);
        if (StrUtil.isNotBlank(integralRatioStr) && order.getPayPrice().compareTo(BigDecimal.ZERO) > 0) {
            for (MerchantOrder merOrder : merchantOrderList) {
                BigDecimal integralBig = new BigDecimal(integralRatioStr);
                int giveIntegral;
                if (integralBig.compareTo(BigDecimal.ZERO) <= 0) {
                    giveIntegral = 0;
                } else {
                    giveIntegral = merOrder.getPayPrice().divide(integralBig, 0, BigDecimal.ROUND_DOWN).intValue();
                    if (isPaidMember) {
                        List<PaidMemberBenefitsVo> benefitsList = paidMemberService.getBenefitsList();
                        for (PaidMemberBenefitsVo b : benefitsList) {
                            if (b.getStatus()) {
                                if (b.getName().equals("integralDoubling") && b.getMultiple() > 1 && b.getChannelStr().contains("2")) {
                                    giveIntegral = giveIntegral * b.getMultiple();
                                }
                            }
                        }
                    }
                }
                integral += giveIntegral;
                merOrder.setGainIntegral(giveIntegral);
                if (giveIntegral > 0) {
                    List<OrderDetail> detailList = orderDetailList.stream().filter(e -> e.getMerId().equals(merOrder.getMerId())).collect(Collectors.toList());
                    // 订单详情
                    for (int i = 0; i < detailList.size(); i++) {
                        OrderDetail orderDetail = detailList.get(i);
                        if (detailList.size() == (i + 1)) {
                            orderDetail.setGainIntegral(giveIntegral);
                        }
                        BigDecimal ratio = orderDetail.getPayPrice().divide(merOrder.getPayPrice(), 10, BigDecimal.ROUND_HALF_UP);
                        int detailIntegral = new BigDecimal(Integer.toString(merOrder.getGainIntegral())).multiply(ratio).setScale(0, BigDecimal.ROUND_DOWN).intValue();
                        orderDetail.setGainIntegral(detailIntegral);
                        giveIntegral = giveIntegral - detailIntegral;
                    }
                }
            }
            if (integral > 0) {
                order.setGainIntegral(integral);
            }
        }
    }

    /**
     * 订单完成后创建商户分账记录
     *
     * @param orderNoList 订单编号列表
     */
    private void orderProfitSharingCreateOperation(List<String> orderNoList) {
        try {
            for (String orderNo : orderNoList) {
                Order order = orderService.getByOrderNo(orderNo);
                if (ObjectUtil.isNull(order)) {
                    logger.warn("订单不存在，跳过分账记录创建: {}", orderNo);
                    continue;
                }

                // 只处理微信支付的订单
                if (!PayConstants.PAY_TYPE_WE_CHAT.equals(order.getPayType())) {
                    logger.info("非微信支付订单，跳过分账记录创建: {}", orderNo);
                    continue;
                }

                // 获取微信支付信息
                WechatPayInfo payInfo = wechatPayInfoService.getByNo(order.getOutTradeNo());
                if (ObjectUtil.isNull(payInfo) || StrUtil.isBlank(payInfo.getTransactionId())) {
                    logger.warn("微信支付信息不存在或交易号为空，跳过分账记录创建: {}", orderNo);
                    continue;
                }

                // 获取商户订单
                MerchantOrder merchantOrder = merchantOrderService.getOneByOrderNo(orderNo);
                if (ObjectUtil.isNull(merchantOrder)) {
                    logger.warn("商户订单不存在，跳过分账记录创建: {}", orderNo);
                    continue;
                }

                // 创建分账记录
                profitSharingDetailService.createSharingRecord(
                    orderNo,
                    payInfo.getTransactionId(),
                    order.getPayPrice(),
                    merchantOrder.getMerId()
                );

                logger.info("分账记录创建成功: 订单号={}, 商户ID={}", orderNo, merchantOrder.getMerId());
            }
        } catch (Exception e) {
            logger.error("创建商户分账记录异常", e);
        }
    }

    /**
     * 为商户创建订单支付成功待办事项
     *
     * @param merId   商户ID
     * @param orderNo 订单号
     * @param user    用户信息
     */
    @Async
    @Override
    public void createOrderTodoForMerchant(Integer merId, String orderNo, User user) {
        try {
            MerchantTodoItem todoItem = new MerchantTodoItem();
            todoItem.setMerId(merId);
            todoItem.setType("order");
            todoItem.setTitle("新订单待发货");
            todoItem.setContent(String.format("订单号：%s，用户：%s 已支付成功，请及时发货！", 
                orderNo, user.getNickname()));
            todoItem.setPriority(2); // 中等优先级
            todoItem.setRelatedId(orderNo);
            todoItem.setTargetUrl("/merchant/order/detail/" + orderNo);
            
            Boolean result = merchantTodoItemService.create(todoItem);
            if (result) {
                logger.info("商户待办事项创建成功，商户ID:{}, 订单号:{}", merId, orderNo);
            } else {
                logger.error("商户待办事项创建失败，商户ID:{}, 订单号:{}", merId, orderNo);
            }
        } catch (Exception e) {
            logger.error("创建商户待办事项异常，商户ID:{}, 订单号:{}, 错误信息:{}", 
                merId, orderNo, e.getMessage(), e);
        }
    }

    @Async("taskExecutor")
    @Override
    public void executeImportTask(String taskId, File tempFile, Integer merId,ConcurrentHashMap<String, Map<String, Object>> IMPORT_TASK_STATUS_MAP) {
        // 检查taskId是否为null
        if (taskId == null || taskId.isEmpty()) {
            logger.error("任务ID为空，无法执行导入任务");
            return;
        }

        Map<String, Object> statusMap = IMPORT_TASK_STATUS_MAP.get(taskId);
        // 检查statusMap是否为null
        if (statusMap == null) {
            logger.error("未找到任务状态信息，任务ID: {}", taskId);
            return;
        }

        try {
            logger.info("开始执行异步导入任务，任务ID: {}", taskId);

            // 更新进度
            statusMap.put("progress", 10);
            statusMap.put("message", "正在读取文件...");

            ProductImportResultVo result = new ProductImportResultVo();
            result.setTotalCount(0);
            result.setSuccessCount(0);
            result.setFailCount(0);
            result.setErrorList(new ArrayList<>());

            // 提取Excel中的图片
            statusMap.put("progress", 20);
            statusMap.put("message", "正在处理图片...");

            Map<String, String> imageMap = extractAndUploadImagesFromExcel(tempFile);
            logger.info("从Excel中提取到{}张图片", imageMap.size());

            // 第一步：读取商品基本信息（第一个sheet）
            statusMap.put("progress", 30);
            statusMap.put("message", "正在读取商品基本信息...");

            Map<String, ProductImportVo> productBasicInfoMap = new HashMap<>();
            EasyExcel.read(tempFile, ProductImportVo.class, new AnalysisEventListener<ProductImportVo>() {
                private int rowIndex = 0;

                @Override
                public void invoke(ProductImportVo data, AnalysisContext context) {
                    rowIndex++;
                    if (StrUtil.isNotBlank(data.getName())) {
                        // 处理Excel中的图片
                        processImagesForRow(data, rowIndex, imageMap);
                        productBasicInfoMap.put(data.getName(), data);
                    }

                    // 更新进度
                    if (rowIndex % 100 == 0) {
                        statusMap.put("message", "已读取 " + rowIndex + " 行商品基本信息...");
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    logger.info("读取到{}个商品基本信息", productBasicInfoMap.size());
                }
            }).sheet("商品基本信息").doRead();

            // 第二步：读取商品规格配置（第二个sheet）
            statusMap.put("progress", 40);
            statusMap.put("message", "正在读取商品规格配置...");

            Map<String, List<ProductSpecConfigVo>> productSpecConfigMap = new HashMap<>();
            EasyExcel.read(tempFile, ProductSpecConfigVo.class, new AnalysisEventListener<ProductSpecConfigVo>() {
                @Override
                public void invoke(ProductSpecConfigVo data, AnalysisContext context) {
                    if (StrUtil.isNotBlank(data.getProductName())) {
                        productSpecConfigMap.computeIfAbsent(data.getProductName(), k -> new ArrayList<>()).add(data);
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    logger.info("读取到{}个商品的规格配置", productSpecConfigMap.size());
                }
            }).sheet("商品规格配置").doRead();

            // 第三步：合并数据并创建商品
            statusMap.put("progress", 50);
            statusMap.put("message", "开始导入商品...");

            int totalProducts = productBasicInfoMap.size();
            int processedCount = 0;

            for (Map.Entry<String, ProductImportVo> entry : productBasicInfoMap.entrySet()) {
                String productName = entry.getKey();
                ProductImportVo basicInfo = entry.getValue();

                result.setTotalCount(result.getTotalCount() + 1);
                processedCount++;

                try {
                    // 数据验证
                    validateProductImportData(basicInfo, 0);

                    // 获取对应的规格配置
                    List<ProductSpecConfigVo> specConfigs = productSpecConfigMap.get(productName);
                    if (CollUtil.isEmpty(specConfigs)) {
                        throw new CrmebException("商品[" + productName + "]未找到对应的规格配置");
                    }

                    // 转换为ProductAddRequest
                    ProductAddRequest productRequest = convertToProductAddRequestWithSpecs(basicInfo, specConfigs, merId);

                    // 调用商品保存服务
                    productService.save(productRequest);

                    result.setSuccessCount(result.getSuccessCount() + 1);
                    logger.info("成功导入商品：{}", productName);

                } catch (Exception e) {
                    result.setFailCount(result.getFailCount() + 1);

                    ProductImportResultVo.ProductImportErrorVo error = new ProductImportResultVo.ProductImportErrorVo();
                    error.setRowIndex(result.getTotalCount());
                    error.setProductName(productName);
                    error.setErrorMessage(e.getMessage());
                    result.getErrorList().add(error);
                    logger.error("导入商品失败：{}，错误：{}", productName, e.getMessage());
                }

                // 更新进度
                int progress = 50 + (processedCount * 40 / totalProducts);
                statusMap.put("progress", progress);
                statusMap.put("message", String.format("正在导入商品 %d/%d...", processedCount, totalProducts));
                statusMap.put("totalCount", result.getTotalCount());
                statusMap.put("successCount", result.getSuccessCount());
                statusMap.put("failCount", result.getFailCount());
            }

            // 更新任务状态为完成
            statusMap.put("status", "completed");
            statusMap.put("progress", 100);
            statusMap.put("totalCount", result.getTotalCount());
            statusMap.put("successCount", result.getSuccessCount());
            statusMap.put("failCount", result.getFailCount());
            statusMap.put("errorList", result.getErrorList());
            statusMap.put("endTime", new Date());
            statusMap.put("message", String.format("导入完成！总计：%d，成功：%d，失败：%d",
                    result.getTotalCount(), result.getSuccessCount(), result.getFailCount()));

            logger.info("商品导入任务完成，任务ID: {}, 总计：{}，成功：{}，失败：{}",
                    taskId, result.getTotalCount(), result.getSuccessCount(), result.getFailCount());

        } catch (Exception e) {
            // 更新任务状态为失败
            statusMap.put("status", "failed");
            statusMap.put("message", "导入失败: " + e.getMessage());
            statusMap.put("endTime", new Date());
            logger.error("商品导入任务失败，任务ID: " + taskId, e);
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (deleted) {
                    logger.info("临时文件已删除: {}", tempFile.getAbsolutePath());
                } else {
                    logger.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 从Excel文件(File类型)中提取并上传图片
     */
    private Map<String, String> extractAndUploadImagesFromExcel(File file) {
        Map<String, String> imageMap = new HashMap<>();

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            // 处理第一个sheet（商品基本信息）
            if (workbook.getNumberOfSheets() > 0) {
                XSSFSheet sheet1 = workbook.getSheetAt(0);
                logger.info("开始提取第一个工作表的图片，工作表名：{}", sheet1.getSheetName());

                // 方法1：通过Drawing获取所有图片及其精确位置
                extractImagesFromDrawing(sheet1, imageMap);

                // 方法2：处理单元格中的图片引用（DISPIMG函数等）
                extractImagesFromCells(workbook, sheet1, imageMap);

                // 方法3：智能分配剩余图片（仅在imageMap为空时）
                if (imageMap.isEmpty()) {
                    intelligentImageAllocation(workbook, sheet1, imageMap);
                }
            }

            // 处理第二个sheet（商品规格配置）
            if (workbook.getNumberOfSheets() > 1) {
                XSSFSheet sheet2 = workbook.getSheetAt(1);
                logger.info("开始提取第二个工作表的图片，工作表名：{}", sheet2.getSheetName());

                // 为规格配置表的图片使用特殊的前缀
                Map<String, String> specImageMap = new HashMap<>();

                // 从第二个sheet提取图片
                extractImagesFromDrawing(sheet2, specImageMap);
                extractImagesFromCells(workbook, sheet2, specImageMap);

                // 将规格配置的图片添加到主imageMap中，使用特殊前缀"spec_"
                for (Map.Entry<String, String> entry : specImageMap.entrySet()) {
                    imageMap.put("spec_" + entry.getKey(), entry.getValue());
                }

                logger.info("从规格配置表提取到{}张图片", specImageMap.size());
            }

            workbook.close();

        } catch (Exception e) {
            logger.error("提取Excel图片失败", e);
        }

        logger.info("图片提取完成，共提取{}张图片", imageMap.size());
        return imageMap;
    }

    /**
     * 智能分配图片
     */
    private void intelligentImageAllocation(XSSFWorkbook workbook, XSSFSheet sheet, Map<String, String> imageMap) {
        List<XSSFPictureData> allPictures = workbook.getAllPictures();
        logger.info("工作簿中总共找到{}张图片，尝试智能分配", allPictures.size());

        if (allPictures.isEmpty()) {
            return;
        }

        // 统计有多少行需要图片
        List<Integer> imageRows = new ArrayList<>();
        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            XSSFRow row = sheet.getRow(rowNum);
            if (row != null && isDataRow(row)) {
                imageRows.add(rowNum);
            }
        }

        logger.info("发现{}行数据需要图片，共有{}张图片", imageRows.size(), allPictures.size());

        // 按比例分配图片
        int pictureIndex = 0;
        for (Integer rowNum : imageRows) {
            if (pictureIndex >= allPictures.size()) {
                break; // 图片用完了
            }

            // 优先分配给第10列（主图）
            String key10 = rowNum + "_10";
            if (!imageMap.containsKey(key10) && pictureIndex < allPictures.size()) {
                try {
                    XSSFPictureData pictureData = allPictures.get(pictureIndex++);
                    String imageUrl = uploadImageFromBytes(pictureData.getData(),
                            pictureData.suggestFileExtension());
                    imageMap.put(key10, imageUrl);
                    logger.info("智能分配图片到行{}，列10，URL：{}", rowNum, imageUrl);
                } catch (Exception e) {
                    logger.error("智能分配图片失败，行{}，列10", rowNum, e);
                }
            }

            // 如果还有图片，分配给第11列（轮播图）
            String key11 = rowNum + "_11";
            if (!imageMap.containsKey(key11) && pictureIndex < allPictures.size()) {
                try {
                    XSSFPictureData pictureData = allPictures.get(pictureIndex++);
                    String imageUrl = uploadImageFromBytes(pictureData.getData(),
                            pictureData.suggestFileExtension());
                    imageMap.put(key11, imageUrl);
                    logger.info("智能分配图片到行{}，列11，URL：{}", rowNum, imageUrl);
                } catch (Exception e) {
                    logger.error("智能分配图片失败，行{}，列11", rowNum, e);
                }
            }
        }
    }
    /**
     * 判断是否为数据行（检查是否有实际的商品数据）
     */
    private boolean isDataRow(XSSFRow row) {
        if (row == null) {
            return false;
        }

        // 优先检查商品名称列（第1列）是否有内容
        XSSFCell nameCell = row.getCell(1);
        if (nameCell != null && hasContent(nameCell)) {
            return true;
        }

        // 检查其他关键字段是否有内容（前10列）
        for (int i = 0; i < 10; i++) {
            XSSFCell cell = row.getCell(i);
            if (cell != null && hasContent(cell)) {
                return true;
            }
        }

        return false;
    }
    /**
     * 检查单元格是否有内容
     */
    private boolean hasContent(XSSFCell cell) {
        if (cell == null) {
            return false;
        }

        try {
            switch (cell.getCellType()) {
                case STRING:
                    String stringValue = cell.getStringCellValue();
                    return StrUtil.isNotBlank(stringValue);
                case NUMERIC:
                    return true; // 数字类型认为有内容
                case BOOLEAN:
                    return true; // 布尔类型认为有内容
                case FORMULA:
                    return StrUtil.isNotBlank(cell.getCellFormula());
                case BLANK:
                    return false;
                case ERROR:
                    return false;
                default:
                    return false;
            }
        } catch (Exception e) {
            logger.debug("检查单元格内容时出错：{}", e.getMessage());
            return false;
        }
    }


    /**
     * 从单元格中提取图片引用
     */
    private void extractImagesFromCells(XSSFWorkbook workbook, XSSFSheet sheet, Map<String, String> imageMap) {
        String sheetName = sheet.getSheetName();
        logger.info("正在检查工作表 '{}' 中的图片函数", sheetName);

        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            XSSFRow row = sheet.getRow(rowNum);
            if (row != null) {
                if ("商品规格配置".equals(sheetName) || sheetName.contains("规格")) {
                    // 规格配置表：检查规格图片列(第2列)
                    processImageInCell(workbook, row, 2, rowNum, imageMap);
                    logger.debug("检查规格配置表第{}行第2列的图片", rowNum);
                } else {
                    // 商品基本信息表：检查商品主图列(第10列)和轮播图列(第11-15列)
                    processImageInCell(workbook, row, 10, rowNum, imageMap);

                    // 检查轮播图列(第11-15列)，支持多张图片
                    for (int col = 11; col <= 15; col++) {
                        processImageInCell(workbook, row, col, rowNum, imageMap);
                    }

                    // 检查商品详情列(第12列)，可能包含多张图片
                    processImageInCell(workbook, row, 12, rowNum, imageMap);

                    logger.debug("检查基本信息表第{}行的图片列", rowNum);
                }
            }
        }

        logger.info("工作表 '{}' 图片函数检查完成", sheetName);
    }
    /**
     * 处理单元格中的图片
     */
    private void processImageInCell(XSSFWorkbook workbook, XSSFRow row, int colIndex, int rowNum, Map<String, String> imageMap) {
        XSSFCell cell = row.getCell(colIndex);
        if (cell == null) {
            return;
        }

        String key = rowNum + "_" + colIndex;

        // 如果已经处理过这个位置的图片，跳过
        if (imageMap.containsKey(key)) {
            return;
        }

        try {
            // 检查单元格类型
            switch (cell.getCellType()) {
                case STRING:
                    String cellValue = cell.getStringCellValue();
                    if (cellValue != null) {
                        // 检查是否包含图片函数
                        if (cellValue.contains("_xlfn.DISPIMG") || cellValue.contains("DISPIMG")) {
                            processImageFunction(workbook, cellValue, key, imageMap, rowNum, colIndex);
                        }
                        // 检查是否已经是图片URL
                        else if (isValidImageUrl(cellValue)) {
                            imageMap.put(key, cellValue);
                            logger.info("发现现有图片URL，位置：行{}，列{}，URL：{}", rowNum, colIndex, cellValue);
                        }
                        // 检查是否是图片占位符或提示文本
                        else if (isImagePlaceholder(cellValue)) {
                            logger.info("发现图片占位符，位置：行{}，列{}，内容：{}", rowNum, colIndex, cellValue);
                            // 这种情况下，图片可能通过其他方式嵌入，等待后续处理
                        }
                    }
                    break;
                case FORMULA:
                    // 处理公式单元格，可能包含图片函数
                    String formula = cell.getCellFormula();
                    if (formula != null && (formula.contains("DISPIMG") || formula.contains("_xlfn.DISPIMG"))) {
                        processImageFunction(workbook, formula, key, imageMap, rowNum, colIndex);
                    }
                    break;
                case BLANK:
                    // 空白单元格可能包含图片，通过Drawing处理
                    logger.debug("空白单元格，位置：行{}，列{}，可能包含图片", rowNum, colIndex);
                    break;
                default:
                    // 其他类型的单元格
                    logger.debug("其他类型单元格，位置：行{}，列{}，类型：{}", rowNum, colIndex, cell.getCellType());
                    break;
            }
        } catch (Exception e) {
            logger.error("处理单元格图片失败，位置：行{}，列{}", rowNum, colIndex, e);
        }
    }
    /**
     * 检查是否为有效的图片URL
     */
    private boolean isValidImageUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return false;
        }

        String lowerUrl = url.toLowerCase();
        return (lowerUrl.startsWith("http") || lowerUrl.startsWith("/")) &&
                (lowerUrl.contains(".jpg") || lowerUrl.contains(".jpeg") ||
                        lowerUrl.contains(".png") || lowerUrl.contains(".gif") ||
                        lowerUrl.contains(".bmp") || lowerUrl.contains(".webp"));
    }

    /**
     * 检查是否为图片占位符
     */
    private boolean isImagePlaceholder(String text) {
        if (StrUtil.isBlank(text)) {
            return false;
        }

        String lowerText = text.toLowerCase();
        return lowerText.contains("请直接粘贴图片") ||
                lowerText.contains("图片") ||
                lowerText.contains("image") ||
                lowerText.contains("picture") ||
                lowerText.contains("请上传") ||
                lowerText.contains("请插入");
    }
    /**
     * 处理图片函数
     */
    private void processImageFunction(XSSFWorkbook workbook, String functionStr, String key, Map<String, String> imageMap, int rowNum, int colIndex) {
        try {
            String imageId = extractImageIdFromDispImg(functionStr);
            if (imageId != null) {
                XSSFPictureData pictureData = findPictureDataById(workbook, imageId);
                if (pictureData != null) {
                    String imageUrl = uploadImageFromBytes(pictureData.getData(),
                            pictureData.suggestFileExtension());
                    imageMap.put(key, imageUrl);
                    logger.info("成功处理图片函数，位置：行{}，列{}，URL：{}", rowNum, colIndex, imageUrl);
                } else {
                    logger.warn("未找到图片数据，图片ID：{}，位置：行{}，列{}", imageId, rowNum, colIndex);
                }
            } else {
                logger.warn("无法提取图片ID，函数：{}，位置：行{}，列{}", functionStr, rowNum, colIndex);
            }
        } catch (Exception e) {
            logger.error("处理图片函数失败，位置：行{}，列{}", rowNum, colIndex, e);
        }
    }
    /**
     * 根据图片ID查找对应的图片数据
     * @param workbook Excel工作簿
     * @param imageId 图片ID
     * @return 图片数据
     */
    private XSSFPictureData findPictureDataById(XSSFWorkbook workbook, String imageId) {
        try {
            List<XSSFPictureData> allPictures = workbook.getAllPictures();

            // 方法1：尝试通过图片ID匹配（如果可能的话）
            for (XSSFPictureData pictureData : allPictures) {
                // 检查图片数据的包名或其他标识
                String fileName = pictureData.getPackagePart().getPartName().getName();
                if (fileName.contains(imageId) || imageId.contains(fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf(".")))) {
                    logger.info("通过ID匹配找到图片：{} -> {}", imageId, fileName);
                    return pictureData;
                }
            }

            // 方法2：如果无法通过ID匹配，使用改进的顺序分配策略
            // 使用图片ID的哈希值来确定索引，保证相同ID返回相同图片
            int index = Math.abs(imageId.hashCode()) % allPictures.size();
            if (index < allPictures.size()) {
                XSSFPictureData pictureData = allPictures.get(index);
                logger.info("通过哈希索引{}找到图片，对应图片ID：{}", index, imageId);
                return pictureData;
            }

            // 方法3：如果还是找不到，返回第一张图片
            if (!allPictures.isEmpty()) {
                logger.info("使用默认第一张图片，对应图片ID：{}", imageId);
                return allPictures.get(0);
            }

        } catch (Exception e) {
            logger.error("查找图片数据失败，图片ID：{}", imageId, e);
        }
        return null;
    }

    /**
     * 从DISPIMG函数中提取图片ID
     * @param dispImgFunction DISPIMG函数字符串，如：_xlfn.DISPIMG("ID_ED5186FBD9E3417986A2668C502A7ECCA",1)
     * @return 图片ID
     */
    private String extractImageIdFromDispImg(String dispImgFunction) {
        try {
            // 匹配模式：_xlfn.DISPIMG("ID_xxxx",1)
            if (dispImgFunction.contains("_xlfn.DISPIMG(\"") && dispImgFunction.contains("\",")) {
                int startIndex = dispImgFunction.indexOf("\"") + 1;
                int endIndex = dispImgFunction.indexOf("\",", startIndex);
                if (startIndex > 0 && endIndex > startIndex) {
                    String imageId = dispImgFunction.substring(startIndex, endIndex);
                    logger.info("提取到图片ID：{}", imageId);
                    return imageId;
                }
            }
        } catch (Exception e) {
            logger.error("提取图片ID失败：{}", dispImgFunction, e);
        }
        return null;
    }

    /**
     * 从Drawing中提取图片（处理单元格内嵌图片）
     * 这是处理Excel中直接插入到单元格内图片的核心方法
     */
    private void extractImagesFromDrawing(XSSFSheet sheet, Map<String, String> imageMap) {
        XSSFDrawing drawing = sheet.getDrawingPatriarch();
        if (drawing == null) {
            logger.info("工作表中没有找到Drawing对象");
            return;
        }

        List<XSSFShape> shapes = drawing.getShapes();
        logger.info("在Drawing中找到{}个图形对象", shapes.size());

        for (XSSFShape shape : shapes) {
            if (shape instanceof XSSFPicture) {
                XSSFPicture picture = (XSSFPicture) shape;
                XSSFPictureData pictureData = picture.getPictureData();

                // 获取图片锚点信息
                XSSFClientAnchor anchor = (XSSFClientAnchor) picture.getAnchor();
                if (anchor != null) {
                    // 获取图片的精确位置信息
                    int row1 = anchor.getRow1();
                    int col1 = anchor.getCol1();
                    int row2 = anchor.getRow2();
                    int col2 = anchor.getCol2();

                    // 获取锚点的偏移量（EMU单位，用于更精确的定位）
                    int dx1 = anchor.getDx1();
                    int dy1 = anchor.getDy1();
                    int dx2 = anchor.getDx2();
                    int dy2 = anchor.getDy2();

                    logger.info("图片详细位置：起始行{}列{}(偏移{},{}), 结束行{}列{}(偏移{},{})",
                            row1, col1, dx1, dy1, row2, col2, dx2, dy2);

                    // 使用更精确的算法确定图片主要位于哪个单元格
                    List<CellLocation> cellLocations = calculateImageCellLocations(anchor);

                    for (CellLocation location : cellLocations) {
                        int targetRow = location.row;
                        int targetCol = location.col;

                        logger.info("确定图片位置：行{}，列{}，覆盖度：{}%", targetRow, targetCol, location.coverage);

                        // 只处理商品主图(第10列)和轮播图(第11列)的图片
                        if (targetCol == 10 || targetCol == 11) {
                            try {
                                // 上传图片
                                String imageUrl = uploadImageFromBytes(pictureData.getData(),
                                        pictureData.suggestFileExtension());

                                // 存储映射关系：行号_列号 -> 图片URL
                                String key = targetRow + "_" + targetCol;
                                if (!imageMap.containsKey(key)) { // 避免重复
                                    imageMap.put(key, imageUrl);
                                    logger.info("成功上传Excel单元格内嵌图片，位置：行{}，列{}，URL：{}", targetRow, targetCol, imageUrl);
                                } else {
                                    logger.info("位置行{}，列{}已有图片，跳过", targetRow, targetCol);
                                }

                            } catch (Exception e) {
                                logger.error("上传Excel单元格内嵌图片失败，位置：行{}，列{}", targetRow, targetCol, e);
                            }
                        } else {
                            logger.debug("图片不在目标列（10或11），跳过。位置：行{}，列{}", targetRow, targetCol);
                        }
                    }
                }
            }
        }
    }
    /**
     * 上传图片字节数组
     * @param imageBytes 图片字节数组
     * @param fileExtension 文件扩展名
     * @return 图片URL
     */
    private String uploadImageFromBytes(byte[] imageBytes, String fileExtension) throws Exception {
        // 生成文件名
        String fileName = UploadUtil.generateFileName(fileExtension);
        // 服务器存储地址
        String rootPath = crmebConfig.getImagePath().trim();
        String modelPath = "public/product/";
        String type = UploadConstants.UPLOAD_FILE_KEYWORD + "/";
        String webPath = type + modelPath + CrmebDateUtil.nowDate("yyyy/MM/dd") + "/";
        String destPath = FilenameUtils.separatorsToSystem(rootPath + webPath) + fileName;

        // 创建临时文件
        File tempFile = UploadUtil.createFile(destPath);

        // 写入图片数据
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(imageBytes);
            fos.flush();
        }

        // 创建附件记录
        SystemAttachment systemAttachment = new SystemAttachment();
        systemAttachment.setName(fileName);
        systemAttachment.setSattDir(webPath + fileName);
        systemAttachment.setAttSize(String.valueOf(tempFile.length()));
        systemAttachment.setAttType(fileExtension);
        systemAttachment.setImageType(1); // 本地存储
        systemAttachment.setPid(0);
        systemAttachment.setOwner(-1); // 平台文件

        systemAttachmentService.save(systemAttachment);
        // 获取上传类型配置
        String uploadType = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_UPLOAD_TYPE);
        Integer uploadTypeInt = Integer.parseInt(uploadType);

        if (uploadTypeInt.equals(1)) {
            // 本地存储
            systemAttachmentService.save(systemAttachment);
            return systemAttachmentService.prefixFile(systemAttachment.getSattDir());
        }

        // 云存储处理
        CloudVo cloudVo = new CloudVo();
        String fileIsSave = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_FILE_IS_SAVE);

        switch (uploadTypeInt) {
            case 2: // 七牛云
                systemAttachment.setImageType(2);
                cloudVo.setDomain(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_UPLOAD_URL));
                cloudVo.setAccessKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_ACCESS_KEY));
                cloudVo.setSecretKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_SECRET_KEY));
                cloudVo.setBucketName(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_STORAGE_NAME));
                cloudVo.setRegion(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_STORAGE_REGION));
                try {
                    Configuration cfg = new Configuration(Region.autoRegion());
                    UploadManager uploadManager = new UploadManager(cfg);
                    Auth auth = Auth.create(cloudVo.getAccessKey(), cloudVo.getSecretKey());
                    String upToken = auth.uploadToken(cloudVo.getBucketName());
                    qiNiuService.uploadFile(uploadManager, upToken, systemAttachment.getSattDir(), destPath, tempFile);
                } catch (Exception e) {
                    logger.error("七牛云上传失败：" + e.getMessage());
                }
                break;
            case 3: // 阿里云OSS
                systemAttachment.setImageType(3);
                cloudVo.setDomain(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_UPLOAD_URL));
                cloudVo.setAccessKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_ACCESS_KEY));
                cloudVo.setSecretKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_SECRET_KEY));
                cloudVo.setBucketName(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_STORAGE_NAME));
                cloudVo.setRegion(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_STORAGE_REGION));
                try {
                    ossService.upload(cloudVo, systemAttachment.getSattDir(), destPath, tempFile);
                } catch (Exception e) {
                    logger.error("阿里云OSS上传失败：" + e.getMessage());
                }
                break;
            case 5: // 京东云
                systemAttachment.setImageType(5);
                String bucket = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_JD_BUCKET_NAME);
                try {
                    jdCloudService.uploadFile(systemAttachment.getSattDir(), destPath, bucket);
                } catch (Exception e) {
                    logger.error("京东云上传失败：" + e.getMessage());
                }
                break;
        }

        // 保存附件记录
        systemAttachmentService.save(systemAttachment);

        // 如果不保存本地文件，删除本地文件
        if (!fileIsSave.equals("1") && tempFile != null) {
            tempFile.delete();
        }

        // 返回文件访问URL
        return systemAttachmentService.prefixFile(systemAttachment.getSattDir());

    }


    /**
     * 计算图片覆盖的单元格位置
     * 返回图片主要覆盖的单元格列表，按覆盖度排序
     */
    private List<CellLocation> calculateImageCellLocations(XSSFClientAnchor anchor) {
        List<CellLocation> locations = new ArrayList<>();

        int row1 = anchor.getRow1();
        int col1 = anchor.getCol1();
        int row2 = anchor.getRow2();
        int col2 = anchor.getCol2();

        // 如果图片只在一个单元格内
        if (row1 == row2 && col1 == col2) {
            locations.add(new CellLocation(row1, col1, 100.0));
            return locations;
        }

        // 如果图片跨越多个单元格，计算每个单元格的覆盖度
        for (int row = row1; row <= row2; row++) {
            for (int col = col1; col <= col2; col++) {
                double coverage = calculateCellCoverage(anchor, row, col);
                if (coverage > 10.0) { // 只考虑覆盖度超过10%的单元格
                    locations.add(new CellLocation(row, col, coverage));
                }
            }
        }

        // 按覆盖度降序排序
        locations.sort((a, b) -> Double.compare(b.coverage, a.coverage));

        return locations;
    }
    /**
     * 计算图片在指定单元格的覆盖度
     */
    private double calculateCellCoverage(XSSFClientAnchor anchor, int targetRow, int targetCol) {
        int row1 = anchor.getRow1();
        int col1 = anchor.getCol1();
        int row2 = anchor.getRow2();
        int col2 = anchor.getCol2();

        // 简化计算：如果图片的起始位置就在目标单元格，给予更高的权重
        if (row1 == targetRow && col1 == targetCol) {
            return 90.0;
        }

        // 如果图片的结束位置在目标单元格
        if (row2 == targetRow && col2 == targetCol) {
            return 70.0;
        }

        // 如果图片跨越目标单元格
        if (targetRow >= row1 && targetRow <= row2 && targetCol >= col1 && targetCol <= col2) {
            return 50.0;
        }

        return 0.0;
    }

    /**
     * 单元格位置信息
     */
    private static class CellLocation {
        int row;
        int col;
        double coverage; // 覆盖度百分比

        CellLocation(int row, int col, double coverage) {
            this.row = row;
            this.col = col;
            this.coverage = coverage;
        }
    }
    /**
     * 从工作表中提取图片
     */
    private void extractImagesFromSheet(XSSFSheet sheet, Map<String, String> imageMap, String prefix, Integer maxRows) {
        XSSFDrawing drawing;
        try {
            drawing = sheet.getDrawingPatriarch();
        } catch (Exception e) {
            logger.warn("工作表 {} 中没有图片或无法访问图片", sheet.getSheetName());
            return;
        }
        
        if (drawing == null) {
            logger.info("工作表 {} 中没有图片", sheet.getSheetName());
            return;
        }
        
        for (XSSFShape shape : drawing.getShapes()) {
            if (shape instanceof XSSFPicture) {
                XSSFPicture picture = (XSSFPicture) shape;
                XSSFClientAnchor anchor = picture.getClientAnchor();
                
                // 获取图片数据
                PictureData pictureData = picture.getPictureData();
                
                // 计算图片所在的行和列
                int row1 = anchor.getRow1();
                int col1 = anchor.getCol1();
                
                // 限制处理的行数（可选）
                if (maxRows != null && row1 > maxRows) {
                    continue;
                }
                
                // 跳过标题行（假设第1行是标题）
                if (row1 < 1) {
                    continue;
                }
                
                // 生成唯一标识
                String key = prefix + (row1 + 1) + "_" + col1;
                
                // 如果已经有相同位置的图片，跳过
                if (imageMap.containsKey(key)) {
                    continue;
                }
                
                try {
                    // 获取文件扩展名
                    String extension = pictureData.suggestFileExtension();
                    if (extension == null || extension.isEmpty()) {
                        extension = "png"; // 默认使用png
                    }
                    
                    // 生成文件名
                    String fileName = IdUtil.simpleUUID() + "." + extension;
                    
                    // 保存到临时文件
                    File tempFile = new File(System.getProperty("java.io.tmpdir"), fileName);
                    FileUtil.writeBytes(pictureData.getData(), tempFile);
                    
                    // 上传图片
                    String imageUrl = uploadImageToCloud(tempFile, fileName);
                    
                    if (StrUtil.isNotBlank(imageUrl)) {
                        imageMap.put(key, imageUrl);
                        logger.info("成功上传图片：位置=({},{})，URL={}", row1 + 1, col1, imageUrl);
                    } else {
                        logger.warn("图片上传失败：位置=({},{})", row1 + 1, col1);
                    }
                    
                    // 删除临时文件
                    tempFile.delete();
                    
                } catch (Exception e) {
                    logger.error("处理图片失败：位置=({},{})", row1 + 1, col1, e);
                }
            }
        }
    }
    
    /**
     * 上传图片到云存储
     */
    private String uploadImageToCloud(File file, String fileName) {
        try {
            // 获取配置的存储类型
            // 创建附件记录
            SystemAttachment systemAttachment = new SystemAttachment();
            systemAttachment.setName(fileName);
            systemAttachment.setSattDir(file.getPath() + fileName);
            systemAttachment.setAttSize(String.valueOf(file.length()));
            systemAttachment.setAttType("xlsx");
            systemAttachment.setImageType(1); // 默认本地
            systemAttachment.setPid(0);
            systemAttachment.setOwner(-1); // 平台文件
            // 获取上传类型配置
            String uploadType = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_UPLOAD_TYPE);
            Integer uploadTypeInt = Integer.parseInt(uploadType);

            if (uploadTypeInt.equals(1)) {
                // 本地存储
                systemAttachmentService.save(systemAttachment);
                return systemAttachmentService.prefixFile(systemAttachment.getSattDir());
            }

            // 云存储处理
            CloudVo cloudVo = new CloudVo();
            String fileIsSave = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_FILE_IS_SAVE);

            switch (uploadTypeInt) {
                case 2: // 七牛云
                    systemAttachment.setImageType(2);
                    cloudVo.setDomain(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_UPLOAD_URL));
                    cloudVo.setAccessKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_ACCESS_KEY));
                    cloudVo.setSecretKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_SECRET_KEY));
                    cloudVo.setBucketName(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_STORAGE_NAME));
                    cloudVo.setRegion(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_STORAGE_REGION));
                    try {
                        Configuration cfg = new Configuration(Region.autoRegion());
                        UploadManager uploadManager = new UploadManager(cfg);
                        Auth auth = Auth.create(cloudVo.getAccessKey(), cloudVo.getSecretKey());
                        String upToken = auth.uploadToken(cloudVo.getBucketName());
                        qiNiuService.uploadFile(uploadManager, upToken, systemAttachment.getSattDir(), file.getPath(), file);
                    } catch (Exception e) {
                        logger.error("七牛云上传失败：" + e.getMessage());
                    }
                    break;
                case 3: // 阿里云OSS
                    systemAttachment.setImageType(3);
                    cloudVo.setDomain(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_UPLOAD_URL));
                    cloudVo.setAccessKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_ACCESS_KEY));
                    cloudVo.setSecretKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_SECRET_KEY));
                    cloudVo.setBucketName(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_STORAGE_NAME));
                    cloudVo.setRegion(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_STORAGE_REGION));
                    try {
                        ossService.upload(cloudVo, systemAttachment.getSattDir(), file.getPath(), file);
                    } catch (Exception e) {
                        logger.error("阿里云OSS上传失败：" + e.getMessage());
                    }
                    break;
                case 5: // 京东云
                    systemAttachment.setImageType(5);
                    String bucket = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_JD_BUCKET_NAME);
                    try {
                        jdCloudService.uploadFile(systemAttachment.getSattDir(), file.getPath(), bucket);
                    } catch (Exception e) {
                        logger.error("京东云上传失败：" + e.getMessage());
                    }
                    break;
            }

            // 保存附件记录
            systemAttachmentService.save(systemAttachment);

            // 如果不保存本地文件，删除本地文件
            if (!fileIsSave.equals("1") && file != null) {
                file.delete();
            }

            // 返回文件访问URL
            return systemAttachmentService.prefixFile(systemAttachment.getSattDir());
        } catch (Exception e) {
            logger.error("上传图片失败：{}", fileName, e);
            return null;
        }
    }
    
    /**
     * 验证商品导入数据
     */
    private void validateProductImportData(ProductImportVo data, int rowIndex) {
        if (StrUtil.isBlank(data.getName())) {
            throw new CrmebException("第" + rowIndex + "行：商品名称不能为空");
        }
        if (StrUtil.isBlank(data.getIntro())) {
            throw new CrmebException("第" + rowIndex + "行：商品简介不能为空");
        }
        // 旧的ID验证已移除，现在使用名称验证
        if (StrUtil.isBlank(data.getUnitName())) {
            throw new CrmebException("第" + rowIndex + "行：单位名不能为空");
        }
        if (StrUtil.isBlank(data.getImage())) {
            throw new CrmebException("第" + rowIndex + "行：商品主图不能为空");
        }

        // 验证必填的名称字段
        if (StrUtil.isBlank(data.getCategoryName())) {
            throw new CrmebException("第" + rowIndex + "行：平台分类名称不能为空");
        }
        if (StrUtil.isBlank(data.getMerCategoryName())) {
            throw new CrmebException("第" + rowIndex + "行：商户分类名称不能为空");
        }
        if (StrUtil.isBlank(data.getBrandName())) {
            throw new CrmebException("第" + rowIndex + "行：品牌名称不能为空");
        }
    }
    
    /**
     * 将商品基本信息和规格配置转换为ProductAddRequest
     */
    /**
     * 将商品基本信息和规格配置转换为ProductAddRequest
     */
    private ProductAddRequest convertToProductAddRequestWithSpecs(ProductImportVo basicInfo,
                                                                  List<ProductSpecConfigVo> specConfigs,
                                                                  Integer merId) {
        // 先使用基本信息创建请求
        ProductAddRequest request = convertToProductAddRequest(basicInfo, merId);

        // 判断是否为多规格商品：根据商品名称查询到的规格配置数量判断
        // 如果有多条规格配置，或者单条配置但不是"默认规格"，则认为是多规格
        boolean isMultiSpec = specConfigs.size() > 1 ||
                (specConfigs.size() == 1 && !"默认规格".equals(specConfigs.get(0).getSpecCombination()));

        if (isMultiSpec) {
            // 多规格商品处理
            request.setSpecType(true);

            // 解析规格属性
            parseAndSetSpecAttributes(request, specConfigs);

            // 创建规格值列表
            List<ProductAttrValueAddRequest> attrValueList = createAttrValueListFromSpecs(specConfigs);
            request.setAttrValueList(attrValueList);

        } else {
            // 单规格商品处理
            request.setSpecType(false);

            // 使用规格配置中的价格和库存信息（如果有的话）
            ProductSpecConfigVo singleSpec = specConfigs.get(0);
            updateRequestFromSingleSpec(request, singleSpec);

            // 创建默认规格值
            List<ProductAttrValueAddRequest> attrValueList = createAttrValueListFromSingleSpec(singleSpec, basicInfo);
            request.setAttrValueList(attrValueList);
        }

        return request;
    }
    /**
     * 从单规格配置创建规格值列表
     */
    private List<ProductAttrValueAddRequest> createAttrValueListFromSingleSpec(ProductSpecConfigVo singleSpec, ProductImportVo basicInfo) {
        List<ProductAttrValueAddRequest> attrValueList = new ArrayList<>();
        ProductAttrValueAddRequest attrValue = new ProductAttrValueAddRequest();

        // 单规格商品的attrValue也使用JSON格式
        if (StrUtil.isNotBlank(singleSpec.getSpecCombination()) && !"默认规格".equals(singleSpec.getSpecCombination())) {
            // 如果规格配置中有具体的规格组合，使用extractAttrValues处理
            attrValue.setAttrValue(extractAttrValues(singleSpec.getSpecCombination()));
        } else {
            // 默认规格
            attrValue.setAttrValue("{\"规格\":\"默认\"}");
        }

        attrValue.setImage(StrUtil.isNotBlank(singleSpec.getSpecImage()) ? singleSpec.getSpecImage() : basicInfo.getImage());

        // 价格信息（优先使用规格配置，否则使用基本信息）
        attrValue.setPrice(StrUtil.isNotBlank(singleSpec.getPrice()) ?
                new BigDecimal(singleSpec.getPrice()) : new BigDecimal(singleSpec.getPrice()));
        attrValue.setOtPrice(StrUtil.isNotBlank(singleSpec.getOtPrice()) ?
                new BigDecimal(singleSpec.getOtPrice()) : new BigDecimal(singleSpec.getOtPrice()));
        attrValue.setCost(StrUtil.isNotBlank(singleSpec.getCost()) ?
                new BigDecimal(singleSpec.getCost()) : new BigDecimal(singleSpec.getCost()));
        attrValue.setVipPrice(StrUtil.isNotBlank(singleSpec.getVipPrice()) ?
                new BigDecimal(singleSpec.getVipPrice()) : new BigDecimal(singleSpec.getVipPrice()));

        // 库存信息
        attrValue.setStock(StrUtil.isNotBlank(singleSpec.getStock()) ?
                Integer.parseInt(singleSpec.getStock()) : Integer.parseInt(singleSpec.getStock()));

        // 重量体积
        attrValue.setWeight(StrUtil.isNotBlank(singleSpec.getWeight()) ?
                new BigDecimal(singleSpec.getWeight()) : new BigDecimal(singleSpec.getWeight()));
        attrValue.setVolume(StrUtil.isNotBlank(singleSpec.getVolume()) ?
                new BigDecimal(singleSpec.getVolume()) : new BigDecimal(singleSpec.getVolume()));

        // 编码
        attrValue.setBarCode(StrUtil.isNotBlank(singleSpec.getBarCode()) ?
                singleSpec.getBarCode() : singleSpec.getBarCode());
        attrValue.setItemNumber(StrUtil.isNotBlank(singleSpec.getItemNumber()) ?
                singleSpec.getItemNumber() : singleSpec.getItemNumber());

        // 返佣信息
        attrValue.setBrokerage(StrUtil.isNotBlank(singleSpec.getBrokerage()) ?
                Double.valueOf(singleSpec.getBrokerage()).intValue() : 0);
        attrValue.setBrokerageTwo(StrUtil.isNotBlank(singleSpec.getBrokerageTwo()) ?
                Double.valueOf(singleSpec.getBrokerageTwo()).intValue() : 0);

        // 显示设置
        attrValue.setIsDefault(StrUtil.isNotBlank(singleSpec.getIsDefault()) ? Boolean.parseBoolean(singleSpec.getIsDefault()) : true);
        attrValue.setIsShow(StrUtil.isNotBlank(singleSpec.getIsShow()) ? Boolean.parseBoolean(singleSpec.getIsShow()) : true);

        attrValueList.add(attrValue);
        return attrValueList;
    }

    /**
     * 解析并设置规格属性
     */
    private void parseAndSetSpecAttributes(ProductAddRequest request, List<ProductSpecConfigVo> specConfigs) {
        // 使用LinkedHashMap保持属性顺序
        Map<String, Set<String>> attrNameToValuesMap = new LinkedHashMap<>();

        // 解析所有规格组合，提取规格属性名称和值
        for (ProductSpecConfigVo spec : specConfigs) {
            String specCombination = spec.getSpecCombination();
            if (StrUtil.isBlank(specCombination) || "默认规格".equals(specCombination)) {
                continue;
            }

            // 解析规格组合：属性名1:属性值1,属性名2:属性值2
            String[] specPairs = specCombination.split(",");
            for (String pair : specPairs) {
                String[] nameValue = pair.trim().split(":", 2);
                if (nameValue.length == 2) {
                    String attrName = nameValue[0].trim();
                    String attrValue = nameValue[1].trim();

                    if (StrUtil.isNotBlank(attrName) && StrUtil.isNotBlank(attrValue)) {
                        attrNameToValuesMap.computeIfAbsent(attrName, k -> new LinkedHashSet<>()).add(attrValue);
                    }
                }
            }
        }

        List<ProductAttrAddRequest> attrList = new ArrayList<>();
        int sort = 0;

        // 为每个属性名创建规格属性
        for (Map.Entry<String, Set<String>> entry : attrNameToValuesMap.entrySet()) {
            String attrName = entry.getKey();
            Set<String> attrValues = entry.getValue();

            ProductAttrAddRequest attr = new ProductAttrAddRequest();
            attr.setAttributeName(attrName); // 使用真实的属性名称，不再写死
            attr.setIsShowImage(false);
            attr.setSort(sort++);

            List<ProductAttrOptionAddRequest> options = new ArrayList<>();
            int optionSort = 0;
            for (String value : attrValues) {
                ProductAttrOptionAddRequest option = new ProductAttrOptionAddRequest();
                option.setOptionName(value);
                option.setImage(""); // 规格属性图片暂时为空
                option.setSort(optionSort++);
                options.add(option);
            }
            attr.setOptionList(options);
            attrList.add(attr);
        }

        request.setAttrList(attrList);
    }

    /**
     * 从规格配置创建规格值列表
     */
    private List<ProductAttrValueAddRequest> createAttrValueListFromSpecs(List<ProductSpecConfigVo> specConfigs) {
        List<ProductAttrValueAddRequest> attrValueList = new ArrayList<>();

        for (ProductSpecConfigVo spec : specConfigs) {
            ProductAttrValueAddRequest attrValue = new ProductAttrValueAddRequest();

            // 从规格组合中提取属性值（去除属性名，只保留属性值）
            String attrValueStr = extractAttrValues(spec.getSpecCombination());
            attrValue.setAttrValue(attrValueStr);
            attrValue.setImage(StrUtil.isNotBlank(spec.getSpecImage()) ? spec.getSpecImage() : "");

            // 价格信息
            attrValue.setPrice(StrUtil.isNotBlank(spec.getPrice()) ? new BigDecimal(spec.getPrice()) : BigDecimal.ZERO);
            attrValue.setOtPrice(StrUtil.isNotBlank(spec.getOtPrice()) ? new BigDecimal(spec.getOtPrice()) : BigDecimal.ZERO);
            attrValue.setCost(StrUtil.isNotBlank(spec.getCost()) ? new BigDecimal(spec.getCost()) : BigDecimal.ZERO);
            attrValue.setVipPrice(StrUtil.isNotBlank(spec.getVipPrice()) ? new BigDecimal(spec.getVipPrice()) : BigDecimal.ZERO);

            // 库存和重量体积
            attrValue.setStock(StrUtil.isNotBlank(spec.getStock()) ? Integer.parseInt(spec.getStock()) : 0);
            attrValue.setWeight(StrUtil.isNotBlank(spec.getWeight()) ? new BigDecimal(spec.getWeight()) : BigDecimal.ZERO);
            attrValue.setVolume(StrUtil.isNotBlank(spec.getVolume()) ? new BigDecimal(spec.getVolume()) : BigDecimal.ZERO);

            // 商品编码
            attrValue.setBarCode(StrUtil.isNotBlank(spec.getBarCode()) ? spec.getBarCode() : "");
            attrValue.setItemNumber(StrUtil.isNotBlank(spec.getItemNumber()) ? spec.getItemNumber() : "");

            // 返佣信息
            attrValue.setBrokerage(StrUtil.isNotBlank(spec.getBrokerage()) ?
                    Double.valueOf(spec.getBrokerage()).intValue() : 0);
            attrValue.setBrokerageTwo(StrUtil.isNotBlank(spec.getBrokerageTwo()) ?
                    Double.valueOf(spec.getBrokerageTwo()).intValue() : 0);

            // 显示设置
            attrValue.setIsDefault(StrUtil.isNotBlank(spec.getIsDefault()) ? Boolean.parseBoolean(spec.getIsDefault()) : false);
            attrValue.setIsShow(StrUtil.isNotBlank(spec.getIsShow()) ? Boolean.parseBoolean(spec.getIsShow()) : true);

            attrValueList.add(attrValue);
        }

        return attrValueList;
    }

    /**
     * 从规格组合中提取属性值，生成JSON格式
     * 输入：颜色:红色,尺寸:L
     * 输出：{"颜色":"红色","尺寸":"L"}
     */
    private String extractAttrValues(String specCombination) {
        if (StrUtil.isBlank(specCombination) || "默认规格".equals(specCombination)) {
            logger.debug("规格组合为空或默认规格，返回：默认JSON");
            return "{\"规格\":\"默认\"}";
        }

        Map<String, String> attrMap = new LinkedHashMap<>();
        String[] specPairs = specCombination.split(",");

        logger.debug("开始解析规格组合：{}，分割后有{}个属性对", specCombination, specPairs.length);

        for (String pair : specPairs) {
            String[] nameValue = pair.trim().split(":", 2);
            if (nameValue.length == 2) {
                String attrName = nameValue[0].trim();
                String attrValue = nameValue[1].trim();
                if (StrUtil.isNotBlank(attrName) && StrUtil.isNotBlank(attrValue)) {
                    attrMap.put(attrName, attrValue);
                    logger.debug("提取属性：{} = {}", attrName, attrValue);
                }
            } else {
                logger.warn("规格对格式错误，跳过：{}", pair);
            }
        }

        // 转换为JSON字符串
        try {
            String result = JSON.toJSONString(attrMap);
            logger.debug("最终生成的JSON格式属性值：{}", result);
            return result;
        } catch (Exception e) {
            logger.error("转换属性值为JSON格式失败：{}", e.getMessage());
            // 降级处理，返回简单格式
            return "{\"规格\":\"" + specCombination + "\"}";
        }
    }

    /**
     * 从单规格配置更新请求（注：价格相关字段在attrValueList中处理）
     */
    private void updateRequestFromSingleSpec(ProductAddRequest request, ProductSpecConfigVo singleSpec) {
        // ProductAddRequest中没有价格字段，这些都通过attrValueList处理
        // 这里处理一些基本配置信息的验证和更新

        if (StrUtil.isNotBlank(singleSpec.getProductName())) {
            logger.debug("处理单规格商品：{}", singleSpec.getProductName());

            // 验证商品名称是否匹配
            if (!singleSpec.getProductName().equals(request.getName())) {
                logger.warn("规格配置中的商品名称[{}]与基本信息中的商品名称[{}]不匹配",
                        singleSpec.getProductName(), request.getName());
            }

            // 对于单规格商品，设置规格类型为false
            request.setSpecType(false);

            // 如果规格配置中有默认选中状态，可以在这里处理一些显示相关的配置
            if (StrUtil.isNotBlank(singleSpec.getIsDefault()) && "true".equals(singleSpec.getIsDefault())) {
                logger.debug("单规格商品默认选中配置：{}", singleSpec.getIsDefault());
            }

            // 如果规格配置中有显示状态配置
            if (StrUtil.isNotBlank(singleSpec.getIsShow()) && "false".equals(singleSpec.getIsShow())) {
                logger.debug("单规格商品显示状态：{}", singleSpec.getIsShow());
            }

            // 记录处理信息
            logger.info("单规格商品[{}]配置处理完成", singleSpec.getProductName());
        }
    }

    /**
     * 转换为ProductAddRequest
     */
    private ProductAddRequest convertToProductAddRequest(ProductImportVo importVo, Integer merId) {
        ProductAddRequest request = new ProductAddRequest();

        // 基本信息
        request.setName(importVo.getName());
        request.setIntro(importVo.getIntro());
        request.setKeyword(StrUtil.isNotBlank(importVo.getKeyword()) ? importVo.getKeyword() : "");

        // 根据名称查找对应的ID
        Integer categoryId = findCategoryIdByName(importVo.getCategoryName());
        request.setCategoryId(categoryId);

        String cateId = findMerchantCategoryIdByName(importVo.getMerCategoryName(), merId);
        request.setCateId(cateId);

        Integer brandId = findBrandIdByName(importVo.getBrandName(), merId);
        request.setBrandId(brandId);

        request.setUnitName(importVo.getUnitName());
        request.setImage(importVo.getImage());
        request.setSliderImage(StrUtil.isNotBlank(importVo.getSliderImage()) ? importVo.getSliderImage() : importVo.getImage());
        request.setContent(StrUtil.isNotBlank(importVo.getContent()) ? importVo.getContent() : "");

        // 商品类型
        request.setType(StrUtil.isNotBlank(importVo.getType()) ? Integer.parseInt(importVo.getType()) : ProductConstants.PRODUCT_TYPE_NORMAL);

        // 佣金设置（这个字段还在基本信息中）
        Boolean isSub = StrUtil.isNotBlank(importVo.getIsSub()) ? Boolean.parseBoolean(importVo.getIsSub()) : false;

        // 配送设置
        String deliveryMethod = StrUtil.isNotBlank(importVo.getDeliveryMethod()) ? importVo.getDeliveryMethod() : "1";
        Boolean postageSwith = StrUtil.isNotBlank(importVo.getPostageSwith()) ? Boolean.parseBoolean(importVo.getPostageSwith()) : false;
        Boolean cityDeliverySwith = StrUtil.isNotBlank(importVo.getCityDeliverySwith()) ? Boolean.parseBoolean(importVo.getCityDeliverySwith()) : false;

        // 运费模板
        Integer tempId = findTempIdByName(importVo.getTempName(), merId);

        // 商品设置
        Boolean isPaidMember = StrUtil.isNotBlank(importVo.getIsPaidMember()) ? Boolean.parseBoolean(importVo.getIsPaidMember()) : false;
        Boolean refundSwitch = StrUtil.isNotBlank(importVo.getRefundSwitch()) ? Boolean.parseBoolean(importVo.getRefundSwitch()) : true;
        Boolean limitSwith = StrUtil.isNotBlank(importVo.getLimitSwith()) ? Boolean.parseBoolean(importVo.getLimitSwith()) : false;
        Integer limitNum = limitSwith && StrUtil.isNotBlank(importVo.getLimitNum()) ? Integer.parseInt(importVo.getLimitNum()) : 0;
        Integer minNum = StrUtil.isNotBlank(importVo.getMinNum()) ? Integer.parseInt(importVo.getMinNum()) : 1;

        // 系统设置
        Boolean isAutoUp = StrUtil.isNotBlank(importVo.getIsAutoUp()) ? Boolean.parseBoolean(importVo.getIsAutoUp()) : false;
        Boolean isAutoSubmitAudit = StrUtil.isNotBlank(importVo.getIsAutoSubmitAudit()) ? Boolean.parseBoolean(importVo.getIsAutoSubmitAudit()) : false;

        // 设置所有属性
        request.setSort(0);
        request.setSpecType(false); // 默认单规格，具体规格信息在规格配置表中处理
        request.setIsSub(isSub);
        request.setIsAutoUp(isAutoUp);
        request.setIsAutoSubmitAudit(isAutoSubmitAudit);
        request.setDeliveryMethod(deliveryMethod);
        request.setRefundSwitch(refundSwitch);
        request.setTempId(tempId);
        request.setSystemFormId(0); // 默认系统表单
        request.setLimitSwith(limitSwith);
        request.setLimitNum(limitNum);
        request.setMinNum(minNum);
        request.setPostageSwith(postageSwith);
        request.setCityDeliverySwith(cityDeliverySwith);
        request.setIsPaidMember(isPaidMember);
//        //地址设置
//        request.setProvince(importVo.getProvince());
//        request.setCity(importVo.getCity());
//        request.setArea(importVo.getArea());
//        request.setStreet(importVo.getStreet());
//        //获取地址ID
//        if(StringUtil.isNotEmpty(importVo.getProvince())){
//            request.setProvinceCode(findCityByName(importVo.getProvince(),null,null, null).getRegionId());
//        }
//        if(StringUtil.isNotEmpty(importVo.getCity())){
//            request.setCityCode(findCityByName(importVo.getProvince(),importVo.getCity(),null, null).getRegionId());
//        }
//        if(StringUtil.isNotEmpty(importVo.getArea())){
//            request.setAreaCode(findCityByName(importVo.getProvince(),importVo.getCity(),importVo.getArea(), null).getRegionId());
//        }
//        if(StringUtil.isNotEmpty(importVo.getStreet())){
//            request.setStreetCode(findCityByName(importVo.getProvince(),importVo.getCity(),importVo.getArea(),importVo.getStreet()).getRegionId());
//        }
        // 创建基础单规格（规格信息将在convertToProductAddRequestWithSpecs中重新设置）
        List<ProductAttrAddRequest> attrList = createBasicSingleSpecAttrs();
        request.setAttrList(attrList);

        // 创建基础属性值（价格和库存信息将在convertToProductAddRequestWithSpecs中重新设置）
        List<ProductAttrValueAddRequest> attrValueList = createBasicAttrValues();
        request.setAttrValueList(attrValueList);

        return request;
    }

    /**
     * 根据运费模板名称查找模板ID
     */
    private Integer findTempIdByName(String tempName, Integer merId) {
        if (StrUtil.isBlank(tempName)) {
            return 0; // 默认模板ID
        }

        try {
            // 查询运费模板
            LambdaQueryWrapper<ShippingTemplates> lqw = Wrappers.lambdaQuery();
            lqw.eq(ShippingTemplates::getName, tempName.trim());
            lqw.eq(ShippingTemplates::getMerId, merId);
            // lqw.eq(ShippingTemplates::getIsDel, false);
            lqw.last(" limit 1");
            ShippingTemplates template = shippingTemplatesService.getOne(lqw);
            return template != null ? template.getId() : 0;
        } catch (Exception e) {
            return 0; // 查找失败时返回默认值
        }
    }
    /**
     * 创建基础单规格属性（简化版本，规格信息将在后续处理中重新设置）
     */
    private List<ProductAttrAddRequest> createBasicSingleSpecAttrs() {
        List<ProductAttrAddRequest> attrList = CollUtil.newArrayList();
        ProductAttrAddRequest attr = new ProductAttrAddRequest();
        attr.setAttributeName("规格");
        attr.setIsShowImage(false);
        attr.setSort(0);

        List<ProductAttrOptionAddRequest> optionList = CollUtil.newArrayList();
        ProductAttrOptionAddRequest option = new ProductAttrOptionAddRequest();
        option.setOptionName("默认");
        option.setImage("");
        option.setSort(0);
        optionList.add(option);

        attr.setOptionList(optionList);
        attrList.add(attr);
        return attrList;
    }

    /**
     * 创建基础属性值（简化版本，具体规格值将在后续处理中重新设置）
     */
    private List<ProductAttrValueAddRequest> createBasicAttrValues() {
        List<ProductAttrValueAddRequest> attrValueList = CollUtil.newArrayList();
        ProductAttrValueAddRequest attrValue = new ProductAttrValueAddRequest();
        attrValue.setAttrValue("默认");
        // 设置默认值，具体值在规格配置中处理
        attrValue.setPrice(new BigDecimal("0.01"));
        attrValue.setOtPrice(new BigDecimal("0.01"));
        attrValue.setCost(BigDecimal.ZERO);
        attrValue.setVipPrice(new BigDecimal("0.01"));
        attrValue.setStock(0);
        attrValue.setWeight(BigDecimal.ZERO);
        attrValue.setVolume(BigDecimal.ZERO);
        attrValue.setBarCode("");
        attrValue.setItemNumber("");
        attrValue.setBrokerage(0);
        attrValue.setBrokerageTwo(0);
        attrValue.setIsShow(true);
        attrValue.setImage("");
        attrValueList.add(attrValue);
        return attrValueList;
    }
    /**
     * 根据分类名称查找分类ID
     */
    private Integer findCategoryIdByName(String categoryName) {
        if (StrUtil.isBlank(categoryName)) {
            return 1; // 默认分类ID
        }

        try {
            // 使用 LambdaQueryWrapper 查询平台分类
            LambdaQueryWrapper<ProductCategory> lqw = Wrappers.lambdaQuery();
            lqw.eq(ProductCategory::getName, categoryName.trim());
            lqw.eq(ProductCategory::getIsDel, false);
            lqw.eq(ProductCategory::getIsShow, true);
            lqw.last(" limit 1");
            ProductCategory category = productCategoryService.getOne(lqw);
            return category != null ? category.getId() : 1;
        } catch (Exception e) {
            return 1; // 查找失败时返回默认值
        }
    }

    /**
     * 根据商户分类名称查找分类ID
     */
    private String findMerchantCategoryIdByName(String merCategoryName, Integer merId) {
        if (StrUtil.isBlank(merCategoryName)) {
            return "1"; // 默认分类ID
        }

        try {
            // 解析多级分类，如：食品>零食>坚果
            String[] categoryPath = merCategoryName.split(">");
            String categoryName = categoryPath[categoryPath.length - 1].trim(); // 取最后一级的名称

            // 查询商户分类
            LambdaQueryWrapper<MerchantProductCategory> lqw = Wrappers.lambdaQuery();
            lqw.eq(MerchantProductCategory::getName, categoryName);
            lqw.eq(MerchantProductCategory::getMerId, merId);
            lqw.eq(MerchantProductCategory::getIsDel, false);
            lqw.eq(MerchantProductCategory::getIsShow, true);
            lqw.last(" limit 1");

            // 使用注入的 MerchantProductCategoryService
            MerchantProductCategory category = merchantProductCategoryService.getOne(lqw);
            return category != null ? category.getId().toString() : "1";
        } catch (Exception e) {
            return "1"; // 查找失败时返回默认值
        }
    }

    /**
     * 根据品牌名称查找品牌ID
     */
    private Integer findBrandIdByName(String brandName, Integer merId) {
        if (StrUtil.isBlank(brandName)) {
            return 1; // 默认品牌ID
        }

        try {
            // 查询品牌 - 优先查询审核通过的品牌
            LambdaQueryWrapper<ProductBrand> lqw = Wrappers.lambdaQuery();
            lqw.eq(ProductBrand::getName, brandName.trim());
            lqw.eq(ProductBrand::getIsDel, false);
            lqw.eq(ProductBrand::getIsShow, true);
            // 先查平台品牌（auditStatus = 1 且 applyMerId 为空）或者查该商户申请的品牌
            lqw.and(wrapper -> wrapper
                    .and(subWrapper -> subWrapper.eq(ProductBrand::getAuditStatus, 1).isNull(ProductBrand::getApplyMerId))
                    .or(subWrapper -> subWrapper.eq(ProductBrand::getApplyMerId, merId).eq(ProductBrand::getAuditStatus, 1))
            );
            lqw.last(" limit 1");
            ProductBrand brand = productBrandService.getOne(lqw);
            return brand != null ? brand.getId() : 1;
        } catch (Exception e) {
            return 1; // 查找失败时返回默认值
        }
    }



    /**
     * 为指定行处理图片数据
     */
    private void processImagesForRow(ProductImportVo data, int rowIndex, Map<String, String> imageMap) {
        // 处理商品主图（第10列）
        String mainImageKey = rowIndex + "_10";
        if (imageMap.containsKey(mainImageKey)) {
            String mainImageUrl = imageMap.get(mainImageKey);
            data.setImage(mainImageUrl);
            logger.info("第{}行设置主图：{}", rowIndex, mainImageUrl);
        }
        
        // 处理轮播图（第11列及后续列）- 支持多张图片
        List<String> sliderImageUrls = new ArrayList<>();
        
        // 检查第11列及后续可能的图片列（最多检查5列）
        for (int col = 11; col <= 15; col++) {
            String sliderImageKey = rowIndex + "_" + col;
            if (imageMap.containsKey(sliderImageKey)) {
                String sliderImageUrl = imageMap.get(sliderImageKey);
                sliderImageUrls.add(sliderImageUrl);
                logger.info("第{}行第{}列发现轮播图：{}", rowIndex, col, sliderImageUrl);
            }
        }
        
        // 如果有轮播图，转换为JSON格式
        if (!sliderImageUrls.isEmpty()) {
            data.setSliderImage(JSON.toJSONString(sliderImageUrls));
        }
    }
}
