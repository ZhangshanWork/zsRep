package im.vinci.server.common.exceptions.handler;

import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.device.InvalidDeviceException;
import im.vinci.server.common.exceptions.device.InvalidDeviceUpdateException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.common.exceptions.error.ErrorInfo;
import im.vinci.server.device.controller.DeviceModifyController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by henryhome on 9/15/15.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
@ControllerAdvice(basePackageClasses = {DeviceModifyController.class})
public class DeviceExceptionHandler {

    public Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(value = {InvalidDeviceException.class, InvalidDeviceUpdateException.class})
    @ResponseBody
    public ErrorInfo handleDeviceExceptions(VinciException ex) {
        if (ex.getErrorCode() == ErrorCode.INTERNAL_SERVER_ERROR) {
            logger.error(ex.toString(), ex);
        } else {
            logger.error(ex.toString());
        }
        return new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg(), ex.getErrorMsgToUser());
    }
}



