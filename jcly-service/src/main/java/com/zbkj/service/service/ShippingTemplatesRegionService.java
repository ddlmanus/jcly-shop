package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.express.ShippingTemplatesRegion;
import com.zbkj.common.request.ShippingTemplatesRegionRequest;
import com.zbkj.common.response.ShippingTemplatesRegionResponse;

import java.util.List;

/**
* ShippingTemplatesRegionService 接口
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
public interface ShippingTemplatesRegionService extends IService<ShippingTemplatesRegion> {

    Boolean saveAll(List<ShippingTemplatesRegionRequest> shippingTemplatesRegionRequestList, Integer type, Integer id);

    List<ShippingTemplatesRegionResponse> getListGroup(Integer tempId);

    Boolean deleteByTempId(Integer tempId);

    /**
     * 根据模板编号、城市ID查询
     * @param tempId 模板编号
     * @param cityId 城市ID
     * @return 运费模板
     */
    ShippingTemplatesRegion getByTempIdAndCityId(Integer tempId, Integer cityId);

    /**
     * 是否存在区域id使用
     * @param regionId 区域id
     * @return Boolean
     */
    Boolean existCityId(Integer regionId);
}
