package com.zbkj.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.merchant.MerchantProfitSharingApply;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.service.dao.MerchantProfitSharingApplyDao;
import com.zbkj.service.service.MerchantProfitSharingApplyService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 商户分账申请记录表 服务实现类
 * </p>
 *
 * @author dudl
 * @since 2025-01-07
 */
@Service
public class MerchantProfitSharingApplyServiceImpl extends ServiceImpl<MerchantProfitSharingApplyDao, MerchantProfitSharingApply> implements MerchantProfitSharingApplyService {

    /**
     * 分页查询分账申请记录
     *
     * @param merId             商户ID
     * @param pageParamRequest  分页参数
     * @return 分页结果
     */
    @Override
    public PageInfo<MerchantProfitSharingApply> getPage(Integer merId, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        
        LambdaQueryWrapper<MerchantProfitSharingApply> lqw = new LambdaQueryWrapper<>();
        if (ObjectUtil.isNotNull(merId)) {
            lqw.eq(MerchantProfitSharingApply::getMerId, merId);
        }
        lqw.orderByDesc(MerchantProfitSharingApply::getCreateTime);
        
        return PageInfo.of(list(lqw));
    }

    /**
     * 提交分账申请
     *
     * @param apply 申请信息
     * @return 是否成功
     */
    @Override
    public Boolean submitApply(MerchantProfitSharingApply apply) {
        // 验证必填字段
        if (ObjectUtil.isNull(apply.getMerId())) {
            throw new CrmebException("商户ID不能为空");
        }
        if (StrUtil.isBlank(apply.getSubMchId())) {
            throw new CrmebException("子商户号不能为空");
        }
        if (StrUtil.isBlank(apply.getAccount())) {
            throw new CrmebException("分账接收方账户不能为空");
        }
        if (StrUtil.isBlank(apply.getName())) {
            throw new CrmebException("分账接收方姓名不能为空");
        }
        if (StrUtil.isBlank(apply.getRelationType())) {
            throw new CrmebException("关系类型不能为空");
        }

        // 检查该商户是否已有待审核的申请
        LambdaQueryWrapper<MerchantProfitSharingApply> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MerchantProfitSharingApply::getMerId, apply.getMerId());
        lqw.eq(MerchantProfitSharingApply::getApplyStatus, "PENDING");
        MerchantProfitSharingApply existApply = getOne(lqw);
        if (ObjectUtil.isNotNull(existApply)) {
            throw new CrmebException("您已有待审核的分账申请，请等待审核完成后再提交新申请");
        }

        // 生成申请单号
        apply.setApplyNo(generateApplyNo());
        apply.setApplyStatus("PENDING");
        apply.setCreateTime(new Date());
        apply.setUpdateTime(new Date());

        return save(apply);
    }

    /**
     * 审核分账申请
     *
     * @param applyId     申请ID
     * @param applyStatus 申请状态
     * @param auditRemark 审核备注
     * @param auditorId   审核人ID
     * @return 是否成功
     */
    @Override
    public Boolean auditApply(Integer applyId, String applyStatus, String auditRemark, Integer auditorId) {
        if (ObjectUtil.isNull(applyId)) {
            throw new CrmebException("申请ID不能为空");
        }
        if (StrUtil.isBlank(applyStatus)) {
            throw new CrmebException("审核状态不能为空");
        }
        if (!applyStatus.equals("APPROVED") && !applyStatus.equals("REJECTED")) {
            throw new CrmebException("审核状态值错误");
        }

        MerchantProfitSharingApply apply = getById(applyId);
        if (ObjectUtil.isNull(apply)) {
            throw new CrmebException("申请记录不存在");
        }
        if (!"PENDING".equals(apply.getApplyStatus())) {
            throw new CrmebException("该申请已经审核过了");
        }

        apply.setApplyStatus(applyStatus);
        apply.setAuditRemark(auditRemark);
        apply.setAuditTime(new Date());
        apply.setAuditorId(auditorId);
        apply.setUpdateTime(new Date());

        return updateById(apply);
    }

    /**
     * 根据申请号获取申请记录
     *
     * @param applyNo 申请号
     * @return 申请记录
     */
    @Override
    public MerchantProfitSharingApply getByApplyNo(String applyNo) {
        LambdaQueryWrapper<MerchantProfitSharingApply> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MerchantProfitSharingApply::getApplyNo, applyNo);
        return getOne(lqw);
    }

    /**
     * 生成申请编号
     *
     * @return 申请编号
     */
    @Override
    public String generateApplyNo() {
        return CrmebUtil.getOrderNo("PSA");
    }

    /**
     * 获取分账申请统计数据
     *
     * @param merId 商户ID
     * @return 统计数据
     */
    @Override
    public Map<String, Object> getApplyStats(Integer merId) {
        Map<String, Object> stats = new HashMap<>();
        
        LambdaQueryWrapper<MerchantProfitSharingApply> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MerchantProfitSharingApply::getMerId, merId);
        
        // 总申请数
        int totalApplyCount = count(lqw);
        stats.put("totalApplyCount", totalApplyCount);
        
        // 待审核申请数
        lqw.clear();
        lqw.eq(MerchantProfitSharingApply::getMerId, merId);
        lqw.eq(MerchantProfitSharingApply::getApplyStatus, "PENDING");
        int pendingCount = count(lqw);
        stats.put("pendingApplyCount", pendingCount);
        
        // 已通过申请数
        lqw.clear();
        lqw.eq(MerchantProfitSharingApply::getMerId, merId);
        lqw.eq(MerchantProfitSharingApply::getApplyStatus, "APPROVED");
        int approvedCount = count(lqw);
        stats.put("approvedApplyCount", approvedCount);
        
        // 已拒绝申请数
        lqw.clear();
        lqw.eq(MerchantProfitSharingApply::getMerId, merId);
        lqw.eq(MerchantProfitSharingApply::getApplyStatus, "REJECTED");
        int rejectedCount = count(lqw);
        stats.put("rejectedApplyCount", rejectedCount);
        
        // 申请通过率
        if (totalApplyCount > 0) {
            BigDecimal approvalRate = new BigDecimal(approvedCount)
                    .divide(new BigDecimal(totalApplyCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            stats.put("approvalRate", approvalRate);
        } else {
            stats.put("approvalRate", BigDecimal.ZERO);
        }
        
        // 本月申请数
        Calendar thisMonth = Calendar.getInstance();
        thisMonth.set(Calendar.DAY_OF_MONTH, 1);
        thisMonth.set(Calendar.HOUR_OF_DAY, 0);
        thisMonth.set(Calendar.MINUTE, 0);
        thisMonth.set(Calendar.SECOND, 0);
        thisMonth.set(Calendar.MILLISECOND, 0);
        Date startOfMonth = thisMonth.getTime();
        
        lqw.clear();
        lqw.eq(MerchantProfitSharingApply::getMerId, merId);
        lqw.ge(MerchantProfitSharingApply::getCreateTime, startOfMonth);
        int monthApplyCount = count(lqw);
        stats.put("monthApplyCount", monthApplyCount);
        
        return stats;
    }
} 