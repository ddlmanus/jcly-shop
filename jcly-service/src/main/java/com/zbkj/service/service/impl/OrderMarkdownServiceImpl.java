package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.zbkj.common.constants.SysConfigConstants;
import com.zbkj.common.model.coze.CozeKnowledgeFile;
import com.zbkj.common.model.merchant.Merchant;
import com.zbkj.common.model.order.Order;
import com.zbkj.common.model.order.OrderDetail;
import com.zbkj.common.model.order.MerchantOrder;
import com.zbkj.common.model.product.Product;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserAddress;
import com.zbkj.service.service.*;
import com.zbkj.service.util.OrderMarkdownGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单Markdown文件服务实现类 - 支持上传到Coze知识库
 */
@Service
public class OrderMarkdownServiceImpl implements OrderMarkdownService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderMarkdownServiceImpl.class);
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Autowired
    private CozeKnowledgeFileService cozeKnowledgeFileService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserAddressService userAddressService;
    
    @Autowired
    private OrderDetailService orderDetailService;
    
    @Autowired
    private MerchantOrderService merchantOrderService;
    
    @Autowired
    private MerchantService merchantService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 生成并上传订单信息的Markdown文件到Coze知识库
     */
    @Override
    public String generateAndUploadOrderMarkdown(Order order) {
        if (order == null || order.getId() == null) {
            logger.warn("订单信息为空或订单ID为空，无法生成Markdown文件");
            return null;
        }
        
        try {
            // 0. 诊断环境配置（可选，在调试时启用）
            if (logger.isDebugEnabled()) {
                diagnoseConfiguration(order.getOrderNo());
            }
            
            // 1. 检查是否配置了Coze知识库ID
            String cozeKnowledgeId = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_COZE_ORDER_KNOWLEDGE_ID);
            if (StrUtil.isBlank(cozeKnowledgeId)) {
                logger.warn("未配置Coze订单知识库ID（配置项：{}），无法上传订单Markdown文件，订单号：{}", 
                    SysConfigConstants.CONFIG_COZE_ORDER_KNOWLEDGE_ID, order.getOrderNo());
                return null;
            }

            // 2. 收集订单相关信息
            User user = userService.getById(order.getUid());
            if (user == null) {
                logger.warn("获取用户信息失败，用户ID：{}", order.getUid());
                return null;
            }
            
            // 获取用户地址信息
            UserAddress userAddress = userAddressService.getDefault();
            
            // 获取订单详情列表
            List<OrderDetail> orderDetailList = null;
            try {
                orderDetailList = orderDetailService.getByOrderNo(order.getOrderNo());
            } catch (Exception e) {
                logger.error("获取订单详情失败，订单号：{}，错误：{}", order.getOrderNo(), e.getMessage());
                orderDetailList = null;
            }
            
            // 获取商户订单列表
            List<MerchantOrder> merchantOrderList = null;
            try {
                merchantOrderList = merchantOrderService.getByOrderNo(order.getOrderNo());
            } catch (Exception e) {
                logger.error("获取商户订单失败，订单号：{}，错误：{}", order.getOrderNo(), e.getMessage());
                merchantOrderList = null;
            }
            
            // 获取商户信息映射
            Map<Integer, Merchant> merchantMap = new HashMap<>();
            if (CollUtil.isNotEmpty(merchantOrderList)) {
                try {
                    List<Integer> merIdList = merchantOrderList.stream()
                        .map(MerchantOrder::getMerId)
                        .distinct()
                        .collect(Collectors.toList());
                    
                    List<Merchant> merchantList = merchantService.getByIdList(merIdList);
                    if (CollUtil.isNotEmpty(merchantList)) {
                        merchantMap = merchantList.stream()
                            .collect(Collectors.toMap(Merchant::getId, merchant -> merchant));
                    }
                } catch (Exception e) {
                    logger.error("获取商户信息失败，订单号：{}，错误：{}", order.getOrderNo(), e.getMessage());
                }
            }
            
            // 获取商品信息映射
            Map<Integer, Product> productMap = new HashMap<>();
            if (CollUtil.isNotEmpty(orderDetailList)) {
                try {
                    List<Integer> productIdList = orderDetailList.stream()
                        .map(OrderDetail::getProductId)
                        .distinct()
                        .collect(Collectors.toList());
                    
                    List<Product> productList = productService.getByIdList(productIdList);
                    if (CollUtil.isNotEmpty(productList)) {
                        productMap = productList.stream()
                            .collect(Collectors.toMap(Product::getId, product -> product));
                    }
                } catch (Exception e) {
                    logger.error("获取商品信息失败，订单号：{}，错误：{}", order.getOrderNo(), e.getMessage());
                }
            }
            
            // 3. 生成Markdown内容
            String markdownContent = OrderMarkdownGenerator.generateOrderMarkdown(
                order, user, userAddress, orderDetailList, merchantOrderList, merchantMap, productMap);
            
            if (StrUtil.isBlank(markdownContent)) {
                logger.error("生成的订单Markdown内容为空，订单号：{}", order.getOrderNo());
                return null;
            }
            
            // 4. 创建临时文件
            String fileName = OrderMarkdownGenerator.generateFileName(order.getOrderNo());
            File tempFile = createTempMarkdownFile(fileName, markdownContent);
            
            if (tempFile == null) {
                logger.error("创建临时订单Markdown文件失败，订单号：{}", order.getOrderNo());
                return null;
            }
            
            // 5. 上传到Coze知识库
            CozeKnowledgeFile cozeFile = null;
            try {
                cozeFile = uploadToCozeKnowledge(cozeKnowledgeId, tempFile, order.getMerId());
            } catch (Exception e) {
                logger.error("上传订单Markdown文件到Coze知识库异常，订单号：{}，知识库ID：{}，文件：{}，错误：{}", 
                    order.getOrderNo(), cozeKnowledgeId, tempFile.getName(), e.getMessage(), e);
                cozeFile = null;
            }
            
            // 6. 清理临时文件
            if (tempFile.exists()) {
                tempFile.delete();
            }
            
            if (cozeFile != null) {
                logger.info("订单Markdown文件上传到Coze知识库成功，订单号：{}，文件ID：{}", 
                    order.getOrderNo(), cozeFile.getCozeFileId());
                
                // 保存Coze文件ID到订单表
                try {
                    order.setCozeFileId(cozeFile.getCozeFileId());
                    orderService.updateById(order);
                    logger.info("订单Coze文件ID保存成功，订单号：{}，文件ID：{}", 
                        order.getOrderNo(), cozeFile.getCozeFileId());
                } catch (Exception e) {
                    logger.error("保存订单Coze文件ID失败，订单号：{}，文件ID：{}，错误：{}", 
                        order.getOrderNo(), cozeFile.getCozeFileId(), e.getMessage(), e);
                }
                
                // 返回Coze文件ID作为标识
                return cozeFile.getCozeFileId();
            } else {
                logger.error("订单Markdown文件上传到Coze知识库失败，订单号：{}", order.getOrderNo());
                return null;
            }
            
        } catch (Exception e) {
            logger.error("生成并上传订单Markdown文件到Coze知识库异常，订单号：{}", order.getOrderNo(), e);
            return null;
        }
    }
    
    /**
     * 删除订单的Markdown文件
     */
    @Override
    public Boolean deleteOrderMarkdown(String orderNo) {
        if (StrUtil.isBlank(orderNo)) {
            return false;
        }
        
        try {
            // 这里可以实现Coze知识库文件删除逻辑
            // 目前暂时返回true，后续可以根据需要实现具体的删除逻辑
            logger.info("订单Markdown文件删除请求，订单号：{}", orderNo);
            return true;
        } catch (Exception e) {
            logger.error("删除订单Markdown文件异常，订单号：{}", orderNo, e);
            return false;
        }
    }

    /**
     * 更新订单在Coze知识库中的Markdown文件
     * 当订单状态发生变更时调用此方法同步更新知识库中的订单信息
     */
    @Override
    public String updateOrderMarkdownInKnowledge(Order order) {
        if (order == null || order.getId() == null) {
            logger.warn("订单信息为空或订单ID为空，无法更新Markdown文件");
            return null;
        }

        logger.info("开始更新订单在Coze知识库中的Markdown文件，订单号：{}，当前文件ID：{}", 
            order.getOrderNo(), order.getCozeFileId());

        try {
            // 1. 检查是否配置了Coze知识库ID
            String cozeKnowledgeId = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_COZE_ORDER_KNOWLEDGE_ID);
            if (StrUtil.isBlank(cozeKnowledgeId)) {
                logger.warn("未配置Coze订单知识库ID，无法更新订单Markdown文件，订单号：{}", order.getOrderNo());
                return null;
            }

            // 2. 如果订单有旧的Coze文件ID，先尝试删除旧文件
            if (StrUtil.isNotBlank(order.getCozeFileId())) {
                try {
                    logger.info("删除订单旧的Coze知识库文件，订单号：{}，旧文件ID：{}", 
                        order.getOrderNo(), order.getCozeFileId());
                    cozeKnowledgeFileService.deleteByFileId(order.getCozeFileId(), order.getMerId());
                } catch (Exception e) {
                    logger.warn("删除订单旧文件失败，继续创建新文件，订单号：{}，旧文件ID：{}，错误：{}", 
                        order.getOrderNo(), order.getCozeFileId(), e.getMessage());
                }
            }

            // 3. 重新收集订单相关信息并生成新的Markdown文件
            User user = userService.getById(order.getUid());
            if (user == null) {
                logger.warn("获取用户信息失败，用户ID：{}", order.getUid());
                return null;
            }

            // 获取用户地址信息
            UserAddress userAddress = userAddressService.getDefault();

            // 获取订单详情列表
            List<OrderDetail> orderDetailList = null;
            try {
                orderDetailList = orderDetailService.getByOrderNo(order.getOrderNo());
            } catch (Exception e) {
                logger.error("获取订单详情失败，订单号：{}，错误：{}", order.getOrderNo(), e.getMessage());
                orderDetailList = null;
            }

            // 获取商户订单列表
            List<MerchantOrder> merchantOrderList = null;
            try {
                merchantOrderList = merchantOrderService.getByOrderNo(order.getOrderNo());
            } catch (Exception e) {
                logger.error("获取商户订单失败，订单号：{}，错误：{}", order.getOrderNo(), e.getMessage());
                merchantOrderList = null;
            }

            // 获取商户信息映射
            Map<Integer, Merchant> merchantMap = new HashMap<>();
            if (CollUtil.isNotEmpty(merchantOrderList)) {
                try {
                    List<Integer> merIdList = merchantOrderList.stream()
                        .map(MerchantOrder::getMerId)
                        .distinct()
                        .collect(Collectors.toList());

                    List<Merchant> merchantList = merchantService.getByIdList(merIdList);
                    if (CollUtil.isNotEmpty(merchantList)) {
                        merchantMap = merchantList.stream()
                            .collect(Collectors.toMap(Merchant::getId, merchant -> merchant));
                    }
                } catch (Exception e) {
                    logger.error("获取商户信息失败，订单号：{}，错误：{}", order.getOrderNo(), e.getMessage());
                }
            }

            // 获取商品信息映射
            Map<Integer, Product> productMap = new HashMap<>();
            if (CollUtil.isNotEmpty(orderDetailList)) {
                try {
                    List<Integer> productIdList = orderDetailList.stream()
                        .map(OrderDetail::getProductId)
                        .distinct()
                        .collect(Collectors.toList());

                    List<Product> productList = productService.getByIdList(productIdList);
                    if (CollUtil.isNotEmpty(productList)) {
                        productMap = productList.stream()
                            .collect(Collectors.toMap(Product::getId, product -> product));
                    }
                } catch (Exception e) {
                    logger.error("获取商品信息失败，订单号：{}，错误：{}", order.getOrderNo(), e.getMessage());
                }
            }

            // 4. 生成新的Markdown内容
            String markdownContent = OrderMarkdownGenerator.generateOrderMarkdown(
                order, user, userAddress, orderDetailList, merchantOrderList, merchantMap, productMap);

            if (StrUtil.isBlank(markdownContent)) {
                logger.error("生成的订单Markdown内容为空，订单号：{}", order.getOrderNo());
                return null;
            }

            // 5. 创建临时文件
            String fileName = OrderMarkdownGenerator.generateFileName(order.getOrderNo());
            File tempFile = createTempMarkdownFile(fileName, markdownContent);

            if (tempFile == null) {
                logger.error("创建临时订单Markdown文件失败，订单号：{}", order.getOrderNo());
                return null;
            }

            // 6. 上传新文件到Coze知识库
            CozeKnowledgeFile newCozeFile = null;
            try {
                newCozeFile = uploadToCozeKnowledge(cozeKnowledgeId, tempFile, order.getMerId());
            } catch (Exception e) {
                logger.error("更新订单Markdown文件到Coze知识库异常，订单号：{}，错误：{}", 
                    order.getOrderNo(), e.getMessage(), e);
                newCozeFile = null;
            }

            // 7. 清理临时文件
            if (tempFile.exists()) {
                tempFile.delete();
            }

            if (newCozeFile != null) {
                logger.info("订单Markdown文件更新到Coze知识库成功，订单号：{}，新文件ID：{}", 
                    order.getOrderNo(), newCozeFile.getCozeFileId());

                // 8. 更新订单表中的Coze文件ID
                try {
                    order.setCozeFileId(newCozeFile.getCozeFileId());
                    orderService.updateById(order);
                    logger.info("订单Coze文件ID更新成功，订单号：{}，新文件ID：{}", 
                        order.getOrderNo(), newCozeFile.getCozeFileId());
                } catch (Exception e) {
                    logger.error("更新订单Coze文件ID失败，订单号：{}，新文件ID：{}，错误：{}", 
                        order.getOrderNo(), newCozeFile.getCozeFileId(), e.getMessage(), e);
                }

                return newCozeFile.getCozeFileId();
            } else {
                logger.error("订单Markdown文件更新到Coze知识库失败，订单号：{}", order.getOrderNo());
                return null;
            }

        } catch (Exception e) {
            logger.error("更新订单Markdown文件到Coze知识库异常，订单号：{}", order.getOrderNo(), e);
            return null;
        }
    }

    /**
     * 根据订单号更新知识库中的Markdown文件
     */
    @Override
    public Boolean updateOrderMarkdownByOrderNo(String orderNo) {
        if (StrUtil.isBlank(orderNo)) {
            logger.warn("订单号为空，无法更新知识库文件");
            return false;
        }

        try {
            // 根据订单号获取订单信息
            Order order = orderService.getByOrderNo(orderNo);
            if (order == null) {
                logger.warn("根据订单号获取订单信息失败，订单号：{}", orderNo);
                return false;
            }

            // 调用更新方法
            String newCozeFileId = updateOrderMarkdownInKnowledge(order);
            return StrUtil.isNotBlank(newCozeFileId);

        } catch (Exception e) {
            logger.error("根据订单号更新知识库文件异常，订单号：{}", orderNo, e);
            return false;
        }
    }
    
    /**
     * 诊断订单Markdown生成环境配置
     */
    private void diagnoseConfiguration(String orderNo) {
        logger.info("开始诊断订单Markdown生成环境配置，订单号：{}", orderNo);
        
        // 检查Coze知识库配置
        String cozeKnowledgeId = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_COZE_ORDER_KNOWLEDGE_ID);
        logger.info("Coze订单知识库ID配置：{}", StrUtil.isNotBlank(cozeKnowledgeId) ? cozeKnowledgeId : "未配置");
        
        // 检查服务注入情况
        logger.info("服务注入检查 - cozeKnowledgeFileService: {}, userService: {}, userAddressService: {}", 
            cozeKnowledgeFileService != null ? "正常" : "失败",
            userService != null ? "正常" : "失败", 
            userAddressService != null ? "正常" : "失败");
        
        // 检查临时目录
        String tempDir = System.getProperty("java.io.tmpdir");
        logger.info("系统临时目录：{}", tempDir);
        
        java.io.File tempDirFile = new java.io.File(tempDir);
        logger.info("临时目录状态 - 存在: {}, 可写: {}, 可读: {}", 
            tempDirFile.exists(), tempDirFile.canWrite(), tempDirFile.canRead());
    }
    
    /**
     * 创建临时Markdown文件
     */
    private File createTempMarkdownFile(String fileName, String content) {
        try {
            // 创建临时目录
            String tempDir = System.getProperty("java.io.tmpdir");
            File tempFile = new File(tempDir, fileName);
            
            // 写入内容
            try (FileWriter writer = new FileWriter(tempFile, false)) {
                writer.write(content);
                writer.flush();
            }
            
            logger.debug("临时订单Markdown文件创建成功：{}", tempFile.getAbsolutePath());
            return tempFile;
            
        } catch (IOException e) {
            logger.error("创建临时订单Markdown文件失败：{}", fileName, e);
            return null;
        }
    }
    
    /**
     * 上传文件到Coze知识库
     */
    private CozeKnowledgeFile uploadToCozeKnowledge(String cozeKnowledgeId, File file, Integer merchantId) {
        try {
            logger.info("开始上传订单Markdown文件到Coze知识库：{}，文件：{}", cozeKnowledgeId, file.getName());
            
            // 使用CozeKnowledgeFileService上传本地文件
            CozeKnowledgeFile cozeFile = cozeKnowledgeFileService.uploadLocalFileToKnowledge(
                cozeKnowledgeId, file, merchantId);
            
            if (cozeFile != null) {
                logger.info("订单Markdown文件上传到Coze知识库成功：文件ID={}，文件名={}", 
                    cozeFile.getCozeFileId(), cozeFile.getFileName());
            } else {
                logger.error("订单Markdown文件上传到Coze知识库失败，返回的文件对象为null");
            }
            
            return cozeFile;
            
        } catch (Exception e) {
            logger.error("上传订单Markdown文件到Coze知识库失败：{}", file.getName(), e);
            return null;
        }
    }
}