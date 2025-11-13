package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.merchant.MerchantAnnouncement;
import com.zbkj.common.request.PageParamRequest;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商户公告表 服务类
 * </p>
 *
 * @author SOLO Coding
 * @since 2024-01-05
 */
public interface MerchantAnnouncementService extends IService<MerchantAnnouncement> {

    /**
     * 分页获取商户公告列表
     *
     * @param merId            商户ID
     * @param title            标题关键词
     * @param type             公告类型
     * @param status           公告状态
     * @param dateLimit        日期限制
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    PageInfo<MerchantAnnouncement> getPage(Integer merId, String title, Integer type, Integer status, String dateLimit, PageParamRequest pageParamRequest);

    /**
     * 创建商户公告
     *
     * @param announcement 公告信息
     * @return Boolean
     */
    Boolean create(MerchantAnnouncement announcement);

    /**
     * 更新商户公告
     *
     * @param announcement 公告信息
     * @return Boolean
     */
    Boolean updateAnnouncement(MerchantAnnouncement announcement);

    /**
     * 删除商户公告
     *
     * @param id 公告ID
     * @return Boolean
     */
    Boolean deleteAnnouncement(Integer id);

    /**
     * 发布公告
     *
     * @param id 公告ID
     * @return Boolean
     */
    Boolean publishAnnouncement(Integer id);

    /**
     * 撤回公告
     *
     * @param id 公告ID
     * @return Boolean
     */
    Boolean withdrawAnnouncement(Integer id);

    /**
     * 获取商户最新公告列表
     *
     * @param merId 商户ID
     * @param limit 数量限制
     * @return List
     */
    List<MerchantAnnouncement> getLatestAnnouncements(Integer merId, Integer limit);

    /**
     * 根据ID获取公告详情
     *
     * @param id 公告ID
     * @return MerchantAnnouncement
     */
    MerchantAnnouncement getDetailById(Integer id);

    /**
     * 获取商户公告统计信息
     *
     * @param merId 商户ID
     * @return Map
     */
    Map<String, Object> getAnnouncementStatistics(Integer merId);

    /**
     * 根据类型获取公告数量
     *
     * @param merId 商户ID
     * @param type  公告类型
     * @return Integer
     */
    Integer getCountByType(Integer merId, Integer type);
}