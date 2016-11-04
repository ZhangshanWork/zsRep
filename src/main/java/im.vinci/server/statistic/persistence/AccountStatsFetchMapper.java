package im.vinci.server.statistic.persistence;

import im.vinci.server.statistic.domain.UserAgeRangeStat;
import im.vinci.server.statistic.domain.UserGenderStat;
import im.vinci.server.statistic.persistence.providers.AccountFetchProvider;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.type.JdbcType;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by henryhome on 2/27/15.
 */
@Repository
public interface AccountStatsFetchMapper {
    
    @SelectProvider(type = AccountFetchProvider.class, method = "statMaleFemale")
    public List<UserGenderStat> statMaleFemale();

    @Select("select count(*) count,age_range from user where age_range in  (SELECT DISTINCT(age_range) from user) GROUP BY age_range")
    @Results(value = {
            @Result(property = "count", column = "count", javaType = Integer.class, jdbcType = JdbcType.BIGINT),
            @Result(property = "ageRange", column = "age_range", javaType = String.class, jdbcType = JdbcType.VARCHAR)
    })
    public List<UserAgeRangeStat> statAgeRange();


}



