package im.vinci.server.other.persistence.modify.providers;

import im.vinci.server.other.domain.profile.UserMusicProfile;
import im.vinci.server.other.domain.user.User;
import org.apache.ibatis.jdbc.SQL;

/**
 * Created by henryhome on 10/3/15.
 */
public class UserModifySqlProvider {

    public String updateUser(final User user) {
        return new SQL() {{
            UPDATE("user");

            if (user.getAge() != null) {
                SET("age = #{age}");
            }

            if (user.getAgeRange() != null) {
                SET("age_range = #{ageRange}");
            }

            if (user.getGender() != null) {
                SET("gender = #{gender}");
            }

            SET("last_modify_time = now()");
            WHERE("user_id = #{userId}");
        }}.toString();
    }

    public String updateUserMusicProfile(final UserMusicProfile userMusicProfile) {
        return new SQL() {
            {
                UPDATE("user_music_profile");

                if (userMusicProfile.getMusicRegion() != null) {
                    SET("music_region = #{musicRegion}");
                }

                if (userMusicProfile.getMusicCategory() != null) {
                    SET("music_category = #{musicCategory}");
                }

                if (userMusicProfile.getMusicStyle() != null) {
                    SET("music_style = #{musicStyle}");
                }

                if (userMusicProfile.getFavoriteSinger() != null) {
                    SET("favorite_singer = #{favoriteSinger}");
                }

                if (userMusicProfile.getMusicTimeliness() != null) {
                    SET("music_timeliness = #{musicTimeliness}");
                }

                SET("last_modify_time = now()");
                WHERE("user_id = #{userId}");
            }}.toString();
    }
}
