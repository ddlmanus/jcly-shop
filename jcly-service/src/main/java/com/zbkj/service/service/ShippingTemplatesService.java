package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.express.ShippingTemplates;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.ShippingTemplatesRequest;
import com.zbkj.common.request.ShippingTemplatesSearchRequest;
import com.zbkj.common.response.ShippingTemplatesInfoResponse;

import java.util.List;

/**
 * ShippingTemplatesService 接口
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
public interface ShippingTemplatesService extends IService<ShippingTemplates> {

    List<ShippingTemplates> getList(ShippingTemplatesSearchRequest request, PageParamRequest pageParamRequest);

    /**
     * 新增运费模板
     *
     * @param request 请求参数
     * @return 新增结果
     */
    Boolean create(ShippingTemplatesRequest request);

    Boolean edit(ShippingTemplatesRequest request);

    /**
     * 删除模板
     *
     * @param id 模板id
     * @return Boolean
     */
    Boolean remove(Integer id);

    /**
     * 获取模板信息
     *
     * @param id 模板id
     * @return ShippingTemplates
     */
    ShippingTemplatesInfoResponse getInfo(Integer id);
}
