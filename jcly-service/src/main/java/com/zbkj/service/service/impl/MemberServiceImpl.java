package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.admin.SystemAdmin;
import com.zbkj.common.model.coupon.Coupon;
import com.zbkj.common.model.member.Member;
import com.zbkj.common.model.member.MemberIntegralRecord;
import com.zbkj.common.model.member.MemberIntegralRule;
import com.zbkj.common.model.member.MemberLevel;
import com.zbkj.common.model.order.MerchantOrder;
import com.zbkj.common.model.order.Order;
import com.zbkj.common.request.MemberIntegralRequest;
import com.zbkj.common.request.MemberPageRequest;
import com.zbkj.common.request.MemberSendCouponRequest;
import com.zbkj.common.response.StoreCouponUserResponse;
import com.zbkj.common.response.UserCouponResponse;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.service.dao.MemberDao;
import com.zbkj.service.dao.MemberIntegralRecordDao;
import com.zbkj.service.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会员服务实现类
 */
@Slf4j
@Service
public class MemberServiceImpl extends ServiceImpl<MemberDao, Member> implements MemberService {

    @Resource
    private MemberDao memberDao;

    @Resource
    private MemberIntegralRecordDao memberIntegralRecordDao;

    @Autowired
    private MemberLevelService memberLevelService;

    @Autowired
    private MemberIntegralRuleService memberIntegralRuleService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponUserService couponUserService;
    
    @Autowired
    private MerchantOrderService merchantOrderService;

    /**
     * 会员列表
     */
    @Override
    public List<Member> getList(MemberPageRequest request) {
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        Integer merId = systemAdmin.getMerId();
        LambdaQueryWrapper<Member> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Member::getMerId, merId);
        
        // 处理前端传递的搜索参数
        final String searchKeyword = StringUtils.isEmpty(request.getKeyword()) ? request.getContent() : request.getKeyword();
        
        // 根据搜索类型和关键词进行搜索
        if (!StringUtils.isEmpty(searchKeyword)) {
            String searchType = request.getSearchType();
            if ("nickname".equals(searchType)) {
                queryWrapper.like(Member::getNickname, searchKeyword);
            } else if ("phone".equals(searchType)) {
                queryWrapper.like(Member::getPhone, searchKeyword);
            } else {
                // 默认搜索昵称和手机号
                queryWrapper.and(wrapper -> wrapper
                        .like(Member::getNickname, searchKeyword)
                        .or()
                        .like(Member::getPhone, searchKeyword));
            }
        }
        
        // 处理等级ID参数
        Integer levelId = request.getLevelId();
        if (levelId == null && request.getLevel_id() != null) {
            levelId = request.getLevel_id();
        }
        if (levelId != null && levelId > 0) {
            queryWrapper.eq(Member::getLevelId, levelId);
        }
        
        // 处理日期范围参数
        Date startTime = request.getStartTime();
        Date endTime = request.getEndTime();
        if (startTime == null && endTime == null && !StringUtils.isEmpty(request.getDateLimit())) {
            try {
                String[] dateArr = request.getDateLimit().split(",");
                if (dateArr.length == 2) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    startTime = sdf.parse(dateArr[0]);
                    // 结束日期设置为当天23:59:59
                    endTime = new Date(sdf.parse(dateArr[1]).getTime() + 24 * 60 * 60 * 1000 - 1);
                }
            } catch (ParseException e) {
                log.warn("日期解析失败: {}", request.getDateLimit(), e);
            }
        }
        if (startTime != null && endTime != null) {
            queryWrapper.between(Member::getCreateTime, startTime, endTime);
        }
        
        queryWrapper.orderByDesc(Member::getCreateTime);
        
        Page<Member> page = PageHelper.startPage(request.getPage(), request.getLimit());
        List<Member> memberList = memberDao.selectList(queryWrapper);
        
        // 设置会员等级名称
        if (!CollectionUtils.isEmpty(memberList)) {
            List<Integer> levelIds = memberList.stream()
                    .map(Member::getLevelId)
                    .filter(id -> id != null && id > 0)
                    .distinct()
                    .collect(Collectors.toList());
            
            if (!CollectionUtils.isEmpty(levelIds)) {
                LambdaQueryWrapper<MemberLevel> levelQueryWrapper = Wrappers.lambdaQuery();
                levelQueryWrapper.in(MemberLevel::getId, levelIds);
                levelQueryWrapper.eq(MemberLevel::getIsDel, false);
                List<MemberLevel> levelList = memberLevelService.list(levelQueryWrapper);
                
                for (Member member : memberList) {
                    if (member.getLevelId() != null && member.getLevelId() > 0) {
                        boolean found = false;
                        for (MemberLevel level : levelList) {
                            if (member.getLevelId().equals(level.getId())) {
                                member.setLevelName(level.getLevelName());
                                member.setLevel_name(level.getLevelName()); // 前端兼容字段
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            member.setLevelName("普通会员");
                            member.setLevel_name("普通会员"); // 前端兼容字段
                        }
                    } else {
                        member.setLevelName("普通会员");
                        member.setLevel_name("普通会员"); // 前端兼容字段
                    }
                    
                    // 重新计算实际的订单数量，确保与订单记录查询一致
                    Integer actualOrderCount = getActualOrderCount(member.getUid(), member.getMerId());
                    member.setTotalOrderCount(actualOrderCount);
                }
            } else {
                // 如果没有有效的等级ID，设置为普通会员
                for (Member member : memberList) {
                    member.setLevelName("普通会员");
                    member.setLevel_name("普通会员"); // 前端兼容字段
                    // 重新计算实际的订单数量，确保与订单记录查询一致
                    Integer actualOrderCount = getActualOrderCount(member.getUid(), member.getMerId());
                    member.setTotalOrderCount(actualOrderCount);
                }
            }
        }
        
        return memberList;
    }

    /**
     * 会员详情
     */
    @Override
    public Member getDetail(Integer id) {
        Member member = memberDao.selectById(id);
        if (member == null) {
            throw new CrmebException("会员不存在");
        }
        
        // 设置会员等级名称，确保与列表查询逻辑一致
        if (member.getLevelId() != null && member.getLevelId() > 0) {
            LambdaQueryWrapper<MemberLevel> levelQueryWrapper = Wrappers.lambdaQuery();
            levelQueryWrapper.eq(MemberLevel::getId, member.getLevelId());
            levelQueryWrapper.eq(MemberLevel::getIsDel, false);
            MemberLevel level = memberLevelService.getOne(levelQueryWrapper);
            if (level != null) {
                member.setLevelName(level.getLevelName());
                member.setLevel_name(level.getLevelName()); // 前端兼容字段
            } else {
                member.setLevelName("普通会员");
                member.setLevel_name("普通会员"); // 前端兼容字段
            }
        } else {
            member.setLevelName("普通会员");
            member.setLevel_name("普通会员"); // 前端兼容字段
        }
        
        // 重新计算实际的订单数量，确保与订单记录查询一致
        Integer actualOrderCount = getActualOrderCount(member.getUid(), member.getMerId());
        member.setTotalOrderCount(actualOrderCount);
        
        return member;
    }
    
    /**
     * 获取实际订单数量（与订单记录查询逻辑一致）
     */
    private Integer getActualOrderCount(Integer uid, Integer merId) {
        LambdaQueryWrapper<MerchantOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(MerchantOrder::getUid, uid);
        lqw.eq(MerchantOrder::getMerId, merId);
        lqw.like(MerchantOrder::getOrderNo, "SH");
        return merchantOrderService.count(lqw);
    }

    /**
     * 会员积分变更
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeIntegral(MemberIntegralRequest request) {
        if(!CollectionUtils.isEmpty(request.getMemberIds())){
            for (Integer memberId : request.getMemberIds()) {
                // 创建积分记录
                Member member = memberDao.selectById(memberId);
                if (member == null) {
                    throw new CrmebException("会员不存在");
                }
                MemberIntegralRecord record = new MemberIntegralRecord();
                record.setMemberId(member.getId());
                record.setMerId(member.getMerId());
                record.setType(request.getType());
                record.setIntegral(request.getIntegral());
                record.setBalance(member.getIntegral() + (request.getType() == 1 ? request.getIntegral() : -request.getIntegral()));
                record.setTitle(request.getTitle());
                record.setRemark(request.getRemark());
                record.setCreateTime(new Date());
                record.setUpdateTime(new Date());
                memberIntegralRecordDao.insert(record);

                // 更新会员积分
                member.setIntegral(record.getBalance());
                memberDao.updateById(member);

                // 更新会员等级
                memberLevelService.updateMemberLevel(member);
            }

        }
        return true;
    }

    /**
     * 会员积分记录
     */
    @Override
    public List<MemberIntegralRecord> getIntegralList(Integer memberId, Integer page, Integer limit) {
        LambdaQueryWrapper<MemberIntegralRecord> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MemberIntegralRecord::getMemberId, memberId);
        queryWrapper.orderByDesc(MemberIntegralRecord::getCreateTime);
        
        Page<MemberIntegralRecord> pageHelper = PageHelper.startPage(page, limit);
        List<MemberIntegralRecord> recordList = memberIntegralRecordDao.selectList(queryWrapper);
        
        // 为每条记录设置前端需要的字段
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (MemberIntegralRecord record : recordList) {
            // 设置备注信息（前端显示description字段）
            record.setDescription(record.getRemark());
            
            // 设置格式化后的变动时间
            if (record.getCreateTime() != null) {
                record.setCreate_time(formatter.format(record.getCreateTime()));
            }
        }
        
        return recordList;
    }

    /**
     * 会员订单记录
     */
    @Override
    public PageInfo<Order> getOrderList(Integer memberId, Integer page, Integer limit) {
        // 获取会员信息
        Member member = memberDao.selectById(memberId);
        if (member == null) {
            throw new CrmebException("会员不存在");
        }

        // 调用商户订单服务获取会员在该商户的订单列表
        PageInfo<MerchantOrder> merchantOrderPage = merchantOrderService.getMerchantOrderListByUid(member.getUid(), member.getMerId(), 0, page, limit);
        
        // 转换为Order类型返回（保持原有接口兼容性）
        PageInfo<Order> orderPage = new PageInfo<>();
        orderPage.setPageNum(merchantOrderPage.getPageNum());
        orderPage.setPageSize(merchantOrderPage.getPageSize());
        orderPage.setSize(merchantOrderPage.getSize());
        orderPage.setTotal(merchantOrderPage.getTotal());
        orderPage.setPages(merchantOrderPage.getPages());
        orderPage.setHasNextPage(merchantOrderPage.isHasNextPage());
        orderPage.setHasPreviousPage(merchantOrderPage.isHasPreviousPage());
        orderPage.setIsFirstPage(merchantOrderPage.isIsFirstPage());
        orderPage.setIsLastPage(merchantOrderPage.isIsLastPage());
        orderPage.setNavigatePages(merchantOrderPage.getNavigatePages());
        orderPage.setNavigatepageNums(merchantOrderPage.getNavigatepageNums());
        orderPage.setNavigateFirstPage(merchantOrderPage.getNavigateFirstPage());
        orderPage.setNavigateLastPage(merchantOrderPage.getNavigateLastPage());
        
        // 将MerchantOrder转换为Order（映射相同字段）
        List<Order> orderList = new ArrayList<>();
        if (merchantOrderPage.getList() != null) {
            for (MerchantOrder merchantOrder : merchantOrderPage.getList()) {
                Order order = new Order();
                order.setId(merchantOrder.getId());
                order.setOrderNo(merchantOrder.getOrderNo());
                order.setMerId(merchantOrder.getMerId());
                order.setUid(merchantOrder.getUid());
                order.setTotalNum(merchantOrder.getTotalNum());
                order.setProTotalPrice(merchantOrder.getProTotalPrice());
                order.setTotalPostage(merchantOrder.getTotalPostage());
                order.setTotalPrice(merchantOrder.getTotalPrice());
                order.setCouponPrice(merchantOrder.getCouponPrice());
                order.setUseIntegral(merchantOrder.getUseIntegral());
                order.setIntegralPrice(merchantOrder.getIntegralPrice());
                order.setPayPrice(merchantOrder.getPayPrice());
                order.setPayPostage(merchantOrder.getPayPostage());
                order.setPayType(merchantOrder.getPayType());
                order.setPayChannel(merchantOrder.getPayChannel());
                order.setGainIntegral(merchantOrder.getGainIntegral());
                order.setType(merchantOrder.getType());
                order.setSecondType(merchantOrder.getSecondType());
                order.setCreateTime(merchantOrder.getCreateTime());
                order.setUpdateTime(merchantOrder.getUpdateTime());
                order.setMerCouponPrice(merchantOrder.getMerCouponPrice());
                order.setPlatCouponPrice(merchantOrder.getPlatCouponPrice());
                order.setSvipDiscountPrice(merchantOrder.getSvipDiscountPrice());
                order.setIsSvip(merchantOrder.getIsSvip());
                Order byOrderNo = orderService.getByOrderNo(merchantOrder.getOrderNo());
                order.setStatus(byOrderNo.getStatus());
                order.setPaid(byOrderNo.getPaid());
                order.setPayType(byOrderNo.getPayType());
                order.setPayChannel(byOrderNo.getPayChannel());
                order.setRefundStatus(byOrderNo.getRefundStatus());
                order.setPayTime(byOrderNo.getPayTime());
                orderList.add(order);
            }
        }
        orderPage.setList(orderList);
        
        return orderPage;
    }

    /**
     * 会员优惠券记录
     */
    @Override
    public List<StoreCouponUserResponse> getCouponList(Integer memberId, Integer page, Integer limit) {
        // 获取会员信息
        Member member = memberDao.selectById(memberId);
        if (member == null) {
            throw new CrmebException("会员不存在");
        }
        
        // 调用优惠券服务获取会员优惠券列表
        PageHelper.startPage(page, limit);
        PageInfo<UserCouponResponse> pageInfo = couponUserService.getMyCouponList(
                new com.zbkj.common.request.MyCouponRequest().setUid(member.getUid()).setType("usable"));
        
        List<StoreCouponUserResponse> responseList = new ArrayList<>();
        if (pageInfo != null && !CollectionUtils.isEmpty(pageInfo.getList())) {
            for (UserCouponResponse coupon : pageInfo.getList()) {
                StoreCouponUserResponse response = new StoreCouponUserResponse();
                // 手动转换字段，确保类型匹配
                response.setId(coupon.getId());
                response.setName(coupon.getName());
                response.setMoney(coupon.getMoney() != null ? new BigDecimal(coupon.getMoney()) : BigDecimal.ZERO);
                response.setMinPrice(coupon.getMinPrice() != null ? new BigDecimal(coupon.getMinPrice()) : BigDecimal.ZERO);
                response.setStartTime(coupon.getStartTime());
                response.setEndTime(coupon.getEndTime());
                response.setUseTime(coupon.getUseTime());
                response.setStatus(coupon.getStatus());
                // 设置是否过期
                response.setIsExpired(coupon.getValidStr() != null && coupon.getValidStr().equals("overdue"));
                // 设置是否可用
                response.setIsAvailable(coupon.getValidStr() != null && coupon.getValidStr().equals("usable"));
                
                responseList.add(response);
            }
        }
        
        return responseList;
    }

    /**
     * 发送优惠券
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sendCoupon(MemberSendCouponRequest request) {
        // 1. 验证优惠券是否存在
        List<Coupon> couponList = new ArrayList<>();
        if (request.getCouponIds() != null && !request.getCouponIds().isEmpty()) {
            LambdaQueryWrapper<Coupon> couponQueryWrapper = Wrappers.lambdaQuery();
            couponQueryWrapper.in(Coupon::getId, request.getCouponIds());
            couponList = couponService.list(couponQueryWrapper);
            if (CollectionUtils.isEmpty(couponList) || couponList.size() != request.getCouponIds().size()) {
                throw new CrmebException("部分优惠券不存在，请检查优惠券ID");
            }
        } else {
            throw new CrmebException("请选择优惠券");
        }
        
        // 2. 验证会员是否存在
        List<Member> memberList;
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            LambdaQueryWrapper<Member> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.in(Member::getId, request.getMemberIds());
            memberList = memberDao.selectList(queryWrapper);
            if (CollectionUtils.isEmpty(memberList)) {
                throw new CrmebException("未找到有效会员");
            }
        } else {
            throw new CrmebException("请选择会员");
        }
        
        // 3. 批量发送优惠券
        List<Integer> userIds = memberList.stream().map(Member::getUid).collect(Collectors.toList());
        
        // 统计发送结果
        int totalSendCount = 0;
        int successCount = 0;
        int failCount = 0;
        List<String> failMessages = new ArrayList<>();
        
        // 为每个用户发送每张优惠券
        for (Integer userId : userIds) {
            for (Coupon coupon : couponList) {
                totalSendCount++;
                try {
                    // 检查用户是否可以领取该优惠券
                    if (couponUserService.userIsCanReceiveCoupon(coupon, userId)) {
                        couponUserService.autoReceiveCoupon(coupon, userId);
                        successCount++;
                        log.info("优惠券发送成功，用户ID: {}, 优惠券ID: {}, 优惠券名称: {}", 
                                userId, coupon.getId(), coupon.getName());
                    } else {
                        failCount++;
                        String failMsg = String.format("用户ID: %d 无法领取优惠券: %s (可能已达到领取限制)", userId, coupon.getName());
                        failMessages.add(failMsg);
                        log.warn(failMsg);
                    }
                } catch (Exception e) {
                    failCount++;
                    String failMsg = String.format("用户ID: %d 领取优惠券: %s 失败，原因: %s", userId, coupon.getName(), e.getMessage());
                    failMessages.add(failMsg);
                    log.error(failMsg, e);
                }
            }
        }
        
        // 4. 记录发送结果日志
        log.info("优惠券批量发送完成，总计: {} 张，成功: {} 张，失败: {} 张", 
                totalSendCount, successCount, failCount);
        
        if (!failMessages.isEmpty()) {
            log.warn("发送失败详情: {}", String.join("; ", failMessages));
        }
        
        // 如果有成功发送的，就认为操作成功
        if (successCount > 0) {
            return true;
        } else if (failCount > 0) {
            // 如果全部失败，抛出异常说明原因
            throw new CrmebException("优惠券发送失败：" + String.join("; ", failMessages.subList(0, Math.min(3, failMessages.size()))));
        }
        
        return false;
    }

    /**
     * 根据用户ID获取会员信息
     */
    @Override
    public Member getByUid(Integer uid) {
        LambdaQueryWrapper<Member> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Member::getUid, uid);
        return memberDao.selectOne(queryWrapper);
    }

    /**
     * 根据用户ID和商户ID获取会员信息
     */
    @Override
    public Member getByUidAndMerId(Integer uid, Integer merId) {
        LambdaQueryWrapper<Member> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Member::getUid, uid);
        queryWrapper.eq(Member::getMerId, merId);
        queryWrapper.eq(Member::getIsDel, false);
        return memberDao.selectOne(queryWrapper);
    }

    /**
     * 根据手机号获取会员信息
     */
    @Override
    public Member getByPhone(String phone) {
        LambdaQueryWrapper<Member> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Member::getPhone, phone);
        return memberDao.selectOne(queryWrapper);
    }

    /**
     * 创建会员
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Member create(Member member) {
        // 设置默认等级
        if (member.getLevelId() == null) {
            LambdaQueryWrapper<MemberLevel> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(MemberLevel::getMerId, member.getMerId());
            queryWrapper.eq(MemberLevel::getIsDefault, true);
            queryWrapper.eq(MemberLevel::getStatus, true);
            MemberLevel defaultLevel = memberLevelService.getOne(queryWrapper);
            if (defaultLevel != null) {
                member.setLevelId(defaultLevel.getId());
                member.setLevelName(defaultLevel.getLevelName());
            } else {
                member.setLevelId(0);
            }
        }
        
        // 设置默认值
        member.setIntegral(member.getIntegral() != null ? member.getIntegral() : 0);
        member.setTotalIntegral(member.getTotalIntegral() != null ? member.getTotalIntegral() : 0);
        member.setTotalConsume(member.getTotalConsume() != null ? member.getTotalConsume() : BigDecimal.ZERO);
        member.setTotalOrderCount(member.getTotalOrderCount() != null ? member.getTotalOrderCount() : 0);
        member.setLoginCount(member.getLoginCount() != null ? member.getLoginCount() : 0);
        member.setCreateTime(new Date());
        member.setUpdateTime(new Date());
        member.setIsDel(false);
        
        memberDao.insert(member);
        
        // 发送新人优惠券
        if (member.getUid() != null) {
            try {
                couponService.sendNewPeopleGift(member.getUid());
            } catch (Exception e) {
                log.error("发送新人优惠券失败", e);
            }
        }
        
        return member;
    }

    /**
     * 更新会员信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Member member) {
        member.setUpdateTime(new Date());
        return memberDao.updateById(member) > 0;
    }

    /**
     * 消费后更新会员积分和等级
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean consumeUpdateIntegralAndLevel(Integer uid, Integer merId, Integer orderId, String orderNo, Integer integral) {
        try {
            log.info("开始处理会员积分更新，用户ID: {}, 商户ID: {}, 订单ID: {}, 积分: {}", uid, merId, orderId, integral);
            
            // 1. 获取或创建会员
            Member member = getOrCreateMember(uid, merId);
            if (member == null) {
                log.error("获取或创建会员失败，用户ID: {}, 商户ID: {}", uid, merId);
                return false;
            }
            
            // 2. 添加积分记录
            addIntegralRecord(member, integral, orderId, orderNo);
            
            // 3. 更新会员积分和消费统计
            updateMemberConsume(member, integral, orderId);
            
            // 4. 检查并更新会员等级
            updateMemberLevelByIntegral(member);
            
            log.info("会员积分更新完成，会员ID: {}, 当前积分: {}, 等级: {}", member.getId(), member.getIntegral(), member.getLevelName());
            return true;
            
        } catch (Exception e) {
            log.error("消费更新积分和等级失败", e);
            throw new CrmebException("积分更新失败：" + e.getMessage());
        }
    }

    /**
     * 获取或创建会员
     */
    private Member getOrCreateMember(Integer uid, Integer merId) {
        LambdaQueryWrapper<Member> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Member::getUid, uid);
        queryWrapper.eq(Member::getMerId, merId);
        Member member = memberDao.selectOne(queryWrapper);
        
        if (member == null) {
            // 创建新会员
            member = new Member();
            member.setUid(uid);
            member.setMerId(merId);
            member.setIntegral(0);
            member.setTotalIntegral(0);
            member.setTotalConsume(BigDecimal.ZERO);
            member.setTotalOrderCount(0);
            member.setLoginCount(0);
            member.setCreateTime(new Date());
            member.setUpdateTime(new Date());
            member.setIsDel(false);
            
            // 设置默认等级
            MemberLevel defaultLevel = getDefaultLevel(merId);
            if (defaultLevel != null) {
                member.setLevelId(defaultLevel.getId());
                member.setLevelName(defaultLevel.getLevelName());
            } else {
                member.setLevelId(0);
                member.setLevelName("普通会员");
            }
            
            memberDao.insert(member);
            log.info("创建新会员，用户ID: {}, 商户ID: {}, 会员ID: {}", uid, merId, member.getId());
        }
        
        return member;
    }

    /**
     * 获取默认会员等级
     */
    private MemberLevel getDefaultLevel(Integer merId) {
        LambdaQueryWrapper<MemberLevel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MemberLevel::getMerId, merId);
        queryWrapper.eq(MemberLevel::getIsDefault, true);
        queryWrapper.eq(MemberLevel::getStatus, 1);
        queryWrapper.eq(MemberLevel::getIsDel, false);
        queryWrapper.orderByAsc(MemberLevel::getMinIntegral);
        return memberLevelService.getOne(queryWrapper);
    }

    /**
     * 添加积分记录
     */
    private void addIntegralRecord(Member member, Integer integral, Integer orderId, String orderNo) {
        if (integral <= 0) {
            return;
        }
        
        MemberIntegralRecord record = new MemberIntegralRecord();
        record.setMemberId(member.getId());
        record.setMerId(member.getMerId());
        record.setType(1); // 1=增加
        record.setIntegral(integral);
        record.setBalance(member.getIntegral() + integral);
        record.setTitle("购物消费获得积分");
        record.setRemark("订单号：" + orderNo + "，消费获得" + integral + "积分");
        record.setLinkId(orderId);
        record.setLinkType("order");
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        
        memberIntegralRecordDao.insert(record);
        log.info("添加积分记录，会员ID: {}, 积分: {}, 余额: {}", member.getId(), integral, record.getBalance());
    }

    /**
     * 更新会员消费信息
     */
    private void updateMemberConsume(Member member, Integer integral, Integer orderId) {
        // 获取平台订单信息
        Order order = orderService.getById(orderId);
        if (order == null) {
            log.warn("平台订单不存在，订单ID: {}", orderId);
            return;
        }
        
        // 获取对应的商户订单信息，使用商户订单的金额进行统计
        MerchantOrder merchantOrder = merchantOrderService.getOneByOrderNo(order.getOrderNo());
        if (merchantOrder == null) {
            log.warn("商户订单不存在，订单号: {}", order.getOrderNo());
            return;
        }
        
        // 更新积分
        member.setIntegral(member.getIntegral() + integral);
        member.setTotalIntegral(member.getTotalIntegral() + integral);
        
        // 使用商户订单的支付金额更新消费金额
        BigDecimal orderAmount = merchantOrder.getPayPrice() != null ? merchantOrder.getPayPrice() : BigDecimal.ZERO;
        member.setTotalConsume(member.getTotalConsume().add(orderAmount));
        
        // 更新订单数量
        member.setTotalOrderCount(member.getTotalOrderCount() + 1);
        
        member.setUpdateTime(new Date());
        memberDao.updateById(member);
        
        log.info("更新会员消费信息，会员ID: {}, 积分: {}, 商户订单金额: {}, 订单数: {}", 
                member.getId(), member.getIntegral(), orderAmount, member.getTotalOrderCount());
    }

    /**
     * 根据积分更新会员等级
     */
    private void updateMemberLevelByIntegral(Member member) {
        // 获取该商户的所有有效等级，按积分要求升序排列
        LambdaQueryWrapper<MemberLevel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MemberLevel::getMerId, member.getMerId());
        queryWrapper.eq(MemberLevel::getStatus, 1);
        queryWrapper.eq(MemberLevel::getIsDel, false);
        queryWrapper.orderByDesc(MemberLevel::getMinIntegral); // 从高到低排序
        
        List<MemberLevel> levels = memberLevelService.list(queryWrapper);
        if (CollectionUtils.isEmpty(levels)) {
            log.warn("商户没有配置会员等级，商户ID: {}", member.getMerId());
            return;
        }
        
        // 找到符合当前积分的最高等级
        MemberLevel targetLevel = null;
        for (MemberLevel level : levels) {
            if (member.getIntegral() >= level.getMinIntegral()) {
                targetLevel = level;
                break;
            }
        }
        
        // 如果没有找到符合条件的等级，使用积分要求最低的等级
        if (targetLevel == null) {
            targetLevel = levels.get(levels.size() - 1);
        }
        
        // 检查是否需要升级
        if (!targetLevel.getId().equals(member.getLevelId())) {
            Integer oldLevelId = member.getLevelId();
            String oldLevelName = member.getLevelName();
            
            member.setLevelId(targetLevel.getId());
            member.setLevelName(targetLevel.getLevelName());
            member.setUpdateTime(new Date());
            memberDao.updateById(member);
            
            // 记录等级变更
            addLevelUpgradeRecord(member, oldLevelId, oldLevelName, targetLevel);
            
            log.info("会员等级升级，会员ID: {}, 从 {} 升级到 {}, 当前积分: {}", 
                    member.getId(), oldLevelName, targetLevel.getLevelName(), member.getIntegral());
        }
    }

    /**
     * 添加等级升级记录
     */
    private void addLevelUpgradeRecord(Member member, Integer oldLevelId, String oldLevelName, MemberLevel newLevel) {
        MemberIntegralRecord record = new MemberIntegralRecord();
        record.setMemberId(member.getId());
        record.setMerId(member.getMerId());
        record.setType(1); // 1=增加（升级奖励可以在这里扩展）
        record.setIntegral(0); // 升级本身不奖励积分，可以根据需要修改
        record.setBalance(member.getIntegral());
        record.setTitle("会员等级升级");
        record.setRemark("从 " + (oldLevelName != null ? oldLevelName : "普通会员") + " 升级到 " + newLevel.getLevelName());
        record.setLinkId(newLevel.getId());
        record.setLinkType("level_upgrade");
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        
        memberIntegralRecordDao.insert(record);
    }

    /**
     * 根据订单金额计算积分
     */
    public Integer calculateIntegralFromOrder(Order order, Integer merId) {
        if (order == null || order.getPayPrice() == null || order.getPayPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        
        // 获取积分规则
        MemberIntegralRule rule = memberIntegralRuleService.getRule();
        if (rule == null || rule.getMoneyToIntegral() == null || rule.getMoneyToIntegral().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("商户积分规则未配置或无效，商户ID: {}", merId);
            return 0;
        }
        
        // 计算积分：订单金额 / 积分规则比例
        BigDecimal integralDecimal = order.getPayPrice().divide(rule.getMoneyToIntegral(), 0, RoundingMode.DOWN);
        return integralDecimal.intValue();
    }
    
    /**
     * 根据商户订单金额计算积分
     */
    public Integer calculateIntegralFromMerchantOrder(MerchantOrder merchantOrder, Integer merId) {
        if (merchantOrder == null || merchantOrder.getPayPrice() == null || merchantOrder.getPayPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        
        // 获取积分规则
        MemberIntegralRule rule = memberIntegralRuleService.getRule();
        if (rule == null || rule.getMoneyToIntegral() == null || rule.getMoneyToIntegral().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("商户积分规则未配置或无效，商户ID: {}", merId);
            return 0;
        }
        
        // 计算积分：商户订单金额 / 积分规则比例
        BigDecimal integralDecimal = merchantOrder.getPayPrice().divide(rule.getMoneyToIntegral(), 0, RoundingMode.DOWN);
        return integralDecimal.intValue();
    }
}