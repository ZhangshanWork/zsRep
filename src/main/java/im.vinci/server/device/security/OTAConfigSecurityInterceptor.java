package im.vinci.server.device.security;

import im.vinci.server.device.domain.wrappers.requests.OTAConfigUser;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by ytl on 15/12/5.
 */
public class OTAConfigSecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 验证用户是否登录
        Object obj = request.getSession().getAttribute("cur_user");
        if (null == obj || !(obj instanceof OTAConfigUser)) {
            response.sendRedirect(request.getContextPath() + "/vinci/device/otaconfig/login");
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
