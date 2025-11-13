package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.product.CsjProduct;
import com.zbkj.common.model.product.CsjProductDescription;
import com.zbkj.common.request.CsjProductAddRequest;
import com.zbkj.common.request.CsjProductSearchRequest;
import com.zbkj.common.response.CsjProductInfoResponse;
import com.zbkj.common.response.CsjProductListResponse;
import com.zbkj.service.dao.CsjProductDao;
import com.zbkj.service.service.CsjProductDescriptionService;
import com.zbkj.service.service.CsjProductService;
import com.zbkj.service.service.SystemAttachmentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 采食家商品Service实现类
 * </p>
 *
 * @author dudl
 * @since 2025-01-16
 */
@Service
public class CsjProductServiceImpl extends ServiceImpl<CsjProductDao, CsjProduct> implements CsjProductService {

    @Resource
    private CsjProductDao dao;

    @Autowired
    private CsjProductDescriptionService csjProductDescriptionService;

    @Autowired
    private SystemAttachmentService systemAttachmentService;

    /**
     * 获取采食家商品分页列表
     *
     * @param request 搜索参数
     * @return PageInfo
     */
    @Override
    public PageInfo<CsjProductListResponse> getList(CsjProductSearchRequest request) {
        Page<CsjProduct> page = PageHelper.startPage(request.getPage(), request.getLimit());

        LambdaQueryWrapper<CsjProduct> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CsjProduct::getIsDel, false);
        
        if (StrUtil.isNotBlank(request.getName())) {
            lqw.like(CsjProduct::getName, request.getName());
        }
        if (StrUtil.isNotBlank(request.getCategoryName())) {
            lqw.eq(CsjProduct::getCategoryName, request.getCategoryName());
        }
        if (StrUtil.isNotBlank(request.getBrandName())) {
            lqw.eq(CsjProduct::getBrandName, request.getBrandName());
        }
        if (ObjectUtil.isNotNull(request.getIsShow())) {
            lqw.eq(CsjProduct::getIsShow, request.getIsShow());
        }
        
        lqw.orderByDesc(CsjProduct::getSort, CsjProduct::getId);
        List<CsjProduct> list = dao.selectList(lqw);

        List<CsjProductListResponse> responseList = CollUtil.newArrayList();
        for (CsjProduct csjProduct : list) {
            CsjProductListResponse response = new CsjProductListResponse();
            BeanUtils.copyProperties(csjProduct, response);

            // 添加图片URL前缀
            response.setImage(response.getImage());
            response.setSliderImage(response.getSliderImage());

            responseList.add(response);
        }

        return new PageInfo<>(responseList);
    }

    /**
     * 新增采食家商品
     *
     * @param request 商品请求对象
     * @return Boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean save(CsjProductAddRequest request) {
        CsjProduct csjProduct = new CsjProduct();
        BeanUtils.copyProperties(request, csjProduct);
        csjProduct.setCreateTime(new Date());
        csjProduct.setUpdateTime(new Date());
        csjProduct.setIsDel(false);

        // 清除图片URL前缀
        String cdnUrl = systemAttachmentService.getCdnUrl();
        csjProduct.setImage(systemAttachmentService.clearPrefix(csjProduct.getImage(), cdnUrl));
        csjProduct.setSliderImage(clearSliderImagePrefixes(csjProduct.getSliderImage(), cdnUrl));

        boolean save = save(csjProduct);
        if (!save) {
            throw new CrmebException("新增采食家商品失败");
        }

        // 保存商品详情
        if (StrUtil.isNotBlank(request.getContent())) {
            CsjProductDescription description = new CsjProductDescription();
            description.setProductId(csjProduct.getId());
            description.setDescription(systemAttachmentService.clearPrefix(request.getContent(), cdnUrl));
            description.setCreateTime(new Date());
            description.setUpdateTime(new Date());
            csjProductDescriptionService.save(description);
        }

        return true;
    }

    /**
     * 更新采食家商品信息
     *
     * @param request 商品参数
     * @return 更新结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateProduct(CsjProductAddRequest request) {
        if (ObjectUtil.isNull(request.getId())) {
            throw new CrmebException("商品ID不能为空");
        }

        CsjProduct existProduct = getById(request.getId());
        if (ObjectUtil.isNull(existProduct) || existProduct.getIsDel()) {
            throw new CrmebException("商品不存在");
        }

        CsjProduct csjProduct = new CsjProduct();
        BeanUtils.copyProperties(request, csjProduct);
        csjProduct.setUpdateTime(new Date());

        // 清除图片URL前缀
        String cdnUrl = systemAttachmentService.getCdnUrl();
        csjProduct.setImage(systemAttachmentService.clearPrefix(csjProduct.getImage(), cdnUrl));
        csjProduct.setSliderImage(clearSliderImagePrefixes(csjProduct.getSliderImage(), cdnUrl));

        boolean update = updateById(csjProduct);
        if (!update) {
            throw new CrmebException("更新采食家商品失败");
        }

        // 更新商品详情
        if (StrUtil.isNotBlank(request.getContent())) {
            CsjProductDescription existDescription = csjProductDescriptionService.getByProductId(request.getId());
            if (ObjectUtil.isNotNull(existDescription)) {
                existDescription.setDescription(systemAttachmentService.clearPrefix(request.getContent(), cdnUrl));
                existDescription.setUpdateTime(new Date());
                csjProductDescriptionService.updateById(existDescription);
            } else {
                CsjProductDescription description = new CsjProductDescription();
                description.setProductId(request.getId());
                description.setDescription(systemAttachmentService.clearPrefix(request.getContent(), cdnUrl));
                description.setCreateTime(new Date());
                description.setUpdateTime(new Date());
                csjProductDescriptionService.save(description);
            }
        }

        return true;
    }

    /**
     * 采食家商品详情
     *
     * @param id 商品id
     * @return CsjProductInfoResponse
     */
    @Override
    public CsjProductInfoResponse getInfo(Integer id) {
        CsjProduct csjProduct = getById(id);
        if (ObjectUtil.isNull(csjProduct) || csjProduct.getIsDel()) {
            throw new CrmebException("商品不存在");
        }

        CsjProductInfoResponse response = new CsjProductInfoResponse();
        BeanUtils.copyProperties(csjProduct, response);

        // 添加图片URL前缀
        response.setImage(response.getImage());
        response.setSliderImage(response.getSliderImage());

        // 获取商品详情
        CsjProductDescription description = csjProductDescriptionService.getByProductId(id);
        if (ObjectUtil.isNotNull(description)) {
            response.setContent(description.getDescription());
        }

        return response;
    }

    /**
     * 删除采食家商品
     *
     * @param id 商品id
     * @return Boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteProduct(Integer id) {
        CsjProduct csjProduct = getById(id);
        if (ObjectUtil.isNull(csjProduct) || csjProduct.getIsDel()) {
            throw new CrmebException("商品不存在");
        }

        // 软删除商品
        LambdaUpdateWrapper<CsjProduct> wrapper =new LambdaUpdateWrapper<>();
        wrapper.eq(CsjProduct::getId, id);
        wrapper.set(CsjProduct::getIsDel, true);
        wrapper.set(CsjProduct::getUpdateTime, new Date());

        boolean update = update(wrapper);
        if (!update) {
            throw new CrmebException("删除商品失败");
        }

        // 删除商品详情
        csjProductDescriptionService.deleteByProductId(id);

        return true;
    }

    /**
     * 上架采食家商品
     *
     * @param id 商品id
     * @return Boolean
     */
    @Override
    public Boolean putOnShelf(Integer id) {
        CsjProduct csjProduct = getById(id);
        if (ObjectUtil.isNull(csjProduct) || csjProduct.getIsDel()) {
            throw new CrmebException("商品不存在");
        }

        if (csjProduct.getIsShow()) {
            throw new CrmebException("商品已上架");
        }

        LambdaUpdateWrapper<CsjProduct> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CsjProduct::getId, id);
        wrapper.set(CsjProduct::getIsShow, true);
        wrapper.set(CsjProduct::getUpdateTime, new Date());

        return update(wrapper);
    }

    /**
     * 下架采食家商品
     *
     * @param id 商品id
     * @return Boolean
     */
    @Override
    public Boolean offShelf(Integer id) {
        CsjProduct csjProduct = getById(id);
        if (ObjectUtil.isNull(csjProduct) || csjProduct.getIsDel()) {
            throw new CrmebException("商品不存在");
        }

        if (!csjProduct.getIsShow()) {
            throw new CrmebException("商品已下架");
        }

        LambdaUpdateWrapper<CsjProduct> wrapper =new LambdaUpdateWrapper<>();
        wrapper.eq(CsjProduct::getId, id);
        wrapper.set(CsjProduct::getIsShow, false);
        wrapper.set(CsjProduct::getUpdateTime, new Date());

        return update(wrapper);
    }

    /**
     * 处理轮播图片URL前缀添加，支持逗号分隔的多个URL
     *
     * @param sliderImage 轮播图片URLs，逗号分隔
     * @return 处理后的轮播图片URLs
     */
    private String prefixSliderImages(String sliderImage) {
        if (StrUtil.isBlank(sliderImage)) {
            return sliderImage;
        }

        String[] imageUrls = sliderImage.split(",");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < imageUrls.length; i++) {
            String url = imageUrls[i].trim();
            if (StrUtil.isNotBlank(url)) {
                if (i > 0) {
                    result.append(",");
                }
                result.append(systemAttachmentService.prefixImage(url));
            }
        }

        return result.toString();
    }

    /**
     * 清除轮播图片URL前缀，支持逗号分隔的多个URL
     *
     * @param sliderImage 轮播图片URLs，逗号分隔
     * @param cdnUrl CDN URL前缀
     * @return 清除前缀后的轮播图片URLs
     */
    private String clearSliderImagePrefixes(String sliderImage, String cdnUrl) {
        if (StrUtil.isBlank(sliderImage)) {
            return sliderImage;
        }

        String[] imageUrls = sliderImage.split(",");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < imageUrls.length; i++) {
            String url = imageUrls[i].trim();
            if (StrUtil.isNotBlank(url)) {
                if (i > 0) {
                    result.append(",");
                }
                result.append(systemAttachmentService.clearPrefix(url, cdnUrl));
            }
        }

        return result.toString();
    }
}
