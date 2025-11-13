package com.zbkj.admin.config;

import cn.hutool.core.util.StrUtil;
import com.github.binarywang.wxpay.service.WxPayService;
import com.zbkj.common.wxjava.WxPayServiceFactory;
import com.zbkj.common.wxjava.properties.WxPayAppProperties;
import com.zbkj.common.wxjava.properties.WxPayMaProperties;
import com.zbkj.common.wxjava.properties.WxPayMpProperties;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Binary Wang
 */
@Configuration
@ConditionalOnClass(WxPayService.class)
@EnableConfigurationProperties({WxPayMaProperties.class, WxPayMpProperties.class, WxPayAppProperties.class})
@AllArgsConstructor
public class WxPayConfiguration {

  private WxPayMaProperties maProperties;
  private WxPayMpProperties mpProperties;
  private WxPayAppProperties appProperties;

  @Bean
  @ConditionalOnMissingBean
  public WxPayServiceFactory wxPayServiceFactory() {
    WxPayServiceFactory factory = new WxPayServiceFactory();
    if (StrUtil.isNotBlank(this.maProperties.getAppId())) {
      factory.registerConfig("ma", maProperties);
    }
    if (StrUtil.isNotBlank(this.mpProperties.getAppId())) {
      factory.registerConfig("mp", mpProperties);
    }
    if (StrUtil.isNotBlank(this.appProperties.getAppId())) {
      factory.registerConfig("app", appProperties);
    }
    return factory;
  }

}
