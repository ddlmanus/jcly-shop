package com.zbkj.admin.task.order;

import cn.hutool.core.date.DateUtil;
import com.zbkj.common.model.merchant.MerchantProfitSharingDetail;
import com.zbkj.service.service.MerchantProfitSharingDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 订单分账定时任务
 * </p>
 *
 * @author dudl
 * @since 2025-01-07
 */
@Component("OrderProfitSharingTask")
public class OrderProfitSharingTask {

    private static final Logger logger = LoggerFactory.getLogger(OrderProfitSharingTask.class);

    @Autowired
    private MerchantProfitSharingDetailService profitSharingDetailService;

    /**
     * 执行分账任务
     * 每小时执行一次，处理到期的分账记录
     */
    public void executeProfitSharing() {
        try {
            logger.info("---OrderProfitSharingTask task------produce Data with fixed rate task: Execution Time - {}", DateUtil.date());

            // 查询需要执行分账的记录
            List<MerchantProfitSharingDetail> pendingList = profitSharingDetailService.getPendingSharingList();
            logger.info("OrderProfitSharingTask.executeProfitSharing | 待分账订单数量: {}", pendingList.size());

            if (pendingList.isEmpty()) {
                return;
            }

            int successCount = 0;
            int failedCount = 0;

            for (MerchantProfitSharingDetail detail : pendingList) {
                try {
                    boolean success = profitSharingDetailService.executeSharing(detail.getId());
                    if (success) {
                        successCount++;
                        logger.info("分账执行成功: 订单号={}, 分账单号={}", detail.getOrderNo(), detail.getSharingNo());
                    } else {
                        failedCount++;
                        logger.warn("分账执行失败: 订单号={}, 分账单号={}", detail.getOrderNo(), detail.getSharingNo());
                    }
                } catch (Exception e) {
                    failedCount++;
                    logger.error("分账执行异常: 订单号=" + detail.getOrderNo() + ", 分账单号=" + detail.getSharingNo(), e);
                }
            }

            logger.info("OrderProfitSharingTask.executeProfitSharing 执行完成 | 成功: {}, 失败: {}", successCount, failedCount);
        } catch (Exception e) {
            logger.error("OrderProfitSharingTask.executeProfitSharing 执行异常", e);
        }
    }

    /**
     * 重试失败的分账任务
     * 每30分钟执行一次，处理需要重试的分账记录
     */
    public void retryFailedProfitSharing() {
        try {
            logger.info("---OrderProfitSharingRetryTask task------produce Data with fixed rate task: Execution Time - {}", DateUtil.date());

            // 查询需要重试的分账记录
            List<MerchantProfitSharingDetail> retryList = profitSharingDetailService.getRetryList();
            logger.info("OrderProfitSharingRetryTask.retryFailedProfitSharing | 需重试分账数量: {}", retryList.size());

            if (retryList.isEmpty()) {
                return;
            }

            int successCount = 0;
            int failedCount = 0;

            for (MerchantProfitSharingDetail detail : retryList) {
                try {
                    boolean success = profitSharingDetailService.executeSharing(detail.getId());
                    if (success) {
                        successCount++;
                        logger.info("分账重试成功: 订单号={}, 分账单号={}, 重试次数={}", 
                            detail.getOrderNo(), detail.getSharingNo(), detail.getRetryCount());
                    } else {
                        failedCount++;
                        logger.warn("分账重试失败: 订单号={}, 分账单号={}, 重试次数={}", 
                            detail.getOrderNo(), detail.getSharingNo(), detail.getRetryCount());
                    }
                } catch (Exception e) {
                    failedCount++;
                    logger.error("分账重试异常: 订单号=" + detail.getOrderNo() + 
                        ", 分账单号=" + detail.getSharingNo() + ", 重试次数=" + detail.getRetryCount(), e);
                }
            }

            logger.info("OrderProfitSharingRetryTask.retryFailedProfitSharing 执行完成 | 成功: {}, 失败: {}", successCount, failedCount);
        } catch (Exception e) {
            logger.error("OrderProfitSharingRetryTask.retryFailedProfitSharing 执行异常", e);
        }
    }
} 