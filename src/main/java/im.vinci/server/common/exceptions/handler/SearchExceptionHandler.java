package im.vinci.server.common.exceptions.handler;

import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.search.controller.MusicSearchController;
import im.vinci.server.utils.apiresp.APIResponse;
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
@ResponseStatus(value = HttpStatus.OK)
@ControllerAdvice(basePackageClasses = {MusicSearchController.class})
public class SearchExceptionHandler {

    public Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(value = VinciException.class)
    @ResponseBody
    public APIResponse handleDeviceExceptions(VinciException ex) {
        return APIResponse.returnFail(ex.getErrorCode(),ex.getErrorMsgToUser());
    }
}



