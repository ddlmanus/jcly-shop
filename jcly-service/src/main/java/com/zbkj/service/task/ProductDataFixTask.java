package com.zbkj.service.task;

import cn.hutool.core.util.StrUtil;
import com.zbkj.common.model.product.Product;
import com.zbkj.common.model.product.ProductAttrValue;
import com.zbkj.common.model.product.ProductDescription;
import com.zbkj.service.service.ProductAttrValueService;
import com.zbkj.service.service.ProductDescriptionService;
import com.zbkj.service.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 商品数据修复定时任务
 * 用于修复迁移后的商品数据问题:
 * 1. 图片路径修复(添加CDN前缀)
 * 2. 分类ID格式修复
 * 3. 商品详情补充
 * 4. SKU图片同步
 */
@Slf4j
@Component
public class ProductDataFixTask {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private ProductDescriptionService productDescriptionService;

    /**
     * CDN地址配置,从配置文件读取
     */
    @Value("${file.cdn.url:}")
    private String cdnUrl;

    /**
     * 是否启用定时修复任务
     */
    @Value("${product.fix.task.enabled:false}")
    private Boolean taskEnabled;

    /**
     * 定时任务: 每天凌晨2点执行
     * 修复商品数据问题
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void fixProductData() {
        if (!taskEnabled) {
            log.info("商品数据修复定时任务已禁用");
            return;
        }

        log.info("========== 开始执行商品数据修复任务 ==========");

        try {
            // 1. 修复分类ID格式
            int fixedCategoryCount = fixCategoryId();
            log.info("修复分类ID完成,修复数量: {}", fixedCategoryCount);

            // 2. 修复图片路径
            int fixedImageCount = fixImagePath();
            log.info("修复图片路径完成,修复数量: {}", fixedImageCount);

            // 3. 补充商品详情
            int fixedDescCount = fixProductDescription();
            log.info("补充商品详情完成,补充数量: {}", fixedDescCount);

            // 4. 同步SKU图片
            int syncedSkuCount = syncSkuImage();
            log.info("同步SKU图片完成,同步数量: {}", syncedSkuCount);

            log.info("========== 商品数据修复任务完成 ==========");

        } catch (Exception e) {
            log.error("商品数据修复任务执行失败", e);
        }
    }

    /**
     * 修复分类ID格式
     * 将 "-66-100-" 格式转换为 "66"
     *
     * @return 修复数量
     */
    public int fixCategoryId() {
        log.info("开始修复分类ID格式...");

        // 查询所有分类ID格式错误的商品
        List<Product> products = productService.lambdaQuery()
                .like(Product::getCateId, "-%")
                .list();

        if (products.isEmpty()) {
            log.info("没有需要修复的分类ID");
            return 0;
        }

        int count = 0;
        for (Product product : products) {
            String cateId = product.getCateId();
            if (cateId == null || cateId.isEmpty()) {
                continue;
            }

            try {
                // 去除前后的 "-" 并分割
                String cleaned = cateId.replaceAll("^-+|-+$", "");
                String[] parts = cleaned.split("-");

                // 提取第一个有效的数字ID
                for (String part : parts) {
                    if (!part.isEmpty() && part.matches("\\d+")) {
                        Integer newCateId = Integer.parseInt(part);
                        product.setCateId(String.valueOf(newCateId));
                        productService.updateById(product);
                        count++;
                        log.info("商品ID={}, 分类ID从 \"{}\" 修复为 \"{}\"",
                                product.getId(), cateId, newCateId);
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("修复商品分类ID失败, 商品ID={}, 分类ID={}", product.getId(), cateId, e);
            }
        }

        return count;
    }

    /**
     * 修复图片路径(添加CDN前缀)
     * 新增商品接口的处理逻辑:
     * - 保存时: systemAttachmentService.clearPrefix() 去除CDN前缀
     * - 查询时: 前端自动拼接CDN前缀
     *
     * @return 修复数量
     */
    public int fixImagePath() {
        if (StrUtil.isBlank(cdnUrl)) {
            log.warn("CDN地址未配置,跳过图片路径修复");
            return 0;
        }

        log.info("开始修复图片路径,CDN地址: {}", cdnUrl);

        // 查询所有图片未包含http的商品
        List<Product> products = productService.lambdaQuery()
                .notLike(Product::getImage, "http%")
                .isNotNull(Product::getImage)
                .ne(Product::getImage, "")
                .list();

        if (products.isEmpty()) {
            log.info("没有需要修复的图片路径");
            return 0;
        }

        int count = 0;
        String cdnPrefix = cdnUrl.endsWith("/") ? cdnUrl : cdnUrl + "/";

        for (Product product : products) {
            try {
                String oldImage = product.getImage();
                String oldSlider = product.getSliderImage();

                // 添加CDN前缀
                if (StrUtil.isNotBlank(oldImage) && !oldImage.startsWith("http")) {
                    product.setImage(cdnPrefix + oldImage);
                }

                if (StrUtil.isNotBlank(oldSlider) && !oldSlider.startsWith("http")) {
                    product.setSliderImage(cdnPrefix + oldSlider);
                }

                if (StrUtil.isNotBlank(product.getFlatPattern()) && !product.getFlatPattern().startsWith("http")) {
                    product.setFlatPattern(cdnPrefix + product.getFlatPattern());
                }

                productService.updateById(product);
                count++;

                log.debug("商品ID={}, 图片路径已添加CDN前缀", product.getId());

            } catch (Exception e) {
                log.error("修复商品图片路径失败, 商品ID={}", product.getId(), e);
            }
        }

        log.info("图片路径修复完成,共修复 {} 个商品", count);
        return count;
    }

    /**
     * 补充商品详情
     * 为没有详情的商品添加默认详情(使用商品名称)
     *
     * @return 补充数量
     */
    public int fixProductDescription() {
        log.info("开始补充商品详情...");

        // 查询所有商品
        List<Product> products = productService.list();

        int count = 0;
        for (Product product : products) {
            try {
                // 检查是否已有详情
                ProductDescription description = productDescriptionService.getOne(
                        productDescriptionService.lambdaQuery()
                                .eq(ProductDescription::getProductId, product.getId())
                                .eq(ProductDescription::getType, product.getType())
                                .eq(ProductDescription::getMarketingType, product.getMarketingType())
                                .last("LIMIT 1")
                                .getWrapper()
                );

                // 如果没有详情或详情为空,则添加默认详情
                if (description == null) {
                    ProductDescription newDesc = new ProductDescription();
                    newDesc.setProductId(product.getId());
                    newDesc.setDescription("<p>" + product.getName() + "</p>");
                    newDesc.setType(product.getType());
                    newDesc.setMarketingType(product.getMarketingType());
                    productDescriptionService.save(newDesc);
                    count++;
                    log.info("商品ID={}, 已添加默认详情", product.getId());
                } else if (StrUtil.isBlank(description.getDescription()) || "[]".equals(description.getDescription())) {
                    description.setDescription("<p>" + product.getName() + "</p>");
                    productDescriptionService.updateById(description);
                    count++;
                    log.info("商品ID={}, 已更新空详情", product.getId());
                }

            } catch (Exception e) {
                log.error("补充商品详情失败, 商品ID={}", product.getId(), e);
            }
        }

        log.info("商品详情补充完成,共补充 {} 个商品", count);
        return count;
    }

    /**
     * 同步SKU图片
     * 将商品主图同步到默认SKU的图片字段
     *
     * @return 同步数量
     */
    public int syncSkuImage() {
        log.info("开始同步SKU图片...");

        // 查询所有默认SKU
        List<ProductAttrValue> skuList = productAttrValueService.lambdaQuery()
                .eq(ProductAttrValue::getIsDefault, true)
                .list();

        if (skuList.isEmpty()) {
            log.info("没有需要同步的SKU");
            return 0;
        }

        int count = 0;
        for (ProductAttrValue sku : skuList) {
            try {
                // 获取商品信息
                Product product = productService.getById(sku.getProductId());
                if (product == null) {
                    continue;
                }

                // 如果SKU图片为空或与商品主图不同,则同步
                if (StrUtil.isBlank(sku.getImage()) || !sku.getImage().equals(product.getImage())) {
                    sku.setImage(product.getImage());
                    productAttrValueService.updateById(sku);
                    count++;
                    log.debug("商品ID={}, SKU图片已同步", product.getId());
                }

            } catch (Exception e) {
                log.error("同步SKU图片失败, SKUID={}", sku.getId(), e);
            }
        }

        log.info("SKU图片同步完成,共同步 {} 个SKU", count);
        return count;
    }

    /**
     * 手动触发修复任务的接口方法
     * 可以在Controller中调用此方法,实现手动触发
     *
     * @return 修复报告
     */
    public String manualFix() {
        log.info("手动触发商品数据修复任务");

        StringBuilder report = new StringBuilder();
        report.append("商品数据修复报告:\n");

        try {
            int fixedCategoryCount = fixCategoryId();
            report.append("- 修复分类ID: ").append(fixedCategoryCount).append(" 个\n");

            int fixedImageCount = fixImagePath();
            report.append("- 修复图片路径: ").append(fixedImageCount).append(" 个\n");

            int fixedDescCount = fixProductDescription();
            report.append("- 补充商品详情: ").append(fixedDescCount).append(" 个\n");

            int syncedSkuCount = syncSkuImage();
            report.append("- 同步SKU图片: ").append(syncedSkuCount).append(" 个\n");

            report.append("\n修复任务执行成功!");

        } catch (Exception e) {
            report.append("\n修复任务执行失败: ").append(e.getMessage());
            log.error("手动修复任务执行失败", e);
        }

        return report.toString();
    }
}
