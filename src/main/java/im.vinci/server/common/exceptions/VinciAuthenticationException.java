package im.vinci.server.common.exceptions;

import im.vinci.server.common.exceptions.error.ErrorCode;

/**
 * Created by tim@vinci on 16/7/16.
 */
public class VinciAuthenticationException extends VinciException{
    public VinciAuthenticationException(String errorMsg) {
        super(null, ErrorCode.UNAUTHORIZED,errorMsg,"unauthorized request");
    }
}
