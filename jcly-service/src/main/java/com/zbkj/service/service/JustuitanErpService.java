package com.zbkj.service.service;

import com.zbkj.common.model.product.Product;
import com.zbkj.common.model.product.ProductAttrValue;
import com.zbkj.common.model.product.ProductCategory;
import com.zbkj.common.request.JustuitanInventoryUpdateRequest;
import com.zbkj.common.request.JustuitanOrderSplitInfo;
import com.zbkj.common.response.JustuitanOrderReponse;
import com.zbkj.common.response.JustuitanOrderUploadResult;
import com.zbkj.common.response.JustuitanOrderSplitResult;
import com.zbkj.common.response.JustuitanProductUploadResult;

/**
 * 聚水潭ERP服务接口
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
public interface JustuitanErpService {

    /**
     * 同步库存到聚水潭ERP
     * @param productId 商品ID
     * @param skuId SKU ID
     * @param quantity 库存数量
     * @param operation 操作类型：add-增加，subtract-减少
     * @return 同步结果
     */
    Boolean syncInventory(Integer productId, Integer skuId, Integer quantity, String operation);

    /**
     * 批量同步库存到聚水潭ERP
     * @param inventoryList 库存变更列表
     * @return 同步结果
     */
    Boolean batchSyncInventory(java.util.List<JustuitanInventoryUpdateRequest> inventoryList);
    
    /**
     * 从聚水潭同步商品信息
     * @param modifiedBegin 修改开始时间
     * @param modifiedEnd 修改结束时间
     * @param pageIndex 页码
     * @param pageSize 每页大小
     * @return 商品列表
     */
    java.util.List<com.zbkj.common.model.product.Product> syncProductsFromJst(String modifiedBegin, String modifiedEnd, Integer pageIndex, Integer pageSize);
    
    /**
     * 全量同步聚水潭商品到系统
     * @return 同步结果
     */
    Boolean fullSyncProductsFromJst();
    
    /**
     * 从聚水潭同步库存信息到系统
     * @return 同步结果
     */
    Boolean syncInventoryFromJst();
    
    /**
     * 上传商品到聚水潭
     * @param product 商品信息
     * @return 上传结果
     */
    JustuitanProductUploadResult uploadProductToJst(Product product);
    
    /**
     * 上传商品分类到聚水潭
     * @param category 分类信息
     * @return 上传结果
     */
    Boolean uploadCategoryToJst(ProductCategory category);
    
    /**
     * 上传订单到聚水潭
     * @param order 订单信息
     * @return 上传结果
     */
    JustuitanOrderUploadResult uploadOrderToJst(com.zbkj.common.model.order.Order order);
    
    /**
     * 订单发货到聚水潭
     * @param orderNo 订单号
     * @param logisticsCompany 物流公司名称
     * @param logisticsNo 物流单号
     * @return 发货结果
     */
    Boolean shipOrderToJst(String orderNo, String logisticsCompany, String logisticsNo);
    
    /**
     * 订单拆分
     * @param orderNo 订单号
     * @param splitInfos 拆分信息列表
     * @return 拆分结果
     */
    JustuitanOrderSplitResult splitOrderInJst(String orderNo, java.util.List<JustuitanOrderSplitInfo> splitInfos);
    
    /**
     * 从聚水潭查询订单信息
     * @param modifiedBegin 修改开始时间
     * @param modifiedEnd 修改结束时间
     * @param pageIndex 页码
     * @param pageSize 每页大小
     * @return 订单查询结果
     */
    java.util.List<JustuitanOrderReponse> queryOrdersFromJst(String modifiedBegin, String modifiedEnd, Integer pageIndex, Integer pageSize);
    
    /**
     * 同步聚水潭订单状态到本地系统
     * @return 同步结果
     */
    Boolean syncOrderStatusFromJst();
    
    /**
     * 同步单个聚水潭订单状态
     * @param jstOrder 聚水潭订单信息
     */
    void syncOrderStatus(JustuitanOrderReponse jstOrder);
    
    /**
     * 处理聚水潭订单发货信息同步
     * @param jstOrder 聚水潭订单信息
     * @return 处理结果
     */
    Boolean processJstOrderShipment(com.alibaba.fastjson.JSONObject jstOrder);
    
    /**
     * 取消聚水潭订单
     * @param orderNo 订单号
     * @param cancelType 取消类型
     * @param remark 备注
     * @return 取消结果
     */
    Boolean cancelOrderInJst(String orderNo, String cancelType, String remark);
    
    /**
     * 上传售后信息到聚水潭
     * @param orderNo 订单号
     * @param afterSaleNo 售后单号
     * @param afterSaleType 售后类型
     * @param refundAmount 退款金额
     * @param remark 备注
     * @param shopStatus 平台单据状态：WAIT_SELLER_AGREE(等待卖家同意), SUCCESS(退款成功)等
     * @return 上传结果
     */
    Boolean uploadAfterSaleToJst(String orderNo, String afterSaleNo, String afterSaleType, java.math.BigDecimal refundAmount, String remark, String shopStatus);

    /**
     * 修改聚水潭订单备注（按内部单号）
     * @param jstOrderId 聚水潭内部订单号
     * @param remark 备注内容
     * @param isAppend 是否追加备注
     * @return 修改结果
     */
    Boolean updateOrderRemarkInJst(String jstOrderId, String remark, Boolean isAppend);

    /**
     * 修改聚水潭订单快递信息
     * @param jstOrderId 聚水潭内部订单号
     * @param logisticsCode 快递公司编码
     * @return 修改结果
     */
    Boolean updateOrderLogisticsInJst(String jstOrderId, String logisticsCode);

    /**
     * 检查商户是否为自营店类型
     * @param merId 商户ID
     * @return 是否为自营店
     */
    Boolean isSelfOperatedStore(Integer merId);

    /**
     * 上传店铺商品资料到聚水潭（建立店铺商品和ERP商品的映射关系）
     * @param product 商品信息
     * @return 上传结果
     */
    Boolean uploadShopProductToJst(Product product);

    /**
     * 更新聚水潭商品状态（上架/下架）
     * @param product 商品信息
     * @param enabled 是否启用：1=启用，-1=禁用
     * @return 更新结果
     */
    Boolean updateProductStatusInJst(Product product, Integer enabled);

}