package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.product.ProductAttrValue;

import java.util.List;
import java.util.Map;

/**
 * ProductAttrValueService 接口
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
public interface ProductAttrValueService extends IService<ProductAttrValue> {


    /**
     * 根据商品id和attrId获取规格属性
     * @param productId 商品id
     * @param attrId 属性id
     * @param type 商品类型
     * @param marketingType 营销类型
     * @return 商品属性集合
     */
    ProductAttrValue getByProductIdAndAttrId(Integer productId, Integer attrId, Integer type, Integer marketingType);


    /**
     * 根据id、类型查询
     * @param id ID
     * @param type 类型
     * @return ProductAttrValue
     */
    ProductAttrValue getByIdAndProductIdAndType(Integer id, Integer productId, Integer type);

    /**
     * 根据id、类型查询
     * @param id ID
     * @param type 类型
     * @param marketingType 营销类型
     * @return ProductAttrValue
     */
    ProductAttrValue getByIdAndProductIdAndType(Integer id, Integer productId, Integer type, Integer marketingType);


    /**
     * 添加/扣减库存
     * @param id 商品属性规格id
     * @param num 数量
     * @param operationType 类型：add—添加，sub—扣减
     * @param type 基础类型：0=普通商品,1-积分商品,2-虚拟商品,4=视频号,5-云盘商品,6-卡密商品
     */
    Boolean operationStock(Integer id, Integer num, String operationType, Integer type, Integer version);

    /**
     * 添加/扣减库存
     * @param id 商品属性规格id
     * @param num 数量
     * @param operationType 类型：add—添加，sub—扣减
     * @param type 基础类型：0=普通商品,1-积分商品,2-虚拟商品,4=视频号,5-云盘商品,6-卡密商品
     * @param marketingType 营销类型
     */
    Boolean operationStock(Integer id, Integer num, String operationType, Integer type, Integer marketingType, Integer version);

    /**
     * 删除商品规格属性值
     * @param productId 商品id
     * @param type 商品类型
     * @param marketingType 营销类型
     * @return Boolean
     */
    Boolean deleteByProductIdAndType(Integer productId, Integer type, Integer marketingType);

    /**
     * 批量删除商品sku
     * @param productIds 商品id
     * @param marketingType 营销类型
     * @return Boolean
     */
    Boolean batchDeleteByProductIdAndMarketingType(List<Integer> productIds, Integer marketingType);

    /**
     * 获取商品规格列表
     * @param productId 商品id
     * @param type 商品类型
     * @param marketingType 营销类型
     * @param showSwitch 显示开关，true-只查询开启显示的sku
     * @return List
     */
    List<ProductAttrValue> getListByProductIdAndType(Integer productId, Integer type, Integer marketingType, Boolean showSwitch);

    /**
     * 根据商品id和attrIdList获取列表集合
     * @param productId 商品id
     * @param attrIdList 属性idList
     * @param type 商品类型
     * @param marketingType 营销类型
     * @return 商品属性集合
     */
    List<ProductAttrValue> getByProductIdAndAttrIdList(Integer productId, List<Integer> attrIdList, Integer type, Integer marketingType);

    /**
     * 更新商品佣金
     * @param proId 商品ID
     * @param type 商品类型
     * @param marketingType 营销类型
     * @param brokerage 一级佣金
     * @param brokerageTwo 二级佣金
     */
    Boolean updateBrokerageByProductId(Integer proId, Integer type, Integer marketingType, Integer brokerage, Integer brokerageTwo);

    Boolean deleteByMasterIdListAndMarktingType(List<Integer> skuIdList, Integer marketingType);

    List<ProductAttrValue> getAll();

    /**
     * 根据商品ID和SKU获取商品规格信息
     *
     * @param productId 商品ID
     * @param sku SKU编码
     * @return ProductAttrValue
     */
    ProductAttrValue getByProductIdAndSku(Integer productId, String sku);

    /**
     * 批量获取商品SKU属性 - 性能优化方法
     * @param productIds 商品ID列表
     * @param marketingType 营销类型
     * @param showSwitch 显示开关
     * @return List<ProductAttrValue>
     */
    List<ProductAttrValue> batchGetListByProductIds(List<Integer> productIds, Integer marketingType, Boolean showSwitch);

    List<ProductAttrValue> getListByProductId(Integer productId);
}
