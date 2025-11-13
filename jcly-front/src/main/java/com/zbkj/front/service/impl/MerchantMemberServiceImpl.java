package com.zbkj.front.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zbkj.common.constants.SmsConstants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.chat.UnifiedChatMessage;
import com.zbkj.common.model.member.Member;
import com.zbkj.common.model.member.MemberLevel;
import com.zbkj.common.model.merchant.Merchant;
import com.zbkj.common.model.merchant.MerchantMemberMessage;
import com.zbkj.common.model.user.User;
import com.zbkj.common.request.MemberRegisterRequest;
import com.zbkj.common.response.MemberInfoResponse;
import com.zbkj.common.utils.RedisUtil;
import com.zbkj.common.utils.ValidateFormUtil;
import com.zbkj.front.service.MerchantMemberService;
import com.zbkj.front.service.SvipMemberService;
import com.zbkj.service.service.*;
import com.zbkj.service.service.impl.UnifiedChatServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 前端商户会员服务实现类
 */
@Slf4j
@Service
public class MerchantMemberServiceImpl implements MerchantMemberService {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberLevelService memberLevelService;

    @Autowired
    private UserService userService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MerchantMemberMessageService merchantMemberMessageService;
    @Autowired
    private UnifiedChatService unifiedChatService;
    @Autowired
    private SystemAttachmentService systemAttachmentService;
    @Autowired
    private MerchantService merchantService;
    /**
     * 会员注册
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberInfoResponse register(MemberRegisterRequest request) {
        // 1. 验证参数
        validateRegisterRequest(request);
        User currentUser = userService.getInfo();
        // 2. 验证验证码
        validateSmsCode(request.getPhone(), request.getCaptcha(), request.getMerId());

        // 3. 检查是否已经是会员
        Member existMember = memberService.getByPhone(currentUser.getPhone());
        if (existMember != null && existMember.getMerId().equals(request.getMerId())) {
            throw new CrmebException("该手机号已经是会员");
        }

        // 4. 获取当前用户

        if (currentUser == null) {
            throw new CrmebException("请先登录");
        }

        // 5. 检查该用户在该商户是否已经是会员
        Member userMember = memberService.getByUid(currentUser.getId());
        if (userMember != null && userMember.getMerId().equals(request.getMerId())) {
            throw new CrmebException("您已经是该商户的会员");
        }

        // 6. 创建会员
        Member member = new Member();
        member.setUid(currentUser.getId());
        member.setMerId(request.getMerId());
        member.setPhone(currentUser.getPhone());
        member.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : currentUser.getNickname());
        member.setAvatar(StringUtils.hasText(request.getAvatar()) ? request.getAvatar() : currentUser.getAvatar());
        member.setCreateTime(new Date());
        Member createdMember = memberService.create(member);

        // 7. 清除验证码缓存
        clearSmsCodeCache(request.getPhone(), request.getMerId());

        // 8. 转换为响应对象
        return convertToMemberInfoResponse(createdMember);
    }

    /**
     * 获取会员信息
     */
    @Override
    public MemberInfoResponse getMemberInfo(Integer merId) {
        User currentUser = userService.getInfo();
        if (currentUser == null) {
            throw new CrmebException("请先登录");
        }

        // 根据用户ID和商户ID查询会员信息
        LambdaQueryWrapper<Member> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Member::getUid, currentUser.getId());
        queryWrapper.eq(Member::getMerId, merId);
        queryWrapper.eq(Member::getIsDel, false);
        
        Member member = memberService.getOne(queryWrapper);
        if (member == null) {
            throw new CrmebException("您还不是该商户的会员，请先注册");
        }

        return convertToMemberInfoResponse(member);
    }

    /**
     * 发送注册验证码
     */
    @Override
    public void sendRegisterSms(String phone, Integer merId) {
        // 1. 验证手机号格式
        ValidateFormUtil.isPhone(phone, "手机号码错误");
        // 2. 检查是否已经是会员
        LambdaQueryWrapper<Member> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Member::getPhone, phone);
        queryWrapper.eq(Member::getMerId, merId);
        queryWrapper.eq(Member::getIsDel, false);
        
        Member existMember = memberService.getOne(queryWrapper);
        if (existMember != null) {
            throw new CrmebException("该手机号已经是会员");
        }

        // 3. 检查发送频率限制
        String rateLimitKey = getSmsRateLimitKey(phone, merId);
        if (redisUtil.exists(rateLimitKey)) {
            throw new CrmebException("验证码发送过于频繁，请稍后再试");
        }

        // 4. 生成并发送验证码
        String code = generateSmsCode();
        String cacheKey = getSmsCodeCacheKey(phone, merId);
        
        try {
            // 发送短信
            smsService.sendSms(phone,code);
            
            // 缓存验证码，5分钟有效
            redisUtil.set(cacheKey, code, 300L);
            
            // 设置发送频率限制，60秒内不能重复发送
            redisUtil.set(rateLimitKey, "1", 60L);
            
            log.info("会员注册验证码发送成功，手机号: {}, 商户ID: {}", phone, merId);
            
        } catch (Exception e) {
            log.error("发送会员注册验证码失败", e);
            throw new CrmebException("验证码发送失败，请稍后重试");
        }
    }

    /**
     * 检查手机号是否已注册
     */
    @Override
    public boolean checkPhoneExists(String phone, Integer merId) {
        ValidateFormUtil.isPhone(phone, "手机号码错误");

        LambdaQueryWrapper<Member> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Member::getPhone, phone);
        queryWrapper.eq(Member::getMerId, merId);
        queryWrapper.eq(Member::getIsDel, false);
        
        return memberService.count(queryWrapper) > 0;
    }

    @Override
    public List<MerchantMemberMessage> getMerchantMessage() {
        User info = userService.getInfo();
        LambdaQueryWrapper<MerchantMemberMessage> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MerchantMemberMessage::getIsDel, false);
        queryWrapper.eq(MerchantMemberMessage::getUid, info.getId());
        queryWrapper.orderByDesc(MerchantMemberMessage::getCreateTime); // 按创建时间倒序排列，最新的在前面
        List<MerchantMemberMessage> list = merchantMemberMessageService.list(queryWrapper);
        if(list != null&&list.size()>0){
            for (MerchantMemberMessage message : list) {
                Integer merId = message.getMerId();
                Merchant merchant = merchantService.getById(merId);
                message.setMerchantAvatar(merchant.getAvatar());
            }
            return list;
        }
        return new ArrayList<>();
    }

    @Override
    public MerchantMemberMessage getMerchantMessageInfo(Integer id) {
        MerchantMemberMessage memberMessage = merchantMemberMessageService.getById(id);
        Merchant merchant = merchantService.getById(memberMessage.getMerId());
        memberMessage.setMerchantAvatar(merchant.getAvatar());
        return merchantMemberMessageService.getById(id);
    }

    /**
     * 设置商户会员消息为已读
     */
    @Override
    public boolean markMessageAsRead(Integer id) {
        User currentUser = userService.getInfo();
        if (currentUser == null) {
            throw new CrmebException("请先登录");
        }

        MerchantMemberMessage message = merchantMemberMessageService.getById(id);
        if (message == null) {
            throw new CrmebException("消息不存在");
        }

        // 检查消息是否属于当前用户
        if (!message.getUid().equals(currentUser.getId())) {
            throw new CrmebException("无权限操作此消息");
        }

        // 如果已经是已读状态，直接返回成功
        if (message.getIsRead()) {
            return true;
        }

        // 更新为已读状态
        message.setIsRead(true);
        message.setUpdateTime(new Date());
        
        return merchantMemberMessageService.updateById(message);
    }

    @Override
    public  List<MemberLevel> getLevel(Integer merId) {

        LambdaQueryWrapper<MemberLevel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MemberLevel::getIsDel, false);
        queryWrapper.eq(MemberLevel::getMerId, merId);
        queryWrapper.orderByAsc(MemberLevel::getMinIntegral);
        List<MemberLevel> memberLevels = memberLevelService.getBaseMapper().selectList(queryWrapper);
        return memberLevels;
    }

    @Override
    public Integer getUnreadMessageCount() {
        User info = userService.getInfo();
        LambdaQueryWrapper<MerchantMemberMessage> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MerchantMemberMessage::getUid, info.getId());
        queryWrapper.eq(MerchantMemberMessage::getIsDel, false);
        queryWrapper.eq(MerchantMemberMessage::getIsRead, false);
        int count = merchantMemberMessageService.count(queryWrapper);
        //统计人工客服发的消息
        int userUnreadMessageCount = unifiedChatService.getUserUnreadMessageCount(info.getId().longValue());
        return count + userUnreadMessageCount;
    }

    /**
     * 验证注册请求参数
     */
    private void validateRegisterRequest(MemberRegisterRequest request) {
        ValidateFormUtil.isPhone(request.getPhone(), "手机号码错误");

        if (!StringUtils.hasText(request.getCaptcha())) {
            throw new CrmebException("验证码不能为空");
        }

        if (request.getMerId() == null || request.getMerId() <= 0) {
            throw new CrmebException("商户ID不能为空");
        }
    }

    /**
     * 验证短信验证码
     */
    private void validateSmsCode(String phone, String code, Integer merId) {
        String cacheKey = getSmsCodeCacheKey(phone, merId);
        String cachedCode = redisUtil.get(cacheKey);
        
        if (!StringUtils.hasText(cachedCode)) {
            throw new CrmebException("验证码已过期，请重新获取");
        }
        
        if (!cachedCode.equals(code)) {
            throw new CrmebException("验证码不正确");
        }
    }

    /**
     * 清除短信验证码缓存
     */
    private void clearSmsCodeCache(String phone, Integer merId) {
        String cacheKey = getSmsCodeCacheKey(phone, merId);
        redisUtil.delete(cacheKey);
    }

    /**
     * 生成短信验证码
     */
    private String generateSmsCode() {
        return String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
    }

    /**
     * 获取短信验证码缓存key
     */
    private String getSmsCodeCacheKey(String phone, Integer merId) {
        return "member:register:sms:" + merId + ":" + phone;
    }

    /**
     * 获取短信发送频率限制key
     */
    private String getSmsRateLimitKey(String phone, Integer merId) {
        return "member:register:rate:" + merId + ":" + phone;
    }

    /**
     * 转换为会员信息响应对象
     */
    private MemberInfoResponse convertToMemberInfoResponse(Member member) {
        MemberInfoResponse response = new MemberInfoResponse();
        BeanUtils.copyProperties(member, response);

        // 设置等级信息
        if (member.getLevelId() != null && member.getLevelId() > 0) {
            MemberLevel level = memberLevelService.getById(member.getLevelId());
            if (level != null) {
                response.setLevelIcon(level.getIcon());
                response.setLevelDiscount(level.getDiscount());
                response.setLevelDescription(level.getDescription());
                
                // 计算距离下一等级所需积分
                setNextLevelInfo(response, level, member);
            }
        }

        return response;
    }

    /**
     * 设置下一等级信息
     */
    private void setNextLevelInfo(MemberInfoResponse response, MemberLevel currentLevel, Member member) {
        // 获取该商户的所有等级，按积分要求升序排列
        LambdaQueryWrapper<MemberLevel> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MemberLevel::getMerId, member.getMerId());
        queryWrapper.eq(MemberLevel::getStatus, 1);
        queryWrapper.eq(MemberLevel::getIsDel, false);
        queryWrapper.gt(MemberLevel::getMinIntegral, currentLevel.getMinIntegral());
        queryWrapper.orderByAsc(MemberLevel::getMinIntegral);

        List<MemberLevel> nextLevels = memberLevelService.list(queryWrapper);
        if (!nextLevels.isEmpty()) {
            MemberLevel nextLevel = nextLevels.get(0);
            response.setNextLevelName(nextLevel.getLevelName());
            response.setNextLevelNeedIntegral(nextLevel.getMinIntegral() - member.getIntegral());
        } else {
            response.setNextLevelName("已是最高等级");
            response.setNextLevelNeedIntegral(0);
        }
    }
}