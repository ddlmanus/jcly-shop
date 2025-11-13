package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.member.MemberLevel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 会员等级数据访问接口
 */
@Mapper
public interface MemberLevelDao extends BaseMapper<MemberLevel> {

    /**
     * 根据积分获取最高等级
     * @param integral 积分
     * @param merId 店铺ID
     * @return 会员等级
     */
    @Select("SELECT * FROM eb_member_level WHERE mer_id = #{merId} AND min_integral <= #{integral} AND status = 1 AND is_del = 0 ORDER BY min_integral DESC LIMIT 1")
    MemberLevel getHighestLevelByIntegral(@Param("integral") Integer integral, @Param("merId") Integer merId);

    /**
     * 获取店铺所有启用的会员等级
     * @param merId 店铺ID
     * @return 会员等级列表
     */
    @Select("SELECT * FROM eb_member_level WHERE mer_id = #{merId} AND status = 1 AND is_del = 0 ORDER BY min_integral ASC")
    List<MemberLevel> getEnabledLevelsByStoreId(@Param("merId") Integer merId);
}