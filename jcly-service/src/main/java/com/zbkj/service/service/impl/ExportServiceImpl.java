package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alipay.api.domain.AlipayInsCooperationRegionQrcodeApplyModel;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.util.StringUtil;
import com.zbkj.common.config.CrmebConfig;
import com.zbkj.common.constants.DateConstants;
import com.zbkj.common.constants.ProductConstants;
import com.zbkj.common.constants.UploadConstants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.admin.SystemAdmin;
import com.zbkj.common.model.city.CityRegion;
import com.zbkj.common.model.coupon.Coupon;
import com.zbkj.common.model.merchant.Merchant;
import com.zbkj.common.model.order.MerchantOrder;
import com.zbkj.common.model.order.Order;
import com.zbkj.common.model.order.OrderDetail;
import com.zbkj.common.model.product.Product;
import com.zbkj.common.model.product.ProductCategory;
import com.zbkj.common.model.product.ProductGuarantee;
import com.zbkj.common.model.user.User;
import com.zbkj.common.request.*;
import com.zbkj.common.response.*;
import com.zbkj.common.result.CommonResultCode;
import com.zbkj.common.utils.*;
import com.zbkj.common.vo.*;
import com.zbkj.service.service.*;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zbkj.common.model.product.ProductBrand;
import com.zbkj.common.model.merchant.MerchantProductCategory;
import com.zbkj.common.model.express.ShippingTemplates;
import com.zbkj.common.model.system.SystemAttachment;
import com.zbkj.common.constants.SysConfigConstants;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;

import java.io.FileOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFShape;

import java.util.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.OutputStream;
import com.alibaba.fastjson.JSON;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;

/**
 * ExcelServiceImpl 接口实现
 * +----------------------------------------------------------------------
 * | JCLY [ JCLY赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2022 https://www.ddlmanus.xyz All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 * +----------------------------------------------------------------------
 * | Author: dudl
 * +----------------------------------------------------------------------
 */
@Service
public class ExportServiceImpl implements ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportServiceImpl.class);

    // 批量导入线程池配置
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors(); // CPU核心数
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2; // 最大线程数
    private static final int BATCH_SIZE = 50; // 每批处理的商品数量
    private static final int QUEUE_CAPACITY = 1000; // 队列容量

    // 用于跟踪图片索引
    private int pictureIndex = 0;

    // 存储异步导入任务状态和结果的Map
    private static final ConcurrentHashMap<String, Map<String, Object>> IMPORT_TASK_STATUS_MAP = new ConcurrentHashMap<>();

    @Autowired
    private OrderService orderService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisUtil redisUtil;


    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private CrmebConfig crmebConfig;
    @Autowired
    private MerchantOrderService merchantOrderService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductCategoryService productCategoryService;
    @Autowired
    private UploadService uploadService;
    @Autowired
    private ProductBrandService productBrandService;
    @Autowired
    private ProductGuaranteeService productGuaranteeService;
    @Autowired
    private CouponService couponService;
    @Autowired
    private SystemFormService systemFormService;
    @Autowired
    private CityRegionService cityRegionService;
    @Autowired
    private MerchantProductCategoryService merchantProductCategoryService;
    @Autowired
    private ShippingTemplatesService shippingTemplatesService;
    @Autowired
    private SystemAttachmentService systemAttachmentService;
    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private QiNiuService qiNiuService;
    @Autowired
    private OssService ossService;
    @Autowired
    private CosService cosService;
    @Autowired
    private JdCloudService jdCloudService;
    @Autowired
    private AsyncService asyncService;

    /**
     * 订单导出
     *
     * @param request 查询条件
     * @return 文件名称
     */
    @Override
    public String exportOrder(OrderSearchRequest request) {
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        if (systemAdmin.getMerId() > 0) {
            request.setMerId(systemAdmin.getMerId());
        }
        List<Order> orderList = orderService.findExportList(request);
        if (CollUtil.isEmpty(orderList)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "没有可导出的数据！");
        }

        List<Integer> merIdList = orderList.stream().filter(e -> e.getMerId() > 0).map(Order::getMerId).distinct().collect(Collectors.toList());
        List<Integer> userIdList = orderList.stream().map(Order::getUid).distinct().collect(Collectors.toList());
        List<String> orderNoList = orderList.stream().map(Order::getOrderNo).distinct().collect(Collectors.toList());
        Map<Integer, Merchant> merchantMap = merchantService.getMapByIdList(merIdList);
        Map<Integer, User> userMap = userService.getUidMapList(userIdList);
        Map<String, List<OrderDetail>> orderDetailMap = orderDetailService.getMapByOrderNoList(orderNoList);
        Map<String, List<MerchantOrder>> merchantOrderMap = merchantOrderService.getMapByOrderNoList(orderNoList);

        // 准备导出数据
        List<List<String>> exportData = new ArrayList<>();

        // 添加表头
        List<String> headers = Arrays.asList(
                "订单类型", "订单号", "商户名称", "用户昵称", "实际支付金额", "支付状态",
                "支付方式", "支付渠道", "订单状态", "退款状态", "创建时间", "商品信息",
                "收货人", "收货电话", "收货地址", "用户备注", "商户备注"
        );
        exportData.add(headers);

        // 添加数据行
        for (Order order : orderList) {
            List<String> rowData = new ArrayList<>();
            rowData.add(getOrderType(order.getType()));
            rowData.add(order.getOrderNo());
            rowData.add(order.getMerId() > 0 ? merchantMap.get(order.getMerId()).getName() : "");
            if(!Objects.isNull(userMap.get(order.getUid()))){
                rowData.add(userMap.get(order.getUid()).getNickname() + "|" + order.getUid());
            }else{
                rowData.add("");
            }
            rowData.add(order.getPayPrice().toString());
            rowData.add(order.getPaid() ? "已支付" : "未支付");
            rowData.add(getOrderPayType(order.getPayType()));
            rowData.add(getOrderPayChannel(order.getPayChannel()));
            rowData.add(getOrderStatus(order.getStatus(), order.getType(), order.getGroupBuyRecordStatus()));
            rowData.add(getOrderRefundStatus(order.getRefundStatus()));
            rowData.add(CrmebDateUtil.dateToStr(order.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            rowData.add(getOrderProductInfo(orderDetailMap.get(order.getOrderNo())));
            rowData.add(StrUtil.isBlank(merchantOrderMap.get(order.getOrderNo()).get(0).getRealName()) ? "" : merchantOrderMap.get(order.getOrderNo()).get(0).getRealName());
            rowData.add(StrUtil.isBlank(merchantOrderMap.get(order.getOrderNo()).get(0).getUserPhone()) ? "" : merchantOrderMap.get(order.getOrderNo()).get(0).getUserPhone());
            rowData.add(StrUtil.isBlank(merchantOrderMap.get(order.getOrderNo()).get(0).getUserAddress()) ? "" : merchantOrderMap.get(order.getOrderNo()).get(0).getUserAddress());
            rowData.add(StrUtil.isBlank(merchantOrderMap.get(order.getOrderNo()).get(0).getUserRemark()) ? "" : merchantOrderMap.get(order.getOrderNo()).get(0).getUserRemark());
            rowData.add(StrUtil.isBlank(merchantOrderMap.get(order.getOrderNo()).get(0).getMerchantRemark()) ? "" : merchantOrderMap.get(order.getOrderNo()).get(0).getMerchantRemark());
            exportData.add(rowData);
        }

        // 服务器存储地址
        String rootPath = crmebConfig.getImagePath().trim();
        // 模块
        String modelPath = "public/export/";
        // 类型
        String type = UploadConstants.UPLOAD_AFTER_FILE_KEYWORD + "/";

        // 变更文件名
        String newFileName = "订单列表导出_" + CrmebDateUtil.nowDateTime(DateConstants.DATE_TIME_FORMAT_NUM) + ".xlsx";
        // 创建目标文件的名称，规则：类型/模块/年/月/日/文件名
        String webPath = type + modelPath + CrmebDateUtil.nowDate("yyyy/MM/dd") + "/";
        // 文件分隔符转化为当前系统的格式
        String destPath = FilenameUtils.separatorsToSystem(rootPath + webPath) + newFileName;

        File file = null;
        try {
            // 创建本地文件
            file = UploadUtil.createFile(destPath);

            // 使用POI创建带样式的Excel
            createStyledOrderExcel(file, exportData, "订单数据导出");

            // 创建附件记录
            SystemAttachment systemAttachment = new SystemAttachment();
            systemAttachment.setName(newFileName);
            systemAttachment.setSattDir(webPath + newFileName);
            systemAttachment.setAttSize(String.valueOf(file.length()));
            systemAttachment.setAttType("xlsx");
            systemAttachment.setImageType(1); // 默认本地
            systemAttachment.setPid(0);
            systemAttachment.setOwner(-1); // 平台文件

            // 获取上传类型配置
            String uploadType = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_UPLOAD_TYPE);
            Integer uploadTypeInt = Integer.parseInt(uploadType);

            if (uploadTypeInt.equals(1)) {
                // 本地存储
                systemAttachmentService.save(systemAttachment);
                return systemAttachmentService.prefixFile(systemAttachment.getSattDir());
            }

            // 云存储处理
            CloudVo cloudVo = new CloudVo();
            String fileIsSave = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_FILE_IS_SAVE);

            switch (uploadTypeInt) {
                case 2: // 七牛云
                    systemAttachment.setImageType(2);
                    cloudVo.setDomain(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_UPLOAD_URL));
                    cloudVo.setAccessKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_ACCESS_KEY));
                    cloudVo.setSecretKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_SECRET_KEY));
                    cloudVo.setBucketName(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_STORAGE_NAME));
                    cloudVo.setRegion(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_STORAGE_REGION));
                    try {
                        Configuration cfg = new Configuration(Region.autoRegion());
                        UploadManager uploadManager = new UploadManager(cfg);
                        Auth auth = Auth.create(cloudVo.getAccessKey(), cloudVo.getSecretKey());
                        String upToken = auth.uploadToken(cloudVo.getBucketName());
                        qiNiuService.uploadFile(uploadManager, upToken, systemAttachment.getSattDir(), destPath, file);
                    } catch (Exception e) {
                        logger.error("七牛云上传失败：" + e.getMessage());
                    }
                    break;
                case 3: // 阿里云OSS
                    systemAttachment.setImageType(3);
                    cloudVo.setDomain(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_UPLOAD_URL));
                    cloudVo.setAccessKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_ACCESS_KEY));
                    cloudVo.setSecretKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_SECRET_KEY));
                    cloudVo.setBucketName(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_STORAGE_NAME));
                    cloudVo.setRegion(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_STORAGE_REGION));
                    try {
                        ossService.upload(cloudVo, systemAttachment.getSattDir(), destPath, file);
                    } catch (Exception e) {
                        logger.error("阿里云OSS上传失败：" + e.getMessage());
                    }
                    break;
                case 5: // 京东云
                    systemAttachment.setImageType(5);
                    String bucket = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_JD_BUCKET_NAME);
                    try {
                        jdCloudService.uploadFile(systemAttachment.getSattDir(), destPath, bucket);
                    } catch (Exception e) {
                        logger.error("京东云上传失败：" + e.getMessage());
                    }
                    break;
            }

            // 保存附件记录
            systemAttachmentService.save(systemAttachment);

            // 如果不保存本地文件，删除本地文件
            if (!fileIsSave.equals("1") && file != null) {
                file.delete();
            }

            // 返回文件访问URL
            return systemAttachmentService.prefixFile(systemAttachment.getSattDir());

        } catch (Exception e) {
            logger.error("生成订单导出文件失败", e);
            throw new CrmebException("生成订单导出文件失败：" + e.getMessage());
        }
    }

    private String getOrderProductInfo(List<OrderDetail> orderDetails) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < orderDetails.size(); i++) {
            OrderDetail orderDetail = orderDetails.get(i);
            stringBuilder.append(StrUtil.format("{}  {} * {}", orderDetail.getProductName(), orderDetail.getPayPrice(), orderDetail.getPayNum()));
            if ((i + 1) < orderDetails.size()) {
                stringBuilder.append("\r\n");
            }
        }
        return stringBuilder.toString();
    }

    private String getOrderType(Integer type) {
        String typeStr = "";
        switch (type) {
            case 0:
                typeStr = "普通";
                break;
            case 1:
                typeStr = "秒杀";
                break;
            case 2:
                typeStr = "拼团";
                break;
            // 视频号订单待定
        }
        return typeStr;
    }

    private String getOrderRefundStatus(Integer refundStatus) {
        String refundStatusStr = "";
        switch (refundStatus) {
            case 0:
                refundStatusStr = "未退款";
                break;
            case 1:
                refundStatusStr = "申请中";
                break;
            case 2:
                refundStatusStr = "部分退款";
                break;
            case 3:
                refundStatusStr = "已退款";
                break;
        }
        return refundStatusStr;
    }

    private String getOrderStatus(Integer status, Integer type, Integer groupBuyRecordStatus) {
        String statusStr = "";
        
        // 特殊处理拼团订单
        if (type != null && type == 2 && groupBuyRecordStatus != null) {
            // 拼团订单的状态处理
            if (groupBuyRecordStatus == 0 && status == 1) {
                // 拼团进行中且订单状态为待发货，显示为拼团中
                return "拼团中";
            } else if (groupBuyRecordStatus == -1) {
                // 拼团失败
                return "拼团失败";
            }
            // 拼团成功后按正常订单状态显示
        }
        
        // 正常订单状态处理
        switch (status) {
            case 0:
                statusStr = "待支付";
                break;
            case 1:
                statusStr = "待发货";
                break;
            case 2:
                statusStr = "部分发货";
                break;
            case 3:
                statusStr = "待核销";
                break;
            case 4:
                statusStr = "待收货";
                break;
            case 5:
                statusStr = "已收货";
                break;
            case 6:
                statusStr = "已完成";
                break;
            case 9:
                statusStr = "已取消";
                break;
        }
        return statusStr;
    }

    private String getOrderPayChannel(String payChannel) {
        String payChannelStr = "";
        switch (payChannel) {
            case "public":
                payChannelStr = "公众号";
                break;
            case "mini":
                payChannelStr = "小程序";
                break;
            case "h5":
                payChannelStr = "微信网页支付";
                break;
            case "yue":
                payChannelStr = "余额";
                break;
            case "wechatIos":
                payChannelStr = "微信Ios";
                break;
            case "wechatAndroid":
                payChannelStr = "微信Android";
                break;
            case "alipay":
                payChannelStr = "支付宝";
                break;
            case "alipayApp":
                payChannelStr = "支付宝App";
                break;
        }
        return payChannelStr;
    }

    private String getOrderPayType(String payType) {
        String payTypeStr = "";
        switch (payType) {
            case "weixin":
                payTypeStr = "微信支付";
                break;
            case "alipay":
                payTypeStr = "支付宝支付";
                break;
            case "yue":
                payTypeStr = "余额支付";
                break;
        }
        return payTypeStr;
    }

    /**
     * 商品导出
     *
     * @param request 查询条件
     * @return 文件名称
     */
    @Override
    public String exportProduct(PlatProductSearchRequest request) {
        String fileName = null;
        try {
            // 设置超大分页参数，避免分页限制
            request.setPage(1);
            request.setLimit(10000);
            PageInfo<PlatformProductListResponse> pageInfo = productService.getPlatformPageList(request);
            List<PlatformProductListResponse> productList = pageInfo.getList();
            if (CollUtil.isEmpty(productList)) {
                throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "没有可导出的数据！");
            }

            // 获取分类映射
            List<Integer> categoryIds = productList.stream()
                    .map(PlatformProductListResponse::getCategoryId)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Integer, String> categoryMap = CollUtil.newHashMap();
            if (CollUtil.isNotEmpty(categoryIds)) {
                List<ProductCategory> categoryList = productCategoryService.findByIdList(categoryIds);
                categoryMap = categoryList.stream()
                        .collect(Collectors.toMap(ProductCategory::getId, ProductCategory::getName));
            }

            // 构建导出数据
            List<ProductExcelVo> voList = CollUtil.newArrayList();
            for (PlatformProductListResponse product : productList) {
                ProductExcelVo vo = new ProductExcelVo();
                vo.setId(product.getId().toString());
                vo.setName(product.getName());
                vo.setCategoryName(categoryMap.getOrDefault(product.getCategoryId(), ""));
                vo.setProductType(getProductTypeStr(product.getType()));
                vo.setMerchantName(product.getMerchantName());
                vo.setMerchantType(product.getIsSelf() ? "自营" : "非自营");
                vo.setPrice(product.getPrice() != null ? product.getPrice().toString() : "0.00");
                vo.setSales(product.getSales() != null ? product.getSales().toString() : "0");
                vo.setStock(product.getStock() != null ? product.getStock().toString() : "0");
                vo.setFicti(product.getFicti() != null ? product.getFicti().toString() : "0");
                vo.setIsShow("上架");
                vo.setAuditStatus(getAuditStatusStr(product.getAuditStatus()));
                vo.setReason(StrUtil.isBlank(product.getReason()) ? "" : product.getReason());
                vo.setSpecType(product.getSpecType() != null ? (product.getSpecType() ? "多规格" : "单规格") : "");
                // 格式化时间为 yyyy-MM-dd HH:mm:ss
                vo.setCreateTime(StrUtil.isNotBlank(product.getCreateTime()) ? 
                    CrmebDateUtil.dateToStr(CrmebDateUtil.strToDate(product.getCreateTime(), DateConstants.DATE_TIME_FORMAT), DateConstants.DATE_TIME_FORMAT) : "");
                vo.setUpdateTime(StrUtil.isNotBlank(product.getUpdateTime()) ? 
                    CrmebDateUtil.dateToStr(CrmebDateUtil.strToDate(product.getUpdateTime(), DateConstants.DATE_TIME_FORMAT), DateConstants.DATE_TIME_FORMAT) : "");
                voList.add(vo);
            }

            /*
              ===============================
              以下为存储部分，参考UploadService的文件组织方式
              ===============================
             */
            // 服务器存储地址
            String rootPath = crmebConfig.getImagePath().trim();
            // 模块
            String modelPath = UploadConstants.UPLOAD_FILE_KEYWORD + "/" + UploadConstants.DOWNLOAD_FILE_KEYWORD + "/" + UploadConstants.UPLOAD_MODEL_PATH_EXCEL + "/";
            // 创建目标文件的名称，规则：年/月/日/文件名
            String datePath = CrmebDateUtil.nowDate(DateConstants.DATE_FORMAT_DATE).replace("-", "/") + "/";
            String webPath = modelPath + datePath;

            // 文件名
            fileName = "商品导出_".concat(CrmebDateUtil.nowDateTime(DateConstants.DATE_TIME_FORMAT_NUM)).concat(CrmebUtil.randomCount(111111111, 999999999).toString()).concat(".xlsx");

            // 文件分隔符转化为当前系统的格式
            String destPath = FilenameUtils.separatorsToSystem(rootPath + webPath);

            // 判断是否存在当前目录，不存在则创建
            File dir = new File(destPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fullFileName = destPath + fileName;

            // 使用EasyExcel写入数据
            EasyExcel.write(fullFileName, ProductExcelVo.class)
                    .sheet("商品数据")
                    .doWrite(voList);

            return fullFileName;
        } catch (Exception e) {
            logger.error("商品导出失败", e);
            throw new CrmebException("商品导出失败");
        }
    }

    /**
     * 商品导出到输出流
     *
     * @param request 查询条件
     */
    @Override
    public String exportProductToStream(PlatProductSearchRequest request,HttpServletResponse response) throws UnsupportedEncodingException {
        // 服务器存储地址
        String rootPath = crmebConfig.getImagePath().trim();
        // 模块
        String modelPath = "public/export/";
        // 类型
        String type = UploadConstants.UPLOAD_AFTER_FILE_KEYWORD + "/";

        // 变更文件名
        String newFileName = "商品导出_" + CrmebDateUtil.nowDateTime(DateConstants.DATE_TIME_FORMAT_NUM) + ".xlsx";
        // 创建目标文件的名称，规则：类型/模块/年/月/日/文件名
        String webPath = type + modelPath + CrmebDateUtil.nowDate("yyyy/MM/dd") + "/";
        // 文件分隔符转化为当前系统的格式
        String destPath = FilenameUtils.separatorsToSystem(rootPath + webPath) + newFileName;

        // 设置超大分页参数，避免分页限制
        request.setPage(1);
        request.setLimit(10000);

        PageInfo<PlatformProductListResponse> pageInfo = productService.getPlatformPageList(request);
        List<PlatformProductListResponse> productList = pageInfo.getList();

        if (CollUtil.isEmpty(productList)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "没有可导出的数据！");
        }

        SystemAdmin admin = SecurityUtil.getLoginUserVo().getUser();
        // 获取分类映射
        List<Integer> categoryIds = productList.stream()
                .map(PlatformProductListResponse::getCategoryId)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, String> categoryMap = CollUtil.newHashMap();
        if (CollUtil.isNotEmpty(categoryIds)) {
            List<ProductCategory> categoryList = productCategoryService.findByIdList(categoryIds);
            categoryMap = categoryList.stream()
                    .collect(Collectors.toMap(ProductCategory::getId, ProductCategory::getName));
        }

        // 构建导出数据
        List<ProductExcelVo> voList = CollUtil.newArrayList();
        for (PlatformProductListResponse product : productList) {
            ProductExcelVo vo = new ProductExcelVo();
            vo.setId(product.getId().toString());
            vo.setName(product.getName());
            vo.setCategoryName(categoryMap.getOrDefault(product.getCategoryId(), ""));
            vo.setProductType(getProductTypeStr(product.getType()));
            vo.setMerchantName(product.getMerchantName());
            vo.setMerchantType(product.getIsSelf() ? "自营" : "非自营");
            vo.setPrice(product.getPrice() != null ? product.getPrice().toString() : "0.00");
            vo.setSales(product.getSales() != null ? product.getSales().toString() : "0");
            vo.setStock(product.getStock() != null ? product.getStock().toString() : "0");
            vo.setFicti(product.getFicti() != null ? product.getFicti().toString() : "0");
            vo.setIsShow("上架");
            vo.setAuditStatus(getAuditStatusStr(product.getAuditStatus()));
            vo.setReason(StrUtil.isBlank(product.getReason()) ? "" : product.getReason());
            vo.setSpecType(product.getSpecType() != null ? (product.getSpecType() ? "多规格" : "单规格") : "");
            // 格式化时间为 yyyy-MM-dd HH:mm:ss
            vo.setCreateTime(StrUtil.isNotBlank(product.getCreateTime()) ? 
                CrmebDateUtil.dateToStr(CrmebDateUtil.strToDate(product.getCreateTime(), DateConstants.DATE_TIME_FORMAT), DateConstants.DATE_TIME_FORMAT) : "");
            vo.setUpdateTime(StrUtil.isNotBlank(product.getUpdateTime()) ? 
                CrmebDateUtil.dateToStr(CrmebDateUtil.strToDate(product.getUpdateTime(), DateConstants.DATE_TIME_FORMAT), DateConstants.DATE_TIME_FORMAT) : "");
            voList.add(vo);
        }

        File file = null;
        try {
            // 创建本地文件
            file = UploadUtil.createFile(destPath);

            // 使用EasyExcel写入数据
            EasyExcel.write(file, ProductExcelVo.class)
                    .sheet("商品数据")
                    .doWrite(voList);

            // 创建附件记录
            SystemAttachment systemAttachment = new SystemAttachment();
            systemAttachment.setName(newFileName);
            systemAttachment.setSattDir(webPath + newFileName);
            systemAttachment.setAttSize(String.valueOf(file.length()));
            systemAttachment.setAttType("xlsx");
            systemAttachment.setImageType(1); // 默认本地
            systemAttachment.setPid(0);
            systemAttachment.setOwner(-1); // 平台文件

            // 获取上传类型配置
            String uploadType = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_UPLOAD_TYPE);
            Integer uploadTypeInt = Integer.parseInt(uploadType);

            if (uploadTypeInt.equals(1)) {
                // 本地存储
                systemAttachmentService.save(systemAttachment);
                return systemAttachmentService.prefixFile(systemAttachment.getSattDir());
            }

            // 云存储处理
            CloudVo cloudVo = new CloudVo();
            String fileIsSave = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_FILE_IS_SAVE);

            switch (uploadTypeInt) {
                case 2: // 七牛云
                    systemAttachment.setImageType(2);
                    cloudVo.setDomain(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_UPLOAD_URL));
                    cloudVo.setAccessKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_ACCESS_KEY));
                    cloudVo.setSecretKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_SECRET_KEY));
                    cloudVo.setBucketName(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_STORAGE_NAME));
                    cloudVo.setRegion(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_STORAGE_REGION));
                    try {
                        Configuration cfg = new Configuration(Region.autoRegion());
                        UploadManager uploadManager = new UploadManager(cfg);
                        Auth auth = Auth.create(cloudVo.getAccessKey(), cloudVo.getSecretKey());
                        String upToken = auth.uploadToken(cloudVo.getBucketName());
                        qiNiuService.uploadFile(uploadManager, upToken, systemAttachment.getSattDir(), destPath, file);
                    } catch (Exception e) {
                        logger.error("七牛云上传失败：" + e.getMessage());
                    }
                    break;
                case 3: // 阿里云OSS
                    systemAttachment.setImageType(3);
                    cloudVo.setDomain(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_UPLOAD_URL));
                    cloudVo.setAccessKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_ACCESS_KEY));
                    cloudVo.setSecretKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_SECRET_KEY));
                    cloudVo.setBucketName(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_STORAGE_NAME));
                    cloudVo.setRegion(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_STORAGE_REGION));
                    try {
                        ossService.upload(cloudVo, systemAttachment.getSattDir(), destPath, file);
                    } catch (Exception e) {
                        logger.error("阿里云OSS上传失败：" + e.getMessage());
                    }
                    break;
                case 5: // 京东云
                    systemAttachment.setImageType(5);
                    String bucket = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_JD_BUCKET_NAME);
                    try {
                        jdCloudService.uploadFile(systemAttachment.getSattDir(), destPath, bucket);
                    } catch (Exception e) {
                        logger.error("京东云上传失败：" + e.getMessage());
                    }
                    break;
            }

            // 保存附件记录
            systemAttachmentService.save(systemAttachment);

            // 如果不保存本地文件，删除本地文件
            if (!fileIsSave.equals("1") && file != null) {
                file.delete();
            }

            // 返回文件访问URL
            return systemAttachmentService.prefixFile(systemAttachment.getSattDir());

        } catch (Exception e) {
            logger.error("生成商品导出文件失败", e);
            throw new CrmebException("生成商品导出文件失败：" + e.getMessage());
        }
    }

    /**
     * 获取商品类型字符串
     */
    private String getProductTypeStr(Integer type) {
        if (type == null) return "";
        switch (type) {
            case 0:
                return "普通商品";
            case 1:
                return "积分商品";
            case 2:
                return "虚拟商品";
            case 4:
                return "视频号商品";
            case 5:
                return "云盘商品";
            case 6:
                return "卡密商品";
            default:
                return "";
        }
    }

    /**
     * 获取商品状态字符串
     */
    private String getProductStatusStr(AdminProductListResponse product) {
        // 这里需要根据实际的商品状态逻辑来判断
        // 暂时简化处理
        return "上架"; // 可以根据实际需要添加更多状态判断
    }

    /**
     * 获取审核状态字符串
     */
    private String getAuditStatusStr(Integer auditStatus) {
        if (auditStatus == null) return "";
        switch (auditStatus) {
            case 0:
                return "无需审核";
            case 1:
                return "待审核";
            case 2:
                return "审核成功";
            case 3:
                return "审核拒绝";
            default:
                return "";
        }
    }
    /**
     * 生成商品导入模板
     */
    @Override
    public void downloadProductImportTemplate(HttpServletResponse response) throws UnsupportedEncodingException {
        setExcelRespProp(response, "商品导入模板");

        XSSFWorkbook workbook = null;
        try {
            workbook = new XSSFWorkbook();

            // 获取下拉数据
            DropdownData dropdownData = getDropdownData();

            // 创建主数据表
            Sheet mainSheet = workbook.createSheet("商品导入模板");
            createMainSheetWithValidation(mainSheet, workbook, dropdownData);

            // 创建数据字典说明表
            Sheet dictSheet = workbook.createSheet("数据字典说明");
            createDictSheet(dictSheet, dropdownData);

            // 创建多规格示例表
            Sheet specSheet = workbook.createSheet("多规格示例");
            createSpecSheet(specSheet);

            // 创建隐藏的数据源表（用于下拉验证）
            createHiddenDataSheets(workbook, dropdownData);

            // 输出到响应
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();

        } catch (IOException e) {
            throw new RuntimeException("生成模板失败", e);
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    // 忽略关闭异常
                }
            }
        }
    }

    /**
     * 创建数据字典说明
     */
    private List<List<String>> createDictData() {
        List<List<String>> dictData = CollUtil.newArrayList();

        // 添加标题行
        dictData.add(CollUtil.newArrayList("字段名称", "可选值", "说明"));

        // 商品类型说明
        dictData.add(CollUtil.newArrayList("商品类型", "0", "普通商品"));
        dictData.add(CollUtil.newArrayList("", "2", "虚拟商品"));
        dictData.add(CollUtil.newArrayList("", "5", "云盘商品"));
        dictData.add(CollUtil.newArrayList("", "6", "卡密商品"));

        // 配送方式说明
        dictData.add(CollUtil.newArrayList("配送方式", "1", "商家配送"));
        dictData.add(CollUtil.newArrayList("", "2", "到店核销"));
        dictData.add(CollUtil.newArrayList("", "1,2", "支持多种配送方式"));

        // 规格类型说明
        dictData.add(CollUtil.newArrayList("规格类型", "single", "单规格商品"));
        dictData.add(CollUtil.newArrayList("", "multiple", "多规格商品（需在多规格示例表配置）"));

        // 布尔值说明
        dictData.add(CollUtil.newArrayList("布尔值字段", "true", "是/启用"));
        dictData.add(CollUtil.newArrayList("", "false", "否/禁用"));

        // 注意事项
        dictData.add(CollUtil.newArrayList("注意事项", "", ""));
        dictData.add(CollUtil.newArrayList("1. 分类名称", "", "请使用实际存在的分类名称"));
        dictData.add(CollUtil.newArrayList("2. 品牌名称", "", "请使用实际存在的品牌名称"));
        dictData.add(CollUtil.newArrayList("3. 运费模板", "", "请使用实际存在的模板名称"));
        dictData.add(CollUtil.newArrayList("4. 图片URL", "", "请确保图片链接可以正常访问"));
        dictData.add(CollUtil.newArrayList("5. 多规格商品", "", "需要在'多规格示例'表中配置详细规格"));

        return dictData;
    }

    /**
     * 创建多规格示例数据
     */
    private List<List<String>> createSpecData() {
        List<List<String>> specData = CollUtil.newArrayList();

        // 添加标题行
        specData.add(CollUtil.newArrayList("商品名称", "规格组合", "规格价格", "市场价", "成本价", "会员价", "库存", "商品编码", "商品条码", "重量", "体积", "规格图片"));

        // 示例数据：T恤多规格 - 使用统一的属性名:属性值格式
        specData.add(CollUtil.newArrayList("示例T恤", "白色", "59.00", "79.00", "30.00", "55.00", "50", "TS-RED-S", "1234567890001", "0.2", "0.001", "https://example.com/red-s.jpg"));
        specData.add(CollUtil.newArrayList("示例T恤", "红色", "59.00", "79.00", "30.00", "55.00", "80", "TS-RED-M", "1234567890002", "0.2", "0.001", "https://example.com/red-m.jpg"));

        // 添加单规格示例
        specData.add(CollUtil.newArrayList("示例单规格商品", "默认规格", "29.00", "39.00", "15.00", "25.00", "100", "DEFAULT-001", "1234567890100", "0.1", "0.0005", "https://example.com/default.jpg"));

        // 添加说明
        specData.add(CollUtil.newArrayList("", "", "", "", "", "", "", "", "", "", "", ""));
        specData.add(CollUtil.newArrayList("说明：", "", "", "", "", "", "", "", "", "", "", ""));
        specData.add(CollUtil.newArrayList("1. 商品名称必须与主表中的商品名称完全一致", "", "", "", "", "", "", "", "", "", "", ""));
        specData.add(CollUtil.newArrayList("2. 规格组合：规格名称", "", "", "", "", "", "", "", "", "", "", ""));
        specData.add(CollUtil.newArrayList("3. 单规格商品的规格组合请填写\"默认规格\"", "", "", "", "", "", "", "", "", "", "", ""));
        specData.add(CollUtil.newArrayList("4. 多规格商品通过商品名称相同的多行数据进行判断", "", "", "", "", "", "", "", "", "", "", ""));
        specData.add(CollUtil.newArrayList("5. 价格、库存等数值字段不能为空", "", "", "", "", "", "", "", "", "", "", ""));

        return specData;
    }

    /**
     * 商品批量导入
     */
    @Override
    public ProductImportResultVo importProducts(MultipartFile file, Integer merId) {
        if (file == null || file.isEmpty()) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "导入文件不能为空");
        }

        ProductImportResultVo result = new ProductImportResultVo();
        result.setTotalCount(0);
        result.setSuccessCount(0);
        result.setFailCount(0);
        result.setErrorList(CollUtil.newArrayList());

        // 提取Excel中的图片
        Map<String, String> imageMap = extractAndUploadImagesFromExcel(file);
        logger.info("从Excel中提取到{}张图片", imageMap.size());

        try {
            // 第一步：读取商品基本信息（第一个sheet）
            Map<String, ProductImportVo> productBasicInfoMap = new HashMap<>();
            EasyExcel.read(file.getInputStream(), ProductImportVo.class, new AnalysisEventListener<ProductImportVo>() {
                private int rowIndex = 0;

                @Override
                public void invoke(ProductImportVo data, AnalysisContext context) {
                    rowIndex++;
                    if (StrUtil.isNotBlank(data.getName())) {
                        // 处理Excel中的图片
                        processImagesForRow(data, rowIndex, imageMap);
                        productBasicInfoMap.put(data.getName(), data);
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    logger.info("读取到{}个商品基本信息", productBasicInfoMap.size());
                }
            }).sheet("商品基本信息").doRead();

            // 第二步：读取商品规格配置（第二个sheet）
            Map<String, List<ProductSpecConfigVo>> productSpecConfigMap = new HashMap<>();
            EasyExcel.read(file.getInputStream(), ProductSpecConfigVo.class, new AnalysisEventListener<ProductSpecConfigVo>() {
                private int rowIndex = 0;

                @Override
                public void invoke(ProductSpecConfigVo data, AnalysisContext context) {
                    rowIndex++;
                    if (StrUtil.isNotBlank(data.getProductName()) && StrUtil.isNotBlank(data.getSpecCombination())) {
                        // 处理规格图片
                        processSpecImagesForRow(data, rowIndex, imageMap);

                        List<ProductSpecConfigVo> specList = productSpecConfigMap.computeIfAbsent(
                                data.getProductName(), k -> new ArrayList<>());
                        specList.add(data);
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    logger.info("读取到{}个商品的规格配置", productSpecConfigMap.size());
                }
            }).sheet("商品规格配置").doRead();

            // 第三步：合并数据并创建商品
            for (Map.Entry<String, ProductImportVo> entry : productBasicInfoMap.entrySet()) {
                String productName = entry.getKey();
                ProductImportVo basicInfo = entry.getValue();

                result.setTotalCount(result.getTotalCount() + 1);

                try {
                    // 数据验证
                    validateProductImportData(basicInfo, 0);

                    // 获取对应的规格配置
                    List<ProductSpecConfigVo> specConfigs = productSpecConfigMap.get(productName);
                    if (CollUtil.isEmpty(specConfigs)) {
                        throw new CrmebException("商品[" + productName + "]未找到对应的规格配置");
                    }

                    // 转换为ProductAddRequest
                    ProductAddRequest productRequest = convertToProductAddRequestWithSpecs(basicInfo, specConfigs, merId);

                    // 调用商品保存服务
                    productService.save(productRequest);

                    result.setSuccessCount(result.getSuccessCount() + 1);
                    logger.info("成功导入商品：{}", productName);

                } catch (Exception e) {
                    result.setFailCount(result.getFailCount() + 1);

                    ProductImportResultVo.ProductImportErrorVo error = new ProductImportResultVo.ProductImportErrorVo();
                    error.setRowIndex(result.getTotalCount());
                    error.setProductName(productName);
                    error.setErrorMessage(e.getMessage());
                    result.getErrorList().add(error);
                    logger.error("导入商品失败：{}，错误：{}", productName, e.getMessage());
                }
            }

        } catch (IOException e) {
            logger.error("文件读取失败", e);
            result.setFailCount(result.getTotalCount() - result.getSuccessCount());

            ProductImportResultVo.ProductImportErrorVo error = new ProductImportResultVo.ProductImportErrorVo();
            error.setRowIndex(0);
            error.setProductName("文件读取");
            error.setErrorMessage("文件读取失败：" + e.getMessage());
            result.getErrorList().add(error);
        } catch (Exception e) {
            logger.error("导入处理失败", e);
            result.setFailCount(result.getTotalCount() - result.getSuccessCount());

            ProductImportResultVo.ProductImportErrorVo error = new ProductImportResultVo.ProductImportErrorVo();
            error.setRowIndex(0);
            error.setProductName("导入处理");
            error.setErrorMessage("导入处理失败：" + e.getMessage());
            result.getErrorList().add(error);
        }

        return result;
    }

    /**
     * 商品批量导入（异步）
     */
    @Override
    public String importProductsAsync(MultipartFile file, Integer merId) {
        if (file == null || file.isEmpty()) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "导入文件不能为空");
        }

        // 生成唯一任务ID
        String taskId = java.util.UUID.randomUUID().toString();

        // 初始化任务状态
        Map<String, Object> statusMap = new ConcurrentHashMap<>();
        statusMap.put("status", "processing");
        statusMap.put("progress", 0);
        statusMap.put("totalCount", 0);
        statusMap.put("successCount", 0);
        statusMap.put("failCount", 0);
        statusMap.put("message", "任务正在处理中");
        statusMap.put("startTime", new Date());
        statusMap.put("errorList", new ArrayList<ProductImportResultVo.ProductImportErrorVo>());

        // 确保taskId和statusMap都不为null再放入Map
        if (taskId != null && statusMap != null) {
            IMPORT_TASK_STATUS_MAP.put(taskId, statusMap);
        } else {
            throw new CrmebException(CommonResultCode.ERROR, "任务初始化失败");
        }

        // 先保存文件到临时目录
        try {
            // 生成临时文件名
            String originalFilename = file.getOriginalFilename();
            String extension = FilenameUtils.getExtension(originalFilename);
            String tempFileName = taskId + "." + extension;

            // 保存到临时目录
            String tempDir = System.getProperty("java.io.tmpdir");
            File tempFile = new File(tempDir, tempFileName);
            file.transferTo(tempFile);

            // 异步执行导入任务
          // asyncService.executeImportTask(taskId, tempFile, merId,IMPORT_TASK_STATUS_MAP);
            executeImportTask(taskId, tempFile, merId);

        } catch (IOException e) {
            // 更新任务状态为失败
            statusMap.put("status", "failed");
            statusMap.put("message", "文件保存失败: " + e.getMessage());
            logger.error("文件保存失败，任务ID: " + taskId, e);
            throw new CrmebException(CommonResultCode.ERROR, "文件保存失败");
        }

        return taskId;
    }

    /**
     * 获取导入任务状态
     */
    @Override
    public Map<String, Object> getImportTaskStatus(String taskId) {
        if (StrUtil.isEmpty(taskId)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "任务ID不能为空");
        }

        Map<String, Object> statusMap = IMPORT_TASK_STATUS_MAP.get(taskId);
        if (statusMap == null) {
            throw new CrmebException(CommonResultCode.ERROR, "未找到对应的任务信息");
        }

        // 返回副本避免外部修改
        return new HashMap<>(statusMap);
    }

    /**
     * 异步执行导入任务（多线程优化版本）
     */
    @Async("taskExecutor")
    public void executeImportTask(String taskId, File tempFile, Integer merId) {
        // 检查taskId是否为null
        if (taskId == null || taskId.isEmpty()) {
            logger.error("任务ID为空，无法执行导入任务");
            return;
        }

        Map<String, Object> statusMap = IMPORT_TASK_STATUS_MAP.get(taskId);
        // 检查statusMap是否为null
        if (statusMap == null) {
            logger.error("未找到任务状态信息，任务ID: {}", taskId);
            return;
        }

        // 创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(CORE_POOL_SIZE);
        
        try {
            logger.info("开始执行多线程异步导入任务，任务ID: {}, 线程池大小: {}", taskId, CORE_POOL_SIZE);

            // 更新进度
            statusMap.put("progress", 10);
            statusMap.put("message", "正在读取文件...");

            ProductImportResultVo result = new ProductImportResultVo();
            result.setTotalCount(0);
            result.setSuccessCount(0);
            result.setFailCount(0);
            result.setErrorList(new ArrayList<>());

            // 提取Excel中的图片
            statusMap.put("progress", 20);
            statusMap.put("message", "正在处理图片...");

            Map<String, String> imageMap = extractAndUploadImagesFromExcel(tempFile);
            logger.info("从Excel中提取到{}张图片", imageMap.size());

            // 第一步：读取商品基本信息（第一个sheet）
            statusMap.put("progress", 30);
            statusMap.put("message", "正在读取商品基本信息...");

            Map<String, ProductImportVo> productBasicInfoMap = new HashMap<>();
            EasyExcel.read(tempFile, ProductImportVo.class, new AnalysisEventListener<ProductImportVo>() {
                private int rowIndex = 0;

                @Override
                public void invoke(ProductImportVo data, AnalysisContext context) {
                    rowIndex++;
                    if (StrUtil.isNotBlank(data.getName())) {
                        // 处理Excel中的图片
                        processImagesForRow(data, rowIndex, imageMap);
                        productBasicInfoMap.put(data.getName(), data);
                    }

                    // 更新进度
                    if (rowIndex % 100 == 0) {
                        statusMap.put("message", "已读取 " + rowIndex + " 行商品基本信息...");
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    logger.info("读取到{}个商品基本信息", productBasicInfoMap.size());
                }
            }).sheet("商品基本信息").doRead();

            // 第二步：读取商品规格配置（第二个sheet）
            statusMap.put("progress", 40);
            statusMap.put("message", "正在读取商品规格配置...");

            Map<String, List<ProductSpecConfigVo>> productSpecConfigMap = new HashMap<>();
            EasyExcel.read(tempFile, ProductSpecConfigVo.class, new AnalysisEventListener<ProductSpecConfigVo>() {
                @Override
                public void invoke(ProductSpecConfigVo data, AnalysisContext context) {
                    if (StrUtil.isNotBlank(data.getProductName())) {
                        productSpecConfigMap.computeIfAbsent(data.getProductName(), k -> new ArrayList<>()).add(data);
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    logger.info("读取到{}个商品的规格配置", productSpecConfigMap.size());
                }
            }).sheet("商品规格配置").doRead();

            // 第三步：预加载缓存数据，避免重复查询
            statusMap.put("progress", 45);
            statusMap.put("message", "预加载缓存数据...");
            
            Map<String, Integer> categoryCache = preloadCategoryCache();
            Map<String, String> merchantCategoryCache = preloadMerchantCategoryCache(merId);
            Map<String, Integer> brandCache = preloadBrandCache(merId);
            Map<String, Integer> templateCache = preloadTemplateCache(merId);
            
            logger.info("预加载完成 - 分类:{}个, 商户分类:{}个, 品牌:{}个, 模板:{}个", 
                    categoryCache.size(), merchantCategoryCache.size(), brandCache.size(), templateCache.size());

            // 第四步：多线程批量处理商品
            statusMap.put("progress", 50);
            statusMap.put("message", "开始多线程批量导入商品...");

            List<Map.Entry<String, ProductImportVo>> productEntries = new ArrayList<>(productBasicInfoMap.entrySet());
            int totalProducts = productEntries.size();
            
            // 使用原子类保证线程安全的计数
            AtomicInteger processedCount = new AtomicInteger(0);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            // 分批处理
            int totalBatches = (totalProducts + BATCH_SIZE - 1) / BATCH_SIZE;
            CountDownLatch latch = new CountDownLatch(totalBatches);
            
            List<CompletableFuture<BatchProcessResult>> futures = new ArrayList<>();

            for (int i = 0; i < totalProducts; i += BATCH_SIZE) {
                int endIndex = Math.min(i + BATCH_SIZE, totalProducts);
                List<Map.Entry<String, ProductImportVo>> batch = productEntries.subList(i, endIndex);
                int batchIndex = i / BATCH_SIZE + 1;
                
                CompletableFuture<BatchProcessResult> future = CompletableFuture.supplyAsync(() -> {
                    return processBatch(batch, productSpecConfigMap, merId, batchIndex, 
                                      categoryCache, merchantCategoryCache, brandCache, templateCache);
                }, executorService).whenComplete((batchResult, ex) -> {
                    if (ex != null) {
                        logger.error("批次{}处理异常", batchIndex, ex);
                        failCount.addAndGet(batch.size());
                    } else {
                        successCount.addAndGet(batchResult.getSuccessCount());
                        failCount.addAndGet(batchResult.getFailCount());
                        // 合并错误列表（线程安全）
                        synchronized (result.getErrorList()) {
                            result.getErrorList().addAll(batchResult.getErrors());
                        }
                    }
                    
                    int currentProcessed = processedCount.addAndGet(batch.size());
                    
                    // 更新进度（线程安全）
                    synchronized (statusMap) {
                        int progress = 50 + (currentProcessed * 40 / totalProducts);
                        statusMap.put("progress", Math.min(progress, 90));
                        statusMap.put("message", String.format("正在批量导入商品 %d/%d (批次%d/%d)...", 
                                      currentProcessed, totalProducts, batchIndex, totalBatches));
                        statusMap.put("totalCount", currentProcessed);
                        statusMap.put("successCount", successCount.get());
                        statusMap.put("failCount", failCount.get());
                    }
                    
                    latch.countDown();
                });
                
                futures.add(future);
            }

            // 等待所有批次完成
            try {
                latch.await(30, TimeUnit.MINUTES); // 最多等待30分钟
            } catch (InterruptedException e) {
                logger.error("等待批次处理完成时被中断", e);
                Thread.currentThread().interrupt();
            }

            // 更新最终结果
            result.setTotalCount(totalProducts);
            result.setSuccessCount(successCount.get());
            result.setFailCount(failCount.get());

            // 更新任务状态为完成
            statusMap.put("status", "completed");
            statusMap.put("progress", 100);
            statusMap.put("totalCount", result.getTotalCount());
            statusMap.put("successCount", result.getSuccessCount());
            statusMap.put("failCount", result.getFailCount());
            statusMap.put("errorList", result.getErrorList());
            statusMap.put("endTime", new Date());
            statusMap.put("message", String.format("多线程导入完成！总计：%d，成功：%d，失败：%d，线程数：%d",
                    result.getTotalCount(), result.getSuccessCount(), result.getFailCount(), CORE_POOL_SIZE));

            logger.info("多线程商品导入任务完成，任务ID: {}, 总计：{}，成功：{}，失败：{}，耗时：{}ms",
                    taskId, result.getTotalCount(), result.getSuccessCount(), result.getFailCount(),
                    System.currentTimeMillis() - ((Date)statusMap.get("startTime")).getTime());

        } catch (Exception e) {
            // 更新任务状态为失败
            statusMap.put("status", "failed");
            statusMap.put("message", "多线程导入失败: " + e.getMessage());
            statusMap.put("endTime", new Date());
            logger.error("多线程商品导入任务失败，任务ID: " + taskId, e);
        } finally {
            // 关闭线程池
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (deleted) {
                    logger.info("临时文件已删除: {}", tempFile.getAbsolutePath());
                } else {
                    logger.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 批处理结果类
     */
    private static class BatchProcessResult {
        private int successCount;
        private int failCount;
        private List<ProductImportResultVo.ProductImportErrorVo> errors;

        public BatchProcessResult() {
            this.errors = new ArrayList<>();
        }

        public int getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        public int getFailCount() {
            return failCount;
        }

        public void setFailCount(int failCount) {
            this.failCount = failCount;
        }

        public List<ProductImportResultVo.ProductImportErrorVo> getErrors() {
            return errors;
        }

        public void setErrors(List<ProductImportResultVo.ProductImportErrorVo> errors) {
            this.errors = errors;
        }
    }

    /**
     * 预加载分类缓存
     */
    private Map<String, Integer> preloadCategoryCache() {
        try {
            List<ProductCategory> categoryList = productCategoryService.list();
            return categoryList.stream()
                    .collect(Collectors.toMap(ProductCategory::getName, ProductCategory::getId, (v1, v2) -> v1));
        } catch (Exception e) {
            logger.error("预加载分类缓存失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 预加载商户分类缓存
     */
    private Map<String, String> preloadMerchantCategoryCache(Integer merId) {
        try {
            LambdaQueryWrapper<MerchantProductCategory> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(MerchantProductCategory::getMerId, merId);
            wrapper.eq(MerchantProductCategory::getIsDel, false);
            wrapper.eq(MerchantProductCategory::getIsShow, true);
            
            List<MerchantProductCategory> categoryList = merchantProductCategoryService.list(wrapper);
            return categoryList.stream()
                    .collect(Collectors.toMap(MerchantProductCategory::getName, 
                             item -> item.getId().toString(), (v1, v2) -> v1));
        } catch (Exception e) {
            logger.error("预加载商户分类缓存失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 预加载品牌缓存
     */
    private Map<String, Integer> preloadBrandCache(Integer merId) {
        try {
            LambdaQueryWrapper<ProductBrand> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(ProductBrand::getIsDel, false);
            wrapper.eq(ProductBrand::getIsShow, true);
            wrapper.and(w -> w
                    .and(subWrapper -> subWrapper.eq(ProductBrand::getAuditStatus, 1).isNull(ProductBrand::getApplyMerId))
                    .or(subWrapper -> subWrapper.eq(ProductBrand::getApplyMerId, merId).eq(ProductBrand::getAuditStatus, 1))
            );
            
            List<ProductBrand> brandList = productBrandService.list(wrapper);
            return brandList.stream()
                    .collect(Collectors.toMap(ProductBrand::getName, ProductBrand::getId, (v1, v2) -> v1));
        } catch (Exception e) {
            logger.error("预加载品牌缓存失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 预加载运费模板缓存
     */
    private Map<String, Integer> preloadTemplateCache(Integer merId) {
        try {
            LambdaQueryWrapper<ShippingTemplates> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(ShippingTemplates::getMerId, merId);
            
            List<ShippingTemplates> templateList = shippingTemplatesService.list(wrapper);
            return templateList.stream()
                    .collect(Collectors.toMap(ShippingTemplates::getName, ShippingTemplates::getId, (v1, v2) -> v1));
        } catch (Exception e) {
            logger.error("预加载运费模板缓存失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 批量处理商品
     */
    private BatchProcessResult processBatch(List<Map.Entry<String, ProductImportVo>> batch,
                                          Map<String, List<ProductSpecConfigVo>> productSpecConfigMap,
                                          Integer merId, int batchIndex,
                                          Map<String, Integer> categoryCache,
                                          Map<String, String> merchantCategoryCache,
                                          Map<String, Integer> brandCache,
                                          Map<String, Integer> templateCache) {
        
        BatchProcessResult result = new BatchProcessResult();
        int successCount = 0;
        int failCount = 0;
        
        logger.info("开始处理批次{}, 包含{}个商品", batchIndex, batch.size());
        
        // 批量处理商品列表
        List<ProductAddRequest> batchProducts = new ArrayList<>();
        
        for (Map.Entry<String, ProductImportVo> entry : batch) {
            String productName = entry.getKey();
            ProductImportVo basicInfo = entry.getValue();
            
            try {
                // 数据验证
                validateProductImportData(basicInfo, 0);
                
                // 获取对应的规格配置
                List<ProductSpecConfigVo> specConfigs = productSpecConfigMap.get(productName);
                if (CollUtil.isEmpty(specConfigs)) {
                    throw new CrmebException("商品[" + productName + "]未找到对应的规格配置");
                }
                
                // 转换为ProductAddRequest（使用缓存优化）
                ProductAddRequest productRequest = convertToProductAddRequestWithCache(
                        basicInfo, specConfigs, merId, categoryCache, merchantCategoryCache, brandCache, templateCache);
                
                batchProducts.add(productRequest);
                
            } catch (Exception e) {
                failCount++;
                ProductImportResultVo.ProductImportErrorVo error = new ProductImportResultVo.ProductImportErrorVo();
                error.setRowIndex(batchIndex * BATCH_SIZE + batch.indexOf(entry) + 1);
                error.setProductName(productName);
                error.setErrorMessage(e.getMessage());
                result.getErrors().add(error);
                logger.warn("批次{}中商品[{}]转换失败: {}", batchIndex, productName, e.getMessage());
            }
        }
        
        // 批量保存商品（如果有成功的商品）
        if (!batchProducts.isEmpty()) {
            try {
                // 注意：这里需要根据实际的ProductService接口来调整
                // 如果没有批量保存接口，则需要循环调用save方法
                for (ProductAddRequest productRequest : batchProducts) {
                    productService.save(productRequest);
                    successCount++;
                }
                
                logger.info("批次{}处理完成，成功：{}个，失败：{}个", batchIndex, successCount, failCount);
                
            } catch (Exception e) {
                // 如果批量保存失败，记录所有商品为失败
                logger.error("批次{}批量保存失败", batchIndex, e);
                failCount += batchProducts.size();
                
                for (ProductAddRequest product : batchProducts) {
                    ProductImportResultVo.ProductImportErrorVo error = new ProductImportResultVo.ProductImportErrorVo();
                    error.setProductName(product.getName());
                    error.setErrorMessage("批量保存失败: " + e.getMessage());
                    result.getErrors().add(error);
                }
            }
        }
        
        result.setSuccessCount(successCount);
        result.setFailCount(failCount);
        
        return result;
    }

    /**
     * 使用缓存优化的转换方法
     */
    private ProductAddRequest convertToProductAddRequestWithCache(ProductImportVo basicInfo, 
                                                                List<ProductSpecConfigVo> specConfigs, 
                                                                Integer merId,
                                                                Map<String, Integer> categoryCache,
                                                                Map<String, String> merchantCategoryCache,
                                                                Map<String, Integer> brandCache,
                                                                Map<String, Integer> templateCache) {
        // 直接使用现有的转换方法，但可以在这里添加缓存优化逻辑
        // 暂时使用现有方法，后续可以优化
        return convertToProductAddRequestWithSpecs(basicInfo, specConfigs, merId);
    }

    /**
     * 为指定行处理规格图片数据
     * @param data 规格数据
     * @param rowIndex 行号
     * @param imageMap 图片映射
     */
    private void processSpecImagesForRow(ProductSpecConfigVo data, int rowIndex, Map<String, String> imageMap) {
        logger.debug("开始处理第{}行的规格图片数据", rowIndex);

        // 处理规格图片（第2列，index为2）
        // 规格配置表的图片使用"spec_"前缀
        String specImageKey = "spec_" + rowIndex + "_2";

        if (imageMap.containsKey(specImageKey)) {
            String imageUrl = imageMap.get(specImageKey);
            data.setSpecImage(imageUrl);
            logger.debug("第{}行规格图片从imageMap设置为：{}", rowIndex, imageUrl);
        } else {
            // 检查原始数据中是否已经有有效的图片URL
            String originalSpecImage = data.getSpecImage();
            if (StrUtil.isNotBlank(originalSpecImage)) {
                if (isValidImageUrl(originalSpecImage)) {
                    logger.debug("第{}行保留原有规格图片URL：{}", rowIndex, originalSpecImage);
                } else if (isImagePlaceholder(originalSpecImage)) {
                    // 如果是占位符文本，清空它
                    data.setSpecImage("");
                    logger.warn("第{}行规格图片为占位符文本，已清空：{}", rowIndex, originalSpecImage);
                } else if (originalSpecImage.contains("DISPIMG") || originalSpecImage.contains("_xlfn.DISPIMG")) {
                    // 处理图片函数 - 这种情况说明图片函数没有被正确处理
                    logger.warn("第{}行规格图片包含DISPIMG函数但未被处理，函数内容：{}", rowIndex, originalSpecImage);

                    // 尝试从所有图片中智能匹配（作为兜底方案）
                    String fallbackImageUrl = findFallbackImageForSpec(imageMap, rowIndex);
                    if (StrUtil.isNotBlank(fallbackImageUrl)) {
                        data.setSpecImage(fallbackImageUrl);
                        logger.info("第{}行规格图片使用兜底方案匹配：{}", rowIndex, fallbackImageUrl);
                    } else {
                        data.setSpecImage("");
                        logger.warn("第{}行规格图片无法找到匹配的图片，已清空", rowIndex);
                    }
                } else {
                    logger.warn("第{}行规格图片未找到对应的图片文件，原始内容：{}", rowIndex, originalSpecImage);
                }
            }
        }
    }

    /**
     * 为规格配置寻找兜底图片
     * @param imageMap 图片映射
     * @param rowIndex 行号
     * @return 图片URL
     */
    private String findFallbackImageForSpec(Map<String, String> imageMap, int rowIndex) {
        // 尝试寻找同行的其他列图片
        for (int col = 0; col <= 15; col++) {
            String key = "spec_" + rowIndex + "_" + col;
            if (imageMap.containsKey(key)) {
                return imageMap.get(key);
            }
        }

        // 如果没找到，尝试寻找相邻行的图片
        for (int offset = -2; offset <= 2; offset++) {
            if (offset == 0) continue;
            int targetRow = rowIndex + offset;
            if (targetRow > 0) {
                String key = "spec_" + targetRow + "_2";
                if (imageMap.containsKey(key)) {
                    return imageMap.get(key);
                }
            }
        }

        return null;
    }

    /**
     * 为指定行处理图片数据
     * @param data 商品数据
     * @param rowIndex 行号
     * @param imageMap 图片映射
     */
    private void processImagesForRow(ProductImportVo data, int rowIndex, Map<String, String> imageMap) {
        logger.debug("开始处理第{}行的图片数据", rowIndex);

        // 处理商品主图（第10列）
        String mainImageKey = rowIndex + "_10";
        if (imageMap.containsKey(mainImageKey)) {
            String mainImageUrl = imageMap.get(mainImageKey);
            data.setImage(mainImageUrl);
            logger.info("第{}行设置主图：{}", rowIndex, mainImageUrl);
        } else {
            // 检查原始数据中是否已经有有效的图片URL
            String originalImage = data.getImage();
            if (StrUtil.isNotBlank(originalImage) && isValidImageUrl(originalImage)) {
                logger.info("第{}行保留原有主图URL：{}", rowIndex, originalImage);
            } else if (StrUtil.isNotBlank(originalImage) && isImagePlaceholder(originalImage)) {
                // 如果是占位符文本，清空它
                data.setImage("");
                logger.warn("第{}行主图为占位符文本，已清空：{}", rowIndex, originalImage);
            } else {
                logger.warn("第{}行主图未找到对应的图片文件，原始内容：{}", rowIndex, originalImage);
            }
        }

        // 处理轮播图（第11列及后续列）- 支持多张图片
        List<String> sliderImageUrls = new ArrayList<>();

        // 检查第11列及后续可能的图片列（最多检查5列）
        for (int col = 11; col <= 15; col++) {
            String sliderImageKey = rowIndex + "_" + col;
            if (imageMap.containsKey(sliderImageKey)) {
                String sliderImageUrl = imageMap.get(sliderImageKey);
                sliderImageUrls.add(sliderImageUrl);
                logger.info("第{}行第{}列发现轮播图：{}", rowIndex, col, sliderImageUrl);
            }
        }

        // 处理轮播图的合并逻辑
        String existingSliderImages = data.getSliderImage();
        if (StrUtil.isNotBlank(existingSliderImages)) {
            // 检查现有数据格式
            if (existingSliderImages.trim().startsWith("[") && existingSliderImages.trim().endsWith("]")) {
                // 已经是JSON数组格式，尝试解析
                try {
                    List<String> existingUrls = JSON.parseArray(existingSliderImages, String.class);
                    sliderImageUrls.addAll(0, existingUrls); // 将现有URL添加到前面
                    logger.debug("第{}行合并现有JSON格式轮播图", rowIndex);
                } catch (Exception e) {
                    logger.warn("第{}行解析现有轮播图JSON失败：{}", rowIndex, existingSliderImages);
                    // 解析失败，检查是否为有效URL
                    if (isValidImageUrl(existingSliderImages)) {
                        sliderImageUrls.add(0, existingSliderImages);
                    }
                }
            } else if (isValidImageUrl(existingSliderImages)) {
                // 单个有效URL
                sliderImageUrls.add(0, existingSliderImages);
                logger.debug("第{}行保留现有单个轮播图URL", rowIndex);
            } else if (existingSliderImages.contains(",")) {
                // 逗号分隔的多个URL
                String[] urls = existingSliderImages.split(",");
                for (String url : urls) {
                    url = url.trim();
                    if (isValidImageUrl(url)) {
                        sliderImageUrls.add(0, url);
                    }
                }
                logger.debug("第{}行合并逗号分隔的轮播图", rowIndex);
            } else if (!isImagePlaceholder(existingSliderImages)) {
                logger.warn("第{}行轮播图格式未知：{}", rowIndex, existingSliderImages);
            }
        }

        // 如果轮播图为空，使用主图作为轮播图
        if (sliderImageUrls.isEmpty()) {
            String mainImage = data.getImage();
            if (StrUtil.isNotBlank(mainImage) && isValidImageUrl(mainImage)) {
                sliderImageUrls.add(mainImage);
                logger.info("第{}行轮播图为空，使用主图：{}", rowIndex, mainImage);
            }
        }

        // 设置轮播图为JSON数组格式
        if (!sliderImageUrls.isEmpty()) {
            String sliderImageJson = JSON.toJSONString(sliderImageUrls);
            data.setSliderImage(sliderImageJson);
            logger.info("第{}行设置轮播图（JSON格式）：{}", rowIndex, sliderImageJson);
        } else {
            data.setSliderImage("[]");
            logger.warn("第{}行无轮播图，设置为空数组", rowIndex);
        }

        logger.debug("第{}行图片处理完成，主图：{}，轮播图：{}", rowIndex, data.getImage(), data.getSliderImage());

        // 处理商品详情中的图片（第12列及后续列）
        List<String> contentImageUrls = new ArrayList<>();

        // 检查商品详情相关的图片列
        for (int col = 12; col <= 16; col++) {
            String contentImageKey = rowIndex + "_" + col;
            if (imageMap.containsKey(contentImageKey)) {
                String contentImageUrl = imageMap.get(contentImageKey);
                contentImageUrls.add(contentImageUrl);
                logger.info("第{}行第{}列发现详情图片：{}", rowIndex, col, contentImageUrl);
            }
        }

        // 处理商品详情字段中的图片
        String existingContent = data.getContent();
        if (StrUtil.isNotBlank(existingContent)) {
            // 检查是否包含DISPIMG函数，如果包含则需要替换而不是追加
            if (existingContent.contains("DISPIMG") || existingContent.contains("_xlfn.DISPIMG")) {
                // 处理DISPIMG函数，替换为实际的图片标签
                String processedContent = processDispImgInContent(existingContent, contentImageUrls);
                data.setContent(processedContent);
                logger.info("第{}行商品详情替换了DISPIMG函数，处理了{}张图片", rowIndex, contentImageUrls.size());
            } else if (!contentImageUrls.isEmpty()) {
                // 如果详情中没有DISPIMG函数但有新找到的图片，则追加
                StringBuilder contentBuilder = new StringBuilder();
                contentBuilder.append(existingContent);

                // 为每张图片添加img标签
                for (String imageUrl : contentImageUrls) {
                    contentBuilder.append("<p><img src=\"").append(imageUrl).append("\" alt=\"商品详情图片\" style=\"max-width: 100%;\"/></p>");
                }

                data.setContent(contentBuilder.toString());
                logger.info("第{}行商品详情添加了{}张图片", rowIndex, contentImageUrls.size());
            }
        } else if (!contentImageUrls.isEmpty()) {
            // 如果详情字段为空，创建包含图片的基本HTML内容
            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append("<div class=\"product-detail\">");

            for (String imageUrl : contentImageUrls) {
                contentBuilder.append("<p><img src=\"").append(imageUrl).append("\" alt=\"商品详情图片\" style=\"max-width: 100%;\"/></p>");
            }

            contentBuilder.append("</div>");
            data.setContent(contentBuilder.toString());
            logger.info("第{}行商品详情创建了包含{}张图片的内容", rowIndex, contentImageUrls.size());
        }
    }

    /**
     * 从Excel中提取图片并上传
     * @param file Excel文件
     * @return 行号和列号对应的图片URL映射
     */
    private Map<String, String> extractAndUploadImagesFromExcel(MultipartFile file) {
        Map<String, String> imageMap = new HashMap<>();

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());

            // 处理第一个sheet（商品基本信息）
            if (workbook.getNumberOfSheets() > 0) {
                XSSFSheet sheet1 = workbook.getSheetAt(0);
                logger.info("开始提取第一个工作表的图片，工作表名：{}", sheet1.getSheetName());

                // 方法1：通过Drawing获取所有图片及其精确位置
                extractImagesFromDrawing(sheet1, imageMap);

                // 方法2：处理单元格中的图片引用（DISPIMG函数等）
                extractImagesFromCells(workbook, sheet1, imageMap);

                // 方法3：智能分配剩余图片（仅在imageMap为空时）
                if (imageMap.isEmpty()) {
                    intelligentImageAllocation(workbook, sheet1, imageMap);
                }
            }

            // 处理第二个sheet（商品规格配置）
            if (workbook.getNumberOfSheets() > 1) {
                XSSFSheet sheet2 = workbook.getSheetAt(1);
                logger.info("开始提取第二个工作表的图片，工作表名：{}", sheet2.getSheetName());

                // 为规格配置表的图片使用特殊的前缀
                Map<String, String> specImageMap = new HashMap<>();

                // 从第二个sheet提取图片
                extractImagesFromDrawing(sheet2, specImageMap);
                extractImagesFromCells(workbook, sheet2, specImageMap);

                // 将规格配置的图片添加到主imageMap中，使用特殊前缀"spec_"
                for (Map.Entry<String, String> entry : specImageMap.entrySet()) {
                    imageMap.put("spec_" + entry.getKey(), entry.getValue());
                }

                logger.info("从规格配置表提取到{}张图片", specImageMap.size());
            }

            workbook.close();

        } catch (Exception e) {
            logger.error("提取Excel图片失败", e);
        }

        logger.info("图片提取完成，共提取{}张图片", imageMap.size());
        return imageMap;
    }

    /**
     * 从Excel文件(File类型)中提取并上传图片
     */
    private Map<String, String> extractAndUploadImagesFromExcel(File file) {
        Map<String, String> imageMap = new HashMap<>();

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            // 处理第一个sheet（商品基本信息）
            if (workbook.getNumberOfSheets() > 0) {
                XSSFSheet sheet1 = workbook.getSheetAt(0);
                logger.info("开始提取第一个工作表的图片，工作表名：{}", sheet1.getSheetName());

                // 方法1：通过Drawing获取所有图片及其精确位置
                extractImagesFromDrawing(sheet1, imageMap);

                // 方法2：处理单元格中的图片引用（DISPIMG函数等）
                extractImagesFromCells(workbook, sheet1, imageMap);

                // 方法3：智能分配剩余图片（仅在imageMap为空时）
                if (imageMap.isEmpty()) {
                    intelligentImageAllocation(workbook, sheet1, imageMap);
                }
            }

            // 处理第二个sheet（商品规格配置）
            if (workbook.getNumberOfSheets() > 1) {
                XSSFSheet sheet2 = workbook.getSheetAt(1);
                logger.info("开始提取第二个工作表的图片，工作表名：{}", sheet2.getSheetName());

                // 为规格配置表的图片使用特殊的前缀
                Map<String, String> specImageMap = new HashMap<>();

                // 从第二个sheet提取图片
                extractImagesFromDrawing(sheet2, specImageMap);
                extractImagesFromCells(workbook, sheet2, specImageMap);

                // 将规格配置的图片添加到主imageMap中，使用特殊前缀"spec_"
                for (Map.Entry<String, String> entry : specImageMap.entrySet()) {
                    imageMap.put("spec_" + entry.getKey(), entry.getValue());
                }

                logger.info("从规格配置表提取到{}张图片", specImageMap.size());
            }

            workbook.close();

        } catch (Exception e) {
            logger.error("提取Excel图片失败", e);
        }

        logger.info("图片提取完成，共提取{}张图片", imageMap.size());
        return imageMap;
    }

    /**
     * 从Drawing中提取图片（处理单元格内嵌图片）
     * 这是处理Excel中直接插入到单元格内图片的核心方法
     */
    private void extractImagesFromDrawing(XSSFSheet sheet, Map<String, String> imageMap) {
        XSSFDrawing drawing = sheet.getDrawingPatriarch();
        if (drawing == null) {
            logger.info("工作表中没有找到Drawing对象");
            return;
        }

        List<XSSFShape> shapes = drawing.getShapes();
        logger.info("在Drawing中找到{}个图形对象", shapes.size());

        for (XSSFShape shape : shapes) {
            if (shape instanceof XSSFPicture) {
                XSSFPicture picture = (XSSFPicture) shape;
                XSSFPictureData pictureData = picture.getPictureData();

                // 获取图片锚点信息
                XSSFClientAnchor anchor = (XSSFClientAnchor) picture.getAnchor();
                if (anchor != null) {
                    // 获取图片的精确位置信息
                    int row1 = anchor.getRow1();
                    int col1 = anchor.getCol1();
                    int row2 = anchor.getRow2();
                    int col2 = anchor.getCol2();

                    // 获取锚点的偏移量（EMU单位，用于更精确的定位）
                    int dx1 = anchor.getDx1();
                    int dy1 = anchor.getDy1();
                    int dx2 = anchor.getDx2();
                    int dy2 = anchor.getDy2();

                    logger.info("图片详细位置：起始行{}列{}(偏移{},{}), 结束行{}列{}(偏移{},{})",
                            row1, col1, dx1, dy1, row2, col2, dx2, dy2);

                    // 使用更精确的算法确定图片主要位于哪个单元格
                    List<CellLocation> cellLocations = calculateImageCellLocations(anchor);

                    for (CellLocation location : cellLocations) {
                        int targetRow = location.row;
                        int targetCol = location.col;

                        logger.info("确定图片位置：行{}，列{}，覆盖度：{}%", targetRow, targetCol, location.coverage);

                        // 只处理商品主图(第10列)和轮播图(第11列)的图片
                        if (targetCol == 10 || targetCol == 11) {
                            try {
                                // 上传图片
                                String imageUrl = uploadImageFromBytes(pictureData.getData(),
                                        pictureData.suggestFileExtension());

                                // 存储映射关系：行号_列号 -> 图片URL
                                String key = targetRow + "_" + targetCol;
                                if (!imageMap.containsKey(key)) { // 避免重复
                                    imageMap.put(key, imageUrl);
                                    logger.info("成功上传Excel单元格内嵌图片，位置：行{}，列{}，URL：{}", targetRow, targetCol, imageUrl);
                                } else {
                                    logger.info("位置行{}，列{}已有图片，跳过", targetRow, targetCol);
                                }

                            } catch (Exception e) {
                                logger.error("上传Excel单元格内嵌图片失败，位置：行{}，列{}", targetRow, targetCol, e);
                            }
                        } else {
                            logger.debug("图片不在目标列（10或11），跳过。位置：行{}，列{}", targetRow, targetCol);
                        }
                    }
                }
            }
        }
    }

    /**
     * 计算图片覆盖的单元格位置
     * 返回图片主要覆盖的单元格列表，按覆盖度排序
     */
    private List<CellLocation> calculateImageCellLocations(XSSFClientAnchor anchor) {
        List<CellLocation> locations = new ArrayList<>();

        int row1 = anchor.getRow1();
        int col1 = anchor.getCol1();
        int row2 = anchor.getRow2();
        int col2 = anchor.getCol2();

        // 如果图片只在一个单元格内
        if (row1 == row2 && col1 == col2) {
            locations.add(new CellLocation(row1, col1, 100.0));
            return locations;
        }

        // 如果图片跨越多个单元格，计算每个单元格的覆盖度
        for (int row = row1; row <= row2; row++) {
            for (int col = col1; col <= col2; col++) {
                double coverage = calculateCellCoverage(anchor, row, col);
                if (coverage > 10.0) { // 只考虑覆盖度超过10%的单元格
                    locations.add(new CellLocation(row, col, coverage));
                }
            }
        }

        // 按覆盖度降序排序
        locations.sort((a, b) -> Double.compare(b.coverage, a.coverage));

        return locations;
    }

    /**
     * 计算图片在指定单元格的覆盖度
     */
    private double calculateCellCoverage(XSSFClientAnchor anchor, int targetRow, int targetCol) {
        int row1 = anchor.getRow1();
        int col1 = anchor.getCol1();
        int row2 = anchor.getRow2();
        int col2 = anchor.getCol2();

        // 简化计算：如果图片的起始位置就在目标单元格，给予更高的权重
        if (row1 == targetRow && col1 == targetCol) {
            return 90.0;
        }

        // 如果图片的结束位置在目标单元格
        if (row2 == targetRow && col2 == targetCol) {
            return 70.0;
        }

        // 如果图片跨越目标单元格
        if (targetRow >= row1 && targetRow <= row2 && targetCol >= col1 && targetCol <= col2) {
            return 50.0;
        }

        return 0.0;
    }

    /**
     * 单元格位置信息
     */
    private static class CellLocation {
        int row;
        int col;
        double coverage; // 覆盖度百分比

        CellLocation(int row, int col, double coverage) {
            this.row = row;
            this.col = col;
            this.coverage = coverage;
        }
    }

    /**
     * 从单元格中提取图片引用
     */
    private void extractImagesFromCells(XSSFWorkbook workbook, XSSFSheet sheet, Map<String, String> imageMap) {
        String sheetName = sheet.getSheetName();
        logger.info("正在检查工作表 '{}' 中的图片函数", sheetName);

        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            XSSFRow row = sheet.getRow(rowNum);
            if (row != null) {
                if ("商品规格配置".equals(sheetName) || sheetName.contains("规格")) {
                    // 规格配置表：检查规格图片列(第2列)
                    processImageInCell(workbook, row, 2, rowNum, imageMap);
                    logger.debug("检查规格配置表第{}行第2列的图片", rowNum);
                } else {
                    // 商品基本信息表：检查商品主图列(第10列)和轮播图列(第11-15列)
                    processImageInCell(workbook, row, 10, rowNum, imageMap);

                    // 检查轮播图列(第11-15列)，支持多张图片
                    for (int col = 11; col <= 15; col++) {
                        processImageInCell(workbook, row, col, rowNum, imageMap);
                    }

                    // 检查商品详情列(第12列)，可能包含多张图片
                    processImageInCell(workbook, row, 12, rowNum, imageMap);

                    logger.debug("检查基本信息表第{}行的图片列", rowNum);
                }
            }
        }

        logger.info("工作表 '{}' 图片函数检查完成", sheetName);
    }

    /**
     * 智能分配图片
     */
    private void intelligentImageAllocation(XSSFWorkbook workbook, XSSFSheet sheet, Map<String, String> imageMap) {
        List<XSSFPictureData> allPictures = workbook.getAllPictures();
        logger.info("工作簿中总共找到{}张图片，尝试智能分配", allPictures.size());

        if (allPictures.isEmpty()) {
            return;
        }

        // 统计有多少行需要图片
        List<Integer> imageRows = new ArrayList<>();
        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            XSSFRow row = sheet.getRow(rowNum);
            if (row != null && isDataRow(row)) {
                imageRows.add(rowNum);
            }
        }

        logger.info("发现{}行数据需要图片，共有{}张图片", imageRows.size(), allPictures.size());

        // 按比例分配图片
        int pictureIndex = 0;
        for (Integer rowNum : imageRows) {
            if (pictureIndex >= allPictures.size()) {
                break; // 图片用完了
            }

            // 优先分配给第10列（主图）
            String key10 = rowNum + "_10";
            if (!imageMap.containsKey(key10) && pictureIndex < allPictures.size()) {
                try {
                    XSSFPictureData pictureData = allPictures.get(pictureIndex++);
                    String imageUrl = uploadImageFromBytes(pictureData.getData(),
                            pictureData.suggestFileExtension());
                    imageMap.put(key10, imageUrl);
                    logger.info("智能分配图片到行{}，列10，URL：{}", rowNum, imageUrl);
                } catch (Exception e) {
                    logger.error("智能分配图片失败，行{}，列10", rowNum, e);
                }
            }

            // 如果还有图片，分配给第11列（轮播图）
            String key11 = rowNum + "_11";
            if (!imageMap.containsKey(key11) && pictureIndex < allPictures.size()) {
                try {
                    XSSFPictureData pictureData = allPictures.get(pictureIndex++);
                    String imageUrl = uploadImageFromBytes(pictureData.getData(),
                            pictureData.suggestFileExtension());
                    imageMap.put(key11, imageUrl);
                    logger.info("智能分配图片到行{}，列11，URL：{}", rowNum, imageUrl);
                } catch (Exception e) {
                    logger.error("智能分配图片失败，行{}，列11", rowNum, e);
                }
            }
        }
    }

    /**
     * 确定图片的目标行（考虑偏移量）
     * EMU (English Metric Units) 是Excel中的度量单位
     * 1英寸 = 914400 EMU，1厘米 = 360000 EMU
     */
    private int determineTargetRow(int row1, int row2, int dy1, int dy2) {
        if (row1 == row2) {
            return row1;
        }

        // 如果图片跨越多行，根据偏移量判断主要位于哪一行
        // 标准行高约为 255000 EMU (约0.7厘米)
        if (dy1 < 127500) { // 小于半个标准行高，图片主要在起始行
            return row1;
        } else if (dy2 > 127500) { // 大于半个标准行高，图片主要在结束行
            return row2;
        } else {
            // 默认选择起始行
            return row1;
        }
    }

    /**
     * 确定图片的目标列（考虑偏移量）
     */
    private int determineTargetCol(int col1, int col2, int dx1, int dx2) {
        if (col1 == col2) {
            return col1;
        }

        // 如果图片跨越多列，根据偏移量判断主要位于哪一列
        // 标准列宽约为 640000 EMU (约1.8厘米)
        if (dx1 < 320000) { // 小于半个标准列宽，图片主要在起始列
            return col1;
        } else if (dx2 > 320000) { // 大于半个标准列宽，图片主要在结束列
            return col2;
        } else {
            // 默认选择起始列
            return col1;
        }
    }

    /**
     * 判断是否为数据行（检查是否有实际的商品数据）
     */
    private boolean isDataRow(XSSFRow row) {
        if (row == null) {
            return false;
        }

        // 优先检查商品名称列（第1列）是否有内容
        XSSFCell nameCell = row.getCell(1);
        if (nameCell != null && hasContent(nameCell)) {
            return true;
        }

        // 检查其他关键字段是否有内容（前10列）
        for (int i = 0; i < 10; i++) {
            XSSFCell cell = row.getCell(i);
            if (cell != null && hasContent(cell)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查单元格是否有内容
     */
    private boolean hasContent(XSSFCell cell) {
        if (cell == null) {
            return false;
        }

        try {
            switch (cell.getCellType()) {
                case STRING:
                    String stringValue = cell.getStringCellValue();
                    return StrUtil.isNotBlank(stringValue);
                case NUMERIC:
                    return true; // 数字类型认为有内容
                case BOOLEAN:
                    return true; // 布尔类型认为有内容
                case FORMULA:
                    return StrUtil.isNotBlank(cell.getCellFormula());
                case BLANK:
                    return false;
                case ERROR:
                    return false;
                default:
                    return false;
            }
        } catch (Exception e) {
            logger.debug("检查单元格内容时出错：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 智能分配图片到数据行
     */
    private void intelligentImageAllocation(List<XSSFPictureData> allPictures, List<Integer> imageRows, Map<String, String> imageMap) {
        int pictureIndex = 0;

        for (Integer rowNum : imageRows) {
            if (pictureIndex >= allPictures.size()) {
                break; // 图片用完了
            }

            // 优先分配给第10列（主图）
            String key10 = rowNum + "_10";
            if (!imageMap.containsKey(key10) && pictureIndex < allPictures.size()) {
                try {
                    XSSFPictureData pictureData = allPictures.get(pictureIndex++);
                    String imageUrl = uploadImageFromBytes(pictureData.getData(),
                            pictureData.suggestFileExtension());
                    imageMap.put(key10, imageUrl);
                    logger.info("智能分配图片到行{}，列10，URL：{}", rowNum, imageUrl);
                } catch (Exception e) {
                    logger.error("智能分配图片失败，行{}，列10", rowNum, e);
                }
            }

            // 如果还有图片，分配给第11列（轮播图）
            String key11 = rowNum + "_11";
            if (!imageMap.containsKey(key11) && pictureIndex < allPictures.size()) {
                try {
                    XSSFPictureData pictureData = allPictures.get(pictureIndex++);
                    String imageUrl = uploadImageFromBytes(pictureData.getData(),
                            pictureData.suggestFileExtension());
                    imageMap.put(key11, imageUrl);
                    logger.info("智能分配图片到行{}，列11，URL：{}", rowNum, imageUrl);
                } catch (Exception e) {
                    logger.error("智能分配图片失败，行{}，列11", rowNum, e);
                }
            }
        }
    }

    /**
     * 处理单元格中的图片
     */
    private void processImageInCell(XSSFWorkbook workbook, XSSFRow row, int colIndex, int rowNum, Map<String, String> imageMap) {
        XSSFCell cell = row.getCell(colIndex);
        if (cell == null) {
            return;
        }

        String key = rowNum + "_" + colIndex;

        // 如果已经处理过这个位置的图片，跳过
        if (imageMap.containsKey(key)) {
            return;
        }

        try {
            // 检查单元格类型
            switch (cell.getCellType()) {
                case STRING:
                    String cellValue = cell.getStringCellValue();
                    if (cellValue != null) {
                        // 检查是否包含图片函数
                        if (cellValue.contains("_xlfn.DISPIMG") || cellValue.contains("DISPIMG")) {
                            processImageFunction(workbook, cellValue, key, imageMap, rowNum, colIndex);
                        }
                        // 检查是否已经是图片URL
                        else if (isValidImageUrl(cellValue)) {
                            imageMap.put(key, cellValue);
                            logger.info("发现现有图片URL，位置：行{}，列{}，URL：{}", rowNum, colIndex, cellValue);
                        }
                        // 检查是否是图片占位符或提示文本
                        else if (isImagePlaceholder(cellValue)) {
                            logger.info("发现图片占位符，位置：行{}，列{}，内容：{}", rowNum, colIndex, cellValue);
                            // 这种情况下，图片可能通过其他方式嵌入，等待后续处理
                        }
                    }
                    break;
                case FORMULA:
                    // 处理公式单元格，可能包含图片函数
                    String formula = cell.getCellFormula();
                    if (formula != null && (formula.contains("DISPIMG") || formula.contains("_xlfn.DISPIMG"))) {
                        processImageFunction(workbook, formula, key, imageMap, rowNum, colIndex);
                    }
                    break;
                case BLANK:
                    // 空白单元格可能包含图片，通过Drawing处理
                    logger.debug("空白单元格，位置：行{}，列{}，可能包含图片", rowNum, colIndex);
                    break;
                default:
                    // 其他类型的单元格
                    logger.debug("其他类型单元格，位置：行{}，列{}，类型：{}", rowNum, colIndex, cell.getCellType());
                    break;
            }
        } catch (Exception e) {
            logger.error("处理单元格图片失败，位置：行{}，列{}", rowNum, colIndex, e);
        }
    }

    /**
     * 处理图片函数
     */
    private void processImageFunction(XSSFWorkbook workbook, String functionStr, String key, Map<String, String> imageMap, int rowNum, int colIndex) {
        try {
            String imageId = extractImageIdFromDispImg(functionStr);
            if (imageId != null) {
                XSSFPictureData pictureData = findPictureDataById(workbook, imageId);
                if (pictureData != null) {
                    String imageUrl = uploadImageFromBytes(pictureData.getData(),
                            pictureData.suggestFileExtension());
                    imageMap.put(key, imageUrl);
                    logger.info("成功处理图片函数，位置：行{}，列{}，URL：{}", rowNum, colIndex, imageUrl);
                } else {
                    logger.warn("未找到图片数据，图片ID：{}，位置：行{}，列{}", imageId, rowNum, colIndex);
                }
            } else {
                logger.warn("无法提取图片ID，函数：{}，位置：行{}，列{}", functionStr, rowNum, colIndex);
            }
        } catch (Exception e) {
            logger.error("处理图片函数失败，位置：行{}，列{}", rowNum, colIndex, e);
        }
    }

    /**
     * 检查是否为有效的图片URL
     */
    private boolean isValidImageUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return false;
        }

        String lowerUrl = url.toLowerCase();
        return (lowerUrl.startsWith("http") || lowerUrl.startsWith("/")) &&
                (lowerUrl.contains(".jpg") || lowerUrl.contains(".jpeg") ||
                        lowerUrl.contains(".png") || lowerUrl.contains(".gif") ||
                        lowerUrl.contains(".bmp") || lowerUrl.contains(".webp"));
    }

    /**
     * 检查是否为图片占位符
     */
    private boolean isImagePlaceholder(String text) {
        if (StrUtil.isBlank(text)) {
            return false;
        }

        String lowerText = text.toLowerCase();
        return lowerText.contains("请直接粘贴图片") ||
                lowerText.contains("图片") ||
                lowerText.contains("image") ||
                lowerText.contains("picture") ||
                lowerText.contains("请上传") ||
                lowerText.contains("请插入");
    }

    /**
     * 从DISPIMG函数中提取图片ID
     * @param dispImgFunction DISPIMG函数字符串，如：_xlfn.DISPIMG("ID_ED5186FBD9E3417986A2668C502A7ECCA",1)
     * @return 图片ID
     */
    private String extractImageIdFromDispImg(String dispImgFunction) {
        try {
            // 匹配模式：_xlfn.DISPIMG("ID_xxxx",1)
            if (dispImgFunction.contains("_xlfn.DISPIMG(\"") && dispImgFunction.contains("\",")) {
                int startIndex = dispImgFunction.indexOf("\"") + 1;
                int endIndex = dispImgFunction.indexOf("\",", startIndex);
                if (startIndex > 0 && endIndex > startIndex) {
                    String imageId = dispImgFunction.substring(startIndex, endIndex);
                    logger.info("提取到图片ID：{}", imageId);
                    return imageId;
                }
            }
        } catch (Exception e) {
            logger.error("提取图片ID失败：{}", dispImgFunction, e);
        }
        return null;
    }

    /**
     * 根据图片ID查找对应的图片数据
     * @param workbook Excel工作簿
     * @param imageId 图片ID
     * @return 图片数据
     */
    private XSSFPictureData findPictureDataById(XSSFWorkbook workbook, String imageId) {
        try {
            List<XSSFPictureData> allPictures = workbook.getAllPictures();

            // 方法1：尝试通过图片ID匹配（如果可能的话）
            for (XSSFPictureData pictureData : allPictures) {
                // 检查图片数据的包名或其他标识
                String fileName = pictureData.getPackagePart().getPartName().getName();
                if (fileName.contains(imageId) || imageId.contains(fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf(".")))) {
                    logger.info("通过ID匹配找到图片：{} -> {}", imageId, fileName);
                    return pictureData;
                }
            }

            // 方法2：如果无法通过ID匹配，使用改进的顺序分配策略
            // 使用图片ID的哈希值来确定索引，保证相同ID返回相同图片
            int index = Math.abs(imageId.hashCode()) % allPictures.size();
            if (index < allPictures.size()) {
                XSSFPictureData pictureData = allPictures.get(index);
                logger.info("通过哈希索引{}找到图片，对应图片ID：{}", index, imageId);
                return pictureData;
            }

            // 方法3：如果还是找不到，返回第一张图片
            if (!allPictures.isEmpty()) {
                logger.info("使用默认第一张图片，对应图片ID：{}", imageId);
                return allPictures.get(0);
            }

        } catch (Exception e) {
            logger.error("查找图片数据失败，图片ID：{}", imageId, e);
        }
        return null;
    }

    /**
     * 上传图片字节数组
     * @param imageBytes 图片字节数组
     * @param fileExtension 文件扩展名
     * @return 图片URL
     */
    private String uploadImageFromBytes(byte[] imageBytes, String fileExtension) throws Exception {
        // 生成文件名
        String fileName = UploadUtil.generateFileName(fileExtension);
        // 服务器存储地址
        String rootPath = crmebConfig.getImagePath().trim();
        String modelPath = "public/product/";
        String type = UploadConstants.UPLOAD_FILE_KEYWORD + "/";
        String webPath = type + modelPath + CrmebDateUtil.nowDate("yyyy/MM/dd") + "/";
        String destPath = FilenameUtils.separatorsToSystem(rootPath + webPath) + fileName;

        // 创建临时文件
        File tempFile = UploadUtil.createFile(destPath);

        // 写入图片数据
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(imageBytes);
            fos.flush();
        }

        // 创建附件记录
        SystemAttachment systemAttachment = new SystemAttachment();
        systemAttachment.setName(fileName);
        systemAttachment.setSattDir(webPath + fileName);
        systemAttachment.setAttSize(String.valueOf(tempFile.length()));
        systemAttachment.setAttType(fileExtension);
        systemAttachment.setImageType(1); // 本地存储
        systemAttachment.setPid(0);
        systemAttachment.setOwner(-1); // 平台文件

        systemAttachmentService.save(systemAttachment);
        // 获取上传类型配置
        String uploadType = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_UPLOAD_TYPE);
        Integer uploadTypeInt = Integer.parseInt(uploadType);

        if (uploadTypeInt.equals(1)) {
            // 本地存储
            systemAttachmentService.save(systemAttachment);
            return systemAttachmentService.prefixFile(systemAttachment.getSattDir());
        }

        // 云存储处理
        CloudVo cloudVo = new CloudVo();
        String fileIsSave = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_FILE_IS_SAVE);

        switch (uploadTypeInt) {
            case 2: // 七牛云
                systemAttachment.setImageType(2);
                cloudVo.setDomain(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_UPLOAD_URL));
                cloudVo.setAccessKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_ACCESS_KEY));
                cloudVo.setSecretKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_SECRET_KEY));
                cloudVo.setBucketName(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_STORAGE_NAME));
                cloudVo.setRegion(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_STORAGE_REGION));
                try {
                    Configuration cfg = new Configuration(Region.autoRegion());
                    UploadManager uploadManager = new UploadManager(cfg);
                    Auth auth = Auth.create(cloudVo.getAccessKey(), cloudVo.getSecretKey());
                    String upToken = auth.uploadToken(cloudVo.getBucketName());
                    qiNiuService.uploadFile(uploadManager, upToken, systemAttachment.getSattDir(), destPath, tempFile);
                } catch (Exception e) {
                    logger.error("七牛云上传失败：" + e.getMessage());
                }
                break;
            case 3: // 阿里云OSS
                systemAttachment.setImageType(3);
                cloudVo.setDomain(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_UPLOAD_URL));
                cloudVo.setAccessKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_ACCESS_KEY));
                cloudVo.setSecretKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_SECRET_KEY));
                cloudVo.setBucketName(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_STORAGE_NAME));
                cloudVo.setRegion(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_STORAGE_REGION));
                try {
                    ossService.upload(cloudVo, systemAttachment.getSattDir(), destPath, tempFile);
                } catch (Exception e) {
                    logger.error("阿里云OSS上传失败：" + e.getMessage());
                }
                break;
            case 5: // 京东云
                systemAttachment.setImageType(5);
                String bucket = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_JD_BUCKET_NAME);
                try {
                    jdCloudService.uploadFile(systemAttachment.getSattDir(), destPath, bucket);
                } catch (Exception e) {
                    logger.error("京东云上传失败：" + e.getMessage());
                }
                break;
        }

        // 保存附件记录
        systemAttachmentService.save(systemAttachment);

        // 如果不保存本地文件，删除本地文件
        if (!fileIsSave.equals("1") && tempFile != null) {
            tempFile.delete();
        }

        // 返回文件访问URL
        return systemAttachmentService.prefixFile(systemAttachment.getSattDir());

    }

    /**
     * 处理商品详情内容中的DISPIMG函数
     * @param content 原始内容
     * @param contentImageUrls 图片URL列表
     * @return 处理后的内容
     */
    private String processDispImgInContent(String content, List<String> contentImageUrls) {
        if (StrUtil.isBlank(content)) {
            return content;
        }

        String processedContent = content;
        
        try {
            // 处理DISPIMG函数，替换为HTML图片标签
            if (content.contains("DISPIMG") || content.contains("_xlfn.DISPIMG")) {
                logger.info("处理DISPIMG函数，原始内容：{}", content);
                
                // 使用正则表达式匹配DISPIMG函数
                String dispImgPattern = "_xlfn\\.DISPIMG\\(\"([^\"]+)\",\\s*(\\d+)\\)";
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(dispImgPattern);
                java.util.regex.Matcher matcher = pattern.matcher(content);
                
                int imageIndex = 0;
                StringBuffer sb = new StringBuffer();
                
                while (matcher.find()) {
                    String imageId = matcher.group(1);
                    String displayMode = matcher.group(2);
                    
                    logger.info("匹配到DISPIMG函数：imageId={}, displayMode={}", imageId, displayMode);
                    
                    // 如果有对应的图片URL，替换为img标签
                    if (imageIndex < contentImageUrls.size()) {
                        String imageUrl = contentImageUrls.get(imageIndex);
                        String imgTag = String.format("<img src=\"%s\" alt=\"商品详情图片\" style=\"max-width: 100%%;\" />", imageUrl);
                        matcher.appendReplacement(sb, imgTag);
                        logger.info("替换DISPIMG函数为图片标签：{}", imgTag);
                        imageIndex++;
                    } else {
                        // 如果没有对应的图片，移除函数
                        matcher.appendReplacement(sb, "");
                        logger.warn("没有对应的图片URL，移除DISPIMG函数：{}", matcher.group(0));
                    }
                }
                matcher.appendTail(sb);
                processedContent = sb.toString();
                
                // 如果还有剩余的图片URL，追加到内容末尾
                if (imageIndex < contentImageUrls.size()) {
                    StringBuilder contentBuilder = new StringBuilder(processedContent);
                    for (int i = imageIndex; i < contentImageUrls.size(); i++) {
                        String imageUrl = contentImageUrls.get(i);
                        contentBuilder.append("<p><img src=\"").append(imageUrl)
                                     .append("\" alt=\"商品详情图片\" style=\"max-width: 100%;\"/></p>");
                    }
                    processedContent = contentBuilder.toString();
                    logger.info("追加剩余图片到内容末尾，共{}张", contentImageUrls.size() - imageIndex);
                }
                
                logger.info("DISPIMG函数处理完成，处理后内容：{}", processedContent);
            }
        } catch (Exception e) {
            logger.error("处理DISPIMG函数时出错", e);
            // 如果处理出错，返回原始内容
            return content;
        }
        
        return processedContent;
    }

    /**
     * 验证商品导入数据
     */
    private void validateProductImportData(ProductImportVo data, int rowIndex) {
        if (StrUtil.isBlank(data.getName())) {
            throw new CrmebException("第" + rowIndex + "行：商品名称不能为空");
        }
        if (StrUtil.isBlank(data.getIntro())) {
            throw new CrmebException("第" + rowIndex + "行：商品简介不能为空");
        }
        // 旧的ID验证已移除，现在使用名称验证
        if (StrUtil.isBlank(data.getUnitName())) {
            throw new CrmebException("第" + rowIndex + "行：单位名不能为空");
        }
        if (StrUtil.isBlank(data.getImage())) {
            throw new CrmebException("第" + rowIndex + "行：商品主图不能为空");
        }

        // 验证必填的名称字段
        if (StrUtil.isBlank(data.getCategoryName())) {
            throw new CrmebException("第" + rowIndex + "行：平台分类名称不能为空");
        }
        if (StrUtil.isBlank(data.getMerCategoryName())) {
            throw new CrmebException("第" + rowIndex + "行：商户分类名称不能为空");
        }
        if (StrUtil.isBlank(data.getBrandName())) {
            throw new CrmebException("第" + rowIndex + "行：品牌名称不能为空");
        }
    }

    /**
     * 将商品基本信息和规格配置转换为ProductAddRequest
     */
    private ProductAddRequest convertToProductAddRequestWithSpecs(ProductImportVo basicInfo,
                                                                  List<ProductSpecConfigVo> specConfigs,
                                                                  Integer merId) {
        // 先使用基本信息创建请求
        ProductAddRequest request = convertToProductAddRequest(basicInfo, merId);

        // 判断是否为多规格商品：根据商品名称查询到的规格配置数量判断
        // 如果有多条规格配置，或者单条配置但不是"默认规格"，则认为是多规格
        boolean isMultiSpec = specConfigs.size() > 1 ||
                (specConfigs.size() == 1 && !"默认规格".equals(specConfigs.get(0).getSpecCombination()));

        if (isMultiSpec) {
            // 多规格商品处理
            request.setSpecType(true);

            // 解析规格属性
            parseAndSetSpecAttributes(request, specConfigs);

            // 创建规格值列表
            List<ProductAttrValueAddRequest> attrValueList = createAttrValueListFromSpecs(specConfigs);
            request.setAttrValueList(attrValueList);

        } else {
            // 单规格商品处理
            request.setSpecType(false);

            // 使用规格配置中的价格和库存信息（如果有的话）
            ProductSpecConfigVo singleSpec = specConfigs.get(0);
            updateRequestFromSingleSpec(request, singleSpec);

            // 创建默认规格值
            List<ProductAttrValueAddRequest> attrValueList = createAttrValueListFromSingleSpec(singleSpec, basicInfo);
            request.setAttrValueList(attrValueList);
        }

        return request;
    }

    /**
     * 解析并设置规格属性
     */
    private void parseAndSetSpecAttributes(ProductAddRequest request, List<ProductSpecConfigVo> specConfigs) {
        // 使用LinkedHashMap保持属性顺序
        Map<String, Set<String>> attrNameToValuesMap = new LinkedHashMap<>();

        // 解析所有规格组合，提取规格属性名称和值
        for (ProductSpecConfigVo spec : specConfigs) {
            String specCombination = spec.getSpecCombination();
            if (StrUtil.isBlank(specCombination) || "默认规格".equals(specCombination)) {
                continue;
            }

            // 解析规格组合：属性名1:属性值1,属性名2:属性值2
            String[] specPairs = specCombination.split(",");
            for (String pair : specPairs) {
                String[] nameValue = pair.trim().split(":", 2);
                if (nameValue.length == 2) {
                    String attrName = nameValue[0].trim();
                    String attrValue = nameValue[1].trim();

                    if (StrUtil.isNotBlank(attrName) && StrUtil.isNotBlank(attrValue)) {
                        attrNameToValuesMap.computeIfAbsent(attrName, k -> new LinkedHashSet<>()).add(attrValue);
                    }
                }
            }
        }

        List<ProductAttrAddRequest> attrList = new ArrayList<>();
        int sort = 0;

        // 为每个属性名创建规格属性
        for (Map.Entry<String, Set<String>> entry : attrNameToValuesMap.entrySet()) {
            String attrName = entry.getKey();
            Set<String> attrValues = entry.getValue();

            ProductAttrAddRequest attr = new ProductAttrAddRequest();
            attr.setAttributeName(attrName); // 使用真实的属性名称，不再写死
            attr.setIsShowImage(false);
            attr.setSort(sort++);

            List<ProductAttrOptionAddRequest> options = new ArrayList<>();
            int optionSort = 0;
            for (String value : attrValues) {
                ProductAttrOptionAddRequest option = new ProductAttrOptionAddRequest();
                option.setOptionName(value);
                option.setImage(""); // 规格属性图片暂时为空
                option.setSort(optionSort++);
                options.add(option);
            }
            attr.setOptionList(options);
            attrList.add(attr);
        }

        request.setAttrList(attrList);
    }

    /**
     * 从规格配置创建规格值列表
     */
    private List<ProductAttrValueAddRequest> createAttrValueListFromSpecs(List<ProductSpecConfigVo> specConfigs) {
        List<ProductAttrValueAddRequest> attrValueList = new ArrayList<>();

        for (ProductSpecConfigVo spec : specConfigs) {
            ProductAttrValueAddRequest attrValue = new ProductAttrValueAddRequest();

            // 从规格组合中提取属性值（去除属性名，只保留属性值）
            String attrValueStr = extractAttrValues(spec.getSpecCombination());
            attrValue.setAttrValue(attrValueStr);
            attrValue.setImage(StrUtil.isNotBlank(spec.getSpecImage()) ? spec.getSpecImage() : "");

            // 价格信息
            attrValue.setPrice(StrUtil.isNotBlank(spec.getPrice()) ? new BigDecimal(spec.getPrice()) : BigDecimal.ZERO);
            attrValue.setOtPrice(StrUtil.isNotBlank(spec.getOtPrice()) ? new BigDecimal(spec.getOtPrice()) : BigDecimal.ZERO);
            attrValue.setCost(StrUtil.isNotBlank(spec.getCost()) ? new BigDecimal(spec.getCost()) : BigDecimal.ZERO);
            attrValue.setVipPrice(StrUtil.isNotBlank(spec.getVipPrice()) ? new BigDecimal(spec.getVipPrice()) : BigDecimal.ZERO);

            // 库存和重量体积
            attrValue.setStock(StrUtil.isNotBlank(spec.getStock()) ? Integer.parseInt(spec.getStock()) : 0);
            attrValue.setWeight(StrUtil.isNotBlank(spec.getWeight()) ? new BigDecimal(spec.getWeight()) : BigDecimal.ZERO);
            attrValue.setVolume(StrUtil.isNotBlank(spec.getVolume()) ? new BigDecimal(spec.getVolume()) : BigDecimal.ZERO);

            // 商品编码
            attrValue.setBarCode(StrUtil.isNotBlank(spec.getBarCode()) ? spec.getBarCode() : "");
            attrValue.setItemNumber(StrUtil.isNotBlank(spec.getItemNumber()) ? spec.getItemNumber() : "");

            // 返佣信息
            attrValue.setBrokerage(StrUtil.isNotBlank(spec.getBrokerage()) ?
                    Double.valueOf(spec.getBrokerage()).intValue() : 0);
            attrValue.setBrokerageTwo(StrUtil.isNotBlank(spec.getBrokerageTwo()) ?
                    Double.valueOf(spec.getBrokerageTwo()).intValue() : 0);

            // 显示设置
            attrValue.setIsDefault(StrUtil.isNotBlank(spec.getIsDefault()) ? Boolean.parseBoolean(spec.getIsDefault()) : false);
            attrValue.setIsShow(StrUtil.isNotBlank(spec.getIsShow()) ? Boolean.parseBoolean(spec.getIsShow()) : true);

            attrValueList.add(attrValue);
        }

        return attrValueList;
    }

    /**
     * 从规格组合中提取属性值，生成JSON格式
     * 输入：颜色:红色,尺寸:L
     * 输出：{"颜色":"红色","尺寸":"L"}
     */
    private String extractAttrValues(String specCombination) {
        if (StrUtil.isBlank(specCombination) || "默认规格".equals(specCombination)) {
            logger.debug("规格组合为空或默认规格，返回：默认JSON");
            return "{\"规格\":\"默认\"}";
        }

        Map<String, String> attrMap = new LinkedHashMap<>();
        String[] specPairs = specCombination.split(",");

        logger.debug("开始解析规格组合：{}，分割后有{}个属性对", specCombination, specPairs.length);

        for (String pair : specPairs) {
            String[] nameValue = pair.trim().split(":", 2);
            if (nameValue.length == 2) {
                String attrName = nameValue[0].trim();
                String attrValue = nameValue[1].trim();
                if (StrUtil.isNotBlank(attrName) && StrUtil.isNotBlank(attrValue)) {
                    attrMap.put(attrName, attrValue);
                    logger.debug("提取属性：{} = {}", attrName, attrValue);
                }
            } else {
                logger.warn("规格对格式错误，跳过：{}", pair);
            }
        }

        // 转换为JSON字符串
        try {
            String result = JSON.toJSONString(attrMap);
            logger.debug("最终生成的JSON格式属性值：{}", result);
            return result;
        } catch (Exception e) {
            logger.error("转换属性值为JSON格式失败：{}", e.getMessage());
            // 降级处理，返回简单格式
            return "{\"规格\":\"" + specCombination + "\"}";
        }
    }

    /**
     * 从单规格配置更新请求（注：价格相关字段在attrValueList中处理）
     */
    private void updateRequestFromSingleSpec(ProductAddRequest request, ProductSpecConfigVo singleSpec) {
        // ProductAddRequest中没有价格字段，这些都通过attrValueList处理
        // 这里处理一些基本配置信息的验证和更新

        if (StrUtil.isNotBlank(singleSpec.getProductName())) {
            logger.debug("处理单规格商品：{}", singleSpec.getProductName());

            // 验证商品名称是否匹配
            if (!singleSpec.getProductName().equals(request.getName())) {
                logger.warn("规格配置中的商品名称[{}]与基本信息中的商品名称[{}]不匹配",
                        singleSpec.getProductName(), request.getName());
            }

            // 对于单规格商品，设置规格类型为false
            request.setSpecType(false);

            // 如果规格配置中有默认选中状态，可以在这里处理一些显示相关的配置
            if (StrUtil.isNotBlank(singleSpec.getIsDefault()) && "true".equals(singleSpec.getIsDefault())) {
                logger.debug("单规格商品默认选中配置：{}", singleSpec.getIsDefault());
            }

            // 如果规格配置中有显示状态配置
            if (StrUtil.isNotBlank(singleSpec.getIsShow()) && "false".equals(singleSpec.getIsShow())) {
                logger.debug("单规格商品显示状态：{}", singleSpec.getIsShow());
            }

            // 记录处理信息
            logger.info("单规格商品[{}]配置处理完成", singleSpec.getProductName());
        }
    }

    /**
     * 从单规格配置创建规格值列表
     */
    private List<ProductAttrValueAddRequest> createAttrValueListFromSingleSpec(ProductSpecConfigVo singleSpec, ProductImportVo basicInfo) {
        List<ProductAttrValueAddRequest> attrValueList = new ArrayList<>();
        ProductAttrValueAddRequest attrValue = new ProductAttrValueAddRequest();

        // 单规格商品的attrValue也使用JSON格式
        if (StrUtil.isNotBlank(singleSpec.getSpecCombination()) && !"默认规格".equals(singleSpec.getSpecCombination())) {
            // 如果规格配置中有具体的规格组合，使用extractAttrValues处理
            attrValue.setAttrValue(extractAttrValues(singleSpec.getSpecCombination()));
        } else {
            // 默认规格
            attrValue.setAttrValue("{\"规格\":\"默认\"}");
        }

        attrValue.setImage(StrUtil.isNotBlank(singleSpec.getSpecImage()) ? singleSpec.getSpecImage() : basicInfo.getImage());

        // 价格信息（优先使用规格配置，否则使用基本信息）
        attrValue.setPrice(StrUtil.isNotBlank(singleSpec.getPrice()) ?
                new BigDecimal(singleSpec.getPrice()) : new BigDecimal(singleSpec.getPrice()));
        attrValue.setOtPrice(StrUtil.isNotBlank(singleSpec.getOtPrice()) ?
                new BigDecimal(singleSpec.getOtPrice()) : new BigDecimal(singleSpec.getOtPrice()));
        attrValue.setCost(StrUtil.isNotBlank(singleSpec.getCost()) ?
                new BigDecimal(singleSpec.getCost()) : new BigDecimal(singleSpec.getCost()));
        attrValue.setVipPrice(StrUtil.isNotBlank(singleSpec.getVipPrice()) ?
                new BigDecimal(singleSpec.getVipPrice()) : new BigDecimal(singleSpec.getVipPrice()));

        // 库存信息
        attrValue.setStock(StrUtil.isNotBlank(singleSpec.getStock()) ?
                Integer.parseInt(singleSpec.getStock()) : Integer.parseInt(singleSpec.getStock()));

        // 重量体积
        attrValue.setWeight(StrUtil.isNotBlank(singleSpec.getWeight()) ?
                new BigDecimal(singleSpec.getWeight()) : new BigDecimal(singleSpec.getWeight()));
        attrValue.setVolume(StrUtil.isNotBlank(singleSpec.getVolume()) ?
                new BigDecimal(singleSpec.getVolume()) : new BigDecimal(singleSpec.getVolume()));

        // 编码
        attrValue.setBarCode(StrUtil.isNotBlank(singleSpec.getBarCode()) ?
                singleSpec.getBarCode() : singleSpec.getBarCode());
        attrValue.setItemNumber(StrUtil.isNotBlank(singleSpec.getItemNumber()) ?
                singleSpec.getItemNumber() : singleSpec.getItemNumber());

        // 返佣信息
        attrValue.setBrokerage(StrUtil.isNotBlank(singleSpec.getBrokerage()) ?
                Double.valueOf(singleSpec.getBrokerage()).intValue() : 0);
        attrValue.setBrokerageTwo(StrUtil.isNotBlank(singleSpec.getBrokerageTwo()) ?
                Double.valueOf(singleSpec.getBrokerageTwo()).intValue() : 0);

        // 显示设置
        attrValue.setIsDefault(StrUtil.isNotBlank(singleSpec.getIsDefault()) ? Boolean.parseBoolean(singleSpec.getIsDefault()) : true);
        attrValue.setIsShow(StrUtil.isNotBlank(singleSpec.getIsShow()) ? Boolean.parseBoolean(singleSpec.getIsShow()) : true);

        attrValueList.add(attrValue);
        return attrValueList;
    }

    /**
     * 转换为ProductAddRequest
     */
    private ProductAddRequest convertToProductAddRequest(ProductImportVo importVo, Integer merId) {
        ProductAddRequest request = new ProductAddRequest();

        // 基本信息
        request.setName(importVo.getName());
        request.setIntro(importVo.getIntro());
        request.setKeyword(StrUtil.isNotBlank(importVo.getKeyword()) ? importVo.getKeyword() : "");

        // 根据名称查找对应的ID
        Integer categoryId = findCategoryIdByName(importVo.getCategoryName());
        request.setCategoryId(categoryId);

        String cateId = findMerchantCategoryIdByName(importVo.getMerCategoryName(), merId);
        request.setCateId(cateId);

        Integer brandId = findBrandIdByName(importVo.getBrandName(), merId);
        request.setBrandId(brandId);

        request.setUnitName(importVo.getUnitName());
        request.setImage(importVo.getImage());
        request.setSliderImage(StrUtil.isNotBlank(importVo.getSliderImage()) ? importVo.getSliderImage() : importVo.getImage());
        request.setContent(StrUtil.isNotBlank(importVo.getContent()) ? importVo.getContent() : "");

        // 商品类型
        request.setType(StrUtil.isNotBlank(importVo.getType()) ? Integer.parseInt(importVo.getType()) : ProductConstants.PRODUCT_TYPE_NORMAL);

        // 佣金设置（这个字段还在基本信息中）
        Boolean isSub = StrUtil.isNotBlank(importVo.getIsSub()) ? Boolean.parseBoolean(importVo.getIsSub()) : false;

        // 配送设置
        String deliveryMethod = StrUtil.isNotBlank(importVo.getDeliveryMethod()) ? importVo.getDeliveryMethod() : "1";
        Boolean postageSwith = StrUtil.isNotBlank(importVo.getPostageSwith()) ? Boolean.parseBoolean(importVo.getPostageSwith()) : false;
        Boolean cityDeliverySwith = StrUtil.isNotBlank(importVo.getCityDeliverySwith()) ? Boolean.parseBoolean(importVo.getCityDeliverySwith()) : false;

        // 运费模板
        Integer tempId = findTempIdByName(importVo.getTempName(), merId);

        // 商品设置
        Boolean isPaidMember = StrUtil.isNotBlank(importVo.getIsPaidMember()) ? Boolean.parseBoolean(importVo.getIsPaidMember()) : false;
        Boolean refundSwitch = StrUtil.isNotBlank(importVo.getRefundSwitch()) ? Boolean.parseBoolean(importVo.getRefundSwitch()) : true;
        Boolean limitSwith = StrUtil.isNotBlank(importVo.getLimitSwith()) ? Boolean.parseBoolean(importVo.getLimitSwith()) : false;
        Integer limitNum = limitSwith && StrUtil.isNotBlank(importVo.getLimitNum()) ? Integer.parseInt(importVo.getLimitNum()) : 0;
        Integer minNum = StrUtil.isNotBlank(importVo.getMinNum()) ? Integer.parseInt(importVo.getMinNum()) : 1;

        // 系统设置
        Boolean isAutoUp = StrUtil.isNotBlank(importVo.getIsAutoUp()) ? Boolean.parseBoolean(importVo.getIsAutoUp()) : false;
        Boolean isAutoSubmitAudit = StrUtil.isNotBlank(importVo.getIsAutoSubmitAudit()) ? Boolean.parseBoolean(importVo.getIsAutoSubmitAudit()) : false;

        // 设置所有属性
        request.setSort(0);
        request.setSpecType(false); // 默认单规格，具体规格信息在规格配置表中处理
        request.setIsSub(isSub);
        request.setIsAutoUp(isAutoUp);
        request.setIsAutoSubmitAudit(isAutoSubmitAudit);
        request.setDeliveryMethod(deliveryMethod);
        request.setRefundSwitch(refundSwitch);
        request.setTempId(tempId);
        request.setSystemFormId(0); // 默认系统表单
        request.setLimitSwith(limitSwith);
        request.setLimitNum(limitNum);
        request.setMinNum(minNum);
        request.setPostageSwith(postageSwith);
        request.setCityDeliverySwith(cityDeliverySwith);
        request.setIsPaidMember(isPaidMember);
//        //地址设置
//        request.setProvince(importVo.getProvince());
//        request.setCity(importVo.getCity());
//        request.setArea(importVo.getArea());
//        request.setStreet(importVo.getStreet());
//        //获取地址ID
//        if(StringUtil.isNotEmpty(importVo.getProvince())){
//            request.setProvinceCode(findCityByName(importVo.getProvince(),null,null, null).getRegionId());
//        }
//        if(StringUtil.isNotEmpty(importVo.getCity())){
//            request.setCityCode(findCityByName(importVo.getProvince(),importVo.getCity(),null, null).getRegionId());
//        }
//        if(StringUtil.isNotEmpty(importVo.getArea())){
//            request.setAreaCode(findCityByName(importVo.getProvince(),importVo.getCity(),importVo.getArea(), null).getRegionId());
//        }
//        if(StringUtil.isNotEmpty(importVo.getStreet())){
//            request.setStreetCode(findCityByName(importVo.getProvince(),importVo.getCity(),importVo.getArea(),importVo.getStreet()).getRegionId());
//        }
        // 创建基础单规格（规格信息将在convertToProductAddRequestWithSpecs中重新设置）
        List<ProductAttrAddRequest> attrList = createBasicSingleSpecAttrs();
        request.setAttrList(attrList);

        // 创建基础属性值（价格和库存信息将在convertToProductAddRequestWithSpecs中重新设置）
        List<ProductAttrValueAddRequest> attrValueList = createBasicAttrValues();
        request.setAttrValueList(attrValueList);

        return request;
    }

    private CityRegion findCityByName(String province, String city,String  area,String street) {
        LambdaQueryWrapper<CityRegion> lqw = Wrappers.lambdaQuery();
        if(StrUtil.isNotBlank(province)){
            lqw.eq(CityRegion::getRegionName, province);
        }
        if(StrUtil.isNotBlank(city)){
            lqw.eq(CityRegion::getRegionName, city);
        }
        if(StrUtil.isNotBlank(area)){
            lqw.eq(CityRegion::getRegionName, area);
        }
        if(StrUtil.isNotBlank(street)){
            lqw.eq(CityRegion::getRegionName, street);
        }
        return cityRegionService.getOne(lqw);
    }

    /**
     * 根据分类名称查找分类ID
     */
    private Integer findCategoryIdByName(String categoryName) {
        if (StrUtil.isBlank(categoryName)) {
            return 1; // 默认分类ID
        }

        try {
            // 使用 LambdaQueryWrapper 查询平台分类
            LambdaQueryWrapper<ProductCategory> lqw = Wrappers.lambdaQuery();
            lqw.eq(ProductCategory::getName, categoryName.trim());
            lqw.eq(ProductCategory::getIsDel, false);
            lqw.eq(ProductCategory::getIsShow, true);
            lqw.last(" limit 1");
            ProductCategory category = productCategoryService.getOne(lqw);
            return category != null ? category.getId() : 1;
        } catch (Exception e) {
            return 1; // 查找失败时返回默认值
        }
    }

    /**
     * 根据商户分类名称查找分类ID
     */
    private String findMerchantCategoryIdByName(String merCategoryName, Integer merId) {
        if (StrUtil.isBlank(merCategoryName)) {
            return "1"; // 默认分类ID
        }

        try {
            // 解析多级分类，如：食品>零食>坚果
            String[] categoryPath = merCategoryName.split(">");
            String categoryName = categoryPath[categoryPath.length - 1].trim(); // 取最后一级的名称

            // 查询商户分类
            LambdaQueryWrapper<MerchantProductCategory> lqw = Wrappers.lambdaQuery();
            lqw.eq(MerchantProductCategory::getName, categoryName);
            lqw.eq(MerchantProductCategory::getMerId, merId);
            lqw.eq(MerchantProductCategory::getIsDel, false);
            lqw.eq(MerchantProductCategory::getIsShow, true);
            lqw.last(" limit 1");

            // 使用注入的 MerchantProductCategoryService
            MerchantProductCategory category = merchantProductCategoryService.getOne(lqw);
            return category != null ? category.getId().toString() : "1";
        } catch (Exception e) {
            return "1"; // 查找失败时返回默认值
        }
    }

    /**
     * 根据品牌名称查找品牌ID
     */
    private Integer findBrandIdByName(String brandName, Integer merId) {
        if (StrUtil.isBlank(brandName)) {
            return 1; // 默认品牌ID
        }

        try {
            // 查询品牌 - 优先查询审核通过的品牌
            LambdaQueryWrapper<ProductBrand> lqw = Wrappers.lambdaQuery();
            lqw.eq(ProductBrand::getName, brandName.trim());
            lqw.eq(ProductBrand::getIsDel, false);
            lqw.eq(ProductBrand::getIsShow, true);
            // 先查平台品牌（auditStatus = 1 且 applyMerId 为空）或者查该商户申请的品牌
            lqw.and(wrapper -> wrapper
                    .and(subWrapper -> subWrapper.eq(ProductBrand::getAuditStatus, 1).isNull(ProductBrand::getApplyMerId))
                    .or(subWrapper -> subWrapper.eq(ProductBrand::getApplyMerId, merId).eq(ProductBrand::getAuditStatus, 1))
            );
            lqw.last(" limit 1");
            ProductBrand brand = productBrandService.getOne(lqw);
            return brand != null ? brand.getId() : 1;
        } catch (Exception e) {
            return 1; // 查找失败时返回默认值
        }
    }

    /**
     * 根据运费模板名称查找模板ID
     */
    private Integer findTempIdByName(String tempName, Integer merId) {
        if (StrUtil.isBlank(tempName)) {
            return 0; // 默认模板ID
        }

        try {
            // 查询运费模板
            LambdaQueryWrapper<ShippingTemplates> lqw = Wrappers.lambdaQuery();
            lqw.eq(ShippingTemplates::getName, tempName.trim());
            lqw.eq(ShippingTemplates::getMerId, merId);
            // lqw.eq(ShippingTemplates::getIsDel, false);
            lqw.last(" limit 1");
            ShippingTemplates template = shippingTemplatesService.getOne(lqw);
            return template != null ? template.getId() : 0;
        } catch (Exception e) {
            return 0; // 查找失败时返回默认值
        }
    }

    /**
     * 创建基础单规格属性（简化版本，规格信息将在后续处理中重新设置）
     */
    private List<ProductAttrAddRequest> createBasicSingleSpecAttrs() {
        List<ProductAttrAddRequest> attrList = CollUtil.newArrayList();
        ProductAttrAddRequest attr = new ProductAttrAddRequest();
        attr.setAttributeName("规格");
        attr.setIsShowImage(false);
        attr.setSort(0);

        List<ProductAttrOptionAddRequest> optionList = CollUtil.newArrayList();
        ProductAttrOptionAddRequest option = new ProductAttrOptionAddRequest();
        option.setOptionName("默认");
        option.setImage("");
        option.setSort(0);
        optionList.add(option);

        attr.setOptionList(optionList);
        attrList.add(attr);
        return attrList;
    }

    /**
     * 创建基础属性值（简化版本，具体规格值将在后续处理中重新设置）
     */
    private List<ProductAttrValueAddRequest> createBasicAttrValues() {
        List<ProductAttrValueAddRequest> attrValueList = CollUtil.newArrayList();
        ProductAttrValueAddRequest attrValue = new ProductAttrValueAddRequest();
        attrValue.setAttrValue("默认");
        // 设置默认值，具体值在规格配置中处理
        attrValue.setPrice(new BigDecimal("0.01"));
        attrValue.setOtPrice(new BigDecimal("0.01"));
        attrValue.setCost(BigDecimal.ZERO);
        attrValue.setVipPrice(new BigDecimal("0.01"));
        attrValue.setStock(0);
        attrValue.setWeight(BigDecimal.ZERO);
        attrValue.setVolume(BigDecimal.ZERO);
        attrValue.setBarCode("");
        attrValue.setItemNumber("");
        attrValue.setBrokerage(0);
        attrValue.setBrokerageTwo(0);
        attrValue.setIsShow(true);
        attrValue.setImage("");
        attrValueList.add(attrValue);
        return attrValueList;
    }

    /**
     * 设置excel下载响应头属性
     */
    private void setExcelRespProp(HttpServletResponse response, String rawFileName) throws UnsupportedEncodingException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(rawFileName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
    }

    /**
     * 下拉数据结构
     */
    private static class DropdownData {
        private List<String> productTypes;
        private List<String> categories;
        //商户分类
        private List<String> merchantCategories;
        private List<String> brands;
        private List<String> guarantees;
        private List<String> coupons;
        private List<String> tempNames;
        private List<String> provinces;
        private List<String> booleanOptions;
        private List<String> deliveryMethods;
        private List<String> specTypes;

        // 级联数据Map
        private Map<String, List<String>> cityMap;      // 省份 -> 城市列表
        private Map<String, List<String>> districtMap;  // 省份_城市 -> 区县列表
        private Map<String, List<String>> streetMap;    // 省份_城市_区县 -> 街道列表

        public List<String> getMerchantCategories() {
            return merchantCategories;
        }

        public void setMerchantCategories(List<String> merchantCategories) {
            this.merchantCategories = merchantCategories;
        }

        // Getters and Setters
        public List<String> getProductTypes() { return productTypes; }
        public void setProductTypes(List<String> productTypes) { this.productTypes = productTypes; }
        public List<String> getCategories() { return categories; }
        public void setCategories(List<String> categories) { this.categories = categories; }
        public List<String> getBrands() { return brands; }
        public void setBrands(List<String> brands) { this.brands = brands; }
        public List<String> getGuarantees() { return guarantees; }
        public void setGuarantees(List<String> guarantees) { this.guarantees = guarantees; }
        public List<String> getCoupons() { return coupons; }
        public void setCoupons(List<String> coupons) { this.coupons = coupons; }
        public List<String> getTempNames() { return tempNames; }
        public void setTempNames(List<String> tempNames) { this.tempNames = tempNames; }
        public List<String> getProvinces() { return provinces; }
        public void setProvinces(List<String> provinces) { this.provinces = provinces; }
        public List<String> getBooleanOptions() { return booleanOptions; }
        public void setBooleanOptions(List<String> booleanOptions) { this.booleanOptions = booleanOptions; }
        public List<String> getDeliveryMethods() { return deliveryMethods; }
        public void setDeliveryMethods(List<String> deliveryMethods) { this.deliveryMethods = deliveryMethods; }
        public List<String> getSpecTypes() { return specTypes; }
        public void setSpecTypes(List<String> specTypes) { this.specTypes = specTypes; }

        // 级联数据的Getters and Setters
        public Map<String, List<String>> getCityMap() { return cityMap; }
        public void setCityMap(Map<String, List<String>> cityMap) { this.cityMap = cityMap; }
        public Map<String, List<String>> getDistrictMap() { return districtMap; }
        public void setDistrictMap(Map<String, List<String>> districtMap) { this.districtMap = districtMap; }
        public Map<String, List<String>> getStreetMap() { return streetMap; }
        public void setStreetMap(Map<String, List<String>> streetMap) { this.streetMap = streetMap; }
    }

    /**
     * 获取下拉数据
     */
    private DropdownData getDropdownData() {
        DropdownData dropdownData = new DropdownData();

        // 商品类型
        dropdownData.setProductTypes(CollUtil.newArrayList("0", "1", "2", "4", "5", "6"));

        // 平台分类
        List<ProductCategory> categoryList = productCategoryService.list();
        dropdownData.setCategories(categoryList.stream().map(ProductCategory::getName).collect(Collectors.toList()));

        // 商户分类
        List<MerchantProductCategory> merCategoryList = merchantProductCategoryService.list();
        dropdownData.setMerchantCategories(merCategoryList.stream().map(MerchantProductCategory::getName).collect(Collectors.toList()));

        // 品牌列表
        List<ProductBrand> brandList = productBrandService.list();
        dropdownData.setBrands(brandList.stream().map(ProductBrand::getName).collect(Collectors.toList()));

        // 保障服务
        List<ProductGuarantee> guaranteeList = productGuaranteeService.list();
        dropdownData.setGuarantees(guaranteeList.stream().map(ProductGuarantee::getName).collect(Collectors.toList()));

        // 优惠券列表
        List<Coupon> couponList = couponService.list();
        dropdownData.setCoupons(couponList.stream().map(Coupon::getName).collect(Collectors.toList()));

        // 运费模板
        List<ShippingTemplates> templateList = shippingTemplatesService.list();
        dropdownData.setTempNames(templateList.stream().map(ShippingTemplates::getName).collect(Collectors.toList()));

//        // 省市区街道数据 - 级联数据 (使用缓存优化)
//        logger.info("开始获取省市区街道级联数据");
//
//        // 从缓存中获取省市区街道数据
//        Map<String, List<String>> cityMap = getCachedCityMap();
//        Map<String, List<String>> districtMap = getCachedDistrictMap();
//        Map<String, List<String>> streetMap = getCachedStreetMap();
//        List<String> provinces = getCachedProvinces();
//
//        // 如果缓存中没有数据，则从数据库查询并存入缓存
//        if (provinces == null || provinces.isEmpty() || cityMap == null || cityMap.isEmpty()) {
//            logger.info("缓存中未找到省市区街道数据，从数据库查询");
//
//            // 获取省份列表
//            List<CityVo> provinceList = cityRegionService.getProvinceList();
//            provinces = provinceList.stream().map(CityVo::getRegionName).collect(Collectors.toList());
//
//            // 获取所有城市数据（按省份分组）
//            cityMap = new LinkedHashMap<>();
//            districtMap = new LinkedHashMap<>();
//            streetMap = new LinkedHashMap<>();
//
//            for (CityVo province : provinceList) {
//                String provinceName = province.getRegionName();
//
//                // 获取该省份下的城市
//                List<CityVo> cityList = cityRegionService.getCityListByProvinceId(province.getRegionId());
//                List<String> cities = cityList.stream().map(CityVo::getRegionName).collect(Collectors.toList());
//                cityMap.put(provinceName, cities);
//
//                // 获取每个城市下的区县
//                for (CityVo city : cityList) {
//                    String cityKey = provinceName + "_" + city.getRegionName();
//
//                    List<CityVo> districtList = cityRegionService.getDistrictListByCityId(city.getRegionId());
//                    List<String> districts = districtList.stream().map(CityVo::getRegionName).collect(Collectors.toList());
//                    districtMap.put(cityKey, districts);
//
//                    // 获取每个区县下的街道
//                    for (CityVo district : districtList) {
//                        String districtKey = cityKey + "_" + district.getRegionName();
//
//                        List<CityVo> streetList = cityRegionService.getStreetListByDistrictId(district.getRegionId());
//                        List<String> streets = streetList.stream().map(CityVo::getRegionName).collect(Collectors.toList());
//                        streetMap.put(districtKey, streets);
//                    }
//                }
//            }
//
//            // 将数据存入缓存，设置过期时间为1小时
//            cacheCityData(provinces, cityMap, districtMap, streetMap);
//        }
//
//        dropdownData.setProvinces(provinces);
//        dropdownData.setCityMap(cityMap);
//        dropdownData.setDistrictMap(districtMap);
//        dropdownData.setStreetMap(streetMap);
//
//        logger.info("省市区街道级联数据获取完成，省份数：{}，城市组数：{}，区县组数：{}，街道组数：{}",
//            provinces.size(), cityMap.size(), districtMap.size(), streetMap.size());
//
//        // 布尔选项
//        dropdownData.setBooleanOptions(CollUtil.newArrayList("true", "false"));
//
//        // 配送方式
//        dropdownData.setDeliveryMethods(CollUtil.newArrayList("1", "2", "3", "4"));

        // 规格类型
        dropdownData.setSpecTypes(CollUtil.newArrayList("单规格", "多规格"));

        return dropdownData;
    }

    /**
     * 从缓存中获取省份列表
     */
    @SuppressWarnings("unchecked")
    private List<String> getCachedProvinces() {
        try {
            Object obj = redisUtil.get("export:provinces");
            return (List<String>) obj;
        } catch (Exception e) {
            logger.error("从缓存获取省份列表失败", e);
            return null;
        }
    }

    /**
     * 从缓存中获取城市映射
     */
    @SuppressWarnings("unchecked")
    private Map<String, List<String>> getCachedCityMap() {
        try {
            Object obj = redisUtil.get("export:city_map");
            return (Map<String, List<String>>) obj;
        } catch (Exception e) {
            logger.error("从缓存获取城市映射失败", e);
            return null;
        }
    }

    /**
     * 从缓存中获取区县映射
     */
    @SuppressWarnings("unchecked")
    private Map<String, List<String>> getCachedDistrictMap() {
        try {
            Object obj = redisUtil.get("export:district_map");
            return (Map<String, List<String>>) obj;
        } catch (Exception e) {
            logger.error("从缓存获取区县映射失败", e);
            return null;
        }
    }

    /**
     * 从缓存中获取街道映射
     */
    @SuppressWarnings("unchecked")
    private Map<String, List<String>> getCachedStreetMap() {
        try {
            Object obj = redisUtil.get("export:street_map");
            return (Map<String, List<String>>) obj;
        } catch (Exception e) {
            logger.error("从缓存获取街道映射失败", e);
            return null;
        }
    }

    /**
     * 将省市区街道数据存入缓存
     */
    private void cacheCityData(List<String> provinces, Map<String, List<String>> cityMap,
                               Map<String, List<String>> districtMap, Map<String, List<String>> streetMap) {
        try {
            // 设置为永不过期
            redisUtil.set("export:provinces", provinces);
            redisUtil.set("export:city_map", cityMap);
            redisUtil.set("export:district_map", districtMap);
            redisUtil.set("export:street_map", streetMap);
            logger.info("省市区街道数据已缓存（永不过期）");
        } catch (Exception e) {
            logger.error("缓存省市区街道数据失败", e);
        }
    }


    /**
     * 清除省市区街道数据缓存
     */
    public void clearCityDataCache() {
        try {
            redisUtil.delete("export:provinces");
            redisUtil.delete("export:city_map");
            redisUtil.delete("export:district_map");
            redisUtil.delete("export:street_map");
            logger.info("省市区街道数据缓存已清除");
        } catch (Exception e) {
            logger.error("清除省市区街道数据缓存失败", e);
        }
    }

    /**
     * 生成商品导出文件并返回文件URL
     */
    @Override
    public String generateProductExport(PlatProductSearchRequest request) {
        // 服务器存储地址
        String rootPath = crmebConfig.getImagePath().trim();
        // 模块
        String modelPath = "public/export/";
        // 类型
        String type = UploadConstants.UPLOAD_AFTER_FILE_KEYWORD + "/";

        // 变更文件名
        String newFileName = "商品导出_" + CrmebDateUtil.nowDateTime(DateConstants.DATE_TIME_FORMAT_NUM) + ".xlsx";
        // 创建目标文件的名称，规则：类型/模块/年/月/日/文件名
        String webPath = type + modelPath + CrmebDateUtil.nowDate("yyyy/MM/dd") + "/";
        // 文件分隔符转化为当前系统的格式
        String destPath = FilenameUtils.separatorsToSystem(rootPath + webPath) + newFileName;

        // 设置超大分页参数，避免分页限制
        request.setPage(1);
        request.setLimit(10000);
        MerProductSearchRequest platProductSearchRequest = new MerProductSearchRequest();
        BeanUtils.copyProperties(request, platProductSearchRequest);
        SystemAdmin admin = SecurityUtil.getLoginUserVo().getUser();
        PageInfo<AdminProductListResponse> pageInfo = productService.getAdminList(platProductSearchRequest, admin);
        List<AdminProductListResponse> productList = pageInfo.getList();

        if (CollUtil.isEmpty(productList)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "没有可导出的数据！");
        }

        // 获取分类映射
        List<Integer> categoryIds = productList.stream()
                .map(AdminProductListResponse::getCategoryId)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, String> categoryMap = CollUtil.newHashMap();
        if (CollUtil.isNotEmpty(categoryIds)) {
            List<ProductCategory> categoryList = productCategoryService.findByIdList(categoryIds);
            categoryMap = categoryList.stream()
                    .collect(Collectors.toMap(ProductCategory::getId, ProductCategory::getName));
        }
        // 构建导出数据
        List<ProductExcelVo> voList = CollUtil.newArrayList();
        for (AdminProductListResponse product : productList) {
            ProductExcelVo vo = new ProductExcelVo();
            vo.setId(product.getId().toString());
            vo.setName(product.getName());
            vo.setCategoryName(categoryMap.getOrDefault(product.getCategoryId(), ""));
            vo.setProductType(getProductTypeStr(product.getType()));
            vo.setMerchantName(admin.getRealName());
            vo.setMerchantType( "自营");
            vo.setPrice(product.getPrice() != null ? product.getPrice().toString() : "0.00");
            vo.setSales(product.getSales() != null ? product.getSales().toString() : "0");
            vo.setStock(product.getStock() != null ? product.getStock().toString() : "0");
            vo.setFicti(product.getFicti() != null ? product.getFicti().toString() : "0");
            vo.setIsShow(getProductStatusStr(product));
            vo.setAuditStatus(getAuditStatusStr(product.getAuditStatus()));
            vo.setReason(StrUtil.isBlank(product.getReason()) ? "" : product.getReason());
            vo.setSpecType(product.getSpecType() != null ? (product.getSpecType() ? "多规格" : "单规格") : "");
            vo.setCreateTime(CrmebDateUtil.nowDateTime(DateConstants.DATE_TIME_FORMAT_NUM));
            vo.setUpdateTime(CrmebDateUtil.nowDateTime(DateConstants.DATE_TIME_FORMAT_NUM));
            voList.add(vo);
        }

        File file = null;
        try {
            // 创建本地文件
            file = UploadUtil.createFile(destPath);

            // 使用EasyExcel写入数据
            EasyExcel.write(file, ProductExcelVo.class)
                    .sheet("商品数据")
                    .doWrite(voList);

            // 创建附件记录
            SystemAttachment systemAttachment = new SystemAttachment();
            systemAttachment.setName(newFileName);
            systemAttachment.setSattDir(webPath + newFileName);
            systemAttachment.setAttSize(String.valueOf(file.length()));
            systemAttachment.setAttType("xlsx");
            systemAttachment.setImageType(1); // 默认本地
            systemAttachment.setPid(0);
            systemAttachment.setOwner(-1); // 平台文件

            // 获取上传类型配置
            String uploadType = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_UPLOAD_TYPE);
            Integer uploadTypeInt = Integer.parseInt(uploadType);

            if (uploadTypeInt.equals(1)) {
                // 本地存储
                systemAttachmentService.save(systemAttachment);
                return systemAttachmentService.prefixFile(systemAttachment.getSattDir());
            }

            // 云存储处理
            CloudVo cloudVo = new CloudVo();
            String fileIsSave = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_FILE_IS_SAVE);

            switch (uploadTypeInt) {
                case 2: // 七牛云
                    systemAttachment.setImageType(2);
                    cloudVo.setDomain(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_UPLOAD_URL));
                    cloudVo.setAccessKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_ACCESS_KEY));
                    cloudVo.setSecretKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_SECRET_KEY));
                    cloudVo.setBucketName(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_STORAGE_NAME));
                    cloudVo.setRegion(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_STORAGE_REGION));
                    try {
                        Configuration cfg = new Configuration(Region.autoRegion());
                        UploadManager uploadManager = new UploadManager(cfg);
                        Auth auth = Auth.create(cloudVo.getAccessKey(), cloudVo.getSecretKey());
                        String upToken = auth.uploadToken(cloudVo.getBucketName());
                        qiNiuService.uploadFile(uploadManager, upToken, systemAttachment.getSattDir(), destPath, file);
                    } catch (Exception e) {
                        logger.error("七牛云上传失败：" + e.getMessage());
                    }
                    break;
                case 3: // 阿里云OSS
                    systemAttachment.setImageType(3);
                    cloudVo.setDomain(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_UPLOAD_URL));
                    cloudVo.setAccessKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_ACCESS_KEY));
                    cloudVo.setSecretKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_SECRET_KEY));
                    cloudVo.setBucketName(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_STORAGE_NAME));
                    cloudVo.setRegion(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_STORAGE_REGION));
                    try {
                        ossService.upload(cloudVo, systemAttachment.getSattDir(), destPath, file);
                    } catch (Exception e) {
                        logger.error("阿里云OSS上传失败：" + e.getMessage());
                    }
                    break;
                case 5: // 京东云
                    systemAttachment.setImageType(5);
                    String bucket = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_JD_BUCKET_NAME);
                    try {
                        jdCloudService.uploadFile(systemAttachment.getSattDir(), destPath, bucket);
                    } catch (Exception e) {
                        logger.error("京东云上传失败：" + e.getMessage());
                    }
                    break;
            }

            // 保存附件记录
            systemAttachmentService.save(systemAttachment);

            // 如果不保存本地文件，删除本地文件
            if (!fileIsSave.equals("1") && file != null) {
                file.delete();
            }

            // 返回文件访问URL
            return systemAttachmentService.prefixFile(systemAttachment.getSattDir());

        } catch (Exception e) {
            logger.error("生成商品导出文件失败", e);
            throw new CrmebException("生成商品导出文件失败：" + e.getMessage());
        }
    }

    /**
     * 创建带样式的订单导出Excel
     * @param file 输出文件
     * @param exportData 导出数据
     * @param sheetName 工作表名称
     */
    private void createStyledOrderExcel(File file, List<List<String>> exportData, String sheetName) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(sheetName);

        // 创建样式
        XSSFCellStyle titleStyle = createOrderTitleStyle(workbook);
        XSSFCellStyle headerStyle = createOrderHeaderStyle(workbook);
        XSSFCellStyle dataStyle = createOrderDataStyle(workbook);
        XSSFCellStyle numberStyle = createOrderNumberStyle(workbook);

        // 创建标题行
        XSSFRow titleRow = sheet.createRow(0);
        titleRow.setHeight((short) 800);
        XSSFCell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("订单数据导出报表");
        titleCell.setCellStyle(titleStyle);

        // 合并标题单元格
        CellRangeAddress titleRange = new CellRangeAddress(0, 0, 0, exportData.get(0).size() - 1);
        sheet.addMergedRegion(titleRange);

        // 添加导出时间行
        XSSFRow timeRow = sheet.createRow(1);
        timeRow.setHeight((short) 400);
        XSSFCell timeCell = timeRow.createCell(0);
        timeCell.setCellValue("导出时间：" + CrmebDateUtil.nowDateTime("yyyy-MM-dd HH:mm:ss"));
        timeCell.setCellStyle(dataStyle);

        // 合并时间单元格
        CellRangeAddress timeRange = new CellRangeAddress(1, 1, 0, exportData.get(0).size() - 1);
        sheet.addMergedRegion(timeRange);

        // 空行
        sheet.createRow(2);

        // 创建表头
        List<String> headers = exportData.get(0);
        XSSFRow headerRow = sheet.createRow(3);
        headerRow.setHeight((short) 600);

        for (int i = 0; i < headers.size(); i++) {
            XSSFCell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers.get(i));
            headerCell.setCellStyle(headerStyle);
        }

        // 创建数据行
        for (int rowIndex = 1; rowIndex < exportData.size(); rowIndex++) {
            List<String> rowData = exportData.get(rowIndex);
            XSSFRow dataRow = sheet.createRow(rowIndex + 3);
            dataRow.setHeight((short) 450);

            for (int colIndex = 0; colIndex < rowData.size(); colIndex++) {
                XSSFCell dataCell = dataRow.createCell(colIndex);
                String cellValue = rowData.get(colIndex);

                // 设置单元格值和样式
                if (colIndex == 4) { // 实际支付金额列
                    try {
                        double numValue = Double.parseDouble(cellValue);
                        dataCell.setCellValue(numValue);
                        dataCell.setCellStyle(numberStyle);
                    } catch (NumberFormatException e) {
                        dataCell.setCellValue(cellValue);
                        dataCell.setCellStyle(dataStyle);
                    }
                } else {
                    dataCell.setCellValue(cellValue);
                    dataCell.setCellStyle(dataStyle);
                }
            }
        }

        // 设置列宽自适应
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
            // 获取自动调整后的列宽
            int autoWidth = sheet.getColumnWidth(i);
            // 设置最小和最大宽度限制
            int minWidth = 2000;  // 最小宽度
            int maxWidth = 8000;  // 最大宽度

            if (autoWidth < minWidth) {
                sheet.setColumnWidth(i, minWidth);
            } else if (autoWidth > maxWidth) {
                sheet.setColumnWidth(i, maxWidth);
            }
        }

        // 特殊列宽设置
        if (headers.size() > 1) sheet.setColumnWidth(1, 4500);  // 订单号
        if (headers.size() > 11) sheet.setColumnWidth(11, 6000); // 商品信息
        if (headers.size() > 14) sheet.setColumnWidth(14, 6000); // 收货地址
        if (headers.size() > 15) sheet.setColumnWidth(15, 4000); // 用户备注
        if (headers.size() > 16) sheet.setColumnWidth(16, 4000); // 商户备注

        // 设置打印属性
        sheet.setFitToPage(true);
        sheet.getPrintSetup().setFitWidth((short) 1);
        sheet.getPrintSetup().setFitHeight((short) 0);

        // 设置冻结窗格（冻结表头）
        sheet.createFreezePane(0, 4);

        // 写入文件
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
        } finally {
            workbook.close();
        }
    }

    /**
     * 创建订单标题样式
     */
    private XSSFCellStyle createOrderTitleStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();

        // 字体设置
        font.setBold(true);
        font.setFontHeightInPoints((short) 18);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        font.setFontName("微软雅黑");

        // 样式设置
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 边框设置
        style.setBorderTop(BorderStyle.THICK);
        style.setBorderBottom(BorderStyle.THICK);
        style.setBorderLeft(BorderStyle.THICK);
        style.setBorderRight(BorderStyle.THICK);
        style.setTopBorderColor(IndexedColors.DARK_BLUE.getIndex());
        style.setBottomBorderColor(IndexedColors.DARK_BLUE.getIndex());
        style.setLeftBorderColor(IndexedColors.DARK_BLUE.getIndex());
        style.setRightBorderColor(IndexedColors.DARK_BLUE.getIndex());

        return style;
    }

    /**
     * 创建订单表头样式
     */
    private XSSFCellStyle createOrderHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();

        // 字体设置
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontName("微软雅黑");

        // 样式设置
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setWrapText(true);

        // 边框设置
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.WHITE.getIndex());
        style.setBottomBorderColor(IndexedColors.WHITE.getIndex());
        style.setLeftBorderColor(IndexedColors.WHITE.getIndex());
        style.setRightBorderColor(IndexedColors.WHITE.getIndex());

        return style;
    }

    /**
     * 创建订单数据样式
     */
    private XSSFCellStyle createOrderDataStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();

        // 字体设置
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setFontName("微软雅黑");

        // 样式设置
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);

        // 边框设置
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());

        return style;
    }

    /**
     * 创建订单数字样式
     */
    private XSSFCellStyle createOrderNumberStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();

        // 字体设置
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setFontName("微软雅黑");

        // 样式设置
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 数字格式
        XSSFDataFormat dataFormat = workbook.createDataFormat();
        style.setDataFormat(dataFormat.getFormat("0.00"));

        // 边框设置
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());

        return style;
    }

    /**
     * 创建标题样式
     */
    private XSSFCellStyle createTitleStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 创建表头样式
     */
    private XSSFCellStyle createHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();

        // 设置背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 设置对齐方式
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 设置字体
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Arial");
        style.setFont(font);

        // 设置自动换行
        style.setWrapText(true);

        return style;
    }

    /**
     * 创建必填表头样式
     */
    private XSSFCellStyle createRequiredHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();

        // 设置背景色（浅红色）
        style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 设置对齐方式
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 设置字体
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Arial");
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);

        // 设置自动换行
        style.setWrapText(true);

        return style;
    }

    /**
     * 创建示例数据样式
     */
    private XSSFCellStyle createExampleStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();

        // 设置背景色（浅蓝色）
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 设置对齐方式
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 设置字体
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);

        // 设置自动换行
        style.setWrapText(true);

        return style;
    }

    /**
     * 创建数据字典内容样式
     */
    private XSSFCellStyle createDictContentStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();

        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 设置对齐方式
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 设置字体
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        return style;
    }

    /**
     * 创建注意事项样式
     */
    private XSSFCellStyle createNoticeStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();

        // 设置背景色（浅黄色）
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 设置对齐方式
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 设置字体
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        style.setFont(font);

        // 设置自动换行
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        font.setColor(IndexedColors.DARK_RED.getIndex());
        style.setFont(font);

        // 设置自动换行
        style.setWrapText(true);

        return style;
    }

    /**
     * 创建主数据表并设置数据验证
     */
    private void createMainSheetWithValidation(Sheet sheet, XSSFWorkbook workbook, DropdownData dropdownData) {
        // 创建样式
        XSSFCellStyle headerStyle = createHeaderStyle(workbook);
        XSSFCellStyle requiredHeaderStyle = createRequiredHeaderStyle(workbook);
        XSSFCellStyle exampleStyle = createExampleStyle(workbook);

        // 创建表头（严格按照ProductImportVo中定义的index顺序，价格库存等规格信息已移至专门的规格配置表）
        String[] headers = {
                "商品类型（0=普通商品,1-积分商品,2-虚拟商品,4=视频号,5-云盘商品,6-卡密商品）", // index 0
                "商品名称", // index 1
                "商户分类名称（多级用>分隔，如：食品>零食>坚果）", // index 2
                "平台分类名称", // index 3
                "品牌名称", // index 4
                "单位名", // index 5
                "是否包邮（true/false）", // index 6
                "运费模板名称（不包邮时必填）", // index 7
                "商品关键字（多个用逗号分隔）", // index 8
                "商品简介", // index 9
                "商品主图", // index 10
                "轮播图（多个用英文逗号分隔）", // index 11
                "商品详情（HTML内容）", // index 12
                "是否单独分佣（true/false）", // index 13
                "配送方式（1-商家配送,2-到店核销,3-快递发货,4-同城配送）", // index 14
                "是否支持同城配送（true/false）", // index 15
//            "所属省", // index 16
//            "所属市", // index 17
//            "所属区/县", // index 18
//            "所属街道", // index 19
//            "详细地址", // index 20
                "是否付费会员商品（true/false）", // index 16
                "是否支持退款（true/false）", // index 17
                "是否限购（true/false）", // index 18
                "限购数量（限购时必填）", // index 19
                "最少购买件数", // index 20
                "保障服务（多个用逗号分隔）", // index 21
                "绑定优惠券名称（多个用逗号分隔）", // index 22
                "是否自动上架（true/false）", // index 23
                "是否自动提审（true/false）", // index 24
                "备注说明" // index 25
        };

        // 必填字段的索引（从0开始） - 按照ProductImportVo的index顺序
        Integer[] requiredColumns = {0, 1, 2, 3, 4, 5,6,7,8, 9, 10,11,12, 13,14, 17,23,24};

        Row headerRow = sheet.createRow(0);
        headerRow.setHeight((short) 800); // 设置行高

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);

            // 设置表头文本，必填字段添加红色*
            boolean isRequired = Arrays.asList(requiredColumns).contains(i);
            if (isRequired) {
                // 使用富文本格式设置红色星号
                XSSFRichTextString richString = new XSSFRichTextString(headers[i] + " *");
                XSSFFont redFont = workbook.createFont();
                redFont.setColor(IndexedColors.RED.getIndex());
                redFont.setBold(true);
                richString.applyFont(headers[i].length(), headers[i].length() + 2, redFont);
                cell.setCellValue(richString);
                cell.setCellStyle(requiredHeaderStyle);
            } else {
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 设置列宽
            if (i < 10) {
                sheet.setColumnWidth(i, 4500); // 基础信息列宽度适中
            } else if (i >= 21 && i <= 23) {
                sheet.setColumnWidth(i, 8000); // 图片和详情列宽度较大
            } else {
                sheet.setColumnWidth(i, 3800); // 其他列稍小
            }
        }

        // 创建示例数据行
        Row exampleRow = sheet.createRow(1);
        exampleRow.setHeight((short) 600); // 设置行高

        String[] exampleData = {
                "0", "示例商品名称", "零食>坚果>开心果", "食品饮料", "示例品牌", "件",
                "false", "标准运费模板", "示例,关键字,商品", "这是一个示例商品的详细介绍",
                "请直接粘贴图片到此单元格", "请直接粘贴图片到此单元格", "<p>商品详情描述</p>",
                "false", "1,2", "true", "false", "true", "false", "10", "1",
                "正品保证,7天无理由退换", "新用户优惠券,满减券",
                "false", "false", "价格、库存、重量等规格信息请在商品规格配置表中填写"
        };

        for (int i = 0; i < exampleData.length; i++) {
            Cell cell = exampleRow.createCell(i);
            cell.setCellValue(exampleData[i]);
            cell.setCellStyle(exampleStyle); // 应用示例数据样式
        }

        // 设置数据验证（下拉列表） - 按照ProductImportVo的正确索引
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();

        // 商品类型下拉验证（index 0）
        setDropdownValidation(sheet, validationHelper, 0, dropdownData.getProductTypes());

        // 商户分类下拉验证（index 2）
        setDropdownValidation(sheet, validationHelper, 2, dropdownData.getMerchantCategories());

        // 平台分类下拉验证（index 3）
        setDropdownValidation(sheet, validationHelper, 3, dropdownData.getCategories());

        // 品牌下拉验证（index 4）
        setDropdownValidation(sheet, validationHelper, 4, dropdownData.getBrands());

        // 运费模板下拉验证（index 7）
        setDropdownValidation(sheet, validationHelper, 7, dropdownData.getTempNames());

        // 布尔值字段下拉验证 - 修正索引（移除价格库存等规格相关字段后）
        int[] booleanColumns = {6, 13, 15, 16,17,18,23,24}; // 是否包邮,是否单独分佣,是否支持同城配送,是否付费会员,是否支持退款,是否限购,是否自动上架,是否自动提审
        for (int col : booleanColumns) {
            setDropdownValidation(sheet, validationHelper, col, dropdownData.getBooleanOptions());
        }

        // 配送方式下拉验证（index 14）
        setDropdownValidation(sheet, validationHelper, 14, dropdownData.getDeliveryMethods());

//        // 省份下拉验证（index 16）
//        setDropdownValidation(sheet, validationHelper, 16, dropdownData.getProvinces());

//        // 实现省市区街道联动下拉
//        // 城市下拉验证（index 17）- 基于省份选择
//        setDataValidationForCity(sheet, validationHelper, 17);
//
//        // 区县下拉验证（index 18）- 基于城市选择
//        setDataValidationForDistrict(sheet, validationHelper, 18);
//
//        // 街道下拉验证（index 19）- 基于区县选择
//        setDataValidationForStreet(sheet, validationHelper, 19);

        // 保障服务下拉验证（index 21）
        setDropdownValidation(sheet, validationHelper, 21, dropdownData.getGuarantees());

        // 优惠券下拉验证（index 22）
        setDropdownValidation(sheet, validationHelper, 22, dropdownData.getCoupons());
    }

    /**
     * 设置城市下拉验证（基于省份选择）
     */
    private void setDataValidationForCity(Sheet sheet, DataValidationHelper helper, int columnIndex) {
        // 使用INDIRECT函数实现联动效果
        DataValidationConstraint constraint = helper.createFormulaListConstraint("INDIRECT(CONCATENATE(\"城市_\",$Q2))");

        // 设置验证范围（从第2行开始，到第1000行）
        CellRangeAddressList addressList = new CellRangeAddressList(1, 999, columnIndex, columnIndex);

        // 创建数据验证
        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox("输入错误", "请从下拉列表中选择！");

        // 应用验证
        sheet.addValidationData(validation);
    }

    /**
     * 设置区县下拉验证（基于城市选择）
     */
    private void setDataValidationForDistrict(Sheet sheet, DataValidationHelper helper, int columnIndex) {
        // 使用INDIRECT函数实现联动效果
        DataValidationConstraint constraint = helper.createFormulaListConstraint("INDIRECT(CONCATENATE(\"区县_\",$Q2,\"_\",$R2))");

        // 设置验证范围（从第2行开始，到第1000行）
        CellRangeAddressList addressList = new CellRangeAddressList(1, 999, columnIndex, columnIndex);

        // 创建数据验证
        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox("输入错误", "请从下拉列表中选择！");

        // 应用验证
        sheet.addValidationData(validation);
    }

    /**
     * 设置街道下拉验证（基于区县选择）
     */
    private void setDataValidationForStreet(Sheet sheet, DataValidationHelper helper, int columnIndex) {
        // 使用INDIRECT函数实现联动效果
        DataValidationConstraint constraint = helper.createFormulaListConstraint("INDIRECT(CONCATENATE(\"街道_\",$Q2,\"_\",$R2,\"_\",$S2))");

        // 设置验证范围（从第2行开始，到第1000行）
        CellRangeAddressList addressList = new CellRangeAddressList(1, 999, columnIndex, columnIndex);

        // 创建数据验证
        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox("输入错误", "请从下拉列表中选择！");

        // 应用验证
        sheet.addValidationData(validation);
    }

    /**
     * 设置下拉验证
     */
    private void setDropdownValidation(Sheet sheet, DataValidationHelper helper, int columnIndex, List<String> options) {
        if (CollUtil.isEmpty(options)) {
            return;
        }

        // 创建下拉列表约束
        DataValidationConstraint constraint = helper.createExplicitListConstraint(
                options.toArray(new String[0]));

        // 设置验证范围（从第2行开始，到第1000行）
        CellRangeAddressList addressList = new CellRangeAddressList(1, 999, columnIndex, columnIndex);

        // 创建数据验证
        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox("输入错误", "请从下拉列表中选择！");

        // 应用验证
        sheet.addValidationData(validation);
    }

    /**
     * 创建商品规格配置表
     */
    private void createSpecConfigSheet(Sheet sheet, XSSFWorkbook workbook, DropdownData dropdownData) {
        // 创建样式
        XSSFCellStyle headerStyle = createHeaderStyle(workbook);
        XSSFCellStyle requiredHeaderStyle = createRequiredHeaderStyle(workbook);
        XSSFCellStyle exampleStyle = createExampleStyle(workbook);
        XSSFCellStyle noticeStyle = createNoticeStyle(workbook);

        // 规格配置表头（按照新增商品页面的规格表格设计）
        String[] headers = {
                "商品名称 *", // 关联商品基本信息表中的商品
                "规格组合 *", // 如：红色+L号，蓝色+M号等
                "规格图片*", // 对应规格的图片
                "售价 *", // 规格售价
                "成本价", // 规格成本价
                "划线价", // 规格划线价（市场价）
                "会员价", // 规格会员价
                "库存 *", // 规格库存
                "商品编码", // 规格商品编码
                "商品条码", // 规格商品条码
                "重量(KG)", // 规格重量
                "体积(m³)", // 规格体积
                "一级返佣(%)", // 规格一级返佣
                "二级返佣(%)", // 规格二级返佣
                "默认选中", // 是否默认选中该规格（true/false）
                "是否显示" // 是否显示该规格（true/false）
        };

        // 必填字段索引
        Integer[] requiredColumns = {0, 1,2, 3, 7}; // 商品名称、规格组合、售价、库存

        // 创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.setHeight((short) 800);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);

            // 设置表头文本和样式
            boolean isRequired = Arrays.asList(requiredColumns).contains(i);
            if (isRequired) {
                XSSFRichTextString richString = new XSSFRichTextString(headers[i]);
                XSSFFont redFont = workbook.createFont();
                redFont.setColor(IndexedColors.RED.getIndex());
                redFont.setBold(true);
                richString.applyFont(headers[i].length() - 2, headers[i].length(), redFont);
                cell.setCellValue(richString);
                cell.setCellStyle(requiredHeaderStyle);
            } else {
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 设置列宽
            int[] columnWidths = {4000, 5000, 4000, 3000, 3000, 3000, 3000, 3000, 4000, 4000, 3000, 3000, 3500, 3500, 3000, 3000};
            if (i < columnWidths.length) {
                sheet.setColumnWidth(i, columnWidths[i]);
            }
        }

        // 创建示例数据行
        String[][] exampleData = {
                {"示例商品1", "红色+L号", "请直接粘贴图片", "99.00", "50.00", "139.00", "89.00", "100", "SP001-RL", "BC001-RL", "0.5", "0.001", "5.0", "3.0", "true", "true"},
                {"示例商品1", "红色+M号", "请直接粘贴图片", "99.00", "50.00", "139.00", "89.00", "100", "SP001-RM", "BC001-RM", "0.5", "0.001", "5.0", "3.0", "false", "true"},
                {"示例商品1", "蓝色+L号", "请直接粘贴图片", "99.00", "50.00", "139.00", "89.00", "100", "SP001-BL", "BC001-BL", "0.5", "0.001", "5.0", "3.0", "false", "true"},
                {"示例商品2", "默认规格", "请直接粘贴图片", "66.00", "30.00", "88.00", "59.00", "200", "SP002-DF", "BC002-DF", "0.3", "0.0005", "3.0", "2.0", "true", "true"}
        };

        for (int i = 0; i < exampleData.length; i++) {
            Row exampleRow = sheet.createRow(i + 1);
            exampleRow.setHeight((short) 600);

            for (int j = 0; j < exampleData[i].length; j++) {
                Cell cell = exampleRow.createCell(j);
                cell.setCellValue(exampleData[i][j]);
                cell.setCellStyle(exampleStyle);
            }
        }

        // 添加说明行
        Row noticeRow1 = sheet.createRow(exampleData.length + 2);
        Cell noticeCell1 = noticeRow1.createCell(0);
        noticeCell1.setCellValue("说明：");
        noticeCell1.setCellStyle(noticeStyle);

        Row noticeRow2 = sheet.createRow(exampleData.length + 3);
        Cell noticeCell2 = noticeRow2.createCell(0);
        noticeCell2.setCellValue("1. 商品名称必须与商品基本信息表中的商品名称完全一致");
        noticeCell2.setCellStyle(noticeStyle);

        Row noticeRow3 = sheet.createRow(exampleData.length + 4);
        Cell noticeCell3 = noticeRow3.createCell(0);
        noticeCell3.setCellValue("2. 规格组合格式：属性名1:属性值1,属性名2:属性值2（如：颜色:红色,尺寸:L）");
        noticeCell3.setCellStyle(noticeStyle);

        Row noticeRow4 = sheet.createRow(exampleData.length + 5);
        Cell noticeCell4 = noticeRow4.createCell(0);
        noticeCell4.setCellValue("3. 单规格商品的规格组合请填写\"默认规格\"");
        noticeCell4.setCellStyle(noticeStyle);

        Row noticeRow5 = sheet.createRow(exampleData.length + 6);
        Cell noticeCell5 = noticeRow5.createCell(0);
        noticeCell5.setCellValue("4. 多规格商品通过商品名称相同的多行数据进行判断");
        noticeCell5.setCellStyle(noticeStyle);

        Row noticeRow6 = sheet.createRow(exampleData.length + 7);
        Cell noticeCell6 = noticeRow6.createCell(0);
        noticeCell6.setCellValue("5. 图片可直接粘贴到单元格中");
        noticeCell6.setCellStyle(noticeStyle);

        // 设置数据验证
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();

        // 默认选中字段布尔值验证（第14列）
        setDropdownValidation(sheet, validationHelper, 14, dropdownData.getBooleanOptions());

        // 是否显示字段布尔值验证（第15列）
        setDropdownValidation(sheet, validationHelper, 15, dropdownData.getBooleanOptions());
    }

    /**
     * 创建数据字典说明表
     */
    private void createDictSheet(Sheet sheet, DropdownData dropdownData) {
        // 创建样式
        XSSFWorkbook workbook = (XSSFWorkbook) sheet.getWorkbook();
        XSSFCellStyle dictHeaderStyle = createHeaderStyle(workbook);
        XSSFCellStyle dictContentStyle = createDictContentStyle(workbook);
        XSSFCellStyle noticeStyle = createNoticeStyle(workbook);

        String[][] dictData = {
                {"字段名称", "可选值", "说明"},
                {"商品类型", "0", "普通商品"},
                {"", "2", "虚拟商品"},
                {"", "5", "云盘商品"},
                {"", "6", "卡密商品"},
                {"配送方式", "1", "商家配送"},
                {"", "2", "到店核销"},
                {"", "1,2", "支持多种配送方式"},
                {"规格类型", "single", "单规格商品"},
                {"", "multiple", "多规格商品（需在多规格示例表配置）"},
                {"布尔值字段", "true", "是/启用"},
                {"", "false", "否/禁用"},
                {"注意事项", "", ""},
                {"1. 分类名称", "", "请从下拉列表中选择实际存在的分类名称"},
                {"2. 品牌名称", "", "请从下拉列表中选择实际存在的品牌名称"},
                {"3. 运费模板", "", "请从下拉列表中选择实际存在的模板名称"},
                {"4. 图片URL", "", "请确保图片链接可以正常访问"},
                {"5. 多规格商品", "", "需要在'多规格示例'表中配置详细规格"}
        };

        for (int i = 0; i < dictData.length; i++) {
            Row row = sheet.createRow(i);
            if (i == 0) {
                row.setHeight((short) 600); // 表头行高
            } else {
                row.setHeight((short) 400); // 内容行高
            }

            for (int j = 0; j < dictData[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(dictData[i][j]);

                // 应用样式
                if (i == 0) {
                    cell.setCellStyle(dictHeaderStyle);
                } else if (i >= 12) { // 注意事项行
                    cell.setCellStyle(noticeStyle);
                } else {
                    cell.setCellStyle(dictContentStyle);
                }
            }
        }

        // 设置列宽
        sheet.setColumnWidth(0, 4500);
        sheet.setColumnWidth(1, 3500);
        sheet.setColumnWidth(2, 10000);
    }

    /**
     * 创建使用说明表
     */
    private void createUsageInstructionSheet(Sheet sheet, XSSFWorkbook workbook) {
        // 创建样式
        XSSFCellStyle titleStyle = createTitleStyle(workbook);
        XSSFCellStyle headerStyle = createHeaderStyle(workbook);
        XSSFCellStyle contentStyle = createDictContentStyle(workbook);
        XSSFCellStyle noticeStyle = createNoticeStyle(workbook);

        int rowIndex = 0;

        // 标题
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("商品导入模板使用说明");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        rowIndex++; // 空行

        // 1. 模板结构说明
        Row structureHeaderRow = sheet.createRow(rowIndex++);
        Cell structureHeaderCell = structureHeaderRow.createCell(0);
        structureHeaderCell.setCellValue("一、模板结构说明");
        structureHeaderCell.setCellStyle(headerStyle);

        String[] structureDesc = {
                "1. 商品基本信息：填写商品的基础信息，如名称、分类、价格、库存等",
                "2. 商品规格配置：配置多规格商品的详细规格信息，单规格商品也需填写一行默认规格",
                "3. 数据字典说明：各字段的可选值和说明",
                "4. 使用说明：当前页面，详细的使用指南"
        };

        for (String desc : structureDesc) {
            Row descRow = sheet.createRow(rowIndex++);
            Cell descCell = descRow.createCell(0);
            descCell.setCellValue(desc);
            descCell.setCellStyle(contentStyle);
        }

        rowIndex++; // 空行

        // 2. 填写步骤
        Row stepHeaderRow = sheet.createRow(rowIndex++);
        Cell stepHeaderCell = stepHeaderRow.createCell(0);
        stepHeaderCell.setCellValue("二、填写步骤");
        stepHeaderCell.setCellStyle(headerStyle);

        String[] steps = {
                "步骤1：在商品基本信息表中填写商品的基础信息",
                "步骤2：在商品规格配置表中配置商品规格",
                "  - 单规格商品：填写一行，规格组合填默认规格",
                "  - 多规格商品：每个规格组合填写一行",
                "步骤3：保存Excel文件并通过系统导入功能上传",
                "步骤4：系统会自动验证数据并生成商品"
        };

        for (String step : steps) {
            Row stepRow = sheet.createRow(rowIndex++);
            Cell stepCell = stepRow.createCell(0);
            stepCell.setCellValue(step);
            stepCell.setCellStyle(contentStyle);
        }

        rowIndex++; // 空行

        // 3. 重要提示
        Row noticeHeaderRow = sheet.createRow(rowIndex++);
        Cell noticeHeaderCell = noticeHeaderRow.createCell(0);
        noticeHeaderCell.setCellValue("三、重要提示");
        noticeHeaderCell.setCellStyle(headerStyle);

        String[] notices = {
                "⚠️ 商品规格配置表中的商品名称必须与基本信息表中的完全一致",
                "⚠️ 必填字段（标记*）不能为空",
                "⚠️ 下拉字段请从下拉列表中选择，不要手动输入",
                "⚠️ 图片可以直接复制粘贴到单元格中",
                "⚠️ 价格字段请使用数字格式，如：99.00",
                "⚠️ 布尔值字段请填写true或false"
        };

        for (String notice : notices) {
            Row noticeRow = sheet.createRow(rowIndex++);
            Cell noticeCell = noticeRow.createCell(0);
            noticeCell.setCellValue(notice);
            noticeCell.setCellStyle(noticeStyle);
        }

        rowIndex++; // 空行

        // 4. 多规格商品示例
        Row exampleHeaderRow = sheet.createRow(rowIndex++);
        Cell exampleHeaderCell = exampleHeaderRow.createCell(0);
        exampleHeaderCell.setCellValue("四、多规格商品示例");
        exampleHeaderCell.setCellStyle(headerStyle);

        // 示例表格表头
        Row exampleTableHeaderRow = sheet.createRow(rowIndex++);
        String[] exampleHeaders = {"商品名称", "规格组合", "售价", "库存", "说明"};
        for (int i = 0; i < exampleHeaders.length; i++) {
            Cell cell = exampleTableHeaderRow.createCell(i);
            cell.setCellValue(exampleHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        // 示例数据
        String[][] exampleData = {
                {"T恤衫", "红色", "89.00", "100", "规格组合格式：属性值1"},
                {"T恤衫", "白色", "89.00", "100", "同一商品的不同规格"},
                {"T恤衫", "蓝色", "89.00", "100", ""},
                {"连衣裙", "默认规格", "199.00", "50", "单规格商品示例"}
        };

        for (String[] rowData : exampleData) {
            Row exampleRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < rowData.length; i++) {
                Cell cell = exampleRow.createCell(i);
                cell.setCellValue(rowData[i]);
                cell.setCellStyle(contentStyle);
            }
        }

        rowIndex++; // 空行

        // 省市区街道级联选择说明
        Row cascadeHeaderRow = sheet.createRow(rowIndex++);
        Cell cascadeHeaderCell = cascadeHeaderRow.createCell(0);
        cascadeHeaderCell.setCellValue("四、省市区街道级联选择说明");
        cascadeHeaderCell.setCellStyle(headerStyle);

        String[] cascadeDesc = {
                "1. 省份：从下拉列表中选择省份",
                "2. 城市：根据选择的省份填写对应的城市名称",
                "3. 区县：根据选择的城市填写对应的区县名称",
                "4. 街道：根据选择的区县填写对应的街道名称",
                "注意：请确保填写的地址信息层级关系正确，系统会自动验证地址的有效性"
        };

        for (String desc : cascadeDesc) {
            Row descRow = sheet.createRow(rowIndex++);
            Cell descCell = descRow.createCell(0);
            descCell.setCellValue(desc);
            if (desc.startsWith("注意")) {
                descCell.setCellStyle(noticeStyle);
            } else {
                descCell.setCellStyle(contentStyle);
            }
        }

        rowIndex++; // 空行

        // 导入注意事项
        Row importHeaderRow = sheet.createRow(rowIndex++);
        Cell importHeaderCell = importHeaderRow.createCell(0);
        importHeaderCell.setCellValue("五、导入注意事项");
        importHeaderCell.setCellStyle(headerStyle);

        String[] importNotes = {
                "1. 确保Excel文件格式为.xlsx或.xls",
                "2. 批量导入时请先备份现有数据",
                "3. 如遇到错误，请检查数据格式和必填字段",
                "4. 单次导入建议不超过1000条商品数据",
                "5. 图片可直接粘贴到单元格中，或填写图片URL",
                "6. 规格组合格式：属性名:属性值，多个属性用逗号分隔"
        };

        for (String note : importNotes) {
            Row noteRow = sheet.createRow(rowIndex++);
            Cell noteCell = noteRow.createCell(0);
            noteCell.setCellValue(note);
            noteCell.setCellStyle(contentStyle);
        }

        // 设置列宽
        sheet.setColumnWidth(0, 4000);
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(2, 3000);
        sheet.setColumnWidth(3, 3000);
        sheet.setColumnWidth(4, 8000);
    }

    /**
     * 创建多规格示例表
     */
    private void createSpecSheet(Sheet sheet) {
        // 创建样式
        XSSFWorkbook workbook = (XSSFWorkbook) sheet.getWorkbook();
        XSSFCellStyle specHeaderStyle = createHeaderStyle(workbook);
        XSSFCellStyle specExampleStyle = createExampleStyle(workbook);
        XSSFCellStyle specNoticeStyle = createNoticeStyle(workbook);

        String[][] specData = {
                {"商品名称", "规格名称", "规格价格", "市场价", "成本价", "会员价", "库存", "商品编码", "商品条码", "重量", "体积", "规格图片"},
                {"示例T恤", "颜色:", "59.00", "79.00", "30.00", "55.00", "50", "TS-RED-S", "1234567890001", "0.2", "0.001", "https://example.com/red-s.jpg"},
                {"说明：", "", "", "", "", "", "", "", "", "", "", ""},
                {"1. 商品名称必须与主表中的商品名称完全一致", "", "", "", "", "", "", "", "", "", "", ""},
                {"2. 规格组合格式：规格名1:规格值1,规格名2:规格값2", "", "", "", "", "", "", "", "", "", "", ""},
                {"3. 每个规格组合对应一行数据", "", "", "", "", "", "", "", "", "", "", ""},
                {"4. 价格、库存等数值字段不能为空", "", "", "", "", "", "", "", "", "", "", ""}
        };

        for (int i = 0; i < specData.length; i++) {
            Row row = sheet.createRow(i);
            if (i == 0) {
                row.setHeight((short) 600); // 表头行高
            } else if (i <= 7) {
                row.setHeight((short) 450); // 示例数据行高
            } else {
                row.setHeight((short) 400); // 说明行高
            }

            for (int j = 0; j < specData[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(specData[i][j]);

                // 应用样式
                if (i == 0) {
                    cell.setCellStyle(specHeaderStyle);
                } else if (i >= 1 && i <= 6) { // 示例数据行
                    cell.setCellStyle(specExampleStyle);
                } else if (i >= 8) { // 说明行
                    cell.setCellStyle(specNoticeStyle);
                }
            }
        }

        // 设置列宽
        String[] columnHeaders = {"商品名称", "规格名称", "规格价格", "市场价", "成本价", "会员价", "库存", "商品编码", "商品条码", "重量", "体积", "规格图片"};
        int[] columnWidths = {4000, 5000, 3000, 3000, 3000, 3000, 3000, 4000, 4000, 3000, 3000, 6000};

        for (int i = 0; i < Math.min(columnWidths.length, 12); i++) {
            sheet.setColumnWidth(i, columnWidths[i]);
        }
    }

    /**
     * 创建隐藏的数据源表（用于复杂下拉验证和级联选择）
     */
    private void createHiddenDataSheets(XSSFWorkbook workbook, DropdownData dropdownData) {
//        logger.info("开始创建省市区街道级联数据源表");
//
//        // 创建省市区街道级联数据源表
//        if (dropdownData.getCityMap() != null && !dropdownData.getCityMap().isEmpty()) {
//            createCascadeDataSheet(workbook, dropdownData);
//        }
//
//        logger.info("省市区街道级联数据源表创建完成");
    }

    /**
     * 创建级联数据源表
     */
    private void createCascadeDataSheet(XSSFWorkbook workbook, DropdownData dropdownData) {
        // 创建级联数据源表
        XSSFSheet cascadeSheet = workbook.createSheet("级联数据源");

        // 设置表为隐藏
        workbook.setSheetHidden(workbook.getSheetIndex(cascadeSheet), true);

        // 创建表头
        XSSFRow headerRow = cascadeSheet.createRow(0);
        headerRow.createCell(0).setCellValue("省份");
        headerRow.createCell(1).setCellValue("城市");
        headerRow.createCell(2).setCellValue("区县");
        headerRow.createCell(3).setCellValue("街道");
        headerRow.createCell(4).setCellValue("省份_城市");
        headerRow.createCell(5).setCellValue("省份_城市_区县");

        int rowIndex = 1;

        // 填充级联数据
        for (Map.Entry<String, List<String>> provinceEntry : dropdownData.getCityMap().entrySet()) {
            String provinceName = provinceEntry.getKey();
            List<String> cities = provinceEntry.getValue();

            for (String cityName : cities) {
                String cityKey = provinceName + "_" + cityName;
                List<String> districts = dropdownData.getDistrictMap().get(cityKey);

                if (districts != null && !districts.isEmpty()) {
                    for (String districtName : districts) {
                        String districtKey = cityKey + "_" + districtName;
                        List<String> streets = dropdownData.getStreetMap().get(districtKey);

                        if (streets != null && !streets.isEmpty()) {
                            for (String streetName : streets) {
                                XSSFRow dataRow = cascadeSheet.createRow(rowIndex++);
                                dataRow.createCell(0).setCellValue(provinceName);
                                dataRow.createCell(1).setCellValue(cityName);
                                dataRow.createCell(2).setCellValue(districtName);
                                dataRow.createCell(3).setCellValue(streetName);
                                dataRow.createCell(4).setCellValue(cityKey);
                                dataRow.createCell(5).setCellValue(districtKey);
                            }
                        } else {
                            // 没有街道数据的区县
                            XSSFRow dataRow = cascadeSheet.createRow(rowIndex++);
                            dataRow.createCell(0).setCellValue(provinceName);
                            dataRow.createCell(1).setCellValue(cityName);
                            dataRow.createCell(2).setCellValue(districtName);
                            dataRow.createCell(3).setCellValue("");
                            dataRow.createCell(4).setCellValue(cityKey);
                            dataRow.createCell(5).setCellValue(districtKey);
                        }
                    }
                } else {
                    // 没有区县数据的城市
                    XSSFRow dataRow = cascadeSheet.createRow(rowIndex++);
                    dataRow.createCell(0).setCellValue(provinceName);
                    dataRow.createCell(1).setCellValue(cityName);
                    dataRow.createCell(2).setCellValue("");
                    dataRow.createCell(3).setCellValue("");
                    dataRow.createCell(4).setCellValue(cityKey);
                    dataRow.createCell(5).setCellValue("");
                }
            }
        }

        // 设置列宽（使用固定宽度代替autoSizeColumn以提高性能）
        for (int i = 0; i < 6; i++) {
            cascadeSheet.setColumnWidth(i, 4000);
        }

        logger.info("级联数据源表创建完成，共{}行数据", rowIndex - 1);
    }

    /**
     * 创建用于省市区街道联动下拉的独立数据表（优化版本）
     */
    private void createDropdownSheetsForCascade(XSSFWorkbook workbook, DropdownData dropdownData) {
        long startTime = System.currentTimeMillis();
        int sheetCount = 0;

        // 为每个省份创建单独的城市列表表（优化：批量创建行）
        for (Map.Entry<String, List<String>> provinceEntry : dropdownData.getCityMap().entrySet()) {
            String provinceName = provinceEntry.getKey();
            List<String> cities = provinceEntry.getValue();

            // 创建城市列表表
            XSSFSheet citySheet = workbook.createSheet("城市_" + provinceName);
            workbook.setSheetHidden(workbook.getSheetIndex(citySheet), true);

            // 批量创建行（优化性能）
            for (int i = 0; i < cities.size(); i++) {
                XSSFRow row = citySheet.createRow(i);
                row.createCell(0).setCellValue(cities.get(i));
            }

            // 设置固定列宽（避免使用autoSizeColumn）
            citySheet.setColumnWidth(0, 4000);
            sheetCount++;
        }

        // 为每个省份_城市组合创建区县列表表（优化：批量创建行）
        for (Map.Entry<String, List<String>> cityEntry : dropdownData.getDistrictMap().entrySet()) {
            String cityKey = cityEntry.getKey(); // 格式：省份_城市
            List<String> districts = cityEntry.getValue();

            // 创建区县列表表
            XSSFSheet districtSheet = workbook.createSheet("区县_" + cityKey);
            workbook.setSheetHidden(workbook.getSheetIndex(districtSheet), true);

            // 批量创建行（优化性能）
            for (int i = 0; i < districts.size(); i++) {
                XSSFRow row = districtSheet.createRow(i);
                row.createCell(0).setCellValue(districts.get(i));
            }

            // 设置固定列宽（避免使用autoSizeColumn）
            districtSheet.setColumnWidth(0, 4000);
            sheetCount++;
        }

        // 为每个省份_城市_区县组合创建街道列表表（优化：批量创建行）
        for (Map.Entry<String, List<String>> streetEntry : dropdownData.getStreetMap().entrySet()) {
            String districtKey = streetEntry.getKey(); // 格式：省份_城市_区县
            List<String> streets = streetEntry.getValue();

            // 如果街道列表不为空才创建表
            if (streets != null && !streets.isEmpty()) {
                // 创建街道列表表
                XSSFSheet streetSheet = workbook.createSheet("街道_" + districtKey);
                workbook.setSheetHidden(workbook.getSheetIndex(streetSheet), true);

                // 批量创建行（优化性能）
                for (int i = 0; i < streets.size(); i++) {
                    XSSFRow row = streetSheet.createRow(i);
                    row.createCell(0).setCellValue(streets.get(i));
                }

                // 设置固定列宽（避免使用autoSizeColumn）
                streetSheet.setColumnWidth(0, 4000);
                sheetCount++;
            }
        }

        long endTime = System.currentTimeMillis();
        logger.info("创建联动下拉数据表完成，共创建{}个工作表，耗时{}毫秒", sheetCount, (endTime - startTime));
    }

    /**
     * 生成商品导入模板文件并上传到服务器
     * @return 文件访问URL
     */
    @Override
    public String generateProductImportTemplate() {
        // 服务器存储地址
        String rootPath = crmebConfig.getImagePath().trim();
        // 模块
        String modelPath = "public/template/";
        // 类型
        String type = UploadConstants.UPLOAD_AFTER_FILE_KEYWORD + "/";

        // 变更文件名
        String newFileName = "商品导入模板_" + CrmebDateUtil.nowDateTime(DateConstants.DATE_TIME_FORMAT_NUM) + ".xlsx";
        // 创建目标文件的名称，规则：类型/模块/年/月/日/文件名
        String webPath = type + modelPath + CrmebDateUtil.nowDate("yyyy/MM/dd") + "/";
        // 文件分隔符转化为当前系统的格式
        String destPath = FilenameUtils.separatorsToSystem(rootPath + webPath) + newFileName;

        try {
            // 创建本地文件
            File file = UploadUtil.createFile(destPath);

            // 使用EasyExcel创建模板
            XSSFWorkbook workbook = new XSSFWorkbook();

            // 获取下拉数据
            DropdownData dropdownData = getDropdownData();

            // 创建主表单
            Sheet mainSheet = workbook.createSheet("商品基本信息");
            createMainSheetWithValidation(mainSheet, workbook, dropdownData);

            // 创建规格配置表
            Sheet specSheet = workbook.createSheet("商品规格配置");
            createSpecConfigSheet(specSheet, workbook, dropdownData);

            // 创建字典表
            Sheet dictSheet = workbook.createSheet("数据字典");
            createDictSheet(dictSheet, dropdownData);

            // 创建使用说明表
            Sheet instructionSheet = workbook.createSheet("使用说明");
            createUsageInstructionSheet(instructionSheet, workbook);

            // 创建规格示例表
            Sheet specExampleSheet = workbook.createSheet("规格示例");
            createSpecSheet(specExampleSheet);

            // 创建隐藏数据表
            createHiddenDataSheets(workbook, dropdownData);

            // 写入文件
            FileOutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            // 创建附件记录
            SystemAttachment systemAttachment = new SystemAttachment();
            systemAttachment.setName(newFileName);
            systemAttachment.setSattDir(webPath + newFileName);
            systemAttachment.setAttSize(String.valueOf(file.length()));
            systemAttachment.setAttType("xlsx");
            systemAttachment.setImageType(1); // 默认本地
            systemAttachment.setPid(0);
            systemAttachment.setOwner(-1); // 平台文件

            // 获取上传类型配置
            String uploadType = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_UPLOAD_TYPE);
            Integer uploadTypeInt = Integer.parseInt(uploadType);

            if (uploadTypeInt.equals(1)) {
                // 本地存储
                systemAttachmentService.save(systemAttachment);
                return systemAttachmentService.prefixFile(systemAttachment.getSattDir());
            }

            // 云存储处理
            CloudVo cloudVo = new CloudVo();
            String fileIsSave = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_FILE_IS_SAVE);

            switch (uploadTypeInt) {
                case 2: // 七牛云
                    systemAttachment.setImageType(2);
                    cloudVo.setDomain(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_UPLOAD_URL));
                    cloudVo.setAccessKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_ACCESS_KEY));
                    cloudVo.setSecretKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_SECRET_KEY));
                    cloudVo.setBucketName(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_STORAGE_NAME));
                    cloudVo.setRegion(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_STORAGE_REGION));
                    try {
                        Configuration cfg = new Configuration(Region.autoRegion());
                        UploadManager uploadManager = new UploadManager(cfg);
                        Auth auth = Auth.create(cloudVo.getAccessKey(), cloudVo.getSecretKey());
                        String upToken = auth.uploadToken(cloudVo.getBucketName());
                        qiNiuService.uploadFile(uploadManager, upToken, systemAttachment.getSattDir(), destPath, file);
                    } catch (Exception e) {
                        logger.error("七牛云上传失败：" + e.getMessage());
                    }
                    break;
                case 3: // 阿里云OSS
                    systemAttachment.setImageType(3);
                    cloudVo.setDomain(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_UPLOAD_URL));
                    cloudVo.setAccessKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_ACCESS_KEY));
                    cloudVo.setSecretKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_SECRET_KEY));
                    cloudVo.setBucketName(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_STORAGE_NAME));
                    cloudVo.setRegion(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_STORAGE_REGION));
                    try {
                        ossService.upload(cloudVo, systemAttachment.getSattDir(), destPath, file);
                    } catch (Exception e) {
                        logger.error("阿里云OSS上传失败：" + e.getMessage());
                    }
                    break;
                case 5: // 京东云
                    systemAttachment.setImageType(5);
                    String bucket = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_JD_BUCKET_NAME);
                    try {
                        jdCloudService.uploadFile(systemAttachment.getSattDir(), destPath, bucket);
                    } catch (Exception e) {
                        logger.error("京东云上传失败：" + e.getMessage());
                    }
                    break;
            }

            // 保存附件记录
            systemAttachmentService.save(systemAttachment);

            // 如果不保存本地文件，删除本地文件
            if (!fileIsSave.equals("1") && file != null) {
                file.delete();
            }

            // 返回文件访问URL
            return systemAttachmentService.prefixFile(systemAttachment.getSattDir());

        } catch (Exception e) {
            logger.error("生成商品导入模板失败：{}", e.getMessage(), e);
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "生成模板失败：" + e.getMessage());
        }
    }

    /**
     * 导出商品导入错误数据
     */
    @Override
    public String exportProductImportErrors(List<ProductImportResultVo.ProductImportErrorVo> errorList) {
        if (CollUtil.isEmpty(errorList)) {
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "没有错误数据可导出");
        }

        // 服务器存储地址
        String rootPath = crmebConfig.getImagePath().trim();
        // 模块
        String modelPath = "public/export/";
        // 类型
        String type = UploadConstants.UPLOAD_AFTER_FILE_KEYWORD + "/";

        // 变更文件名
        String newFileName = "商品导入错误数据_" + CrmebDateUtil.nowDateTime(DateConstants.DATE_TIME_FORMAT_NUM) + ".xlsx";
        // 创建目标文件的名称，规则：类型/模块/年/月/日/文件名
        String webPath = type + modelPath + CrmebDateUtil.nowDate("yyyy/MM/dd") + "/";
        // 文件分隔符转化为当前系统的格式
        String destPath = FilenameUtils.separatorsToSystem(rootPath + webPath) + newFileName;

        // 构建导出数据
        List<ProductImportErrorExportVo> voList = CollUtil.newArrayList();
        String currentTime = CrmebDateUtil.nowDateTime(DateConstants.DATE_TIME_FORMAT_NUM);

        for (ProductImportResultVo.ProductImportErrorVo error : errorList) {
            ProductImportErrorExportVo vo = new ProductImportErrorExportVo();
            vo.setRowIndex(error.getRowIndex());
            vo.setProductName(error.getProductName());
            vo.setErrorMessage(error.getErrorMessage());
            vo.setImportTime(currentTime);

            // 根据错误信息分类错误类型并提供建议
            String errorMessage = error.getErrorMessage();
            if (errorMessage.contains("名称不能为空")) {
                vo.setErrorType("必填字段缺失");
                vo.setSuggestion("请填写商品名称");
            } else if (errorMessage.contains("分类") && errorMessage.contains("不存在")) {
                vo.setErrorType("分类错误");
                vo.setSuggestion("请检查分类名称是否正确，或先创建对应分类");
            } else if (errorMessage.contains("品牌") && errorMessage.contains("不存在")) {
                vo.setErrorType("品牌错误");
                vo.setSuggestion("请检查品牌名称是否正确，或先创建对应品牌");
            } else if (errorMessage.contains("规格组合")) {
                vo.setErrorType("规格格式错误");
                vo.setSuggestion("请使用正确的规格组合格式：属性名1:属性值1,属性名2:属性值2");
            } else if (errorMessage.contains("运费模板")) {
                vo.setErrorType("运费模板错误");
                vo.setSuggestion("请检查运费模板名称是否正确，或先创建对应运费模板");
            } else if (errorMessage.contains("价格") || errorMessage.contains("库存")) {
                vo.setErrorType("数值格式错误");
                vo.setSuggestion("请检查价格和库存是否为有效数值");
            } else {
                vo.setErrorType("其他错误");
                vo.setSuggestion("请根据错误信息进行相应调整");
            }

            voList.add(vo);
        }

        File file = null;
        try {
            // 创建本地文件
            file = UploadUtil.createFile(destPath);

            // 使用EasyExcel写入数据
            EasyExcel.write(file, ProductImportErrorExportVo.class)
                    .sheet("导入错误数据")
                    .doWrite(voList);

            // 创建附件记录
            SystemAttachment systemAttachment = new SystemAttachment();
            systemAttachment.setName(newFileName);
            systemAttachment.setSattDir(webPath + newFileName);
            systemAttachment.setAttSize(String.valueOf(file.length()));
            systemAttachment.setAttType("xlsx");
            systemAttachment.setImageType(1); // 默认本地
            systemAttachment.setPid(0);
            systemAttachment.setOwner(-1); // 平台文件

            // 获取上传类型配置
            String uploadType = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_UPLOAD_TYPE);
            Integer uploadTypeInt = Integer.parseInt(uploadType);

            if (uploadTypeInt.equals(1)) {
                // 本地存储
                systemAttachmentService.save(systemAttachment);
                return systemAttachmentService.prefixFile(systemAttachment.getSattDir());
            }

            // 云存储处理
            CloudVo cloudVo = new CloudVo();
            String fileIsSave = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_FILE_IS_SAVE);

            switch (uploadTypeInt) {
                case 2: // 七牛云
                    systemAttachment.setImageType(2);
                    cloudVo.setDomain(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_UPLOAD_URL));
                    cloudVo.setAccessKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_ACCESS_KEY));
                    cloudVo.setSecretKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_SECRET_KEY));
                    cloudVo.setBucketName(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_STORAGE_NAME));
                    cloudVo.setRegion(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_QN_STORAGE_REGION));
                    try {
                        Configuration cfg = new Configuration(Region.autoRegion());
                        UploadManager uploadManager = new UploadManager(cfg);
                        Auth auth = Auth.create(cloudVo.getAccessKey(), cloudVo.getSecretKey());
                        String upToken = auth.uploadToken(cloudVo.getBucketName());
                        qiNiuService.uploadFile(uploadManager, upToken, systemAttachment.getSattDir(), destPath, file);
                    } catch (Exception e) {
                        logger.error("七牛云上传失败：" + e.getMessage());
                    }
                    break;
                case 3: // 阿里云OSS
                    systemAttachment.setImageType(3);
                    cloudVo.setDomain(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_UPLOAD_URL));
                    cloudVo.setAccessKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_ACCESS_KEY));
                    cloudVo.setSecretKey(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_SECRET_KEY));
                    cloudVo.setBucketName(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_STORAGE_NAME));
                    cloudVo.setRegion(systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_AL_STORAGE_REGION));
                    try {
                        ossService.upload(cloudVo, systemAttachment.getSattDir(), destPath, file);
                    } catch (Exception e) {
                        logger.error("阿里云OSS上传失败：" + e.getMessage());
                    }
                    break;
                case 5: // 京东云
                    systemAttachment.setImageType(5);
                    String bucket = systemConfigService.getValueByKeyException(SysConfigConstants.CONFIG_JD_BUCKET_NAME);
                    try {
                        jdCloudService.uploadFile(systemAttachment.getSattDir(), destPath, bucket);
                    } catch (Exception e) {
                        logger.error("京东云上传失败：" + e.getMessage());
                    }
                    break;
            }

            // 保存附件记录
            systemAttachmentService.save(systemAttachment);

            // 如果不保存本地文件，删除本地文件
            if (!fileIsSave.equals("1") && file != null) {
                file.delete();
            }

            // 返回文件访问URL
            return systemAttachmentService.prefixFile(systemAttachment.getSattDir());

        } catch (Exception e) {
            logger.error("商品导出异常：", e);
            throw new CrmebException(CommonResultCode.VALIDATE_FAILED, "导出失败：" + e.getMessage());
        }
    }
}

