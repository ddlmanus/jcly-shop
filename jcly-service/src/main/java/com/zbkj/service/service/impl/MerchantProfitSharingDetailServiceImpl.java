package com.zbkj.service.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.merchant.MerchantProfitSharingConfig;
import com.zbkj.common.model.merchant.MerchantProfitSharingDetail;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.service.dao.MerchantProfitSharingDetailDao;
import com.zbkj.service.service.MerchantProfitSharingConfigService;
import com.zbkj.service.service.MerchantProfitSharingDetailService;
import com.zbkj.service.service.WechatProfitSharingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * <p>
 * 商户分账明细记录表 服务实现类
 * </p>
 *
 * @author dudl
 * @since 2025-01-07
 */
@Service
public class MerchantProfitSharingDetailServiceImpl extends ServiceImpl<MerchantProfitSharingDetailDao, MerchantProfitSharingDetail> implements MerchantProfitSharingDetailService {

    private static final Logger logger = LoggerFactory.getLogger(MerchantProfitSharingDetailServiceImpl.class);

    @Autowired
    private MerchantProfitSharingConfigService configService;

    @Autowired
    private WechatProfitSharingService wechatProfitSharingService;

    /**
     * 分页查询分账明细记录
     *
     * @param merId            商户ID
     * @param pageParamRequest 分页参数
     * @return 分页结果
     */
    @Override
    public PageInfo<MerchantProfitSharingDetail> getPage(Integer merId, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        
        LambdaQueryWrapper<MerchantProfitSharingDetail> lqw = new LambdaQueryWrapper<>();
        if (ObjectUtil.isNotNull(merId)) {
            lqw.eq(MerchantProfitSharingDetail::getMerId, merId);
        }
        lqw.orderByDesc(MerchantProfitSharingDetail::getCreateTime);
        
        return PageInfo.of(list(lqw));
    }

    /**
     * 创建分账记录
     *
     * @param orderNo       订单号
     * @param transactionId 微信支付订单号
     * @param totalAmount   订单总金额
     * @param merId         商户ID
     * @return 是否成功
     */
    @Override
    public Boolean createSharingRecord(String orderNo, String transactionId, BigDecimal totalAmount, Integer merId) {
        // 检查商户是否开启分账
        if (!configService.isProfitSharingEnabled(merId)) {
            logger.info("商户{}未开启分账功能，跳过分账记录创建", merId);
            return true;
        }

        // 检查该订单是否已创建分账记录
        MerchantProfitSharingDetail existRecord = getByOrderNo(orderNo);
        if (ObjectUtil.isNotNull(existRecord)) {
            logger.warn("订单{}已存在分账记录，跳过创建", orderNo);
            return true;
        }

        // 获取分账配置
        MerchantProfitSharingConfig config = configService.getByMerId(merId);
        if (ObjectUtil.isNull(config)) {
            throw new CrmebException("商户分账配置不存在");
        }

        // 计算分账金额（转换为分）
        BigDecimal sharingAmount = totalAmount.multiply(config.getSharingRatio())
                .divide(new BigDecimal("100"), 2, RoundingMode.DOWN)
                .multiply(new BigDecimal("100"));

        // 创建分账记录
        MerchantProfitSharingDetail detail = new MerchantProfitSharingDetail();
        detail.setSharingNo(generateSharingNo());
        detail.setMerId(merId);
        detail.setOrderNo(orderNo);
        detail.setTransactionId(transactionId);
        detail.setOutOrderNo(CrmebUtil.getOrderNo("PSO"));
        detail.setSubMchId(config.getSubMchId());
        detail.setSharingAmount(sharingAmount);
        detail.setSharingRatio(config.getSharingRatio());
        detail.setTotalAmount(totalAmount.multiply(new BigDecimal("100"))); // 转换为分
        detail.setSharingType(config.getSharingType());
        detail.setAccount(config.getAccount());
        detail.setName(config.getName());
        detail.setRelationType(config.getRelationType());
        detail.setSharingStatus("PROCESSING");
        detail.setRetryCount(0);
        detail.setMaxRetryCount(3);
        detail.setCreateTime(new Date());
        detail.setUpdateTime(new Date());

        // 计算分账执行时间（订单完成后N天）
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, config.getSharingDelayDays());
        detail.setSharingTime(calendar.getTime());

        return save(detail);
    }

    /**
     * 执行分账
     *
     * @param sharingId 分账记录ID
     * @return 是否成功
     */
    @Override
    public Boolean executeSharing(Integer sharingId) {
        MerchantProfitSharingDetail detail = getById(sharingId);
        if (ObjectUtil.isNull(detail)) {
            logger.error("分账记录不存在: {}", sharingId);
            return false;
        }

        if (!"PROCESSING".equals(detail.getSharingStatus())) {
            logger.warn("分账记录状态不正确: {}, 状态: {}", sharingId, detail.getSharingStatus());
            return false;
        }

        try {
            // 调用微信分账接口
            Map<String, Object> result = wechatProfitSharingService.profitSharing(detail);
            
            if (result != null && "SUCCESS".equals(result.get("return_code"))) {
                // 分账成功
                updateSharingStatus(sharingId, "SUCCESS", result.toString(), null, null);
                detail.setCompleteTime(new Date());
                updateById(detail);
                logger.info("分账执行成功: {}", sharingId);
                return true;
            } else {
                // 分账失败，准备重试
                String errorCode = result != null ? (String) result.get("err_code") : "UNKNOWN";
                String errorMsg = result != null ? (String) result.get("err_code_des") : "未知错误";
                
                if (detail.getRetryCount() < detail.getMaxRetryCount()) {
                    // 增加重试次数，设置下次重试时间
                    detail.setRetryCount(detail.getRetryCount() + 1);
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.HOUR, detail.getRetryCount()); // 递增重试间隔
                    detail.setNextRetryTime(calendar.getTime());
                    detail.setErrorCode(errorCode);
                    detail.setErrorMsg(errorMsg);
                    detail.setWechatResult(result != null ? result.toString() : null);
                    detail.setUpdateTime(new Date());
                    updateById(detail);
                    logger.warn("分账执行失败，等待重试: {}, 错误: {}", sharingId, errorMsg);
                } else {
                    // 超过最大重试次数，标记为失败
                    updateSharingStatus(sharingId, "FAILED", result != null ? result.toString() : null, errorCode, errorMsg);
                    logger.error("分账执行失败，超过最大重试次数: {}, 错误: {}", sharingId, errorMsg);
                }
                return false;
            }
        } catch (Exception e) {
            logger.error("分账执行异常: " + sharingId, e);
            updateSharingStatus(sharingId, "FAILED", null, "SYSTEM_ERROR", e.getMessage());
            return false;
        }
    }

    /**
     * 查询需要执行分账的订单
     *
     * @return 分账记录列表
     */
    @Override
    public List<MerchantProfitSharingDetail> getPendingSharingList() {
        LambdaQueryWrapper<MerchantProfitSharingDetail> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MerchantProfitSharingDetail::getSharingStatus, "PROCESSING");
        lqw.le(MerchantProfitSharingDetail::getSharingTime, new Date());
        lqw.isNull(MerchantProfitSharingDetail::getNextRetryTime);
        return list(lqw);
    }

    /**
     * 查询需要重试的分账记录
     *
     * @return 重试记录列表
     */
    @Override
    public List<MerchantProfitSharingDetail> getRetryList() {
        LambdaQueryWrapper<MerchantProfitSharingDetail> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MerchantProfitSharingDetail::getSharingStatus, "PROCESSING");
        lqw.le(MerchantProfitSharingDetail::getNextRetryTime, new Date());
        lqw.apply("retry_count < max_retry_count");
        return list(lqw);
    }

    /**
     * 更新分账状态
     *
     * @param sharingId     分账记录ID
     * @param sharingStatus 分账状态
     * @param wechatResult  微信返回结果
     * @param errorCode     错误代码
     * @param errorMsg      错误信息
     * @return 是否成功
     */
    @Override
    public Boolean updateSharingStatus(Integer sharingId, String sharingStatus, String wechatResult, String errorCode, String errorMsg) {
        MerchantProfitSharingDetail detail = getById(sharingId);
        if (ObjectUtil.isNull(detail)) {
            return false;
        }

        detail.setSharingStatus(sharingStatus);
        detail.setWechatResult(wechatResult);
        detail.setErrorCode(errorCode);
        detail.setErrorMsg(errorMsg);
        detail.setUpdateTime(new Date());

        if ("SUCCESS".equals(sharingStatus) || "FAILED".equals(sharingStatus)) {
            detail.setCompleteTime(new Date());
        }

        return updateById(detail);
    }

    /**
     * 根据订单号查询分账记录
     *
     * @param orderNo 订单号
     * @return 分账记录
     */
    @Override
    public MerchantProfitSharingDetail getByOrderNo(String orderNo) {
        LambdaQueryWrapper<MerchantProfitSharingDetail> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MerchantProfitSharingDetail::getOrderNo, orderNo);
        return getOne(lqw);
    }

    /**
     * 生成分账单号
     *
     * @return 分账单号
     */
    @Override
    public String generateSharingNo() {
        return CrmebUtil.getOrderNo("PS");
    }

    /**
     * 重试分账
     *
     * @param sharingId 分账记录ID
     * @return 是否成功
     */
    @Override
    public Boolean retrySharing(Integer sharingId) {
        MerchantProfitSharingDetail detail = getById(sharingId);
        if (ObjectUtil.isNull(detail)) {
            return false;
        }

        if (!"PROCESSING".equals(detail.getSharingStatus())) {
            throw new CrmebException("只能重试处理中的分账记录");
        }

        if (detail.getRetryCount() >= detail.getMaxRetryCount()) {
            throw new CrmebException("已达到最大重试次数");
        }

        return executeSharing(sharingId);
    }

    /**
     * 获取分账统计数据
     *
     * @param merId 商户ID
     * @return 统计数据
     */
    @Override
    public Map<String, Object> getProfitSharingStats(Integer merId) {
        Map<String, Object> stats = new HashMap<>();
        
        LambdaQueryWrapper<MerchantProfitSharingDetail> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MerchantProfitSharingDetail::getMerId, merId);
        
        // 总分账记录数
        int totalCount = count(lqw);
        stats.put("totalCount", totalCount);
        
        // 成功分账记录数
        lqw.clear();
        lqw.eq(MerchantProfitSharingDetail::getMerId, merId);
        lqw.eq(MerchantProfitSharingDetail::getSharingStatus, "SUCCESS");
        int successCount = count(lqw);
        stats.put("successCount", successCount);
        
        // 失败分账记录数
        lqw.clear();
        lqw.eq(MerchantProfitSharingDetail::getMerId, merId);
        lqw.eq(MerchantProfitSharingDetail::getSharingStatus, "FAILED");
        int failedCount = count(lqw);
        stats.put("failedCount", failedCount);
        
        // 处理中分账记录数
        lqw.clear();
        lqw.eq(MerchantProfitSharingDetail::getMerId, merId);
        lqw.eq(MerchantProfitSharingDetail::getSharingStatus, "PROCESSING");
        int processingCount = count(lqw);
        stats.put("processingCount", processingCount);
        
        // 总分账金额
        lqw.clear();
        lqw.eq(MerchantProfitSharingDetail::getMerId, merId);
        lqw.eq(MerchantProfitSharingDetail::getSharingStatus, "SUCCESS");
        List<MerchantProfitSharingDetail> successList = list(lqw);
        BigDecimal totalAmount = successList.stream()
                .map(detail -> detail.getSharingAmount() != null ? detail.getSharingAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalSharingAmount", totalAmount.divide(new BigDecimal("100"), 2, RoundingMode.DOWN)); // 转换为元
        
        // 成功率
        if (totalCount > 0) {
            BigDecimal successRate = new BigDecimal(successCount)
                    .divide(new BigDecimal(totalCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            stats.put("successRate", successRate);
        } else {
            stats.put("successRate", BigDecimal.ZERO);
        }
        
        // 今日分账数据
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Date startOfDay = today.getTime();
        
        lqw.clear();
        lqw.eq(MerchantProfitSharingDetail::getMerId, merId);
        lqw.ge(MerchantProfitSharingDetail::getCreateTime, startOfDay);
        int todayCount = count(lqw);
        stats.put("todayCount", todayCount);
        
        lqw.clear();
        lqw.eq(MerchantProfitSharingDetail::getMerId, merId);
        lqw.eq(MerchantProfitSharingDetail::getSharingStatus, "SUCCESS");
        lqw.ge(MerchantProfitSharingDetail::getCompleteTime, startOfDay);
        List<MerchantProfitSharingDetail> todaySuccessList = list(lqw);
        BigDecimal todayAmount = todaySuccessList.stream()
                .map(detail -> detail.getSharingAmount() != null ? detail.getSharingAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("todaySharingAmount", todayAmount.divide(new BigDecimal("100"), 2, RoundingMode.DOWN));
        
        // 本月分账数据
        Calendar thisMonth = Calendar.getInstance();
        thisMonth.set(Calendar.DAY_OF_MONTH, 1);
        thisMonth.set(Calendar.HOUR_OF_DAY, 0);
        thisMonth.set(Calendar.MINUTE, 0);
        thisMonth.set(Calendar.SECOND, 0);
        thisMonth.set(Calendar.MILLISECOND, 0);
        Date startOfMonth = thisMonth.getTime();
        
        lqw.clear();
        lqw.eq(MerchantProfitSharingDetail::getMerId, merId);
        lqw.ge(MerchantProfitSharingDetail::getCreateTime, startOfMonth);
        int monthCount = count(lqw);
        stats.put("monthCount", monthCount);
        
        lqw.clear();
        lqw.eq(MerchantProfitSharingDetail::getMerId, merId);
        lqw.eq(MerchantProfitSharingDetail::getSharingStatus, "SUCCESS");
        lqw.ge(MerchantProfitSharingDetail::getCompleteTime, startOfMonth);
        List<MerchantProfitSharingDetail> monthSuccessList = list(lqw);
        BigDecimal monthAmount = monthSuccessList.stream()
                .map(detail -> detail.getSharingAmount() != null ? detail.getSharingAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("monthSharingAmount", monthAmount.divide(new BigDecimal("100"), 2, RoundingMode.DOWN));
        
        return stats;
    }
} 