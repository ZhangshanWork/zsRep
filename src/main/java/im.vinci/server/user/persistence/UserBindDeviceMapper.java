package im.vinci.server.user.persistence;

import im.vinci.server.user.domain.UserBindDevice;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by tim@vinci on 16/7/21.
 */
@Repository
public interface UserBindDeviceMapper {
    @Select("select * from user_bind_device where id=#{id}")
    UserBindDevice getUserBindDeviceById(@Param("id") long id);
    @Select("select * from user_bind_device where real_user_id=#{id}")
    List<UserBindDevice> getUserBindDeviceByUserInfoId(@Param("id") long real_user_id);
    //1:headphone 2:mobile
    @Select("select * from user_bind_device where real_user_id=#{id} and device_type=#{type}")
    UserBindDevice getUserBindDeviceByRealUserIdAndType(@Param("id") long real_user_id, @Param("type") String deviceType);
    @Select("select * from user_bind_device where device_id=#{id}")
    UserBindDevice getUserBindDeviceByDeviceId(@Param("id") String deviceId);

    @Insert("insert into user_bind_device " +
            "   (real_user_id, device_id, device_type, imei, mac, phone_model, dt_create, dt_update) " +
            "values (#{realUserId},#{deviceId},#{deviceType},#{imei},#{mac},#{phoneModel},now(),now())")
    @Options(useGeneratedKeys = true, keyColumn = "id")
    int insertUserDeviceBind(UserBindDevice userBindDevice);

    @Update("update user_bind_device set device_id=#{deviceId},imei=#{imei},mac=#{mac},phone_model=#{phoneModel}, " +
            "dt_update=now() where real_user_id=#{realUserId} and device_type=#{deviceType}")
    int updateUserDeviceBind(UserBindDevice userBindDevice);

    @Delete("delete from user_bind_device where real_user_id=#{realUserId} and device_type=#{deviceType}")
    int deleteUserDeviceBind(UserBindDevice userBindDevice);
}
