package im.vinci.server.other.persistence.modify;

import im.vinci.server.other.domain.profile.UserMusicProfile;
import im.vinci.server.other.domain.user.User;
import im.vinci.server.other.persistence.modify.providers.UserModifySqlProvider;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.UpdateProvider;
import org.springframework.stereotype.Repository;

/**
 * Created by henryhome on 3/11/15.
 */
@Repository
public interface UserModifyMapper {

    @Insert("insert into user (user_id, age, age_range, gender) values (#{userId}, #{age}, #{ageRange}, #{gender})")
    @Options(useGeneratedKeys = true)
    public Integer addUser(User user);

    @UpdateProvider(type=UserModifySqlProvider.class, method="updateUser")
    public Integer updateUser(User user);

    @Insert("insert into user_music_profile (user_id, music_region, music_category, music_style, favorite_singer, " +
            "music_timeliness) values (#{userId}, #{musicRegion}, #{musicCategory}, #{musicStyle}, " +
            "#{favoriteSinger}, #{musicTimeliness})")
    @Options(useGeneratedKeys = true)
    public Integer addUserMusicProfile(UserMusicProfile userMusicProfile);

    @UpdateProvider(type=UserModifySqlProvider.class, method="updateUserMusicProfile")
    public Integer updateUserMusicProfile(UserMusicProfile userMusicProfile);
}




