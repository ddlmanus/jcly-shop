package com.zbkj.admin.task;

import com.zbkj.service.service.JustuitanErpService;
import com.zbkj.service.service.ProductCategoryService;
import com.zbkj.common.model.product.ProductCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 聚水潭商品分类上传定时任务
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
@Component("JstCategoryTask")
public class JstCategoryTask {

    //日志
    private static final Logger logger = LoggerFactory.getLogger(JstCategoryTask.class);
    
    @Autowired
    private JustuitanErpService justuitanErpService;
    
    @Autowired
    private ProductCategoryService productCategoryService;
    
    /**
     * 批量上传所有商品分类到聚水潭
     */
    public void uploadAllCategoriesToJst() {
        logger.info("开始批量上传所有商品分类到聚水潭");
        try {
            // 获取所有商品分类
            List<ProductCategory> categoryList = productCategoryService.getAdminList();
            
            if (categoryList == null || categoryList.isEmpty()) {
                logger.info("没有找到商品分类，跳过上传");
                return;
            }
            
            int successCount = 0;
            int failCount = 0;
            
            // 逐个上传分类到聚水潭
            for (ProductCategory category : categoryList) {
                try {
                    Boolean result = justuitanErpService.uploadCategoryToJst(category);
                    if (result != null && result) {
                        successCount++;
                        logger.debug("分类上传成功: ID={}, 名称={}", category.getId(), category.getName());
                    } else {
                        failCount++;
                        logger.warn("分类上传失败: ID={}, 名称={}", category.getId(), category.getName());
                    }
                } catch (Exception e) {
                    failCount++;
                    logger.error("分类上传异常: ID={}, 名称={}", category.getId(), category.getName(), e);
                }
            }
            
            logger.info("批量上传商品分类到聚水潭完成，总数: {}, 成功: {}, 失败: {}", 
                categoryList.size(), successCount, failCount);
                
        } catch (Exception e) {
            logger.error("批量上传商品分类到聚水潭异常", e);
        }
    }
}