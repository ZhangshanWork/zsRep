package im.vinci.server.device.domain.wrappers.requests;

/**
 * Created by ytl on 15/12/5.
 */
public class OTAConfigUser {
    private String username;
    private String password;
    private boolean isLogin;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }
}
