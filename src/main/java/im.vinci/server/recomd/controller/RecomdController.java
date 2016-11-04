package im.vinci.server.recomd.controller;

import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.other.domain.wrappers.requests.music.MusicSimilarity;
import im.vinci.server.recomd.domain.RecomdInput;
import im.vinci.server.recomd.service.RecomdService;
import im.vinci.server.utils.BizTemplate;
import im.vinci.server.utils.apiresp.RecommendResultList;
import im.vinci.server.utils.apiresp.ResultList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by mlc on 2016/9/21.
 */
@RestController
@RequestMapping(
        value = {"/vinci/music"},
        produces = {"application/json;charset=UTF-8"}
)
public class RecomdController {

    @Autowired
    RecomdService recomdService;

    @RequestMapping({"/get_recommend"})
    public RecommendResultList<String> getSingersResult(RecomdInput input) throws Exception {
        return new RecommendResultList<>(recomdService.getRecomdList(input), null);
    }

    @RequestMapping(value = "/similar", method = RequestMethod.GET)
    public ResultList<String> getSimilarMusic(@RequestParam("device_id") String userId,
                                              @RequestParam("music_id") String musicId,
                                              @RequestParam(value = "size", required = false) Integer size)
            throws Exception {

        return (new BizTemplate<ResultList<String>>("similar") {
            @Override
            protected void checkParams() throws VinciException {
                //userId对应的头机的imei码,形如:864765020184019,因该函数对account表有创建行为,所以严格检查userId
                if (userId == null || userId.length() != 15 || !userId.startsWith("8647")) {
                    throw new VinciException(ErrorCode.INVALID_DEVICE, "arg device_id:" + userId + " is invalid", "用户");
                }
            }

            @Override
            protected ResultList<String> process() throws Exception {
                MusicSimilarity musicSimilarity = new MusicSimilarity();
                musicSimilarity.setUserId(userId);
                musicSimilarity.setMusicId(musicId);
                musicSimilarity.setSize(size);

                List<String> musicList = recomdService.getSimilarMusic(musicSimilarity);
                return new ResultList<>(musicList);
            }

        }).execute();
    }

}
