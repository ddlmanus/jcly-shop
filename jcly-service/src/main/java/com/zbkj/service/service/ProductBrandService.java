package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.product.ProductBrand;
import com.zbkj.common.request.BrandCategorySearchRequest;
import com.zbkj.common.request.MerchantApplyProductBrandRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.ProductBrandAuditRequest;
import com.zbkj.common.request.ProductBrandAuditListRequest;
import com.zbkj.common.request.ProductBrandBatchAuditRequest;
import com.zbkj.common.request.ProductBrandRequest;
import com.zbkj.common.response.ProductBrandListResponse;
import com.zbkj.common.response.ProductBrandResponse;
import com.zbkj.common.response.ProductBrandAuditListResponse;

import java.util.List;

/**
*  ProductBrandService 接口
*  +----------------------------------------------------------------------
*  | JCLY [ JCLY赋能开发者，助力企业发展 ]
*  +----------------------------------------------------------------------
*  | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
*  +----------------------------------------------------------------------
*  | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
*  +----------------------------------------------------------------------
*  | Author: dudl
*  +----------------------------------------------------------------------
*/
public interface ProductBrandService extends IService<ProductBrand> {

    /**
     * 品牌分页列表
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    PageInfo<ProductBrandListResponse> getAdminPage(PageParamRequest pageParamRequest);

    /**
     * 添加品牌
     * @param request 添加参数
     * @return Boolean
     */
    Boolean add(ProductBrandRequest request);

    /**
     * 删除品牌
     * @param id 品牌ID
     * @return Boolean
     */
    Boolean delete(Integer id);

    /**
     * 编辑品牌
     * @param request 修改参数
     * @return Boolean
     */
    Boolean edit(ProductBrandRequest request);

    /**
     * 修改品牌显示状态
     * @param id 品牌ID
     * @return Boolean
     */
    Boolean updateShowStatus(Integer id);

    /**
     * 品牌缓存列表(全部)
     * @return List
     */
    List<ProductBrandResponse> getCacheAllList();

    /**
     * 通过分类查询品牌分页列表
     * @param request 查询参数
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    PageInfo<ProductBrandResponse> getPageListByCategory(BrandCategorySearchRequest request, PageParamRequest pageParamRequest);

    /**
     * 根据ID列表获取品牌列表
     *
     * @param brandIdList 品牌ID列表
     */
    List<ProductBrand> findByIdList(List<Integer> brandIdList);

    /**
     * 商户申请平台品牌
     * @param request 申请参数
     * @return Boolean
     */
    Boolean merchantApplyBrand(MerchantApplyProductBrandRequest request);

    /**
     * 审核商户申请的品牌
     * @param request 审核参数
     * @return Boolean
     */
    Boolean auditMerchantBrand(ProductBrandAuditRequest request);

    /**
     * 获取品牌审核列表
     * @param request 查询参数
     * @return 分页结果
     */
    PageInfo<ProductBrandAuditListResponse> getAuditList(ProductBrandAuditListRequest request);
    
    /**
     * 批量审核商户申请的品牌
     * @param request 审核参数
     * @return 审核结果
     */
    Boolean batchAuditMerchantBrand(ProductBrandBatchAuditRequest request);

    /**
     * 获取商户自己申请的品牌列表
     * @param pageParamRequest 分页参数
     * @return 分页结果
     */
    PageInfo<ProductBrandAuditListResponse> getMerchantAppliedBrandList(PageParamRequest pageParamRequest);
}
