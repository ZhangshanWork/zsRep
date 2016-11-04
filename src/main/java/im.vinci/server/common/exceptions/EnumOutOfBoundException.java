package im.vinci.server.common.exceptions;

/**
 * Created by henryhome on 3/13/15.
 */
public class EnumOutOfBoundException extends Exception {
    public EnumOutOfBoundException() {
    }

    public EnumOutOfBoundException(String message) {

        super(message);
    }

    public EnumOutOfBoundException(String message, Throwable cause) {

        super(message, cause);
    }

    public EnumOutOfBoundException(Throwable cause) {

        super(cause);
    }

    public EnumOutOfBoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {

        super(message, cause, enableSuppression, writableStackTrace);
    }
}
