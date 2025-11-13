package com.zbkj.front.service;

import com.zbkj.common.model.member.MemberLevel;
import com.zbkj.common.model.merchant.MerchantMemberMessage;
import com.zbkj.common.request.MemberRegisterRequest;
import com.zbkj.common.response.MemberInfoResponse;
import com.zbkj.common.vo.MyRecord;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 前端商户会员服务接口
 */
public interface MerchantMemberService {

    /**
     * 会员注册
     * @param request 注册请求
     * @return 会员信息
     */
    MemberInfoResponse register(MemberRegisterRequest request);

    /**
     * 获取会员信息
     * @param merId 商户ID
     * @return 会员信息
     */
    MemberInfoResponse getMemberInfo(Integer merId);

    /**
     * 发送注册验证码
     * @param phone 手机号
     * @param merId 商户ID
     */
    void sendRegisterSms(String phone, Integer merId);

    /**
     * 检查手机号是否已注册
     * @param phone 手机号
     * @param merId 商户ID
     * @return 是否存在
     */
    boolean checkPhoneExists(String phone, Integer merId);

    List<MerchantMemberMessage> getMerchantMessage();

    MerchantMemberMessage getMerchantMessageInfo(@NotNull(message = "会员消息ID不能为空") Integer id);

    /**
     * 设置商户会员消息为已读
     * @param id 消息ID
     * @return 是否成功
     */
    boolean markMessageAsRead(Integer id);

    List<MemberLevel> getLevel(Integer merId);

    Integer getUnreadMessageCount();
}