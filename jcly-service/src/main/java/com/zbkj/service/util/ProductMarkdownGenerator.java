package com.zbkj.service.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.zbkj.common.model.product.Product;
import com.zbkj.common.model.product.ProductAttrValue;
import com.zbkj.common.model.product.ProductDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品信息Markdown文件生成器
 * 用于将商品信息转换为适合AI知识库的Markdown格式
 */
public class ProductMarkdownGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductMarkdownGenerator.class);
    
    /**
     * 生成商品信息的Markdown内容
     * @param product 商品信息
     * @param productDescription 商品描述
     * @param attrValueList 商品规格信息
     * @param brandName 品牌名称
     * @param categoryName 分类名称
     * @return Markdown格式的商品信息
     */
    public static String generateProductMarkdown(Product product, 
                                               ProductDescription productDescription,
                                               List<ProductAttrValue> attrValueList,
                                               String brandName,
                                               String categoryName) {
        if (product == null) {
            logger.warn("商品信息为空，无法生成Markdown");
            return "";
        }
        
        StringBuilder markdown = new StringBuilder();
        
        // 文档头部
        markdown.append("# ").append(product.getName()).append("\n\n");
        
        // 基本信息
        markdown.append("## 基本信息\n\n");
        markdown.append("| 字段 | 值 |\n");
        markdown.append("|------|----|\n");
        markdown.append("| 商品ID | ").append(product.getId()).append(" |\n");
        markdown.append("| 商品名称 | ").append(product.getName()).append(" |\n");
        
        if (StrUtil.isNotBlank(product.getIntro())) {
            markdown.append("| 商品简介 | ").append(product.getIntro()).append(" |\n");
        }
        
        if (StrUtil.isNotBlank(brandName)) {
            markdown.append("| 品牌 | ").append(brandName).append(" |\n");
        }
        
        if (StrUtil.isNotBlank(categoryName)) {
            markdown.append("| 分类 | ").append(categoryName).append(" |\n");
        }
        
        markdown.append("| 价格 | ").append(formatPrice(product.getPrice())).append(" |\n");
        
        if (product.getOtPrice() != null && product.getOtPrice().compareTo(BigDecimal.ZERO) > 0) {
            markdown.append("| 原价 | ").append(formatPrice(product.getOtPrice())).append(" |\n");
        }
        
        if (product.getVipPrice() != null && product.getVipPrice().compareTo(BigDecimal.ZERO) > 0) {
            markdown.append("| 会员价 | ").append(formatPrice(product.getVipPrice())).append(" |\n");
        }
        
        markdown.append("| 库存 | ").append(product.getStock()).append(" |\n");
        markdown.append("| 销量 | ").append(product.getSales()).append(" |\n");
        
        if (StrUtil.isNotBlank(product.getUnitName())) {
            markdown.append("| 单位 | ").append(product.getUnitName()).append(" |\n");
        }
        
        markdown.append("| 状态 | ").append("上架").append(" |\n");
        
        if (StrUtil.isNotBlank(product.getKeyword())) {
            markdown.append("| 关键词 | ").append(product.getKeyword()).append(" |\n");
        }
        
        markdown.append("| 创建时间 | ").append(DateUtil.formatDateTime(product.getCreateTime())).append(" |\n");
        markdown.append("| 更新时间 | ").append(DateUtil.formatDateTime(product.getUpdateTime())).append(" |\n");
        
        markdown.append("\n");
        
        // 商品特性
        markdown.append("## 商品特性\n\n");
        markdown.append("- **商品类型**: ").append(getProductType(product.getType())).append("\n");
        markdown.append("- **规格类型**: ").append(product.getSpecType() ? "多规格" : "单规格").append("\n");
        markdown.append("- **配送方式**: ").append(getDeliveryMethod(product.getDeliveryMethod())).append("\n");
        
        if (product.getIsPaidMember() != null && product.getIsPaidMember()) {
            markdown.append("- **会员专享**: 是\n");
        }
        
        if (product.getLimitSwith() != null && product.getLimitSwith()) {
            markdown.append("- **限购**: 是，限购").append(product.getLimitNum()).append("件\n");
        }
        
        if (product.getPostageSwith() != null && product.getPostageSwith()) {
            markdown.append("- **包邮**: 是\n");
        }
        
        if (product.getRefundSwitch() != null && product.getRefundSwitch()) {
            markdown.append("- **支持退款**: 是\n");
        }
        
        if (product.getIsHot() != null && product.getIsHot() == 1) {
            markdown.append("- **热门推荐**: 是\n");
        }
        
        markdown.append("\n");
        
        // 规格信息
        if (attrValueList != null && !attrValueList.isEmpty()) {
            markdown.append("## 商品规格\n\n");
            markdown.append("| 规格名称 | SKU | 价格 | 库存 | 重量 |\n");
            markdown.append("|----------|-----|------|------|------|\n");
            
            for (ProductAttrValue attrValue : attrValueList) {
                markdown.append("| ").append(StrUtil.blankToDefault(attrValue.getAttrValue(), "默认"))
                        .append(" | ").append(StrUtil.blankToDefault(attrValue.getSku(), ""))
                        .append(" | ").append(formatPrice(attrValue.getPrice()))
                        .append(" | ").append(attrValue.getStock())
                        .append(" | ").append(attrValue.getWeight() != null ? attrValue.getWeight() + "kg" : "")
                        .append(" |\n");
            }
            markdown.append("\n");
        }
        
        // 商品详情
        if (productDescription != null && StrUtil.isNotBlank(productDescription.getDescription())) {
            markdown.append("## 商品详情\n\n");
            // 清理HTML标签，保留文本内容
            String cleanDescription = cleanHtmlContent(productDescription.getDescription());
            markdown.append(cleanDescription).append("\n\n");
        }
        
        // 地址信息（如果有）
        if (StrUtil.isNotBlank(product.getProvince()) || StrUtil.isNotBlank(product.getCity())) {
            markdown.append("## 商品地址\n\n");
            if (StrUtil.isNotBlank(product.getProvince())) {
                markdown.append("- **省份**: ").append(product.getProvince()).append("\n");
            }
            if (StrUtil.isNotBlank(product.getCity())) {
                markdown.append("- **城市**: ").append(product.getCity()).append("\n");
            }
            if (StrUtil.isNotBlank(product.getArea())) {
                markdown.append("- **区域**: ").append(product.getArea()).append("\n");
            }
            if (StrUtil.isNotBlank(product.getDetail())) {
                markdown.append("- **详细地址**: ").append(product.getDetail()).append("\n");
            }
            markdown.append("\n");
        }
        
        // 文档结尾
        markdown.append("---\n");
        markdown.append("*此文档由系统自动生成，用于AI知识库*\n");
        markdown.append("*生成时间: ").append(DateUtil.now()).append("*\n");
        
        return markdown.toString();
    }
    
    /**
     * 格式化价格
     */
    private static String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0.00";
        }
        return "￥" + price.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }
    
    /**
     * 获取商品类型描述
     */
    private static String getProductType(Integer type) {
        if (type == null) {
            return "普通商品";
        }
        switch (type) {
            case 0: return "普通商品";
            case 1: return "秒杀商品";
            case 2: return "砍价商品";
            case 3: return "拼团商品";
            case 4: return "视频号商品";
            case 5: return "云盘商品";
            case 6: return "卡密商品";
            default: return "未知类型";
        }
    }
    
    /**
     * 获取配送方式描述
     */
    private static String getDeliveryMethod(String deliveryMethod) {
        if (StrUtil.isBlank(deliveryMethod)) {
            return "未设置";
        }
        switch (deliveryMethod) {
            case "1": return "商家配送";
            case "2": return "到店核销";
            case "3": return "快递发货";
            case "4": return "同城配送";
            default: return deliveryMethod;
        }
    }
    
    /**
     * 清理HTML内容，保留文本
     */
    private static String cleanHtmlContent(String htmlContent) {
        if (StrUtil.isBlank(htmlContent)) {
            return "";
        }
        
        // 简单的HTML标签清理
        String cleaned = htmlContent
                .replaceAll("<[^>]+>", "") // 移除HTML标签
                .replaceAll("&nbsp;", " ") // 替换HTML空格
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&")
                .replaceAll("&quot;", "\"")
                .replaceAll("\\s+", " ") // 多个空格替换为单个空格
                .trim();
        
        return cleaned;
    }
    
    /**
     * 生成文件名
     * @param productId 商品ID
     * @return Markdown文件名
     */
    public static String generateFileName(Integer productId) {
        return "product_" + productId + ".md";
    }
}
