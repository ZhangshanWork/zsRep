package im.vinci.server.user.persistence;

import im.vinci.server.user.domain.UserSettings;
import im.vinci.server.user.persistence.handler.UserSettingsProvider;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.UpdateProvider;
import org.springframework.stereotype.Repository;

/**
 * real_user_settings 操作
 * Created by tim@vinci on 16/8/31.
 */
@Repository
public interface UserSettingsMapper {

    @Insert("insert into real_user_settings(real_user_id) value(#{userId})")
    int insertUserSettings(@Param("userId") long userId);

    @Select("select * from real_user_settings where real_user_id = #{userId}")
    UserSettings getUserSettings(@Param("userId") long userId);

    @UpdateProvider(type = UserSettingsProvider.class, method = "updateUserSettings")
    boolean updateUserSettings(UserSettings userSettings);
}
