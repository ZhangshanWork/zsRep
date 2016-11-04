package im.vinci.server.other.persistence.mobileapp;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * Created by ASUS on 2016/7/22.
 */
@Repository
public interface MobileAppPicMapper {
    @Select("select url from mobileapppic where id= #{id}")
    public String getPicUrl(int id);
}
