package im.vinci.server.common.exceptions;

import im.vinci.server.common.exceptions.error.ErrorCode;

/**
 * Created by henryhome on 9/14/15.
 */
public class InvalidParameterException extends VinciException {

    public InvalidParameterException() {
        super(ErrorCode.BAD_REQUEST, "Parameter Error", "参数错误");
    }

    public InvalidParameterException(Integer errorCode, String errorMsg, String errorMsgToUser) {
        super(errorCode, errorMsg, errorMsgToUser);
    }
}
