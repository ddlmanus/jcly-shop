package com.zbkj.admin.controller.merchant;

import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeBot;
import com.zbkj.common.model.coze.CozeBotConfig;
import com.zbkj.common.model.coze.CozeConversation;
import com.zbkj.common.model.coze.CozeKnowledge;
import com.zbkj.common.model.coze.CozeKnowledgeFile;
import com.zbkj.common.model.coze.CozeMessage;
import com.zbkj.common.model.coze.CozeWorkflow;
import com.zbkj.common.model.coze.CozeSpace;
import com.zbkj.common.model.coze.CozeSpaceMember;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.coze.*;
import java.util.List;
import com.zbkj.common.response.coze.*;
import com.zbkj.common.response.coze.CozeUploadFileResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.common.vo.LoginUserVo;
import com.zbkj.service.service.*;
import com.zbkj.service.service.CozeSpaceMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

/**
 * Coze AI 接口控制器
 * 提供智能体、会话、消息、知识库等相关API接口
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
@RequestMapping("api/admin/merchant/coze")
@Api(tags = "Coze AI 接口控制器")
public class CozeController {

    @Autowired
    private CozeService cozeService;
    
    @Autowired
    private CozeBotService cozeBotService;
    
    @Autowired
    private CozeKnowledgeService cozeKnowledgeService;
    
    @Autowired
    private CozeKnowledgeFileService cozeKnowledgeFileService;
    
    @Autowired
    private CozeWorkflowService cozeWorkflowService;
    
    @Autowired
    private CozeSpaceMemberService cozeSpaceMemberService;
    
    @Autowired
    private CozeConversationService cozeConversationService;
    
    @Autowired
    private CozeMessageService cozeMessageService;

    @Autowired
    private CozeSpaceService cozeSpaceService;

    @Autowired
    private CozeBotConfigService cozeBotConfigService;

    // =================================================================================
    // 智能体管理相关接口
    // =================================================================================

    @ApiOperation(value = "创建智能体")
  //  @PreAuthorize("hasAuthority('merchant:coze:bot:create')")
    @PostMapping("/bot/create")
    public CommonResult<CozeCreateBotResponse> createBot(@RequestBody @Validated CozeCreateBotRequest request) {
        CozeCreateBotResponse response = cozeService.createBot(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "更新智能体")
  //  @PreAuthorize("hasAuthority('merchant:coze:bot:update')")
    @PostMapping("/bot/update")
    public CommonResult<CozeBaseResponse> updateBot(@RequestBody @Validated CozeUpdateBotRequest request) {
        CozeBaseResponse response = cozeService.updateBot(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "发布智能体")
   // @PreAuthorize("hasAuthority('merchant:coze:bot:publish')")
    @PostMapping("/bot/publish")
    public CommonResult<CozeBaseResponse> publishBot(@RequestBody @Validated CozePublishBotRequest request) {
        CozeBaseResponse response = cozeService.publishBot(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "下架智能体")
  //  @PreAuthorize("hasAuthority('merchant:coze:bot:unpublish')")
    @PostMapping("/bot/{botId}/unpublish")
    public CommonResult<CozeBaseResponse> unpublishBot(@PathVariable String botId, @RequestBody @Validated CozeUnpublishBotRequest request) {
        request.setBotId(botId);
        CozeBaseResponse response = cozeService.unpublishBot(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "查看智能体列表 (Coze API)")
   // @PreAuthorize("hasAuthority('merchant:coze:bot:list')")
    @GetMapping("/bot/list")
    public CommonResult<CozeBotListResponse> getBotList(@RequestParam String spaceId, 
                                          @RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "20") Integer pageSize) {
        CozeBotListResponse response = cozeService.getBotList(spaceId, pageNum, pageSize);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "获取本地智能体列表")
  //  @PreAuthorize("hasAuthority('merchant:coze:bot:view')")
    @GetMapping("/bot/local/list")
    public CommonResult<CommonPage<CozeBot>> getLocalBotList(PageParamRequest pageParamRequest) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        PageInfo<CozeBot> response = cozeBotService.getByMerchantId(merchantId, pageParamRequest);
        return CommonResult.success(CommonPage.restPage(response));
    }

    @ApiOperation(value = "获取本地智能体详情")
   // @PreAuthorize("hasAuthority('merchant:coze:bot:view')")
    @GetMapping("/bot/local/{cozeBotId}")
    public CommonResult<CozeBot> getLocalBotDetail(@PathVariable String cozeBotId) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        CozeBot response = cozeBotService.getByCozeBotId(cozeBotId, merchantId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "删除本地智能体")
  //  @PreAuthorize("hasAuthority('merchant:coze:bot:delete')")
    @DeleteMapping("/bot/local/{cozeBotId}")
    public CommonResult<Boolean> deleteLocalBot(@PathVariable String cozeBotId) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean response = cozeBotService.deleteByBotId(cozeBotId, merchantId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "设置默认智能体")
  //  @PreAuthorize("hasAuthority('merchant:coze:bot:setDefault')")
    @PostMapping("/bot/local/{cozeBotId}/setDefault")
    public CommonResult<Boolean> setDefaultBot(@PathVariable String cozeBotId) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean response = cozeBotService.setDefaultBot(cozeBotId, merchantId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "获取默认智能体")
  //  @PreAuthorize("hasAuthority('merchant:coze:bot:view')")
    @GetMapping("/bot/local/default")
    public CommonResult<CozeBot> getDefaultBot() {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        CozeBot response = cozeBotService.getDefaultBot(merchantId);
        return CommonResult.success(response);
    }

    // =================================================================================
    // 知识库管理相关接口
    // =================================================================================

    @ApiOperation(value = "创建知识库")
  //  @PreAuthorize("hasAuthority('merchant:coze:knowledge:create')")
    @PostMapping("/knowledge/create")
    public CommonResult<Object> createKnowledge(@RequestBody @Validated CozeCreateKnowledgeRequest request) {
        try {
            log.info("创建知识库请求参数: {}", request);
            Object response = cozeService.createKnowledge(request);
            log.info("创建知识库响应: {}", response);
            return CommonResult.success(response);
        } catch (Exception e) {
            log.error("创建知识库失败", e);
            return CommonResult.failed("创建知识库失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "修改知识库信息")
  //  @PreAuthorize("hasAuthority('merchant:coze:knowledge:update')")
    @PutMapping("/knowledge/{datasetId}")
    public CommonResult<CozeBaseResponse> updateKnowledge(@PathVariable String datasetId, @RequestBody @Validated CozeUpdateKnowledgeRequest request) {
        request.setDatasetId(datasetId);
        CozeBaseResponse response = cozeService.updateKnowledge(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "删除知识库")
  //  @PreAuthorize("hasAuthority('merchant:coze:knowledge:delete')")
    @DeleteMapping("/knowledge/{datasetId}")
    public CommonResult<CozeBaseResponse> deleteKnowledge(@PathVariable String datasetId) {
        CozeBaseResponse response = cozeService.deleteKnowledge(datasetId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "创建知识库文件")
   // @PreAuthorize("hasAuthority('merchant:coze:knowledge:file:create')")
    @PostMapping("/knowledge/file/create")
    public CommonResult<CozeCreateKnowledgeFileResponse> createKnowledgeFile(@RequestBody @Validated CozeCreateKnowledgeFileRequest request) {
        CozeCreateKnowledgeFileResponse response = cozeService.createKnowledgeFile(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "修改知识库文件")
   // @PreAuthorize("hasAuthority('merchant:coze:knowledge:file:update')")
    @PutMapping("/knowledge/file/update")
    public CommonResult<CozeBaseResponse> updateKnowledgeFile(@RequestBody @Validated CozeUpdateKnowledgeFileRequest request) {
        try {
            CozeBaseResponse response = cozeService.updateKnowledgeFile(request);
            return CommonResult.success(response);
        } catch (Exception e) {
            log.error("修改知识库文件失败", e);
            return CommonResult.failed("修改知识库文件失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "批量查看知识库文件上传进度（符合API文档）")
   // @PreAuthorize("hasAuthority('merchant:coze:knowledge:file:progress')")
    @PostMapping("/knowledge/{datasetId}/file/progress")
    public CommonResult<CozeKnowledgeFileProgressResponse> getKnowledgeFileProgressBatch(
            @PathVariable String datasetId,
            @RequestBody CozeKnowledgeFileProgressRequest request) {
        request.setDatasetId(datasetId);
        CozeKnowledgeFileProgressResponse response = cozeService.getKnowledgeFileProgressBatch(datasetId, request.getDocumentIds());
        return CommonResult.success(response);
    }

    // =================================================================================
    // 知识库本地数据管理相关接口
    // =================================================================================

    @ApiOperation(value = "获取本地知识库列表")
   // @PreAuthorize("hasAuthority('merchant:coze:knowledge:view')")
    @GetMapping("/knowledge/local/list")
    public CommonResult<CommonPage<CozeKnowledge>> getLocalKnowledgeList(PageParamRequest pageParamRequest) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        PageInfo<CozeKnowledge> response = cozeKnowledgeService.getByMerchantId(merchantId, pageParamRequest);
        return CommonResult.success(CommonPage.restPage( response));
    }

    @ApiOperation(value = "获取本地知识库详情")
   // @PreAuthorize("hasAuthority('merchant:coze:knowledge:view')")
    @GetMapping("/knowledge/local/{id}")
    public CommonResult<CozeKnowledge> getLocalKnowledgeDetail(@PathVariable String id) {
        CozeKnowledge response = null;
        Integer merId = SecurityUtil.getLoginUserVo().getUser().getMerId();
        // 判断传入的是本地数据库ID还是Coze知识库ID
        try {
            // 先尝试按本地数据库ID查找
            Long localId = Long.parseLong(id);
            response = cozeKnowledgeService.getById(localId);
        } catch (NumberFormatException e) {
            // 如果不是数字，则按Coze知识库ID查找
            response = cozeKnowledgeService.getByCozeKnowledgeId(id, merId);
        }
        
        // 如果还是没找到，再尝试另一种方式
        if (response == null) {
            if (id.matches("\\d+")) {
                // 是数字但没找到，尝试按cozeKnowledgeId查找
                response = cozeKnowledgeService.getByCozeKnowledgeId(id, merId);
            } else {
                // 不是数字但没找到，尝试按本地ID查找（如果可能的话）
                try {
                    Long localId = Long.parseLong(id);
                    response = cozeKnowledgeService.getById(localId);
                } catch (NumberFormatException ex) {
                    // 忽略异常
                }
            }
        }
        
        return CommonResult.success(response);
    }

    @ApiOperation(value = "删除本地知识库")
  //  @PreAuthorize("hasAuthority('merchant:coze:knowledge:delete')")
    @DeleteMapping("/knowledge/local/{cozeKnowledgeId}")
    public CommonResult<Boolean> deleteLocalKnowledge(@PathVariable String cozeKnowledgeId) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean response = cozeKnowledgeService.deleteByKnowledgeId(cozeKnowledgeId, merchantId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "上传文件到知识库（支持多文件，最多10个）")
   // @PreAuthorize("hasAuthority('merchant:coze:knowledge:file:upload')")
    @PostMapping("/knowledge/{cozeKnowledgeId}/file/upload")
    public CommonResult<List<CozeKnowledgeFile>> uploadFileToKnowledge(
            @PathVariable String cozeKnowledgeId,
            @RequestParam("files") MultipartFile[] files) {
        try {
            // 验证文件数量
            if (files == null || files.length == 0) {
                return CommonResult.failed("请选择要上传的文件");
            }
            if (files.length > 10) {
                return CommonResult.failed("最多只能同时上传10个文件");
            }
            
            log.info("批量上传文件到知识库，知识库ID：{}，文件数量：{}", cozeKnowledgeId, files.length);
            
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Integer merchantId = loginUser.getUser().getMerId();
            
            List<CozeKnowledgeFile> response = cozeKnowledgeFileService.uploadMultipleFilesToKnowledge(cozeKnowledgeId, files, merchantId);
            
            log.info("批量文件上传到知识库成功，成功上传：{} 个文件", response.size());
            return CommonResult.success(response);
        } catch (Exception e) {
            log.error("批量上传文件到知识库失败，知识库ID：{}", cozeKnowledgeId, e);
            return CommonResult.failed("批量上传文件失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "检查文件处理进度")
   // @PreAuthorize("hasAuthority('merchant:coze:knowledge:file:progress')")
    @GetMapping("/knowledge/file/{cozeFileId}/progress")
    public CommonResult<CozeKnowledgeFile> checkFileProgress(@PathVariable String cozeFileId) {
        CozeKnowledgeFile response = cozeKnowledgeFileService.checkFileProgress(cozeFileId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "删除知识库文件")
 //   @PreAuthorize("hasAuthority('merchant:coze:knowledge:file:delete')")
    @DeleteMapping("/knowledge/file/{cozeFileId}")
    public CommonResult<Boolean> deleteKnowledgeFile(@PathVariable String cozeFileId) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean response = cozeKnowledgeFileService.deleteByFileId(cozeFileId, merchantId);
        return CommonResult.success(response);
    }

    // =================================================================================
    // 知识库同步相关接口
    // =================================================================================

    @ApiOperation(value = "同步知识库列表")
  //  @PreAuthorize("hasAuthority('merchant:coze:knowledge:sync')")
    @PostMapping("/knowledge/sync")
    public CommonResult<Boolean> syncKnowledgeList(@RequestBody @Valid CozeSyncKnowledgeRequest request) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean response = cozeKnowledgeService.syncKnowledgeListFromCoze(merchantId, request.getSpaceId());
        return CommonResult.success(response);
    }

    @ApiOperation(value = "同步知识库文件列表")
   // @PreAuthorize("hasAuthority('merchant:coze:knowledge:file:sync')")
    @PostMapping("/knowledge/file/sync")
    public CommonResult<Boolean> syncKnowledgeFileList(@RequestBody @Valid CozeSyncKnowledgeFileRequest request) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean response = cozeKnowledgeFileService.syncKnowledgeFileListFromCoze(merchantId, request.getCozeKnowledgeId());
        return CommonResult.success(response);
    }

    // =================================================================================
    // 智能体配置管理相关接口
    // =================================================================================

    @ApiOperation(value = "获取智能体配置")
  //  @PreAuthorize("hasAuthority('merchant:coze:bot:config:view')")
    @GetMapping("/bot/{botId}/config")
    public CommonResult<Object> getBotConfig(@PathVariable String botId) {
        Object response = cozeService.getBotConfig(botId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "获取本地智能体配置")
  //  @PreAuthorize("hasAuthority('merchant:coze:bot:config:view')")
    @GetMapping("/bot/{botId}/config/local")
    public CommonResult<CozeBotConfig> getLocalBotConfig(@PathVariable String botId) {
        CozeBotConfig response = cozeService.getLocalBotConfig(botId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "同步智能体配置到本地")
  //  @PreAuthorize("hasAuthority('merchant:coze:bot:config:sync')")
    @PostMapping("/bot/{botId}/config/sync")
    public CommonResult<Boolean> syncBotConfigToLocal(@PathVariable String botId, @RequestBody(required = false) Object configData) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean response = cozeService.syncBotConfigToLocal(botId, merchantId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "从Coze获取知识库列表（带强类型）")
    @PreAuthorize("hasAuthority('merchant:coze:knowledge:list')")
    @GetMapping("/knowledge/coze/list")
    public CommonResult<CozeKnowledgeListResponse> getCozeKnowledgeList(
            @RequestParam String spaceId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer formatType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        CozeKnowledgeListResponse response = cozeKnowledgeService.getKnowledgeListFromCoze(spaceId, name, formatType, pageNum, pageSize);
        return CommonResult.success(response);
    }


    @ApiOperation(value = "上传文件到Coze")
  //  @PreAuthorize("hasAuthority('merchant:coze:file:upload')")
    @PostMapping("/file/upload")
    public CommonResult<CozeUploadFileResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            log.info("上传文件到Coze，文件名：{}", file.getOriginalFilename());
            CozeUploadFileResponse response = cozeService.uploadFile(file);
            log.info("文件上传成功，文件ID：{}", response != null && response.getData() != null ? response.getData().getId() : "null");
            return CommonResult.success(response);
        } catch (Exception e) {
            log.error("上传文件到Coze失败", e);
            return CommonResult.failed("文件上传失败: " + e.getMessage());
        }
    }
    @ApiOperation(value = "获取知识库文件列表")
   // @PreAuthorize("hasAuthority('merchant:coze:knowledge:file:view')")
    @GetMapping("/knowledge/{cozeKnowledgeId}/files")
    public CommonResult<CommonPage<CozeKnowledgeFile>> getKnowledgeFiles(
            @PathVariable String cozeKnowledgeId,
            PageParamRequest pageParamRequest,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {
        try {
            // 先尝试获取本地文件列表（带搜索条件）
            PageInfo<CozeKnowledgeFile> response = cozeKnowledgeFileService.getByKnowledgeIdWithSearch(cozeKnowledgeId, pageParamRequest, name, type, status);
            
            // 如果本地没有文件记录，尝试从Coze同步一次
            if (response == null || response.getList() == null || response.getList().isEmpty()) {
                log.info("本地暂无文件记录，尝试从Coze同步文件列表，知识库ID：{}", cozeKnowledgeId);
                LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
                Integer merchantId = loginUser.getUser().getMerId();
                
                try {
                    Boolean syncResult = cozeKnowledgeFileService.syncKnowledgeFileListFromCoze(merchantId, cozeKnowledgeId);
                    if (syncResult) {
                        log.info("文件同步成功，重新获取文件列表，知识库ID：{}", cozeKnowledgeId);
                        // 同步成功后重新获取文件列表（带搜索条件）
                        response = cozeKnowledgeFileService.getByKnowledgeIdWithSearch(cozeKnowledgeId, pageParamRequest, name, type, status);
                    } else {
                        log.warn("文件同步失败，知识库ID：{}", cozeKnowledgeId);
                    }
                } catch (Exception syncException) {
                    log.warn("同步文件列表时出现异常，知识库ID：{}，错误：{}", cozeKnowledgeId, syncException.getMessage());
                }
            }
            
            return CommonResult.success(CommonPage.restPage(response));
        } catch (Exception e) {
            log.error("获取知识库文件列表失败，知识库ID：{}，错误信息：{}", cozeKnowledgeId, e.getMessage(), e);
            return CommonResult.failed("获取知识库文件列表失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "从Coze获取知识库文件列表（带强类型）")
     // @PreAuthorize("hasAuthority('merchant:coze:knowledge:file:list')")
     @GetMapping("/knowledge/{datasetId}/files/coze/list")
     public CommonResult<CozeKnowledgeFileListResponse> getCozeKnowledgeFileList(
             @PathVariable String datasetId,
             @RequestParam(defaultValue = "1") Integer pageNum,
             @RequestParam(defaultValue = "20") Integer pageSize) {
         try {
             log.info("获取Coze知识库文件列表，知识库ID：{}，页码：{}，页大小：{}", datasetId, pageNum, pageSize);
             CozeKnowledgeFileListResponse response = cozeKnowledgeFileService.getKnowledgeFileListFromCoze(datasetId, pageNum, pageSize);
             if (response == null) {
                 return CommonResult.failed("获取知识库文件列表失败，返回数据为空");
             }
             return CommonResult.success(response);
         } catch (Exception e) {
             log.error("从Coze获取知识库文件列表失败，知识库ID：{}，错误信息：{}", datasetId, e.getMessage(), e);
             return CommonResult.failed("获取知识库文件列表失败：" + e.getMessage());
         }
     }

    // =================================================================================
    // 工作流管理相关接口
    // =================================================================================

    @ApiOperation(value = "查询工作流列表 (Coze API)")
  //  @PreAuthorize("hasAuthority('merchant:coze:workflow:list')")
    @GetMapping("/workflow/list")
    public CommonResult<CozeGetWorkflowListResponse> getWorkflowList(@RequestParam String workspaceId,
                                                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                                                     @RequestParam(required = false) String workflowMode,
                                                                     @RequestParam(required = false) String appId,
                                                                     @RequestParam(required = false) String publishStatus) {
        CozeGetWorkflowListRequest request = CozeGetWorkflowListRequest.builder()
                .workspaceId(workspaceId)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .workflowMode(workflowMode)
                .appId(appId)
                .publishStatus(publishStatus)
                .build();
        CozeGetWorkflowListResponse response = cozeService.getWorkflowList(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "执行工作流")
  //  @PreAuthorize("hasAuthority('merchant:coze:workflow:execute')")
    @PostMapping("/workflow/execute")
    public CommonResult<CozeExecuteWorkflowResponse> executeWorkflow(@RequestBody CozeExecuteWorkflowRequest request) {
        CozeExecuteWorkflowResponse response = cozeService.runWorkflow(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "执行对话流")
 //   @PreAuthorize("hasAuthority('merchant:coze:workflow:execute')")
    @PostMapping("/workflow/execute/chat")
    public CommonResult<CozeExecuteWorkflowResponse> executeChatWorkflow(@RequestBody CozeExecuteWorkflowRequest request) {
        CozeExecuteWorkflowResponse response = cozeService.runChatWorkflow(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "获取本地工作流列表")
  //  @PreAuthorize("hasAuthority('merchant:coze:workflow:view')")
    @GetMapping("/workflow/local/list")
    public CommonResult<CommonPage<CozeWorkflow>> getLocalWorkflowList(PageParamRequest pageParamRequest) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        PageInfo<CozeWorkflow> response = cozeWorkflowService.getByMerchantId(merchantId, pageParamRequest);
        return CommonResult.success(CommonPage.restPage(response));
    }

    @ApiOperation(value = "获取本地工作流详情")
 //   @PreAuthorize("hasAuthority('merchant:coze:workflow:view')")
    @GetMapping("/workflow/local/detail/{cozeWorkflowId}")
    public CommonResult<CozeWorkflow> getLocalWorkflowDetail(@PathVariable String cozeWorkflowId) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        CozeWorkflow response = cozeWorkflowService.getByCozeWorkflowId(cozeWorkflowId, merchantId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "删除本地工作流")
  //  @PreAuthorize("hasAuthority('merchant:coze:workflow:delete')")
    @DeleteMapping("/workflow/local/delete/{cozeWorkflowId}")
    public CommonResult<Boolean> deleteLocalWorkflow(@PathVariable String cozeWorkflowId) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean response = cozeWorkflowService.deleteByCozeWorkflowId(cozeWorkflowId, merchantId);
        return CommonResult.success(response);
    }

    // =================================================================================
    // 会话管理相关接口
    // =================================================================================

    @ApiOperation(value = "创建会话")
  //  @PreAuthorize("hasAuthority('merchant:coze:conversation:create')")
    @PostMapping("/conversation/create")
    public CommonResult<CozeCreateConversationResponse> createConversation(@RequestBody @Validated CozeCreateConversationRequest request) {
        CozeCreateConversationResponse response = cozeService.createConversation(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "查看会话列表 (Coze API)")
  //  @PreAuthorize("hasAuthority('merchant:coze:conversation:list')")
    @PostMapping("/conversation/list")
    public CommonResult<CozeGetConversationListResponse> getConversationList(@RequestBody @Validated CozeGetConversationListRequest request) {
        CozeGetConversationListResponse response = cozeService.getConversationList(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "更新会话名称")
  //  @PreAuthorize("hasAuthority('merchant:coze:conversation:update')")
    @PostMapping("/conversation/update")
    public CommonResult<CozeUpdateConversationResponse> updateConversationName(@RequestBody @Validated CozeUpdateConversationRequest request) {
        CozeUpdateConversationResponse response = cozeService.updateConversationName(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "删除会话")
  //  @PreAuthorize("hasAuthority('merchant:coze:conversation:delete')")
    @DeleteMapping("/conversation/{conversationId}")
    public CommonResult<CozeBaseResponse> deleteConversation(@PathVariable String conversationId) {
        // 先调用Coze API删除
        CozeBaseResponse response = cozeService.deleteConversation(conversationId);
        
        // 如果API删除成功，也删除本地记录
        if (response != null && response.getCode() == 0) {
            LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
            Integer merchantId = loginUser.getUser().getMerId();
            cozeConversationService.deleteConversationByMerchant(conversationId, merchantId);
        }
        
        return CommonResult.success(response);
    }

    @ApiOperation(value = "查看本地会话列表")
  //  @PreAuthorize("hasAuthority('merchant:coze:conversation:list')")
    @PostMapping("/conversation/local/list")
    public CommonResult<CommonPage<CozeConversation>> getLocalConversationList(@RequestBody PageParamRequest pageRequest,
                                                                                @RequestParam(required = false) String botId) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        
        CommonPage<CozeConversation> page = CommonPage.restPage(cozeConversationService.getConversationsByMerchant(pageRequest, merchantId, botId));
        return CommonResult.success(page);
    }

    @ApiOperation(value = "查看本地会话详情")
  //  @PreAuthorize("hasAuthority('merchant:coze:conversation:detail')")
    @GetMapping("/conversation/local/detail/{conversationId}")
    public CommonResult<CozeConversation> getLocalConversationDetail(@PathVariable String conversationId) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        CozeConversation conversation = cozeConversationService.getByConversationIdAndMerchant(conversationId, merchantId);
        return CommonResult.success(conversation);
    }

    @ApiOperation(value = "删除本地会话")
  //  @PreAuthorize("hasAuthority('merchant:coze:conversation:delete')")
    @DeleteMapping("/conversation/local/{conversationId}")
    public CommonResult<Boolean> deleteLocalConversation(@PathVariable String conversationId) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean result = cozeConversationService.deleteConversationByMerchant(conversationId, merchantId);
        return CommonResult.success(result);
    }

    // =================================================================================
    // 消息管理相关接口
    // =================================================================================

    @ApiOperation(value = "创建消息")
  //  @PreAuthorize("hasAuthority('merchant:coze:message:create')")
    @PostMapping("/message/create")
    public CommonResult<CozeCreateMessageResponse> createMessage(@RequestBody @Validated CozeCreateMessageRequest request) {
        CozeCreateMessageResponse response = cozeService.createMessage(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "修改消息")
   // @PreAuthorize("hasAuthority('merchant:coze:message:modify')")
    @PostMapping("/message/modify")
    public CommonResult<CozeModifyMessageResponse> modifyMessage(@RequestBody @Validated CozeModifyMessageRequest request) {
        CozeModifyMessageResponse response = cozeService.modifyMessage(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "删除消息")
   // @PreAuthorize("hasAuthority('merchant:coze:message:delete')")
    @DeleteMapping("/message/{conversationId}/{messageId}")
    public CommonResult<CozeCreateMessageResponse> deleteMessage(@PathVariable String conversationId,
                                                                 @PathVariable String messageId) {
        // 先调用Coze API删除
        CozeCreateMessageResponse response = cozeService.deleteMessage(conversationId, messageId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "查看消息列表 (Coze API)")
   // @PreAuthorize("hasAuthority('merchant:coze:message:list')")
    @PostMapping("/message/list")
    public CommonResult<CozeGetMessageListResponse> getMessageList(@RequestBody @Validated CozeGetMessageListRequest request) {
        CozeGetMessageListResponse response = cozeService.getMessageList(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "查看消息详情")
 //   @PreAuthorize("hasAuthority('merchant:coze:message:detail')")
    @GetMapping("/message/detail/{conversationId}/{messageId}")
    public CommonResult<CozeGetMessageDetailResponse> getMessageDetail(@PathVariable String conversationId,
                                                                       @PathVariable String messageId) {
        CozeGetMessageDetailResponse response = cozeService.getMessageDetail(conversationId, messageId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "查看本地消息列表")
  //  @PreAuthorize("hasAuthority('merchant:coze:message:list')")
    @PostMapping("/message/local/list")
    public CommonResult<CommonPage<CozeMessage>> getLocalMessageList(@RequestBody PageParamRequest pageRequest,
                                                                     @RequestParam(required = false) String conversationId,
                                                                     @RequestParam(required = false) String messageType) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        
        CommonPage<CozeMessage> page = CommonPage.restPage(cozeMessageService.getMessagesByMerchant(pageRequest, merchantId, conversationId, messageType));
        return CommonResult.success(page);
    }

    @ApiOperation(value = "查看本地消息详情")
  //  @PreAuthorize("hasAuthority('merchant:coze:message:detail')")
    @GetMapping("/message/local/detail/{messageId}")
    public CommonResult<CozeMessage> getLocalMessageDetail(@PathVariable String messageId) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        CozeMessage message = cozeMessageService.getByMessageIdAndMerchant(messageId, merchantId);
        return CommonResult.success(message);
    }

    @ApiOperation(value = "删除本地消息")
  //  @PreAuthorize("hasAuthority('merchant:coze:message:delete')")
    @DeleteMapping("/message/local/{messageId}")
    public CommonResult<Boolean> deleteLocalMessage(@PathVariable String messageId) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean result = cozeMessageService.deleteMessageByMerchant(messageId, merchantId);
        return CommonResult.success(result);
    }

    @ApiOperation(value = "根据会话查看本地消息列表")
  //  @PreAuthorize("hasAuthority('merchant:coze:message:list')")
    @PostMapping("/message/local/conversation/{conversationId}")
    public CommonResult<CommonPage<CozeMessage>> getLocalMessagesByConversation(@PathVariable String conversationId,
                                                                                @RequestBody PageParamRequest pageRequest) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();

        CommonPage<CozeMessage> page = CommonPage.restPage(cozeMessageService.getMessagesByConversationAndMerchant(pageRequest, conversationId, merchantId));
        return CommonResult.success(page);
    }

    // =================================================================================
    // 对话相关接口
    // =================================================================================

    @ApiOperation(value = "发起对话")
  //  @PreAuthorize("hasAuthority('merchant:coze:chat:start')")
    @PostMapping("/chat/start")
    public CommonResult<Object> startChat(@RequestParam(required = false) String conversationId,
                                         @RequestBody @Validated CozeStartChatRequest request) {
        if (conversationId != null) {
            request.setConversationId(conversationId);
        }
        Object response = cozeService.startChat(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "取消进行中的对话")
   // @PreAuthorize("hasAuthority('merchant:coze:chat:cancel')")
    @PostMapping("/chat/cancel")
    public CommonResult<Object> cancelChat(@RequestBody @Validated CozeCancelChatRequest request) {
        Object response = cozeService.cancelChat(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "查看对话详情")
   // @PreAuthorize("hasAuthority('merchant:coze:chat:detail')")
    @GetMapping("/chat/detail")
    public CommonResult<Object> getChatDetail(@RequestParam String chatId, @RequestParam String conversationId) {
        Object response = cozeService.getChatDetail(conversationId, chatId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "查看对话消息详情")
  //  @PreAuthorize("hasAuthority('merchant:coze:chat:messages')")
    @GetMapping("/chat/messages")
    public CommonResult<Object> getChatMessages(@RequestParam String chatId,
                                               @RequestParam String conversationId,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "20") Integer pageSize) {
        Object response = cozeService.getChatMessages(conversationId, chatId, pageNum, pageSize);
        return CommonResult.success(response);
    }

    // =================================================================================
    // 其他相关接口
    // =================================================================================

    @ApiOperation(value = "查看空间列表")
   // @PreAuthorize("hasAuthority('merchant:coze:space:list')")
    @GetMapping("/space/list")
    public CommonResult<Object> getSpaceList(@RequestBody  CozeGetSpaceListRequest request ) {
        Object response = cozeService.getSpaceList(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "查看空间成员")
  //  @PreAuthorize("hasAuthority('merchant:coze:space:members')")
    @GetMapping("/space/{spaceId}/members")
    public CommonResult<Object> getSpaceMembers(@PathVariable String spaceId,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "20") Integer pageSize) {
        CozeGetSpaceMembersRequest request = new CozeGetSpaceMembersRequest();
        request.setWorkspaceId(spaceId);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        Object response = cozeService.getSpaceMembers(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "同步空间成员")
  //  @PreAuthorize("hasAuthority('merchant:coze:space:member:sync')")
    @PostMapping("/space/{spaceId}/members/sync")
    public CommonResult<Boolean> syncSpaceMembers(@PathVariable String spaceId) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean response = cozeSpaceMemberService.syncSpaceMembersFromCoze(merchantId, spaceId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "获取本地空间成员列表")
  //  @PreAuthorize("hasAuthority('merchant:coze:space:member:list')")
    @GetMapping("/space/{spaceId}/members/local")
    public CommonResult<CommonPage<CozeSpaceMember>> getLocalSpaceMembers(@PathVariable String spaceId,
                                                                          PageParamRequest pageParamRequest) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        PageInfo<CozeSpaceMember> response = cozeSpaceMemberService.getList(merchantId, spaceId, pageParamRequest);
        return CommonResult.success(CommonPage.restPage(response));
    }

    @ApiOperation(value = "删除空间成员")
  //  @PreAuthorize("hasAuthority('merchant:coze:space:member:delete')")
    @DeleteMapping("/space/member/{memberId}")
    public CommonResult<Boolean> deleteSpaceMember(@PathVariable Integer memberId) {
        LoginUserVo loginUser = SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean response = cozeSpaceMemberService.deleteById(memberId, merchantId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "查看应用列表")
  //  @PreAuthorize("hasAuthority('merchant:coze:app:list')")
    @GetMapping("/app/list")
    public CommonResult<Object> getAppList(@RequestParam String spaceId,
                                          @RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "20") Integer pageSize) {
        Object response = cozeService.getAppList(spaceId, pageNum, pageSize);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "添加发布渠道")
   // @PreAuthorize("hasAuthority('merchant:coze:channel:add')")
    @PostMapping("/channel/add")
    public CommonResult<CozeBaseResponse> addChannel(@RequestBody Object request) {
        CozeBaseResponse response = cozeService.addChannel(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "更新审核结果")
   // @PreAuthorize("hasAuthority('merchant:coze:audit:update')")
    @PutMapping("/audit/update")
    public CommonResult<CozeBaseResponse> updateAuditResult(@RequestBody Object request) {
        CozeBaseResponse response = cozeService.updateAuditResult(request);
        return CommonResult.success(response);
    }

    // =================================================================================
    // 空间管理接口
    // =================================================================================

    @ApiOperation(value = "同步Coze空间到本地")
   // @PreAuthorize("hasAuthority('merchant:coze:space:sync')")
    @PostMapping("/space/sync")
    public CommonResult<String> syncSpaces() {
        Integer merId = SecurityUtil.getLoginUserVo().getUser().getMerId();
        Boolean result = cozeSpaceService.syncCozeSpaces(merId);
        if (result) {
            return CommonResult.success("同步成功");
        } else {
            return CommonResult.failed("同步失败");
        }
    }

    @ApiOperation(value = "获取本地空间列表")
    //@PreAuthorize("hasAuthority('merchant:coze:space:local:list')")
    @GetMapping("/space/local/list")
    public CommonResult<CommonPage<CozeSpace>> getLocalSpaceList(@Validated PageParamRequest pageParamRequest) {
        CommonPage<CozeSpace> list = cozeSpaceService.getList(pageParamRequest);
        return CommonResult.success(list);
    }

    @ApiOperation(value = "获取当前商户的空间列表")
   // @PreAuthorize("hasAuthority('merchant:coze:space:merchant:list')")
    @GetMapping("/space/merchant/list")
    public CommonResult<List<CozeSpace>> getMerchantSpaceList() {
        Integer merId = SecurityUtil.getLoginUserVo().getUser().getMerId();
        List<CozeSpace> list = cozeSpaceService.getByMerId(merId);
        return CommonResult.success(list);
    }

    @ApiOperation(value = "获取空间详情")
   // @PreAuthorize("hasAuthority('merchant:coze:space:detail')")
    @GetMapping("/space/{spaceId}")
    public CommonResult<CozeSpace> getSpaceDetail(@PathVariable String spaceId) {
        Integer merId = SecurityUtil.getLoginUserVo().getUser().getMerId();
        CozeSpace space = cozeSpaceService.getBySpaceId(spaceId, merId);
        return CommonResult.success(space);
    }

    @ApiOperation(value = "删除本地空间记录")
  //  @PreAuthorize("hasAuthority('merchant:coze:space:delete')")
    @DeleteMapping("/space/local/{id}")
    public CommonResult<String> deleteLocalSpace(@PathVariable Integer id) {
        Boolean result = cozeSpaceService.deleteLocalSpace(id);
        if (result) {
            return CommonResult.success("删除成功");
        } else {
            return CommonResult.failed("删除失败");
        }
    }

}