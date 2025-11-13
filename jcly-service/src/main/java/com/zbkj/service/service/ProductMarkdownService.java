package com.zbkj.service.service;

import com.zbkj.common.model.product.Product;

/**
 * 商品Markdown文件服务接口
 */
public interface ProductMarkdownService {
    
    /**
     * 生成并上传商品信息的Markdown文件到阿里云OSS
     * @param product 商品信息
     * @return 上传成功返回文件访问URL，失败返回null
     */
    String generateAndUploadProductMarkdown(Product product);
    
    /**
     * 删除商品的Markdown文件
     * @param productId 商品ID
     * @return 删除是否成功
     */
    Boolean deleteProductMarkdown(Integer productId);
}
