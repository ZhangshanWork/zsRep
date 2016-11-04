package im.vinci.server.utils;

import im.vinci.monitor.QMonitor;
import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BizTemplate
 */
public abstract class BizTemplate<T> {
    protected String monitorKey;
    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected BizTemplate(String monitorKey) {
        this.monitorKey = monitorKey;
    }

    protected abstract void checkParams() throws VinciException;

    protected abstract T process() throws Exception;

    protected void afterProcess() {
    }

    protected void onSuccess(T result) {
    }

    /**
     * @return 是否抛出exception, 默认向上抛出
     */
    protected boolean onError(Throwable e) {
        return true;
    }

    /**
     * 如果exception不抛出,需要设定一个默认值
     */
    protected T defaultResult() {
        return null;
    }

    protected boolean isCriticalErrorCode(int errorCode) {
        return false;
    }

    public T execute() throws VinciException {
        try {
            checkParams();
        } catch (VinciException|IllegalArgumentException e) {
            recordInvalidParam(e);
            throw e;
        } catch (Throwable e) {
            recordInvalidParam(e);
            throw new VinciException();
        }

        long start = System.currentTimeMillis();
        try {
            T result = process();
            onSuccess(result);
            long time = System.currentTimeMillis() - start;
            QMonitor.recordOne(monitorKey + "_Success", time);
            logger.info("执行业务逻辑成功: monitorKey={}, time={}",monitorKey, time);
            return result;
        } catch (VinciException|IllegalArgumentException ex) {
            boolean isThrow = onError(ex);
            VinciException e;
            if (!(ex instanceof VinciException)) {
                e = new VinciException(ex,ErrorCode.ARGUMENT_ERROR,ex.getMessage(),ex.getMessage());
            } else {
                e = (VinciException)ex;
            }
            QMonitor.recordOne(monitorKey + "_VinciException");
            QMonitor.recordOne(monitorKey + "_Failed");
            QMonitor.recordOne("BizTemplate_VinciException");

            if (isCriticalErrorCode(e.getErrorCode())) {
                QMonitor.recordOne(monitorKey + "_CriticalError");
                logger.warn("执行业务逻辑出现异常错误: monitorKey="+ monitorKey, e);
            } else {
                QMonitor.recordOne(monitorKey + "_CommonError");
                if (logger.isDebugEnabled()) {
                    logger.warn("执行业务逻辑出现普通错误: monitorKey="+monitorKey,e);
                } else {
                    logger.warn("执行业务逻辑出现普通错误: monitorKey={}, code={}, msg={}", monitorKey, e.getErrorCode(), e.getMessage());
                }
            }
            if (isThrow)
                throw e;
        } catch (Throwable e) {
            boolean isThrow = onError(e);
            logger.error("执行业务逻辑出现未知异常 monitoryKey={}", monitorKey, e);
            QMonitor.recordOne(monitorKey + "_UnknownError");
            QMonitor.recordOne(monitorKey + "_Failed");
            QMonitor.recordOne(monitorKey + "_CriticalError");
            QMonitor.recordOne("BizTemplate_UnknownError");
            if (isThrow)
                throw new VinciException(e, ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage(), "未知错误");
        } finally {
            afterProcess();
            QMonitor.recordOne(monitorKey + "_Invoke", System.currentTimeMillis() - start);
        }
        return defaultResult();
    }

    private void recordInvalidParam(Throwable e) {
        if (logger.isDebugEnabled()) {
            logger.debug(monitorKey + "_校验参数失败", e);
        } else {
            logger.info(monitorKey + "_校验参数失败: " + e.toString());
        }
        QMonitor.recordOne(this.monitorKey + "_Invalid_Parameter");
        QMonitor.recordOne("BizTemplate_VinciException_Invalid_Parameter");
    }
}
