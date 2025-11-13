package com.zbkj.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zbkj.common.model.csj.CsjAreaStatic;
import com.zbkj.common.model.csj.CsjStatic;
import com.zbkj.common.request.CsjStaticrequest;
import com.zbkj.service.service.CsjAreaStaticService;
import com.zbkj.service.service.CsjStaticService;
import com.zbkj.service.service.WebHookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class WebHookServiceImpl implements WebHookService {
    @Resource
    private CsjStaticService csjStaticService;
    @Resource
    private CsjAreaStaticService csjAreaStaticService;
    @Override
    public void handleCsjEvent(JSONObject jsonObject) {
        log.info("接受到采食家推送到数据{}", jsonObject);
        CsjStaticrequest javaObject = JSONObject.toJavaObject(jsonObject, CsjStaticrequest.class);
        log.info("转换后的数据{}", javaObject);
        CsjStatic csjStatic = csjStaticService.getBaseMapper().selectOne(new LambdaQueryWrapper<>());
        if(csjStatic == null){
            csjStatic = new CsjStatic();
            BeanUtils.copyProperties(javaObject, csjStatic);
            csjStaticService.save(csjStatic);
        }
        List<CsjAreaStatic> csjAreaStatics = javaObject.getCsjAreaStatics();
        for (CsjAreaStatic csjAreaStatic : csjAreaStatics) {
            CsjAreaStatic csjAreaStatic1 = csjAreaStaticService.getBaseMapper().selectOne(new LambdaQueryWrapper<CsjAreaStatic>().eq(CsjAreaStatic::getRegionName, csjAreaStatic.getRegionName()));
            if(csjAreaStatic1 == null){
                csjAreaStaticService.save(csjAreaStatic);
            }else{
                BeanUtils.copyProperties(csjAreaStatic, csjAreaStatic1);
                csjAreaStaticService.updateById(csjAreaStatic1);
            }
        }
    }
}
