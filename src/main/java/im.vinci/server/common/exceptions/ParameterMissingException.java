package im.vinci.server.common.exceptions;

import im.vinci.server.common.exceptions.error.ErrorCode;

/**
 * Created by henryhome on 9/14/15.
 */
public class ParameterMissingException extends VinciException {

    public ParameterMissingException() {
        super(ErrorCode.BAD_REQUEST, "Missing parameters", "参数不全");
    }

    public ParameterMissingException(Integer errorCode, String errorMsg, String errorMsgToUser) {
        super(errorCode, errorMsg, errorMsgToUser);
    }
}
