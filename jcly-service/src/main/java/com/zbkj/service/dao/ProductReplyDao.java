package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.product.ProductReply;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 评论表 Mapper 接口
 * </p>
 *
 * @author dudl
 * @since 2022-07-19
 */
public interface ProductReplyDao extends BaseMapper<ProductReply> {

    List<ProductReply> getMerchantAdminPage(Map<String, Object> map);

    List<ProductReply> getPlatAdminPage(Map<String, Object> map);

    /**
     * 批量获取商品评论统计数据
     * @param productIds 商品ID列表
     * @param type 评价类型
     * @return List<Map<String, Object>>
     */
    List<Map<String, Object>> batchGetReplyCountByProductIds(@Param("productIds") List<Integer> productIds, @Param("type") String type);
}
