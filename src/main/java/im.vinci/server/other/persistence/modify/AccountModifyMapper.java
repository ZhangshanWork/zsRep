package im.vinci.server.other.persistence.modify;

import im.vinci.server.other.domain.account.VinciUserDetails;
import im.vinci.server.other.persistence.modify.providers.AccountModifySqlProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.UpdateProvider;
import org.springframework.stereotype.Repository;

/**
 * Created by henryhome on 2/20/15.
 */
@Repository
public interface AccountModifyMapper {
    
    @InsertProvider(type=AccountModifySqlProvider.class, method="addVinciUserDetails")
    @Options(useGeneratedKeys = true)
    public Integer addVinciUserDetails(VinciUserDetails details);

    @UpdateProvider(type=AccountModifySqlProvider.class, method="updateVinciUserDetails")
    public Integer updateVinciUserDetails(VinciUserDetails details);
}




