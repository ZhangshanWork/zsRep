package im.vinci.server.device.persistence.providers;

import im.vinci.server.device.domain.SystemVersion;
import org.apache.ibatis.jdbc.SQL;

/**
 * Created by ytl on 15/12/7.
 */
public class SystemVersionModifySqlProvider {
    public String updateSystemVersion(final SystemVersion systemVersion) {
        return new SQL(){
            {
                UPDATE("system_version");
                if (systemVersion.getIsForced() != null) {
                    SET("is_forced=#{isForced}");
                }
                if (systemVersion.getIsFull() != null) {
                    SET("is_full=#{isFull}");
                }
                if (systemVersion.getAddr() != null) {
                    SET("addr=#{addr}");
                }
                if (systemVersion.getHash() != null) {
                    SET("hash=#{hash}");
                }
                if (systemVersion.getLength() != null) {
                    SET("length=#{length}");
                }
                if (systemVersion.getVersionName() != null) {
                    SET("version_name=#{versionName}");
                }
                WHERE("id=#{id}");
            }
        }.toString();
    }
}
