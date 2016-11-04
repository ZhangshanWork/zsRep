package im.vinci.server.user.persistence;

import im.vinci.server.user.domain.UserInfo;
import im.vinci.server.user.domain.UserLocation;
import im.vinci.server.user.persistence.handler.UserInfoUpdateProvider;
import im.vinci.server.user.persistence.handler.UserLocationHandler;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;

/**
 * real_user表和user_bind_device的操作
 * 这个真的是登录表，每个用户第一次注册登录后，都会在这个表中insert一条record
 * 目前所有的登录用户，都需要通过微信来登录，如果之后有需要其他方式登录，再通过扩展这个表来实现。
 * Created by tim@vinci on 16/6/20.
 */
@Repository
public interface RealUserAndLoginMapper {

    @Select("select count(nick_name_check) from real_user_info where nick_name_check=#{nick_name_check}")
    int checkDuplicate(@Param("nick_name_check") String nick_name_check);

    @Select("select * from real_user_info where id=#{id}")
    @Results({
            @Result(column = "location", property = "location", javaType = UserLocation.class, jdbcType = JdbcType.VARCHAR, typeHandler = UserLocationHandler.class)
    })
    UserInfo getUserInfoById(@Param("id") long id);

    @Select({"<script>",
            "select * from real_user_info where id in ",
            "<foreach item='item' index='index' collection='userIdList' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"})
    @Results({
            @Result(column = "location", property = "location", javaType = UserLocation.class, jdbcType = JdbcType.VARCHAR, typeHandler = UserLocationHandler.class)
    })
    @MapKey("id")
    Map<Long,UserInfo> getUserInfoListById(@Param("userIdList") Collection<Long> userIdList);


    @Select("select * from real_user_info where login_source=#{sourceType} and external_source_uid=#{sourceId}")
    @Results({
            @Result(column = "location", property = "location", javaType = UserLocation.class, jdbcType = JdbcType.VARCHAR, typeHandler = UserLocationHandler.class)
    })
    UserInfo getUserInfoByExternalId(@Param("sourceType") String type, @Param("sourceId") String sourceId);

    @Insert("insert into real_user_info " +
            "(login_source, external_source_uid, nick_name,nick_name_check, location, sex, birth_date, head_img,password,dt_create,dt_update) "+
            "values " +
            "(#{loginSource}, #{externalSourceUid}, #{nickName},#{nickNameCheck}" +
            ", #{location, typeHandler=im.vinci.server.user.persistence.handler.UserLocationHandler}" +
            ", #{sex}, #{birthDate}, #{headImg},#{password},now(),now())")
    @Options(useGeneratedKeys = true, keyColumn = "id")
    int insertUserInfo(UserInfo userInfo);

    @UpdateProvider(type= UserInfoUpdateProvider.class, method = "updateUserInfo")
    int updateUserInfo(UserInfo oldUser, @Param("user") UserInfo newUser);

    @Update("update real_user_info set password = #{password}, dt_update=now() where id=#{id}")
    int updateUserPassword(@Param("id") long id, @Param("password") String password);

    @Update("update real_user_info set head_img = #{url}  where id=#{id}")
    int uploadHeadImg(@Param("id") long id, @Param("url") String url);
}