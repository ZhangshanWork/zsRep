package im.vinci.server.other.services.modify;

import im.vinci.server.other.domain.account.VinciUserDetails;

/**
 * Created by henryhome on 2/27/15.
 */
public interface AccountModifyService {
    
    VinciUserDetails createUserDetails(String deviceId) throws Exception;
}



