package com.zbkj.admin.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;
import cn.hutool.core.util.StrUtil;
import org.springframework.http.HttpMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 静态资源日志过滤器
 *
 * @author Han
 * @version 1.0.0
 * @Date 2025/4/23
 */
public class StaticResourcesLogFilter extends AbstractMatcherFilter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event.getFormattedMessage().contains("GET")) {
            HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
            if (request.getMethod().equals(HttpMethod.GET.toString())) {
                String uri = request.getRequestURI();
                if (StrUtil.isNotBlank(uri) && (uri.contains("png") || uri.contains("jpg") || uri.contains("jpeg") || uri.contains("gif") || uri.contains("bmp"))) {
                    return FilterReply.DENY;
                }
            }
        }
        return FilterReply.NEUTRAL;
    }
}
