package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.member.Member;
import com.zbkj.common.model.member.MemberIntegralRecord;
import com.zbkj.common.model.order.Order;
import com.zbkj.common.request.MemberIntegralRequest;
import com.zbkj.common.request.MemberPageRequest;
import com.zbkj.common.request.MemberSendCouponRequest;
import com.zbkj.common.response.StoreCouponUserResponse;

import java.util.List;

/**
 * 会员服务接口
 */
public interface MemberService extends IService<Member> {

    /**
     * 会员列表
     * @param request 请求参数
     * @return 会员列表
     */
    List<Member> getList(MemberPageRequest request);

    /**
     * 会员详情
     * @param id 会员ID
     * @return 会员详情
     */
    Member getDetail(Integer id);

    /**
     * 会员积分变更
     * @param request 请求参数
     * @return 是否成功
     */
    boolean changeIntegral(MemberIntegralRequest request);

    /**
     * 会员积分记录
     * @param memberId 会员ID
     * @param page 页码
     * @param limit 每页数量
     * @return 积分记录列表
     */
    List<MemberIntegralRecord> getIntegralList(Integer memberId, Integer page, Integer limit);

    /**
     * 会员订单记录
     * @param memberId 会员ID
     * @param page 页码
     * @param limit 每页数量
     * @return 订单记录列表
     */
    PageInfo<Order> getOrderList(Integer memberId, Integer page, Integer limit);

    /**
     * 会员优惠券记录
     * @param memberId 会员ID
     * @param page 页码
     * @param limit 每页数量
     * @return 优惠券记录列表
     */
    List<StoreCouponUserResponse> getCouponList(Integer memberId, Integer page, Integer limit);

    /**
     * 发送优惠券
     * @param request 请求参数
     * @return 是否成功
     */
    boolean sendCoupon(MemberSendCouponRequest request);

    /**
     * 根据用户ID获取会员信息
     * @param uid 用户ID
     * @return 会员信息
     */
    Member getByUid(Integer uid);

    /**
     * 根据用户ID和商户ID获取会员信息
     * @param uid 用户ID
     * @param merId 商户ID
     * @return 会员信息
     */
    Member getByUidAndMerId(Integer uid, Integer merId);

    /**
     * 根据手机号获取会员信息
     * @param phone 手机号
     * @return 会员信息
     */
    Member getByPhone(String phone);

    /**
     * 创建会员
     * @param member 会员信息
     * @return 创建的会员
     */
    Member create(Member member);

    /**
     * 更新会员
     * @param member 会员信息
     * @return 是否成功
     */
    boolean update(Member member);

    /**
     * 消费后更新会员积分和等级
     * @param uid 用户ID
     * @param merId 商户ID
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param integral 积分
     * @return 是否成功
     */
    boolean consumeUpdateIntegralAndLevel(Integer uid, Integer merId, Integer orderId, String orderNo, Integer integral);
}