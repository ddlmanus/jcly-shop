package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.community.CommunityNotesProduct;

import java.util.List;

/**
* CommunityNotesProduct 接口
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
public interface CommunityNotesProductService extends IService<CommunityNotesProduct> {

    /**
     * 获取笔记关联商品
     * @param noteId 笔记ID
     */
    List<CommunityNotesProduct> findListByNoteId(Integer noteId);

    /**
     * 通过笔记ID删除关联商品
     * @param noteId 笔记ID
     */
    void deleteByNoteId(Integer noteId);

    /**
     * 根据商品ID获取关联的笔记ID列表
     * @param productId 商品ID
     * @return 笔记ID列表
     */
    List<Integer> findNoteIdsByProductId(Integer productId);
}