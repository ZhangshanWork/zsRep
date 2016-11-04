//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package im.vinci.server.device.persistence.providers;

import im.vinci.server.device.domain.DeviceUserLogForDB;
import org.apache.ibatis.jdbc.SQL;

public class UserLogSqlProvider {
    public UserLogSqlProvider() {
    }

    public String addUserLog(DeviceUserLogForDB logForDB) {
        return (new SQL() {
            {
                this.INSERT_INTO("user_logs");
                this.VALUES("name, uuid, create_timestamp, json_data", "#{name}, #{uuid}, #{create_timestamp}, #{json_data}");
            }
        }).toString();
    }
}
