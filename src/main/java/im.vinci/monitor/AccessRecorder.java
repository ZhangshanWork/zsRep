package im.vinci.monitor;

import java.lang.reflect.Method;

/**
 * Created by zhongzhengkai on 15/12/26.
 */
public class AccessRecorder {


    private final static ThreadLocal<AccessRecorder> threadLocal = new ThreadLocal<>();

    public static AccessRecorder getAccessRecorder() {
        AccessRecorder accessRecorder = threadLocal.get();
        if (accessRecorder == null) {
            accessRecorder = new AccessRecorder();
            threadLocal.set(accessRecorder);
        }
        return accessRecorder;

    }

    public static void clear() {
        threadLocal.remove();
    }

    private long startTimestamp = System.currentTimeMillis();

    private Object[] request;

    private Object response;

    private Method method;

    private Exception exception;

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public Object[] getRequest() {
        return request;
    }

    public AccessRecorder setRequest(Object[] request) {
        this.request = request;
        return this;
    }

    public Object getResponse() {
        return response;
    }

    public AccessRecorder setResponse(Object response) {
        this.response = response;
        return this;
    }

    public Method getMethod() {
        return method;
    }

    public AccessRecorder setMethod(Method method) {
        this.method = method;
        return this;
    }

    public Exception getException() {
        return exception;
    }

    public AccessRecorder setException(Exception exception) {
        this.exception = exception;
        return this;
    }
}
