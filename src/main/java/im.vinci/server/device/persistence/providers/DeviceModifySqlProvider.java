package im.vinci.server.device.persistence.providers;

import im.vinci.server.device.domain.Device;
import org.apache.ibatis.jdbc.SQL;

/**
 * Created by henryhome on 11/12/15.
 */
public class DeviceModifySqlProvider {

    public String updateDevice(final Device device) {
        return new SQL() {{
            UPDATE("device");

            if (device.getName() != null) {
                SET("name = #{name}");
            }

            if (device.getMac() != null) {
                SET("mac = #{mac}");
            }

            if (device.getImei() != null) {
                SET("imei = #{imei}");
            }

            if (device.getFirstUpdateTime() != null) {
                SET("first_update_time = #{firstUpdateTime}");
            }

            SET("last_modify_time = now()");
            WHERE("id = #{id}");
        }}.toString();
    }
}
