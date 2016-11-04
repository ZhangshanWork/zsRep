package im.vinci.server.device.persistence;

import im.vinci.server.device.domain.CrashLog;
import im.vinci.server.device.domain.Device;
import im.vinci.server.device.persistence.providers.DeviceModifySqlProvider;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by henryhome on 9/15/15.
 */
@Repository
public interface DeviceMapper {

    @Insert("insert into crash_log (imei, mac, crash_type, crash_count, app_name, app_version, log_sign, log) " +
            "values (#{imei}, #{mac}, #{crashType}, #{crashCount}, #{appName}, #{appVersion}, #{logSign}, #{log})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    public Integer addCrashLog(CrashLog crashLog);

    @Insert("insert into device (name, mac, imei, first_update_time) values (#{name}, #{mac}, #{imei}, #{firstUpdateTime})")
    @Options(useGeneratedKeys = true)
    public void addDevice(Device device);

    @UpdateProvider(type=DeviceModifySqlProvider.class, method="updateDevice")
    public void updateDevice(Device device);

    @Delete("delete from device where id = #{deviceId}")
    public void deleteDevice(Integer deviceId);

    @Select("select * from device")
    public List<Device> getDevices();

    @Select("select * from device where mac = #{mac}")
    public Device getDeviceByMac(String mac);

    @Select("select * from device where imei = #{imei}")
    public Device getDeviceByImei(String imei);
}



