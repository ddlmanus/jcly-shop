package com.zbkj.service.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.utils.RestTemplateUtil;
import com.zbkj.service.service.AmapService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 高德地图服务实现类
 * 
 * @author 系统
 * @date 2025-01-14
 */
@Slf4j
@Service
public class AmapServiceImpl implements AmapService {

    @Autowired
    private RestTemplateUtil restTemplateUtil;

    @Value("${amap.key:}")
    private String amapKey;

    // 高德地图API基础URL
    private static final String AMAP_BASE_URL = "https://restapi.amap.com/v3";
    
    // 地理编码API
    private static final String GEOCODING_URL = AMAP_BASE_URL + "/geocode/geo";
    
    // 逆地理编码API
    private static final String REVERSE_GEOCODING_URL = AMAP_BASE_URL + "/geocode/regeo";
    
    // 距离计算API
    private static final String DISTANCE_URL = AMAP_BASE_URL + "/distance";
    
    // 地址建议API
    private static final String SUGGESTION_URL = AMAP_BASE_URL + "/assistant/inputtips";
    
    // IP定位API
    private static final String IP_LOCATION_URL = AMAP_BASE_URL + "/ip";

    /**
     * 检查API密钥
     */
    private void checkApiKey() {
        if (StringUtils.isBlank(amapKey)) {
            throw new CrmebException("高德地图API密钥未配置");
        }
    }

    @Override
    public Map<String, Object> geocoding(String address) {
        checkApiKey();
        
        try {
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.add("key", amapKey);
            params.add("address", address);
            params.add("output", "JSON");
            
            JSONObject response = restTemplateUtil.getDataForm(GEOCODING_URL, params, null);
            log.info("高德地图地理编码响应: {}", response);
            
            if (response == null || !"1".equals(response.getString("status"))) {
                String info = response != null ? response.getString("info") : "请求失败";
                throw new CrmebException("地理编码失败: " + info);
            }
            
            JSONArray geocodes = response.getJSONArray("geocodes");
            if (geocodes == null || geocodes.isEmpty()) {
                throw new CrmebException("未找到该地址的坐标信息");
            }
            
            JSONObject geocode = geocodes.getJSONObject(0);
            String location = geocode.getString("location");
            String[] coordinates = location.split(",");
            
            Map<String, Object> result = new HashMap<>();
            result.put("lng", coordinates[0]);
            result.put("lat", coordinates[1]);
            result.put("formatted_address", geocode.getString("formatted_address"));
            result.put("level", geocode.getString("level"));
            result.put("province", geocode.getString("province"));
            result.put("city", geocode.getString("city"));
            result.put("district", geocode.getString("district"));
            
            return result;
        } catch (Exception e) {
            log.error("高德地图地理编码失败", e);
            throw new CrmebException("地理编码失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> reverseGeocoding(Double lat, Double lng) {
        checkApiKey();
        
        try {
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.add("key", amapKey);
            params.add("location", lng + "," + lat);
            params.add("output", "JSON");
            params.add("extensions", "all");
            
            JSONObject response = restTemplateUtil.getDataForm(REVERSE_GEOCODING_URL, params, null);
            log.info("高德地图逆地理编码响应: {}", response);
            
            if (response == null || !"1".equals(response.getString("status"))) {
                String info = response != null ? response.getString("info") : "请求失败";
                throw new CrmebException("逆地理编码失败: " + info);
            }
            
            JSONObject regeocode = response.getJSONObject("regeocode");
            if (regeocode == null) {
                throw new CrmebException("未找到该坐标的地址信息");
            }
            
            JSONObject addressComponent = regeocode.getJSONObject("addressComponent");
            
            Map<String, Object> result = new HashMap<>();
            result.put("formatted_address", regeocode.getString("formatted_address"));
            result.put("country", addressComponent.getString("country"));
            result.put("province", addressComponent.getString("province"));
            result.put("city", addressComponent.getString("city"));
            result.put("district", addressComponent.getString("district"));
            result.put("township", addressComponent.getString("township"));
            result.put("street", addressComponent.getString("streetNumber"));
            result.put("adcode", addressComponent.getString("adcode"));
            
            return result;
        } catch (Exception e) {
            log.error("高德地图逆地理编码失败", e);
            throw new CrmebException("逆地理编码失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        checkApiKey();
        
        try {
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.add("key", amapKey);
            params.add("origins", lng1 + "," + lat1);
            params.add("destination", lng2 + "," + lat2);
            params.add("type", "1"); // 直线距离
            params.add("output", "JSON");
            
            JSONObject response = restTemplateUtil.getDataForm(DISTANCE_URL, params, null);
            log.info("高德地图距离计算响应: {}", response);
            
            if (response == null || !"1".equals(response.getString("status"))) {
                String info = response != null ? response.getString("info") : "请求失败";
                throw new CrmebException("距离计算失败: " + info);
            }
            
            JSONArray results = response.getJSONArray("results");
            if (results == null || results.isEmpty()) {
                throw new CrmebException("距离计算失败");
            }
            
            JSONObject distanceResult = results.getJSONObject(0);
            String distance = distanceResult.getString("distance");
            String duration = distanceResult.getString("duration");
            
            Map<String, Object> result = new HashMap<>();
            result.put("distance", Integer.parseInt(distance));
            result.put("duration", Integer.parseInt(duration));
            result.put("unit", "米");
            
            return result;
        } catch (Exception e) {
            log.error("高德地图距离计算失败", e);
            throw new CrmebException("距离计算失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> addressSuggestion(String keyword, String city) {
        checkApiKey();
        
        try {
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.add("key", amapKey);
            params.add("keywords", keyword);
            params.add("output", "JSON");
            if (StringUtils.isNotBlank(city)) {
                params.add("city", city);
            }
            
            JSONObject response = restTemplateUtil.getDataForm(SUGGESTION_URL, params, null);
            log.info("高德地图地址建议响应: {}", response);
            
            if (response == null || !"1".equals(response.getString("status"))) {
                String info = response != null ? response.getString("info") : "请求失败";
                throw new CrmebException("地址建议失败: " + info);
            }
            
            JSONArray tips = response.getJSONArray("tips");
            List<Map<String, Object>> suggestions = new ArrayList<>();
            
            if (tips != null) {
                for (int i = 0; i < tips.size(); i++) {
                    JSONObject tip = tips.getJSONObject(i);
                    Map<String, Object> suggestion = new HashMap<>();
                    suggestion.put("name", tip.getString("name"));
                    suggestion.put("address", tip.getString("address"));
                    suggestion.put("location", tip.getString("location"));
                    suggestion.put("district", tip.getString("district"));
                    suggestions.add(suggestion);
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("suggestions", suggestions);
            result.put("count", suggestions.size());
            
            return result;
        } catch (Exception e) {
            log.error("高德地图地址建议失败", e);
            throw new CrmebException("地址建议失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> ipLocation(String ip) {
        checkApiKey();
        
        try {
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.add("key", amapKey);
            params.add("output", "JSON");
            if (StringUtils.isNotBlank(ip)) {
                params.add("ip", ip);
            }
            
            JSONObject response = restTemplateUtil.getDataForm(IP_LOCATION_URL, params, null);
            log.info("高德地图IP定位响应: {}", response);
            
            if (response == null || !"1".equals(response.getString("status"))) {
                String info = response != null ? response.getString("info") : "请求失败";
                throw new CrmebException("IP定位失败: " + info);
            }
            
            String province = response.getString("province");
            String city = response.getString("city");
            String rectangle = response.getString("rectangle");
            
            Map<String, Object> result = new HashMap<>();
            result.put("country", "中国");
            result.put("province", province);
            result.put("city", city);
            result.put("adcode", response.getString("adcode"));
            
            // 解析矩形区域中心点作为大概坐标
            if (StringUtils.isNotBlank(rectangle)) {
                String[] coords = rectangle.split(";");
                if (coords.length == 2) {
                    String[] leftBottom = coords[0].split(",");
                    String[] rightTop = coords[1].split(",");
                    if (leftBottom.length == 2 && rightTop.length == 2) {
                        double centerLng = (Double.parseDouble(leftBottom[0]) + Double.parseDouble(rightTop[0])) / 2;
                        double centerLat = (Double.parseDouble(leftBottom[1]) + Double.parseDouble(rightTop[1])) / 2;
                        result.put("lng", String.valueOf(centerLng));
                        result.put("lat", String.valueOf(centerLat));
                    }
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("高德地图IP定位失败", e);
            throw new CrmebException("IP定位失败: " + e.getMessage());
        }
    }
}