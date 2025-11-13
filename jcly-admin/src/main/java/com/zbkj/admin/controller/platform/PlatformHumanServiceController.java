package com.zbkj.admin.controller.platform;

import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.service.CustomerServiceStaff;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.vo.LoginUserVo;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.common.constants.SmsConstants;
import com.zbkj.common.request.CustomerServiceLoginRequest;
import com.zbkj.common.response.CustomerServiceLoginResponse;
import com.zbkj.service.service.*;
import com.zbkj.common.response.chat.MessageResponse;
import com.zbkj.common.model.chat.UnifiedChatMessage;
import com.zbkj.common.model.chat.UnifiedChatSession;
import com.zbkj.common.response.humanservice.HumanServiceStatisticsResponse;
import com.zbkj.common.model.humanservice.HumanServiceRating;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人工客服管理控制器 - 平台端
 */
@Slf4j
@RestController
@RequestMapping("api/admin/platform/human-service")
@Api(tags = "平台端 - 人工客服管理")
public class PlatformHumanServiceController {

    @Autowired
    private UnifiedChatService unifiedChatService;

    @Autowired
    private HumanServiceStatisticsService humanServiceStatisticsService;

    @Autowired
    private HumanServiceRatingService humanServiceRatingService;

    @Autowired
    private HumanServiceService humanServiceService;

    @Autowired
    private ContactRelationshipService contactRelationshipService;
    @Autowired
    private CustomerServiceStaffService customerServiceStaffService;

    @Autowired
    private SmsService smsService;

    // ================== 客服登录与认证相关API ==================

    /**
     * 发送客服登录验证码
     */
    @ApiOperation(value = "发送客服登录验证码")
    @PostMapping("/send-code")
    public CommonResult<String> sendLoginCode(@RequestParam String phone) {
        try {
            log.info("发送平台端客服登录验证码请求: phone={}", phone);
            Boolean result = smsService.sendCommonCode(phone, SmsConstants.VERIFICATION_CODE_SCENARIO_CUSTOMER_SERVICE_LOGIN);
            if (result) {
                return CommonResult.success("验证码发送成功");
            } else {
                return CommonResult.failed("验证码发送失败");
            }
        } catch (Exception e) {
            log.error("发送平台端客服登录验证码失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    /**
     * 平台端客服登录
     */
    @ApiOperation(value = "平台端客服登录")
    @PostMapping("/login")
    public CommonResult<CustomerServiceLoginResponse> login(
            @RequestBody @Validated CustomerServiceLoginRequest request) {
        try {
            log.info("平台端客服登录请求: account={}", request.getAccount());
            CustomerServiceLoginResponse response = customerServiceStaffService.login(request);
            return CommonResult.success(response);
        } catch (Exception e) {
            log.error("平台端客服登录失败", e);
            return CommonResult.failed(e.getMessage());
        }
    }

    // ================== 人工客服消息管理API ==================

    @ApiOperation(value = "获取人工客服消息列表（排除AI助手消息）")
   // @PreAuthorize("hasAuthority('platform:human-service:message:view')")
    @GetMapping("/messages")
    public CommonResult<CommonPage<MessageResponse>> getHumanServiceMessages(
            @ApiParam(value = "会话ID") @RequestParam(required = false) String sessionId,
            @ApiParam(value = "消息类型") @RequestParam(required = false) String messageType,
            @ApiParam(value = "角色") @RequestParam(required = false) String role,
            @ApiParam(value = "消息内容") @RequestParam(required = false) String content,
            @ApiParam(value = "发送者类型") @RequestParam(required = false) String senderType,
            @ApiParam(value = "消息状态") @RequestParam(required = false) String status,
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        
        try {
            // 获取当前平台管理员信息
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Long merId = loginUser.getUser().getMerId() != null ? loginUser.getUser().getMerId().longValue() : 0L;
            
            // 调用统一聊天服务获取人工客服消息（排除AI助手消息）
            PageInfo<MessageResponse> messages = unifiedChatService.getHumanServiceMessages(
                merId, sessionId, messageType, role, content, senderType, status, page, size);
            
            return CommonResult.success(CommonPage.restPage(messages));
        } catch (Exception e) {
            log.error("获取人工客服消息列表失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "获取消息详情")
   // @PreAuthorize("hasAuthority('platform:human-service:message:detail')")
    @GetMapping("/messages/{messageId}")
    public CommonResult<UnifiedChatMessage> getMessageDetail(
            @ApiParam(value = "消息ID") @PathVariable String messageId) {
        
        try {
            UnifiedChatMessage message = unifiedChatService.getMessageById(messageId);
            if (message == null) {
                return CommonResult.failed("消息不存在");
            }
            return CommonResult.success(message);
        } catch (Exception e) {
            log.error("获取消息详情失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    /**
     * 统计未读消息数量
     * @param
     * @return
     */
    @ApiOperation(value = "统计未读消息数量")
    @GetMapping("/messages//unread-stats")
    public CommonResult<Integer> getUnreadMessageCount() {

        try {
            // 获取当前平台管理员信息
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Long merId = loginUser.getUser().getMerId() != null ? loginUser.getUser().getMerId().longValue() : 0L;

            // 调用统一聊天服务获取未读消息数量
            int count = unifiedChatService.getUnreadMessageCount(merId);
            return CommonResult.success(count);
        } catch (Exception e) {
            log.error("统计未读消息数量失败", e);
        }
        return CommonResult.failed();
    }

    @ApiOperation(value = "标记消息为已读")
   // @PreAuthorize("hasAuthority('platform:human-service:message:edit')")
    @PutMapping("/messages/{messageId}/read")
    public CommonResult<String> markMessageAsRead(
            @ApiParam(value = "消息ID") @PathVariable String messageId) {
        
        try {
            boolean success = unifiedChatService.markMessageAsRead(messageId);
            if (success) {
                return CommonResult.success("标记成功");
            } else {
                return CommonResult.failed("标记失败");
            }
        } catch (Exception e) {
            log.error("标记消息已读失败", e);
            return CommonResult.failed("标记失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "批量删除消息")
   // @PreAuthorize("hasAuthority('platform:human-service:message:delete')")
    @DeleteMapping("/messages/batch")
    public CommonResult<String> batchDeleteMessages(
            @ApiParam(value = "消息ID列表") @RequestBody List<String> messageIds) {
        
        try {
            boolean success = unifiedChatService.batchDeleteMessages(messageIds);
            if (success) {
                return CommonResult.success("删除成功");
            } else {
                return CommonResult.failed("删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除消息失败", e);
            return CommonResult.failed("删除失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "获取人工客服会话列表")
   // @PreAuthorize("hasAuthority('platform:human-service:session:view')")
    @GetMapping("/sessions")
    public CommonResult<PageInfo<UnifiedChatSession>> getHumanServiceSessions(
            @ApiParam(value = "会话类型") @RequestParam(required = false) String sessionType,
            @ApiParam(value = "会话状态") @RequestParam(required = false) String status,
            @ApiParam(value = "用户ID") @RequestParam(required = false) Long userId,
            @ApiParam(value = "会话ID") @RequestParam(required = false) String sessionId,
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量") @RequestParam(defaultValue = "20") Integer size) {
        
        try {
            // 获取当前平台管理员信息
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Long merId = loginUser.getUser().getMerId() != null ? loginUser.getUser().getMerId().longValue() : 0L;
            
            // 调用统一聊天服务获取人工客服会话列表
            PageInfo<UnifiedChatSession> sessions = unifiedChatService.getHumanServiceSessions(
                merId, sessionType, status, userId, sessionId, page, size);
            
            return CommonResult.success(sessions);
        } catch (Exception e) {
            log.error("获取人工客服会话列表失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "获取指定会话的消息列表")
   // @PreAuthorize("hasAuthority('platform:human-service:message:view')")
    @GetMapping("/sessions/{sessionId}/messages")
    public CommonResult<PageInfo<MessageResponse>> getSessionMessages(
            @ApiParam(value = "会话ID") @PathVariable String sessionId,
            @ApiParam(value = "消息类型") @RequestParam(required = false) String messageType,
            @ApiParam(value = "角色") @RequestParam(required = false) String role,
            @ApiParam(value = "发送者类型") @RequestParam(required = false) String senderType,
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量") @RequestParam(defaultValue = "20") Integer size) {
        
        try {
            // 获取当前平台管理员信息
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Long merId = loginUser.getUser().getMerId() != null ? loginUser.getUser().getMerId().longValue() : 0L;
            
            // 调用统一聊天服务获取指定会话的消息列表
            PageInfo<MessageResponse> messages = unifiedChatService.getSessionMessages(
                merId, sessionId, messageType, role, senderType, page, size);
            
            return CommonResult.success(messages);
        } catch (Exception e) {
            log.error("获取会话消息列表失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "获取人工客服综合统计数据")
   // @PreAuthorize("hasAuthority('platform:human-service:statistics:view')")
    @GetMapping("/statistics/comprehensive")
    public CommonResult<HumanServiceStatisticsResponse> getComprehensiveStatistics(
            @ApiParam(value = "统计天数") @RequestParam(defaultValue = "7") Integer days) {
        
        try {
            // 获取当前平台管理员信息
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Long merId = loginUser.getUser().getMerId() != null ? loginUser.getUser().getMerId().longValue() : 0L;
            
            HumanServiceStatisticsResponse statistics = humanServiceStatisticsService.getComprehensiveStatistics(merId, days);
            
            return CommonResult.success(statistics);
        } catch (Exception e) {
            log.error("获取人工客服综合统计数据失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "获取客服回复统计数据")
   // @PreAuthorize("hasAuthority('platform:human-service:statistics:view')")
    @GetMapping("/statistics/staff-replies")
    public CommonResult<List<HumanServiceStatisticsResponse.StaffReplyStatistics>> getStaffReplyStatistics(
            @ApiParam(value = "统计天数") @RequestParam(defaultValue = "7") Integer days) {
        
        try {
            // 获取当前平台管理员信息
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Long merId = loginUser.getUser().getMerId() != null ? loginUser.getUser().getMerId().longValue() : 0L;
            
            List<HumanServiceStatisticsResponse.StaffReplyStatistics> statistics = 
                humanServiceStatisticsService.getStaffReplyStatistics(merId, days);
            
            return CommonResult.success(statistics);
        } catch (Exception e) {
            log.error("获取客服回复统计数据失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "获取每日消息统计数据")
   // @PreAuthorize("hasAuthority('platform:human-service:statistics:view')")
    @GetMapping("/statistics/daily-messages")
    public CommonResult<List<HumanServiceStatisticsResponse.DailyMessageStatistics>> getDailyMessageStatistics(
            @ApiParam(value = "统计天数") @RequestParam(defaultValue = "7") Integer days) {
        
        try {
            // 获取当前平台管理员信息
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Long merId = loginUser.getUser().getMerId() != null ? loginUser.getUser().getMerId().longValue() : 0L;
            
            List<HumanServiceStatisticsResponse.DailyMessageStatistics> statistics = 
                humanServiceStatisticsService.getDailyMessageStatistics(merId, days);
            
            return CommonResult.success(statistics);
        } catch (Exception e) {
            log.error("获取每日消息统计数据失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "获取消息类型分布统计")
    //@PreAuthorize("hasAuthority('platform:human-service:statistics:view')")
    @GetMapping("/statistics/message-types")
    public CommonResult<List<HumanServiceStatisticsResponse.MessageTypeStatistics>> getMessageTypeStatistics(
            @ApiParam(value = "统计天数") @RequestParam(defaultValue = "7") Integer days) {
        
        try {
            // 获取当前平台管理员信息
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Long merId = loginUser.getUser().getMerId() != null ? loginUser.getUser().getMerId().longValue() : 0L;
            
            List<HumanServiceStatisticsResponse.MessageTypeStatistics> statistics = 
                humanServiceStatisticsService.getMessageTypeStatistics(merId, days);
            
            return CommonResult.success(statistics);
        } catch (Exception e) {
            log.error("获取消息类型分布统计失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "获取客服评价统计数据")
   // @PreAuthorize("hasAuthority('platform:human-service:statistics:view')")
    @GetMapping("/statistics/staff-ratings")
    public CommonResult<List<HumanServiceStatisticsResponse.StaffRatingStatistics>> getStaffRatingStatistics(
            @ApiParam(value = "统计天数") @RequestParam(defaultValue = "30") Integer days) {
        
        try {
            // 获取当前平台管理员信息
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Long merId = loginUser.getUser().getMerId() != null ? loginUser.getUser().getMerId().longValue() : 0L;
            
            List<HumanServiceStatisticsResponse.StaffRatingStatistics> statistics = 
                humanServiceStatisticsService.getStaffRatingStatistics(merId, days);
            
            return CommonResult.success(statistics);
        } catch (Exception e) {
            log.error("获取客服评价统计数据失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "获取热门问题统计")
   // @PreAuthorize("hasAuthority('platform:human-service:statistics:view')")
    @GetMapping("/statistics/popular-questions")
    public CommonResult<List<HumanServiceStatisticsResponse.PopularQuestionStatistics>> getPopularQuestionStatistics(
            @ApiParam(value = "统计天数") @RequestParam(defaultValue = "30") Integer days,
            @ApiParam(value = "返回数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        
        try {
            // 获取当前平台管理员信息
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Long merId = loginUser.getUser().getMerId() != null ? loginUser.getUser().getMerId().longValue() : 0L;
            
            List<HumanServiceStatisticsResponse.PopularQuestionStatistics> statistics = 
                humanServiceStatisticsService.getPopularQuestionStatistics(merId, days, limit);
            
            return CommonResult.success(statistics);
        } catch (Exception e) {
            log.error("获取热门问题统计失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "获取客服工作效率统计")
  //  @PreAuthorize("hasAuthority('platform:human-service:statistics:view')")
    @GetMapping("/statistics/staff-efficiency")
    public CommonResult<Map<String, Object>> getStaffEfficiencyStatistics(
            @ApiParam(value = "客服ID") @RequestParam(required = false) Long staffId,
            @ApiParam(value = "统计天数") @RequestParam(defaultValue = "7") Integer days) {
        
        try {
            // 获取当前平台管理员信息
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Long merId = loginUser.getUser().getMerId() != null ? loginUser.getUser().getMerId().longValue() : 0L;
            
            Map<String, Object> statistics = 
                humanServiceStatisticsService.getStaffEfficiencyStatistics(merId, staffId, days);
            
            return CommonResult.success(statistics);
        } catch (Exception e) {
            log.error("获取客服工作效率统计失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "获取客户满意度趋势")
    //@PreAuthorize("hasAuthority('platform:human-service:statistics:view')")
    @GetMapping("/statistics/satisfaction-trend")
    public CommonResult<List<Map<String, Object>>> getSatisfactionTrend(
            @ApiParam(value = "统计天数") @RequestParam(defaultValue = "30") Integer days) {
        
        try {
            // 获取当前平台管理员信息
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Long merId = loginUser.getUser().getMerId() != null ? loginUser.getUser().getMerId().longValue() : 0L;
            
            List<Map<String, Object>> trend =
                humanServiceStatisticsService.getSatisfactionTrend(merId, days);
            
            return CommonResult.success(trend);
        } catch (Exception e) {
            log.error("获取客户满意度趋势失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    // ================== 评价管理相关API ==================

    @ApiOperation(value = "获取评价管理列表")
    //@PreAuthorize("hasAuthority('platform:human-service:rating:view')")
    @GetMapping("/ratings")
    public CommonResult<PageInfo<HumanServiceRating>> getRatingManagementList(
            @ApiParam(value = "客服ID") @RequestParam(required = false) Long staffId,
            @ApiParam(value = "评价类型") @RequestParam(required = false) Integer ratingType,
            @ApiParam(value = "状态") @RequestParam(required = false) Integer status,
            @ApiParam(value = "关键词搜索") @RequestParam(required = false) String keyword,
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量") @RequestParam(defaultValue = "20") Integer size) {
        
        try {
            // 获取当前平台管理员信息
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Long merId = loginUser.getUser().getMerId() != null ? loginUser.getUser().getMerId().longValue() : 0L;
            
            PageInfo<HumanServiceRating> ratings = humanServiceRatingService.getRatingManagementList(
                merId, staffId, ratingType, status, keyword, page, size);
            
            return CommonResult.success(ratings);
        } catch (Exception e) {
            log.error("获取评价管理列表失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "获取评价详情")
  //  @PreAuthorize("hasAuthority('platform:human-service:rating:detail')")
    @GetMapping("/ratings/{ratingId}")
    public CommonResult<HumanServiceRating> getRatingDetail(
            @ApiParam(value = "评价ID") @PathVariable Long ratingId) {
        
        try {
            HumanServiceRating rating = humanServiceRatingService.getRatingDetail(ratingId);
            if (rating == null) {
                return CommonResult.failed("评价不存在");
            }
            return CommonResult.success(rating);
        } catch (Exception e) {
            log.error("获取评价详情失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "批量审核评价")
   // @PreAuthorize("hasAuthority('platform:human-service:rating:audit')")
    @PutMapping("/ratings/batch-audit")
    public CommonResult<String> batchAuditRatings(
            @ApiParam(value = "评价ID列表") @RequestBody List<Long> ratingIds,
            @ApiParam(value = "审核状态") @RequestParam Integer status) {
        
        try {
            boolean success = humanServiceRatingService.batchAuditRatings(ratingIds, status);
            if (success) {
                return CommonResult.success("批量审核成功");
            } else {
                return CommonResult.failed("批量审核失败");
            }
        } catch (Exception e) {
            log.error("批量审核评价失败", e);
            return CommonResult.failed("批量审核失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "删除评价")
   // @PreAuthorize("hasAuthority('platform:human-service:rating:delete')")
    @DeleteMapping("/ratings/{ratingId}")
    public CommonResult<String> deleteRating(
            @ApiParam(value = "评价ID") @PathVariable Long ratingId) {
        
        try {
            boolean success = humanServiceRatingService.deleteRating(ratingId);
            if (success) {
                return CommonResult.success("删除成功");
            } else {
                return CommonResult.failed("删除失败");
            }
        } catch (Exception e) {
            log.error("删除评价失败", e);
            return CommonResult.failed("删除失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "获取客服评价统计")
   // @PreAuthorize("hasAuthority('platform:human-service:rating:statistics')")
    @GetMapping("/ratings/statistics")
    public CommonResult<Map<String, Object>> getStaffRatingStatistics(
            @ApiParam(value = "客服ID") @RequestParam(required = false) Long staffId,
            @ApiParam(value = "统计天数") @RequestParam(defaultValue = "30") Integer days) {
        
        try {
            // 获取当前平台管理员信息
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Long merId = loginUser.getUser().getMerId() != null ? loginUser.getUser().getMerId().longValue() : 0L;
            
            Map<String, Object> statistics = humanServiceRatingService.getStaffRatingStatistics(merId, staffId, days);
            
            return CommonResult.success(statistics);
        } catch (Exception e) {
            log.error("获取客服评价统计失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    // ================== 快捷回复模板相关API ==================

    @ApiOperation(value = "获取快捷回复模板列表")
  //  @PreAuthorize("hasAuthority('platform:human-service:template:view')")
    @GetMapping("/templates")
    public CommonResult<com.github.pagehelper.PageInfo<com.zbkj.common.model.service.QuickReplyTemplate>> getQuickReplyTemplates(
            @ApiParam(value = "分类") @RequestParam(required = false) String category,
            @ApiParam(value = "关键词") @RequestParam(required = false) String keyword,
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量") @RequestParam(defaultValue = "20") Integer size) {
        try {
            com.github.pagehelper.PageInfo<com.zbkj.common.model.service.QuickReplyTemplate> templates = 
                humanServiceService.getQuickReplyTemplatesWithPage(category, keyword, page, size);
            
            return CommonResult.success(templates);
        } catch (Exception e) {
            log.error("获取快捷回复模板列表失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "添加快捷回复模板")
   // @PreAuthorize("hasAuthority('platform:human-service:template:add')")
    @PostMapping("/templates")
    public CommonResult<String> addQuickReplyTemplate(@RequestBody com.zbkj.common.model.service.QuickReplyTemplate template) {
        try {
            humanServiceService.addQuickReplyTemplate(template);
            return CommonResult.success("添加成功");
        } catch (Exception e) {
            log.error("添加快捷回复模板失败", e);
            return CommonResult.failed("添加失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "更新快捷回复模板")
   // @PreAuthorize("hasAuthority('platform:human-service:template:edit')")
    @PutMapping("/templates/{templateId}")
    public CommonResult<String> updateQuickReplyTemplate(
            @ApiParam(value = "模板ID") @PathVariable Long templateId,
            @RequestBody com.zbkj.common.model.service.QuickReplyTemplate template) {
        try {
            template.setId(templateId);
            humanServiceService.updateQuickReplyTemplate(template);
            return CommonResult.success("更新成功");
        } catch (Exception e) {
            log.error("更新快捷回复模板失败", e);
            return CommonResult.failed("更新失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "删除快捷回复模板")
   // @PreAuthorize("hasAuthority('platform:human-service:template:delete')")
    @DeleteMapping("/templates/{templateId}")
    public CommonResult<String> deleteQuickReplyTemplate(
            @ApiParam(value = "模板ID") @PathVariable Long templateId) {
        try {
            humanServiceService.deleteQuickReplyTemplate(templateId);
            return CommonResult.success("删除成功");
        } catch (Exception e) {
            log.error("删除快捷回复模板失败", e);
            return CommonResult.failed("删除失败：" + e.getMessage());
        }
    }

    // ================== 客服配置相关API ==================

    @ApiOperation(value = "获取客服配置")
   // @PreAuthorize("hasAuthority('platform:human-service:config:view')")
    @GetMapping("/config")
    public CommonResult<com.zbkj.common.model.service.CustomerServiceConfig> getHumanServiceConfig() {
        try {
            com.zbkj.common.model.service.CustomerServiceConfig config = humanServiceService.getServiceConfig();
            return CommonResult.success(config);
        } catch (Exception e) {
            log.error("获取客服配置失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "保存客服配置")
   // @PreAuthorize("hasAuthority('platform:human-service:config:edit')")
    @PostMapping("/config")
    public CommonResult<String> saveHumanServiceConfig(@RequestBody com.zbkj.common.model.service.CustomerServiceConfig config) {
        try {
            humanServiceService.updateServiceConfig(config);
            return CommonResult.success("保存成功");
        } catch (Exception e) {
            log.error("保存客服配置失败", e);
            return CommonResult.failed("保存失败：" + e.getMessage());
        }
    }

    // ================== 联系人管理相关API ==================

    @ApiOperation(value = "获取联系人列表")
   // @PreAuthorize("hasAuthority('platform:human-service:contact:view')")
    @GetMapping("/contacts")
    public CommonResult<com.github.pagehelper.PageInfo<com.zbkj.common.model.service.ContactRelationship>> getContacts(
            @ApiParam(value = "用户类型") @RequestParam(required = false) String contactType,
            @ApiParam(value = "分组名称") @RequestParam(required = false) String groupName,
            @ApiParam(value = "关键词") @RequestParam(required = false) String keyword,
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量") @RequestParam(defaultValue = "20") Integer size) {
        try {
            com.github.pagehelper.PageInfo<com.zbkj.common.model.service.ContactRelationship> contacts;
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                contacts = contactRelationshipService.searchContactsWithPage(keyword, page, size);
            } else {
                contacts = contactRelationshipService.getContactListWithPage(contactType, groupName, page, size);
            }
            
            return CommonResult.success(contacts);
        } catch (Exception e) {
            log.error("获取联系人列表失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "添加联系人")
   // @PreAuthorize("hasAuthority('platform:human-service:contact:add')")
    @PostMapping("/contacts")
    public CommonResult<String> addContact(@RequestBody com.zbkj.common.request.ContactManageRequest request) {
        try {
            contactRelationshipService.addContact(request);
            return CommonResult.success("添加成功");
        } catch (Exception e) {
            log.error("添加联系人失败", e);
            return CommonResult.failed("添加失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "更新联系人")
   // @PreAuthorize("hasAuthority('platform:human-service:contact:edit')")
    @PutMapping("/contacts/{contactId}")
    public CommonResult<String> updateContact(
            @ApiParam(value = "联系人ID") @PathVariable Long contactId,
            @RequestBody com.zbkj.common.request.ContactManageRequest request) {
        try {
            contactRelationshipService.updateContact(contactId, request);
            return CommonResult.success("更新成功");
        } catch (Exception e) {
            log.error("更新联系人失败", e);
            return CommonResult.failed("更新失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "删除联系人")
   // @PreAuthorize("hasAuthority('platform:human-service:contact:delete')")
    @DeleteMapping("/contacts/{contactId}")
    public CommonResult<String> deleteContact(
            @ApiParam(value = "联系人ID") @PathVariable Long contactId) {
        try {
            contactRelationshipService.deleteContact(contactId);
            return CommonResult.success("删除成功");
        } catch (Exception e) {
            log.error("删除联系人失败", e);
            return CommonResult.failed("删除失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "置顶/取消置顶联系人")
    //@PreAuthorize("hasAuthority('platform:human-service:contact:pin')")
    @PostMapping("/contacts/{contactId}/pin")
    public CommonResult<String> pinContact(
            @ApiParam(value = "联系人ID") @PathVariable Long contactId,
            @RequestBody Map<String, Object> pinData) {
        try {
            Boolean pinned = (Boolean) pinData.get("isPinned");
            contactRelationshipService.pinContact(contactId, pinned);
            return CommonResult.success(pinned ? "置顶成功" : "取消置顶成功");
        } catch (Exception e) {
            log.error("置顶联系人失败", e);
            return CommonResult.failed("操作失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "获取联系人在线状态")
   // @PreAuthorize("hasAuthority('platform:human-service:contact:status')")
    @PostMapping("/contacts/online-status")
    public CommonResult<Map<String, Object>> getContactOnlineStatus(@RequestBody Map<String, Object> requestData) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> userIds = (List<Integer>) requestData.get("userIds");
            
            Map<String, Object> statusMap = new HashMap<>();
            for (Integer userId : userIds) {
                // 调用已有的在线状态查询方法
                Map<String, Object> status = contactRelationshipService.getContactOnlineStatus(userId, "USER");
                statusMap.put(userId.toString(), status.get("status"));
            }
            
            return CommonResult.success(statusMap);
        } catch (Exception e) {
            log.error("获取联系人在线状态失败", e);
            return CommonResult.failed("获取失败：" + e.getMessage());
        }
    }
    /**
     * 获取客服列表
     */
    @ApiOperation(value = "获取客服列表")
    // @PreAuthorize("hasAuthority('platform:human-service:staff:list')")
    @GetMapping("/staff")
    public CommonResult<List<CustomerServiceStaff>> getStaffList(
            @RequestParam(required = false) String onlineStatus) {

        List<CustomerServiceStaff> staffList = customerServiceStaffService.getStaffList(onlineStatus);
        return CommonResult.success(staffList);
    }

    /**
     * 获取可用客服列表
     */
    @ApiOperation(value = "获取可用客服列表")
    //  @PreAuthorize("hasAuthority('platform:human-service:staff:available')")
    @GetMapping("/staff/available")
    public CommonResult<List<Map<String, Object>>> getAvailableStaff() {
        List<Map<String, Object>> availableStaff = customerServiceStaffService.getAvailableStaff();
        return CommonResult.success(availableStaff);
    }
    /**
     * 添加客服
     */
    @ApiOperation(value = "添加客服")
    //  @PreAuthorize("hasAuthority('platform:human-service:staff:add')")
    @PostMapping("/staff")
    public CommonResult<CustomerServiceStaff> addStaff(@RequestBody Map<String, Object> requestData) {
        try {
            // 处理前端传递的数据
            CustomerServiceStaff staff = new CustomerServiceStaff();

            // 从adminId获取employeeId
            if (requestData.get("adminId") != null) {
                Integer adminId = Integer.valueOf(requestData.get("adminId").toString());
                staff.setAdminId(adminId);
            }

            // 设置其他字段
            if (requestData.get("staffName") != null) {
                staff.setStaffName(requestData.get("staffName").toString());
            }
            if (requestData.get("avatar") != null) {
                staff.setAvatar(requestData.get("avatar").toString());
            }
            if (requestData.get("serviceLevel") != null) {
                staff.setServiceLevel(requestData.get("serviceLevel").toString());
            }
            if (requestData.get("maxConcurrentSessions") != null) {
                staff.setMaxConcurrentSessions(Integer.valueOf(requestData.get("maxConcurrentSessions").toString()));
            }
            if (requestData.get("skillTags") != null) {
                staff.setSkillTags(requestData.get("skillTags").toString());
            }

            customerServiceStaffService.addStaff(staff);
            return CommonResult.success(staff);
        } catch (Exception e) {
            log.error("添加客服失败", e);
            return CommonResult.failed("添加客服失败: " + e.getMessage());
        }
    }

    /**
     * 更新客服信息
     */
    @ApiOperation(value = "更新客服信息")
    // @PreAuthorize("hasAuthority('platform:human-service:staff:update')")
    @PutMapping("/staff/{staffId}")
    public CommonResult<CustomerServiceStaff> updateStaff(
            @PathVariable Integer staffId,
            @RequestBody Map<String, Object> requestData) {

        try {
            // 获取现有客服信息
            CustomerServiceStaff existingStaff = customerServiceStaffService.getStaffList(null)
                    .stream()
                    .filter(s -> s.getId().equals(staffId))
                    .findFirst()
                    .orElse(null);

            if (existingStaff == null) {
                return CommonResult.failed("客服不存在");
            }

            // 更新字段
            if (requestData.get("staffName") != null) {
                existingStaff.setStaffName(requestData.get("staffName").toString());
            }
            if (requestData.get("avatar") != null) {
                existingStaff.setAvatar(requestData.get("avatar").toString());
            }
            if (requestData.get("serviceLevel") != null) {
                existingStaff.setServiceLevel(requestData.get("serviceLevel").toString());
            }
            if (requestData.get("maxConcurrentSessions") != null) {
                existingStaff.setMaxConcurrentSessions(Integer.valueOf(requestData.get("maxConcurrentSessions").toString()));
            }
            if (requestData.get("skillTags") != null) {
                existingStaff.setSkillTags(requestData.get("skillTags").toString());
            }
            if (requestData.get("status") != null) {
                existingStaff.setStatus(Boolean.valueOf(requestData.get("status").toString()));
            }

            customerServiceStaffService.updateStaff(existingStaff);
            return CommonResult.success(existingStaff);
        } catch (Exception e) {
            log.error("更新客服信息失败", e);
            return CommonResult.failed("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除客服
     */
    @ApiOperation(value = "删除客服")
    // @PreAuthorize("hasAuthority('platform:human-service:staff:delete')")
    @DeleteMapping("/staff/{staffId}")
    public CommonResult<Void> deleteStaff(@PathVariable Integer staffId) {
        customerServiceStaffService.deleteStaff(staffId);
        return CommonResult.success();
    }

    /**
     * 获取客服详情
     */
    @ApiOperation(value = "获取客服详情")
    @GetMapping("/staff/{staffId}")
    public CommonResult<CustomerServiceStaff> getStaffDetail(@PathVariable Integer staffId) {
        try {
            CustomerServiceStaff staff = customerServiceStaffService.getStaffList(null)
                    .stream()
                    .filter(s -> s.getId().equals(staffId))
                    .findFirst()
                    .orElse(null);

            if (staff == null) {
                return CommonResult.failed("客服不存在");
            }

            return CommonResult.success(staff);
        } catch (Exception e) {
            log.error("获取客服详情失败", e);
            return CommonResult.failed("获取失败: " + e.getMessage());
        }
    }

}