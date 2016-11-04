package im.vinci.server.common.exceptions;

/**
 * Created by henryhome on 10/19/15.
 */
public class DatabaseException extends VinciException {

    public DatabaseException() {
        super(500, "Internal server error", "数据库错误");
    }

    public DatabaseException(Integer errorCode, String errorMsg, String errorMsgToUser) {
        super(errorCode, errorMsg, errorMsgToUser);
    }
}
