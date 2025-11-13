package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.merchant.AnnouncementSendRecord;
import com.zbkj.common.model.merchant.MerchantAnnouncement;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.result.CommonResultCode;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.vo.DateLimitUtilVo;
import com.zbkj.service.dao.MerchantAnnouncementDao;
import com.zbkj.service.service.AnnouncementSendRecordService;
import com.zbkj.service.service.MerchantAnnouncementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商户公告表 服务实现类
 * </p>
 *
 * @author SOLO Coding
 * @since 2024-01-05
 */
@Service
public class MerchantAnnouncementServiceImpl extends ServiceImpl<MerchantAnnouncementDao, MerchantAnnouncement> implements MerchantAnnouncementService {

    @Resource
    private MerchantAnnouncementDao dao;

    private final Logger logger = LoggerFactory.getLogger(MerchantAnnouncementServiceImpl.class);

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private AnnouncementSendRecordService announcementSendRecordService;

    /**
     * 分页获取公告列表
     *
     * @param merId            商户ID
     * @param title            标题
     * @param type             类型
     * @param status           状态
     * @param dateLimit        时间范围
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    @Override
    public PageInfo<MerchantAnnouncement> getPage(Integer merId, String title, Integer type, Integer status, String dateLimit, PageParamRequest pageParamRequest) {
        Page<MerchantAnnouncement> page = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<MerchantAnnouncement> lqw = Wrappers.lambdaQuery();
        if (ObjectUtil.isNotNull(merId)) {
            lqw.eq(MerchantAnnouncement::getMerId, merId);
        }
        if (StrUtil.isNotBlank(title)) {
            lqw.like(MerchantAnnouncement::getTitle, title);
        }
        if (ObjectUtil.isNotNull(type)) {
            lqw.eq(MerchantAnnouncement::getType, type);
        }
        if (ObjectUtil.isNotNull(status)) {
            lqw.eq(MerchantAnnouncement::getStatus, status);
        }
        if (StrUtil.isNotBlank(dateLimit)) {
            DateLimitUtilVo dateLimitUtilVo = CrmebDateUtil.getDateLimit(dateLimit);
            lqw.between(MerchantAnnouncement::getCreateTime, dateLimitUtilVo.getStartTime(), dateLimitUtilVo.getEndTime());
        }
        lqw.eq(MerchantAnnouncement::getIsDelete, false);
        lqw.orderByDesc(MerchantAnnouncement::getCreateTime);
        List<MerchantAnnouncement> list = dao.selectList(lqw);
        return CommonPage.copyPageInfo(page, list);
    }

    /**
     * 创建公告
     *
     * @param announcement 公告信息
     * @return Boolean
     */
    @Override
    public Boolean create(MerchantAnnouncement announcement) {
        announcement.setCreateTime(new Date());
        announcement.setUpdateTime(new Date());
        announcement.setIsDelete(false);
        return save(announcement);
    }

    /**
     * 更新公告
     *
     * @param announcement 公告信息
     * @return Boolean
     */
    @Override
    public Boolean updateAnnouncement(MerchantAnnouncement announcement) {
        announcement.setUpdateTime(new Date());
        return updateById(announcement);
    }

    /**
     * 删除公告
     *
     * @param id 公告ID
     * @return Boolean
     */
    @Override
    public Boolean deleteAnnouncement(Integer id) {
        MerchantAnnouncement announcement = getById(id);
        if (ObjectUtil.isNull(announcement)) {
            throw new CrmebException(CommonResultCode.ERROR.setMessage("公告不存在"));
        }
        if (announcement.getStatus().equals(1)) {
            throw new CrmebException(CommonResultCode.ERROR.setMessage("已发布的公告不能删除"));
        }
        UpdateWrapper<MerchantAnnouncement> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        updateWrapper.set("is_delete", true);
        updateWrapper.set("update_time", new Date());
        return update(updateWrapper);
    }

    /**
     * 发布公告
     *
     * @param id 公告ID
     * @return Boolean
     */
    @Override
    public Boolean publishAnnouncement(Integer id) {
        MerchantAnnouncement announcement = getById(id);
        if (ObjectUtil.isNull(announcement)) {
            throw new CrmebException(CommonResultCode.ERROR.setMessage("公告不存在"));
        }
        if (announcement.getStatus().equals(1)) {
            throw new CrmebException(CommonResultCode.ERROR.setMessage("公告已发布"));
        }
        
        return transactionTemplate.execute(e -> {
            // 更新公告状态
            UpdateWrapper<MerchantAnnouncement> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id);
            updateWrapper.set("status", 1);
            updateWrapper.set("publish_time", new Date());
            updateWrapper.set("update_time", new Date());
            boolean updateResult = update(updateWrapper);
            
            if (!updateResult) {
                logger.error("发布公告失败，公告ID: {}", id);
                e.setRollbackOnly();
                return false;
            }
            
            // 创建发送记录
            AnnouncementSendRecord sendRecord = new AnnouncementSendRecord();
            sendRecord.setAnnouncementId(id);
            sendRecord.setMerId(announcement.getMerId());
            sendRecord.setSendStatus(0); // 待发送
            sendRecord.setCreateTime(new Date());
            boolean recordResult = announcementSendRecordService.save(sendRecord);
            
            if (!recordResult) {
                logger.error("创建公告发送记录失败，公告ID: {}", id);
                e.setRollbackOnly();
                return false;
            }
            
            return true;
        });
    }

    /**
     * 撤回公告
     *
     * @param id 公告ID
     * @return Boolean
     */
    @Override
    public Boolean withdrawAnnouncement(Integer id) {
        MerchantAnnouncement announcement = getById(id);
        if (ObjectUtil.isNull(announcement)) {
            throw new CrmebException(CommonResultCode.ERROR.setMessage("公告不存在"));
        }
        if (!announcement.getStatus().equals(1)) {
            throw new CrmebException(CommonResultCode.ERROR.setMessage("只能撤回已发布的公告"));
        }
        
        UpdateWrapper<MerchantAnnouncement> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        updateWrapper.set("status", 0);
        updateWrapper.set("update_time", new Date());
        return update(updateWrapper);
    }

    /**
     * 获取最新公告列表
     *
     * @param merId 商户ID
     * @param limit 数量限制
     * @return List
     */
    @Override
    public List<MerchantAnnouncement> getLatestAnnouncements(Integer merId, Integer limit) {
        LambdaQueryWrapper<MerchantAnnouncement> lqw = Wrappers.lambdaQuery();
        lqw.eq(MerchantAnnouncement::getMerId, merId);
        lqw.eq(MerchantAnnouncement::getStatus, 1);
        lqw.eq(MerchantAnnouncement::getIsDelete, false);
        lqw.orderByDesc(MerchantAnnouncement::getPublishTime);
        lqw.last("LIMIT " + limit);
        return dao.selectList(lqw);
    }

    /**
     * 获取公告详情
     *
     * @param id 公告ID
     * @return MerchantAnnouncement
     */
    @Override
    public MerchantAnnouncement getDetailById(Integer id) {
        MerchantAnnouncement announcement = getById(id);
        if (ObjectUtil.isNull(announcement) || announcement.getIsDelete()) {
            throw new CrmebException(CommonResultCode.ERROR.setMessage("公告不存在"));
        }
        return announcement;
    }

    /**
     * 获取商户公告统计
     *
     * @param merId 商户ID
     * @return Map
     */
    @Override
    public Map<String, Object> getAnnouncementStatistics(Integer merId) {
        Map<String, Object> result = new HashMap<>();
        
        // 总公告数
        LambdaQueryWrapper<MerchantAnnouncement> totalLqw = Wrappers.lambdaQuery();
        totalLqw.eq(MerchantAnnouncement::getMerId, merId);
        totalLqw.eq(MerchantAnnouncement::getIsDelete, false);
        Integer totalCount = dao.selectCount(totalLqw);
        
        // 已发布公告数
        LambdaQueryWrapper<MerchantAnnouncement> publishedLqw = Wrappers.lambdaQuery();
        publishedLqw.eq(MerchantAnnouncement::getMerId, merId);
        publishedLqw.eq(MerchantAnnouncement::getStatus, 1);
        publishedLqw.eq(MerchantAnnouncement::getIsDelete, false);
        Integer publishedCount = dao.selectCount(publishedLqw);
        
        // 草稿公告数
        LambdaQueryWrapper<MerchantAnnouncement> draftLqw = Wrappers.lambdaQuery();
        draftLqw.eq(MerchantAnnouncement::getMerId, merId);
        draftLqw.eq(MerchantAnnouncement::getStatus, 0);
        draftLqw.eq(MerchantAnnouncement::getIsDelete, false);
        Integer draftCount = dao.selectCount(draftLqw);
        
        // 今日发布公告数
        String today = DateUtil.today();
        LambdaQueryWrapper<MerchantAnnouncement> todayLqw = Wrappers.lambdaQuery();
        todayLqw.eq(MerchantAnnouncement::getMerId, merId);
        todayLqw.eq(MerchantAnnouncement::getStatus, 1);
        todayLqw.eq(MerchantAnnouncement::getIsDelete, false);
        todayLqw.apply("DATE(publish_time) = {0}", today);
        Integer todayCount = dao.selectCount(todayLqw);
        
        result.put("totalCount", totalCount);
        result.put("publishedCount", publishedCount);
        result.put("draftCount", draftCount);
        result.put("todayCount", todayCount);
        
        return result;
    }

    /**
     * 根据类型获取公告数量
     *
     * @param merId 商户ID
     * @param type  公告类型
     * @return Integer
     */
    @Override
    public Integer getCountByType(Integer merId, Integer type) {
        LambdaQueryWrapper<MerchantAnnouncement> lqw = Wrappers.lambdaQuery();
        lqw.eq(MerchantAnnouncement::getMerId, merId);
        lqw.eq(MerchantAnnouncement::getType, type);
        lqw.eq(MerchantAnnouncement::getStatus, 1);
        lqw.eq(MerchantAnnouncement::getIsDelete, false);
        return dao.selectCount(lqw);
    }
}