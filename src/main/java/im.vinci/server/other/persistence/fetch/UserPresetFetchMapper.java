package im.vinci.server.other.persistence.fetch;

import im.vinci.server.other.domain.preset.UserPresetMusic;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by zhongzhengkai on 15/12/25.
 */
@Repository
public interface UserPresetFetchMapper {

    @Select("select * from user_preset_music where device_id=#{deviceId}")
    List<UserPresetMusic> listUserPresetMusicByDeviceId(String deviceId);

}
