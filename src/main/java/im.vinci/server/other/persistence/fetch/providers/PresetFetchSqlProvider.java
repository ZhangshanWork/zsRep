package im.vinci.server.other.persistence.fetch.providers;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * Created by zhongzhengkai on 15/12/24.
 */
public class PresetFetchSqlProvider {

    public String listTagNameByPlaylistNameIds(Map<String, String[]> condition) {
        return new SQL() {{
            SELECT("*");
            FROM("playlistname");
            String[] playlistNameIds = condition.get("playlistNameIds");
            StringBuilder inSqlClauseBuilder = new StringBuilder();
            int lastIdx = playlistNameIds.length - 1;
            int curIdx = 0;
            for (String id : playlistNameIds) {
                if (curIdx == lastIdx) {
                    inSqlClauseBuilder.append(id);
                } else {
                    inSqlClauseBuilder.append(id + ",");
                }
                curIdx++;
            }
            WHERE("id in(" + inSqlClauseBuilder.toString() + ")");
        }}.toString();
    }

}
