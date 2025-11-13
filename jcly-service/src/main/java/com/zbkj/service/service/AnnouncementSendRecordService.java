package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.merchant.AnnouncementSendRecord;
import com.zbkj.common.request.PageParamRequest;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 公告发送记录表 服务类
 * </p>
 *
 * @author SOLO Coding
 * @since 2024-01-05
 */
public interface AnnouncementSendRecordService extends IService<AnnouncementSendRecord> {

    /**
     * 分页获取发送记录列表
     *
     * @param announcementId   公告ID
     * @param merId            商户ID
     * @param sendStatus       发送状态
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    PageInfo<AnnouncementSendRecord> getPage(Integer announcementId, Integer merId, Integer sendStatus, PageParamRequest pageParamRequest);

    /**
     * 创建发送记录
     *
     * @param sendRecord 发送记录信息
     * @return Boolean
     */
    Boolean create(AnnouncementSendRecord sendRecord);

    /**
     * 更新发送状态
     *
     * @param id         记录ID
     * @param sendStatus 发送状态
     * @param errorMsg   错误信息
     * @return Boolean
     */
    Boolean updateSendStatus(Integer id, Integer sendStatus, String errorMsg);

    /**
     * 批量更新发送状态
     *
     * @param ids        记录ID列表
     * @param sendStatus 发送状态
     * @return Boolean
     */
    Boolean batchUpdateSendStatus(List<Integer> ids, Integer sendStatus);

    /**
     * 根据公告ID获取发送记录
     *
     * @param announcementId 公告ID
     * @return List
     */
    List<AnnouncementSendRecord> getByAnnouncementId(Integer announcementId);

    /**
     * 获取发送统计信息
     *
     * @param announcementId 公告ID
     * @return Map
     */
    Map<String, Object> getSendStatistics(Integer announcementId);

    /**
     * 获取待发送的记录
     *
     * @param limit 数量限制
     * @return List
     */
    List<AnnouncementSendRecord> getPendingSendRecords(Integer limit);

    /**
     * 重新发送失败的记录
     *
     * @param id 记录ID
     * @return Boolean
     */
    Boolean resendFailedRecord(Integer id);

    /**
     * 根据商户ID获取发送记录统计
     *
     * @param merId 商户ID
     * @return Map
     */
    Map<String, Object> getMerchantSendStatistics(Integer merId);
}