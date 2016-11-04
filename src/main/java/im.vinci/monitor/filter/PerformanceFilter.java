package im.vinci.monitor.filter;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import im.vinci.monitor.AccessRecorder;
import im.vinci.monitor.util.PerformanceMonitor;
import im.vinci.monitor.util.RequestStats;
import im.vinci.monitor.util.SystemTimer;
import im.vinci.server.utils.JsonUtils;
import im.vinci.server.utils.Networks;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author sunli
 */
@Component
public class PerformanceFilter implements Filter {
    private static final String monitorPrefix = "GlobalPerformance";
    private PerformanceMonitor monitor = null;
    private static Logger logger = LoggerFactory.getLogger("httpAccess");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        long start = SystemTimer.currentTimeMillis();
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        try {
            chain.doFilter(request, response);
            ((HttpServletResponse) response).setHeader("server_time", SystemTimer.currentTimeMillis() + "");
        } finally {
            long send = SystemTimer.currentTimeMillis() - start;
            monitor.markSlowrRquests(send);
            RequestStats.incrementPath(this.getMMVCpath((HttpServletRequest) request), send);
            //记录http请求日志,/healthcheck.html是阿里slb发过来的健康检查请求,每秒一个,为了不让日志大量记录这种请求,先过滤掉
            if (!httpServletRequest.getRequestURI().equals("/healthcheck.html")) {
                logAccessRecord(httpServletRequest, (HttpServletResponse) response, send);
            }
        }
    }
    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {

    }

    private String getMMVCpath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.substring(request.getContextPath().length());
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        monitor = new PerformanceMonitor(monitorPrefix);
    }


    private void logAccessRecord(HttpServletRequest request, HttpServletResponse response, long time) {
        Map<String, Object> map = Maps.newLinkedHashMap();
        map.put("method", request.getMethod());
        map.put("elapsed_time", time);
        if ("GET".equals(request.getMethod()) && StringUtils.hasText(request.getQueryString())) {
            map.put("uri", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getRequestURI() + "?" + request.getQueryString());
        } else {
            map.put("uri", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getRequestURI());
        }

        map.put("protocol", request.getProtocol());
        map.put("remote_addr", Networks.getClientIpAddress(request));
        map.put("status", response.getStatus());
        map.put("content-length", response.getHeader("Content-Length"));

        AccessRecorder recorder = AccessRecorder.getAccessRecorder();
        Method method = recorder.getMethod();
        if (method != null) {
            map.put("method_name", method.getName());
            Parameter[] parameters = method.getParameters();
            if (parameters == null || parameters.length == 0) {
                map.put("params", Collections.emptyList());
            } else {
                Object[] params = recorder.getRequest();
                if (parameters.length == 1 && parameters[0].isAnnotationPresent(RequestBody.class)) {
                    map.put("param", JsonUtils.encode(params[0]));
                } else {
                    List<BasicNameValuePair> pairList = Lists.newArrayListWithCapacity(parameters.length);
                    for (int i=0; i<parameters.length; i++) {
                        String name = null;
                        if (name == null) {
                            RequestParam p = parameters[i].getAnnotation(RequestParam.class);
                            name = (p != null?p.value():null);
                        }
                        if (name == null) {
                            RequestHeader p = parameters[i].getAnnotation(RequestHeader.class);
                            name = (p != null?p.value():null);
                        }
                        if (name == null) {
                            ModelAttribute p = parameters[i].getAnnotation(ModelAttribute.class);
                            name = (p != null?p.value():null);
                        }
                        if (name == null) {
                            name = parameters[i].getName();
                        }
                        pairList.add(new BasicNameValuePair(name,String.valueOf(params[i])));
                    }
                    map.put("params", pairList);
                }
            }

        }
        Object o = recorder.getResponse();
        Exception e = recorder.getException();
        if (e != null) {
            logger.info("{}|exception:{}", JsonUtils.encode(map), e.toString());
        } else if (o instanceof ModelAndView) {
            logger.info("{}|view:{}", JsonUtils.encode(map), ((ModelAndView) o).getViewName());
            if (logger.isDebugEnabled()) {
                logger.debug("modelMap:{}",JsonUtils.encode(((ModelAndView) o).getModelMap()));
            }
        } else if (o instanceof Serializable) {
            if (logger.isDebugEnabled()) {
                logger.info("{}|result:{}", JsonUtils.encode(map), JsonUtils.encode(o));
            } else {
                logger.info("{}|result:{}", JsonUtils.encode(map), JsonUtils.encodeWithMask(o));
            }
        } else {
            logger.info("{}", JsonUtils.encode(map));
        }

        AccessRecorder.clear();

    }

}