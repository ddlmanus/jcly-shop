package com.zbkj.service.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zbkj.common.model.express.Express;
import com.zbkj.common.model.order.Order;
import com.zbkj.common.model.product.Product;
import com.zbkj.common.request.OrderSendRequest;
import com.zbkj.common.response.JustuitanOrderReponse;
import com.zbkj.common.response.JustuitanOrderUploadResult;
import com.zbkj.common.response.JustuitanProductUploadResult;
import com.zbkj.service.service.ExpressService;
import com.zbkj.service.service.JustuitanErpService;
import com.zbkj.service.service.OrderService;
import com.zbkj.service.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 聚水潭订单同步定时任务
 * +----------------------------------------------------------------------
 * | JCLY [ JCLY赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2024 https://www.ddlmanus.xyz All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 * +----------------------------------------------------------------------
 * | Author: dudl
 * +----------------------------------------------------------------------
 */
@Component("JustuitanOrderSyncTask")
public class JustuitanOrderSyncTask {

    private static final Logger logger = LoggerFactory.getLogger(JustuitanOrderSyncTask.class);

    @Autowired
    private JustuitanErpService justuitanErpService;
    @Autowired
    private ExpressService expressService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductService productService;
    
    // 可以通过配置文件控制是否启用定时任务
    // 在 application.yml 中添加：jushuitan.sync.order-upload-enabled: true/false
    // @Value("${jushuitan.sync.order-upload-enabled:false}")
    // private Boolean orderUploadEnabled;

    /**
     * 定时同步聚水潭订单状态
     * 每10分钟执行一次 corn
     */
 //   @Scheduled(fixedRate = 600000) // 10分钟 = 600000毫秒
    public void syncOrderStatusFromJst() {
        try {
            logger.info("开始执行聚水潭订单状态同步定时任务");
            
            Boolean result = justuitanErpService.syncOrderStatusFromJst();
            
            if (result) {
                logger.info("聚水潭订单状态同步定时任务执行成功");
            } else {
                logger.warn("聚水潭订单状态同步定时任务执行失败");
            }
            
        } catch (Exception e) {
            logger.error("聚水潭订单状态同步定时任务执行异常", e);
        }
    }

    /**
     * 定时查询聚水潭新订单
     * 每5分钟执行一次 corn表达式
     *
     */
  //  @Scheduled(fixedRate = 300000) // 5分钟 = 300000毫秒
    public void queryNewOrdersFromJst() {
        try {
            logger.info("开始执行聚水潭新订单查询定时任务");
            
            // 查询最近1小时的订单变更
            String modifiedEnd = cn.hutool.core.date.DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss");
            String modifiedBegin = cn.hutool.core.date.DateUtil.format(
                cn.hutool.core.date.DateUtil.offsetHour(new java.util.Date(), -1), 
                "yyyy-MM-dd HH:mm:ss"
            );
            
            // 分页查询订单
            int pageIndex = 1;
            int pageSize = 50;
            boolean hasMoreData = true;
            int totalProcessed = 0;
            
            while (hasMoreData) {
                List<JustuitanOrderReponse> orders = justuitanErpService.queryOrdersFromJst(modifiedBegin, modifiedEnd, pageIndex, pageSize);
                
                if (orders == null || orders.isEmpty()) {
                    hasMoreData = false;
                } else {
                    totalProcessed += orders.size();
                    logger.info("第{}页查询到{}个订单", pageIndex, orders.size());
                    
                    // 处理每个订单的状态同步
                    for (JustuitanOrderReponse order : orders) {
                        try {
                            // 调用聚水潭ERP服务的订单状态同步方法
                            justuitanErpService.syncOrderStatus(order);
                            logger.debug("订单状态同步成功: {}", order.getOrderNo());
                        } catch (Exception e) {
                            logger.error("订单状态同步失败: {}", order.getOrderNo(), e);
                        }
                    }
                    
                    pageIndex++;
                    
                    // 防止无限循环，最多处理10页
                    if (pageIndex > 10) {
                        hasMoreData = false;
                        logger.warn("聚水潭订单查询达到最大页数限制，停止查询");
                    }
                }
            }
            
            logger.info("聚水潭新订单查询定时任务执行完成，共处理{}个订单", totalProcessed);
            
        } catch (Exception e) {
            logger.error("聚水潭新订单查询定时任务执行异常", e);
        }
    }

    /**
     * 定时上传本地订单到聚水潭
     * 上传 jst_order_id 为空的已支付订单
     * 每15分钟执行一次
     * 
     * ⚠️ 首次使用建议先手动测试，确认无误后再启用定时任务
     * 启用方法：取消注释下面的 @Scheduled 注解
     */
    // @Scheduled(fixedRate = 900000) // 15分钟 = 900000毫秒
    public void uploadLocalOrdersToJst() {
        try {
            logger.info("==================== 开始执行本地订单上传到聚水潭定时任务 ====================");
            
            // 查询 jst_order_id 为空的已支付订单
            LambdaQueryWrapper<Order> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.isNull(Order::getJstOrderId);  // jst_order_id 为空
            queryWrapper.eq(Order::getPaid, true);       // 已支付
            queryWrapper.eq(Order::getIsDel, false);     // 未删除
            queryWrapper.in(Order::getStatus, 1, 2, 3, 4, 5); // 订单状态：待发货、部分发货、待核销、待收货、已完成
            queryWrapper.orderByAsc(Order::getCreateTime); // 按创建时间升序，优先处理旧订单
            queryWrapper.last("LIMIT 100"); // 每次最多处理100个订单，避免一次性处理太多
            
            List<Order> pendingOrders = orderService.selectList(queryWrapper);
            
            if (CollUtil.isEmpty(pendingOrders)) {
                logger.info("没有需要上传的订单（jst_order_id 为空的已支付订单）");
                return;
            }
            
            logger.info("查询到 {} 个待上传订单", pendingOrders.size());
            
            int successCount = 0;
            int failCount = 0;
            int skipCount = 0;
            
            // 逐个上传订单
            for (Order order : pendingOrders) {
                try {
                    // 检查订单是否属于自营店（如果需要）
                    if (!justuitanErpService.isSelfOperatedStore(order.getMerId())) {
                        logger.debug("订单不属于自营店，跳过上传: orderNo={}, merId={}", order.getOrderNo(), order.getMerId());
                        skipCount++;
                        continue;
                    }
                    
                    logger.info("开始上传订单: orderNo={}, uid={}, payPrice={}, status={}", 
                        order.getOrderNo(), order.getUid(), order.getPayPrice(), order.getStatus());
                    
                    // 调用聚水潭订单上传接口
                    JustuitanOrderUploadResult result = justuitanErpService.uploadOrderToJst(order);
                    
                    if (result != null && result.isSuccess()) {
                        successCount++;
                        logger.info("订单上传成功: orderNo={}, jstOrderId={}", 
                            order.getOrderNo(), result.getJstOrderId());
                    } else {
                        failCount++;
                        String errorMsg = result != null ? result.getMessage() : "未知错误";
                        logger.error("订单上传失败: orderNo={}, 错误信息: {}", 
                            order.getOrderNo(), errorMsg);
                    }
                    
                    // 添加延时，避免频繁调用API
                    Thread.sleep(500); // 每个订单之间延迟500毫秒
                    
                } catch (Exception e) {
                    failCount++;
                    logger.error("订单上传异常: orderNo={}", order.getOrderNo(), e);
                }
            }
            
            logger.info("==================== 本地订单上传到聚水潭定时任务执行完成 ====================");
            logger.info("总计处理: {} 个订单, 成功: {}, 失败: {}, 跳过: {}", 
                pendingOrders.size(), successCount, failCount, skipCount);
            
        } catch (Exception e) {
            logger.error("本地订单上传到聚水潭定时任务执行异常", e);
        }
    }

    /**
     * 定时上传本地商品到聚水潭
     * 上传 jst_item_id 为空的商品（自营店商品）
     * 每30分钟执行一次
     * 
     * ⚠️ 首次使用建议先手动测试，确认无误后再启用定时任务
     * 启用方法：取消注释下面的 @Scheduled 注解
     */
    // @Scheduled(fixedRate = 1800000) // 30分钟 = 1800000毫秒
    public void uploadLocalProductsToJst() {
        try {
            logger.info("==================== 开始执行本地商品上传到聚水潭定时任务 ====================");
            
            // 查询 jst_item_id 为空的商品（只上传自营店商品）
            LambdaQueryWrapper<Product> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.isNull(Product::getJstItemId);   // jst_item_id 为空
            queryWrapper.eq(Product::getIsDel, false);    // 未删除
            queryWrapper.eq(Product::getIsShow, true);    // 已上架
            queryWrapper.eq(Product::getAuditStatus, 0);  // 审核通过
            queryWrapper.orderByAsc(Product::getCreateTime); // 按创建时间升序，优先处理旧商品
          //  queryWrapper.last("LIMIT 50"); // 每次最多处理50个商品，避免一次性处理太多
            
            List<Product> pendingProducts = productService.list(queryWrapper);
            
            if (CollUtil.isEmpty(pendingProducts)) {
                logger.info("没有需要上传的商品（jst_item_id 为空的已上架商品）");
                return;
            }
            
            logger.info("查询到 {} 个待上传商品", pendingProducts.size());
            
            int successCount = 0;
            int failCount = 0;
            int skipCount = 0;
            
            // 逐个上传商品
            for (Product product : pendingProducts) {
                try {
                    // 检查商品是否属于自营店
                    if (!justuitanErpService.isSelfOperatedStore(product.getMerId())) {
                        logger.debug("商品不属于自营店，跳过上传: productId={}, merId={}, productName={}", 
                            product.getId(), product.getMerId(), product.getName());
                        skipCount++;
                        continue;
                    }
                    
                    logger.info("开始上传商品: productId={}, productName={}, price={}, stock={}", 
                        product.getId(), product.getName(), product.getPrice(), product.getStock());
                    
                    // 调用聚水潭商品上传接口
                    JustuitanProductUploadResult result = justuitanErpService.uploadProductToJst(product);
                    
                    if (result != null && result.isSuccess()) {
                        successCount++;
                        logger.info("商品上传成功: productId={}, productName={}, 上传SKU数量={}", 
                            product.getId(), product.getName(), 
                            result.getSkuResults() != null ? result.getSkuResults().size() : 0);
                    } else {
                        failCount++;
                        String errorMsg = result != null ? result.getMessage() : "未知错误";
                        logger.error("商品上传失败: productId={}, productName={}, 错误信息: {}", 
                            product.getId(), product.getName(), errorMsg);
                    }
                    
                    // 添加延时，避免频繁调用API
                    Thread.sleep(800); // 每个商品之间延迟800毫秒（商品上传比订单复杂，需要更长时间）
                    
                } catch (Exception e) {
                    failCount++;
                    logger.error("商品上传异常: productId={}, productName={}", 
                        product.getId(), product.getName(), e);
                }
            }
            
            logger.info("==================== 本地商品上传到聚水潭定时任务执行完成 ====================");
            logger.info("总计处理: {} 个商品, 成功: {}, 失败: {}, 跳过: {}", 
                pendingProducts.size(), successCount, failCount, skipCount);
            
        } catch (Exception e) {
            logger.error("本地商品上传到聚水潭定时任务执行异常", e);
        }
    }
}