package com.zbkj.admin.controller.merchant;

import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.AsyncTaskService;
import com.zbkj.service.service.ExportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * 商户端商品模板控制器
 */
@Slf4j
@RestController
@RequestMapping("api/admin/merchant/product/template")
@Api(tags = "商户端商品模板控制器")
public class MerchantProductTemplateController {

    @Autowired
    private ExportService exportService;

    @Autowired
    private AsyncTaskService asyncTaskService;

    @ApiOperation(value = "下载商品导入模板")
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void downloadImportTemplate(HttpServletResponse response) throws UnsupportedEncodingException {
        exportService.downloadProductImportTemplate(response);
    }

    @ApiOperation(value = "异步生成商品导入模板")
    @RequestMapping(value = "/async/generate", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> asyncGenerateImportTemplate() {
        String taskId = asyncTaskService.generateProductImportTemplateAsync();
        log.info("模板生成任务已提交，请使用任务ID查询进度：{}", taskId);
        Map<String, Object> result=new HashMap<>();
        result.put("taskId", taskId);
        result.put("message", "模板生成任务已提交，请使用任务ID查询进度");
       return CommonResult.success(result);
    }

    @ApiOperation(value = "查询模板生成任务状态")
    @RequestMapping(value = "/task/status/{taskId}", method = RequestMethod.GET)
    public CommonResult<Map<String, Object>> getTaskStatus(@PathVariable String taskId) {
        Map<String, Object> status = asyncTaskService.getTaskStatus(taskId);
        return CommonResult.success(status);
    }
}