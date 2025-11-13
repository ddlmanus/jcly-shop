package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.member.Member;
import com.zbkj.common.model.member.MemberLevel;
import com.zbkj.common.request.MemberLevelRequest;
import com.zbkj.common.request.MemberLevelSearchRequest;
import com.zbkj.common.request.MemberLevelStatusRequest;

import java.util.List;

/**
 * 会员等级服务接口
 */
public interface MemberLevelService extends IService<MemberLevel> {

    /**
     * 会员等级列表
     * @param request 请求参数
     * @return 会员等级列表
     */
    List<MemberLevel> getList(MemberLevelSearchRequest request);

    /**
     * 会员等级详情
     * @param id 等级ID
     * @return 会员等级详情
     */
    MemberLevel getDetail(Integer id);

    /**
     * 新增会员等级
     * @param request 请求参数
     * @return 是否成功
     */
    boolean add(MemberLevelRequest request);

    /**
     * 编辑会员等级
     * @param request 请求参数
     * @return 是否成功
     */
    boolean edit(MemberLevelRequest request);

    /**
     * 删除会员等级
     * @param id 等级ID
     * @return 是否成功
     */
    boolean delete(Integer id);

    /**
     * 修改会员等级状态
     * @param request 请求参数
     * @return 是否成功
     */
    boolean updateStatus(MemberLevelStatusRequest request);

    /**
     * 更新会员等级统计
     * @param storeId 店铺ID
     * @return 是否成功
     */
    boolean updateLevelStatistics(Integer storeId);

    /**
     * 根据积分更新会员等级
     * @param member 会员信息
     * @return 是否成功
     */
    boolean updateMemberLevel(Member member);
}