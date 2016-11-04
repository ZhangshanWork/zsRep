package im.vinci.server.common.exceptions.device;

import im.vinci.server.common.exceptions.VinciException;

/**
 * Created by henryhome on 11/25/15.
 */
public class InvalidDeviceUpdateException extends VinciException {

    public InvalidDeviceUpdateException(Integer errorCode, String errorMsg, String errorMsgToUser) {
        super(errorCode, errorMsg, errorMsgToUser);
    }
}
