package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.member.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 会员数据访问接口
 */
@Mapper
public interface MemberDao extends BaseMapper<Member> {

    /**
     * 根据商户id和等级id查询会员数量
     * @param storeId 商户id
     * @param id 等级id
     * @return Integer
     */
    @Select("select count(1) from tb_member where mer_id = #{storeId} and level_id = #{id}")
    int countByStoreIdAndLevelId(Integer storeId, Integer id);
}