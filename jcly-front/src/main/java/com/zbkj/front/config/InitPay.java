package com.zbkj.front.config;

import com.zbkj.common.unionpay.SDKConfig;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author 杜典龙
 * @Date 2025/9/2 0027 16:48
 */
@Component
public class InitPay implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments var){
        //初始化银联参数
        SDKConfig.getConfig().loadPropertiesFromSrc();
    }
}
