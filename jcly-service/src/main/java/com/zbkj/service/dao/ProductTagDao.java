package com.zbkj.service.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.product.ProductTag;
import com.zbkj.common.response.ProductTagTaskItem;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author dazongzi
 * @since 2025-10-11
 */
public interface ProductTagDao extends BaseMapper<ProductTag> {

    /**
     * 自营标签
     * @return 当前的自营商品列表
     */
    List<ProductTagTaskItem> calcProductTagForIsSelf();

    /**
     * 热卖
     * @return 符合条件了热卖商品
     */
    List<ProductTagTaskItem> calcProductTagForReplayCount(Map<String, Object> map);

    /**
     *  优选
     * @return 符合条件的五星评价
     */
    List<ProductTagTaskItem> calcProductTagForFiveStart(Map<String, Object> map);

    /**
     * 新品
     * @param map 新品查询天数
     * @return 符合条件的新品商品
     */
    List<ProductTagTaskItem> calcProductTagForNewGoods(Map<String, Object> map);

    /**
     *  包邮标签
     * @return 符合包邮标签的商品
     */
    List<ProductTagTaskItem> calcProductTagForFreeDelivery();
}
