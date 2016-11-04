package im.vinci.server.other.controllers.fetch;

import im.vinci.server.other.domain.wrappers.requests.user.FirstMusicGet;
import im.vinci.server.other.services.fetch.UserFetchService;
import im.vinci.server.utils.apiresp.ResultObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by henryhome on 10/17/15.
 */
@RestController
@RequestMapping(value = "/vinci/users", produces = "application/json;charset=UTF-8")
public class UserFetchController {

    @Autowired
    private UserFetchService userFetchService;

    @RequestMapping(value = "/{device_id}/get_first_music", method= RequestMethod.GET)
    public ResultObject<String> getFirstMusic(@PathVariable("device_id") String deviceId,
                                              @RequestParam("current_mood") String currentMood)
        throws Exception {

        FirstMusicGet firstMusicGet = new FirstMusicGet();
        firstMusicGet.setDeviceId(deviceId);
        firstMusicGet.setCurrentMood(currentMood);

        String firstMusic = userFetchService.getFirstMusic(firstMusicGet);

        return new ResultObject<>(firstMusic);
    }
}
