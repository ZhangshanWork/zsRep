package im.vinci.server.utils;


import im.vinci.server.user.domain.UserInfo;

public class UserContext {
    private static final ThreadLocal<UserInfo> userInfoLocal = new ThreadLocal<>();

    public static void setUserInfo(UserInfo userInfo) {
        userInfoLocal.set(userInfo);
    }

    public static UserInfo getUserInfo() {
        return userInfoLocal.get();
    }

}