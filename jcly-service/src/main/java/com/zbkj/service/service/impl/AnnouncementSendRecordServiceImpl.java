package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.merchant.AnnouncementSendRecord;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.result.CommonResultCode;
import com.zbkj.service.dao.AnnouncementSendRecordDao;
import com.zbkj.service.service.AnnouncementSendRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 公告发送记录表 服务实现类
 * </p>
 *
 * @author SOLO Coding
 * @since 2024-01-05
 */
@Service
public class AnnouncementSendRecordServiceImpl extends ServiceImpl<AnnouncementSendRecordDao, AnnouncementSendRecord> implements AnnouncementSendRecordService {

    @Resource
    private AnnouncementSendRecordDao dao;

    private final Logger logger = LoggerFactory.getLogger(AnnouncementSendRecordServiceImpl.class);

    /**
     * 分页获取发送记录列表
     *
     * @param announcementId   公告ID
     * @param merId            商户ID
     * @param sendStatus       发送状态
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    @Override
    public PageInfo<AnnouncementSendRecord> getPage(Integer announcementId, Integer merId, Integer sendStatus, PageParamRequest pageParamRequest) {
        Page<AnnouncementSendRecord> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<AnnouncementSendRecord> lqw = Wrappers.lambdaQuery();
        if (ObjectUtil.isNotNull(announcementId)) {
            lqw.eq(AnnouncementSendRecord::getAnnouncementId, announcementId);
        }
        if (ObjectUtil.isNotNull(merId)) {
            lqw.eq(AnnouncementSendRecord::getMerId, merId);
        }
        if (ObjectUtil.isNotNull(sendStatus)) {
            lqw.eq(AnnouncementSendRecord::getSendStatus, sendStatus);
        }
        lqw.orderByDesc(AnnouncementSendRecord::getCreateTime);
        List<AnnouncementSendRecord> list = dao.selectList(lqw);
        return CommonPage.copyPageInfo(page, list);
    }

    /**
     * 创建发送记录
     *
     * @param sendRecord 发送记录信息
     * @return Boolean
     */
    @Override
    public Boolean create(AnnouncementSendRecord sendRecord) {
        sendRecord.setCreateTime(new Date());
        sendRecord.setUpdateTime(new Date());
        return save(sendRecord);
    }

    /**
     * 更新发送状态
     *
     * @param id         记录ID
     * @param sendStatus 发送状态
     * @param errorMsg   错误信息
     * @return Boolean
     */
    @Override
    public Boolean updateSendStatus(Integer id, Integer sendStatus, String errorMsg) {
        UpdateWrapper<AnnouncementSendRecord> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        updateWrapper.set("send_status", sendStatus);
        updateWrapper.set("update_time", new Date());
        if (sendStatus.equals(1)) {
            // 发送成功
            updateWrapper.set("send_time", new Date());
        } else if (sendStatus.equals(2)) {
            // 发送失败
            updateWrapper.set("error_msg", errorMsg);
        }
        return update(updateWrapper);
    }

    /**
     * 批量更新发送状态
     *
     * @param ids        记录ID列表
     * @param sendStatus 发送状态
     * @return Boolean
     */
    @Override
    public Boolean batchUpdateSendStatus(List<Integer> ids, Integer sendStatus) {
        if (CollUtil.isEmpty(ids)) {
            return true;
        }
        UpdateWrapper<AnnouncementSendRecord> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", ids);
        updateWrapper.set("send_status", sendStatus);
        updateWrapper.set("update_time", new Date());
        if (sendStatus.equals(1)) {
            updateWrapper.set("send_time", new Date());
        }
        return update(updateWrapper);
    }

    /**
     * 根据公告ID获取发送记录
     *
     * @param announcementId 公告ID
     * @return List
     */
    @Override
    public List<AnnouncementSendRecord> getByAnnouncementId(Integer announcementId) {
        LambdaQueryWrapper<AnnouncementSendRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(AnnouncementSendRecord::getAnnouncementId, announcementId);
        lqw.orderByDesc(AnnouncementSendRecord::getCreateTime);
        return dao.selectList(lqw);
    }

    /**
     * 获取发送统计信息
     *
     * @param announcementId 公告ID
     * @return Map
     */
    @Override
    public Map<String, Object> getSendStatistics(Integer announcementId) {
        Map<String, Object> result = new HashMap<>();
        
        // 总发送数
        LambdaQueryWrapper<AnnouncementSendRecord> totalLqw = Wrappers.lambdaQuery();
        totalLqw.eq(AnnouncementSendRecord::getAnnouncementId, announcementId);
        Integer totalCount = dao.selectCount(totalLqw);
        
        // 发送成功数
        LambdaQueryWrapper<AnnouncementSendRecord> successLqw = Wrappers.lambdaQuery();
        successLqw.eq(AnnouncementSendRecord::getAnnouncementId, announcementId);
        successLqw.eq(AnnouncementSendRecord::getSendStatus, 1);
        Integer successCount = dao.selectCount(successLqw);
        
        // 发送失败数
        LambdaQueryWrapper<AnnouncementSendRecord> failedLqw = Wrappers.lambdaQuery();
        failedLqw.eq(AnnouncementSendRecord::getAnnouncementId, announcementId);
        failedLqw.eq(AnnouncementSendRecord::getSendStatus, 2);
        Integer failedCount = dao.selectCount(failedLqw);
        
        // 待发送数
        LambdaQueryWrapper<AnnouncementSendRecord> pendingLqw = Wrappers.lambdaQuery();
        pendingLqw.eq(AnnouncementSendRecord::getAnnouncementId, announcementId);
        pendingLqw.eq(AnnouncementSendRecord::getSendStatus, 0);
        Integer pendingCount = dao.selectCount(pendingLqw);
        
        result.put("totalCount", totalCount);
        result.put("successCount", successCount);
        result.put("failedCount", failedCount);
        result.put("pendingCount", pendingCount);
        
        // 计算发送成功率
        if (totalCount > 0) {
            double successRate = (double) successCount / totalCount * 100;
            result.put("successRate", Math.round(successRate * 100.0) / 100.0);
        } else {
            result.put("successRate", 0.0);
        }
        
        return result;
    }

    /**
     * 获取待发送的记录
     *
     * @param limit 数量限制
     * @return List
     */
    @Override
    public List<AnnouncementSendRecord> getPendingSendRecords(Integer limit) {
        LambdaQueryWrapper<AnnouncementSendRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(AnnouncementSendRecord::getSendStatus, 0);
        lqw.orderByAsc(AnnouncementSendRecord::getCreateTime);
        lqw.last("LIMIT " + limit);
        return dao.selectList(lqw);
    }

    /**
     * 重新发送失败的记录
     *
     * @param id 记录ID
     * @return Boolean
     */
    @Override
    public Boolean resendFailedRecord(Integer id) {
        AnnouncementSendRecord record = getById(id);
        if (ObjectUtil.isNull(record)) {
            throw new CrmebException(CommonResultCode.ERROR.setMessage("发送记录不存在"));
        }
        if (!record.getSendStatus().equals(2)) {
            throw new CrmebException(CommonResultCode.ERROR.setMessage("只能重新发送失败的记录"));
        }
        
        UpdateWrapper<AnnouncementSendRecord> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        updateWrapper.set("send_status", 0); // 重置为待发送
        updateWrapper.set("error_msg", null);
        updateWrapper.set("update_time", new Date());
        return update(updateWrapper);
    }

    /**
     * 根据商户ID获取发送记录统计
     *
     * @param merId 商户ID
     * @return Map
     */
    @Override
    public Map<String, Object> getMerchantSendStatistics(Integer merId) {
        Map<String, Object> result = new HashMap<>();
        
        // 总发送数
        LambdaQueryWrapper<AnnouncementSendRecord> totalLqw = Wrappers.lambdaQuery();
        totalLqw.eq(AnnouncementSendRecord::getMerId, merId);
        Integer totalCount = dao.selectCount(totalLqw);
        
        // 发送成功数
        LambdaQueryWrapper<AnnouncementSendRecord> successLqw = Wrappers.lambdaQuery();
        successLqw.eq(AnnouncementSendRecord::getMerId, merId);
        successLqw.eq(AnnouncementSendRecord::getSendStatus, 1);
        Integer successCount = dao.selectCount(successLqw);
        
        // 今日发送数
        String today = DateUtil.today();
        LambdaQueryWrapper<AnnouncementSendRecord> todayLqw = Wrappers.lambdaQuery();
        todayLqw.eq(AnnouncementSendRecord::getMerId, merId);
        todayLqw.apply("DATE(create_time) = {0}", today);
        Integer todayCount = dao.selectCount(todayLqw);
        
        result.put("totalCount", totalCount);
        result.put("successCount", successCount);
        result.put("todayCount", todayCount);
        
        return result;
    }
}