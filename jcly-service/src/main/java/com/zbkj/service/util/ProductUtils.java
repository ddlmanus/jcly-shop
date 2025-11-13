package com.zbkj.service.util;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.zbkj.common.constants.SysConfigConstants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.product.ProductAttribute;
import com.zbkj.common.model.product.ProductAttributeOption;
import com.zbkj.common.response.ProductResponseForCopyProduct;
import com.zbkj.common.result.CommonResultCode;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.common.utils.UrlUtil;
import com.zbkj.service.service.SystemConfigService;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * 商品工具类
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
public class ProductUtils {
    private String baseUrl;

    String rightUrl;

    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * 解析淘宝产品数据
     * @param url
     * @param tag
     * @throws IOException
     * @throws JSONException
     */
    public ProductResponseForCopyProduct getTaobaoProductInfo99Api(String url, int tag) throws JSONException, IOException {
        setConfig(url, tag);
        JSONObject tbJsonData = getRequestFromUrl(baseUrl + rightUrl);
        JSONObject data = tbJsonData.getJSONObject("data");
        if (null == data) throw new CrmebException("复制商品失败--返回数据格式错误--未找到data");
        JSONObject item = data.getJSONObject("item");
        if (null == item) throw new CrmebException("复制商品失败--返回数据格式错误--未找到item");

        ProductResponseForCopyProduct copyProduct = new ProductResponseForCopyProduct();
        copyProduct.setName(item.getString("title"));
        copyProduct.setIntro(item.getString("title"));
        copyProduct.setSliderImage(item.getString("images"));
        copyProduct.setImage(item.getString("images").split(",")[0]
                .replace("[", "").replace("\"", ""));
        copyProduct.setKeyword(item.getString("title"));
        copyProduct.setContent(item.getString("desc"));

        copyProduct.setSpecType(true);
        JSONArray props = item.getJSONArray("props");
        if (null == props || props.length() < 1) {
            copyProduct.setSpecType(false);
            return copyProduct;
        }
        List<ProductAttribute> spaAttes = new ArrayList<>();
        for (int i = 0; i < props.length(); i++) {
            JSONObject pItem = props.getJSONObject(i);
            ProductAttribute spattr = new ProductAttribute();
            spattr.setAttributeName(pItem.getString("name"));
            JSONArray values = pItem.getJSONArray("values");

            List<ProductAttributeOption> optionList = new ArrayList<>();
            for (int j = 0; j < values.length(); j++) {
                JSONObject value = values.getJSONObject(j);
                ProductAttributeOption option = new ProductAttributeOption();
                option.setOptionName(value.getString("name"));
                optionList.add(option);
            }
            spattr.setOptionList(optionList);
            spaAttes.add(spattr);
        }
        copyProduct.setAttrList(spaAttes);
        return copyProduct;
    }

    /**
     * 解析京东产品数据
     * @param url
     * @param tag
     * @return
     * @throws JSONException
     */
    public ProductResponseForCopyProduct getJDProductInfo99Api(String url, int tag) throws IOException, JSONException {
        setConfig(url,tag);
        JSONObject tbJsonData = getRequestFromUrl(baseUrl + rightUrl);

        JSONObject data = tbJsonData.getJSONObject("data");
        if (null == data) throw new CrmebException("复制商品失败--返回数据格式错误--未找到data");
        JSONObject item = data.getJSONObject("item");
        if (null == item) throw new CrmebException("复制商品失败--返回数据格式错误--未找到item");

        ProductResponseForCopyProduct copyProduct = new ProductResponseForCopyProduct();
        copyProduct.setName(item.getString("name"));
        copyProduct.setIntro(item.getString("name"));
        copyProduct.setSliderImage(item.getString("images"));
        copyProduct.setImage(item.getString("images").split(",")[0]
                .replace("[", "").replace("\"", ""));
        copyProduct.setPrice(new BigDecimal(item.getString("price")));
        copyProduct.setContent(item.getString("desc"));

        JSONObject props = item.getJSONObject("skuProps");
        if (null == props) throw new CrmebException("复制商品失败--返回数据格式错误--未找到props");

        List<ProductAttribute> spaAttes = new ArrayList<>();
        JSONObject saleJson = item.getJSONObject("saleProp");
        int attrValueIsNullCount = 0;
        Iterator<String> saleProps = saleJson.keys();
        while (saleProps.hasNext()) {
            ProductAttribute spattr = new ProductAttribute();
            String stepkey = saleProps.next();
            String stepValue = props.getString(stepkey);
            String stepValueValidLength = stepValue.replace("[","").replace("]","").replace("\"","");
            if(stepValueValidLength.length() > 0){
                com.alibaba.fastjson.JSONArray stepValues = JSON.parseArray(stepValue);
                int c = stepValues.get(0).toString().length();
                attrValueIsNullCount += c == 0 ? 1 : 0;
                spattr.setAttributeName(saleJson.getString(stepkey));

                List<ProductAttributeOption> optionList = new ArrayList<>();
                String stepKeyStr = props.getString(stepkey);
                List<String> stepKeyList = CrmebUtil.stringToArrayStr(stepKeyStr);
                for (String value : stepKeyList) {
                    ProductAttributeOption option = new ProductAttributeOption();
                    option.setOptionName(value);
                    optionList.add(option);
                }
                spattr.setOptionList(optionList);
                spaAttes.add(spattr);
                copyProduct.setAttrList(spaAttes);
            }else{
                attrValueIsNullCount += 1;
            }
        }
        // 判断是否单属性
        copyProduct.setSpecType(spaAttes.size() != attrValueIsNullCount);
        return copyProduct;
    }

    /**
     * 解析天猫产品数据
     * @param url
     * @param tag
     * @return
     * @throws JSONException
     */
    public ProductResponseForCopyProduct getTmallProductInfo99Api(String url, int tag) throws JSONException, IOException {
        setConfig(url, tag);
        JSONObject tbJsonData = getRequestFromUrl(baseUrl + rightUrl);
        JSONObject data = tbJsonData.getJSONObject("data");
        if (null == data) throw new CrmebException("复制商品失败--返回数据格式错误--未找到data");
        JSONObject item = data.getJSONObject("item");
        if (null == item) throw new CrmebException("复制商品失败--返回数据格式错误--未找到item");

        ProductResponseForCopyProduct copyProduct = new ProductResponseForCopyProduct();
        copyProduct.setName(item.getString("title"));
        copyProduct.setIntro(item.getString("subTitle"));
        copyProduct.setSliderImage(item.getString("images"));
        copyProduct.setImage(item.getString("images").split(",")[0]
                .replace("[", "").replace("\"", ""));
        copyProduct.setKeyword(item.getString("title"));
        copyProduct.setContent(item.getString("desc"));

        copyProduct.setSpecType(true);
        JSONArray props = item.getJSONArray("props");
        if (null == props || props.length() < 1) {
            // 无规格商品
            copyProduct.setSpecType(false);
            return copyProduct;
        }
        List<ProductAttribute> spaAttes = new ArrayList<>();
        for (int i = 0; i < props.length(); i++) {
            JSONObject pItem = props.getJSONObject(i);
            ProductAttribute spattr = new ProductAttribute();
            spattr.setAttributeName(pItem.getString("name"));
            JSONArray values = pItem.getJSONArray("values");

            List<ProductAttributeOption> optionList = new ArrayList<>();
            for (int j = 0; j < values.length(); j++) {
                JSONObject value = values.getJSONObject(j);
                ProductAttributeOption option = new ProductAttributeOption();
                option.setOptionName(value.getString("name"));
                optionList.add(option);
            }
            spattr.setOptionList(optionList);
            spaAttes.add(spattr);
        }
        copyProduct.setAttrList(spaAttes);
        return copyProduct;
    }

    /**
     * 解析拼多多产品数据
     * @param url
     * @param tag
     * @return
     * @throws JSONException
     */
    public ProductResponseForCopyProduct getPddProductInfo99Api(String url, int tag) throws JSONException, IOException {
        setConfig(url, tag);
        JSONObject tbJsonData = getRequestFromUrl(baseUrl + rightUrl);
        JSONObject data = tbJsonData.getJSONObject("data");
        if (null == data) throw new CrmebException("复制商品失败--返回数据格式错误--未找到data");
        JSONObject item = data.getJSONObject("item");
        if (null == item) throw new CrmebException("复制商品失败--返回数据格式错误--未找到item");

        ProductResponseForCopyProduct copyProduct = new ProductResponseForCopyProduct();
        copyProduct.setName(item.getString("goodsName"));
        copyProduct.setIntro(item.getString("goodsDesc"));
        copyProduct.setSliderImage(item.getString("thumbUrl"));
        copyProduct.setImage(item.getString("banner"));
        copyProduct.setVideoLink(item.getJSONArray("video").getJSONObject(0).getString("videoUrl"));
        copyProduct.setPrice(BigDecimal.valueOf(item.getDouble("maxNormalPrice")));
        copyProduct.setOtPrice(BigDecimal.valueOf(item.getDouble("marketPrice")));

        JSONArray props = item.getJSONArray("skus");
        if (null == props) throw new CrmebException("复制商品失败--返回数据格式错误--未找到props");
        if (props.length() > 0) {
            List<ProductAttribute> spaAttes = new ArrayList<>();
            HashMap<String,List<String>> tempAttr = new HashMap<>();
            for (int i = 0; i < props.length(); i++) {
                JSONObject pItem = props.getJSONObject(i);
                JSONArray specArray = pItem.getJSONArray("specs");
                for (int j = 0; j < specArray.length(); j++) {
                    JSONObject specItem = specArray.getJSONObject(j);
                    String keyTemp = specItem.getString("spec_key");
                    String valueTemp = specItem.getString("spec_value");
                    if(tempAttr.containsKey(keyTemp)){
                        if(!tempAttr.get(keyTemp).contains(valueTemp)){
                            tempAttr.get(keyTemp).add(valueTemp);
                        }
                    }else{
                        List<String> tempList = new ArrayList<>();
                        tempList.add(valueTemp);
                        tempAttr.put(keyTemp, tempList);
                    }
                }

            }
            Iterator iterator = tempAttr.keySet().iterator();
            while (iterator.hasNext()){
                String key = (String)iterator.next();
                ProductAttribute spattr = new ProductAttribute();
                spattr.setAttributeName(key);
                String string = tempAttr.get(key).toString();
                List<String> opList = CrmebUtil.stringToArrayStr(string);
                List<ProductAttributeOption> optionList = new ArrayList<>();
                for (String optionValue : opList) {
                    ProductAttributeOption option = new ProductAttributeOption();
                    option.setOptionName(optionValue);
                    optionList.add(option);
                }
                spattr.setOptionList(optionList);
                spaAttes.add(spattr);
            }
            copyProduct.setAttrList(spaAttes);
        }
        return copyProduct;
    }

    /**
     * *** 苏宁返回的数据不一致，暂放
     * 解析苏宁产品数据
     * @param url
     * @param tag
     * @return
     * @throws JSONException
     */
    public ProductResponseForCopyProduct getSuningProductInfo99Api(String url, int tag) throws JSONException, IOException {
        setConfig(url,tag);
        JSONObject tbJsonData = getRequestFromUrl(baseUrl + rightUrl);
        System.out.println("tbJsonData:"+tbJsonData);
        JSONObject data = tbJsonData.getJSONObject("data");
        if (null == data) throw new CrmebException("复制商品失败--返回数据格式错误--未找到data");

        ProductResponseForCopyProduct copyProduct = new ProductResponseForCopyProduct();
        copyProduct.setName(data.getString("title"));
        copyProduct.setIntro(data.getString("title"));
        copyProduct.setSliderImage(data.getString("images"));
        copyProduct.setImage(data.getString("images").split(",")[0]
                .replace("[", "").replace("\"", ""));
        Long priceS = data.getLong("price");
        copyProduct.setPrice(BigDecimal.valueOf(priceS));
        copyProduct.setContent(data.getString("desc"));

        List<ProductAttribute> spaAttes = new ArrayList<>();
        ProductAttribute spattr = new ProductAttribute();
        spattr.setAttributeName("默认");
        List<ProductAttributeOption> optionList = new ArrayList<>();
        ProductAttributeOption option = new ProductAttributeOption();
        option.setOptionName("默认");
        optionList.add(option);
        spattr.setOptionList(optionList);
        copyProduct.setSpecType(false);
        copyProduct.setAttrList(spaAttes);
        return copyProduct;
    }

    public static void main(String[] args) {
        String rightEndUrl = "&itemid=";
        String url = "https://detail.tmall.hk/hk/item.htm?id=619049590004&spm=a1z10.1-b.w4004-21302253139.41.23962b1brA9Glm";
        rightEndUrl += UrlUtil.getParamsByKey(url, "id");
        System.out.println(rightEndUrl);
    }

    /**
     * 设置配置数据
     * @param tag
     */
    public void setConfig(String url, int tag){
        String rightEndUrl = "&itemid=";
        switch (tag){ // 导入平台1=淘宝，2=京东，3=苏宁，4=拼多多， 5=天猫
            case 1:
                baseUrl = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_IMPORT_PRODUCT_TB);
                rightEndUrl += UrlUtil.getParamsByKey(url, "id");
                break;
            case 2:
                baseUrl = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_IMPORT_PRODUCT_JD);
                String replace = url.substring(url.lastIndexOf("/") + 1);
                String substring = replace.substring(0, replace.indexOf("."));
                rightEndUrl += substring;
                break;
            case 3:
                baseUrl = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_IMPORT_PRODUCT_SN);
                int start = url.indexOf(".com/") + 5;
                int end = url.indexOf(".html");
                String sp = url.substring(start,end);
                String[] shopProduct = sp.split("/");
                rightEndUrl += shopProduct[1]+"&shopid="+shopProduct[0];
                break;
            case 4:
                rightEndUrl += UrlUtil.getParamsByKey(url, "goods_id");
                baseUrl = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_IMPORT_PRODUCT_PDD);
                break;
            case 5:
                rightEndUrl += UrlUtil.getParamsByKey(url, "id");
                baseUrl = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_IMPORT_PRODUCT_TM);
                break;
        }
        String token = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_COPY_PRODUCT_APIKEY);
        if(StringUtils.isBlank(token)){
            throw new CrmebException("请配置复制产品平台的Token -- www.99api.com");
        }
        if(StringUtils.isBlank(baseUrl)){
            throw new CrmebException("请配置复制产品平台的Url-- www.99api.com");
        }
        rightUrl = "?apikey=" + token + rightEndUrl;

    }

    /**
     * 99api产品复制工具方法
     * @param rd
     * @return
     * @throws IOException
     */
    public static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    /**
     * 根据url访问99api后返回对应的平台的产品json数据
     * @param url
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public static JSONObject getRequestFromUrl(String url) throws IOException, JSONException {
        URL realUrl = new URL(url);
        URLConnection conn = realUrl.openConnection();
        // 兼容一号通请求新商品时时间过长的问题
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        InputStream instream = conn.getInputStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(instream, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            instream.close();
        }
    }

    /**
     * 一号通复制商品转公共商品参数
     * @param jsonObject 一号通复制商品
     *
     */
    public static ProductResponseForCopyProduct onePassCopyTransition(com.alibaba.fastjson.JSONObject jsonObject) {
        if (ObjectUtil.isNull(jsonObject)) {
            throw new CrmebException(CommonResultCode.NOT_FOUND, "商品资源不存在");
        }

        ProductResponseForCopyProduct productRequestForCC = new ProductResponseForCopyProduct();
        productRequestForCC.setName(jsonObject.getString("store_name"));
        productRequestForCC.setIntro(jsonObject.getString("store_info"));
        productRequestForCC.setSliderImage(jsonObject.getString("slider_image"));
        productRequestForCC.setImage(jsonObject.getString("image").replace("[", "").replace("\"", ""));
        productRequestForCC.setKeyword(jsonObject.getString("store_name"));
        productRequestForCC.setCost(jsonObject.getBigDecimal("cost"));
        productRequestForCC.setPrice(jsonObject.getBigDecimal("price"));
        productRequestForCC.setOtPrice(jsonObject.getBigDecimal("ot_price"));
        productRequestForCC.setUnitName(jsonObject.getString("unit_name"));
        productRequestForCC.setContent(jsonObject.getString("description"));
        productRequestForCC.setSpecType(true);
        com.alibaba.fastjson.JSONArray props = jsonObject.getJSONArray("items");
        if (null == props || props.size() < 1) {
            // 无规格商品
            productRequestForCC.setSpecType(false);
            return productRequestForCC;
        }
        productRequestForCC.setAttrList(getAttrListByJsonArray(props));
        return productRequestForCC;
    }

    private static List<ProductAttribute> getAttrListByJsonArray(com.alibaba.fastjson.JSONArray items) {
        List<ProductAttribute> spaAttes = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            com.alibaba.fastjson.JSONObject pItem = items.getJSONObject(i);
            ProductAttribute spattr = new ProductAttribute();
            spattr.setAttributeName(pItem.getString("value"));
            com.alibaba.fastjson.JSONArray values = pItem.getJSONArray("detail");

            List<ProductAttributeOption> optionList = new ArrayList<>();
            for (int j = 0; j < values.size(); j++) {
                String value = values.getString(j);
                ProductAttributeOption option = new ProductAttributeOption();
                option.setOptionName(value);
                optionList.add(option);
            }
            spattr.setOptionList(optionList);
            spaAttes.add(spattr);
        }
        return spaAttes;
    }

}
