package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zbkj.common.model.coze.CozeKnowledgeFile;
import com.zbkj.common.model.coupon.Coupon;
import com.zbkj.common.model.groupbuy.GroupBuyActivity;
import com.zbkj.common.model.product.Product;
import com.zbkj.common.model.seckill.SeckillActivity;
import com.zbkj.common.model.seckill.SeckillProduct;
import com.zbkj.service.dao.CouponDao;
import com.zbkj.service.dao.ProductDao;
import com.zbkj.service.dao.SeckillActivityDao;
import com.zbkj.service.dao.SeckillProductDao;
import com.zbkj.service.dao.groupby.GroupBuyActivityDao;
import com.zbkj.service.service.CozeKnowledgeFileService;
import com.zbkj.service.service.KnowledgeMarkdownService;
import com.zbkj.service.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 知识库Markdown文件生成和上传服务实现类
 * 
 * @author Auto Generator
 * @since 2024-01-01
 */
@Slf4j
@Service
public class KnowledgeMarkdownServiceImpl implements KnowledgeMarkdownService {

    @Autowired
    private CozeKnowledgeFileService cozeKnowledgeFileService;

    @Autowired
    private CouponDao couponDao;

    @Autowired
    private GroupBuyActivityDao groupBuyActivityDao;

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Autowired
    private SeckillProductDao seckillProductDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ProductService productService;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public CozeKnowledgeFile generateAndUploadCouponMarkdown(Integer couponId, String knowledgeId, Integer merchantId) {
        try {
            // 查询优惠券信息
            Coupon coupon = couponDao.selectById(couponId);
            if (coupon == null) {
                log.error("优惠券不存在，ID: {}", couponId);
                return null;
            }

            // 生成MD文件内容
            String markdownContent = generateCouponMarkdown(coupon);
            String fileName = String.format("优惠券_%s_%d.md", coupon.getName(), System.currentTimeMillis());

            // 上传到知识库
            return uploadMarkdownToKnowledge(markdownContent, fileName, knowledgeId, merchantId);

        } catch (Exception e) {
            log.error("生成优惠券MD文件失败，couponId: {}", couponId, e);
            return null;
        }
    }

    @Override
    public CozeKnowledgeFile generateAndUploadGroupBuyMarkdown(Integer activityId, String knowledgeId, Integer merchantId) {
        try {
            // 查询拼团活动信息
            GroupBuyActivity activity = groupBuyActivityDao.selectById(activityId);
            if (activity == null) {
                log.error("拼团活动不存在，ID: {}", activityId);
                return null;
            }

            // 生成MD文件内容
            String markdownContent = generateGroupBuyActivityMarkdown(activity);
            String fileName = String.format("拼团活动_%s_%d.md", activity.getGroupName(), System.currentTimeMillis());

            // 上传到知识库
            return uploadMarkdownToKnowledge(markdownContent, fileName, knowledgeId, merchantId);

        } catch (Exception e) {
            log.error("生成拼团活动MD文件失败，activityId: {}", activityId, e);
            return null;
        }
    }

    @Override
    public CozeKnowledgeFile generateAndUploadSeckillMarkdown(Integer activityId, String knowledgeId, Integer merchantId) {
        try {
            // 查询秒杀活动信息
            SeckillActivity activity = seckillActivityDao.selectById(activityId);
            if (activity == null) {
                log.error("秒杀活动不存在，ID: {}", activityId);
                return null;
            }

            // 查询秒杀活动关联的商品
            LambdaQueryWrapper<SeckillProduct> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SeckillProduct::getActivityId, activityId);
            wrapper.eq(SeckillProduct::getIsDel, false);
            List<SeckillProduct> seckillProducts = seckillProductDao.selectList(wrapper);

            // 生成MD文件内容
            String markdownContent = generateSeckillActivityMarkdown(activity, seckillProducts);
            String fileName = String.format("秒杀活动_%s_%d.md", activity.getName(), System.currentTimeMillis());

            // 上传到知识库
            return uploadMarkdownToKnowledge(markdownContent, fileName, knowledgeId, merchantId);

        } catch (Exception e) {
            log.error("生成秒杀活动MD文件失败，activityId: {}", activityId, e);
            return null;
        }
    }

    @Override
    public CozeKnowledgeFile generateAndUploadIntegralProductMarkdown(Integer productId, String knowledgeId, Integer merchantId) {
        try {
            // 查询积分商品信息
            Product product = productDao.selectById(productId);
            if (product == null) {
                log.error("积分商品不存在，ID: {}", productId);
                return null;
            }

            // 生成MD文件内容
            String markdownContent = generateIntegralProductMarkdown(product);
            String fileName = String.format("积分商品_%s_%d.md", product.getName(), System.currentTimeMillis());

            // 上传到知识库
            return uploadMarkdownToKnowledge(markdownContent, fileName, knowledgeId, merchantId);

        } catch (Exception e) {
            log.error("生成积分商品MD文件失败，productId: {}", productId, e);
            return null;
        }
    }

    @Override
    public CozeKnowledgeFile uploadMarkdownToKnowledge(String content, String fileName, String knowledgeId, Integer merchantId) {
        try {
            // 创建临时文件
            File tempFile = createTempMarkdownFile(content, fileName);

            // 上传到知识库
            CozeKnowledgeFile result = cozeKnowledgeFileService.uploadLocalFileToKnowledge(knowledgeId, tempFile, merchantId);

            // 删除临时文件
            if (tempFile.exists()) {
                tempFile.delete();
            }

            return result;

        } catch (Exception e) {
            log.error("上传MD文件到知识库失败，fileName: {}", fileName, e);
            return null;
        }
    }

    /**
     * 生成优惠券Markdown内容
     */
    private String generateCouponMarkdown(Coupon coupon) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("# 优惠券信息\n\n");
        sb.append("## 基本信息\n\n");
        sb.append("- **优惠券名称**: ").append(coupon.getName()).append("\n");
        sb.append("- **优惠券ID**: ").append(coupon.getId()).append("\n");
        sb.append("- **商户ID**: ").append(coupon.getMerId()).append("\n");
        
        // 优惠券类型
        String categoryStr = getCouponCategoryString(coupon.getCategory());
        sb.append("- **优惠券类别**: ").append(categoryStr).append("\n");
        
        String receiveTypeStr = getCouponReceiveTypeString(coupon.getReceiveType());
        sb.append("- **领取类型**: ").append(receiveTypeStr).append("\n");
        
        String couponTypeStr = getCouponTypeString(coupon.getCouponType());
        sb.append("- **优惠券类型**: ").append(couponTypeStr).append("\n");
        
        // 优惠信息
        sb.append("\n## 优惠详情\n\n");
        if (coupon.getCouponType() == 1) {
            // 满减券
//            BigDecimal money = new BigDecimal(coupon.getMoney()).divide(new BigDecimal(100));
            sb.append("- **优惠金额**: ").append(coupon.getMoney()).append("元\n");
        } else if (coupon.getCouponType() == 2) {
            // 折扣券
          //  BigDecimal discount = new BigDecimal(coupon.getDiscount()).divide(new BigDecimal(10));
            sb.append("- **折扣**: ").append(coupon.getDiscount()).append("折\n");
        }
        
        if (coupon.getMinPrice() > 0) {
           // BigDecimal minPrice = new BigDecimal(coupon.getMinPrice()).divide(new BigDecimal(100));
            sb.append("- **最低消费**: ").append(coupon.getMinPrice()).append("元\n");
        } else {
            sb.append("- **最低消费**: 无限制\n");
        }
        
        // 数量限制
        sb.append("\n## 数量限制\n\n");
        if (coupon.getIsLimited()) {
            sb.append("- **发放总数**: ").append(coupon.getTotal()).append("张\n");
            sb.append("- **剩余数量**: ").append(coupon.getLastTotal()).append("张\n");
        } else {
            sb.append("- **数量限制**: 不限量\n");
        }
        
        // 时间限制
        sb.append("\n## 时间限制\n\n");
        if (coupon.getIsTimeReceive()) {
            sb.append("- **可领取时间**: ").append(dateFormat.format(coupon.getReceiveStartTime()))
              .append(" 至 ").append(dateFormat.format(coupon.getReceiveEndTime())).append("\n");
        } else {
            sb.append("- **领取时间**: 不限制\n");
        }
        
        if (coupon.getIsFixedTime()) {
            sb.append("- **可使用时间**: ").append(dateFormat.format(coupon.getUseStartTime()))
              .append(" 至 ").append(dateFormat.format(coupon.getUseEndTime())).append("\n");
        } else {
            sb.append("- **使用期限**: 领取后").append(coupon.getDay()).append("天内有效\n");
        }
        
        // 状态信息
        sb.append("\n## 状态信息\n\n");
        sb.append("- **当前状态**: ").append(coupon.getStatus() ? "开启" : "关闭").append("\n");
        sb.append("- **是否可重复领取**: ").append(coupon.getIsRepeated() ? "是" : "否").append("\n");
        sb.append("- **创建时间**: ").append(dateFormat.format(coupon.getCreateTime())).append("\n");
        sb.append("- **更新时间**: ").append(dateFormat.format(coupon.getUpdateTime())).append("\n");
        
        return sb.toString();
    }

    /**
     * 生成拼团活动Markdown内容
     */
    private String generateGroupBuyActivityMarkdown(GroupBuyActivity activity) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("# 拼团活动信息\n\n");
        sb.append("## 基本信息\n\n");
        sb.append("- **活动名称**: ").append(activity.getGroupName()).append("\n");
        sb.append("- **活动ID**: ").append(activity.getId()).append("\n");
        sb.append("- **商户ID**: ").append(activity.getMerId()).append("\n");
        sb.append("- **商户名称**: ").append(activity.getMerName()).append("\n");
        
        // 活动时间
        sb.append("\n## 活动时间\n\n");
        sb.append("- **开始时间**: ").append(dateFormat.format(activity.getStartTime())).append("\n");
        sb.append("- **结束时间**: ").append(dateFormat.format(activity.getEndTime())).append("\n");
        sb.append("- **成团有效期**: ").append(activity.getValidHour()).append("小时\n");
        
        // 拼团规则
        sb.append("\n## 拼团规则\n\n");
        sb.append("- **成团总人数**: ").append(activity.getBuyCount()).append("人\n");
        sb.append("- **购买上限**: ").append(activity.getAllQuota()).append("件\n");
        sb.append("- **单次购买数量**: ").append(activity.getOncQuota()).append("件\n");
        sb.append("- **参与商品数量**: ").append(activity.getProductCount()).append("个\n");
        
        // 活动设置
        sb.append("\n## 活动设置\n\n");
        sb.append("- **凑团显示**: ").append(activity.getShowGroup() == 1 ? "可见" : "不可见").append("\n");
        sb.append("- **虚拟成团**: ").append(activity.getFictiStatus() == 1 ? "开启" : "关闭").append("\n");
        
        // 状态信息
        sb.append("\n## 状态信息\n\n");
        String statusStr = getGroupBuyStatusString(activity.getGroupStatus());
        sb.append("- **控制状态**: ").append(statusStr).append("\n");
        sb.append("- **活动状态**: ").append(activity.getActivityStatus() == 1 ? "开启" : "关闭").append("\n");
        
        // 统计数据
        sb.append("\n## 统计数据\n\n");
        sb.append("- **开团数**: ").append(activity.getTotalActivityBegin()).append("\n");
        sb.append("- **成团数**: ").append(activity.getTotalActivityDone()).append("\n");
        sb.append("- **参团订单数**: ").append(activity.getTotalOrderBegin()).append("\n");
        sb.append("- **成团订单数**: ").append(activity.getTotalOrderDone()).append("\n");
        
        if (StringUtils.hasText(activity.getRefusal())) {
            sb.append("- **审核拒绝理由**: ").append(activity.getRefusal()).append("\n");
        }
        
        sb.append("- **创建时间**: ").append(dateFormat.format(activity.getCreateTime())).append("\n");
        sb.append("- **更新时间**: ").append(dateFormat.format(activity.getUpdateTime())).append("\n");
        
        return sb.toString();
    }

    /**
     * 生成秒杀活动Markdown内容
     */
    private String generateSeckillActivityMarkdown(SeckillActivity activity, List<SeckillProduct> seckillProducts) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("# 秒杀活动信息\n\n");
        sb.append("## 基本信息\n\n");
        sb.append("- **活动名称**: ").append(activity.getName()).append("\n");
        sb.append("- **活动ID**: ").append(activity.getId()).append("\n");
        
        // 活动时间
        sb.append("\n## 活动时间\n\n");
        sb.append("- **开始日期**: ").append(activity.getStartDate()).append("\n");
        sb.append("- **结束日期**: ").append(activity.getEndDate()).append("\n");
        
        // 购买限制
        sb.append("\n## 购买限制\n\n");
        if (activity.getOneQuota() > 0) {
            sb.append("- **单笔下单限制**: ").append(activity.getOneQuota()).append("件\n");
        } else {
            sb.append("- **单笔下单限制**: 不限制\n");
        }
        
        if (activity.getAllQuota() > 0) {
            sb.append("- **总购买限制**: ").append(activity.getAllQuota()).append("件\n");
        } else {
            sb.append("- **总购买限制**: 不限制\n");
        }
        
        // 参与条件
        sb.append("\n## 参与条件\n\n");
        sb.append("- **商家星级要求**: ").append(activity.getMerStars()).append("星及以上\n");
        if (StringUtils.hasText(activity.getProCategory())) {
            sb.append("- **商品类型**: ").append(activity.getProCategory()).append("\n");
        }
        
        // 状态信息
        sb.append("\n## 状态信息\n\n");
        sb.append("- **开启状态**: ").append(activity.getIsOpen() == 1 ? "开启" : "关闭").append("\n");
        String statusStr = getSeckillStatusString(activity.getStatus());
        sb.append("- **活动状态**: ").append(statusStr).append("\n");
        sb.append("- **创建时间**: ").append(dateFormat.format(activity.getCreateTime())).append("\n");
        sb.append("- **更新时间**: ").append(dateFormat.format(activity.getUpdateTime())).append("\n");
        
        // 参与商品
        if (seckillProducts != null && !seckillProducts.isEmpty()) {
            sb.append("\n## 参与商品\n\n");
            for (int i = 0; i < seckillProducts.size(); i++) {
                SeckillProduct seckillProduct = seckillProducts.get(i);
                Product product = productDao.selectById(seckillProduct.getProductId());
                if (product != null) {
                    sb.append("### ").append(i + 1).append(". ").append(product.getName()).append("\n\n");
                    sb.append("- **商品ID**: ").append(product.getId()).append("\n");
                    sb.append("- **秒杀价格**: ").append(seckillProduct.getPrice()).append("元\n");
                    sb.append("- **秒杀库存**: ").append(seckillProduct.getQuota()).append("\n");
                    sb.append("- **销量**: ").append(seckillProduct.getSales()).append("\n\n");
                }
            }
        }
        
        return sb.toString();
    }

    /**
     * 生成积分商品Markdown内容
     */
    private String generateIntegralProductMarkdown(Product product) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("# 积分商品信息\n\n");
        sb.append("## 基本信息\n\n");
        sb.append("- **商品名称**: ").append(product.getName()).append("\n");
        sb.append("- **商品ID**: ").append(product.getId()).append("\n");
        sb.append("- **商户ID**: ").append(product.getMerId()).append("\n");
        
        if (StringUtils.hasText(product.getIntro())) {
            sb.append("- **商品简介**: ").append(product.getIntro()).append("\n");
        }
        
        if (StringUtils.hasText(product.getKeyword())) {
            sb.append("- **关键词**: ").append(product.getKeyword()).append("\n");
        }
        
        // 价格信息
        sb.append("\n## 价格信息\n\n");
        if (product.getRedeemIntegral() != null && product.getRedeemIntegral() > 0) {
            sb.append("- **积分价格**: ").append(product.getRedeemIntegral()).append("积分\n");
        }
        
        if (product.getPrice() != null) {
            sb.append("- **现价**: ").append(product.getPrice()).append("元\n");
        }
        
        if (product.getOtPrice() != null) {
            sb.append("- **原价**: ").append(product.getOtPrice()).append("元\n");
        }
        
        // 库存信息
        sb.append("\n## 库存信息\n\n");
        sb.append("- **库存**: ").append(product.getStock()).append("\n");
        sb.append("- **销量**: ").append(product.getSales()).append("\n");
        sb.append("- **收藏**: ").append(product.getFicti()).append("\n");
        sb.append("- **浏览量**: ").append(product.getBrowse()).append("\n");
        
        // 商品设置
        sb.append("\n## 商品设置\n\n");
        sb.append("- **是否热门推荐**: ").append(product.getIsHot() != null && product.getIsHot() == 1 ? "是" : "否").append("\n");
        sb.append("- **是否单独分佣**: ").append(product.getIsSub() != null && product.getIsSub() ? "是" : "否").append("\n");
        sb.append("- **是否付费会员商品**: ").append(product.getIsPaidMember() != null && product.getIsPaidMember() ? "是" : "否").append("\n");
        sb.append("- **是否自动上架**: ").append(product.getIsAutoUp() != null && product.getIsAutoUp() ? "是" : "否").append("\n");
        
        // 状态信息
        sb.append("\n## 状态信息\n\n");
        sb.append("- **商品状态**: ").append(product.getIsShow() != null && product.getIsShow() ? "上架" : "下架").append("\n");
        sb.append("- **审核状态**: ").append(getProductAuditStatusString(product.getAuditStatus())).append("\n");
        sb.append("- **排序**: ").append(product.getSort() != null ? product.getSort() : 0).append("\n");
        sb.append("- **创建时间**: ").append(dateFormat.format(product.getCreateTime())).append("\n");
        sb.append("- **更新时间**: ").append(dateFormat.format(product.getUpdateTime())).append("\n");
        
        return sb.toString();
    }

    /**
     * 创建临时Markdown文件
     */
    private File createTempMarkdownFile(String content, String fileName) throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "coze-markdown");
        if (!tempDir.exists()) {
            boolean created = tempDir.mkdirs();
            if (!created) {
                log.warn("Failed to create temp directory: {}", tempDir.getPath());
            }
        }
        
        File tempFile = new File(tempDir, fileName);
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
        
        return tempFile;
    }

    // 辅助方法：获取优惠券类别字符串
    private String getCouponCategoryString(Integer category) {
        if (category == null) return "未知";
        switch (category) {
            case 1: return "商家券";
            case 2: return "商品券";
            case 3: return "通用券";
            case 4: return "品类券";
            case 5: return "品牌券";
            case 6: return "跨店券";
            default: return "未知";
        }
    }

    // 辅助方法：获取优惠券领取类型字符串
    private String getCouponReceiveTypeString(Integer receiveType) {
        if (receiveType == null) return "未知";
        switch (receiveType) {
            case 1: return "手动领取";
            case 2: return "商品赠送券";
            case 3: return "平台活动发放";
            default: return "未知";
        }
    }

    // 辅助方法：获取优惠券类型字符串
    private String getCouponTypeString(Integer couponType) {
        if (couponType == null) return "未知";
        switch (couponType) {
            case 1: return "满减券";
            case 2: return "折扣券";
            default: return "未知";
        }
    }

    // 辅助方法：获取拼团状态字符串
    private String getGroupBuyStatusString(Integer groupStatus) {
        if (groupStatus == null) return "未知";
        switch (groupStatus) {
            case 0: return "初始化";
            case 1: return "已拒绝";
            case 2: return "已撤销";
            case 3: return "待审核";
            case 4: return "已通过";
            default: return "未知";
        }
    }

    // 辅助方法：获取秒杀状态字符串
    private String getSeckillStatusString(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "未开始";
            case 1: return "进行中";
            case 2: return "已结束";
            default: return "未知";
        }
    }

    // 辅助方法：获取商品审核状态字符串
    private String getProductAuditStatusString(Integer auditStatus) {
        if (auditStatus == null) return "未知";
        switch (auditStatus) {
            case 0: return "待审核";
            case 1: return "审核通过";
            case 2: return "审核拒绝";
            default: return "未知";
        }
    }
}