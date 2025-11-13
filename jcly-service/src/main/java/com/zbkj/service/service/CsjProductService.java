package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.product.CsjProduct;
import com.zbkj.common.request.CsjProductAddRequest;
import com.zbkj.common.request.CsjProductSearchRequest;
import com.zbkj.common.response.CsjProductInfoResponse;
import com.zbkj.common.response.CsjProductListResponse;

/**
 * <p>
 * 采食家商品Service接口
 * </p>
 *
 * @author dudl
 * @since 2025-01-16
 */
public interface CsjProductService extends IService<CsjProduct> {

    /**
     * 获取采食家商品分页列表
     *
     * @param request 搜索参数
     * @return PageInfo
     */
    PageInfo<CsjProductListResponse> getList(CsjProductSearchRequest request);

    /**
     * 新增采食家商品
     *
     * @param request 商品请求对象
     * @return Boolean
     */
    Boolean save(CsjProductAddRequest request);

    /**
     * 更新采食家商品信息
     *
     * @param request 商品参数
     * @return 更新结果
     */
    Boolean updateProduct(CsjProductAddRequest request);

    /**
     * 采食家商品详情
     *
     * @param id 商品id
     * @return CsjProductInfoResponse
     */
    CsjProductInfoResponse getInfo(Integer id);

    /**
     * 删除采食家商品
     *
     * @param id 商品id
     * @return Boolean
     */
    Boolean deleteProduct(Integer id);

    /**
     * 上架采食家商品
     *
     * @param id 商品id
     * @return Boolean
     */
    Boolean putOnShelf(Integer id);

    /**
     * 下架采食家商品
     *
     * @param id 商品id
     * @return Boolean
     */
    Boolean offShelf(Integer id);
}
