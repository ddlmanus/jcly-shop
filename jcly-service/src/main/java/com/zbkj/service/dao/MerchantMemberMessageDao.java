package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.merchant.MerchantMemberMessage;
import com.zbkj.common.response.MerchantMemberMessageResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商户会员消息数据访问接口
 */
@Mapper
public interface MerchantMemberMessageDao extends BaseMapper<MerchantMemberMessage> {

    /**
     * 获取消息列表（带会员信息）
     * @param merId 商户ID
     * @param memberId 会员ID
     * @param messageType 消息类型
     * @param isRead 是否已读
     * @param keywords 关键词
     * @return 消息列表
     */
    @Select("<script>" +
            "SELECT " +
            "m.id, m.mer_id, m.uid, m.merchant_name, m.merchant_avatar, " +
            "m.content, m.message_type, m.is_read, m.create_time, m.update_time, " +
            "mem.nickname as member_nickname, mem.avatar as member_avatar, mem.phone as member_phone " +
            "FROM eb_merchant_member_message m " +
            "LEFT JOIN eb_member mem ON m.uid = mem.uid AND m.mer_id = mem.mer_id " +
            "WHERE m.is_del = 0 " +
            "<if test='merId != null'> AND m.mer_id = #{merId} </if>" +
            "<if test='memberId != null'> AND m.uid = #{memberId} </if>" +
            "<if test='messageType != null'> AND m.message_type = #{messageType} </if>" +
            "<if test='isRead != null'> AND m.is_read = #{isRead} </if>" +
            "<if test='keywords != null and keywords != \"\"'> " +
            "AND (m.content LIKE CONCAT('%', #{keywords}, '%') OR mem.nickname LIKE CONCAT('%', #{keywords}, '%')) " +
            "</if>" +
            "ORDER BY m.create_time DESC" +
            "</script>")
    List<MerchantMemberMessageResponse> selectMessageListWithMember(@Param("merId") Integer merId,
                                                                    @Param("memberId") Integer memberId,
                                                                    @Param("messageType") Integer messageType,
                                                                    @Param("isRead") Boolean isRead,
                                                                    @Param("keywords") String keywords);

    /**
     * 统计未读消息数量
     * @param merId 商户ID
     * @param uid 用户ID
     * @return 未读消息数量
     */
    @Select("SELECT COUNT(1) FROM eb_merchant_member_message WHERE mer_id = #{merId} AND uid = #{uid} AND is_read = 0 AND is_del = 0")
    int countUnreadMessages(@Param("merId") Integer merId, @Param("uid") Integer uid);
}
