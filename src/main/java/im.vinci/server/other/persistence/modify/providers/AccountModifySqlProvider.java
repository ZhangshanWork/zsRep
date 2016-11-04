package im.vinci.server.other.persistence.modify.providers;

import im.vinci.server.other.domain.account.VinciUserDetails;
import im.vinci.server.other.domain.account.VinciUserDetailsBuilder;
import org.apache.ibatis.jdbc.SQL;

/**
 * Created by henryhome on 3/13/15.
 */
public class AccountModifySqlProvider {

    public String addVinciUserDetails(final VinciUserDetails userDetails) {
        return new SQL() {{
            INSERT_INTO("account");
            VALUES("access_id, password, device_id, access_key, account_expired_time, credentials_expired_time",
                   "#{username}, #{password}, #{deviceId}, #{accessKey}, #{accountExpiredTime}, #{credentialsExpiredTime}");
            if (userDetails.isAccountNonExpired()) {
                VALUES("is_account_non_expired", "1");
            } else {
                VALUES("is_account_non_expired", "0");
            }

            if (userDetails.isCredentialsNonExpired()) {
                VALUES("is_credentials_non_expired", "1");
            } else {
                VALUES("is_credentials_non_expired", "0");
            }

            if (userDetails.isEnabled()) {
                VALUES("is_enabled", "1");
            } else {
                VALUES("is_enabled", "0");
            }
        }}.toString();
    }

    public String updateVinciUserDetails(final VinciUserDetailsBuilder builder) {
        return new SQL() {{
            UPDATE("account");

            if (builder.getUsername() != null) {
                SET("access_id = #{username}");
            }

            if (builder.getPassword() != null) {
                SET("password = #{password}");
            }

            if (builder.getDeviceId() != null) {
                SET("deviceId = #{deviceId}");
            }

            if (builder.getAccessKey() != null) {
                SET("access_key = #{accessKey}");
            }

            if (builder.getAccountExpiredTime() != null) {
                SET("account_expired_time = #{accountExpiredTime}");
            }

            if (builder.getCredentialsExpiredTime() != null) {
                SET("credentials_expired_time = #{credentialsExpiredTime}");
            }

            if (builder.isAccountNonExpired() != null) {
                if (builder.isAccountNonExpired()) {
                    SET("is_account_non_expired = 1");
                } else {
                    SET("is_account_non_expired = 0");
                }
            }

            if (builder.isCredentialsNonExpired() != null) {
                if (builder.isCredentialsNonExpired()) {
                    SET("is_credentials_non_expired = 1");
                } else {
                    SET("is_credentials_non_expired = 0");
                }
            }

            if (builder.isEnabled() != null) {
                if (builder.isEnabled()) {
                    SET("is_enabled = 1");
                } else {
                    SET("is_enabled = 0");
                }
            }

            SET("last_modify_time = now()");
            WHERE("user_id = #{userId}");
        }}.toString();
    }
}
