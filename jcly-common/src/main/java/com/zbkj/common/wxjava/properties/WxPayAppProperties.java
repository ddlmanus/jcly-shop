package com.zbkj.common.wxjava.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * wxpay pay properties.
 *
 * @author Binary Wang
 */
@ConfigurationProperties(prefix = "wx.pay.app")
public class WxPayAppProperties extends WxPayProperties {

}
