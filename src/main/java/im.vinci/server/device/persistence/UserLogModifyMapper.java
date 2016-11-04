/**
 * Created by zhongzhengkai on 15/12/3.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package im.vinci.server.device.persistence;

import im.vinci.server.device.domain.DeviceUserLogForDB;
import im.vinci.server.device.persistence.providers.UserLogSqlProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLogModifyMapper {
    @InsertProvider(
            type = UserLogSqlProvider.class,
            method = "addUserLog"
    )
    @Options(
            useGeneratedKeys = true
    )
    Long addUserLog(DeviceUserLogForDB logForDB);
}
