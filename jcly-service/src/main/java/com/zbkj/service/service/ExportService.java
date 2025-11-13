package com.zbkj.service.service;


import com.zbkj.common.request.OrderSearchRequest;
import com.zbkj.common.request.PlatProductSearchRequest;
import com.zbkj.common.vo.ProductImportResultVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
* StoreProductService 接口
*  +----------------------------------------------------------------------
 *  | JCLY [ JCLY赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2022 https://www.ddlmanus.xyz All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 *  +----------------------------------------------------------------------
 *  | Author: dudl
 *  +----------------------------------------------------------------------
*/
public interface ExportService {

    /**
     * 订单导出
     * @param request 查询条件
     * @return 文件名称
     */
    String exportOrder(OrderSearchRequest request);

    /**
     * 商品导出
     * @param request 查询条件
     * @return 文件名称
     */
    String exportProduct(PlatProductSearchRequest request);

    /**
     * 商品导出到输出流
     * @param request 查询条件
     */
    String exportProductToStream(PlatProductSearchRequest request, HttpServletResponse response) throws UnsupportedEncodingException;

    /**
     * 生成商品导出文件并返回文件URL
     * @param request 查询条件
     * @return 文件访问URL
     */
    String generateProductExport(PlatProductSearchRequest request);

    /**
     * 生成商品导入模板
     * @param response HttpServletResponse
     */
    void downloadProductImportTemplate(HttpServletResponse response) throws UnsupportedEncodingException;
    
    /**
     * 生成商品导入模板文件并上传到服务器
     * @return 文件访问URL
     */
    String generateProductImportTemplate();

    /**
     * 商品批量导入（同步）
     * @param file Excel文件
     * @param merId 商户ID
     * @return 导入结果
     */
    ProductImportResultVo importProducts(MultipartFile file, Integer merId);

    /**
     * 商品批量导入（异步）
     * @param file Excel文件
     * @param merId 商户ID
     * @return 任务ID
     */
    String importProductsAsync(MultipartFile file, Integer merId);

    /**
     * 获取导入任务状态
     * @param taskId 任务ID
     * @return 任务状态信息
     */
    Map<String, Object> getImportTaskStatus(String taskId);
    
    /**
     * 导出商品导入错误数据
     * @param errorList 错误数据列表
     * @return 文件访问URL
     */
    String exportProductImportErrors(List<ProductImportResultVo.ProductImportErrorVo> errorList);
}
