package im.vinci.server.statistic.persistence.providers;

import org.apache.ibatis.jdbc.SQL;

/**
 * Created by zhongzhengkai on 15/12/22.
 */
public class AccountFetchProvider {

    public String statMaleFemale(){
        String sql=new SQL() {{
            SELECT("count(*) as count,gender");
            FROM("user");
            WHERE("gender in('MALE','FEMALE')");
            GROUP_BY("gender");
        }}.toString();
        return sql;
    }


}
