package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zbkj.common.constants.SysConfigConstants;
import com.zbkj.common.model.express.Express;
import com.zbkj.common.model.merchant.Merchant;
import com.zbkj.common.model.product.Product;
import com.zbkj.common.model.product.ProductCategory;
import com.zbkj.common.model.product.ProductBrand;
import com.zbkj.common.model.product.ProductAttrValue;
import com.zbkj.common.model.merchant.MerchantProductCategory;
import com.zbkj.common.model.order.Order;
import com.zbkj.common.model.order.OrderDetail;
import com.zbkj.common.model.order.MerchantOrder;
import com.zbkj.common.model.order.OrderInvoice;
import com.zbkj.common.model.order.OrderInvoiceDetail;
import com.zbkj.common.model.user.UserAddress;
import com.zbkj.common.model.user.User;
import com.zbkj.common.constants.ProductConstants;
import com.zbkj.common.request.OrderSendRequest;
import com.zbkj.common.response.*;
import com.zbkj.common.response.OrderInvoiceResponse;
import com.zbkj.common.utils.RestTemplateUtil;
import com.zbkj.common.request.JustuitanInventoryUpdateRequest;
import com.zbkj.common.request.JustuitanOrderSplitInfo;
import com.zbkj.common.utils.JustuitanApiUtil;
import com.zbkj.service.config.JustuitanErpConfig;
import com.zbkj.service.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聚水潭ERP服务实现类
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
public class JustuitanErpServiceImpl implements JustuitanErpService {

    private static final Logger logger = LoggerFactory.getLogger(JustuitanErpServiceImpl.class);

    @Autowired
    private RestTemplateUtil restTemplateUtil;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductCategoryService productCategoryService;
    
    @Autowired
    private ProductBrandService productBrandService;
    
    @Autowired
    private MerchantProductCategoryService merchantProductCategoryService;
    
    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private JustuitanErpConfig justuitanErpConfig;
    
    // 注入订单相关服务
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderDetailService orderDetailService;
    
    @Autowired
    private UserAddressService userAddressService;
    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private MerchantOrderService merchantOrderService;
    
    @Autowired
    private OrderInvoiceService orderInvoiceService;
    
    @Autowired
    private OrderInvoiceDetailService orderInvoiceDetailService;
    
    @Autowired
    private OrderStatusService orderStatusService;
    
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private ExpressService expressService;
    @Autowired
    private MerchantService merchantService;


    /**
     * 动态获取access_token
     * @return access_token
     */
    private String getAccessToken() {
        try {
            String apikey = systemConfigService.getValueByKey("jushuitan_apikey");
            String appsecret = systemConfigService.getValueByKey("jushuitan_secret");
            // 构建请求参数
            MultiValueMap<String, Object> params =JustuitanApiUtil.buildTokenParams(apikey, justuitanErpConfig.getAuth().getGrantType(), justuitanErpConfig.getAuth().getCode(), justuitanErpConfig.getAuth().getCharset());
            String sign = JustuitanApiUtil.generateSign(params, justuitanErpConfig.getAuth().getAppSecret());
            params.add("sign", sign);
            
            logger.info("获取access_token请求参数: {}", params);
            
            // 发送请求
            String response = restTemplateUtil.postFromUrlencoded(justuitanErpConfig.getAuth().getTokenUrl(), params, null);
            logger.info("获取access_token响应: {}", response);
            
            // 解析响应
            JSONObject jsonResponse = JSONObject.parseObject(response);
            if (jsonResponse.getInteger("code") == 0) {
                JSONObject data = jsonResponse.getJSONObject("data");
                if (data != null) {
                    String accessToken = data.getString("access_token");
                    if (StrUtil.isNotBlank(accessToken)) {
                        logger.info("成功获取access_token: {}", accessToken);
                        return accessToken;
                    }
                }
            }
            
            logger.error("获取access_token失败: {}", response);
            return null;
            
        } catch (Exception e) {
            logger.error("获取access_token异常", e);
            return null;
        }
    }

    @Override
    public Boolean syncInventory(Integer productId, Integer skuId, Integer stock, String operation) {
        if (!justuitanErpConfig.getSync().getEnabled()) {
            logger.debug("聚水潭ERP同步未启用，跳过库存同步");
            return true;
        }

        // 检查商品所属商户是否为自营店
//        try {
//            Product product = productService.getById(productId);
//            if (product == null || !isSelfOperatedStore(product.getMerId())) {
//                logger.debug("商品不属于自营店，跳过库存同步: productId={}, merId={}", productId, product != null ? product.getMerId() : "null");
//                return true;
//            }
//        } catch (Exception e) {
//            logger.error("检查商品商户类型失败，跳过库存同步: productId={}", productId, e);
//            return true;
//        }

        String apikey = systemConfigService.getValueByKey("jushuitan_apikey");
        String appsecret = systemConfigService.getValueByKey("jushuitan_secret");
        if (StrUtil.isBlank(apikey) || StrUtil.isBlank(appsecret)){
            logger.error("聚水潭ERP认证参数未配置，无法同步库存");
            return false;
        }

        try {
            // 构建库存更新请求
            JustuitanInventoryUpdateRequest request = new JustuitanInventoryUpdateRequest(productId, skuId, stock, operation);
            return callInventoryUpdateApi(request);
        } catch (Exception e) {
            logger.error("同步库存到聚水潭ERP失败: productId={}, skuId={}, quantity={}, operation={}", 
                    productId, skuId, stock, operation, e);
            return false;
        }
    }

    @Override
    public Boolean batchSyncInventory(List<JustuitanInventoryUpdateRequest> inventoryList) {
        if (!justuitanErpConfig.getSync().getEnabled()) {
            logger.debug("聚水潭ERP同步未启用，跳过批量库存同步");
            return true;
        }
        String apikey = systemConfigService.getValueByKey("jushuitan_apikey");
        String appsecret = systemConfigService.getValueByKey("jushuitan_secret");
        if (StrUtil.isBlank(apikey) ||
            StrUtil.isBlank(appsecret)) {
            logger.error("聚水潭ERP认证参数未配置，无法批量同步库存");
            return false;
        }

        try {
            return callBatchInventoryUpdateApi(inventoryList);
        } catch (Exception e) {
            logger.error("批量同步库存到聚水潭ERP失败", e);
            return false;
        }
    }

    /**
     * 调用聚水潭库存更新API
     */
    private Boolean callInventoryUpdateApi(JustuitanInventoryUpdateRequest request) {
        try {
            // 构建API请求参数
            Map<String, Object> params =JustuitanApiUtil.buildApiParams(justuitanErpConfig.getAuth().getAppKey(), getAccessToken());
            if (params == null) {
                logger.error("无法构建API请求参数，库存更新失败");
                return false;
            }
            
            // 构建业务参数 - 库存盘点数据
            JSONArray items = new JSONArray();
            JSONObject item = new JSONObject();
            item.put("sku_id", request.getSkuCode() != null ? request.getSkuCode() : String.valueOf(request.getSkuId()));
            item.put("qty", request.getQuantity());
            items.add(item);
            
            // 构建业务参数JSON对象
            JSONObject bizParams = new JSONObject();
            if(!StringUtils.isEmpty(request.getOrderNo())){
                bizParams.put("so_id", request.getOrderNo()); // 外部单号，使用时间戳确保唯一性
            }else{
                bizParams.put("so_id", "INV_" + System.currentTimeMillis()); // 外部单号，使用时间戳确保唯一性
            }
            bizParams.put("warehouse", "1"); // 主仓
            bizParams.put("type", "check"); // 盘点类型：全量覆盖
            bizParams.put("is_confirm", true); // 自动确认单据
            bizParams.put("items", items);
            
            // 将业务参数作为biz参数传递
            params.put("biz", bizParams.toJSONString());
            
            // 生成签名
            String sign = JustuitanApiUtil.generateSign(params, justuitanErpConfig.getAuth().getAppSecret());
            params.put("sign", sign);
            
            // 发送请求
            String url = justuitanErpConfig.getApi().getUrl() + "/open/jushuitan/inventoryv2/upload";
            
            // 使用正确的Content-Type: application/x-www-form-urlencoded;charset=UTF-8
            String response = restTemplateUtil.postFormData(url, params);
            
            // 解析响应
            return parseApiResponse(response);
            
        } catch (Exception e) {
            logger.error("调用聚水潭库存更新API失败", e);
            return false;
        }
    }

    /**
     * 调用聚水潭批量库存更新API
     */
    private Boolean callBatchInventoryUpdateApi(List<JustuitanInventoryUpdateRequest> inventoryList) {
        try {
            // 构建API请求参数
            Map<String, Object> params =JustuitanApiUtil.buildApiParams(justuitanErpConfig.getAuth().getAppKey(), getAccessToken());
            if (params == null) {
                logger.error("无法构建API请求参数，批量库存更新失败");
                return false;
            }
            
            // 构建业务参数 - 批量库存盘点数据
            JSONArray items = new JSONArray();
            for (JustuitanInventoryUpdateRequest request : inventoryList) {
                JSONObject item = new JSONObject();
                item.put("sku_id", request.getSkuCode() != null ? request.getSkuCode() : String.valueOf(request.getSkuId()));
                item.put("qty", request.getQuantity());
                items.add(item);
            }
            
            // 构建业务参数JSON对象
            JSONObject bizParams = new JSONObject();
            bizParams.put("so_id", "BATCH_INV_" + System.currentTimeMillis()); // 外部单号，使用时间戳确保唯一性
            bizParams.put("warehouse", "1"); // 主仓
            bizParams.put("type", "check"); // 盘点类型：全量覆盖
            bizParams.put("is_confirm", true); // 自动确认单据
            bizParams.put("items", items);
            
            // 将业务参数作为biz参数传递
            params.put("biz", bizParams.toJSONString());
            
            // 生成签名
            String sign = JustuitanApiUtil.generateSign(params, justuitanErpConfig.getAuth().getAppSecret());
            params.put("sign", sign);
            
            // 发送请求
            String url = justuitanErpConfig.getApi().getUrl() + "/open/jushuitan/inventoryv2/upload";
            
            // 使用正确的Content-Type: application/x-www-form-urlencoded;charset=UTF-8
            String response = restTemplateUtil.postFormData(url, params);
            
            // 解析响应
            return parseApiResponse(response);
            
        } catch (Exception e) {
            logger.error("调用聚水潭批量库存更新API失败", e);
            return false;
        }
    }


    /**
     * 解析API响应
     */
    private Boolean parseApiResponse(String response) {
        if (StrUtil.isBlank(response)) {
            logger.error("聚水潭API响应为空");
            return false;
        }
        
        try {
            JSONObject jsonResponse = JSONObject.parseObject(response);
            Integer code = jsonResponse.getInteger("code");
            if (code != 0) {
                String msg = jsonResponse.getString("msg");
                logger.error("调用聚水潭库存更新API失败: code={}, msg={}", code, msg);
                return false;
            }else {
                logger.debug("调用聚水潭库存更新API成功: response={}", response);
                return true;
            }
            
        } catch (Exception e) {
            logger.error("解析聚水潭API响应失败: response={}", response, e);
            return false;
        }
    }
    
    @Override
    public List<Product> syncProductsFromJst(String modifiedBegin, String modifiedEnd, Integer pageIndex, Integer pageSize) {
        if (!justuitanErpConfig.getSync().getEnabled()) {
            logger.debug("聚水潭ERP同步未启用，跳过商品同步");
            return new ArrayList<>();
        }
        
        if (StrUtil.isBlank(justuitanErpConfig.getAuth().getAppKey()) || 
            StrUtil.isBlank(justuitanErpConfig.getAuth().getAppSecret())) {
            logger.error("聚水潭ERP认证参数未配置，无法同步商品");
            return new ArrayList<>();
        }

        try {
            // 构建业务参数JSONObject
            JSONObject bizParams = new JSONObject();
            bizParams.put("page_index", pageIndex != null ? pageIndex : 1);
            bizParams.put("page_size", pageSize != null ? pageSize : 50);
            bizParams.put("item_flds", new JSONArray()); // 查询所有字段
            
            if (StrUtil.isNotBlank(modifiedBegin) && StrUtil.isNotBlank(modifiedEnd)) {
                bizParams.put("modified_begin", modifiedBegin);
                bizParams.put("modified_end", modifiedEnd);
                bizParams.put("date_field", "modified"); // 按修改时间查询
            } else {
                bizParams.put("date_field", "create"); // 按创建时间查询
                // 设置默认时间范围：最近30天
                String endDate = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
                String startDate = DateUtil.format(DateUtil.offsetDay(new Date(), -30), "yyyy-MM-dd HH:mm:ss");
                bizParams.put("modified_begin", startDate);
                bizParams.put("modified_end", endDate);
            }

            // 构建API请求参数
            Map<String, Object> params = JustuitanApiUtil.buildApiParams(justuitanErpConfig.getAuth().getAppKey(), getAccessToken());

//            // 构建API请求参数
//            Map<String, Object> params = new HashMap<>();
//            params.put("app_key", justuitanErpConfig.getAuth().getAppKey());
//
//            // 动态获取access_token
//            String accessToken = getAccessToken();
//            if (StrUtil.isBlank(accessToken)) {
//                logger.error("无法获取access_token，商品同步失败");
//                return new ArrayList<>();
//            }
//            params.put("access_token", accessToken);
//            params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
//            params.put("charset", "utf-8");
//            params.put("version", "2");
            
            // 将业务参数作为biz参数传递
            params.put("biz", bizParams.toJSONString());
            
            // 生成签名（必须在所有参数都设置完成后）
            String sign = JustuitanApiUtil.generateSign(params, justuitanErpConfig.getAuth().getAppSecret());
            params.put("sign", sign);
            // 发送请求
            String url = justuitanErpConfig.getApi().getUrl() + "/open/mall/item/query";
            
            // 使用正确的Content-Type: application/x-www-form-urlencoded;charset=UTF-8
            String response = restTemplateUtil.postFormData(url, params);
            
            // 解析响应并转换为Product对象
            return parseProductResponse(response);
            
        } catch (Exception e) {
            logger.error("从聚水潭同步商品失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Boolean fullSyncProductsFromJst() {
        try {
            logger.info("开始全量同步聚水潭商品");
            
            // 获取最近7天的商品数据
            String endTime = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            String startTime = DateUtil.format(DateUtil.offsetDay(new Date(), -7), "yyyy-MM-dd HH:mm:ss");
            
            int pageIndex = 1;
            int pageSize = 50;
            int totalSynced = 0;
            
            // 直接调用API，不使用syncProductsFromJst方法
            while (true) {

                Map<String, Object> params = JustuitanApiUtil.buildApiParams(justuitanErpConfig.getAuth().getAppKey(), getAccessToken());

                // 构建业务参数JSON
                JSONObject bizParams = new JSONObject();
                bizParams.put("item_flds", new JSONArray()); // 商品字段，空数组表示查询所有字段
                bizParams.put("page_index", pageIndex);
                bizParams.put("page_size", pageSize);
                bizParams.put("date_field", "create"); // date_field应该在biz参数中
                // 添加时间范围参数
                bizParams.put("modified_begin", startTime);
                bizParams.put("modified_end", endTime);
                // 将业务参数转换为JSON字符串
                params.put("biz", bizParams.toJSONString());
                
                // 生成签名（必须在所有参数都设置完成后）
                String sign = JustuitanApiUtil.generateSign(params, justuitanErpConfig.getAuth().getAppSecret());
                params.put("sign", sign);
                
                // 发送请求
                String url = justuitanErpConfig.getApi().getUrl() + "/open/mall/item/query";
                
                // 使用正确的Content-Type: application/x-www-form-urlencoded;charset=UTF-8
                String response = restTemplateUtil.postFormData(url, params);
                
                // 解析响应并转换为Product对象
                List<Product> products = parseProductResponse(response);
                
                if (CollUtil.isEmpty(products)) {
                    break;
                }
                
                // 保存商品到数据库
                for (Product product : products) {
                    try {
                        saveOrUpdateProduct(product);
                        totalSynced++;
                    } catch (Exception e) {
                        logger.error("保存商品失败: {}", product.getName(), e);
                    }
                }
                
                // 如果返回的商品数量小于页面大小，说明已经是最后一页
                if (products.size() < pageSize) {
                    break;
                }
                
                pageIndex++;
            }
            
            logger.info("全量同步聚水潭商品完成，共同步{}个商品", totalSynced);
            return true;
            
        } catch (Exception e) {
            logger.error("全量同步聚水潭商品失败", e);
            return false;
        }
    }
    
    /**
     * 解析商品响应数据
     */
    private List<Product> parseProductResponse(String response) {
        List<Product> products = new ArrayList<>();
        
        if (StrUtil.isBlank(response)) {
            logger.error("聚水潭商品API响应为空");
            return products;
        }
        
        try {
            JSONObject jsonResponse = JSONObject.parseObject(response);
            Integer code = jsonResponse.getInteger("code");
            
            if (code != 0) {
                String msg = jsonResponse.getString("msg");
                logger.error("聚水潭商品查询失败: code={}, msg={}", code, msg);
                return products;
            }
            
            JSONObject data = jsonResponse.getJSONObject("data");
            if (data == null) {
                return products;
            }
            
            JSONArray datas = data.getJSONArray("datas");
            if (datas == null || datas.isEmpty()) {
                return products;
            }
            
            for (int i = 0; i < datas.size(); i++) {
                JSONObject item = datas.getJSONObject(i);
                Product product = convertJstItemToProduct(item);
                if (product != null) {
                    products.add(product);
                }
            }
            
        } catch (Exception e) {
            logger.error("解析聚水潭商品响应失败: response={}", response, e);
        }
        
        return products;
    }
    
    /**
     * 将聚水潭商品数据转换为Product对象
     */
    /**
     * 根据聚水潭API文档字段定义，将聚水潭商品数据转换为系统Product对象
     * 参考文档：聚水潭对接api说明.md
     */
    private Product convertJstItemToProduct(JSONObject jstItem) {
        try {
            // 检查输入参数
            if (jstItem == null) {
                logger.warn("聚水潭商品数据为空，跳过转换");
                return null;
            }
            
            // 检查必要的服务依赖
            if (productBrandService == null || productCategoryService == null) {
                logger.error("必要的服务依赖未正确注入，无法转换商品数据");
                return null;
            }
            
            // 检查关键字段 - 根据API文档，i_id是款式编码，必填
            String itemId = jstItem.getString("i_id");
            if (StrUtil.isBlank(itemId)) {
                logger.warn("款式编码(i_id)为空，跳过转换: {}", jstItem.toJSONString());
                return null;
            }
            
            logger.debug("开始转换聚水潭商品: 款式编码={}", itemId);
            
            Product product = new Product();
            
            // 设置商户ID为3（默认值）
            product.setMerId(3);
            
            // ========== 基本信息字段映射 ==========
            // 款式编码 (i_id) - 必填
            product.setJstItemId(itemId);
            
            // 商品名称 (name) - 必填
            String itemName = jstItem.getString("name");
            if (StrUtil.isNotBlank(itemName)) {
                product.setName(itemName);
            } else {
                product.setName("未命名商品_" + itemId);
                logger.warn("商品名称为空，使用默认名称: {}", product.getName());
            }
            
            // 商品描述 - 优先级：remark > short_name > name
            String intro = jstItem.getString("remark");
            if (StrUtil.isBlank(intro)) {
                intro = jstItem.getString("short_name");
            }
            if (StrUtil.isBlank(intro)) {
                intro = itemName;
            }
            if (StrUtil.isNotBlank(intro)) {
                product.setIntro(intro);
            } else {
                product.setIntro(product.getName());
            }
            
            // 商品主图 (pic)
            String pic = jstItem.getString("pic");
            if (StrUtil.isNotBlank(pic)) {
                String ossUrl = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_AL_UPLOAD_URL);
                product.setImage(ossUrl+"/"+pic);
            }
            
            // ========== 价格信息字段映射 ==========
            // 基本售价 (sale_price) -> 系统销售价格
            setSafePrice(jstItem, "sale_price", product::setPrice, "销售价格");
            
            // 成本价 (cost_price) -> 系统成本价格  
            setSafePrice(jstItem, "cost_price", product::setCost, "成本价格");
            
            // 市场价 - 优先级：market_price > other_price_1 > sale_price
            BigDecimal marketPrice = getSafePrice(jstItem, "market_price");
            if (marketPrice == null || marketPrice.compareTo(BigDecimal.ZERO) == 0) {
                marketPrice = getSafePrice(jstItem, "other_price_1");
            }
            if (marketPrice == null || marketPrice.compareTo(BigDecimal.ZERO) == 0) {
                marketPrice = product.getPrice(); // 使用销售价格作为市场价
            }
            product.setOtPrice(marketPrice);
            
            // ========== 分类信息字段映射 ==========
            // 分类处理 - 优先级：category > c_name > item_type > vc_name
            String categoryName = getFirstNonBlank(
                jstItem.getString("category"),
                jstItem.getString("c_name"), 
                jstItem.getString("item_type"),
                jstItem.getString("vc_name"),
                "默认分类"
            );
            
            try {
                Integer categoryId = findOrCreateProductCategory(categoryName);
                if (categoryId != null) {
                    product.setCategoryId(categoryId);
                }
            } catch (Exception e) {
                logger.warn("处理分类信息失败: {}, 错误: {}", categoryName, e.getMessage());
                // 设置默认分类
                try {
                    Integer defaultCategoryId = findOrCreateProductCategory("默认分类");
                    if (defaultCategoryId != null) {
                        product.setCategoryId(defaultCategoryId);
                    }
                } catch (Exception ex) {
                    logger.error("创建默认分类失败: {}", ex.getMessage());
                }
            }
            
            // ========== 品牌信息字段映射 ==========
            // 品牌处理 - 优先级：brand > supplier_name
            String brandName = getFirstNonBlank(
                jstItem.getString("brand"),
                jstItem.getString("supplier_name"),
                "默认品牌"
            );
            
            try {
                Integer brandId = findOrCreateProductBrand(brandName, product.getMerId());
                if (brandId != null) {
                    product.setBrandId(brandId);
                }
                product.setKeyword(brandName);
            } catch (Exception e) {
                logger.warn("处理品牌信息失败: {}, 错误: {}", brandName, e.getMessage());
                // 设置默认品牌
                try {
                    Integer defaultBrandId = findOrCreateProductBrand("默认品牌", product.getMerId());
                    if (defaultBrandId != null) {
                        product.setBrandId(defaultBrandId);
                    }
                } catch (Exception ex) {
                    logger.error("创建默认品牌失败: {}", ex.getMessage());
                }
            }
            
            // ========== 其他属性字段映射 ==========
            // 单位 (unit)
            String unit = jstItem.getString("unit");
            if (StrUtil.isNotBlank(unit)) {
                product.setUnitName(unit);
            }
            
            // 重量 (weight)
            try {
                Object weightObj = jstItem.get("weight");
                if (weightObj != null) {
                    BigDecimal weight = new BigDecimal(weightObj.toString());
                    // 如果系统有重量字段，可以在这里设置
                    // product.setWeight(weight);
                }
            } catch (Exception e) {
                logger.debug("重量字段转换失败: {}", e.getMessage());
            }
            
            // 商品状态 (enabled) - 是否启用，默认值1,可选值:-1=禁用,0=备用,1=启用
            Integer enabled = jstItem.getInteger("enabled");
            if (enabled != null) {
                product.setIsShow(enabled == 1); // 只有enabled=1时才上架
            } else {
                product.setIsShow(true); // 默认上架
            }
            
            // ========== SKU信息处理 ==========
            // 商品编码 (sku_id) - 如果存在说明这是SKU级别的数据
            String skuId = jstItem.getString("sku_id");
            if (StrUtil.isNotBlank(skuId)) {
                product.setJstSkuId(skuId);
            }
            
            // 国际码/条形码 (sku_code)
            String skuCode = jstItem.getString("sku_code");
            if (StrUtil.isNotBlank(skuCode)) {
                // 如果系统有条形码字段，可以在这里设置
                // product.setBarCode(skuCode);
            }
            
            // 颜色及规格 (properties_value)
            String propertiesValue = jstItem.getString("properties_value");
            if (StrUtil.isNotBlank(propertiesValue)) {
                // 可以用作商品规格描述
                // product.setSpecDesc(propertiesValue);
            }
            
            // 检查是否有skus数组来判断是否为多规格商品
            JSONArray skus = jstItem.getJSONArray("skus");
            if (skus != null && !skus.isEmpty()) {
                product.setSpecType(skus.size() > 1);
            } else {
                product.setSpecType(false); // 单规格商品
            }
            
            // ========== 供应商信息 ==========
            // 供应商编码 (supplier_id)
            Integer supplierId = jstItem.getInteger("supplier_id");
            // 供应商名称 (supplier_name) - 已在品牌处理中使用
            // 供应商商品编码 (supplier_sku_id)
            String supplierSkuId = jstItem.getString("supplier_sku_id");
            // 供应商款式编码 (supplier_i_id)
            String supplierItemId = jstItem.getString("supplier_i_id");
            
            // ========== 商品标签 ==========
            String labels = jstItem.getString("labels");
            if (StrUtil.isNotBlank(labels)) {
                // 可以用作商品标签或关键词
                if (StrUtil.isBlank(product.getKeyword())) {
                    product.setKeyword(labels);
                }
            }
            
            // ========== 处理轮播图 ==========
            try {
                processProductImages(jstItem, product);
            } catch (Exception e) {
                logger.warn("处理商品图片失败: {}", e.getMessage());
            }
            
            // ========== 设置默认值 ==========
            setProductDefaults(product);
            
            logger.debug("成功转换聚水潭商品: 款式编码={}, 商品名称={}", itemId, product.getName());
            
            return product;
            
        } catch (Exception e) {
            String itemId = "未知";
            String itemName = "未知";
            try {
                itemId = jstItem.getString("i_id");
                itemName = jstItem.getString("name");
            } catch (Exception ex) {
                // 忽略获取基本信息时的异常
            }
            
            logger.error("转换聚水潭商品数据失败 - 款式编码: {}, 商品名称: {}, 原始数据: {}", 
                        itemId, itemName, jstItem.toJSONString(), e);
            
            return null;
        }
    }
    
    /**
     * 安全设置价格字段
     */
    private void setSafePrice(JSONObject jstItem, String fieldName, java.util.function.Consumer<BigDecimal> setter, String fieldDesc) {
        try {
            Object priceObj = jstItem.get(fieldName);
            if (priceObj != null) {
                BigDecimal price = new BigDecimal(priceObj.toString());
                setter.accept(price);
            } else {
                setter.accept(BigDecimal.ZERO);
            }
        } catch (Exception e) {
            logger.warn("{}转换失败，使用默认值0: {}", fieldDesc, e.getMessage());
            setter.accept(BigDecimal.ZERO);
        }
    }
    
    /**
     * 安全获取价格字段
     */
    private BigDecimal getSafePrice(JSONObject jstItem, String fieldName) {
        try {
            Object priceObj = jstItem.get(fieldName);
            if (priceObj != null) {
                return new BigDecimal(priceObj.toString());
            }
        } catch (Exception e) {
            logger.debug("获取价格字段{}失败: {}", fieldName, e.getMessage());
        }
        return null;
    }
    
    /**
     * 获取第一个非空字符串
     */
    private String getFirstNonBlank(String... values) {
        for (String value : values) {
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }
    
    /**
     * 设置商品默认值
     */
    private void setProductDefaults(Product product) {
        product.setType(0); // 普通商品
        product.setAuditStatus(0); // 无需审核
        product.setIsAudit(false);
        product.setIsDel(false);
        product.setIsRecycle(false);
        product.setIsSub(false);
        
        // 确保specType有值
        if (product.getSpecType() == null) {
            product.setSpecType(false); // 默认单规格
        }
        
        // 确保价格字段有值
        if (product.getPrice() == null) {
            product.setPrice(BigDecimal.ZERO);
        }
        if (product.getCost() == null) {
            product.setCost(BigDecimal.ZERO);
        }
        if (product.getOtPrice() == null) {
            product.setOtPrice(product.getPrice());
        }

        product.setStock(0); // 默认库存为0
        product.setSales(0); // 默认销量为0
        product.setFicti(0); // 默认虚拟销量为0
        product.setBrowse(0); // 默认浏览量为0
        product.setSort(0);
        product.setRanks(0);
        product.setIsPaidMember(false);
        product.setIsAutoUp(false);
        product.setMarketingType(0);
        product.setRefundSwitch(true);
        product.setIsHot(0);
        product.setLimitSwith(false);
        product.setLimitNum(0);
        product.setMinNum(1);
        product.setPostageSwith(false);
        product.setCityDeliverySwith(false);
        
        // 设置创建和更新时间
        Date now = new Date();
        product.setCreateTime(now);
        product.setUpdateTime(now);
    }
    
    /**
     * 保存或更新商品
     */
    private void saveOrUpdateProduct(Product product) {
        try {
            // 检查商品是否已存在（根据聚水潭商品ID）
            Product existingProduct = productService.getByJstItemId(product.getJstItemId());
            
            if (existingProduct != null) {
                // 更新现有商品
                product.setId(existingProduct.getId());
                product.setCreateTime(existingProduct.getCreateTime());
                product.setUpdateTime(new Date());
                
                boolean updateResult = productService.updateById(product);
                if (updateResult) {
                    logger.info("更新聚水潭商品成功: itemId={}, name={}", product.getJstItemId(), product.getName());
                } else {
                    logger.error("更新聚水潭商品失败: itemId={}, name={}", product.getJstItemId(), product.getName());
                }
            } else {
                // 新增商品
                product.setCreateTime(new Date());
                product.setUpdateTime(new Date());
                
                boolean saveResult = productService.save(product);
                if (saveResult) {
                    logger.info("新增聚水潭商品成功: itemId={}, name={}", product.getJstItemId(), product.getName());
                } else {
                    logger.error("新增聚水潭商品失败: itemId={}, name={}", product.getJstItemId(), product.getName());
                }
            }
            
        } catch (Exception e) {
            logger.error("保存聚水潭商品异常: itemId={}, name={}", product.getJstItemId(), product.getName(), e);
        }
    }
    
    /**
     * 保存或更新商品信息
     * @param jstItem 聚水潭商品信息
     * @param merId 商户ID
     * @return 保存结果
     */
    private boolean saveOrUpdateProduct(JSONObject jstItem, Integer merId) {
        try {
            String jstItemId = jstItem.getString("i_id");
            if (StrUtil.isBlank(jstItemId)) {
                logger.warn("聚水潭商品ID为空，跳过处理");
                return false;
            }
            
            // 查找是否已存在该商品
            Product existingProduct = productService.getByJstItemId(jstItemId);
            
            Product product;
            boolean isNewProduct = false;
            if (ObjectUtil.isNotNull(existingProduct)) {
                // 更新现有商品
                product = existingProduct;
                logger.info("更新现有商品，商品ID: {}, 聚水潭商品ID: {}", product.getId(), jstItemId);
            } else {
                // 创建新商品
                product = new Product();
                product.setJstItemId(jstItemId);
                product.setMerId(merId);
                product.setCreateTime(new Date());
                isNewProduct = true;
                logger.info("创建新商品，聚水潭商品ID: {}", jstItemId);
            }
            
            // 转换聚水潭商品信息到系统商品
            Product convertedProduct = convertJstItemToProduct(jstItem);
            if (convertedProduct != null) {
                // 复制转换后的属性到现有商品对象
                product.setName(convertedProduct.getName());
                product.setIntro(convertedProduct.getIntro());
                product.setImage(convertedProduct.getImage());
                product.setPrice(convertedProduct.getPrice());
                product.setCost(convertedProduct.getCost());
                product.setOtPrice(convertedProduct.getOtPrice());
                product.setBrandId(convertedProduct.getBrandId());
                product.setCategoryId(convertedProduct.getCategoryId());
                product.setUnitName(convertedProduct.getUnitName());
                product.setJstSkuId(convertedProduct.getJstSkuId());
                product.setSpecType(convertedProduct.getSpecType());
                product.setSliderImage(convertedProduct.getSliderImage());
                product.setKeyword(convertedProduct.getKeyword());
                product.setUpdateTime(new Date());
            }
            
            // 保存商品
            boolean result;
            if (!isNewProduct) {
                result = productService.updateById(product);
            } else {
                result = productService.save(product);
            }
            
            if (result) {
                // 商品保存成功后，处理SKU信息
                processProductSkus(jstItem, product);
                logger.info("商品保存成功，商品ID: {}, 聚水潭商品ID: {}", product.getId(), jstItemId);
            } else {
                logger.error("商品保存失败，聚水潭商品ID: {}", jstItemId);
            }
            
            return result;
        } catch (Exception e) {
            logger.error("保存商品信息时发生异常", e);
            return false;
        }
    }
    
    /**
      * 查找或创建商品分类
      */
     private Integer findOrCreateProductCategory(String categoryName) {
         try {
             if (StrUtil.isBlank(categoryName)) {
                 return null;
             }
             
             // 查找是否已存在该分类
             LambdaQueryWrapper<ProductCategory> queryWrapper = Wrappers.lambdaQuery();
             queryWrapper.eq(ProductCategory::getName, categoryName);
             ProductCategory category = productCategoryService.getOne(queryWrapper);
             
             if (category != null) {
                 return category.getId();
             }
             
             // 创建新分类
             ProductCategory newCategory = new ProductCategory();
             newCategory.setName(categoryName);
             newCategory.setPid(0); // 默认为顶级分类
             newCategory.setSort(0);
             newCategory.setIsShow(true);
             newCategory.setIsDel(false);
             newCategory.setCreateTime(new Date());
             newCategory.setUpdateTime(new Date());
             
             boolean saveResult = productCategoryService.save(newCategory);
             if (saveResult) {
                 logger.info("创建商品分类成功: {}", categoryName);
                 return newCategory.getId();
             } else {
                 logger.error("创建商品分类失败: {}", categoryName);
                 return null;
             }
             
         } catch (Exception e) {
             logger.error("查找或创建商品分类异常: {}", categoryName, e);
             return null;
         }
     }
    
    /**
      * 查找或创建商品品牌
      */
     private Integer findOrCreateProductBrand(String brandName, Integer merId) {
         try {
             if (StrUtil.isBlank(brandName)) {
                 return null;
             }
             
             // 查找是否已存在该品牌
             LambdaQueryWrapper<ProductBrand> queryWrapper = Wrappers.lambdaQuery();
             queryWrapper.eq(ProductBrand::getName, brandName);
             ProductBrand brand = productBrandService.getOne(queryWrapper);
             
             if (brand != null) {
                 return brand.getId();
             }
             
             // 创建新品牌
             ProductBrand newBrand = new ProductBrand();
             newBrand.setName(brandName);
             newBrand.setApplyMerId(merId); // 使用申请商户ID字段
             newBrand.setSort(0);
             newBrand.setIsShow(true);
             newBrand.setIsDel(false);
             newBrand.setCreateTime(new Date());
             newBrand.setUpdateTime(new Date());
             
             boolean saveResult = productBrandService.save(newBrand);
             if (saveResult) {
                 logger.info("创建商品品牌成功: {}", brandName);
                 return newBrand.getId();
             } else {
                 logger.error("创建商品品牌失败: {}", brandName);
                 return null;
             }
             
         } catch (Exception e) {
             logger.error("查找或创建商品品牌异常: {}", brandName, e);
             return null;
         }
     }
    
    /**
      * 查找或创建商户商品分类
      */
     private String findOrCreateMerchantProductCategory(String categoryName, Integer merId) {
         try {
             if (StrUtil.isBlank(categoryName)) {
                 return null;
             }
             
             // 查找是否已存在该商户分类
             LambdaQueryWrapper<MerchantProductCategory> queryWrapper = Wrappers.lambdaQuery();
             queryWrapper.eq(MerchantProductCategory::getName, categoryName);
             queryWrapper.eq(MerchantProductCategory::getMerId, merId);
             MerchantProductCategory category = merchantProductCategoryService.getOne(queryWrapper);
             
             if (category != null) {
                 return category.getId().toString();
             }
             
             // 创建新商户分类
             MerchantProductCategory newCategory = new MerchantProductCategory();
             newCategory.setName(categoryName);
             newCategory.setMerId(merId);
             newCategory.setPid(0); // 默认为顶级分类
             newCategory.setSort(0);
             newCategory.setIsShow(true);
             newCategory.setIsDel(false);
             newCategory.setCreateTime(new Date());
             newCategory.setUpdateTime(new Date());
             
             boolean saveResult = merchantProductCategoryService.save(newCategory);
             if (saveResult) {
                 logger.info("创建商户商品分类成功: {}", categoryName);
                 return newCategory.getId().toString();
             } else {
                 logger.error("创建商户商品分类失败: {}", categoryName);
                 return null;
             }
             
         } catch (Exception e) {
             logger.error("查找或创建商户商品分类异常: {}", categoryName, e);
             return null;
         }
     }
    
    /**
     * 处理商品图片
     */
    private void processProductImages(JSONObject jstItem, Product product) {
        try {
            String ossUrl = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_AL_UPLOAD_URL);
            // 主图
            String mainPic = jstItem.getString("pic");
            mainPic=ossUrl+"/"+mainPic;
            if (StrUtil.isNotBlank(mainPic)) {
                product.setImage(mainPic);
            }
            
            // 轮播图
            JSONArray pics = jstItem.getJSONArray("pics");
            List<String> sliderImages = new ArrayList<>();
            
            // 如果有轮播图数据，使用轮播图数据
            if (pics != null && !pics.isEmpty()) {
                for (int i = 0; i < pics.size(); i++) {
                    String pic = pics.getString(i);
                    if (StrUtil.isNotBlank(pic)) {
                        sliderImages.add(ossUrl+"/"+pic);
                    }
                }
            } 
            // 否则至少使用主图作为轮播图
            else if (StrUtil.isNotBlank(mainPic)) {
                sliderImages.add(mainPic);
            }
            
            // 设置轮播图
            if (!sliderImages.isEmpty()) {
                product.setSliderImage(JSONArray.toJSONString(sliderImages));
            }
            
        } catch (Exception e) {
            logger.error("处理商品图片异常: {}", jstItem.getString("i_id"), e);
        }
    }
    
    /**
     * 处理商品SKU信息
     */
    private void processProductSkus(JSONObject jstItem, Product product) {
        try {
            JSONArray skus = jstItem.getJSONArray("skus");
            if (skus == null || skus.isEmpty()) {
                // 单规格商品，创建默认SKU
                createDefaultSku(jstItem, product);
                return;
            }
            
            // 多规格商品
            product.setSpecType(true);
            
            // 处理每个SKU
            for (int i = 0; i < skus.size(); i++) {
                JSONObject skuData = skus.getJSONObject(i);
                createProductAttrValue(skuData, product, jstItem);
            }
            
        } catch (Exception e) {
            logger.error("处理商品SKU异常: {}", jstItem.getString("i_id"), e);
        }
    }
    
    /**
     * 创建默认SKU（单规格商品）
     */
    private void createDefaultSku(JSONObject jstItem, Product product) {
        try {
            ProductAttrValue attrValue = new ProductAttrValue();
            attrValue.setProductId(product.getId());
            attrValue.setSku("default");
            attrValue.setAttrValue("{}"); // 空属性值
            
            // 价格信息
            BigDecimal salePrice = jstItem.getBigDecimal("sale_price");
            if (salePrice != null) {
                attrValue.setPrice(salePrice);
                attrValue.setOtPrice(salePrice);
            }
            
            BigDecimal costPrice = jstItem.getBigDecimal("cost_price");
            if (costPrice != null) {
                attrValue.setCost(costPrice);
            }
            
            // 库存信息
            Integer qty = jstItem.getInteger("qty");
            if (qty != null) {
                attrValue.setStock(qty);
            }
            
            // 重量信息
            BigDecimal weight = jstItem.getBigDecimal("weight");
            if (weight != null) {
                attrValue.setWeight(weight);
            }
            
            // 其他默认值
            attrValue.setSales(0);
            attrValue.setType(0);
            attrValue.setQuota(0);
            attrValue.setQuotaShow(0);
            attrValue.setIsDel(false);
            attrValue.setVersion(0);
            attrValue.setIsCallback(false);
            attrValue.setMarketingType(0);
            attrValue.setIsShow(true);
            attrValue.setIsDefault(true);
            
            // 如果商品已保存，则保存SKU
            if (product.getId() != null) {
                productAttrValueService.save(attrValue);
            }
            
        } catch (Exception e) {
            logger.error("创建默认SKU异常: {}", jstItem.getString("i_id"), e);
        }
    }
    
    /**
     * 创建商品属性值（多规格商品）
     */
    private void createProductAttrValue(JSONObject skuData, Product product, JSONObject jstItem) {
        try {
            ProductAttrValue attrValue = new ProductAttrValue();
            attrValue.setProductId(product.getId());
            
            // SKU编码
            String skuId = skuData.getString("sku_id");
            if (StrUtil.isNotBlank(skuId)) {
                attrValue.setSku(skuId);
                // 保存聚水潭SKU ID
                product.setJstSkuId(skuId);
            }
            
            // 属性值（从properties_value字段获取）
            String propertiesValue = skuData.getString("properties_value");
            if (StrUtil.isNotBlank(propertiesValue)) {
                attrValue.setAttrValue(propertiesValue);
            } else {
                attrValue.setAttrValue("{}");
            }
            
            // 价格信息
            BigDecimal salePrice = skuData.getBigDecimal("sale_price");
            if (salePrice != null) {
                attrValue.setPrice(salePrice);
                attrValue.setOtPrice(salePrice);
            }
            
            BigDecimal costPrice = skuData.getBigDecimal("cost_price");
            if (costPrice != null) {
                attrValue.setCost(costPrice);
            }
            
            // 库存信息
            Integer qty = skuData.getInteger("qty");
            if (qty != null) {
                attrValue.setStock(qty);
            }
            
            // 重量信息
            BigDecimal weight = skuData.getBigDecimal("weight");
            if (weight != null) {
                attrValue.setWeight(weight);
            }
            
            // 商品条码
            String barcode = skuData.getString("barcode");
            if (StrUtil.isNotBlank(barcode)) {
                attrValue.setBarCode(barcode);
            }
            
            // 其他默认值
            attrValue.setSales(0);
            attrValue.setType(0);
            attrValue.setQuota(0);
            attrValue.setQuotaShow(0);
            attrValue.setIsDel(false);
            attrValue.setVersion(0);
            attrValue.setIsCallback(false);
            attrValue.setMarketingType(0);
            attrValue.setIsShow(true);
            attrValue.setIsDefault(false);
            
            // 如果商品已保存，则保存SKU
            if (product.getId() != null) {
                productAttrValueService.save(attrValue);
            }
            
        } catch (Exception e) {
            logger.error("创建商品属性值异常: {}", skuData.getString("sku_id"), e);
        }
    }
    
    /**
     * 上传商品到聚水潭
     * @param product 商品对象
     * @return 上传结果
     */
    @Override
    public JustuitanProductUploadResult uploadProductToJst(Product product) {
        try {
            logger.info("开始上传商品到聚水潭，商品ID: {}, 商品名称: {}", product.getId(), product.getName());
            
            // 获取商品规格列表
            List<ProductAttrValue> attrValues = productAttrValueService.getListByProductIdAndType(
                product.getId(), product.getType(), product.getMarketingType(), false);
            
            if (CollUtil.isEmpty(attrValues)) {
                logger.error("商品没有规格信息，无法上传到聚水潭，商品ID: {}", product.getId());
                return new JustuitanProductUploadResult(false, "商品没有规格信息");
            }
            
            // 构建商品上传数据列表
            JSONArray items = new JSONArray();
            for (ProductAttrValue attrValue : attrValues) {
                JSONObject itemData = buildProductUploadData(product, attrValue);
                items.add(itemData);
            }
            
            // 构建业务参数
            JSONObject bizParams = new JSONObject();
            bizParams.put("items", items);

            Map<String, Object> params = JustuitanApiUtil.buildApiParams(justuitanErpConfig.getAuth().getAppKey(), getAccessToken());

            // 将业务参数作为biz参数传递
            params.put("biz", bizParams.toJSONString());
            
            // 生成签名
            String sign = JustuitanApiUtil.generateSign(params, justuitanErpConfig.getAuth().getAppSecret());
            params.put("sign", sign);
            
            // 发送请求
            String url = justuitanErpConfig.getApi().getUrl() + "/open/jushuitan/itemsku/upload";
            String response = restTemplateUtil.postFormData(url, params);
            
            logger.info("聚水潭商品上传响应: {}", response);
            
            // 解析响应
            JustuitanProductUploadResult result = parseUploadResponse(response, attrValues);
            
            // 如果上传成功，更新库存
            if (result.isSuccess()) {
                updateInventoryAfterProductUpload(result, attrValues);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("上传商品到聚水潭失败，商品ID: {}, 错误信息: {}", product.getId(), e.getMessage(), e);
            return new JustuitanProductUploadResult(false, "上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传商品分类到聚水潭
     * @param category 商品分类对象
     * @return 上传结果
     */
    @Override
    public Boolean uploadCategoryToJst(ProductCategory category) {
        try {
            logger.info("开始上传商品分类到聚水潭，分类ID: {}, 分类名称: {}", category.getId(), category.getName());
            
            // 构建分类上传数据
            Map<String, Object> categoryData = buildCategoryUploadData(category);
            
            // 构建业务参数
            JSONObject bizParams = new JSONObject();
            bizParams.putAll(categoryData);
            Map<String, Object> params = JustuitanApiUtil.buildApiParams(justuitanErpConfig.getAuth().getAppKey(), getAccessToken());
            // 将业务参数作为biz参数传递
            params.put("biz", bizParams.toJSONString());
            logger.info("聚水潭分类上传参数: {}", bizParams.toJSONString());

            // 生成签名
            String sign = JustuitanApiUtil.generateSign(params, justuitanErpConfig.getAuth().getAppSecret());
            params.put("sign", sign);
            
            // 发送请求
            String url = justuitanErpConfig.getApi().getUrl() + "/open/webapi/itemapi/category/addorupdate";
            // 使用正确的Content-Type: application/x-www-form-urlencoded;charset=UTF-8
            String response = restTemplateUtil.postFormData(url, params);
            
            logger.info("聚水潭分类上传响应: {}", response);
            
            // 解析响应
            return parseUploadResponseForCategory(response, category);
            
        } catch (Exception e) {
            logger.error("上传商品分类到聚水潭失败，分类ID: {}, 错误信息: {}", category.getId(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Boolean syncInventoryFromJst() {
        try {
            logger.info("开始从聚水潭同步库存信息");
            
            // 获取最近1天的库存数据
            String endTime = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            String startTime = DateUtil.format(DateUtil.offsetDay(new Date(), -1), "yyyy-MM-dd HH:mm:ss");
            
            int pageIndex = 1;
            int pageSize = 100;
            int totalSynced = 0;
            
            while (true) {
                // 调用聚水潭库存查询API
                List<InventoryInfo> inventoryList = queryInventoryFromJst(startTime, endTime, pageIndex, pageSize);
                
                if (CollUtil.isEmpty(inventoryList)) {
                    break;
                }
                
                // 更新本地库存
                for (InventoryInfo inventory : inventoryList) {
                    try {
                        updateLocalInventory(inventory);
                        totalSynced++;
                    } catch (Exception e) {
                        logger.error("更新本地库存失败: {}", inventory.getSkuId(), e);
                    }
                }
                
                // 如果返回的库存数量小于页面大小，说明已经是最后一页
                if (inventoryList.size() < pageSize) {
                    break;
                }
                
                pageIndex++;
            }
            
            logger.info("从聚水潭同步库存信息完成，共同步{}个SKU库存", totalSynced);
            return true;
            
        } catch (Exception e) {
            logger.error("从聚水潭同步库存信息失败", e);
            return false;
        }
    }
    
    /**
     * 调用聚水潭库存查询API
     */
    private List<InventoryInfo> queryInventoryFromJst(String modifiedBegin, String modifiedEnd, Integer pageIndex, Integer pageSize) {
        try {
            // 构建业务参数JSON
            Map<String, Object> bizParams = new HashMap<>();
            bizParams.put("modified_begin", modifiedBegin);
            bizParams.put("modified_end", modifiedEnd);
            bizParams.put("page_index", pageIndex);
            bizParams.put("page_size", pageSize);
            bizParams.put("wms_co_id", 0); // 查询所有仓的总库存
            
            // 构建API请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("app_key", justuitanErpConfig.getAuth().getAppKey());
            
            // 动态获取access_token
            String accessToken = getAccessToken();
            if (StrUtil.isBlank(accessToken)) {
                logger.error("无法获取access_token，库存查询失败");
                return new ArrayList<>();
            }
            params.put("access_token", accessToken);
            
            params.put("timestamp", System.currentTimeMillis() / 1000);
            params.put("charset", "utf-8");
            params.put("version", "2");
            
            // 将业务参数转换为JSON字符串
            String bizJson = JSONObject.toJSONString(bizParams);
            params.put("biz", bizJson);
            
            // 调试日志：打印请求参数
            logger.info("聚水潭库存查询请求参数: {}", params);
            
            // 生成签名（必须在所有参数都设置完成后）
            String sign = JustuitanApiUtil.generateSign(params, justuitanErpConfig.getAuth().getAppSecret());
            params.put("sign", sign);
            
            // 调试日志：打印签名
            logger.info("聚水潭库存查询签名: {}", sign);
            
            // 发送请求
            String url = justuitanErpConfig.getApi().getUrl() + "/open/inventory/query";
            
            // 使用正确的Content-Type: application/x-www-form-urlencoded;charset=UTF-8
            String response = restTemplateUtil.postFormData(url, params);
            
            // 调试日志：打印响应
            logger.info("聚水潭库存查询响应: {}", response);
            
            // 解析响应
            return parseInventoryResponse(response);
            
        } catch (Exception e) {
            logger.error("调用聚水潭库存查询API失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 解析库存查询响应
     */
    private List<InventoryInfo> parseInventoryResponse(String response) {
        List<InventoryInfo> inventoryList = new ArrayList<>();
        
        try {
            JSONObject jsonResponse = JSONObject.parseObject(response);
            Integer code = jsonResponse.getInteger("code");
            
            if (code != null && code == 0) {
                JSONObject data = jsonResponse.getJSONObject("data");
                if (data != null) {
                    JSONArray inventorys = data.getJSONArray("inventorys");
                    if (inventorys != null) {
                        for (int i = 0; i < inventorys.size(); i++) {
                            JSONObject inventoryData = inventorys.getJSONObject(i);
                            InventoryInfo inventory = parseInventoryData(inventoryData);
                            if (inventory != null) {
                                inventoryList.add(inventory);
                            }
                        }
                    }
                }
            } else {
                String msg = jsonResponse.getString("msg");
                logger.error("聚水潭库存查询API返回错误: code={}, msg={}", code, msg);
            }
            
        } catch (Exception e) {
            logger.error("解析聚水潭库存查询响应失败", e);
        }
        
        return inventoryList;
    }
    
    /**
     * 解析单个库存数据
     */
    private InventoryInfo parseInventoryData(JSONObject inventoryData) {
        try {
            InventoryInfo inventory = new InventoryInfo();
            inventory.setSkuId(inventoryData.getString("sku_id"));
            inventory.setItemId(inventoryData.getString("i_id"));
            inventory.setQty(inventoryData.getInteger("qty"));
            inventory.setName(inventoryData.getString("name"));
            inventory.setModified(inventoryData.getString("modified"));
            
            return inventory;
        } catch (Exception e) {
            logger.error("解析库存数据失败", e);
            return null;
        }
    }
    
    /**
     * 更新本地库存
     */
    private void updateLocalInventory(InventoryInfo inventory) {
        try {
            // 根据聚水潭SKU ID查找本地商品
            Product product = productService.getByJstSkuId(inventory.getSkuId());
            if (product == null) {
                // 如果根据SKU ID找不到，尝试根据商品ID查找
                product = productService.getByJstItemId(inventory.getItemId());
            }
            
            if (product != null) {
                // 更新商品库存
                product.setStock(inventory.getQty());
                product.setUpdateTime(new Date());
                productService.updateById(product);
                
                // 更新SKU库存
                updateSkuInventory(product.getId(), inventory.getSkuId(), inventory.getQty());
                
                logger.debug("更新商品库存成功: productId={}, skuId={}, qty={}", 
                    product.getId(), inventory.getSkuId(), inventory.getQty());
            } else {
                logger.warn("未找到对应的本地商品: skuId={}, itemId={}", 
                    inventory.getSkuId(), inventory.getItemId());
            }
            
        } catch (Exception e) {
            logger.error("更新本地库存失败: skuId={}", inventory.getSkuId(), e);
        }
    }
    
    /**
     * 更新SKU库存
     */
    private void updateSkuInventory(Integer productId, String skuId, Integer qty) {
        try {
            // 查找对应的SKU
            ProductAttrValue attrValue = productAttrValueService.getByProductIdAndSku(productId, skuId);
            if (attrValue != null) {
                attrValue.setStock(qty);
                productAttrValueService.updateById(attrValue);
            }
        } catch (Exception e) {
            logger.error("更新SKU库存失败: productId={}, skuId={}", productId, skuId, e);
        }
    }
    
    /**
     * 库存信息类
     */
    /**
     * 构建商品上传数据
     * @param product 商品对象
     * @param attrValue 商品规格对象
     * @return 商品上传数据
     */
    private JSONObject buildProductUploadData(Product product, ProductAttrValue attrValue) {
        JSONObject productData = new JSONObject();
        String ossUrl = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_AL_UPLOAD_URL);
        try {
            // 必填字段 - 根据用户要求：sku_id是商品规格id，i_id是商品id
            productData.put("sku_id", attrValue.getId().toString());
            productData.put("i_id", product.getId().toString());
            productData.put("name", product.getName());
            
            // 价格字段 - 使用规格的价格信息
            if (attrValue.getPrice() != null) {
                productData.put("s_price", attrValue.getPrice());
            }
            if (attrValue.getCost() != null) {
                productData.put("c_price", attrValue.getCost());
            }
            if (attrValue.getOtPrice() != null) {
                productData.put("market_price", attrValue.getOtPrice());
            }
            
            // 库存信息 - 使用规格的库存
            if (attrValue.getStock() != null) {
                productData.put("qty", attrValue.getStock());
            }
            
            // 商品信息
            if (StrUtil.isNotBlank(product.getIntro())) {
                productData.put("remark", product.getIntro());
            }
            if (StrUtil.isNotBlank(product.getImage())) {
                productData.put("pic", ossUrl+"/"+product.getImage());
                productData.put("sku_pic", ossUrl+"/"+product.getImage());
            }
            if (StrUtil.isNotBlank(product.getSliderImage())) {
//                productData.put("pic_big", product.getSliderImage());
                productData.put("pic_big", product.getImage());
            }
            
            // 重量信息 - 使用规格的重量
            if (attrValue.getWeight() != null) {
                productData.put("weight", attrValue.getWeight());
            }
            
            if (StrUtil.isNotBlank(product.getUnitName())) {
                productData.put("unit", product.getUnitName());
            }
            
            // 启用状态
            productData.put("enabled", product.getIsShow() ? 1 : -1);
            
            // 商品类型
            productData.put("item_type", "成品");
            
            // 分类信息 - 必须是商品类目管理中的叶子节点
            if (product.getCategoryId() != null) {
                ProductCategory category = productCategoryService.getById(product.getCategoryId());
                if (category != null) {
                    productData.put("c_name", category.getName());
                }
            }
            
            // 品牌信息
            if (product.getBrandId() != null) {
                ProductBrand brand = productBrandService.getById(product.getBrandId());
                if (brand != null) {
                    productData.put("brand", brand.getName());
                }
            }
            
            // 商品编码 - 使用规格的SKU编码
            if (StrUtil.isNotBlank(attrValue.getSku())) {
                productData.put("sku_code", attrValue.getSku());
            }
            
            // 条码信息
            if (StrUtil.isNotBlank(attrValue.getBarCode())) {
                productData.put("barcode", attrValue.getBarCode());
            }
            
            // 规格信息 - 颜色及规格（从attrValue字段获取）
            if (StrUtil.isNotBlank(attrValue.getAttrValue()) && !"{}".equals(attrValue.getAttrValue())) {
                try {
                    JSONObject attrJson = JSONObject.parseObject(attrValue.getAttrValue());
                    if (attrJson != null && !attrJson.isEmpty()) {
                        String propertiesValue = attrJson.values().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(";"));
                        if (StrUtil.isNotBlank(propertiesValue) && propertiesValue.length() <= 100) {
                            productData.put("properties_value", propertiesValue);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("解析商品规格属性失败，商品ID: {}, 规格ID: {}", product.getId(), attrValue.getId());
                }
            }
            
            // 简称 - 如果商品名称过长，截取前10个字符作为简称
            if (StrUtil.isNotBlank(product.getName()) && product.getName().length() > 10) {
                productData.put("short_name", product.getName().substring(0, 10));
            }
            
            // 库存同步设置
            productData.put("stock_disabled", false);
            
            logger.info("构建商品上传数据完成，商品ID: {}, 规格ID: {}, 数据: {}", product.getId(), attrValue.getId(), productData.toJSONString());
            
        } catch (Exception e) {
            logger.error("构建商品上传数据失败，商品ID: {}, 规格ID: {}, 错误信息: {}", product.getId(), attrValue.getId(), e.getMessage(), e);
        }
        
        return productData;
    }
    
    /**
     * 查询聚水潭分类信息
     * @param categoryId 聚水潭分类ID
     * @return 分类信息
     */
    private JSONObject queryCategoryFromJst(String categoryId) {
        try {
            // 构建业务参数
            JSONObject bizParams = new JSONObject();
            JSONArray cIds = new JSONArray();
            cIds.add(categoryId);
            bizParams.put("c_ids", cIds);
            bizParams.put("page_index", 1);
            bizParams.put("page_size", 1);
            
            // 构建API请求参数
            Map<String, Object> params = JustuitanApiUtil.buildApiParams(justuitanErpConfig.getAuth().getAppKey(),getAccessToken());
            if (params == null) {
                logger.error("无法构建API请求参数，分类查询失败");
                return null;
            }
            
            // 将业务参数作为biz参数传递
            params.put("biz", bizParams.toJSONString());
            
            // 生成签名
            String sign = JustuitanApiUtil.generateSign(params, justuitanErpConfig.getAuth().getAppSecret());
            params.put("sign", sign);
            
            // 发送请求
            String url = justuitanErpConfig.getApi().getUrl() + "/open/category/query";
            String response = restTemplateUtil.postFormData(url, params);
            
            logger.debug("聚水潭分类查询响应: {}", response);
            
            // 解析响应
            if (StrUtil.isNotBlank(response)) {
                JSONObject responseObj = JSONObject.parseObject(response);
                Integer code = responseObj.getInteger("code");
                if (code != null && code == 0) {
                    JSONObject data = responseObj.getJSONObject("data");
                    if (data != null) {
                        JSONArray datas = data.getJSONArray("datas");
                        if (datas != null && !datas.isEmpty()) {
                            return datas.getJSONObject(0);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("查询聚水潭分类失败，分类ID: {}, 错误信息: {}", categoryId, e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * 构建分类上传数据
     * @param category 分类对象
     * @return 分类上传数据
     */
    private Map<String, Object> buildCategoryUploadData(ProductCategory category) {
        Map<String, Object> categoryData = new HashMap<>();
        
        try {
            // 必填字段
            categoryData.put("c_name", category.getName());
            
            // 父级分类ID处理
            Integer parentCId = 0; // 默认为顶级分类
            if (category.getPid() != null && category.getPid() > 0) {
                // 根据父级分类的本地ID查找父级分类
                ProductCategory parentCategory = productCategoryService.getById(category.getPid());
                if (parentCategory != null && StrUtil.isNotBlank(parentCategory.getJstCategoryId())) {
                    // 验证父级分类在聚水潭中是否存在
                    JSONObject parentJstCategory = queryCategoryFromJst(parentCategory.getJstCategoryId());
                    if (parentJstCategory != null) {
                        parentCId = Integer.valueOf(parentCategory.getJstCategoryId());
                        logger.info("找到有效的父级分类，本地ID: {}, 聚水潭ID: {}", category.getPid(), parentCId);
                    } else {
                        logger.warn("父级分类在聚水潭中不存在，本地ID: {}, 聚水潭ID: {}, 将使用顶级分类", 
                                category.getPid(), parentCategory.getJstCategoryId());
                    }
                } else {
                    logger.warn("父级分类没有聚水潭ID，本地ID: {}, 将使用顶级分类", category.getPid());
                }
            }
            categoryData.put("parent_c_id", parentCId);
            
            // 分类ID（更新时需要）
            if (StrUtil.isNotBlank(category.getJstCategoryId())) {
                categoryData.put("c_id", Integer.valueOf(category.getJstCategoryId()));
            }
            
            // 排序
            if (category.getSort() != null) {
                categoryData.put("sort", category.getSort());
            } else {
                categoryData.put("sort", 1);
            }
            
            // 是否启用
            categoryData.put("enable", category.getIsShow());
            
            // 是否添加PV
            categoryData.put("is_pv", false);
            categoryData.put("pv_names", new String[]{});
            
            logger.info("构建分类上传数据完成，分类ID: {}, 数据: {}", category.getId(), categoryData);
            
        } catch (Exception e) {
            logger.error("构建分类上传数据失败，分类ID: {}, 错误信息: {}", category.getId(), e.getMessage(), e);
        }
        
        return categoryData;
    }
    
    /**
     * 解析上传响应
     * @param response 响应字符串
     * @return 是否成功
     */
    private JustuitanProductUploadResult parseUploadResponse(String response, List<ProductAttrValue> attrValues) {
        try {
            if (StrUtil.isBlank(response)) {
                logger.error("聚水潭上传响应为空");
                return new JustuitanProductUploadResult(false, "聚水潭上传响应为空");
            }
            
            JSONObject responseObj = JSONObject.parseObject(response);
            Integer code = responseObj.getInteger("code");
            String msg = responseObj.getString("msg");

            JustuitanProductUploadResult result = new JustuitanProductUploadResult();
            
            if (code != null && code == 0) {
                logger.info("聚水潭上传成功: {}", msg);
                result.setSuccess(true);
                result.setMessage(msg != null ? msg : "上传成功");
                
                // 解析data中的datas数组，获取每个SKU的上传结果
                JSONObject data = responseObj.getJSONObject("data");
                if (data != null) {
                    JSONArray datas = data.getJSONArray("datas");
                    if (datas != null && !datas.isEmpty()) {
                        for (int i = 0; i < datas.size() && i < attrValues.size(); i++) {
                            JSONObject skuResult = datas.getJSONObject(i);
                            ProductAttrValue attrValue = attrValues.get(i);
                            
                            Boolean isSuccess = skuResult.getBoolean("is_success");
                            String skuId = skuResult.getString("sku_id");
                            String skuMsg = skuResult.getString("msg");

                            JustuitanSkuUploadResult skuUploadResult = new JustuitanSkuUploadResult(
                                isSuccess != null ? isSuccess : false,
                                skuId,
                                null, // itemId在这个响应中可能没有
                                skuMsg,
                                attrValue.getId()
                            );
                            
                            result.getSkuResults().add(skuUploadResult);
                        }
                    }
                }
                
            } else {
                logger.error("聚水潭上传失败，错误码: {}, 错误信息: {}", code, msg);
                result.setSuccess(false);
                result.setMessage(msg != null ? msg : "上传失败");
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("解析聚水潭上传响应失败，响应: {}, 错误信息: {}", response, e.getMessage(), e);
            return new JustuitanProductUploadResult(false, "解析响应失败: " + e.getMessage());
        }
    }

    /**
     * 解析分类上传响应
     * @param response 响应字符串
     * @return 是否成功
     */
    /**
     * 商品上传成功后更新库存
     * @param result 商品上传结果
     * @param attrValues 商品规格列表
     */
    private void updateInventoryAfterProductUpload(JustuitanProductUploadResult result, List<ProductAttrValue> attrValues) {
        try {
            logger.info("开始更新商品上传成功后的库存信息");
            
            List<JustuitanSkuUploadResult> skuResults = result.getSkuResults();
            if (CollUtil.isEmpty(skuResults)) {
                logger.warn("没有SKU上传结果，跳过库存更新");
                return;
            }
            
            for (JustuitanSkuUploadResult skuResult : skuResults) {
                if (skuResult.isSuccess() && StrUtil.isNotBlank(skuResult.getSkuId())) {
                    try {
                        // 更新本地数据库中的jst_sku_id
                        ProductAttrValue attrValue = attrValues.stream()
                            .filter(attr -> attr.getId().equals(skuResult.getLocalSkuId()))
                            .findFirst()
                            .orElse(null);
                        
                        if (attrValue != null) {
                            // 更新jst_sku_id
                            attrValue.setJstSkuId(skuResult.getSkuId());
                            productAttrValueService.updateById(attrValue);
                            
                            // 调用库存更新接口
                          //  updateInventoryForSku(attrValue);
                            syncInventory(attrValue.getProductId(), attrValue.getId(),attrValue.getStock(),"add");
                            
                            logger.info("成功更新SKU库存，本地SKU ID: {}, 聚水潭SKU ID: {}", 
                                attrValue.getId(), skuResult.getSkuId());
                        } else {
                            logger.warn("未找到对应的本地SKU，本地SKU ID: {}", skuResult.getLocalSkuId());
                        }
                    } catch (Exception e) {
                        logger.error("更新SKU库存失败，本地SKU ID: {}, 聚水潭SKU ID: {}, 错误: {}", 
                            skuResult.getLocalSkuId(), skuResult.getSkuId(), e.getMessage(), e);
                    }
                } else {
                    logger.warn("SKU上传失败或缺少聚水潭SKU ID，跳过库存更新，本地SKU ID: {}, 消息: {}", 
                        skuResult.getLocalSkuId(), skuResult.getMessage());
                }
            }
            
        } catch (Exception e) {
            logger.error("商品上传成功后更新库存失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 更新单个SKU的库存
     * @param attrValue 商品规格
     */
    private void updateInventoryForSku(ProductAttrValue attrValue) {
        try {
            if (StrUtil.isBlank(attrValue.getJstSkuId())) {
                logger.warn("SKU缺少聚水潭SKU ID，无法更新库存，本地SKU ID: {}", attrValue.getId());
                return;
            }
            
            // 构建库存更新参数
            JSONObject bizParams = new JSONObject();
            JSONArray items = new JSONArray();
            
            JSONObject item = new JSONObject();
            item.put("sku_id", attrValue.getJstSkuId());
            item.put("qty", attrValue.getStock() != null ? attrValue.getStock() : 0);
            items.add(item);
            
            bizParams.put("items", items);
            
            // 构建API请求参数
            Map<String, Object> params = JustuitanApiUtil.buildApiParams(justuitanErpConfig.getAuth().getAppKey(), getAccessToken());

            // 将业务参数作为biz参数传递
            params.put("biz", bizParams.toJSONString());
            
            // 生成签名
            String sign = JustuitanApiUtil.generateSign(params, justuitanErpConfig.getAuth().getAppSecret());
            params.put("sign", sign);
            
            // 发送请求
            String url = justuitanErpConfig.getApi().getUrl() + "/open/webapi/stockapi/inventory/update";
            String response = restTemplateUtil.postFormData(url, params);
            
            logger.info("库存更新请求: {}, 响应: {}", bizParams.toJSONString(), response);
            
            // 解析响应
            if (StrUtil.isNotBlank(response)) {
                JSONObject responseObj = JSONObject.parseObject(response);
                Integer code = responseObj.getInteger("code");
                String msg = responseObj.getString("msg");
                
                if (code != null && code == 0) {
                    logger.info("库存更新成功，SKU ID: {}, 库存: {}", attrValue.getJstSkuId(), attrValue.getStock());
                } else {
                    logger.error("库存更新失败，SKU ID: {}, 错误码: {}, 错误信息: {}", 
                        attrValue.getJstSkuId(), code, msg);
                }
            }
            
        } catch (Exception e) {
            logger.error("更新SKU库存失败，本地SKU ID: {}, 聚水潭SKU ID: {}, 错误: {}", 
                attrValue.getId(), attrValue.getJstSkuId(), e.getMessage(), e);
        }
    }
    
    /**
     * 更新指定分类下所有商品的库存信息
     * @param categoryId 分类ID
     */
    private void updateInventoryForCategoryProducts(Integer categoryId) {
        try {
            // 查询该分类下的所有商品
            List<Product> products = getProductsByCategoryId(categoryId);
            
            if (CollUtil.isEmpty(products)) {
                logger.info("分类ID: {} 下没有找到商品，跳过库存更新", categoryId);
                return;
            }
            
            logger.info("找到分类ID: {} 下的商品数量: {}", categoryId, products.size());
            
            // 为每个商品更新库存
            for (Product product : products) {
                try {
                    // 获取商品的SKU信息
                    List<ProductAttrValue> attrValues = productAttrValueService.getListByProductIdAndType(product.getId(), product.getType(), ProductConstants.PRODUCT_MARKETING_TYPE_BASE, false);
                    
                    if (CollUtil.isNotEmpty(attrValues)) {
                        // 有SKU的商品，更新每个SKU的库存
                        for (ProductAttrValue attrValue : attrValues) {
                            if (StrUtil.isNotBlank(attrValue.getJstSkuId())) {
                                callInventoryUpdateApi(product.getId(), attrValue.getId(), attrValue.getStock(), "update");
                            }
                        }
                    } else {
                        // 没有SKU的商品，更新商品本身的库存
                        if (StrUtil.isNotBlank(product.getJstItemId())) {
                            callInventoryUpdateApi(product.getId(), null, product.getStock(), "update");
                        }
                    }
                } catch (Exception e) {
                    logger.error("更新商品库存失败，商品ID: {}, 错误信息: {}", product.getId(), e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            logger.error("更新分类下商品库存失败，分类ID: {}, 错误信息: {}", categoryId, e.getMessage(), e);
        }
    }
    
    /**
     * 根据分类ID查询商品列表
     * @param categoryId 分类ID
     * @return 商品列表
     */
    private List<Product> getProductsByCategoryId(Integer categoryId) {
        LambdaQueryWrapper<Product> lqw = Wrappers.lambdaQuery();
        lqw.eq(Product::getCategoryId, categoryId);
        lqw.eq(Product::getIsDel, false);
        lqw.eq(Product::getIsShow, true);
        return productService.list(lqw);
    }
    
    /**
     * 调用库存更新API
     * @param productId 商品ID
     * @param skuId SKU ID（可为空）
     * @param quantity 库存数量
     * @param operation 操作类型
     */
    private void callInventoryUpdateApi(Integer productId, Integer skuId, Integer quantity, String operation) {
        try {
            // 构建业务参数
            JSONObject bizParams = new JSONObject();
            bizParams.put("so_id", ""); // 单据编号，可为空
            bizParams.put("warehouse", "主仓"); // 仓库名称
            bizParams.put("type", operation); // 操作类型
            bizParams.put("is_confirm", true); // 是否确认
            
            // 构建库存项目
            JSONArray items = new JSONArray();
            JSONObject item = new JSONObject();
            
            if (skuId != null) {
                // 有SKU的情况
                ProductAttrValue attrValue = productAttrValueService.getById(skuId);
                if (attrValue != null && StrUtil.isNotBlank(attrValue.getJstSkuId())) {
                    item.put("sku_id", attrValue.getJstSkuId());
                    item.put("qty", quantity);
                    items.add(item);
                }
            } else {
                // 没有SKU的情况
                Product product = productService.getById(productId);
                if (product != null && StrUtil.isNotBlank(product.getJstItemId())) {
                    item.put("sku_id", product.getJstItemId());
                    item.put("qty", quantity);
                    items.add(item);
                }
            }
            
            if (items.isEmpty()) {
                logger.warn("商品ID: {}, SKU ID: {} 没有聚水潭ID，跳过库存更新", productId, skuId);
                return;
            }
            
            bizParams.put("items", items);
            
            // 构建请求参数
            Map<String, Object> params = JustuitanApiUtil.buildApiParams(justuitanErpConfig.getAuth().getAppKey(),getAccessToken());
            params.put("biz", bizParams.toJSONString());
            
            // 生成签名
            String sign = JustuitanApiUtil.generateSign(params, justuitanErpConfig.getAuth().getAppSecret());
            params.put("sign", sign);
            
            // 发送请求
            String url = justuitanErpConfig.getApi().getUrl() + "/open/webapi/stockapi/inventory/update";
            String response = restTemplateUtil.postFormData(url, params);
            
            logger.info("库存更新响应，商品ID: {}, SKU ID: {}, 响应: {}", productId, skuId, response);
            
        } catch (Exception e) {
            logger.error("调用库存更新API失败，商品ID: {}, SKU ID: {}, 错误信息: {}", productId, skuId, e.getMessage(), e);
        }
    }

    private Boolean parseUploadResponseForCategory(String response,ProductCategory category) {
        try {
            if (StrUtil.isBlank(response)) {
                logger.error("聚水潭分类上传响应为空");
                return false;
            }
            
            JSONObject responseObj = JSONObject.parseObject(response);
            Integer code = responseObj.getInteger("code");
            String msg = responseObj.getString("msg");
            
            if (code != null && code == 0) {
                logger.info("聚水潭分类上传成功: {}", msg);
                //需要更新商品分类中的JstCategoryId字段
                productCategoryService.updateJstCategoryId(JSONObject.parseObject(responseObj.getString("data")),category);
                return true;
            } else {
                logger.error("聚水潭分类上传失败，错误码: {}, 错误信息: {}", code, msg);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("解析聚水潭分类上传响应失败，响应: {}, 错误信息: {}", response, e.getMessage(), e);
            return false;
        }
    }
    @Override
    public JustuitanOrderUploadResult uploadOrderToJst(Order order) {
        // 检查订单所属商户是否为自营店
        Boolean selfOperatedStore = isSelfOperatedStore(order.getMerId());
        if (selfOperatedStore) {

            try {
                logger.info("开始上传订单到聚水潭，订单号: {}", order.getOrderNo());

                // 构建订单上传数据
                JSONObject orderData = buildOrderUploadData(order);
                if (orderData == null) {
                    return new JustuitanOrderUploadResult(false, "构建订单数据失败");
                }

                // 构建API请求参数
                Map<String, Object> params = JustuitanApiUtil.buildApiParams(justuitanErpConfig.getAuth().getAppKey(), getAccessToken());
                if (params == null) {
                    logger.error("无法构建API请求参数，订单上传失败");
                    return new JustuitanOrderUploadResult(false, "构建API请求参数失败");
                }

                // 构建业务参数 - 订单上传数据（直接使用数组格式）
                JSONArray bizParams = new JSONArray();
                bizParams.add(orderData);

                // 将业务参数作为biz参数传递（直接传递数组）
                params.put("biz", bizParams.toJSONString());
                logger.info("订单上传数据: {}", bizParams.toJSONString());

                // 生成签名
                String sign = JustuitanApiUtil.generateSign(params, justuitanErpConfig.getAuth().getAppSecret());
                params.put("sign", sign);

                // 调用聚水潭API
                String response = restTemplateUtil.postFormData(
                        justuitanErpConfig.getApi().getUrl() + "/open/jushuitan/orders/upload",
                        params
                );

                // 解析响应结果
                return parseOrderUploadResponse(response, order.getOrderNo());

            } catch (Exception e) {
                logger.error("上传订单到聚水潭异常，订单号: {}", order.getOrderNo(), e);
                return new JustuitanOrderUploadResult(false, "上传订单异常: " + e.getMessage());
            }
        }
        return new JustuitanOrderUploadResult(false, "非自营店上传 " );
    }
    
    @Override
    public Boolean shipOrderToJst(String orderNo, String logisticsCompany, String logisticsNo) {
        try {
            logger.info("开始发货订单到聚水潭，订单号: {}, 物流公司: {}, 物流单号: {}", orderNo, logisticsCompany, logisticsNo);
            
            // 查找本地订单获取必要信息
            Order localOrder = orderService.getByOrderNo(orderNo);
            if (localOrder == null) {
                logger.warn("本地订单不存在: {}", orderNo);
                return false;
            }

//            // 检查订单所属商户是否为自营店
//            if (!isSelfOperatedStore(localOrder.getMerId())) {
//                logger.debug("订单不属于自营店，跳过发货到聚水潭: orderNo={}, merId={}", orderNo, localOrder.getMerId());
//                return true;
//            }
            
            // 获取聚水潭店铺ID
        //    String shopId = getJstShopId(localOrder.getMerId());
//            if (StrUtil.isBlank(shopId)) {
//                logger.warn("未找到商户对应的聚水潭店铺ID: merId={}", localOrder.getMerId());
//                return false;
//            }
            
            // 构建发货数据
            JSONObject shipData = new JSONObject();
            shipData.put("shop_id", 18743335);
            shipData.put("so_id", orderNo); // 线上订单号
            shipData.put("lc_name", logisticsCompany); // 快递公司名称
            shipData.put("l_id", logisticsNo); // 快递单号
            
            // 获取快递公司编码
            String lcId = getLogisticsCode(logisticsCompany);
            if (StrUtil.isNotBlank(lcId)) {
                shipData.put("lc_id", lcId);
            }
            
            // 获取聚水潭内部订单号（如果有的话）
            String jstOrderId = getJstOrderId(orderNo);
            if (StrUtil.isNotBlank(jstOrderId)) {
                shipData.put("o_id", Integer.parseInt(jstOrderId));
            }
            
            // 构建API请求参数
            Map<String, Object> params = JustuitanApiUtil.buildApiParams(justuitanErpConfig.getAuth().getAppKey(), getAccessToken());
            
            // 构建items数组
            JSONArray items = new JSONArray();
            items.add(shipData);
            
            // 构建业务参数
            JSONObject bizParams = new JSONObject();
            bizParams.put("items", items);
            
            // 将业务参数作为biz参数传递
            params.put("biz", bizParams.toJSONString());
            
            // 生成签名
            String sign = JustuitanApiUtil.generateSign(params, justuitanErpConfig.getAuth().getAppSecret());
            params.put("sign", sign);
            
            logger.info("调用聚水潭发货接口: orderNo={}, data={}", orderNo, shipData.toJSONString());
            
            // 调用聚水潭API
            String response = restTemplateUtil.postFormData(
                justuitanErpConfig.getApi().getUrl() + "/open/order/sent/upload",
                params
            );
            
            // 解析响应结果
            return parseShipmentResponse(response, orderNo);
            
        } catch (Exception e) {
            logger.error("发货订单到聚水潭异常，订单号: {}", orderNo, e);
            return false;
        }
    }
    
    @Override
    public JustuitanOrderSplitResult splitOrderInJst(String orderNo, List<JustuitanOrderSplitInfo> splitInfos) {
        try {
            logger.info("开始拆分订单，订单号: {}, 拆分信息数量: {}", orderNo, splitInfos.size());
            
            // 获取订单信息
            Order order = orderService.getByOrderNo(orderNo);
            if (order == null) {
                return new JustuitanOrderSplitResult(false, "订单不存在");
            }
            
            // 构建拆分数据
            JSONArray splitArray = new JSONArray();
            for (JustuitanOrderSplitInfo splitInfo : splitInfos) {
                JSONObject splitItem = new JSONObject();
                splitItem.put("oi_id", splitInfo.getOrderDetailId());
                splitItem.put("qty", splitInfo.getSplitQuantity());
                splitArray.add(splitItem);
            }
            
            JSONObject splitData = new JSONObject();
            splitData.put("o_id", order.getId()); // 使用内部订单ID
            splitData.put("split_infos", splitArray);
            
            // 构建API请求参数
            Map<String, Object> params = JustuitanApiUtil.buildApiParams(justuitanErpConfig.getAuth().getAppKey(),getAccessToken());
            params.put("data", splitData.toJSONString());
            
            // 调用聚水潭API
            String response = restTemplateUtil.postFormData(
                justuitanErpConfig.getApi().getUrl() + "/open/jushuitan/drporder/split",
                params
            );
            
            // 解析响应结果
            return parseOrderSplitResponse(response);
            
        } catch (Exception e) {
            logger.error("拆分订单异常，订单号: {}", orderNo, e);
            return new JustuitanOrderSplitResult(false, "拆分订单异常: " + e.getMessage());
        }
    }
    
    /**
     * 构建订单上传数据
     */
    private JSONObject buildOrderUploadData(Order order) {
        try {
            JSONObject orderData = new JSONObject();
            
            // 基础订单信息
            orderData.put("shop_id", 18743335); // 店铺ID，使用整数类型
            orderData.put("so_id", order.getOrderNo()); // 外部订单号
            orderData.put("outer_so_id",order.getOrderNo());
            orderData.put("order_date", DateUtil.format(order.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            orderData.put("shop_buyer_id", order.getUid().toString()); // 买家ID
            orderData.put("shop_modified", DateUtil.format(order.getUpdateTime() != null ? order.getUpdateTime() : order.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            
            // 订单状态映射
            String shopStatus = mapOrderStatus(order.getStatus());
            orderData.put("shop_status", shopStatus);
            
            // 金额信息
            orderData.put("pay_amount", order.getPayPrice().doubleValue()); // 实付金额
            orderData.put("freight", order.getPayPostage() != null ? order.getPayPostage().doubleValue() : 0); // 运费
            
            // 收货人信息
            UserAddress address = getUserAddressByOrder(order.getUid());
            if (address != null) {
                orderData.put("receiver_name", address.getRealName());
                orderData.put("receiver_phone", address.getPhone());
                orderData.put("receiver_state", address.getProvince());
                orderData.put("receiver_city", address.getCity());
                orderData.put("receiver_district", address.getDistrict());
                orderData.put("receiver_address", address.getDetail());
                orderData.put("receiver_zip", address.getPostCode() != null ? address.getPostCode().toString() : "");
            }
            
            // 备注信息（从MerchantOrder表获取）
            String userRemark = ""; // 用户备注（买家留言）
            String merchantRemark = ""; // 商家备注
            try {
                MerchantOrder merchantOrder = merchantOrderService.getOneByOrderNo(order.getOrderNo());
                if (merchantOrder != null) {
                    if (StrUtil.isNotBlank(merchantOrder.getUserRemark())) {
                        userRemark = merchantOrder.getUserRemark();
                    }
                    if (StrUtil.isNotBlank(merchantOrder.getMerchantRemark())) {
                        merchantRemark = merchantOrder.getMerchantRemark();
                    }
                }
            } catch (Exception e) {
                logger.warn("获取订单备注失败，订单号: {}", order.getOrderNo());
            }
            orderData.put("buyer_message", userRemark); // 买家留言使用用户备注
            orderData.put("remark", merchantRemark); // 卖家备注使用商家备注
            
            // 物流信息（可选）
            orderData.put("l_id", ""); // 物流单号
            orderData.put("logistics_company", ""); // 物流公司
            orderData.put("lc_id", ""); // 物流公司编码
            orderData.put("labels", ""); // 标签
            
            // 订单商品信息
            List<OrderDetail> orderDetails = orderDetailService.getByOrderNo(order.getOrderNo());
            JSONArray itemsArray = new JSONArray();
            
            for (OrderDetail detail : orderDetails) {
                JSONObject item = new JSONObject();
                
                // 获取商品和规格信息
                ProductAttrValue attrValue = productAttrValueService.getById(detail.getAttrValueId());
                Product product = productService.getById(detail.getProductId());
                
                // 必需字段 - sku_id（聚水潭必填，优先使用聚水潭SKU ID，否则使用本地SKU或商品ID）
                String skuId = null;
                if (attrValue != null && StrUtil.isNotBlank(attrValue.getJstSkuId())) {
                    skuId = attrValue.getJstSkuId(); // 优先使用聚水潭SKU ID
                } else if (StrUtil.isNotBlank(detail.getSku())) {
                    skuId = detail.getSku(); // 使用本地SKU编码
                } else {
                    skuId = detail.getProductId() + "_" + detail.getAttrValueId(); // 使用商品ID+规格ID组合
                }
                item.put("sku_id", skuId); // 聚水潭SKU ID（必填）
                
                item.put("shop_sku_id", detail.getSku()); // 商家SKU编码
                item.put("name", detail.getProductName()); // 商品名称
                item.put("base_price", detail.getPrice().doubleValue()); // 商品单价
                item.put("amount", detail.getPayPrice().doubleValue()); // 商品总价
                item.put("qty", detail.getPayNum()); // 购买数量
                item.put("outer_oi_id", detail.getId().toString()); // 外部订单明细ID
                
                // 可选字段
                if (product != null && StrUtil.isNotBlank(product.getJstItemId())) {
                    item.put("i_id", product.getJstItemId()); // 聚水潭商品ID
                }
                item.put("shop_i_id", detail.getProductId().toString()); // 商家商品ID
                
                itemsArray.add(item);
            }
            
            orderData.put("items", itemsArray);
            
            // 支付信息（仅对已支付订单）
            if (order.getPaid() != null && order.getPaid() && order.getPayTime() != null) {
                JSONObject payInfo = new JSONObject();
                payInfo.put("outer_pay_id", order.getOrderNo()); // 外部支付单号
                payInfo.put("pay_date", DateUtil.format(order.getPayTime(), "yyyy-MM-dd HH:mm:ss"));
                payInfo.put("payment", getPaymentMethod(order.getPayType())); // 支付方式
                payInfo.put("seller_account", "system"); // 卖家账户
                payInfo.put("buyer_account", order.getUid().toString()); // 买家账户
                payInfo.put("amount", order.getPayPrice().doubleValue()); // 支付金额
                orderData.put("pay", payInfo);
            }
            
            return orderData;
            
        } catch (Exception e) {
            logger.error("构建订单上传数据异常，订单号: {}", order.getOrderNo(), e);
            return null;
        }
    }
    
    /**
     * 映射订单状态
     */
    private String mapOrderStatus(Integer status) {
        if (status == null) {
            return "WAIT_BUYER_PAY";
        }
        
        switch (status) {
            case 0: // 待付款
                return "WAIT_BUYER_PAY";
            case 1: // 待发货
                return "WAIT_SELLER_SEND_GOODS";
            case 2: // 待收货
            case 4: // 已发货
                return "WAIT_BUYER_CONFIRM_GOODS";
            case 3: // 已完成
            case 5: // 用户确认收货
                return "TRADE_FINISHED";
            case 9: // 已取消
                return "TRADE_CLOSED";
            default:
                return "WAIT_SELLER_SEND_GOODS";
        }
    }
    
    /**
     * 获取支付方式
     */
    private String getPaymentMethod(String payType) {
        if (StrUtil.isBlank(payType)) {
            return "other";
        }
        
        switch (payType.toLowerCase()) {
            case "weixin":
            case "wechat":
                return "weixin";
            case "alipay":
                return "alipay";
            case "yue":
            case "balance":
                return "balance";
            default:
                return "other";
        }
    }
    
    /**
     * 获取订单收货地址
     */
    private UserAddress getUserAddressByOrder(Integer uid) {
        try {
            LambdaQueryWrapper<UserAddress> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(UserAddress::getUid, uid);
            wrapper.eq(UserAddress::getIsDel, false);
            wrapper.last("limit 1");
            return userAddressService.getOne(wrapper);
        } catch (Exception e) {
            logger.error("获取用户地址异常，用户ID: {}", uid, e);
            return null;
        }
    }
    
    /**
     * 解析订单上传响应
     */
    private JustuitanOrderUploadResult parseOrderUploadResponse(String response, String orderNo) {
        try {
            JSONObject jsonResponse = JSONObject.parseObject(response);
            Integer code = jsonResponse.getInteger("code");
            String message = jsonResponse.getString("msg");
            
            if (code != null && code == 0) {
                // 上传成功
                JSONObject data = jsonResponse.getJSONObject("data");
                JustuitanOrderUploadResult result = new JustuitanOrderUploadResult(true, "订单上传成功");
                result.setOrderNo(orderNo);
                
                if (data != null) {
                    JSONArray datas = data.getJSONArray("datas");
                    if (datas != null && !datas.isEmpty()) {
                        JSONObject jsonObject = datas.getJSONObject(0);
                        String jstOrderId = jsonObject.getString("o_id");
                        Order order = orderService.getByOrderNo(orderNo);
                        order.setJstOrderId(jstOrderId);
                        orderService.updateById(order);
                        result.setJstOrderId(jstOrderId);
                    }
                }
                
                return result;
            } else {
                // 上传失败
                return new JustuitanOrderUploadResult(false, "订单上传失败: " + message);
            }
        } catch (Exception e) {
            logger.error("解析订单上传响应异常: {}", response, e);
            return new JustuitanOrderUploadResult(false, "解析响应异常: " + e.getMessage());
        }
    }
    
    /**
     * 解析订单拆分响应
     */
    private JustuitanOrderSplitResult parseOrderSplitResponse(String response) {
        try {
            JSONObject jsonResponse = JSONObject.parseObject(response);
            Integer code = jsonResponse.getInteger("code");
            String message = jsonResponse.getString("msg");
            
            if (code != null && code == 0) {
                // 拆分成功
                JSONObject data = jsonResponse.getJSONObject("data");
                JustuitanOrderSplitResult result = new JustuitanOrderSplitResult(true, "订单拆分成功");
                
                if (data != null) {
                    JSONArray newOrderIds = data.getJSONArray("new_o_ids");
                    if (newOrderIds != null) {
                        List<String> orderIdList = new ArrayList<>();
                        for (int i = 0; i < newOrderIds.size(); i++) {
                            orderIdList.add(newOrderIds.getString(i));
                        }
                        result.setNewOrderIds(orderIdList);
                    }
                }
                
                return result;
            } else {
                // 拆分失败
                return new JustuitanOrderSplitResult(false, "订单拆分失败: " + message);
            }
        } catch (Exception e) {
            logger.error("解析订单拆分响应异常: {}", response, e);
            return new JustuitanOrderSplitResult(false, "解析响应异常: " + e.getMessage());
        }
    }
    
    @Override
    public List<JustuitanOrderReponse> queryOrdersFromJst(String modifiedBegin, String modifiedEnd, Integer pageIndex, Integer pageSize) {
        if (!justuitanErpConfig.getSync().getEnabled()) {
            logger.debug("聚水潭ERP同步未启用，跳过订单查询");
            return new ArrayList<>();
        }
        
        if (StrUtil.isBlank(justuitanErpConfig.getAuth().getAppKey()) || 
            StrUtil.isBlank(justuitanErpConfig.getAuth().getAppSecret())) {
            logger.error("聚水潭ERP认证参数未配置，无法查询订单");
            return new ArrayList<>();
        }

        try {
            // 构建业务参数JSONObject
            JSONObject bizParams = new JSONObject();
            bizParams.put("shop_id", 18743335); // 店铺ID
            bizParams.put("page_index", pageIndex != null ? pageIndex : 1);
            bizParams.put("page_size", pageSize != null ? pageSize : 50);
            bizParams.put("date_type", 0); // 按修改时间查询
            
            if (StrUtil.isNotBlank(modifiedBegin) && StrUtil.isNotBlank(modifiedEnd)) {
                bizParams.put("modified_begin", modifiedBegin);
                bizParams.put("modified_end", modifiedEnd);
            } else {
                // 设置默认时间范围：最近24小时
                String endDate = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
                String startDate = DateUtil.format(DateUtil.offsetHour(new Date(), -24), "yyyy-MM-dd HH:mm:ss");
                bizParams.put("modified_begin", startDate);
                bizParams.put("modified_end", endDate);
            }
            
            // 设置查询字段
            JSONArray orderFlds = new JSONArray();
            orderFlds.add("so_id");
            orderFlds.add("shop_status");
            orderFlds.add("status");
            orderFlds.add("pay_date");
            orderFlds.add("logistics_company");
            orderFlds.add("l_id");
            orderFlds.add("receiver_name");
            orderFlds.add("receiver_phone");
            orderFlds.add("receiver_address");
            bizParams.put("order_flds", orderFlds);

            // 构建API请求参数
            Map<String, Object> params = JustuitanApiUtil.buildApiParams(justuitanErpConfig.getAuth().getAppKey(), getAccessToken());
            
            // 将业务参数作为biz参数传递
            params.put("biz", bizParams.toJSONString());
            
            // 生成签名
            String sign = JustuitanApiUtil.generateSign(params, justuitanErpConfig.getAuth().getAppSecret());
            params.put("sign", sign);
            
            // 发送请求
            String url = justuitanErpConfig.getApi().getUrl() + "/open/orders/single/query";
            String response = restTemplateUtil.postFormData(url, params);
            
            // 解析响应并转换为Order对象
            return parseOrderQueryResponse(response);
            
        } catch (Exception e) {
            logger.error("从聚水潭查询订单失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Boolean syncOrderStatusFromJst() {
        try {
            logger.info("开始同步聚水潭订单状态");
            
            // 获取最近24小时的订单数据
            String endTime = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            String startTime = DateUtil.format(DateUtil.offsetHour(new Date(), -24), "yyyy-MM-dd HH:mm:ss");
            
            int pageIndex = 1;
            int pageSize = 50;
            int totalSynced = 0;
            
            while (true) {
                List<JustuitanOrderReponse> jstOrders = queryOrdersFromJst(startTime, endTime, pageIndex, pageSize);
                
                if (CollUtil.isEmpty(jstOrders)) {
                    break;
                }
                
                // 处理每个订单
                for (JustuitanOrderReponse jstOrder : jstOrders) {
                    try {
                        syncSingleOrderStatus(jstOrder);
                        totalSynced++;
                    } catch (Exception e) {
                        logger.error("同步订单状态失败: {}", jstOrder.getOrderNo(), e);
                    }
                }
                
                // 如果返回的订单数量小于页面大小，说明已经是最后一页
                if (jstOrders.size() < pageSize) {
                    break;
                }
                
                pageIndex++;
            }
            
            logger.info("同步聚水潭订单状态完成，共同步{}个订单", totalSynced);
            return true;
            
        } catch (Exception e) {
            logger.error("同步聚水潭订单状态失败", e);
            return false;
        }
    }
    
    @Override
    public Boolean processJstOrderShipment(JSONObject jstOrder) {
        try {
            String orderNo = jstOrder.getString("so_id");
            String status = jstOrder.getString("status");
            String logisticsCompany = jstOrder.getString("logistics_company");
            String trackingNo = jstOrder.getString("l_id");
            
            // 检查是否为已发货状态
            if (!"Sent".equals(status) && !"Delivering".equals(status)) {
                return true; // 不是发货状态，跳过处理
            }
            
            // 查找本地订单
            Order localOrder = orderService.getByOrderNo(orderNo);
            if (localOrder == null) {
                logger.warn("本地订单不存在: {}", orderNo);
                return false;
            }
            
            // 检查是否已存在发货单
            List<OrderInvoiceResponse> existingInvoices = orderService.getInvoiceList(orderNo);
            if (CollUtil.isNotEmpty(existingInvoices)) {
                logger.debug("订单已存在发货单，跳过创建: {}", orderNo);
                return true;
            }
            
            // 获取订单详情
            List<OrderDetail> orderDetailList = orderDetailService.getByOrderNo(orderNo);
            if (CollUtil.isEmpty(orderDetailList)) {
                logger.warn("订单详情不存在: {}", orderNo);
                return false;
            }
            
            // 创建发货单
            OrderInvoice invoice = new OrderInvoice();
            invoice.setOrderNo(orderNo);
            invoice.setMerId(localOrder.getMerId());
            invoice.setUid(localOrder.getUid());
            invoice.setTrackingNumber(trackingNo);
            invoice.setExpressName(logisticsCompany);
            invoice.setExpressCode(""); // 聚水潭可能没有提供快递公司编码
            invoice.setDeliveryType("express");
            invoice.setExpressRecordType("1"); // 快递发货
            invoice.setTotalNum(orderDetailList.stream().mapToInt(OrderDetail::getPayNum).sum());
            invoice.setCreateTime(new Date());
            invoice.setUpdateTime(new Date());
            
            // 创建发货单详情
            List<OrderInvoiceDetail> invoiceDetailList = new ArrayList<>();
            for (OrderDetail orderDetail : orderDetailList) {
                OrderInvoiceDetail invoiceDetail = new OrderInvoiceDetail();
                invoiceDetail.setProductId(orderDetail.getProductId());
                invoiceDetail.setProductName(orderDetail.getProductName());
                invoiceDetail.setImage(orderDetail.getImage());
                invoiceDetail.setAttrValueId(orderDetail.getAttrValueId());
                invoiceDetail.setSku(orderDetail.getSku());
                invoiceDetail.setNum(orderDetail.getPayNum());
                invoiceDetail.setCreateTime(new Date());
                invoiceDetail.setUpdateTime(new Date());
                invoiceDetailList.add(invoiceDetail);
                
                // 更新订单详情发货数量
                orderDetail.setDeliveryNum(orderDetail.getPayNum());
                orderDetail.setUpdateTime(new Date());
            }
            
            // 更新订单状态为已发货
            localOrder.setStatus(4); // 4表示待收货
            localOrder.setUpdateTime(new Date());
            
            // 更新商户订单
            MerchantOrder merchantOrder = merchantOrderService.getOneByOrderNo(orderNo);
            if (merchantOrder != null) {
                merchantOrder.setDeliveryType("express");
                merchantOrder.setUpdateTime(new Date());
            }
            
            // 事务保存
            Boolean result = transactionTemplate.execute(transactionStatus -> {
                try {
                    // 保存发货单
                    orderInvoiceService.save(invoice);
                    
                    // 设置发货单ID并保存详情
                    invoiceDetailList.forEach(detail -> detail.setInvoiceId(invoice.getId()));
                    orderInvoiceDetailService.saveBatch(invoiceDetailList);
                    
                    // 更新订单和订单详情
                    orderService.updateById(localOrder);
                    if (merchantOrder != null) {
                        merchantOrderService.updateById(merchantOrder);
                    }
                    orderDetailService.updateBatchById(orderDetailList);

                    return true;
                } catch (Exception e) {
                    logger.error("保存发货信息失败", e);
                    transactionStatus.setRollbackOnly();
                    return false;
                }
            });
            
            if (result) {
                // 记录订单状态变更日志
                String message = String.format("聚水潭同步发货信息：%s %s", logisticsCompany, trackingNo);
                orderStatusService.createLog(orderNo, "ORDER_STATUS_EXPRESS", message);
                
                logger.info("成功同步订单发货信息: {} -> 物流公司: {}, 物流单号: {}", 
                    orderNo, logisticsCompany, trackingNo);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("处理聚水潭订单发货信息失败", e);
            return false;
        }
    }
    
    /**
     * 解析订单查询响应
     */
    private List<JustuitanOrderReponse> parseOrderQueryResponse(String response) {
        List<JustuitanOrderReponse> orders = new ArrayList<>();
        
        try {
            JSONObject jsonResponse = JSONObject.parseObject(response);
            Integer code = jsonResponse.getInteger("code");
            
            if (code != null && code == 0) {
                JSONObject data = jsonResponse.getJSONObject("data");
                if (data != null) {
                    JSONArray orderArray = data.getJSONArray("orders");
                    if (orderArray != null) {
                        for (int i = 0; i < orderArray.size(); i++) {
                            JSONObject jstOrder = orderArray.getJSONObject(i);
                            Order order = convertJstOrderToLocal(jstOrder);
                            JustuitanOrderReponse orderReponse = new JustuitanOrderReponse();
                            BeanUtils.copyProperties(order, orderReponse);
                            // 使用shop_status而不是status，因为shop_status更准确地反映订单的支付和发货状态
                            String shopStatus = jstOrder.getString("shop_status");
                            String erpStatus = jstOrder.getString("status");
                            // 保存两个状态用于日志和调试
                            orderReponse.setJustuitanOrderStatus(erpStatus);
                            orderReponse.setJstShopStatus(shopStatus); // 需要在JustuitanOrderReponse类中添加此字段
                            orderReponse.setJstLogisticsCompany(jstOrder.getString("logistics_company"));
                            orderReponse.setJstLogisticsNo(jstOrder.getString("l_id"));
                            if (order != null) {
                                orders.add(orderReponse);
                            }
                        }
                    }
                }
            } else {
                logger.error("聚水潭订单查询失败: {}", jsonResponse.getString("msg"));
            }
            
        } catch (Exception e) {
            logger.error("解析订单查询响应异常: {}", response, e);
        }
        
        return orders;
    }
    
    /**
     * 将聚水潭订单转换为本地订单对象
     */
    private Order convertJstOrderToLocal(JSONObject jstOrder) {
        try {
            Order order = new Order();
            order.setOrderNo(jstOrder.getString("so_id"));
            
            // 状态映射
            String jstStatus = jstOrder.getString("status");
            Integer localStatus = mapJstStatusToLocal(jstStatus);
            order.setStatus(localStatus);
            
            return order;
            
        } catch (Exception e) {
            logger.error("转换聚水潭订单失败: {}", jstOrder, e);
            return null;
        }
    }
    
    /**
     * 映射聚水潭平台订单状态（shop_status）到本地状态
     * 使用shop_status更准确，因为它反映了订单在平台上的真实状态
     * 
     * 聚水潭平台订单状态（shop_status）说明：
     * WAIT_BUYER_PAY - 等待买家付款 -> 本地待付款(0)
     * WAIT_SELLER_SEND_GOODS - 等待卖家发货 -> 本地待发货(1)
     * WAIT_BUYER_CONFIRM_GOODS - 等待买家确认收货 -> 本地已发货(4)
     * TRADE_FINISHED - 交易成功 -> 本地已完成(3)
     * TRADE_CLOSED - 交易关闭 -> 本地已取消(9)
     * TRADE_CLOSED_BY_TAOBAO - 付款前交易关闭 -> 本地已取消(9)
     */
    private Integer mapShopStatusToLocal(String shopStatus) {
        if (StrUtil.isBlank(shopStatus)) {
            logger.warn("聚水潭平台订单状态为空，保持原状态");
            return null;
        }
        
        switch (shopStatus) {
            case "WAIT_BUYER_PAY":
                return 0; // 待付款
            case "WAIT_SELLER_SEND_GOODS":
                return 1; // 待发货
            case "WAIT_BUYER_CONFIRM_GOODS":
                return 4; // 已发货（等待买家确认收货，说明已经发货了）
            case "TRADE_FINISHED":
                return 3; // 已完成
            case "TRADE_CLOSED":
            case "TRADE_CLOSED_BY_TAOBAO":
                return 9; // 已取消
            default:
                logger.warn("未知的聚水潭平台订单状态: {}，保持原状态不变", shopStatus);
                return null;
        }
    }
    
    /**
     * 映射聚水潭ERP内部订单状态（status）到本地状态
     * 注意：此方法仅用于兼容性，推荐使用mapShopStatusToLocal
     * 
     * 聚水潭ERP订单状态说明：
     * WaitPay - 待付款 -> 本地待付款(0)
     * Delivering - 发货中 -> 本地待发货(1)
     * Merged - 被合并 -> 本地待发货(1)
     * Question - 异常 -> 本地待发货(1)
     * Split - 被拆分 -> 本地待发货(1)
     * WaitOuterSent - 等供销商|外仓发货 -> 本地待发货(1)
     * WaitConfirm - 已付款待审核 -> 本地待发货(1)
     * WaitFConfirm - 已客审待财审 -> 本地待发货(1)
     * Sent - 已发货 -> 本地已发货(4)
     * Cancelled - 取消 -> 本地已取消(9)
     */
    private Integer mapJstStatusToLocal(String jstStatus) {
        if (StrUtil.isBlank(jstStatus)) {
            logger.warn("聚水潭ERP订单状态为空，保持原状态");
            return null; // 返回null表示不更新状态
        }
        
        switch (jstStatus) {
            case "WaitPay":
                return 0; // 待付款
            case "WaitConfirm":
            case "WaitFConfirm":
            case "Delivering":
            case "WaitOuterSent":
            case "Merged":
            case "Split":
            case "Question":
                return 1; // 待发货
            case "Sent":
                return 4; // 已发货
            case "Finished":
                return 3; // 已完成
            case "Cancelled":
                return 9; // 已取消
            default:
                logger.warn("未知的聚水潭ERP订单状态: {}，保持原状态不变", jstStatus);
                return null; // 返回null表示不更新状态，保持原有状态
        }
    }
    
    /**
     * 同步单个聚水潭订单状态
     * @param jstOrder 聚水潭订单信息
     */
    @Override
    public void syncOrderStatus(JustuitanOrderReponse jstOrder) {
        syncSingleOrderStatus(jstOrder);
    }
    
    /**
     * 同步单个订单状态
     */
    private void syncSingleOrderStatus(JustuitanOrderReponse jstOrder) {
        try {
            String orderNo = jstOrder.getOrderNo();
            
            // 查找本地订单
            Order localOrder = orderService.getByOrderNo(orderNo);
            if (localOrder == null) {
                logger.debug("本地订单不存在，跳过同步: {}", orderNo);
                return;
            }
            
            // 优先使用status（ERP内部订单状态），因为发货操作在聚水潭ERP上进行
            String erpStatus = jstOrder.getJustuitanOrderStatus();
            String shopStatus = jstOrder.getJstShopStatus();
            
            logger.info("订单状态同步信息: orderNo={}, erp_status={}, shop_status={}, 当前本地状态={}, 本地支付状态={}", 
                orderNo, erpStatus, shopStatus, localOrder.getStatus(), localOrder.getPaid());
            
            // ===== 重要：检查本地订单支付状态 =====
            // 如果本地订单未支付，强制保持待付款状态，并同步更新聚水潭
            if (localOrder.getPaid() == null || !localOrder.getPaid()) {
                logger.info("本地订单未支付，检查订单状态: orderNo={}, paid={}, status={}", orderNo, localOrder.getPaid(), localOrder.getStatus());
                
                // 如果订单已经取消（状态9），则不处理，保持取消状态
                if (localOrder.getStatus() == 9) {
                    logger.info("本地未支付订单已取消，保持取消状态: orderNo={}", orderNo);
                    return; // 已取消的订单不同步状态
                }
                
                // 如果本地状态不是待付款且不是已取消，更正为待付款
                if (localOrder.getStatus() != 0) {
                    logger.warn("本地未支付订单状态异常，更正为待付款: orderNo={}, 原状态={}", orderNo, localOrder.getStatus());
                    localOrder.setStatus(0);
                    localOrder.setUpdateTime(new Date());
                    orderService.updateById(localOrder);
                }
                
            // 如果聚水潭状态不是待付款，同步更新聚水潭为待付款
            if (!"WaitPay".equals(erpStatus) && !"WAIT_BUYER_PAY".equals(shopStatus)) {
                logger.warn("本地未支付但聚水潭状态异常，开始同步更新聚水潭为待付款: orderNo={}, jst_erp_status={}, jst_shop_status={}", 
                    orderNo, erpStatus, shopStatus);
                
                try {
                    // 聚水潭没有专门的订单状态修改接口
                    // 需要重新调用订单上传接口，使用相同的so_id来更新订单状态
                    // 构建订单数据，强制设置为待付款状态
                    Order tempOrder = new Order();
                    BeanUtils.copyProperties(localOrder, tempOrder);
                    tempOrder.setStatus(0); // 设置为待付款
                    tempOrder.setPaid(false); // 未支付
                    
                    JustuitanOrderUploadResult uploadResult = uploadOrderToJst(tempOrder);
                    if (uploadResult.isSuccess()) {
                        logger.info("成功将聚水潭订单状态更新为待付款: orderNo={}", orderNo);
                    } else {
                        logger.error("更新聚水潭订单状态为待付款失败: orderNo={}, 错误: {}", orderNo, uploadResult.getMessage());
                    }
                } catch (Exception e) {
                    logger.error("同步聚水潭订单状态为待付款异常: orderNo={}, 错误: {}", orderNo, e.getMessage(), e);
                }
            }
                
                return; // 未支付订单不继续同步其他状态
            }
            
            Integer mappedStatus = null;
            String statusSource = null;
            
            // 优先使用ERP内部状态（status）
            if (StrUtil.isNotBlank(erpStatus)) {
                mappedStatus = mapJstStatusToLocal(erpStatus);
                statusSource = "erp_status[" + erpStatus + "]";
            } 
            // 如果ERP状态为空，则使用平台状态作为备选
            else if (StrUtil.isNotBlank(shopStatus)) {
                mappedStatus = mapShopStatusToLocal(shopStatus);
                statusSource = "shop_status[" + shopStatus + "]";
                logger.warn("erp_status为空，使用shop_status: orderNo={}, shop_status={}", orderNo, shopStatus);
            } 
            // 两个状态都为空
            else {
                logger.warn("聚水潭订单状态为空，跳过同步: {}", orderNo);
                return;
            }
            
            // 如果映射结果为null，表示未知状态，不更新
            if (mappedStatus == null) {
                logger.info("订单状态未知，跳过更新: orderNo={}, {}", orderNo, statusSource);
                return;
            }
            
            // 如果聚水潭显示待付款，但本地已支付，以本地支付状态为准
            if (mappedStatus == 0 && localOrder.getPaid()) {
                logger.warn("聚水潭显示待付款，但本地已支付，保持本地状态: orderNo={}, 本地状态={}", 
                    orderNo, localOrder.getStatus());
                return; // 不更新状态，以本地支付状态为准
            }
            
            // 如果聚水潭ERP状态为已发货（Sent），并且本地订单状态为待发货（1），则调用本地发货接口
            boolean isJstSent = "Sent".equals(erpStatus);
            boolean isLocalWaitSend = localOrder.getStatus() == 1; // 待发货
            
            if (isJstSent && isLocalWaitSend && StrUtil.isNotBlank(jstOrder.getJstLogisticsNo())) {
                logger.info("聚水潭订单已发货，本地订单待发货，开始调用本地发货接口: orderNo={}", orderNo);
                logger.info("快递公司: {}, 快递单号: {}", jstOrder.getJstLogisticsCompany(), jstOrder.getJstLogisticsNo());
                
                try {
                    OrderSendRequest orderSendRequest = new OrderSendRequest();
                    orderSendRequest.setOrderNo(orderNo);
                    orderSendRequest.setDeliveryType("express");
                    orderSendRequest.setIsSplit(false);
                    
                    // 处理快递公司名称
                    String logisticsCompany = jstOrder.getJstLogisticsCompany();
                    if (StrUtil.isNotBlank(logisticsCompany) && logisticsCompany.contains("邮政快递包裹")) {
                        logisticsCompany = "邮政快递包裹";
                    }
                    
                    // 通过快递公司名称查询快递公司编号
                    if (StrUtil.isNotBlank(logisticsCompany)) {
                        Express express = expressService.getbyName(logisticsCompany);
                        if (Objects.nonNull(express)) {
                            orderSendRequest.setExpressCode(express.getCode());
                            logger.info("找到快递公司编号: {} -> {}", logisticsCompany, express.getCode());
                        } else {
                            logger.warn("未找到快递公司: {}", logisticsCompany);
                        }
                    }
                    
                    orderSendRequest.setExpressNumber(jstOrder.getJstLogisticsNo());
                    orderSendRequest.setDeliveryMark(logisticsCompany);
                    orderSendRequest.setExpressRecordType("1");
                    
                    // 调用发货接口，发货接口会自动更新订单状态为已发货（4）
                    orderService.send(orderSendRequest);
                    logger.info("本地发货接口调用成功，订单状态已更新: orderNo={}", orderNo);
                    
                    // 发货接口调用成功后，直接返回，不需要再次更新状态
                    return;
                    
                } catch (Exception e) {
                    logger.error("调用本地发货接口失败: orderNo={}, 错误: {}", orderNo, e.getMessage(), e);
                    // 发货接口失败时，继续后续的状态更新逻辑
                }
            }
            
            // 检查状态是否需要更新
            if (mappedStatus.equals(localOrder.getStatus())) {
                logger.debug("订单状态相同，无需更新: orderNo={}, status={}", orderNo, mappedStatus);
                return;
            }
            
            // ===== 重要：状态优先级保护 =====
            // 如果本地订单状态比聚水潭状态更"新"，则不更新（避免状态倒退）
            // 状态优先级：已完成(5) > 已收货(4) > 待发货(1) > 待付款(0)
            Integer localStatus = localOrder.getStatus();
            
            // 场景1：本地已收货/已完成，聚水潭还是待发货/已发货，不能倒退
            if ((localStatus == 4 || localStatus == 5) && (mappedStatus == 1 || mappedStatus == 2)) {
                logger.warn("本地订单状态优先级更高，拒绝更新: orderNo={}, 本地状态={}, 聚水潭映射状态={}", 
                    orderNo, localStatus, mappedStatus);
                return;
            }
            
            // 场景2：本地已完成(5)，聚水潭还是已发货(4)，不能倒退
            if (localStatus == 5 && mappedStatus == 4) {
                logger.warn("本地订单已完成，拒绝更新为已发货: orderNo={}, 本地状态={}, 聚水潭映射状态={}", 
                    orderNo, localStatus, mappedStatus);
                return;
            }
            
            // 场景3：本地已发货(4)，但聚水潭还是待发货(1)，可能聚水潭数据延迟，不更新
            if (localStatus == 4 && mappedStatus == 1) {
                logger.warn("本地已发货但聚水潭待发货，可能数据延迟，保持本地状态: orderNo={}, 本地状态={}, 聚水潭映射状态={}", 
                    orderNo, localStatus, mappedStatus);
                return;
            }
            
            // 更新订单状态
            localOrder.setStatus(mappedStatus);
            localOrder.setUpdateTime(new Date());
            
            boolean updateResult = orderService.updateById(localOrder);

            if (updateResult) {
                logger.info("成功同步订单状态: orderNo={}, {} -> 本地状态[{}]", orderNo, statusSource, mappedStatus);
            } else {
                logger.error("更新订单状态失败: {}", orderNo);
            }
            
        } catch (Exception e) {
            logger.error("同步单个订单状态失败: {}", jstOrder.getOrderNo(), e);
        }
    }
    
    /**
     * 解析聚水潭发货响应
     */
    private Boolean parseShipmentResponse(String response, String orderNo) {
        try {
            JSONObject jsonResponse = JSONObject.parseObject(response);
            Integer code = jsonResponse.getInteger("code");
            String msg = jsonResponse.getString("msg");
            Boolean issuccess = jsonResponse.getBoolean("issuccess");
            
            if (code != null && code == 0 && (issuccess == null || issuccess)) {
                logger.info("聚水潭发货成功: orderNo={}, msg={}", orderNo, msg);
                return true;
            } else {
                // 检查详细错误信息
                JSONArray dataArray = jsonResponse.getJSONArray("data");
                if (dataArray != null && !dataArray.isEmpty()) {
                    JSONObject errorData = dataArray.getJSONObject(0);
                    String errorMsg = errorData.getString("msg");
                    Boolean success = errorData.getBoolean("issuccess");
                    
                    if (success != null && !success) {
                        logger.warn("聚水潭发货失败: orderNo={}, error={}", orderNo, errorMsg);
                        return false;
                    }
                }
                
                logger.warn("聚水潭发货响应异常: orderNo={}, code={}, msg={}", orderNo, code, msg);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("解析聚水潭发货响应失败: orderNo={}, response={}", orderNo, response, e);
            return false;
        }
    }
    
    /**
     * 获取商户对应的聚水潭店铺ID
     */
    private String getJstShopId(Integer merId) {
        // 从系统配置中获取商户与聚水潭店铺的映射关系
        String configKey = "jst_shop_mapping_" + merId;
        String shopId = systemConfigService.getValueByKey(configKey);
        
        if (StrUtil.isBlank(shopId)) {
            // 如果没有配置，使用默认店铺ID
            shopId = systemConfigService.getValueByKey("jst_default_shop_id");
        }
        
        return shopId;
    }
    
    /**
     * 获取聚水潭内部订单号
     */
    private String getJstOrderId(String orderNo) {
        Order byOrderNo = orderService.getByOrderNo(orderNo);
        if (byOrderNo != null) {
            return byOrderNo.getJstOrderId();
        }
        return null;
    }
    
    /**
     * 获取快递公司编码
     */
    private String getLogisticsCode(String logisticsCompany) {
        // 快递公司名称到编码的映射
        Map<String, String> logisticsMapping = new HashMap<>();
        logisticsMapping.put("顺丰速运", "SF");
        logisticsMapping.put("圆通速递", "YTO");
        logisticsMapping.put("中通快递", "ZTO");
        logisticsMapping.put("申通快递", "STO");
        logisticsMapping.put("韵达速递", "YD");
        logisticsMapping.put("百世快递", "HTKY");
        logisticsMapping.put("天天快递", "HHTT");
        logisticsMapping.put("京东快递", "JD");
        logisticsMapping.put("邮政快递包裹", "YZPY");
        logisticsMapping.put("EMS", "EMS");
        
        return logisticsMapping.get(logisticsCompany);
    }
    
    @Override
    public Boolean cancelOrderInJst(String orderNo, String cancelType, String remark) {
        try {
            logger.info("开始取消聚水潭订单，订单号: {}, 取消类型: {}", orderNo, cancelType);
            
            // 根据订单号获取聚水潭内部订单ID
            String jstOrderId = getJstOrderId(orderNo);
            if (StrUtil.isBlank(jstOrderId)) {
                logger.error("无法获取聚水潭内部订单ID，订单号: {}", orderNo);
                return false;
            }
            
            // 构建业务参数
            JSONObject bizParams = new JSONObject();
            JSONArray oIds = new JSONArray();
            oIds.add(Integer.parseInt(jstOrderId));
            bizParams.put("o_ids", oIds);
            bizParams.put("cancel_type", StrUtil.isNotBlank(cancelType) ? cancelType : "不需要了");
            if (StrUtil.isNotBlank(remark)) {
                bizParams.put("remark", remark);
            }
            
            // 构建API请求参数
            Map<String, Object> params = JustuitanApiUtil.buildApiParams(justuitanErpConfig.getAuth().getAppKey(), getAccessToken());
            if (params == null) {
                logger.error("无法构建API请求参数，订单取消失败");
                return false;
            }
            
            // 将业务参数作为biz参数传递
            params.put("biz", bizParams.toJSONString());
            
            // 生成签名
            String sign = JustuitanApiUtil.generateSign(params, justuitanErpConfig.getAuth().getAppSecret());
            params.put("sign", sign);
            
            // 发送请求
            String url = justuitanErpConfig.getApi().getUrl() + "/open/jushuitan/orderbyoid/cancel";
            String response = restTemplateUtil.postFormData(url, params);
            
            logger.info("聚水潭订单取消响应，订单号: {}, 响应: {}", orderNo, response);
            
            // 解析响应
            return parseCancelOrderResponse(response, orderNo);
            
        } catch (Exception e) {
            logger.error("取消聚水潭订单异常，订单号: {}", orderNo, e);
            return false;
        }
    }
    
    @Override
    public Boolean uploadAfterSaleToJst(String orderNo, String afterSaleNo, String afterSaleType, BigDecimal refundAmount, String remark, String shopStatus) {
        try {
            logger.info("开始上传售后信息到聚水潭，订单号: {}, 售后单号: {}, 售后类型: {}, 状态: {}", orderNo, afterSaleNo, afterSaleType, shopStatus);
            
            // 获取订单信息
            Order order = orderService.getByOrderNo(orderNo);
            if (order == null) {
                logger.error("订单不存在，订单号: {}", orderNo);
                return false;
            }
            
            // 检查订单所属商户是否为自营店
            if (!isSelfOperatedStore(order.getMerId())) {
                logger.debug("订单不属于自营店，跳过售后单上传到聚水潭: orderNo={}, merId={}", orderNo, order.getMerId());
                return true;
            }
            
            // 获取订单详情
            List<OrderDetail> orderDetails = orderDetailService.getByOrderNo(orderNo);
            if (CollUtil.isEmpty(orderDetails)) {
                logger.error("订单详情不存在，订单号: {}", orderNo);
                return false;
            }
            
            // 使用默认状态WAIT_SELLER_AGREE如果未指定
            if (StrUtil.isBlank(shopStatus)) {
                shopStatus = "WAIT_SELLER_AGREE";
            }
            
            // 构建售后数据
            JSONObject afterSaleData = new JSONObject();
            afterSaleData.put("shop_id", 18743335);
            afterSaleData.put("outer_as_id", afterSaleNo);
            afterSaleData.put("so_id", orderNo);
            afterSaleData.put("type", "仅退款");
            afterSaleData.put("shop_status", shopStatus);
            afterSaleData.put("good_status", "BUYER_NOT_RECEIVED");
            afterSaleData.put("question_type", remark);
            afterSaleData.put("total_amount", order.getPayPrice());
            afterSaleData.put("refund", refundAmount != null ? refundAmount : BigDecimal.ZERO);
            afterSaleData.put("payment", BigDecimal.ZERO);
            if (StrUtil.isNotBlank(remark)) {
                afterSaleData.put("remark", remark);
            }
            
            // 构建商品列表
            JSONArray items = new JSONArray();
            for (OrderDetail orderDetail : orderDetails) {
                JSONObject item = new JSONObject();
                item.put("sku_id", orderDetail.getSku());
                item.put("qty", orderDetail.getPayNum());
                item.put("amount", orderDetail.getPrice().multiply(new BigDecimal(orderDetail.getPayNum())));
                item.put("type", "退货");
                item.put("name", orderDetail.getProductName());
                items.add(item);
            }
            afterSaleData.put("items", items);
            
            // 构建业务参数（数组格式）
            JSONArray bizParams = new JSONArray();
            bizParams.add(afterSaleData);
            
            // 构建API请求参数
            Map<String, Object> params = JustuitanApiUtil.buildApiParams(justuitanErpConfig.getAuth().getAppKey(), getAccessToken());
            if (params == null) {
                logger.error("无法构建API请求参数，售后上传失败");
                return false;
            }
            
            // 将业务参数作为biz参数传递
            params.put("biz", bizParams.toJSONString());
            
            // 生成签名
            String sign = JustuitanApiUtil.generateSign(params, justuitanErpConfig.getAuth().getAppSecret());
            params.put("sign", sign);
            
            // 发送请求
            String url = justuitanErpConfig.getApi().getUrl() + "/open/aftersale/upload";
            String response = restTemplateUtil.postFormData(url, params);
            
            logger.info("聚水潭售后上传响应，订单号: {}, 售后单号: {}, 状态: {}, 响应: {}", orderNo, afterSaleNo, shopStatus, response);
            
            // 解析响应
            return parseAfterSaleUploadResponse(response, orderNo, afterSaleNo);
            
        } catch (Exception e) {
            logger.error("上传售后信息到聚水潭异常，订单号: {}, 售后单号: {}", orderNo, afterSaleNo, e);
            return false;
        }
    }
    
    /**
     * 解析订单取消响应
     */
    private Boolean parseCancelOrderResponse(String response, String orderNo) {
        try {
            if (StrUtil.isBlank(response)) {
                logger.error("聚水潭订单取消响应为空，订单号: {}", orderNo);
                return false;
            }
            
            JSONObject jsonResponse = JSONObject.parseObject(response);
            Integer code = jsonResponse.getInteger("code");
            String msg = jsonResponse.getString("msg");
            
            if (code != null && code == 0) {
                logger.info("聚水潭订单取消成功，订单号: {}, 响应消息: {}", orderNo, msg);
                return true;
            } else {
                logger.error("聚水潭订单取消失败，订单号: {}, 错误码: {}, 错误消息: {}", orderNo, code, msg);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("解析聚水潭订单取消响应异常，订单号: {}, 响应: {}", orderNo, response, e);
            return false;
        }
    }
    
    /**
     * 解析售后上传响应
     */
    private Boolean parseAfterSaleUploadResponse(String response, String orderNo, String afterSaleNo) {
        try {
            if (StrUtil.isBlank(response)) {
                logger.error("聚水潭售后上传响应为空，订单号: {}, 售后单号: {}", orderNo, afterSaleNo);
                return false;
            }
            
            JSONObject jsonResponse = JSONObject.parseObject(response);
            Integer code = jsonResponse.getInteger("code");
            String msg = jsonResponse.getString("msg");
            
            if (code != null && code == 0) {
                // 检查具体的上传结果
                JSONObject data = jsonResponse.getJSONObject("data");
                if (data != null) {
                    JSONArray datas = data.getJSONArray("datas");
                    if (datas != null && !datas.isEmpty()) {
                        JSONObject result = datas.getJSONObject(0);
                        Boolean isSuccess = result.getBoolean("issuccess");
                        String resultMsg = result.getString("msg");
                        
                        if (isSuccess != null && isSuccess) {
                            logger.info("聚水潭售后上传成功，订单号: {}, 售后单号: {}, 聚水潭售后ID: {}", 
                                orderNo, afterSaleNo, result.getInteger("as_id"));
                            return true;
                        } else {
                            logger.error("聚水潭售后上传失败，订单号: {}, 售后单号: {}, 错误消息: {}", 
                                orderNo, afterSaleNo, resultMsg);
                            return false;
                        }
                    }
                }
                
                logger.info("聚水潭售后上传成功，订单号: {}, 售后单号: {}", orderNo, afterSaleNo);
                return true;
            } else {
                logger.error("聚水潭售后上传失败，订单号: {}, 售后单号: {}, 错误码: {}, 错误消息: {}", 
                    orderNo, afterSaleNo, code, msg);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("解析聚水潭售后上传响应异常，订单号: {}, 售后单号: {}, 响应: {}", 
                orderNo, afterSaleNo, response, e);
            return false;
        }
    }

    @Override
    public Boolean updateOrderRemarkInJst(String jstOrderId, String remark, Boolean isAppend) {
        if (!justuitanErpConfig.getSync().getEnabled()) {
            logger.debug("聚水潭ERP同步未启用，跳过订单备注修改");
            return true;
        }

        try {
            logger.info("开始修改聚水潭订单备注，订单ID: {}, 备注: {}", jstOrderId, remark);

            // 构建业务参数
            JSONObject bizParams = new JSONObject();
            bizParams.put("o_id", Integer.parseInt(jstOrderId));
            bizParams.put("remark", remark);
            bizParams.put("is_append", isAppend != null ? isAppend : false);

            // 构建API请求参数
            Map<String, Object> params = JustuitanApiUtil.buildApiParams(justuitanErpConfig.getAuth().getAppKey(), getAccessToken());
            if (params == null) {
                logger.error("无法构建API请求参数，订单备注修改失败");
                return false;
            }

            // 将业务参数作为biz参数传递
            params.put("biz", bizParams.toJSONString());

            // 生成签名
            String sign = JustuitanApiUtil.generateSign(params, justuitanErpConfig.getAuth().getAppSecret());
            params.put("sign", sign);

            // 发送请求
            String url = justuitanErpConfig.getApi().getUrl() + "/open/jushuitan/order/remark/upload";
            String response = restTemplateUtil.postFormData(url, params);

            logger.info("聚水潭订单备注修改响应，订单ID: {}, 响应: {}", jstOrderId, response);

            // 解析响应
            return parseApiResponse(response);

        } catch (Exception e) {
            logger.error("修改聚水潭订单备注异常，订单ID: {}", jstOrderId, e);
            return false;
        }
    }

    @Override
    public Boolean updateOrderLogisticsInJst(String jstOrderId, String logisticsCode) {
        if (!justuitanErpConfig.getSync().getEnabled()) {
            logger.debug("聚水潭ERP同步未启用，跳过订单快递修改");
            return true;
        }

        try {
            logger.info("开始修改聚水潭订单快递，订单ID: {}, 快递公司编码: {}", jstOrderId, logisticsCode);

            // 构建业务参数
            JSONObject orderLc = new JSONObject();
            orderLc.put("oid", Integer.parseInt(jstOrderId));
            orderLc.put("lc_id", logisticsCode);

            JSONArray orderLcs = new JSONArray();
            orderLcs.add(orderLc);

            JSONObject bizParams = new JSONObject();
            bizParams.put("order_lcs", orderLcs);

            // 构建API请求参数
            Map<String, Object> params = JustuitanApiUtil.buildApiParams(justuitanErpConfig.getAuth().getAppKey(), getAccessToken());
            if (params == null) {
                logger.error("无法构建API请求参数，订单快递修改失败");
                return false;
            }

            // 将业务参数作为biz参数传递
            params.put("biz", bizParams.toJSONString());

            // 生成签名
            String sign = JustuitanApiUtil.generateSign(params, justuitanErpConfig.getAuth().getAppSecret());
            params.put("sign", sign);

            // 发送请求
            String url = justuitanErpConfig.getApi().getUrl() + "/open/webapi/orderapi/modifyorder/modifyorderlc";
            String response = restTemplateUtil.postFormData(url, params);

            logger.info("聚水潭订单快递修改响应，订单ID: {}, 响应: {}", jstOrderId, response);

            // 解析响应
            return parseLogisticsUpdateResponse(response);

        } catch (Exception e) {
            logger.error("修改聚水潭订单快递异常，订单ID: {}", jstOrderId, e);
            return false;
        }
    }

    @Override
    public Boolean isSelfOperatedStore(Integer merId) {
        try {
            if(merId!=0){
                Merchant merchant = merchantService.getById(merId);
                if(merchant.getIsSelf()){
                    return true;
                }
            }else{
                return true;
            }
            return false;

        } catch (Exception e) {
            logger.error("检查商户类型异常，商户ID: {}", merId, e);
            return false;
        }
    }

    @Override
    public Boolean uploadShopProductToJst(Product product) {
        try {
            logger.info("开始上传店铺商品资料到聚水潭，商品ID: {}, 商品名称: {}", product.getId(), product.getName());
            
            // 获取商品规格列表
            List<ProductAttrValue> attrValues = productAttrValueService.getListByProductIdAndType(
                product.getId(), product.getType(), product.getMarketingType(), false);
            
            if (CollUtil.isEmpty(attrValues)) {
                logger.error("商品没有规格信息，无法上传店铺商品，商品ID: {}", product.getId());
                return false;
            }
            
            // 构建店铺商品上传数据列表
            JSONArray items = new JSONArray();
            for (ProductAttrValue attrValue : attrValues) {
                JSONObject itemData = buildShopProductUploadData(product, attrValue);
                items.add(itemData);
            }
            
            // 构建业务参数
            JSONObject bizParams = new JSONObject();
            bizParams.put("items", items);
            
            Map<String, Object> params = JustuitanApiUtil.buildApiParams(justuitanErpConfig.getAuth().getAppKey(), getAccessToken());
            
            // 将业务参数作为biz参数传递
            params.put("biz", bizParams.toJSONString());
            
            // 生成签名
            String sign = JustuitanApiUtil.generateSign(params, justuitanErpConfig.getAuth().getAppSecret());
            params.put("sign", sign);
            
            // 发送请求
            String url = justuitanErpConfig.getApi().getUrl() + "/open/jushuitan/skumap/upload";
            String response = restTemplateUtil.postFormData(url, params);
            
            logger.info("聚水潭店铺商品上传响应: {}", response);
            
            // 解析响应
            return parseApiResponse(response);
            
        } catch (Exception e) {
            logger.error("上传店铺商品到聚水潭失败，商品ID: {}, 错误信息: {}", product.getId(), e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Boolean updateProductStatusInJst(Product product, Integer enabled) {
        try {
            logger.info("开始更新聚水潭商品状态，商品ID: {}, 商品名称: {}, 状态: {}", product.getId(), product.getName(), enabled);
            
            // 获取商品规格列表
            List<ProductAttrValue> attrValues = productAttrValueService.getListByProductIdAndType(
                product.getId(), product.getType(), product.getMarketingType(), false);
            
            if (CollUtil.isEmpty(attrValues)) {
                logger.error("商品没有规格信息，无法更新状态，商品ID: {}", product.getId());
                return false;
            }
            
            // 构建商品状态更新数据列表
            JSONArray items = new JSONArray();
            for (ProductAttrValue attrValue : attrValues) {
                JSONObject itemData = buildProductStatusUpdateData(product, attrValue, enabled);
                items.add(itemData);
            }
            
            // 构建业务参数
            JSONObject bizParams = new JSONObject();
            bizParams.put("items", items);
            
            Map<String, Object> params = JustuitanApiUtil.buildApiParams(justuitanErpConfig.getAuth().getAppKey(), getAccessToken());
            
            // 将业务参数作为biz参数传递
            params.put("biz", bizParams.toJSONString());
            
            // 生成签名
            String sign = JustuitanApiUtil.generateSign(params, justuitanErpConfig.getAuth().getAppSecret());
            params.put("sign", sign);
            
            // 发送请求 - 使用商品上传接口更新状态
            String url = justuitanErpConfig.getApi().getUrl() + "/open/jushuitan/itemsku/upload";
            String response = restTemplateUtil.postFormData(url, params);
            
            logger.info("聚水潭商品状态更新响应，商品ID: {}, 状态: {}, 响应: {}", product.getId(), enabled, response);
            
            // 解析响应
            return parseApiResponse(response);
            
        } catch (Exception e) {
            logger.error("更新聚水潭商品状态失败，商品ID: {}, 错误信息: {}", product.getId(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 构建店铺商品上传数据
     */
    private JSONObject buildShopProductUploadData(Product product, ProductAttrValue attrValue) {
        JSONObject shopItemData = new JSONObject();
        try {
            // 必填字段
            shopItemData.put("shop_id", 18743335); // 店铺ID（自营店铺）
            shopItemData.put("sku_id", attrValue.getId().toString()); // ERP商品编码
            shopItemData.put("i_id", product.getId().toString()); // ERP款式编码
            shopItemData.put("shop_sku_id", attrValue.getId().toString()); // 店铺商品编码
            shopItemData.put("shop_i_id", product.getId().toString()); // 店铺款式编码
            
            // 可选字段
            if (StrUtil.isNotBlank(attrValue.getBarCode())) {
                shopItemData.put("sku_code", attrValue.getBarCode()); // 国标码
            }
            if (StrUtil.isNotBlank(product.getName())) {
                shopItemData.put("name", product.getName()); // 商品名称
            }
            if (StrUtil.isNotBlank(attrValue.getAttrValue()) && !"{}".equals(attrValue.getAttrValue())) {
                try {
                    JSONObject attrJson = JSONObject.parseObject(attrValue.getAttrValue());
                    if (attrJson != null && !attrJson.isEmpty()) {
                        String propertiesValue = attrJson.values().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(";"));
                        if (StrUtil.isNotBlank(propertiesValue) && propertiesValue.length() <= 100) {
                            shopItemData.put("shop_properties_value", propertiesValue); // 店铺颜色规格
                        }
                    }
                } catch (Exception e) {
                    logger.warn("解析商品规格属性失败，商品ID: {}, 规格ID: {}", product.getId(), attrValue.getId());
                }
            }
            
            logger.debug("构建店铺商品上传数据完成，商品ID: {}, 规格ID: {}", product.getId(), attrValue.getId());
            
        } catch (Exception e) {
            logger.error("构建店铺商品上传数据失败，商品ID: {}, 规格ID: {}", product.getId(), attrValue.getId(), e);
        }
        
        return shopItemData;
    }
    
    /**
     * 构建商品状态更新数据（仅更新enabled字段）
     */
    private JSONObject buildProductStatusUpdateData(Product product, ProductAttrValue attrValue, Integer enabled) {
        JSONObject itemData = new JSONObject();
        try {
            // 必填字段
            itemData.put("sku_id", attrValue.getId().toString()); // 商品编码
            itemData.put("i_id", product.getId().toString()); // 款式编码
            itemData.put("name", product.getName()); // 商品名称
            
            // 状态字段
            itemData.put("enabled", enabled); // 是否启用：1=启用，-1=禁用
            
            logger.debug("构建商品状态更新数据完成，商品ID: {}, 规格ID: {}, 状态: {}", product.getId(), attrValue.getId(), enabled);
            
        } catch (Exception e) {
            logger.error("构建商品状态更新数据失败，商品ID: {}, 规格ID: {}", product.getId(), attrValue.getId(), e);
        }
        
        return itemData;
    }

    /**
     * 解析快递修改响应
     */
    private Boolean parseLogisticsUpdateResponse(String response) {
        try {
            if (StrUtil.isBlank(response)) {
                logger.error("聚水潭快递修改响应为空");
                return false;
            }

            JSONObject jsonResponse = JSONObject.parseObject(response);
            Integer code = jsonResponse.getInteger("code");
            String msg = jsonResponse.getString("msg");

            if (code != null && code == 0) {
                // 检查是否有失败的订单
                JSONObject data = jsonResponse.getJSONObject("data");
                if (data != null) {
                    JSONArray failOrders = data.getJSONArray("fail_orders");
                    if (failOrders != null && !failOrders.isEmpty()) {
                        logger.warn("聚水潭快递修改部分失败: {}", failOrders.toJSONString());
                        return false;
                    }
                }

                logger.info("聚水潭快递修改成功: {}", msg);
                return true;
            } else {
                logger.error("聚水潭快递修改失败，错误码: {}, 错误信息: {}", code, msg);
                return false;
            }

        } catch (Exception e) {
            logger.error("解析聚水潭快递修改响应异常，响应: {}", response, e);
            return false;
        }
    }
}