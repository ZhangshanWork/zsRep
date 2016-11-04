package im.vinci.server.device.persistence;

import im.vinci.server.device.domain.SystemVersion;
import im.vinci.server.device.persistence.providers.SystemVersionModifySqlProvider;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.UpdateProvider;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by ytl on 15/12/3.
 */
@Repository
public interface SystemVersionMapper {
    @Insert("insert into system_version (version_name, is_forced, is_full, addr, hash, length, config_id) values(#{versionName}, #{isForced}, #{isFull}, #{addr}, #{hash}, #{length}, #{configId})")
    public Integer addSystemVersion(SystemVersion systemVersion);

    @UpdateProvider(type = SystemVersionModifySqlProvider.class, method = "updateSystemVersion")
    public void updateSystemVersion(SystemVersion systemVersion);

    @Delete("delete from system_version where id = #{id}")
    public void delSystemVersionById(Long id);

    @Select("select * from system_version where config_id = #{romOTAConfigId}")
    public List<SystemVersion> getVersionPackagesByConfigId(Long romOTAConfigId);

}
