package im.vinci.server.common.exceptions;

/**
 * Created by henryhome on 2/28/15.
 */
public class VinciException extends RuntimeException {

    private Integer errorCode;
    private String errorMsg;
    private String errorMsgToUser;

    public VinciException() {
    }

    public VinciException(Integer errorCode, String errorMsg, String errorMsgToUser) {
        this(null,errorCode,errorMsg,errorMsgToUser);
    }


    public VinciException(Throwable cause) {
        super(cause);
    }

    public VinciException(Throwable cause, Integer errorCode, String errorMsg, String errorMsgToUser) {
        super(errorMsg, cause);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.errorMsgToUser = errorMsgToUser;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getErrorMsgToUser() {
        return errorMsgToUser;
    }

    public void setErrorMsgToUser(String errorMsgToUser) {
        this.errorMsgToUser = errorMsgToUser;
    }

    public String toString() {
        return "{\"status\":" + errorCode + ",\"message\":\"" + errorMsg + "\",\"message_to_user\":\"" + errorMsgToUser + "\"}";
    }

}
