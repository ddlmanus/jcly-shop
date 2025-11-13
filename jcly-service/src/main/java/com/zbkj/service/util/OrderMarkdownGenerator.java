package com.zbkj.service.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.zbkj.common.model.order.Order;
import com.zbkj.common.model.order.OrderDetail;
import com.zbkj.common.model.order.MerchantOrder;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserAddress;
import com.zbkj.common.model.merchant.Merchant;
import com.zbkj.common.model.product.Product;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * 订单Markdown生成工具类
 */
public class OrderMarkdownGenerator {
    
    /**
     * 生成订单信息的Markdown内容
     * 
     * @param order 订单信息
     * @param user 用户信息
     * @param userAddress 用户地址信息（可为null）
     * @param orderDetailList 订单详情列表
     * @param merchantOrderList 商户订单列表
     * @param merchantMap 商户信息映射
     * @param productMap 商品信息映射
     * @return 生成的Markdown内容
     */
    public static String generateOrderMarkdown(Order order, User user, UserAddress userAddress,
            List<OrderDetail> orderDetailList, List<MerchantOrder> merchantOrderList,
            Map<Integer, Merchant> merchantMap, Map<Integer, Product> productMap) {
        
        if (order == null || user == null) {
            return "";
        }
        
        StringBuilder markdown = new StringBuilder();
        
        // 订单标题
        markdown.append("# 订单详情信息\n\n");
        
        // 基本订单信息
        markdown.append("## 订单基本信息\n\n");
        markdown.append("| 项目 | 信息 |\n");
        markdown.append("|------|------|\n");
        markdown.append(String.format("| 订单编号 | %s |\n", order.getOrderNo()));
        markdown.append(String.format("| 订单状态 | %s |\n", getOrderStatusText(order.getStatus())));
        markdown.append(String.format("| 订单类型 | %s |\n", getOrderTypeText(order.getType())));
        markdown.append(String.format("| 下单时间 | %s |\n", DateUtil.formatDateTime(order.getCreateTime())));
        markdown.append(String.format("| 订单总金额 | ¥%.2f |\n", order.getTotalPrice()));
        markdown.append(String.format("| 实付金额 | ¥%.2f |\n", order.getPayPrice()));
        if (order.getCouponPrice() != null && order.getCouponPrice().compareTo(BigDecimal.ZERO) > 0) {
            markdown.append(String.format("| 优惠券抵扣 | ¥%.2f |\n", order.getCouponPrice()));
        }
        if (order.getUseIntegral() != null && order.getUseIntegral() > 0) {
            markdown.append(String.format("| 使用积分 | %d |\n", order.getUseIntegral()));
            markdown.append(String.format("| 积分抵扣金额 | ¥%.2f |\n", order.getIntegralPrice()));
        }
        
        markdown.append("\n");
        
        // 用户信息
        markdown.append("## 用户信息\n\n");
        markdown.append("| 项目 | 信息 |\n");
        markdown.append("|------|------|\n");
        markdown.append(String.format("| 用户ID | %d |\n", user.getId()));
        markdown.append(String.format("| 用户昵称 | %s |\n", StrUtil.isNotBlank(user.getNickname()) ? user.getNickname() : "未设置"));
        markdown.append(String.format("| 用户手机号 | %s |\n", StrUtil.isNotBlank(user.getPhone()) ? user.getPhone() : "未设置"));
        if (user.getIntegral() != null) {
            markdown.append(String.format("| 用户积分 | %d |\n", user.getIntegral()));
        }
        
        markdown.append("\n");
        
        // 收货地址信息（如果有）
        if (userAddress != null) {
            markdown.append("## 收货地址信息\n\n");
            markdown.append("| 项目 | 信息 |\n");
            markdown.append("|------|------|\n");
            markdown.append(String.format("| 收货人 | %s |\n", userAddress.getRealName()));
            markdown.append(String.format("| 联系电话 | %s |\n", userAddress.getPhone()));
            markdown.append(String.format("| 收货地址 | %s %s %s %s |\n", 
                StrUtil.isNotBlank(userAddress.getProvince()) ? userAddress.getProvince() : "",
                StrUtil.isNotBlank(userAddress.getCity()) ? userAddress.getCity() : "",
                StrUtil.isNotBlank(userAddress.getDistrict()) ? userAddress.getDistrict() : "",
                StrUtil.isNotBlank(userAddress.getDetail()) ? userAddress.getDetail() : ""));
            
            markdown.append("\n");
        }
        
        // 商户订单信息
        if (CollUtil.isNotEmpty(merchantOrderList)) {
            markdown.append("## 商户订单信息\n\n");
            for (int i = 0; i < merchantOrderList.size(); i++) {
                MerchantOrder merchantOrder = merchantOrderList.get(i);
                Merchant merchant = merchantMap.get(merchantOrder.getMerId());
                
                markdown.append(String.format("### 商户订单 %d\n\n", i + 1));
                markdown.append("| 项目 | 信息 |\n");
                markdown.append("|------|------|\n");
                markdown.append(String.format("| 商户订单号 | %s |\n", merchantOrder.getOrderNo()));
                if (merchant != null) {
                    markdown.append(String.format("| 商户名称 | %s |\n", merchant.getName()));
                }
                markdown.append(String.format("| 商户订单金额 | ¥%.2f |\n", merchantOrder.getTotalPrice()));
                markdown.append(String.format("| 商户实付金额 | ¥%.2f |\n", merchantOrder.getPayPrice()));
                markdown.append(String.format("| 配送方式 | %s |\n", getShippingTypeText(merchantOrder.getShippingType())));
                if (merchantOrder.getTotalPostage() != null && merchantOrder.getTotalPostage().compareTo(BigDecimal.ZERO) > 0) {
                    markdown.append(String.format("| 运费 | ¥%.2f |\n", merchantOrder.getTotalPostage()));
                }
                
                markdown.append("\n");
            }
        }
        
        // 订单商品详情
        if (CollUtil.isNotEmpty(orderDetailList)) {
            markdown.append("## 订单商品详情\n\n");
            markdown.append("| 商品名称 | 商品规格 | 单价 | 数量 | 小计 |\n");
            markdown.append("|----------|----------|------|------|------|\n");
            
            for (OrderDetail detail : orderDetailList) {
                Product product = productMap.get(detail.getProductId());
                String productName = product != null ? product.getName() : "未知商品";
                String attrValue = StrUtil.isNotBlank(detail.getSku()) ? detail.getSku() : "默认规格";
                
                markdown.append(String.format("| %s | %s | ¥%.2f | %d | ¥%.2f |\n",
                    productName, attrValue, detail.getPayPrice(), detail.getPayNum(),
                    detail.getPayPrice().multiply(BigDecimal.valueOf(detail.getPayNum()))));
            }
            
            markdown.append("\n");
        }
        
        // 订单时间信息
        markdown.append("## 订单时间记录\n\n");
        markdown.append("| 项目 | 时间 |\n");
        markdown.append("|------|------|\n");
        markdown.append(String.format("| 创建时间 | %s |\n", DateUtil.formatDateTime(order.getCreateTime())));
        if (order.getPayTime() != null) {
            markdown.append(String.format("| 支付时间 | %s |\n", DateUtil.formatDateTime(order.getPayTime())));
        }
        if (order.getReceivingTime() != null) {
            markdown.append(String.format("| 收货时间 | %s |\n", DateUtil.formatDateTime(order.getReceivingTime())));
        }
        
        markdown.append("\n");
        
        // 生成时间戳
        markdown.append("---\n");
        markdown.append(String.format("*本订单信息生成于 %s*\n", DateUtil.formatDateTime(DateUtil.date())));
        
        return markdown.toString();
    }
    
    /**
     * 生成订单Markdown文件名
     * 
     * @param orderNo 订单号
     * @return 文件名
     */
    public static String generateFileName(String orderNo) {
        return String.format("order_%s_%s.md", orderNo, DateUtil.format(DateUtil.date(), "yyyyMMdd_HHmmss"));
    }
    
    /**
     * 获取订单状态文本
     */
    private static String getOrderStatusText(Integer status) {
        if (status == null) return "待支付";
        switch (status) {
            case 0: return "待支付";
            case 1: return "待发货";
            case 2: return "部分发货";
            case 3: return "待核销";
            case 4: return "待收货";
            case 5: return "已收货";
            case 6: return "已完成";
            case 9: return "已取消";
            default: return "待支付";
        }
    }
    
    /**
     * 获取订单类型文本
     */
    private static String getOrderTypeText(Integer type) {
        if (type == null) return "普通订单";
        switch (type) {
            case 0: return "普通订单";
            case 1: return "秒杀订单";
            case 2: return "拼团订单";
            default: return "普通订单";
        }
    }
    
    /**
     * 获取配送方式文本
     */
    private static String getShippingTypeText(Integer shippingType) {
        if (shippingType == null) return "快递配送";
        switch (shippingType) {
            case 1: return "快递配送";
            case 2: return "到店自提";
            case 3: return "同城配送";
            default: return "快递配送";
        }
    }
}