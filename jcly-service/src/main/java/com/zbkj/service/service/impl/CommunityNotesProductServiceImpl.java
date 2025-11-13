package com.zbkj.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.community.CommunityNotesProduct;
import com.zbkj.common.model.product.Product;
import com.zbkj.service.dao.community.CommunityNotesProductDao;
import com.zbkj.service.service.CommunityNotesProductService;
import com.zbkj.service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
* CommunityNotesProduct 接口实现
* +----------------------------------------------------------------------
* | JCLY [ JCLY赋能开发者，助力企业发展 ]
* +----------------------------------------------------------------------
* | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
* +----------------------------------------------------------------------
* | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
* +----------------------------------------------------------------------
* | Author: dudl
* +----------------------------------------------------------------------
*/
@Service
public class CommunityNotesProductServiceImpl extends ServiceImpl<CommunityNotesProductDao, CommunityNotesProduct> implements CommunityNotesProductService {

    @Resource
    private CommunityNotesProductDao dao;

    @Autowired
    private ProductService productService;

    /**
     * 获取笔记关联商品
     * @param noteId 笔记ID
     */
    @Override
    public List<CommunityNotesProduct> findListByNoteId(Integer noteId) {
        LambdaQueryWrapper<CommunityNotesProduct> lqw = Wrappers.lambdaQuery();
        lqw.eq(CommunityNotesProduct::getNoteId, noteId);
        List<CommunityNotesProduct> list = dao.selectList(lqw);
        list.forEach(np -> {
            Product product = productService.getById(np.getProductId());
            if (ObjectUtil.isNotNull(product)) {
                np.setProductName(product.getName());
                np.setProductImage(product.getImage());
                np.setPrice(product.getPrice());
                np.setOtPrice(product.getOtPrice());
            }
        });
        return list;
    }

    /**
     * 通过笔记ID删除关联商品
     * @param noteId 笔记ID
     */
    @Override
    public void deleteByNoteId(Integer noteId) {
        LambdaUpdateWrapper<CommunityNotesProduct> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(CommunityNotesProduct::getNoteId, noteId);
        dao.delete(wrapper);
    }

    /**
     * 根据商品ID获取关联的笔记ID列表
     * @param productId 商品ID
     * @return 笔记ID列表
     */
    @Override
    public List<Integer> findNoteIdsByProductId(Integer productId) {
        LambdaQueryWrapper<CommunityNotesProduct> lqw = Wrappers.lambdaQuery();
        lqw.eq(CommunityNotesProduct::getProductId, productId);
        lqw.select(CommunityNotesProduct::getNoteId);
        List<CommunityNotesProduct> list = dao.selectList(lqw);
        return list.stream().map(CommunityNotesProduct::getNoteId).collect(Collectors.toList());
    }
}

