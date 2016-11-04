package im.vinci.server.other.domain.account;

import java.util.Date;

/**
 * Created by henryhome on 2/15/15.
 */
public class VinciUserDetails {

	private static final long serialVersionUID = 7924306413481596924L;
    private Integer id;
    private final String username;
    private final String password;
    private final String deviceId;
    private final String accessKey;
    private final Date accountExpiredTime;
    private final Date credentialsExpiredTime;
    private final boolean isAccountNonExpired;
    private final boolean isCredentialsNonExpired;
    private final boolean isEnabled;

    public VinciUserDetails(String username, String password, String deviceId, String accessKey,
                            Date accountExpiredTime, Date credentialsExpiredTime, boolean isAccountNonExpired,
                            boolean isCredentialsNonExpired, boolean isEnabled) {
        this.username = username;
        this.password = password;
        this.deviceId = deviceId;
        this.accessKey = accessKey;
        this.accountExpiredTime = accountExpiredTime;
        this.credentialsExpiredTime = credentialsExpiredTime;
        this.isAccountNonExpired = isAccountNonExpired;
        this.isCredentialsNonExpired = isCredentialsNonExpired;
        this.isEnabled = isEnabled;
    }

    public VinciUserDetails(Integer id, String username, String password, String deviceId, String accessKey,
                            Date accountExpiredTime, Date credentialsExpiredTime, boolean isAccountNonExpired,
                            boolean isCredentialsNonExpired, boolean isEnabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.deviceId = deviceId;
        this.accessKey = accessKey;
        this.accountExpiredTime = accountExpiredTime;
        this.credentialsExpiredTime = credentialsExpiredTime;
        this.isAccountNonExpired = isAccountNonExpired;
        this.isCredentialsNonExpired = isCredentialsNonExpired;
        this.isEnabled = isEnabled;
    }

    public Integer getId() { return id; }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Date getAccountExpiredTime() {
        return accountExpiredTime;
    }

    public Date getCredentialsExpiredTime() {
        return credentialsExpiredTime;
    }

    public boolean isAccountNonExpired() {
        return isAccountNonExpired;
    }

    public boolean isCredentialsNonExpired() {
        return isCredentialsNonExpired;
    }

    public boolean isAccountNonLocked() {
        return isAccountNonExpired && isCredentialsNonExpired;
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VinciUserDetails that = (VinciUserDetails) o;

        if (!username.equals(that.username)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VinciUserDetails{");
        sb.append("id=").append(id);
        sb.append(", username='").append(username).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", accessKey='").append(accessKey).append('\'');
        sb.append(", deviceId='").append(deviceId).append('\'');
        sb.append(", accountExpiredTime=").append(accountExpiredTime);
        sb.append(", credentialsExpiredTime=").append(credentialsExpiredTime);
        sb.append(", isAccountNonExpired=").append(isAccountNonExpired);
        sb.append(", isCredentialsNonExpired=").append(isCredentialsNonExpired);
        sb.append(", isEnabled=").append(isEnabled);
        sb.append('}');
        return sb.toString();
    }
}



