package com.zbkj.common.wxjava.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * wxpay pay properties.
 *
 * @author Binary Wang
 */
@ConfigurationProperties(prefix = "wx.pay.mp")
public class WxPayMpProperties extends WxPayProperties {

}
