package im.vinci.server.other.persistence.fetch;

import im.vinci.server.other.domain.account.VinciUserDetailsBuilder;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.JdbcType;
import org.springframework.stereotype.Repository;

/**
 * Created by henryhome on 2/27/15.
 */
@Repository
public interface AccountFetchMapper {
    
    @Select("SELECT * from account where device_id = #{deviceId}")
    @Results(value={
            @Result(property="username", column="access_id", javaType=String.class, jdbcType=JdbcType.VARCHAR)
    })
    public VinciUserDetailsBuilder getUserDetailsByDeviceId(String deviceId);

    @Select("select * from account where access_id = #{accessId}")
    @Results(value={
            @Result(property="username", column="access_id", javaType=String.class, jdbcType=JdbcType.VARCHAR)
    })
    public VinciUserDetailsBuilder getUserDetailsByAccessId(String accessId);

    @Select("select * from account where access_key = #{accessKey}")
    @Results(value={
            @Result(property="username", column="access_id", javaType=String.class, jdbcType=JdbcType.VARCHAR)
    })
    public VinciUserDetailsBuilder getUserDetailsByAccessKey(String accessKey);
}



