package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.invoice.Invoice;
import com.zbkj.common.model.order.Order;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.merchant.Merchant;
import com.zbkj.common.model.admin.SystemAdmin;
import com.zbkj.common.request.InvoiceAuditRequest;
import com.zbkj.common.request.InvoiceSearchRequest;
import com.zbkj.common.request.MerchantInvoiceMergeRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.InvoiceCountResponse;
import com.zbkj.common.response.InvoiceDetailResponse;
import com.zbkj.common.response.InvoicePageResponse;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.service.dao.InvoiceDao;
import com.zbkj.service.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 发票服务实现类
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
@Slf4j
@Service
public class InvoiceServiceImpl extends ServiceImpl<InvoiceDao, Invoice> implements InvoiceService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private SystemAdminService systemAdminService;
    @Autowired
    private InvoiceDao dao;

    /**
     * 发票分页列表
     */
    @Override
    public PageInfo<InvoicePageResponse> getPageList(InvoiceSearchRequest request) {
        // 设置分页参数
        PageHelper.startPage(request.getPageParamRequest().getPage(), request.getPageParamRequest().getLimit());
        
        // 查询发票列表
        List<InvoicePageResponse> list = dao.selectInvoicePageList(request);
        
        // 填充状态文本和其他信息
        if (CollUtil.isNotEmpty(list)) {
            fillInvoicePageResponseInfo(list);
        }
        
        return new PageInfo<>(list);
    }

    /**
     * 获取发票各状态数量统计
     */
    @Override
    public InvoiceCountResponse getInvoiceCount(InvoiceSearchRequest request) {
        InvoiceCountResponse response = new InvoiceCountResponse();
        
        // 全部发票数量
        InvoiceSearchRequest allRequest = new InvoiceSearchRequest();
        BeanUtils.copyProperties(request, allRequest);
        allRequest.setStatus(null);
        allRequest.setOrderStatus(null);
        response.setAllCount(dao.getInvoiceCountByStatus(allRequest, null));
        
        // 待付款订单发票数量
        response.setUnpaidCount(dao.getInvoiceCountByStatus(request, 0));
        
        // 待审核发票数量
        InvoiceSearchRequest auditRequest = new InvoiceSearchRequest();
        BeanUtils.copyProperties(request, auditRequest);
        auditRequest.setAuditStatus(0);
        response.setPendingAuditCount(dao.getInvoiceCountByStatus(auditRequest, null));
        
        // 待发货订单发票数量
        response.setPendingShipmentCount(dao.getInvoiceCountByStatus(request, 1));
        
        // 交易完成发票数量
        response.setCompletedCount(dao.getInvoiceCountByStatus(request, 6));
        
        // 已退款订单发票数量
        response.setRefundedCount(dao.getInvoiceCountByStatus(request, 9));
        
        // 已删除发票数量（暂时设为0，可根据实际需求实现）
        response.setDeletedCount(0);
        
        return response;
    }

    /**
     * 获取发票详情
     */
    @Override
    public InvoiceDetailResponse getInvoiceDetail(Integer invoiceId) {
        Invoice invoice = getById(invoiceId);
        if (ObjectUtil.isNull(invoice)) {
            throw new RuntimeException("发票不存在");
        }
        
        return buildInvoiceDetailResponse(invoice);
    }

    /**
     * 根据发票申请单号获取发票详情
     */
    @Override
    public InvoiceDetailResponse getInvoiceDetailByNo(String invoiceNo) {
        Invoice invoice = getByInvoiceNo(invoiceNo);
        if (ObjectUtil.isNull(invoice)) {
            throw new RuntimeException("发票不存在");
        }
        
        return buildInvoiceDetailResponse(invoice);
    }

    /**
     * 发票审核
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean auditInvoice(InvoiceAuditRequest request, Integer auditorId) {
        Invoice invoice = getById(request.getInvoiceId());
        if (ObjectUtil.isNull(invoice)) {
            throw new RuntimeException("发票不存在");
        }
        
        if (!invoice.getAuditStatus().equals(0)) {
            throw new RuntimeException("发票已审核，无法重复审核");
        }
        
        // 更新发票信息
        Invoice updateInvoice = new Invoice();
        updateInvoice.setId(request.getInvoiceId());
        updateInvoice.setAuditStatus(request.getAuditStatus());
        updateInvoice.setAuditorId(auditorId);
        updateInvoice.setAuditTime(new Date());
        updateInvoice.setAuditRemark(request.getAuditRemark());
        
        // 如果审核通过，更新开票信息
        if (request.getAuditStatus().equals(1)) {
            updateInvoice.setStatus(1); // 已开票
            updateInvoice.setInvoiceCode(request.getInvoiceCode());
            updateInvoice.setInvoiceDate(new Date());
            updateInvoice.setRemark(request.getRemark());
            updateInvoice.setInvoiceFileUrl(request.getInvoiceFileUrl());
            updateInvoice.setInvoiceFileName(request.getInvoiceFileName());
            
            // 更新订单发票状态
            Order order = orderService.getByOrderNo(invoice.getOrderNo());
            if (ObjectUtil.isNotNull(order)) {
                Order updateOrder = new Order();
                updateOrder.setId(order.getId());
                updateOrder.setInvoiceStatus(2); // 已开票
                orderService.updateById(updateOrder);
            }
        }
        
        return updateById(updateInvoice);
    }

    /**
     * 根据订单号获取发票信息
     */
    @Override
    public Invoice getByOrderNo(String orderNo) {
        return dao.getByOrderNo(orderNo);
    }

    /**
     * 根据发票申请单号获取发票信息
     */
    @Override
    public Invoice getByInvoiceNo(String invoiceNo) {
        return dao.getByInvoiceNo(invoiceNo);
    }

    /**
     * 生成发票申请单号
     */
    @Override
    public String generateInvoiceNo() {
        return "PT" + System.currentTimeMillis() + CrmebUtil.randomCount(9999999, 100000);
    }

    /**
     * 删除发票（逻辑删除）
     */
    @Override
    public Boolean deleteInvoice(Integer invoiceId) {
        Invoice invoice = getById(invoiceId);
        if (ObjectUtil.isNull(invoice)) {
            throw new RuntimeException("发票不存在");
        }
        
        Invoice updateInvoice = new Invoice();
        updateInvoice.setId(invoiceId);
        updateInvoice.setIsDel(true);
        
        return updateById(updateInvoice);
    }

    /**
     * 批量删除发票（逻辑删除）
     */
    @Override
    public Boolean batchDeleteInvoice(String invoiceIds) {
        if (StrUtil.isBlank(invoiceIds)) {
            throw new RuntimeException("发票ID不能为空");
        }
        
        List<Integer> idList = CrmebUtil.stringToArray(invoiceIds);
        if (CollUtil.isEmpty(idList)) {
            throw new RuntimeException("发票ID格式错误");
        }
        
        List<Invoice> updateList = idList.stream().map(id -> {
            Invoice invoice = new Invoice();
            invoice.setId(id);
            invoice.setIsDel(true);
            return invoice;
        }).collect(Collectors.toList());
        
        return updateBatchById(updateList);
    }

    /**
     * 填充发票分页响应信息
     */
    private void fillInvoicePageResponseInfo(List<InvoicePageResponse> list) {
        for (InvoicePageResponse response : list) {
            // 发票类型文本
            response.setInvoiceTypeText(response.getInvoiceType() == 1 ? "普通发票" : "专用发票");
            
            // 发票抬头类型文本
            response.setInvoiceTitleTypeText(response.getInvoiceTitleType() == 1 ? "个人" : "企业");
            
            // 开票状态文本
            String statusText = "";
            switch (response.getStatus()) {
                case 0: statusText = "待开票"; break;
                case 1: statusText = "已开票"; break;
                case 2: statusText = "开票失败"; break;
                default: statusText = "未知"; break;
            }
            response.setStatusText(statusText);
            
            // 审核状态文本
            String auditStatusText = "";
            switch (response.getAuditStatus()) {
                case 0: auditStatusText = "待审核"; break;
                case 1: auditStatusText = "审核通过"; break;
                case 2: auditStatusText = "审核拒绝"; break;
                default: auditStatusText = "未知"; break;
            }
            response.setAuditStatusText(auditStatusText);
            
            // 订单状态文本
            String orderStatusText = "";
            switch (response.getOrderStatus()) {
                case 0: orderStatusText = "待支付"; break;
                case 1: orderStatusText = "待发货"; break;
                case 2: orderStatusText = "部分发货"; break;
                case 3: orderStatusText = "待核销"; break;
                case 4: orderStatusText = "待收货"; break;
                case 5: orderStatusText = "已收货"; break;
                case 6: orderStatusText = "已完成"; break;
                case 9: orderStatusText = "已取消"; break;
                default: orderStatusText = "未知"; break;
            }
            response.setOrderStatusText(orderStatusText);
        }
    }

    /**
     * 构建发票详情响应
     */
    private InvoiceDetailResponse buildInvoiceDetailResponse(Invoice invoice) {
        InvoiceDetailResponse response = new InvoiceDetailResponse();
        BeanUtils.copyProperties(invoice, response);
        
        // 获取订单信息
        Order order = orderService.getByOrderNo(invoice.getOrderNo());
        if (ObjectUtil.isNotNull(order)) {
            response.setOrderAmount(order.getPayPrice());
            response.setOrderStatus(order.getStatus());
            response.setPayTime(order.getPayTime());
            response.setPayType(order.getPayType());
        }
        
        // 获取用户信息
        User user = userService.getById(invoice.getUid());
        if (ObjectUtil.isNotNull(user)) {
            response.setUserAccount(user.getAccount());
            response.setNickname(user.getNickname());
            response.setUserPhone(user.getPhone());
        }
        
        // 获取商户信息
        Merchant merchant = merchantService.getById(invoice.getMerId());
        if (ObjectUtil.isNotNull(merchant)) {
            response.setMerchantName(merchant.getName());
        }
        
        // 获取审核人信息
        if (ObjectUtil.isNotNull(invoice.getAuditorId())) {
            SystemAdmin auditor = systemAdminService.getById(invoice.getAuditorId());
            if (ObjectUtil.isNotNull(auditor)) {
                response.setAuditorName(auditor.getRealName());
            }
        }
        
        // 填充状态文本
        response.setInvoiceTypeText(invoice.getInvoiceType() == 1 ? "普通发票" : "专用发票");
        response.setInvoiceTitleTypeText(invoice.getInvoiceTitleType() == 1 ? "个人" : "企业");
        
        String statusText = "";
        switch (invoice.getStatus()) {
            case 0: statusText = "待开票"; break;
            case 1: statusText = "已开票"; break;
            case 2: statusText = "开票失败"; break;
            default: statusText = "未知"; break;
        }
        response.setStatusText(statusText);
        
        String auditStatusText = "";
        switch (invoice.getAuditStatus()) {
            case 0: auditStatusText = "待审核"; break;
            case 1: auditStatusText = "审核通过"; break;
            case 2: auditStatusText = "审核拒绝"; break;
            default: auditStatusText = "未知"; break;
        }
        response.setAuditStatusText(auditStatusText);
        
        if (ObjectUtil.isNotNull(order)) {
            String orderStatusText = "";
            switch (order.getStatus()) {
                case 0: orderStatusText = "待支付"; break;
                case 1: orderStatusText = "待发货"; break;
                case 2: orderStatusText = "部分发货"; break;
                case 3: orderStatusText = "待核销"; break;
                case 4: orderStatusText = "待收货"; break;
                case 5: orderStatusText = "已收货"; break;
                case 6: orderStatusText = "已完成"; break;
                case 9: orderStatusText = "已取消"; break;
                default: orderStatusText = "未知"; break;
            }
            response.setOrderStatusText(orderStatusText);
        }
        
        return response;
    }

    /**
     * 商户端发票分页列表
     */
    @Override
    public PageInfo<InvoicePageResponse> getMerchantPage(InvoiceSearchRequest request, PageParamRequest pageParamRequest) {
        // 获取当前商户ID
        Integer merId = SecurityUtil.getLoginUserVo().getUser().getMerId();
        request.setMerId(merId);
        
        // 设置分页参数
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        
        // 查询发票列表
        List<InvoicePageResponse> list = dao.selectInvoicePageList(request);
        
        // 填充状态文本和其他信息
        if (CollUtil.isNotEmpty(list)) {
            fillInvoicePageResponseInfo(list);
        }
        
        return new PageInfo<>(list);
    }

    /**
     * 商户端发票数量统计
     */
    @Override
    public InvoiceCountResponse getMerchantCount(InvoiceSearchRequest request) {
        // 获取当前商户ID
        Integer merId = SecurityUtil.getLoginUserVo().getUser().getMerId();
        request.setMerId(merId);
        
        return getInvoiceCount(request);
    }

    /**
     * 商户端发票详情
     */
    @Override
    public InvoiceDetailResponse getMerchantDetail(Integer id) {
        // 获取当前商户ID
        Integer merId = SecurityUtil.getLoginUserVo().getUser().getMerId();
        
        Invoice invoice = getById(id);
        if (ObjectUtil.isNull(invoice)) {
            throw new RuntimeException("发票不存在");
        }
        
        // 验证发票是否属于当前商户
        if (!invoice.getMerId().equals(merId)) {
            throw new RuntimeException("无权访问该发票");
        }
        
        return buildInvoiceDetailResponse(invoice);
    }

    /**
     * 商户端发票审核
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean merchantAudit(InvoiceAuditRequest request) {
        // 获取当前商户ID和管理员ID
        Integer merId = SecurityUtil.getLoginUserVo().getUser().getMerId();
        Integer auditorId = SecurityUtil.getLoginUserVo().getUser().getId();
        
        Invoice invoice = getById(request.getInvoiceId());
        if (ObjectUtil.isNull(invoice)) {
            throw new RuntimeException("发票不存在");
        }
        
        // 验证发票是否属于当前商户
        if (!invoice.getMerId().equals(merId)) {
            throw new RuntimeException("无权操作该发票");
        }
        
        return auditInvoice(request, auditorId);
    }

    /**
     * 商户端合并开票
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean merchantMergeInvoice(MerchantInvoiceMergeRequest request) {
        // 获取当前商户ID
        Integer merId = SecurityUtil.getLoginUserVo().getUser().getMerId();
        
        if (CollUtil.isEmpty(request.getInvoiceIds()) || request.getInvoiceIds().size() < 1) {
            throw new RuntimeException("合并开票至少需要选择1张发票");
        }
        
        // 查询要合并的发票
        List<Invoice> invoiceList = listByIds(request.getInvoiceIds());
        if (invoiceList.size() != request.getInvoiceIds().size()) {
            throw new RuntimeException("部分发票不存在");
        }
        
        // 验证发票状态和权限
        for (Invoice invoice : invoiceList) {
            if (!invoice.getMerId().equals(merId)) {
                throw new RuntimeException("无权操作发票：" + invoice.getInvoiceNo());
            }
            if (!invoice.getAuditStatus().equals(0)) {
                throw new RuntimeException("发票已审核，无法合并：" + invoice.getInvoiceNo());
            }
            if (!invoice.getStatus().equals(0)) {
                throw new RuntimeException("发票已开票，无法合并：" + invoice.getInvoiceNo());
            }
        }
        
        // 直接更新发票状态，添加发票号和备注
        Integer auditorId = SecurityUtil.getLoginUserVo().getUser().getId();
        List<Invoice> updateList = invoiceList.stream().map(invoice -> {
            Invoice updateInvoice = new Invoice();
            updateInvoice.setId(invoice.getId());
            updateInvoice.setInvoiceCode(request.getInvoiceCode());
            updateInvoice.setRemark(request.getRemark());
            updateInvoice.setStatus(StrUtil.isNotBlank(request.getInvoiceCode()) ? 1 : 0); // 如果有发票号码则已开票
            updateInvoice.setAuditStatus(1); // 商户开票直接审核通过
            updateInvoice.setAuditorId(auditorId);
            updateInvoice.setAuditTime(new Date());
            updateInvoice.setUpdateTime(new Date());
            
            if (updateInvoice.getStatus().equals(1)) {
                updateInvoice.setInvoiceDate(new Date());
            }
            
            return updateInvoice;
        }).collect(Collectors.toList());
        
        // 批量更新发票状态
        boolean result = updateBatchById(updateList);
        
        // 如果已开票，需要更新相关订单的发票状态
        if (StrUtil.isNotBlank(request.getInvoiceCode())) {
            for (Invoice invoice : invoiceList) {
                if (StrUtil.isNotBlank(invoice.getOrderNo()) && !invoice.getOrderNo().startsWith("MERGE_")) {
                    Order order = orderService.getByOrderNo(invoice.getOrderNo());
                    if (ObjectUtil.isNotNull(order)) {
                        Order updateOrder = new Order();
                        updateOrder.setId(order.getId());
                        updateOrder.setInvoiceStatus(2); // 已开票
                        orderService.updateById(updateOrder);
                    }
                }
            }
        }
        
        return result;
    }

    /**
     * 商户端删除发票
     */
    @Override
    public Boolean merchantDelete(Integer id) {
        // 获取当前商户ID
        Integer merId = SecurityUtil.getLoginUserVo().getUser().getMerId();
        
        Invoice invoice = getById(id);
        if (ObjectUtil.isNull(invoice)) {
            throw new RuntimeException("发票不存在");
        }
        
        // 验证发票是否属于当前商户
        if (!invoice.getMerId().equals(merId)) {
            throw new RuntimeException("无权删除该发票");
        }
        
        return deleteInvoice(id);
    }
} 