package im.vinci.server.user.persistence.handler;

import im.vinci.server.user.domain.UserSettings;
import org.apache.ibatis.jdbc.SQL;

/**
 * real_user_settings 操作
 * Created by tim@vinci on 16/7/21.
 */
public class UserSettingsProvider {
    public String updateUserSettings(UserSettings userSettings) {
        return new SQL() {{
            UPDATE("real_user_settings");
            if (userSettings.getCollectToShare() != null) {
                SET("collect_to_share = #{collectToShare}");
            }
            if (userSettings.getCommentToShare() != null) {
                SET("comment_to_share = #{commentToShare}");
            }
            if (userSettings.getSportsToShare() != null) {
                SET("sports_to_share = #{sportsToShare}");
            }
            SET("dt_update = now()");
            WHERE("real_user_id = "+ userSettings.getRealUserId());
        }}.toString();
    }
}
