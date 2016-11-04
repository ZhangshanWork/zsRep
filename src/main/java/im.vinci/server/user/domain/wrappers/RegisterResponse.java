package im.vinci.server.user.domain.wrappers;

import im.vinci.server.user.domain.UserInfo;

import java.io.Serializable;

/**
 * 用户注册response
 * Created by tim@vinci on 16/7/21.
 */
public class RegisterResponse implements Serializable{
    private UserInfo userInfo;

    private boolean isNewUser;

    public RegisterResponse() {
    }

    public RegisterResponse(UserInfo userInfo, boolean isNewUser) {
        this.userInfo = userInfo;
        this.isNewUser = isNewUser;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public RegisterResponse setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
        return this;
    }

    public boolean isNewUser() {
        return isNewUser;
    }

    public RegisterResponse setNewUser(boolean newUser) {
        isNewUser = newUser;
        return this;
    }
}
