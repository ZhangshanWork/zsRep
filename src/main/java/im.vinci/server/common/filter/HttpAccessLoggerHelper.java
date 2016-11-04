package im.vinci.server.common.filter;

import im.vinci.monitor.AccessRecorder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Created by zhongzhengkai on 15/12/26.
 */
@Aspect
@Order(1) //这个要最后来
@Component
public class HttpAccessLoggerHelper {
    private Logger logger = LoggerFactory.getLogger(getClass());
//    @Around("(execution(* im.vinci.server.controllers.*.*.*(..)) || execution(* im.vinci.server.controllers.*.*(..)) " +
//            "|| execution(* im.vinci.*.*.controller.*.*(..)))" +
//            "&& @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        AccessRecorder recorder = AccessRecorder.getAccessRecorder();
        recorder.setMethod(MethodSignature.class.cast(point.getSignature()).getMethod());
        recorder.setRequest(point.getArgs());
        try {
            Object result = point.proceed();
            recorder.setResponse(result);
            return result;
        }catch (Exception e) {
            recorder.setException(e);
            throw e;
        }
    }
}

