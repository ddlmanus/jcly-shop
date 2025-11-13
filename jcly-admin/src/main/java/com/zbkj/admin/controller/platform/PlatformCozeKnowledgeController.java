package com.zbkj.admin.controller.platform;

import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeKnowledge;
import com.zbkj.common.model.coze.CozeKnowledgeFile;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.coze.*;
import com.zbkj.common.response.coze.*;
import com.zbkj.common.result.CommonResult;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.common.vo.LoginUserVo;
import com.zbkj.service.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

/**
 * 平台端 - Coze知识库管理控制器
 */
@Slf4j
@RestController
@RequestMapping("api/admin/platform/coze/knowledge")
@Api(tags = "平台端 - Coze知识库管理")
public class PlatformCozeKnowledgeController {

    @Autowired
    private CozeService cozeService;
    
    @Autowired
    private CozeKnowledgeService cozeKnowledgeService;
    
    @Autowired
    private CozeKnowledgeFileService cozeKnowledgeFileService;
    @ApiOperation(value = "获取知识库文件列表")
    // @PreAuthorize("hasAuthority('merchant:coze:knowledge:file:view')")
    @GetMapping("/{cozeKnowledgeId}/files")
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
    @GetMapping("/{datasetId}/files/coze/list")
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
    // 知识库API管理相关接口
    // =================================================================================

    @ApiOperation(value = "创建知识库")
    // @PreAuthorize("hasAuthority('platform:coze:knowledge:create')")
    @PostMapping("/create")
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
    // @PreAuthorize("hasAuthority('platform:coze:knowledge:update')")
    @PutMapping("/{datasetId}")
    public CommonResult<CozeBaseResponse> updateKnowledge(@PathVariable String datasetId, @RequestBody @Validated CozeUpdateKnowledgeRequest request) {
        request.setDatasetId(datasetId);
        CozeBaseResponse response = cozeService.updateKnowledge(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "删除知识库")
    // @PreAuthorize("hasAuthority('platform:coze:knowledge:delete')")
    @DeleteMapping("/{datasetId}")
    public CommonResult<CozeBaseResponse> deleteKnowledge(@PathVariable String datasetId) {
        CozeBaseResponse response = cozeService.deleteKnowledge(datasetId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "创建知识库文件")
    // @PreAuthorize("hasAuthority('platform:coze:knowledge:file:create')")
    @PostMapping("/file/create")
    public CommonResult<CozeCreateKnowledgeFileResponse> createKnowledgeFile(@RequestBody @Validated CozeCreateKnowledgeFileRequest request) {
        CozeCreateKnowledgeFileResponse response = cozeService.createKnowledgeFile(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "修改知识库文件")
    // @PreAuthorize("hasAuthority('platform:coze:knowledge:file:update')")
    @PutMapping("/file/update")
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
    // @PreAuthorize("hasAuthority('platform:coze:knowledge:file:progress')")
    @PostMapping("/{datasetId}/file/progress")
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
    // @PreAuthorize("hasAuthority('platform:coze:knowledge:view')")
    @GetMapping("/list")
    public CommonResult<CommonPage<CozeKnowledge>> getKnowledgeList(PageParamRequest pageParamRequest) {
        // 平台端使用登录用户ID作为merchantId
        com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        PageInfo<CozeKnowledge> response = cozeKnowledgeService.getByMerchantId(merchantId, pageParamRequest);
        return CommonResult.success(CommonPage.restPage(response));
    }

    @ApiOperation(value = "获取本地知识库列表（别名）")
    // @PreAuthorize("hasAuthority('platform:coze:knowledge:view')")
    @GetMapping("/local/list")
    public CommonResult<CommonPage<CozeKnowledge>> getLocalKnowledgeList(PageParamRequest pageParamRequest) {
        // 平台端使用登录用户ID作为merchantId
        com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        PageInfo<CozeKnowledge> response = cozeKnowledgeService.getByMerchantId(merchantId, pageParamRequest);
        return CommonResult.success(CommonPage.restPage(response));
    }

    @ApiOperation(value = "获取本地知识库详情")
    // @PreAuthorize("hasAuthority('platform:coze:knowledge:view')")
    @GetMapping("/local/{id}")
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
    // @PreAuthorize("hasAuthority('platform:coze:knowledge:delete')")
    @DeleteMapping("/local/{cozeKnowledgeId}")
    public CommonResult<Boolean> deleteLocalKnowledge(@PathVariable String cozeKnowledgeId) {
        // 平台端使用登录用户ID作为merchantId
        com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean response = cozeKnowledgeService.deleteByKnowledgeId(cozeKnowledgeId, merchantId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "上传文件到知识库（支持多文件，最多10个）")
    // @PreAuthorize("hasAuthority('platform:coze:knowledge:file:upload')")
    @PostMapping("/{cozeKnowledgeId}/file/upload")
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
            
            // 平台端使用登录用户ID作为merchantId
        com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
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
    // @PreAuthorize("hasAuthority('platform:coze:knowledge:file:progress')")
    @GetMapping("/file/{cozeFileId}/progress")
    public CommonResult<CozeKnowledgeFile> checkFileProgress(@PathVariable String cozeFileId) {
        CozeKnowledgeFile response = cozeKnowledgeFileService.checkFileProgress(cozeFileId);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "删除知识库文件")
    // @PreAuthorize("hasAuthority('platform:coze:knowledge:file:delete')")
    @DeleteMapping("/file/{cozeFileId}")
    public CommonResult<Boolean> deleteKnowledgeFile(@PathVariable String cozeFileId) {
        // 平台端使用登录用户ID作为merchantId
        com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean response = cozeKnowledgeFileService.deleteByFileId(cozeFileId, merchantId);
        return CommonResult.success(response);
    }

    // =================================================================================
    // 知识库同步相关接口
    // =================================================================================

    @ApiOperation(value = "同步知识库列表")
    // @PreAuthorize("hasAuthority('platform:coze:knowledge:sync')")
    @PostMapping("/sync")
    public CommonResult<Boolean> syncKnowledgeList(@RequestBody @Valid CozeSyncKnowledgeRequest request) {
        // 平台端使用登录用户ID作为merchantId
        com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean response = cozeKnowledgeService.syncKnowledgeListFromCoze(merchantId, request.getSpaceId());
        return CommonResult.success(response);
    }

    @ApiOperation(value = "同步知识库文件列表")
    // @PreAuthorize("hasAuthority('platform:coze:knowledge:file:sync')")
    @PostMapping("/file/sync")
    public CommonResult<Boolean> syncKnowledgeFileList(@RequestBody @Valid CozeSyncKnowledgeFileRequest request) {
        // 平台端使用登录用户ID作为merchantId
        com.zbkj.common.vo.LoginUserVo loginUser = com.zbkj.common.utils.SecurityUtil.getLoginUserVo();
        Integer merchantId = loginUser.getUser().getMerId();
        Boolean response = cozeKnowledgeFileService.syncKnowledgeFileListFromCoze(merchantId, request.getCozeKnowledgeId());
        return CommonResult.success(response);
    }
}