package im.vinci.server.other.persistence.modify.providers;

import im.vinci.server.other.domain.preset.UserPresetMusic;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;
import java.util.Map;

/**
 * Created by zhongzhengkai on 15/12/25.
 */
public class UserPresetModifySqlProvider {

    public String saveUserPresetMusicBatch(Map<String, List<UserPresetMusic>> condition) {
        String sql= new SQL() {{
            INSERT_INTO("user_preset_music");
            StringBuilder valuesBuilder = new StringBuilder();
            List<UserPresetMusic> list = condition.get("list");
            int lastIdx = list.size() - 1;
            int curIdx = 0;
            for (UserPresetMusic e : list) {
                String valueStr = e.getAccountId() + ",'" + e.getDeviceId() + "'," + e.getSongId() + "," + e.getMusicId() + ",'" + e.getMusicSource()+"'";
                if (curIdx < lastIdx) {
                    valuesBuilder.append(valueStr + "),(");
                }else{
                    valuesBuilder.append(valueStr);
                }
                curIdx++;
            }
            VALUES("account_id,device_id,song_id,music_id,music_source", valuesBuilder.toString());
        }}.toString();
        return sql;
    }
}
