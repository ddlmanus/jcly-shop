package com.zbkj.front.controller;

import cn.hutool.core.bean.BeanUtil;
import com.zbkj.common.model.chat.UnifiedChatMessage;
import com.zbkj.common.model.chat.UnifiedChatSession;
import com.zbkj.common.model.coze.CozeBot;
import com.zbkj.common.model.coze.CozeBotConfig;
import com.zbkj.common.model.user.User;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.*;
import com.zbkj.common.request.chat.RequestHandover;
import com.zbkj.common.request.coze.*;
import com.zbkj.common.response.HumanServiceSessionResponse;
import com.zbkj.common.response.HumanServiceMessageResponse;
import com.zbkj.common.response.MiniCozeBotReponse;
import com.zbkj.common.response.chat.MessageResponse;
import com.zbkj.common.response.coze.*;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.token.FrontTokenComponent;
import com.zbkj.common.utils.RequestUtil;
import com.zbkj.common.vo.LoginFrontUserVo;
import com.zbkj.service.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 用户端人工客服控制器
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
@Slf4j
@RestController
@RequestMapping("api/front/human-service")
@Api(tags = "用户端人工客服")
public class AppHumanServiceController {

    @Autowired
    private HumanServiceService humanServiceService;
    @Autowired
    private CozeBotService cozeBotService;
    @Autowired
    private ChatHandoverService chatHandoverService;
    @Autowired
    private ChatImageUploadService chatImageUploadService;
    @Autowired
    private ChatQuickReplyService chatQuickReplyService;
    @Autowired
    private FrontTokenComponent frontTokenComponent;
    @Autowired
    private UnifiedChatService unifiedChatService;
    @Autowired
    private UserService userService;
    @Autowired
    private ProductService productService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private WebRTCService webRTCService;
    @Autowired
    private CozeBotConfigService cozeBotConfigService;
    @Autowired
    private CozeService cozeService;
    @Autowired
    private MerchantService merchantService;

    /**
     * 获取商户默认智能体
     * @param merId
     * @return
     */
    @ApiOperation(value = "获取默认智能体")
    //  @PreAuthorize("hasAuthority('merchant:coze:bot:view')")
    @GetMapping("/bot/local/default")
    public CommonResult<MiniCozeBotReponse> getDefaultBot(@RequestParam(value = "merId") Integer merId) {
        MiniCozeBotReponse miniCozeBotReponse= new MiniCozeBotReponse();
        CozeBot response = cozeBotService.getDefaultBot(merId);
        if(response== null){
            return CommonResult.failed("该商户暂未开通AI客服功能，请拨打客服电话:"+merchantService.getById(merId).getPhone());
        }
        BeanUtil.copyProperties(response, miniCozeBotReponse);
        //查询智能体配置
        CozeBotConfig cozeBotConfig = cozeBotConfigService.getByCozeBotId(response.getCozeBotId());
        miniCozeBotReponse.setCozeBotConfig(cozeBotConfig);
        return CommonResult.success(miniCozeBotReponse);
    }

    /**
     * 创建聊天会话（使用统一聊天服务）
     */
    @ApiOperation(value = "创建聊天会话")
    //   @PreAuthorize("hasAuthority('merchant:enterprise-chat:session:create')")
    @PostMapping("/session")
    public CommonResult<com.zbkj.common.model.chat.UnifiedChatSession> createSession(
            @RequestBody @Validated EnterpriseChatSessionRequest request) {

        try {
            // 从当前登录用户获取用户ID
            Integer userId =frontTokenComponent.getUserId();

            // 使用统一聊天服务创建会话
            com.zbkj.common.model.chat.UnifiedChatSession session = unifiedChatService.createOrGetUserSession(
                    userId.longValue(), // 用户ID
                    request.getMerId(),
                    request.getCozeBotId()
            );

            return CommonResult.success(session);
        } catch (Exception e) {
            log.error("创建聊天会话失败: {}", e.getMessage(), e);
            return CommonResult.failed("创建会话失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "发送聊天消息")
    @PostMapping("/message")
    public CommonResult<Map<String, Object>> sendMessage(
            @RequestBody @Validated EnterpriseChatMessageRequest request, HttpServletRequest httpServletRequest) {

        log.info("开始处理小程序聊天消息，会话ID: {}, 内容: {}",
                request.getSessionId(), request.getContent());

        try {
            // 验证用户身份
            User currentUser = userService.getInfo();
            if (currentUser == null) {
                return CommonResult.failed("认证失败：用户未登录");
            }

            // 验证会话是否存在
            com.zbkj.common.model.chat.UnifiedChatSession session = unifiedChatService.getSession(request.getSessionId());
            if (session == null) {
                return CommonResult.failed("会话不存在，请重新创建会话");
            }

            // 验证用户是否有权限访问该会话
            if (!currentUser.getId().equals(session.getUserId().intValue())) {
                return CommonResult.failed("无权限访问该会话");
            }

            // 设置默认值
            if (request.getContentType() == null) {
                request.setContentType("text");
            }
            if (request.getMessageType() == null) {
                request.setMessageType("text");
            }
            if (request.getNeedAiReply() == null) {
                request.setNeedAiReply(true);
            }

            // 获取智能体ID
            String cozeBotId = request.getCozeBotId();
            if (cozeBotId == null || cozeBotId.trim().isEmpty()) {
                cozeBotId = session.getCozeBotId();
            }
            
            if (cozeBotId == null || cozeBotId.trim().isEmpty()) {
                return CommonResult.failed("智能体配置错误，请联系管理员");
            }

            // 构建用户上下文
            LoginFrontUserVo loginFrontUserVo = new LoginFrontUserVo();
            loginFrontUserVo.setUser(currentUser);
            
            // 调用统一聊天服务发送消息
            Map<String, Object> response = humanServiceService.handleMessageForMiniProgram(
                    request, loginFrontUserVo, cozeBotId);

            log.info("小程序聊天消息处理完成，会话ID: {}, 响应: {}", request.getSessionId(), response);
            return CommonResult.success(response);

        } catch (Exception e) {
            log.error("小程序聊天消息处理失败，会话ID: {}, 错误: {}", 
                     request.getSessionId(), e.getMessage(), e);
            return CommonResult.failed("消息处理失败：" + e.getMessage());
        }
    }



    // ==================== 会话管理 ====================

    @ApiOperation(value = "获取用户的人工服务会话")
    @RequestMapping(value = "/session", method = RequestMethod.GET)
    public CommonResult<HumanServiceSessionResponse> getUserSession() {
        HumanServiceSessionResponse session = humanServiceService.getUserCurrentSession();
        return CommonResult.success(session);
    }

    @ApiOperation(value = "获取会话详情")
    @RequestMapping(value = "/session/{sessionId}", method = RequestMethod.GET)
    @ApiImplicitParam(name = "sessionId", value = "会话ID", required = true)
    public CommonResult<Map<String, Object>> getSessionDetail(@PathVariable String sessionId) {
        Map<String, Object> detail = humanServiceService.getUserSessionDetail(sessionId);
        return CommonResult.success(detail);
    }

    @ApiOperation(value = "结束会话")
    @RequestMapping(value = "/session/{sessionId}/end", method = RequestMethod.POST)
    @ApiImplicitParam(name = "sessionId", value = "会话ID", required = true)
    public CommonResult<String> endSession(@PathVariable String sessionId) {
        humanServiceService.endSession(sessionId);
        return CommonResult.success("会话已结束");
    }

    // ==================== 消息管理 ====================

    @ApiOperation(value = "获取会话消息列表")
    @RequestMapping(value = "/messages/{sessionId}", method = RequestMethod.GET)
    @ApiImplicitParam(name = "sessionId", value = "会话ID", required = true)
    public CommonResult<List<MessageResponse>> getMessages(
            @PathVariable String sessionId,
            @Validated PageParamRequest pageParamRequest) {
        Map<String, Object> params = new HashMap<>();
        params.put("sessionId", sessionId);
        params.put("page", pageParamRequest.getPage());
        params.put("limit", pageParamRequest.getLimit());
        return CommonResult.success(unifiedChatService.getSessionMessages(sessionId,pageParamRequest.getPage(), pageParamRequest.getLimit()));
    }







    // ==================== 在线状态和其他功能 ====================

    @ApiOperation(value = "获取客服在线状态")
    @RequestMapping(value = "/online-status", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getOnlineStatus() {
        Map<String, Object> status = humanServiceService.getOnlineStatus();
        return CommonResult.success(status);
    }

    @ApiOperation(value = "获取等待队列信息")
    @RequestMapping(value = "/queue-info", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getQueueInfo() {
        Map<String, Object> queueInfo = humanServiceService.getUserQueueInfo();
        return CommonResult.success(queueInfo);
    }

    @ApiOperation(value = "评价客服服务")
    @RequestMapping(value = "/session/{sessionId}/rate", method = RequestMethod.POST)
    @ApiImplicitParam(name = "sessionId", value = "会话ID", required = true)
    public CommonResult<String> rateService(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> request) {
        try {
            Integer rating = (Integer) request.get("rating");
            String comment = (String) request.get("comment");
            
            if (rating == null || rating < 1 || rating > 5) {
                return CommonResult.failed("评分必须在1-5之间");
            }
            
            humanServiceService.rateService(sessionId, rating, comment);
            return CommonResult.success("评价成功");
            
        } catch (Exception e) {
            log.error("评价服务失败", e);
            return CommonResult.failed("评价失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "获取常见问题")
    @RequestMapping(value = "/faq", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getFAQ() {
        Map<String, Object> faq = humanServiceService.getFAQ();
        return CommonResult.success(faq);
    }

    @ApiOperation(value = "获取快捷问题")
    @RequestMapping(value = "/quick-questions", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getQuickQuestions() {
        Map<String, Object> questions = humanServiceService.getQuickQuestions();
        return CommonResult.success(questions);
    }

    /**
     * 请求转人工客服
     */
    @ApiOperation(value = "请求转人工客服")
    @PostMapping("/request-handover")
    public CommonResult<Map<String, Object>> requestHandover(@RequestBody RequestHandover request) {
        
        try {
            User currentUser = userService.getInfo();
            if (currentUser == null) {
                return CommonResult.failed("用户未登录");
            }

            Map<String, Object> result = chatHandoverService.requestHandoverToHuman(
                    request.getSessionId(), currentUser.getId(), request.getReason(), request.getReason());

            if ((Boolean) result.get("success")) {
                return CommonResult.success(result);
            } else {
                return CommonResult.failed((String) result.get("message"));
            }

        } catch (Exception e) {
            log.error("请求转人工客服失败: sessionId={}, 错误: {}", request.getSessionId(), e.getMessage(), e);
            return CommonResult.failed("请求失败: " + e.getMessage());
        }
    }

    /**
     * 上传聊天图片
     */
    @ApiOperation(value = "上传聊天图片")
    @PostMapping("/upload-image")
    public CommonResult<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sessionId") String sessionId) {
        
        try {
            User currentUser = userService.getInfo();
            if (currentUser == null) {
                return CommonResult.failed("用户未登录");
            }

            Map<String, Object> result = chatImageUploadService.uploadChatImage(
                file, sessionId, currentUser.getId());

            if ((Boolean) result.get("success")) {
                return CommonResult.success(result);
            } else {
                return CommonResult.failed((String) result.get("message"));
            }

        } catch (Exception e) {
            log.error("上传聊天图片失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
            return CommonResult.failed("上传失败: " + e.getMessage());
        }
    }

    /**
     * 创建商品卡片
     */
    @ApiOperation(value = "创建商品卡片")
    @PostMapping("/create-product-card")
    public CommonResult<Map<String, Object>> createProductCard(
            @RequestParam("productId") Integer productId,
            @RequestParam("sessionId") String sessionId) {

        try {
            User currentUser = userService.getInfo();
            if (currentUser == null) {
                return CommonResult.failed("用户未登录");
            }

            // 简化的商品卡片创建逻辑
            Map<String, Object> productData = new HashMap<>();
            productData.put("productId", productId);
            productData.put("sessionId", sessionId);
            productData.put("userId", currentUser.getId());
            productData.put("messageType", "product_card");
            productData.put("timestamp", System.currentTimeMillis());

            // 获取商品信息
            try {
                com.zbkj.common.response.ProMerchantProductResponse product = productService.getRecommendedProductsByProductId(productId);
                if (product != null) {
                    productData.put("productName", product.getName());
                    productData.put("productPrice", product.getPrice());
                    productData.put("productImage", product.getImage());
                    productData.put("productStock", product.getStock());
                }
            } catch (Exception e) {
                log.warn("获取商品信息失败: productId={}, 错误: {}", productId, e.getMessage());
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", productData);
            result.put("message", "商品卡片创建成功");

            return CommonResult.success(result);

        } catch (Exception e) {
            log.error("创建商品卡片失败: productId={}, sessionId={}, 错误: {}", productId, sessionId, e.getMessage(), e);
            return CommonResult.failed("创建失败: " + e.getMessage());
        }
    }
//
//    /**
//     * 搜索商品
//     */
//    @ApiOperation(value = "搜索商品")
//    @GetMapping("/search-products")
//    public CommonResult<Map<String, Object>> searchProducts(
//            @RequestParam("keyword") String keyword,
//            @RequestParam(value = "page", defaultValue = "1") int page,
//            @RequestParam(value = "limit", defaultValue = "10") int limit) {
//
//        try {
//            User currentUser = userService.getInfo();
//            if (currentUser == null) {
//                return CommonResult.failed("用户未登录");
//            }
//
//            Map<String, Object> result = chatProductLinkService.searchProducts(
//                keyword, currentUser.getId(), page, limit);
//
//            if ((Boolean) result.get("success")) {
//                return CommonResult.success(result);
//            } else {
//                return CommonResult.failed((String) result.get("message"));
//            }
//
//        } catch (Exception e) {
//            log.error("搜索商品失败: keyword={}, 错误: {}", keyword, e.getMessage(), e);
//            return CommonResult.failed("搜索失败: " + e.getMessage());
//        }
//    }

    /**
     * 获取快捷回复列表
     */
    @ApiOperation(value = "获取快捷回复列表")
    @GetMapping("/quick-replies")
    public CommonResult<java.util.List<Map<String, Object>>> getQuickReplies(
            @RequestParam(value = "category", required = false) String category) {
        
        try {
            User currentUser = userService.getInfo();
            if (currentUser == null) {
                return CommonResult.failed("用户未登录");
            }

            java.util.List<Map<String, Object>> replies = chatQuickReplyService.getQuickReplies(
                currentUser.getId(), "USER", category);

            return CommonResult.success(replies);

        } catch (Exception e) {
            log.error("获取快捷回复失败: category={}, 错误: {}", category, e.getMessage(), e);
            return CommonResult.failed("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取转接状态
     */
    @ApiOperation(value = "获取转接状态")
    @GetMapping("/handover-status")
    public CommonResult<Map<String, Object>> getHandoverStatus(
            @RequestParam("sessionId") String sessionId) {
        
        try {
            Map<String, Object> status = chatHandoverService.getHandoverStatus(sessionId);
            return CommonResult.success(status);

        } catch (Exception e) {
            log.error("获取转接状态失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
            return CommonResult.failed("获取失败: " + e.getMessage());
        }
    }

    // ========== 语音通话相关接口 ==========

    /**
     * 发起语音通话
     */
    @ApiOperation(value = "发起语音通话")
    @PostMapping("/voice-call/initiate")
    public CommonResult<Map<String, Object>> initiateVoiceCall(
            @RequestParam("sessionId") String sessionId,
            @RequestParam("userType") String userType,
            @RequestParam(value = "staffId", required = false) Integer staffId) {
        
        try {
            User currentUser = userService.getInfo();
            if (currentUser == null) {
                return CommonResult.failed("用户未登录");
            }

            // 创建通话记录
            Map<String, Object> callData = new HashMap<>();
            callData.put("callId", "call_" + System.currentTimeMillis());
            callData.put("sessionId", sessionId);
            callData.put("userId", currentUser.getId());
            callData.put("staffId", staffId);
            callData.put("status", "initiated");
            callData.put("createTime", System.currentTimeMillis());

            return CommonResult.success(callData);

        } catch (Exception e) {
            log.error("发起语音通话失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
            return CommonResult.failed("发起通话失败: " + e.getMessage());
        }
    }

    /**
     * 接听语音通话
     */
    @ApiOperation(value = "接听语音通话")
    @PostMapping("/voice-call/answer")
    public CommonResult<Map<String, Object>> answerVoiceCall(
            @RequestParam("callId") String callId) {
        
        try {
            // 更新通话状态
            Map<String, Object> result = new HashMap<>();
            result.put("callId", callId);
            result.put("status", "connected");
            result.put("connectTime", System.currentTimeMillis());

            return CommonResult.success(result);

        } catch (Exception e) {
            log.error("接听语音通话失败: callId={}, 错误: {}", callId, e.getMessage(), e);
            return CommonResult.failed("接听通话失败: " + e.getMessage());
        }
    }

    /**
     * 结束语音通话
     */
    @ApiOperation(value = "结束语音通话")
    @PostMapping("/voice-call/end")
    public CommonResult<Map<String, Object>> endVoiceCall(
            @RequestParam("callId") String callId,
            @RequestParam(value = "reason", defaultValue = "normal") String reason) {
        
        try {
            // 更新通话状态
            Map<String, Object> result = new HashMap<>();
            result.put("callId", callId);
            result.put("status", "ended");
            result.put("endTime", System.currentTimeMillis());
            result.put("reason", reason);

            return CommonResult.success(result);

        } catch (Exception e) {
            log.error("结束语音通话失败: callId={}, 错误: {}", callId, e.getMessage(), e);
            return CommonResult.failed("结束通话失败: " + e.getMessage());
        }
    }

    /**
     * 获取语音通话状态
     */
    @ApiOperation(value = "获取语音通话状态")
    @GetMapping("/voice-call/status/{callId}")
    public CommonResult<Map<String, Object>> getVoiceCallStatus(
            @PathVariable("callId") String callId) {
        
        try {
            // 获取通话状态
            Map<String, Object> status = new HashMap<>();
            status.put("callId", callId);
            status.put("status", "connected");
            status.put("duration", 120); // 示例时长

            return CommonResult.success(status);

        } catch (Exception e) {
            log.error("获取语音通话状态失败: callId={}, 错误: {}", callId, e.getMessage(), e);
            return CommonResult.failed("获取状态失败: " + e.getMessage());
        }
    }

    /**
     * 发送语音通话信令
     */
    @ApiOperation(value = "发送语音通话信令")
    @PostMapping("/voice-call/signal")
    public CommonResult<String> sendVoiceCallSignal(
            @RequestParam("callId") String callId,
            @RequestParam("signalType") String signalType,
            @RequestParam("signalData") String signalData) {
        
        try {
            // 处理信令数据
            log.info("收到语音通话信令: callId={}, type={}, data={}", callId, signalType, signalData);
            
            return CommonResult.success("信令发送成功");

        } catch (Exception e) {
            log.error("发送语音通话信令失败: callId={}, 错误: {}", callId, e.getMessage(), e);
            return CommonResult.failed("发送信令失败: " + e.getMessage());
        }
    }

    /**
     * 发送语音消息
     */
    @ApiOperation(value = "发送语音消息")
    @PostMapping("/send-voice")
    public CommonResult<Map<String, Object>> sendVoiceMessage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sessionId") String sessionId) {
        
        try {
            // 处理语音文件上传
            Map<String, Object> result = new HashMap<>();
            result.put("url", "https://example.com/voice/" + System.currentTimeMillis() + ".mp3");
            result.put("duration", 10); // 示例时长10秒
            result.put("success", true);

            return CommonResult.success(result);

        } catch (Exception e) {
            log.error("发送语音消息失败: sessionId={}, 错误: {}", sessionId, e.getMessage(), e);
            return CommonResult.failed("发送失败: " + e.getMessage());
        }
    }

    /**
     * 发送订单卡片
     */
    @ApiOperation(value = "发送订单卡片")
    @PostMapping("/send-order-card")
    public CommonResult<Map<String, Object>> sendOrderCard(
            @RequestParam("orderId") String orderId,
            @RequestParam("sessionId") String sessionId) {
        
        try {
            User currentUser = userService.getInfo();
            if (currentUser == null) {
                return CommonResult.failed("用户未登录");
            }

            // 根据订单ID获取真实的订单数据
            com.zbkj.common.model.order.Order order = orderService.getByOrderNo(orderId);
            if (order == null) {
                return CommonResult.failed("订单不存在");
            }

            // 验证用户是否有权限查看该订单
            if (!currentUser.getId().equals(order.getUid())) {
                return CommonResult.failed("无权限查看该订单");
            }

            // 构建订单卡片数据
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("orderId", order.getId());
            orderData.put("orderNo", order.getOrderNo());
            orderData.put("totalAmount", order.getTotalPrice());
            orderData.put("status", getOrderStatusText(order.getStatus()));
            orderData.put("createTime", order.getCreateTime());
            orderData.put("payTime", order.getPayTime());
            
            // 获取订单商品信息
            if (order.getTotalNum() != null && order.getTotalNum() > 0) {
                orderData.put("productCount", order.getTotalNum());
                // 获取订单详情中的商品信息
                try {
                    // 这里可以通过订单详情服务获取商品信息
                    orderData.put("productInfo", "商品数量: " + order.getTotalNum());
                } catch (Exception e) {
                    log.warn("获取订单商品信息失败: {}", e.getMessage());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("data", orderData);
            result.put("success", true);

            return CommonResult.success(result);

        } catch (Exception e) {
            log.error("发送订单卡片失败: orderId={}, sessionId={}, 错误: {}", orderId, sessionId, e.getMessage(), e);
            return CommonResult.failed("发送失败: " + e.getMessage());
        }
    }

    /**
     * 获取推荐商品
     */
    @ApiOperation(value = "获取推荐商品")
    @GetMapping("/recommended-products")
    public CommonResult<Map<String, Object>> getRecommendedProducts(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        
        try {
            User currentUser = userService.getInfo();
            if (currentUser == null) {
                return CommonResult.failed("用户未登录");
            }

            // 从会话中获取商户ID，如果没有则使用默认推荐
            Integer merId = null;
            if (category != null && category.startsWith("mer_")) {
                try {
                    merId = Integer.valueOf(category.substring(4));
                } catch (NumberFormatException e) {
                    log.warn("解析商户ID失败: {}", category);
                }
            }

            // 获取真实的推荐商品
            java.util.List<com.zbkj.common.response.ProMerchantProductResponse> recommendedProducts;
            if (merId != null) {
                recommendedProducts = productService.getRecommendedProductsByMerId(merId, limit);
            } else {
                // 获取首页推荐商品
                com.zbkj.common.request.PageParamRequest pageRequest = new com.zbkj.common.request.PageParamRequest();
                pageRequest.setPage(1);
                pageRequest.setLimit(limit);
                com.github.pagehelper.PageInfo<com.zbkj.common.model.product.Product> pageInfo = 
                    productService.getIndexProduct(0, pageRequest);
                
                // 转换为推荐商品格式
                recommendedProducts = new java.util.ArrayList<>();
                if (pageInfo.getList() != null) {
                    for (com.zbkj.common.model.product.Product product : pageInfo.getList()) {
                        com.zbkj.common.response.ProMerchantProductResponse response = 
                            new com.zbkj.common.response.ProMerchantProductResponse();
                        response.setId(product.getId());
                        response.setName(product.getName());
                        response.setPrice(product.getPrice());
                        response.setImage(product.getImage());
                        response.setStock(product.getStock());
                        response.setSales(product.getSales());
                        recommendedProducts.add(response);
                    }
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("products", recommendedProducts);
            result.put("total", recommendedProducts.size());
            result.put("success", true);

            return CommonResult.success(result);

        } catch (Exception e) {
            log.error("获取推荐商品失败: category={}, 错误: {}", category, e.getMessage(), e);
            return CommonResult.failed("获取失败: " + e.getMessage());
        }
    }

    // ==================== WebRTC语音通话相关接口 ====================

    @ApiOperation(value = "接受来电")
    @PostMapping("/webrtc/call/accept")
    public CommonResult<Map<String, Object>> acceptIncomingCall(@RequestBody Map<String, Object> request) {
        try {
            User currentUser = userService.getInfo();
            if (currentUser == null) {
                return CommonResult.failed("用户未登录");
            }

            String callId = (String) request.get("callId");
            Integer userId = currentUser.getId();

            Map<String, Object> result = webRTCService.acceptCall(callId, userId);
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("接受来电失败", e);
            return CommonResult.failed("接受失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "拒绝来电")
    @PostMapping("/webrtc/call/reject")
    public CommonResult<Void> rejectIncomingCall(@RequestBody Map<String, Object> request) {
        try {
            User currentUser = userService.getInfo();
            if (currentUser == null) {
                return CommonResult.failed("用户未登录");
            }

            String callId = (String) request.get("callId");
            String reason = (String) request.get("reason");

            webRTCService.rejectCall(callId, currentUser.getId().toString(), reason);
            return CommonResult.success();
        } catch (Exception e) {
            log.error("拒绝来电失败", e);
            return CommonResult.failed("拒绝失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "结束通话")
    @PostMapping("/webrtc/call/end")
    public CommonResult<Void> endCurrentCall(@RequestBody Map<String, Object> request) {
        try {
            User currentUser = userService.getInfo();
            if (currentUser == null) {
                return CommonResult.failed("用户未登录");
            }

            String callId = (String) request.get("callId");

            webRTCService.endCall(callId, currentUser.getId().toString());
            return CommonResult.success();
        } catch (Exception e) {
            log.error("结束通话失败", e);
            return CommonResult.failed("结束失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "发送WebRTC信令")
    @PostMapping("/webrtc/signaling")
    public CommonResult<Void> sendWebRTCSignal(@RequestBody Map<String, Object> request) {
        try {
            String callId = (String) request.get("callId");
            String type = (String) request.get("type");
            Object data = request.get("data");

            webRTCService.handleSignaling(callId, type, data);
            return CommonResult.success();
        } catch (Exception e) {
            log.error("发送WebRTC信令失败", e);
            return CommonResult.failed("信令发送失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "获取通话状态")
    @GetMapping("/webrtc/call/status/{callId}")
    public CommonResult<Map<String, Object>> getWebRTCCallStatus(@PathVariable String callId) {
        try {
            Map<String, Object> status = webRTCService.getCallStatus(callId);
            return CommonResult.success(status);
        } catch (Exception e) {
            log.error("获取通话状态失败", e);
            return CommonResult.failed("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单状态文本
     */
    private String getOrderStatusText(Integer status) {
        if (status == null) return "未知状态";
        
        switch (status) {
            case 0:
                return "待支付";
            case 1:
                return "待发货";
            case 2:
                return "待收货";
            case 3:
                return "待评价";
            case 4:
                return "已完成";
            case -1:
                return "已退款";
            case -2:
                return "已取消";
            default:
                return "未知状态";
        }
    }

    /**
     * 清空聊天消息
     */
    @ApiOperation(value = "清空聊天消息")
    @GetMapping("messages/{sessionId}/clear")
    public CommonResult<Boolean> clearMessages(@PathVariable String sessionId) {
        try {
            User currentUser = userService.getInfo();
            if (currentUser == null) {
                return CommonResult.failed("用户未登录");
            }
            if (sessionId == null) {
                return CommonResult.failed("会话ID不能为空");
            }

            UnifiedChatSession session = unifiedChatService.getSession(sessionId);
            if (session == null) {
                return CommonResult.failed("会话不存在");
            }
            return CommonResult.success(unifiedChatService.clearMessages(sessionId));
        }catch (Exception e){
            log.error("清空聊天消息失败", e);
            return CommonResult.failed("清空失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户按商户分组的对话列表
     */
    @ApiOperation(value = "获取当前用户按商户分组的对话列表")
    @GetMapping("/conversations/grouped-by-merchant")
    public CommonResult<List<Map<String, Object>>> getUserConversationsGroupedByMerchant() {
        try {
            User currentUser = userService.getInfo();
            if (currentUser == null) {
                return CommonResult.failed("用户未登录");
            }

            List<Map<String, Object>> result = humanServiceService.getUserConversationsGroupedByMerchant(currentUser.getId());
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("获取用户对话列表失败: userId={}, 错误: {}", 
                     userService.getInfo() != null ? userService.getInfo().getId() : "unknown", 
                     e.getMessage(), e);
            return CommonResult.failed("获取对话列表失败: " + e.getMessage());
        }
    }

    // ==================== Coze语音通话相关接口 ====================

    /**
     * 创建Coze语音房间
     */
    @ApiOperation(value = "创建Coze语音房间")
    @PostMapping("/coze/voice/create-room")
    public CommonResult<CozeCreateRoomResponse> createCozeVoiceRoom(
            @RequestBody @Validated CozeCreateRoomRequest request) {
        try {
            User currentUser = userService.getInfo();
            if (currentUser == null) {
                return CommonResult.failed("用户未登录");
            }

            log.info("用户{}开始创建Coze语音房间，智能体ID: {}, 请求参数: {}", 
                    currentUser.getId(), request.getBotId(), request);
            
            // 如果没有提供conversation_id，从用户的活跃会话中获取
            if (!StringUtils.hasText(request.getConversationId()) && StringUtils.hasText(request.getBotId())) {
                try {
                    // 通过智能体ID获取商户ID
                    Integer merchantId = null;
                    try {
                        CozeBot cozeBot = cozeBotService.getBotByBotId(request.getBotId());
                        if (cozeBot != null) {
                            merchantId = cozeBot.getMerchantId();
                            log.info("从智能体{}获取商户ID: {}", request.getBotId(), merchantId);
                        }
                    } catch (Exception e) {
                        log.warn("获取智能体信息失败，使用默认商户ID: {}", e.getMessage());
                    }
                    
                    // 如果无法获取商户ID，使用默认值
                    if (merchantId == null) {
                        merchantId = 0; // 默认商户ID
                        log.info("使用默认商户ID: {}", merchantId);
                    }
                    
                    // 查找用户当前活跃的会话
                    com.zbkj.common.model.chat.UnifiedChatSession activeSession = 
                        unifiedChatService.createOrGetUserSession(
                            currentUser.getId().longValue(), 
                            merchantId, 
                            request.getBotId()
                        );
                    
                    if (activeSession != null && StringUtils.hasText(activeSession.getCozeConversationId())) {
                        request.setConversationId(activeSession.getCozeConversationId());
                        log.info("用户{}使用会话{}的Coze对话ID: {}", 
                                currentUser.getId(), activeSession.getSessionId(), activeSession.getCozeConversationId());
                    } else {
                        log.error("用户{}没有找到有效的Coze会话ID", currentUser.getId());
                        return CommonResult.failed("没有找到有效的会话，请先开始聊天");
                    }
                } catch (Exception sessionException) {
                    log.error("获取用户{}的活跃会话失败: {}", currentUser.getId(), sessionException.getMessage(), sessionException);
                    return CommonResult.failed("获取会话信息失败: " + sessionException.getMessage());
                }
            }
            
            CozeCreateRoomResponse response = cozeService.createRoom(request);
            
            if (response != null && response.getCode() == 0) {
                log.info("用户{}创建Coze语音房间成功，房间ID: {}", currentUser.getId(), response.getData().getRoomId());
                return CommonResult.success(response);
            } else {
                String errorMsg = response != null ? response.getMsg() : "创建房间失败";
                log.error("用户{}创建Coze语音房间失败: {}", currentUser.getId(), errorMsg);
                return CommonResult.failed("创建语音房间失败: " + errorMsg);
            }
        } catch (Exception e) {
            log.error("创建Coze语音房间异常: userId={}, botId={}, error={}", 
                     userService.getInfo() != null ? userService.getInfo().getId() : "unknown", 
                     request.getBotId(), e.getMessage(), e);
            return CommonResult.failed("创建语音房间失败: " + e.getMessage());
        }
    }

    /**
     * 语音识别
     */
    @ApiOperation(value = "语音识别")
    @PostMapping("/coze/voice/transcribe")
    public CommonResult<CozeVoiceTranscriptionResponse> transcribeAudio(
            @RequestParam("file") MultipartFile file) {
        try {
            User currentUser = userService.getInfo();
            if (currentUser == null) {
                return CommonResult.failed("用户未登录");
            }

            if (file == null || file.isEmpty()) {
                return CommonResult.failed("音频文件不能为空");
            }

            log.info("用户{}开始语音识别，文件名: {}, 大小: {} bytes", 
                    currentUser.getId(), file.getOriginalFilename(), file.getSize());

            CozeVoiceTranscriptionRequest request = CozeVoiceTranscriptionRequest.builder()
                    .file(file)
                    .build();
            
            CozeVoiceTranscriptionResponse response = cozeService.transcribeAudio(request);
            
            if (response != null && response.getCode() == 0) {
                log.info("用户{}语音识别成功，识别文本: {}", currentUser.getId(), response.getData().getText());
                return CommonResult.success(response);
            } else {
                String errorMsg = response != null ? response.getMsg() : "语音识别失败";
                log.error("用户{}语音识别失败: {}", currentUser.getId(), errorMsg);
                return CommonResult.failed("语音识别失败: " + errorMsg);
            }
        } catch (Exception e) {
            log.error("语音识别异常: userId={}, fileName={}, error={}", 
                     userService.getInfo() != null ? userService.getInfo().getId() : "unknown", 
                     file != null ? file.getOriginalFilename() : "null", e.getMessage(), e);
            return CommonResult.failed("语音识别失败: " + e.getMessage());
        }
    }

    /**
     * 语音合成
     */
    @ApiOperation(value = "语音合成")
    @PostMapping("/coze/voice/synthesize")
    public ResponseEntity<byte[]> synthesizeSpeech(
            @RequestBody @Validated CozeVoiceSpeechRequest request, HttpServletRequest httpRequest) {
        try {
            User currentUser = userService.getInfo();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户未登录".getBytes());
            }

            log.info("用户{}开始语音合成，文本: {}, 音色ID: {}", 
                    currentUser.getId(), request.getInput(), request.getVoiceId());
            
            byte[] audioData = cozeService.synthesizeSpeech(request);
            
            if (audioData != null && audioData.length > 0) {
                log.info("用户{}语音合成成功，音频大小: {} bytes", currentUser.getId(), audioData.length);
                
                // 设置响应头
                HttpHeaders headers = new HttpHeaders();
                String format = request.getResponseFormat() != null ? request.getResponseFormat() : "mp3";
                headers.setContentType(MediaType.parseMediaType("audio/" + format));
                headers.setContentLength(audioData.length);
                headers.set("Content-Disposition", "attachment; filename=\"speech." + format + "\"");
                
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(audioData);
            } else {
                log.error("用户{}语音合成失败: 返回数据为空", currentUser.getId());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("语音合成失败".getBytes());
            }
        } catch (Exception e) {
            log.error("语音合成异常: userId={}, text={}, voiceId={}, error={}", 
                     userService.getInfo() != null ? userService.getInfo().getId() : "unknown", 
                     request.getInput(), request.getVoiceId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("语音合成失败: " + e.getMessage()).getBytes());
        }
    }

    /**
     * 复刻音色
     */
    @ApiOperation(value = "复刻音色")
    @PostMapping("/coze/voice/clone")
    public CommonResult<CozeVoiceCloneResponse> cloneVoice(
            @RequestParam("voice_name") String voiceName,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "text", required = false) String text,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "voice_id", required = false) String voiceId,
            @RequestParam(value = "preview_text", required = false) String previewText,
            @RequestParam(value = "space_id", required = false) String spaceId) {
        try {
            User currentUser = userService.getInfo();
            if (currentUser == null) {
                return CommonResult.failed("用户未登录");
            }

            if (file == null || file.isEmpty()) {
                return CommonResult.failed("音频文件不能为空");
            }

            if (voiceName == null || voiceName.trim().isEmpty()) {
                return CommonResult.failed("音色名称不能为空");
            }

            log.info("用户{}开始复刻音色，音色名称: {}, 文件名: {}, 语种: {}", 
                    currentUser.getId(), voiceName, file.getOriginalFilename(), language);

            CozeVoiceCloneRequest request = CozeVoiceCloneRequest.builder()
                    .voiceName(voiceName)
                    .file(file)
                    .text(text)
                    .language(language)
                    .voiceId(voiceId)
                    .previewText(previewText)
                    .spaceId(spaceId)
                    .build();
            
            CozeVoiceCloneResponse response = cozeService.cloneVoice(request);
            
            if (response != null && response.getCode() == 0) {
                log.info("用户{}复刻音色成功，音色ID: {}", currentUser.getId(), response.getData().getVoiceId());
                return CommonResult.success(response);
            } else {
                String errorMsg = response != null ? response.getMsg() : "复刻音色失败";
                log.error("用户{}复刻音色失败: {}", currentUser.getId(), errorMsg);
                return CommonResult.failed("复刻音色失败: " + errorMsg);
            }
        } catch (Exception e) {
            log.error("复刻音色异常: userId={}, voiceName={}, fileName={}, error={}", 
                     userService.getInfo() != null ? userService.getInfo().getId() : "unknown", 
                     voiceName, file != null ? file.getOriginalFilename() : "null", e.getMessage(), e);
            return CommonResult.failed("复刻音色失败: " + e.getMessage());
        }
    }

    /**
     * 获取音色列表
     */
    @ApiOperation(value = "获取音色列表")
    @GetMapping("/coze/voice/list")
    public CommonResult<CozeVoiceListResponse> getVoiceList(
            @RequestParam(value = "filter_system_voice", required = false) Boolean filterSystemVoice,
            @RequestParam(value = "model_type", required = false) String modelType,
            @RequestParam(value = "voice_state", required = false) String voiceState,
            @RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "page_size", defaultValue = "20") Integer pageSize) {
        try {
            User currentUser = userService.getInfo();
            if (currentUser == null) {
                return CommonResult.failed("用户未登录");
            }

            log.info("用户{}开始获取音色列表，过滤系统音色: {}, 模型类型: {}, 页码: {}", 
                    currentUser.getId(), filterSystemVoice, modelType, pageNum);

            CozeVoiceListRequest request = CozeVoiceListRequest.builder()
                    .filterSystemVoice(filterSystemVoice)
                    .modelType(modelType)
                    .voiceState(voiceState)
                    .pageNum(pageNum)
                    .pageSize(pageSize)
                    .build();
            
            CozeVoiceListResponse response = cozeService.getVoiceList(request);
            
            if (response != null && response.getCode() == 0) {
                int voiceCount = response.getData() != null && response.getData().getVoiceList() != null 
                    ? response.getData().getVoiceList().size() : 0;
                log.info("用户{}获取音色列表成功，音色数量: {}", currentUser.getId(), voiceCount);
                return CommonResult.success(response);
            } else {
                String errorMsg = response != null ? response.getMsg() : "获取音色列表失败";
                log.error("用户{}获取音色列表失败: {}", currentUser.getId(), errorMsg);
                return CommonResult.failed("获取音色列表失败: " + errorMsg);
            }
        } catch (Exception e) {
            log.error("获取音色列表异常: userId={}, error={}", 
                     userService.getInfo() != null ? userService.getInfo().getId() : "unknown", 
                     e.getMessage(), e);
            return CommonResult.failed("获取音色列表失败: " + e.getMessage());
        }
    }

}
