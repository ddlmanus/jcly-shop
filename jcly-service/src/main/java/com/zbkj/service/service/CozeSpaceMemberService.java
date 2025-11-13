package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.coze.CozeSpaceMember;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.coze.CozeGetSpaceMembersResponse;

import java.util.List;

/**
 * CozeSpaceMemberService 接口
 */
public interface CozeSpaceMemberService extends IService<CozeSpaceMember> {

    /**
     * 分页获取空间成员列表
     * @param merchantId 商户ID
     * @param spaceId 空间ID
     * @param pageParamRequest 分页参数
     * @return 分页结果
     */
    PageInfo<CozeSpaceMember> getList(Integer merchantId, String spaceId, PageParamRequest pageParamRequest);

    /**
     * 根据空间ID获取成员列表
     * @param merchantId 商户ID
     * @param spaceId 空间ID
     * @return 成员列表
     */
    List<CozeSpaceMember> getBySpaceId(Integer merchantId, String spaceId);

    /**
     * 从Coze平台同步空间成员到本地数据库
     * @param merchantId 商户ID
     * @param spaceId 空间ID
     * @return 同步结果
     */
    Boolean syncSpaceMembersFromCoze(Integer merchantId, String spaceId);

    /**
     * 将Coze API响应的成员数据保存到本地数据库
     * @param merchantId 商户ID
     * @param spaceId 空间ID
     * @param members 成员数据列表
     * @return 保存结果
     */
    Boolean saveSpaceMembersFromCoze(Integer merchantId, String spaceId, List<CozeGetSpaceMembersResponse.OpenSpaceMember> members);

    /**
     * 删除空间成员
     * @param id 成员ID
     * @param merchantId 商户ID
     * @return 删除结果
     */
    Boolean deleteById(Integer id, Integer merchantId);
}
