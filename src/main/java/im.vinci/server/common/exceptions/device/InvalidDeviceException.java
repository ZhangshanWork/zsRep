package im.vinci.server.common.exceptions.device;

import im.vinci.server.common.exceptions.VinciException;

/**
 * Created by henryhome on 11/12/15.
 */
public class InvalidDeviceException extends VinciException {

    public InvalidDeviceException(Integer errorCode, String errorMsg, String errorMsgToUser) {
        super(errorCode, errorMsg, errorMsgToUser);
    }
}
