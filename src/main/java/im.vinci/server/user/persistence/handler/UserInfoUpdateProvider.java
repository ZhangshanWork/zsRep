package im.vinci.server.user.persistence.handler;

import com.google.common.base.Objects;
import im.vinci.server.user.domain.UserInfo;
import org.apache.ibatis.jdbc.SQL;

/**
 * 用户UserInfo更新 Provider class
 * Created by tim@vinci on 16/7/21.
 */
public class UserInfoUpdateProvider {
    public String updateUserInfo(UserInfo oldUser, UserInfo newUser) {
        return new SQL() {{
            UPDATE("real_user_info");
            if (!Objects.equal(oldUser.getNickName(),newUser.getNickName())) {
                SET("nick_name = #{user.nickName}");
            }
            if (!Objects.equal(oldUser.getNickName(),newUser.getNickName())) {
                SET("nick_name_check = #{user.nickNameCheck}");
            }
            if (!Objects.equal(oldUser.getLocation(),newUser.getLocation())) {
                SET("location = #{user.location , typeHandler=im.vinci.server.user.persistence.handler.UserLocationHandler}");
            }
            if (oldUser.getSex() != newUser.getSex()) {
                SET("sex = #{user.sex}");
            }
            if (oldUser.getBirthDate() != newUser.getBirthDate()) {
                SET("birth_date = #{user.birthDate}");
            }
            
            SET("dt_update = now()");
            WHERE("id = "+oldUser.getId());
        }}.toString();
    }
}
