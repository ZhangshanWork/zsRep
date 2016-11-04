package im.vinci.server.common.filter;


import im.vinci.server.user.service.UserLoginAndBindDeviceService;
import im.vinci.server.utils.UserContext;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author sunli
 */
@Component
public class UserLoginFilter implements Filter {

    @Autowired
    private UserLoginAndBindDeviceService service;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        String uid = httpServletRequest.getHeader("uid");
        if (StringUtils.hasText(uid) && NumberUtils.isNumber(uid)) {
            UserContext.setUserInfo(service.getUserInfo(Long.parseLong(uid),true));
        }
        try {
            chain.doFilter(request, response);
        }finally {
            UserContext.setUserInfo(null);
        }
    }
    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {

    }

}