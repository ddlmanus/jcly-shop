package com.zbkj.service.service.impl;

import cn.hutool.core.util.StrUtil;
import com.zbkj.common.constants.SysConfigConstants;
import com.zbkj.common.model.coze.CozeKnowledgeFile;
import com.zbkj.common.model.product.Product;
import com.zbkj.common.model.product.ProductAttrValue;
import com.zbkj.common.model.product.ProductDescription;
import com.zbkj.common.vo.CloudVo;
import com.zbkj.service.service.*;
import com.zbkj.service.util.ProductMarkdownGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * 商品Markdown文件服务实现类 - 支持上传到Coze知识库
 */
@Service
public class ProductMarkdownServiceImpl implements ProductMarkdownService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductMarkdownServiceImpl.class);
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Autowired
    private OssService ossService;
    
    @Autowired
    private ProductAttrValueService productAttrValueService;
    
    @Autowired
    private ProductDescriptionService productDescriptionService;
    
    @Autowired
    private ProductBrandService productBrandService;
    
    @Autowired
    private ProductCategoryService productCategoryService;
    
    @Autowired
    private CozeKnowledgeFileService cozeKnowledgeFileService;
    
    /**
     * 生成并上传商品信息的Markdown文件到Coze知识库
     */
    @Override
    public String  generateAndUploadProductMarkdown(Product product) {
        if (product == null || product.getId() == null) {
            logger.warn("商品信息为空或商品ID为空，无法生成Markdown文件");
            return null;
        }
        
        try {
            // 1. 检查是否配置了Coze知识库ID
            String cozeKnowledgeId = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_COZE_PRODUCT_KNOWLEDGE_ID);
            if (StrUtil.isBlank(cozeKnowledgeId)) {
                logger.warn("未配置Coze知识库ID，无法上传商品Markdown文件，商品ID：{}", product.getId());
                return null;
            }
            
            // 2. 收集商品相关信息
            List<ProductAttrValue> attrValueList = productAttrValueService.getListByProductId(product.getId());
            ProductDescription productDescription = productDescriptionService.getByProductIdAndType(product.getId(), product.getType());
            
            String brandName = "";
            if (product.getBrandId() != null && product.getBrandId() > 0) {
                try {
                    brandName = productBrandService.getById(product.getBrandId()).getName();
                } catch (Exception e) {
                    logger.warn("获取品牌名称失败：{}", e.getMessage());
                }
            }
            
            String categoryName = "";
            if (product.getCategoryId() != null && product.getCategoryId() > 0) {
                try {
                    categoryName = productCategoryService.getById(product.getCategoryId()).getName();
                } catch (Exception e) {
                    logger.warn("获取分类名称失败：{}", e.getMessage());
                }
            }
            
            // 3. 生成Markdown内容
            String markdownContent = ProductMarkdownGenerator.generateProductMarkdown(
                product, productDescription, attrValueList, brandName, categoryName);
            
            if (StrUtil.isBlank(markdownContent)) {
                logger.error("生成的Markdown内容为空，商品ID：{}", product.getId());
                return null;
            }
            
            // 4. 创建临时文件
            String fileName = ProductMarkdownGenerator.generateFileName(product.getId());
            File tempFile = createTempMarkdownFile(fileName, markdownContent);
            
            if (tempFile == null) {
                logger.error("创建临时Markdown文件失败，商品ID：{}", product.getId());
                return null;
            }
            
            // 5. 上传到Coze知识库
            CozeKnowledgeFile cozeFile = uploadToCozeKnowledge(cozeKnowledgeId, tempFile, product.getMerId());
            
            // 6. 清理临时文件
            if (tempFile.exists()) {
                tempFile.delete();
            }
            
            if (cozeFile != null) {
                logger.info("商品Markdown文件上传到Coze知识库成功，商品ID：{}，文件ID：{}", 
                    product.getId(), cozeFile.getCozeFileId());
                // 返回Coze文件ID作为标识
                return cozeFile.getCozeFileId();
            } else {
                logger.error("商品Markdown文件上传到Coze知识库失败，商品ID：{}", product.getId());
                return null;
            }
            
        } catch (Exception e) {
            logger.error("生成并上传商品Markdown文件到Coze知识库异常，商品ID：{}", product.getId(), e);
            return null;
        }
    }
    
    /**
     * 删除商品的Markdown文件
     */
    @Override
    public Boolean deleteProductMarkdown(Integer productId) {
        if (productId == null) {
            return false;
        }
        
        try {
            // 这里可以实现Coze知识库文件删除逻辑
            // 目前暂时返回true，后续可以根据需要实现具体的删除逻辑
            logger.info("商品Markdown文件删除请求，商品ID：{}", productId);
            return true;
        } catch (Exception e) {
            logger.error("删除商品Markdown文件异常，商品ID：{}", productId, e);
            return false;
        }
    }
    
    /**
     * 创建临时Markdown文件
     */
    private File createTempMarkdownFile(String fileName, String content) {
        try {
            // 创建临时目录
            String tempDir = System.getProperty("java.io.tmpdir");
            File tempFile = new File(tempDir, fileName);
            
            // 写入内容
            try (FileWriter writer = new FileWriter(tempFile, false)) {
                writer.write(content);
                writer.flush();
            }
            
            logger.debug("临时Markdown文件创建成功：{}", tempFile.getAbsolutePath());
            return tempFile;
            
        } catch (IOException e) {
            logger.error("创建临时Markdown文件失败：{}", fileName, e);
            return null;
        }
    }
    
    /**
     * 上传文件到Coze知识库
     */
    private CozeKnowledgeFile uploadToCozeKnowledge(String cozeKnowledgeId, File file, Integer merchantId) {
        try {
            logger.info("开始上传Markdown文件到Coze知识库：{}，文件：{}", cozeKnowledgeId, file.getName());
            
            // 使用CozeKnowledgeFileService上传本地文件
            CozeKnowledgeFile cozeFile = cozeKnowledgeFileService.uploadLocalFileToKnowledge(
                cozeKnowledgeId, file, merchantId);
            
            if (cozeFile != null) {
                logger.info("Markdown文件上传到Coze知识库成功：文件ID={}，文件名={}", 
                    cozeFile.getCozeFileId(), cozeFile.getFileName());
            } else {
                logger.error("Markdown文件上传到Coze知识库失败，返回的文件对象为null");
            }
            
            return cozeFile;
            
        } catch (Exception e) {
            logger.error("上传Markdown文件到Coze知识库失败：{}", file.getName(), e);
            return null;
        }
    }
}
