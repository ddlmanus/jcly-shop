package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.chat.UnifiedChatSession;
import com.zbkj.common.model.humanservice.HumanServiceRating;
import com.zbkj.common.model.user.User;
import com.zbkj.service.dao.chat.UnifiedChatSessionDao;
import com.zbkj.service.dao.humanservice.HumanServiceRatingDao;
import com.zbkj.service.service.HumanServiceRatingService;
import com.zbkj.service.service.MerchantService;
import com.zbkj.service.service.SystemAdminService;
import com.zbkj.service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 人工客服评价服务实现
 * @author AI Assistant
 * @since 2025-01-09
 */
@Slf4j
@Service
public class HumanServiceRatingServiceImpl extends ServiceImpl<HumanServiceRatingDao, HumanServiceRating> 
        implements HumanServiceRatingService {

    @Autowired
    private HumanServiceRatingDao ratingDao;

    @Autowired
    private UnifiedChatSessionDao sessionDao;

    @Autowired
    private UserService userService;

    @Autowired
    private MerchantService merchantService;
    @Autowired
    private SystemAdminService systemAdminService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rateService(String sessionId, Long userId, Integer rating, String comment,
                              String tags, Map<String, BigDecimal> detailScores, Boolean isAnonymous) {
        log.info("用户评价客服服务，会话ID: {}, 用户ID: {}, 评分: {}", sessionId, userId, rating);

        try {
            // 检查是否已经评价过
            if (hasUserRatedSession(sessionId, userId)) {
                log.warn("用户已经评价过该会话，sessionId: {}, userId: {}", sessionId, userId);
                return false;
            }

            // 获取会话信息
            LambdaQueryWrapper<UnifiedChatSession> sessionQuery = new LambdaQueryWrapper<>();
            sessionQuery.eq(UnifiedChatSession::getSessionId, sessionId);
            UnifiedChatSession session = sessionDao.selectOne(sessionQuery);

            if (session == null) {
                log.warn("会话不存在，sessionId: {}", sessionId);
                return false;
            }

            // 获取用户信息
            User user = userService.getById(userId);
            if (user == null) {
                log.warn("用户不存在，userId: {}", userId);
                return false;
            }

            // 创建评价记录
            HumanServiceRating ratingRecord = new HumanServiceRating();
            ratingRecord.setMerId(session.getMerId());
            ratingRecord.setSessionId(sessionId);
            ratingRecord.setStaffId(session.getStaffId());
          //  ratingRecord.setStaffName(session.get());
            ratingRecord.setUserId(userId);
            ratingRecord.setUserNickname(isAnonymous ? "匿名用户" : user.getNickname());
            ratingRecord.setScore(new BigDecimal(rating));
            ratingRecord.setComment(comment);
            ratingRecord.setTags(tags);
            ratingRecord.setIsAnonymous(isAnonymous != null ? isAnonymous : false);
            ratingRecord.setStatus(HumanServiceRating.STATUS_APPROVED); // 默认审核通过

            // 确定评价类型
            if (rating >= 4) {
                ratingRecord.setRatingType(HumanServiceRating.RATING_TYPE_GOOD);
            } else if (rating >= 3) {
                ratingRecord.setRatingType(HumanServiceRating.RATING_TYPE_AVERAGE);
            } else {
                ratingRecord.setRatingType(HumanServiceRating.RATING_TYPE_BAD);
            }

            // 设置详细评分
            if (detailScores != null) {
                ratingRecord.setServiceAttitudeScore(detailScores.get("serviceAttitude"));
                ratingRecord.setProfessionalScore(detailScores.get("professional"));
                ratingRecord.setResponseSpeedScore(detailScores.get("responseSpeed"));
                ratingRecord.setSolutionScore(detailScores.get("solution"));
            }

            ratingRecord.setCreateTime(LocalDateTime.now());
            ratingRecord.setUpdateTime(LocalDateTime.now());

            // 保存评价
            int result = ratingDao.insert(ratingRecord);

            if (result > 0) {
                log.info("评价保存成功，评价ID: {}", ratingRecord.getId());
                return true;
            } else {
                log.error("评价保存失败");
                return false;
            }

        } catch (Exception e) {
            log.error("评价客服服务异常", e);
            throw new RuntimeException("评价失败", e);
        }
    }

    @Override
    public PageInfo<HumanServiceRating> getRatingManagementList(Long merId, Long staffId,
                                                               Integer ratingType, Integer status,
                                                               String keyword, Integer page, Integer size) {
        log.info("获取评价管理列表，商户ID: {}, 客服ID: {}, 评价类型: {}, 状态: {}, 关键词: {}, 页码: {}, 页大小: {}",
                merId, staffId, ratingType, status, keyword, page, size);

        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 20;
        if (size > 100) size = 100;

        // 使用 PageHelper 进行分页
        PageHelper.startPage(page, size);

        // 构建查询条件
        LambdaQueryWrapper<HumanServiceRating> queryWrapper = new LambdaQueryWrapper<>();

        // 商户ID过滤
        if (merId != null) {
            queryWrapper.eq(HumanServiceRating::getMerId, merId);
        }

        // 客服ID过滤
        if (staffId != null) {
            queryWrapper.eq(HumanServiceRating::getStaffId, staffId);
        }

        // 评价类型过滤
        if (ratingType != null) {
            queryWrapper.eq(HumanServiceRating::getRatingType, ratingType);
        }

        // 状态过滤
        if (status != null) {
            queryWrapper.eq(HumanServiceRating::getStatus, status);
        }

        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(HumanServiceRating::getComment, keyword)
                    .or()
                    .like(HumanServiceRating::getStaffName, keyword)
                    .or()
                    .like(HumanServiceRating::getUserNickname, keyword)
            );
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc(HumanServiceRating::getCreateTime);

        // 执行查询
        List<HumanServiceRating> ratingList = ratingDao.selectList(queryWrapper);

        // 转换为PageInfo
        PageInfo<HumanServiceRating> result = new PageInfo<>(ratingList);

        log.info("获取评价管理列表成功，总数: {}, 当前页: {}, 页大小: {}",
                result.getTotal(), result.getPageNum(), result.getPageSize());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAuditRatings(List<Long> ratingIds, Integer status) {
        log.info("批量审核评价，评价ID列表: {}, 状态: {}", ratingIds, status);

        if (CollectionUtils.isEmpty(ratingIds)) {
            log.warn("评价ID列表为空");
            return false;
        }

        try {
            LambdaQueryWrapper<HumanServiceRating> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(HumanServiceRating::getId, ratingIds);

            HumanServiceRating updateRecord = new HumanServiceRating();
            updateRecord.setStatus(status);
            updateRecord.setUpdateTime(LocalDateTime.now());

            int result = ratingDao.update(updateRecord, queryWrapper);

            if (result > 0) {
                log.info("批量审核评价成功，影响行数: {}", result);
                return true;
            } else {
                log.warn("批量审核评价失败，没有找到匹配的记录");
                return false;
            }

        } catch (Exception e) {
            log.error("批量审核评价异常", e);
            throw new RuntimeException("批量审核失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRating(Long ratingId) {
        log.info("删除评价，评价ID: {}", ratingId);

        try {
            int result = ratingDao.deleteById(ratingId);

            if (result > 0) {
                log.info("删除评价成功");
                return true;
            } else {
                log.warn("删除评价失败，评价不存在");
                return false;
            }

        } catch (Exception e) {
            log.error("删除评价异常", e);
            throw new RuntimeException("删除评价失败", e);
        }
    }

    @Override
    public HumanServiceRating getRatingDetail(Long ratingId) {
        log.info("获取评价详情，评价ID: {}", ratingId);

        HumanServiceRating rating = ratingDao.selectById(ratingId);
        // 判断评价是否存在
        if (rating != null) {
            Long staffId = rating.getStaffId();
            Long merId = rating.getMerId();
            if(merId!=null&&merId>0){
                rating.setStaffName(merchantService.getByIdException(merId.intValue()).getName());
            }else{
                rating.setStaffName(systemAdminService.getById(staffId).getRealName());
            }
        } else {
            log.warn("评价不存在，评价ID: {}", ratingId);
        }

        return rating;
    }

    @Override
    public Map<String, Object> getStaffRatingStatistics(Long merId, Long staffId, Integer days) {
        log.info("获取客服评价统计，商户ID: {}, 客服ID: {}, 统计天数: {}", merId, staffId, days);

        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<HumanServiceRating> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HumanServiceRating::getMerId, merId)
                   .eq(HumanServiceRating::getStatus, HumanServiceRating.STATUS_APPROVED)
                   .ge(HumanServiceRating::getCreateTime, startTime);

        if (staffId != null) {
            queryWrapper.eq(HumanServiceRating::getStaffId, staffId);
        }

        List<HumanServiceRating> ratings = ratingDao.selectList(queryWrapper);

        Map<String, Object> statistics = new HashMap<>();

        // 总评价数
        statistics.put("totalRatings", ratings.size());

        // 按类型分组统计
        Map<Integer, Long> typeCount = ratings.stream()
                .collect(Collectors.groupingBy(HumanServiceRating::getRatingType, Collectors.counting()));

        statistics.put("goodRatings", typeCount.getOrDefault(HumanServiceRating.RATING_TYPE_GOOD, 0L));
        statistics.put("averageRatings", typeCount.getOrDefault(HumanServiceRating.RATING_TYPE_AVERAGE, 0L));
        statistics.put("badRatings", typeCount.getOrDefault(HumanServiceRating.RATING_TYPE_BAD, 0L));

        // 计算好评率
        if (ratings.size() > 0) {
            long goodCount = typeCount.getOrDefault(HumanServiceRating.RATING_TYPE_GOOD, 0L);
            double goodRate = (double) goodCount / ratings.size() * 100;
            statistics.put("goodRatingRate", Math.round(goodRate * 100.0) / 100.0);
        } else {
            statistics.put("goodRatingRate", 0.0);
        }

        // 平均评分
        if (!ratings.isEmpty()) {
            double avgScore = ratings.stream()
                    .mapToDouble(rating -> rating.getScore().doubleValue())
                    .average()
                    .orElse(0.0);
            statistics.put("avgScore", Math.round(avgScore * 100.0) / 100.0);
        } else {
            statistics.put("avgScore", 0.0);
        }

        return statistics;
    }

    @Override
    public HumanServiceRating getUserRatingBySession(String sessionId, Long userId) {
        log.info("获取用户对会话的评价，会话ID: {}, 用户ID: {}", sessionId, userId);

        LambdaQueryWrapper<HumanServiceRating> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HumanServiceRating::getSessionId, sessionId)
                   .eq(HumanServiceRating::getUserId, userId);

        return ratingDao.selectOne(queryWrapper);
    }

    @Override
    public boolean hasUserRatedSession(String sessionId, Long userId) {
        log.info("检查用户是否已评价会话，会话ID: {}, 用户ID: {}", sessionId, userId);

        LambdaQueryWrapper<HumanServiceRating> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HumanServiceRating::getSessionId, sessionId)
                   .eq(HumanServiceRating::getUserId, userId);

        Integer count = ratingDao.selectCount(queryWrapper);
        boolean hasRated = count > 0;

        log.info("用户评价状态检查结果: {}", hasRated ? "已评价" : "未评价");
        return hasRated;
    }
}


