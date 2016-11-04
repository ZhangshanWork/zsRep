package im.vinci.server.device.persistence.providers;

import im.vinci.server.device.domain.RomOTAConfig;
import org.apache.ibatis.jdbc.SQL;

/**
 * Created by ytl on 15/12/3.
 */
public class RomOTAConfigModifySqlProvider {

    public String updateRomOTAConfig(final RomOTAConfig romOTAConfig) {
        return new SQL(){{
            UPDATE("rom_ota_config");

            if (romOTAConfig.getSysVersionName() != null) {
                SET("sys_version_name = #{sysVersionName}");
            }

            if (romOTAConfig.getDesc() != null) {
                SET("`desc` = #{desc}");
            }

            if (romOTAConfig.getDescEn() != null) {
                SET("`desc_en` = #{descEn}");
            }

            if (romOTAConfig.getStatus() != null) {
                SET("status = #{status}");
            }

            WHERE("id = #{id} and region_code=#{regionCode} and hardware_code=#{hardwareCode}");
        }}.toString();
    }

//    public String updateRomOTADeployToExpired() {}

}
