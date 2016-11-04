package im.vinci.server.common.exceptions;

/**
 * Created by henryhome on 2/28/15.
 */
public class ServerErrorException extends VinciException {

    public ServerErrorException() {
        super(500, "Internal server error", "服务器错误");
    }

    public ServerErrorException(Integer errorCode, String errorMsg, String errorMsgToUser) {
        super(errorCode, errorMsg, errorMsgToUser);
    }
}




