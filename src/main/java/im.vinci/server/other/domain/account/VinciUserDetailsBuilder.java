package im.vinci.server.other.domain.account;

import java.util.Date;

/**
 * Created by henryhome on 3/13/15.
 */
public class VinciUserDetailsBuilder {

    private Integer id;
    private String username;
    private String password;
    private String deviceId;
    private String accessKey;
    private Date accountExpiredTime;
    private Date credentialsExpiredTime;
    private Boolean isAccountNonExpired;
    private Boolean isCredentialsNonExpired;
    private Boolean isEnabled;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public Date getAccountExpiredTime() {
        return accountExpiredTime;
    }

    public void setAccountExpiredTime(Date accountExpiredTime) {
        this.accountExpiredTime = accountExpiredTime;
    }

    public Date getCredentialsExpiredTime() {
        return credentialsExpiredTime;
    }

    public void setCredentialsExpiredTime(Date credentialsExpiredTime) {
        this.credentialsExpiredTime = credentialsExpiredTime;
    }

    public Boolean isAccountNonExpired() {
        return isAccountNonExpired;
    }

    public void setAccountNonExpired(Boolean isAccountNonExpired) {
        this.isAccountNonExpired = isAccountNonExpired;
    }

    public Boolean isCredentialsNonExpired() {
        return isCredentialsNonExpired;
    }

    public void setCredentialsNonExpired(Boolean isCredentialsNonExpired) {
        this.isCredentialsNonExpired = isCredentialsNonExpired;
    }

    public Boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public VinciUserDetails build() {

        VinciUserDetails vinciUserDetails = null;
        try {
            if (id != null) {
                vinciUserDetails = new VinciUserDetails(id, username, password, deviceId, accessKey, accountExpiredTime,
                        credentialsExpiredTime, isAccountNonExpired, isCredentialsNonExpired, isEnabled);
            } else {
                vinciUserDetails = new VinciUserDetails(username, password, deviceId, accessKey, accountExpiredTime,
                        credentialsExpiredTime, isAccountNonExpired, isCredentialsNonExpired, isEnabled);
            }
        } catch (Exception e) {
            System.out.println("The error will be" + e);
        }

        return vinciUserDetails;
    }
}
