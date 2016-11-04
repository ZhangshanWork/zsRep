package im.vinci.server.device.persistence;

import im.vinci.server.device.domain.OTATestMac;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by ytl on 15/12/7.
 */
@Repository
public interface OTATestMacMapper {
    @Select("select * from ota_test_mac")
    public List<OTATestMac> getOtaTestMacs();

    @Select("select * from ota_test_mac where mac=#{mac}")
    public Long getOtaTestMacByMac(String mac);

    @Insert("insert into ota_test_mac (mac) values (#{mac})")
    public void addOtaTestMac(OTATestMac otaTestMac);

    @Update("update ota_test_mac set mac = #{mac} where id=#{id}")
    public void updateOtaTestMac(OTATestMac otaTestMac);

    @Delete("delete from ota_test_mac where id = #{id}")
    public void deleteOtaTestMac(Long id);

}
