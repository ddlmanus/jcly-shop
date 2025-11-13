package com.zbkj.service.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;
import com.zbkj.common.constants.RedisConstants;
import com.zbkj.common.constants.SysConfigConstants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.utils.RedisUtil;
import com.zbkj.common.utils.RestTemplateUtil;
import com.zbkj.common.response.CityDeliveryOrderResponse;
import com.zbkj.common.vo.LogisticsResultListVo;
import com.zbkj.common.vo.LogisticsResultVo;
import com.zbkj.common.vo.OnePassLogisticsQueryVo;
import com.zbkj.service.service.CityDeliveryService;
import com.zbkj.service.service.LogisticService;
import com.zbkj.service.service.OnePassService;
import com.zbkj.service.service.SystemConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * ExpressServiceImpl 接口实现
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
public class LogisticsServiceImpl implements LogisticService {

    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private RestTemplateUtil restTemplateUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private OnePassService onePassService;
    @Autowired
    private CityDeliveryService cityDeliveryService;

    /**
     * 快递
     *
     * @param expressNo String 物流单号
     * @param type      String 快递公司字母简写：不知道可不填 95%能自动识别，填写查询速度会更快 https://market.aliyun.com/products/56928004/cmapi021863.html#sku=yuncode15863000015
     * @param com       快递公司编号
     * @param phone     手机号
     * @return LogisticsResultVo
     */
    @Override
    public LogisticsResultVo info(String expressNo, String type, String com, String phone) {
        LogisticsResultVo resultVo = new LogisticsResultVo();
        String redisKey = RedisConstants.LOGISTICS_KEY + expressNo;
        JSONObject result = getCache(redisKey);
        if (ObjectUtil.isNotNull(result)) {
            return JSONObject.toJavaObject(result, LogisticsResultVo.class);
        }
        
        // 检查是否为同城配送订单
        if ("CITY_DELIVERY".equals(com) || (StrUtil.isNotBlank(expressNo) && expressNo.startsWith("CD"))) {
            return queryCityDeliveryInfo(expressNo);
        }
        String logisticsType = systemConfigService.getValueByKeyException(SysConfigConstants.LOGISTICS_QUERY_TYPE);
        if (logisticsType.equals("1")) {// 平台查询
            OnePassLogisticsQueryVo queryVo = onePassService.exprQuery(expressNo, com, phone);
            if (ObjectUtil.isNull(queryVo)) {
                resultVo.setNumber(expressNo);
                resultVo.setExpName(com);
                return resultVo;
            }
            // 一号通vo转公共返回vo
            resultVo = queryToResultVo(queryVo);
            String jsonString = JSONObject.toJSONString(resultVo);
            saveCache(redisKey, JSONObject.parseObject(jsonString));
        }
        if (logisticsType.equals("2")) {// 阿里云查询
            // 顺丰请输入单号 : 收件人或寄件人手机号后四位。例如：123456789:1234
            if (StrUtil.isNotBlank(com) && com.equals("shunfeng")) {
                expressNo = expressNo.concat(":").concat(StrUtil.sub(phone, 7, -1));
            }
            String appCode = systemConfigService.getValueByKey(SysConfigConstants.LOGISTICS_QUERY_ALIYUN_CODE);
            String url = StrUtil.format(SysConfigConstants.LOGISTICS_QUERY_ALIYUN_URL, expressNo);
            if (StringUtils.isNotBlank(type)) {
                url = url.concat(StrUtil.format("&type={}", type));
            }
            HashMap<String, String> header = new HashMap<>();
            header.put("Authorization", "APPCODE " + appCode);

            JSONObject data = restTemplateUtil.getData(url, header);
            checkResult(data);
            //把数据解析成对象返回到前端
            result = data.getJSONObject("result");
            saveCache(redisKey, result);
            resultVo = JSONObject.toJavaObject(result, LogisticsResultVo.class);
        }
        if (logisticsType.equals("3")) {// 快递100查询
            resultVo = queryByKuaiDi100(expressNo, com, phone);
            if (ObjectUtil.isNotNull(resultVo)) {
                String jsonString = JSONObject.toJSONString(resultVo);
                saveCache(redisKey, JSONObject.parseObject(jsonString));
            }
        }
        return resultVo;
    }

    /**
     * 快递100查询
     *
     * @param expressNo 快递单号
     * @param com       快递公司编号
     * @param phone     手机号（顺丰需要）
     * @return LogisticsResultVo
     */
    private LogisticsResultVo queryByKuaiDi100(String expressNo, String com, String phone) {
        LogisticsResultVo resultVo = new LogisticsResultVo();
        
        try {
            // 检查是否为同城配送订单
            if ("CITY_DELIVERY".equals(com) || (StrUtil.isNotBlank(expressNo) && expressNo.startsWith("CD"))) {
                return queryKuaiDi100CityDelivery(expressNo);
            }
            
            // 获取快递100配置
            String key = systemConfigService.getValueByKey(SysConfigConstants.LOGISTICS_QUERY_KD100_KEY);
            String customer = systemConfigService.getValueByKey(SysConfigConstants.LOGISTICS_QUERY_KD100_CUSTORER);
            
            if (StrUtil.isBlank(key) || StrUtil.isBlank(customer)) {
                throw new CrmebException("快递100配置信息不完整，请检查key和customer配置");
            }
            
            // 构建查询参数
            JSONObject paramObj = new JSONObject();
            paramObj.put("com", com);
            paramObj.put("num", expressNo);
            paramObj.put("resultv2", "1"); // 开启行政区域解析
            
            // 顺丰需要手机号后四位
            if (StrUtil.isNotBlank(com) && "shunfeng".equals(com) && StrUtil.isNotBlank(phone)) {
                if (phone.length() >= 4) {
                    paramObj.put("phone", phone.substring(phone.length() - 4));
                }
            }
            
            String param = paramObj.toJSONString();
            
            // 生成签名：param + key + customer 的MD5加密（转大写）
            String signStr = param + key + customer;
            String sign = SecureUtil.md5(signStr).toUpperCase();
            
            // 构建POST请求参数
            MultiValueMap<String, Object> postParams = new LinkedMultiValueMap<>();
            postParams.add("customer", customer);
            postParams.add("sign", sign);
            postParams.add("param", param);
            
            // 发送POST请求
            String url = SysConfigConstants.LOGISTICS_QUERY_KD100_URL;
            String responseStr = restTemplateUtil.postFromUrlencoded(url, postParams, null);
            JSONObject response = JSONObject.parseObject(responseStr);
            
            // 检查返回结果
            if (ObjectUtil.isNull(response)) {
                throw new CrmebException("快递100查询返回数据为空");
            }
            
            if (!"200".equals(response.getString("status"))) {
                String message = response.getString("message");
                throw new CrmebException("快递100查询失败：" + (StrUtil.isNotBlank(message) ? message : "未知错误"));
            }
            
            // 解析返回数据
            resultVo = parseKuaiDi100Response(response, expressNo, com);
            
        } catch (Exception e) {
            // 查询失败时返回基本信息
            resultVo.setNumber(expressNo);
            resultVo.setExpName(com);
            // 可以记录日志或抛出异常，这里选择记录日志并返回空结果
            System.err.println("快递100查询失败：" + e.getMessage());
        }
        
        return resultVo;
    }

    /**
     * 快递100同城配送查询
     *
     * @param deliveryOrderNo 配送单号
     * @return LogisticsResultVo
     */
    private LogisticsResultVo queryKuaiDi100CityDelivery(String deliveryOrderNo) {
        LogisticsResultVo resultVo = new LogisticsResultVo();
        
        try {
            // 获取快递100配置
            String key = systemConfigService.getValueByKey(SysConfigConstants.LOGISTICS_QUERY_KD100_KEY);
            String customer = systemConfigService.getValueByKey(SysConfigConstants.LOGISTICS_QUERY_KD100_CUSTORER);
            
            if (StrUtil.isBlank(key) || StrUtil.isBlank(customer)) {
                // 如果没有配置快递100，回退到本地查询
                return queryCityDeliveryInfo(deliveryOrderNo);
            }
            
            // 首先查询本地配送订单信息
            CityDeliveryOrderResponse deliveryOrder = null;
            
            // 如果是配送单号，直接查询
            if (deliveryOrderNo.startsWith("CD")) {
                deliveryOrder = cityDeliveryService.getDeliveryOrderByDeliveryNo(deliveryOrderNo);
            } else {
                // 否则按订单号查询
                deliveryOrder = cityDeliveryService.getDeliveryOrderByOrderNo(deliveryOrderNo);
            }
            
            if (ObjectUtil.isNull(deliveryOrder)) {
                resultVo.setNumber(deliveryOrderNo);
                resultVo.setExpName("同城配送");
                return resultVo;
            }
            
            // 构建快递100查询参数（使用同城配送的特殊编码）
            JSONObject paramObj = new JSONObject();
            paramObj.put("com", "city_delivery"); // 同城配送的快递100编码
            paramObj.put("num", deliveryOrder.getDeliveryOrderNo());
            paramObj.put("resultv2", "1");
            
            String param = paramObj.toJSONString();
            
            // 生成签名
            String signStr = param + key + customer;
            String sign = SecureUtil.md5(signStr).toUpperCase();
            
            // 构建POST请求参数
            MultiValueMap<String, Object> postParams = new LinkedMultiValueMap<>();
            postParams.add("customer", customer);
            postParams.add("sign", sign);
            postParams.add("param", param);
            
            // 发送POST请求
            String url = SysConfigConstants.LOGISTICS_QUERY_KD100_URL;
            String responseStr = restTemplateUtil.postFromUrlencoded(url, postParams, null);
            JSONObject response = JSONObject.parseObject(responseStr);
            
            // 检查返回结果
            if (ObjectUtil.isNotNull(response) && "200".equals(response.getString("status"))) {
                // 快递100有数据，使用快递100的轨迹
                resultVo = parseKuaiDi100CityDeliveryResponse(response, deliveryOrder);
            } else {
                // 快递100没有数据，使用本地轨迹
                resultVo = parseLocalCityDeliveryResponse(deliveryOrder);
            }
            
        } catch (Exception e) {
            // 查询失败时回退到本地查询
            System.err.println("快递100同城配送查询失败，回退到本地查询：" + e.getMessage());
            resultVo = queryCityDeliveryInfo(deliveryOrderNo);
        }
        
        return resultVo;
    }

    /**
     * 解析快递100返回数据
     *
     * @param response  快递100返回的JSON数据
     * @param expressNo 快递单号
     * @param com       快递公司编号
     * @return LogisticsResultVo
     */
    private LogisticsResultVo parseKuaiDi100Response(JSONObject response, String expressNo, String com) {
        LogisticsResultVo resultVo = new LogisticsResultVo();
        
        // 基本信息
        resultVo.setNumber(expressNo);
        resultVo.setExpName(response.getString("com"));
        
        // 签收状态：0-在途中、1-已发货、2-疑难、3-已签收、4-退签、5-同城派送中、6-退回、7-转单等
        String state = response.getString("state");
        if (StrUtil.isNotBlank(state)) {
            resultVo.setDeliveryStatus(state);
            // 判断是否已签收
            resultVo.setIsSign("3".equals(state) ? "1" : "0");
        }
        
        // 物流轨迹信息
        if (response.containsKey("data") && ObjectUtil.isNotNull(response.get("data"))) {
            com.alibaba.fastjson.JSONArray dataArray = response.getJSONArray("data");
            List<LogisticsResultListVo> list = CollUtil.newArrayList();
            
            if (ObjectUtil.isNotNull(dataArray)) {
                for (int i = 0; i < dataArray.size(); i++) {
                    JSONObject item = dataArray.getJSONObject(i);
                    LogisticsResultListVo listVo = new LogisticsResultListVo();
                    listVo.setTime(item.getString("time"));
                    listVo.setStatus(item.getString("context"));
                    list.add(listVo);
                }
            }
            resultVo.setList(list);
        }
        
        return resultVo;
    }

    /**
     * 解析快递100同城配送返回数据
     *
     * @param response      快递100返回的JSON数据
     * @param deliveryOrder 本地配送订单信息
     * @return LogisticsResultVo
     */
    private LogisticsResultVo parseKuaiDi100CityDeliveryResponse(JSONObject response, CityDeliveryOrderResponse deliveryOrder) {
        LogisticsResultVo resultVo = new LogisticsResultVo();
        
        // 基本信息
        resultVo.setNumber(deliveryOrder.getDeliveryOrderNo());
        resultVo.setExpName("同城配送 - " + deliveryOrder.getDriverName());
        
        // 签收状态：快递100的状态码：5-同城派送中
        String state = response.getString("state");
        if (StrUtil.isNotBlank(state)) {
            resultVo.setDeliveryStatus(state);
            // 判断是否已签收（3-已签收）
            resultVo.setIsSign("3".equals(state) ? "1" : "0");
        } else {
            // 使用本地状态
            resultVo.setDeliveryStatus(deliveryOrder.getDeliveryStatus().toString());
            resultVo.setIsSign(deliveryOrder.getDeliveryStatus().equals(4) ? "1" : "0"); // 4-已送达
        }
        
        // 物流轨迹信息
        if (response.containsKey("data") && ObjectUtil.isNotNull(response.get("data"))) {
            com.alibaba.fastjson.JSONArray dataArray = response.getJSONArray("data");
            List<LogisticsResultListVo> list = CollUtil.newArrayList();
            
            if (ObjectUtil.isNotNull(dataArray)) {
                for (int i = 0; i < dataArray.size(); i++) {
                    JSONObject item = dataArray.getJSONObject(i);
                    LogisticsResultListVo listVo = new LogisticsResultListVo();
                    listVo.setTime(item.getString("time"));
                    listVo.setStatus(item.getString("context"));
                    list.add(listVo);
                }
            }
            resultVo.setList(list);
        } else {
            // 使用本地轨迹数据
            List<LogisticsResultListVo> localList = parseLocalTrackData(deliveryOrder);
            resultVo.setList(localList);
        }
        
        return resultVo;
    }

    /**
     * 解析本地同城配送返回数据
     *
     * @param deliveryOrder 本地配送订单信息
     * @return LogisticsResultVo
     */
    private LogisticsResultVo parseLocalCityDeliveryResponse(CityDeliveryOrderResponse deliveryOrder) {
        LogisticsResultVo resultVo = new LogisticsResultVo();
        
        // 填充基本信息
        resultVo.setNumber(deliveryOrder.getDeliveryOrderNo());
        resultVo.setExpName("同城配送 - " + deliveryOrder.getDriverName());
        resultVo.setDeliveryStatus(deliveryOrder.getDeliveryStatus().toString());
        resultVo.setIsSign(deliveryOrder.getDeliveryStatus().equals(4) ? "1" : "0"); // 4-已送达
        
        // 转换配送轨迹
        List<LogisticsResultListVo> list = parseLocalTrackData(deliveryOrder);
        resultVo.setList(list);
        
        return resultVo;
    }

    /**
     * 解析本地轨迹数据
     *
     * @param deliveryOrder 配送订单信息
     * @return 轨迹列表
     */
    private List<LogisticsResultListVo> parseLocalTrackData(CityDeliveryOrderResponse deliveryOrder) {
        List<LogisticsResultListVo> list = CollUtil.newArrayList();
        
        if (CollUtil.isNotEmpty(deliveryOrder.getDeliveryTrackList())) {
            for (CityDeliveryOrderResponse.DeliveryTrackData trackData : deliveryOrder.getDeliveryTrackList()) {
                LogisticsResultListVo listVo = new LogisticsResultListVo();
                listVo.setTime(trackData.getTime());
                listVo.setStatus(trackData.getStatus());
                list.add(listVo);
            }
            // 按时间倒序排列（最新的在前面）
            list.sort((a, b) -> b.getTime().compareTo(a.getTime()));
        }
        
        return list;
    }

    /**
     * 查询同城配送物流信息
     *
     * @param deliveryOrderNo 配送单号或快递单号
     * @return LogisticsResultVo
     */
    private LogisticsResultVo queryCityDeliveryInfo(String deliveryOrderNo) {
        LogisticsResultVo resultVo = new LogisticsResultVo();
        
        try {
            // 根据配送单号查询
            CityDeliveryOrderResponse deliveryOrder = null;
            
            // 如果是配送单号，直接查询
            if (deliveryOrderNo.startsWith("CD")) {
                deliveryOrder = cityDeliveryService.getDeliveryOrderByDeliveryNo(deliveryOrderNo);
            } else {
                // 否则按订单号查询
                deliveryOrder = cityDeliveryService.getDeliveryOrderByOrderNo(deliveryOrderNo);
            }
            
            if (ObjectUtil.isNull(deliveryOrder)) {
                resultVo.setNumber(deliveryOrderNo);
                resultVo.setExpName("同城配送");
                return resultVo;
            }
            
            // 填充基本信息
            resultVo.setNumber(deliveryOrder.getDeliveryOrderNo());
            resultVo.setExpName("同城配送 - " + deliveryOrder.getDriverName());
            resultVo.setDeliveryStatus(deliveryOrder.getDeliveryStatus().toString());
            resultVo.setIsSign(deliveryOrder.getDeliveryStatus().equals(4) ? "1" : "0"); // 4-已送达
            
            // 转换配送轨迹
            if (CollUtil.isNotEmpty(deliveryOrder.getDeliveryTrackList())) {
                List<LogisticsResultListVo> list = CollUtil.newArrayList();
                for (CityDeliveryOrderResponse.DeliveryTrackData trackData : deliveryOrder.getDeliveryTrackList()) {
                    LogisticsResultListVo listVo = new LogisticsResultListVo();
                    listVo.setTime(trackData.getTime());
                    listVo.setStatus(trackData.getStatus());
                    list.add(listVo);
                }
                // 按时间倒序排列（最新的在前面）
                list.sort((a, b) -> b.getTime().compareTo(a.getTime()));
                resultVo.setList(list);
            }
            
        } catch (Exception e) {
            // 查询失败时返回基本信息
            resultVo.setNumber(deliveryOrderNo);
            resultVo.setExpName("同城配送");
            System.err.println("同城配送物流查询失败：" + e.getMessage());
        }
        
        return resultVo;
    }

    /**
     * 一号通vo转公共返回vo
     */
    private LogisticsResultVo queryToResultVo(OnePassLogisticsQueryVo queryVo) {
        LogisticsResultVo resultVo = new LogisticsResultVo();
        resultVo.setNumber(queryVo.getNum());
        resultVo.setExpName(queryVo.getCom());
        resultVo.setIsSign(queryVo.getIscheck());
        resultVo.setDeliveryStatus(queryVo.getStatus());

        if (CollUtil.isNotEmpty(queryVo.getContent())) {
            List<LogisticsResultListVo> list = CollUtil.newArrayList();
            queryVo.getContent().forEach(i -> {
                LogisticsResultListVo listVo = new LogisticsResultListVo();
                listVo.setTime(i.getTime());
                listVo.setStatus(i.getStatus());
                list.add(listVo);
            });
            resultVo.setList(list);
        }
        return resultVo;
    }

    /**
     * 获取快递缓存
     *
     * @return JSONObject
     */
    private JSONObject getCache(String redisKey) {
        Object data = redisUtil.get(redisKey);
        if (ObjectUtil.isNotNull(data)) {
            return JSONObject.parseObject(data.toString());
        }
        return null;
    }

    /**
     * 获取快递缓存
     *
     * @param data JSONObject 需要保存的数据
     */
    private void saveCache(String redisKey, JSONObject data) {
        redisUtil.set(redisKey, data.toJSONString(), 1800L, TimeUnit.SECONDS);
    }

    /**
     * 获取快递缓存
     *
     * @param data JSONObject 检测返回的结果
     */
    private void checkResult(JSONObject data) {
        if (!data.getString("status").equals("0")) {
            throw new CrmebException(data.getString("msg"));
        }
    }
}

