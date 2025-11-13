package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.member.MemberIntegralRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员积分记录数据访问接口
 */
@Mapper
public interface MemberIntegralRecordDao extends BaseMapper<MemberIntegralRecord> {
}