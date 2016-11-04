package im.vinci.server.common.exceptions.error;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by henryhome on 2/21/15.
 */
public class ErrorInfo {

    private Integer status;
    @JsonIgnore
    private String message;
    private String messageToUser;

    public ErrorInfo() {
    }

    public ErrorInfo(Integer status, String message, String messageToUser) {
        this.status = status;
        this.message = message;
        this.messageToUser = messageToUser;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageToUser() {
        return messageToUser;
    }

    public void setMessageToUser(String messageToUser) {
        this.messageToUser = messageToUser;
    }
}
