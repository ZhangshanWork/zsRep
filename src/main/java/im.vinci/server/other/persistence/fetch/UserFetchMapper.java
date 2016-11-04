package im.vinci.server.other.persistence.fetch;

import im.vinci.server.other.domain.profile.UserMusicProfile;
import im.vinci.server.other.domain.user.User;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * Created by henryhome on 10/2/15.
 */
@Repository
public interface UserFetchMapper {

    @Select("select * from user u, account a where u.user_id = a.id and a.device_id = #{deviceId}")
    public User getUserByDeviceId(String deviceId);

    @Select("select * from user_music_profile where user_id = #{userId}")
    public UserMusicProfile getUserMusicProfileByUserId(Integer userId);

}



