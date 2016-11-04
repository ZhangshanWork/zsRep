package im.vinci.server.other.controllers.modify;

import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.other.domain.wrappers.requests.profile.UserMusicProfileGeneration;
import im.vinci.server.other.domain.wrappers.requests.user.UserInfoGeneration;
import im.vinci.server.other.services.fetch.UserFetchService;
import im.vinci.server.other.services.modify.UserModifyService;
import im.vinci.server.utils.BizTemplate;
import im.vinci.server.utils.apiresp.Result;
import im.vinci.server.utils.apiresp.ResultList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Created by henryhome on 3/26/15.
 */
@RestController
@RequestMapping(value = "/vinci/users", produces = "application/json;charset=UTF-8")
public class UserModifyController {

    @Autowired
    UserModifyService userModifyService;

    @Autowired
    private UserFetchService userFetchService;

    @RequestMapping(value = "/gen_info", method = RequestMethod.POST)
    public Result addUserInfo(Principal principal, @RequestBody UserInfoGeneration userInfoGeneration)
            throws Exception {
        Integer userId = Integer.parseInt(principal.getName());
        userInfoGeneration.setUserId(userId);
        userModifyService.addUserInfo(userInfoGeneration);

        return new Result();
    }

    @RequestMapping(value = "/{device_id}/gen_music_profile", method = RequestMethod.POST)
    public Result addUserMusicProfile(@PathVariable("device_id") String deviceId,
                                      @RequestBody UserMusicProfileGeneration userMusicProfileGeneration)
            throws Exception {
        ResultList<String> result;

        return (new BizTemplate<ResultList<String>>("gen_music_profile") {
            @Override
            protected void checkParams() throws VinciException {
                //deviceId对应的头机的imei码,形如:864765020184019,因该函数对account表有创建行为,所以严格检查deviceId
                if (deviceId == null || deviceId.length() != 15 || !deviceId.startsWith("8647")) {
                    throw new VinciException(ErrorCode.INVALID_DEVICE, "arg device_id:" + deviceId + " is invalid", "用户");
                }
            }

            @Override
            protected ResultList<String> process() throws Exception {
                userMusicProfileGeneration.setDeviceId(deviceId);
                userModifyService.addUserMusicProfile(userMusicProfileGeneration);

                String singer = userMusicProfileGeneration.getFavoriteSinger();
                //目前的策略是50首歌里选3首返回给前端
                return new ResultList<>(userFetchService.ranMusicsInTopRankList(singer, 50, 3));
            }

        }).execute();
    }
}



