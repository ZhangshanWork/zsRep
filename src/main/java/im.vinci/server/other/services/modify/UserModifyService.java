package im.vinci.server.other.services.modify;

import im.vinci.server.other.domain.wrappers.requests.profile.UserMusicProfileGeneration;
import im.vinci.server.other.domain.wrappers.requests.user.UserInfoGeneration;

/**
 * Created by henryhome on 9/11/15.
 */
public interface UserModifyService {

    public void addUserInfo(UserInfoGeneration userInfoGeneration) throws Exception;
    public void addUserMusicProfile(UserMusicProfileGeneration userMusicProfileGeneration) throws Exception;
}



