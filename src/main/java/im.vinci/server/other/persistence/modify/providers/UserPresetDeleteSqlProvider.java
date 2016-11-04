package im.vinci.server.other.persistence.modify.providers;

import org.apache.ibatis.jdbc.SQL;

import java.util.List;
import java.util.Map;

/**
 * Created by zhongzhengkai on 15/12/25.
 */
public class UserPresetDeleteSqlProvider {

    public String deleteBatchByIds(Map<String, List<Integer>> condition){
        String sql = new SQL() {{
            List<Integer> ids = condition.get("ids");
            DELETE_FROM("user_preset_music");
            StringBuilder inSqlClauseBuilder = new StringBuilder("id in (");
            int lastIdx = ids.size() - 1;
            int curIdx = 0;
            for (Integer id : ids) {
                inSqlClauseBuilder.append(id);
                if (curIdx < lastIdx) {
                    inSqlClauseBuilder.append(",");
                }else{
                    inSqlClauseBuilder.append(")");
                }
                curIdx++;
            }
            WHERE(inSqlClauseBuilder.toString());
        }}.toString();
        return sql;
    }

}
