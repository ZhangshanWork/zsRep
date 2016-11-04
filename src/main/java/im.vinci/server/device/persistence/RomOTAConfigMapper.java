package im.vinci.server.device.persistence;

import im.vinci.server.device.domain.OTAHardwareCode;
import im.vinci.server.device.domain.OTARegionCode;
import im.vinci.server.device.domain.RomOTAConfig;
import im.vinci.server.device.persistence.providers.RomOTAConfigModifySqlProvider;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by ytl on 15/12/4.
 */
@Repository
public interface RomOTAConfigMapper {

    @Select("select * from rom_ota_config where id=#{id}")
    public RomOTAConfig findRomOTAConfigById(Long id);

    @Select("select * from rom_ota_config where region_code=#{region_code} and hardware_code=#{hardware_code} and status=#{status}")
    public RomOTAConfig getRomOTAConfigByStatus(@Param("region_code") OTARegionCode regionCode, @Param("hardware_code") OTAHardwareCode hardwareCode, @Param("status") Integer status);

    @Select("select * from rom_ota_config where region_code=#{region_code} and hardware_code=#{hardware_code} and sys_version_name=#{sysVersionName}")
    public RomOTAConfig getDeployRomOTAConfigByVersion(@Param("region_code") OTARegionCode regionCode, @Param("hardware_code") OTAHardwareCode hardwareCode, @Param("sysVersionName") String sysVersionName);

    @Select("select * from rom_ota_config where region_code=#{region_code} and hardware_code=#{hardware_code} and status != 0")
    public List<RomOTAConfig> getAllConfigs(@Param("region_code") OTARegionCode regionCode, @Param("hardware_code") OTAHardwareCode hardwareCode);

    @Insert("insert into rom_ota_config (id, sys_version_name,region_code,hardware_code,`desc`,`desc_en`,status) values (NULL, #{sysVersionName}, #{regionCode},#{hardwareCode},#{desc}, #{descEn}, #{status})")
    @SelectKey(before = false, statement = "SELECT LAST_INSERT_ID()", keyProperty = "id", resultType = Long.class)
    Long addRomOTAConfig(RomOTAConfig romOTAConfig);

    @UpdateProvider(type=RomOTAConfigModifySqlProvider.class, method="updateRomOTAConfig")
    void updateRomOTAConfig(RomOTAConfig romOTAConfig);

    @Update("update rom_ota_config set `status`=#{toStatus} where `status`=#{fromStatus} and region_code=#{regionCode} and hardware_code=#{hardwareCode}")
    void updateRomOTAConfigStatus(@Param("regionCode") OTARegionCode regionCode, @Param("hardwareCode") OTAHardwareCode hardwareCode, @Param("fromStatus") Integer fromStatus, @Param("toStatus") Integer toStatus);

}
