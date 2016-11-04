package org.springframework.web.filter;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 增加了可以多次读http request body的东西
 * Created by tim@vinci on 16/7/16.
 */
public class RequestContextFilter extends OncePerRequestFilter {


    private boolean threadContextInheritable = false;


    /**
     * Set whether to expose the LocaleContext and RequestAttributes as inheritable
     * for child threads (using an {@link InheritableThreadLocal}).
     * <p>Default is "false", to avoid side effects on spawned background threads.
     * Switch this to "true" to enable inheritance for custom child threads which
     * are spawned during request processing and only used for this request
     * (that is, ending after their initial task, without reuse of the thread).
     * <p><b>WARNING:</b> Do not use inheritance for child threads if you are
     * accessing a thread pool which is configured to potentially add new threads
     * on demand (e.g. a JDK {@link java.util.concurrent.ThreadPoolExecutor}),
     * since this will expose the inherited context to such a pooled thread.
     */
    public void setThreadContextInheritable(boolean threadContextInheritable) {
        this.threadContextInheritable = threadContextInheritable;
    }


    /**
     * Returns "false" so that the filter may set up the request context in each
     * asynchronously dispatched thread.
     */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    /**
     * Returns "false" so that the filter may set up the request context in an
     * error dispatch.
     */
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        MultiReadHttpServletRequest readHttpServletRequest = new MultiReadHttpServletRequest(request);
        ServletRequestAttributes attributes = new ServletRequestAttributes(readHttpServletRequest, response);
        initContextHolders(readHttpServletRequest, attributes);

        try {
            filterChain.doFilter(readHttpServletRequest, response);
        }
        finally {
            resetContextHolders();
            if (logger.isDebugEnabled()) {
                logger.debug("Cleared thread-bound request context: " + request);
            }
            attributes.requestCompleted();
        }
    }

    private void initContextHolders(HttpServletRequest request, ServletRequestAttributes requestAttributes) {
        LocaleContextHolder.setLocale(request.getLocale(), this.threadContextInheritable);
        RequestContextHolder.setRequestAttributes(requestAttributes, this.threadContextInheritable);
        if (logger.isDebugEnabled()) {
            logger.debug("Bound request context to thread: " + request);
        }
    }

    private void resetContextHolders() {
        LocaleContextHolder.resetLocaleContext();
        RequestContextHolder.resetRequestAttributes();
    }
}
