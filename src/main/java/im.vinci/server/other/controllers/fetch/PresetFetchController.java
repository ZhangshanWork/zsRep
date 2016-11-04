package im.vinci.server.other.controllers.fetch;

import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.other.domain.account.VinciUserDetailsBuilder;
import im.vinci.server.other.domain.preset.UserPresetMusic;
import im.vinci.server.other.domain.wrappers.requests.user.FirstMusicGet;
import im.vinci.server.other.domain.wrappers.responses.preset.PresetUpdateInfo;
import im.vinci.server.other.persistence.fetch.AccountFetchMapper;
import im.vinci.server.other.persistence.fetch.PresetFetchMapper;
import im.vinci.server.other.persistence.fetch.UserPresetFetchMapper;
import im.vinci.server.other.persistence.modify.UserPresetModifyMapper;
import im.vinci.server.other.services.fetch.PresetFetchService;
import im.vinci.server.utils.BizTemplate;
import im.vinci.server.utils.apiresp.ResultObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by ZhongZhengKai on 24/12/15.
 */
@RestController
@RequestMapping(value = "/preset_update", produces = "application/json;charset=UTF-8")
public class PresetFetchController {
    private static Logger logger = LoggerFactory.getLogger(PresetFetchController.class);

    @Autowired
    PresetFetchService service;

    @Autowired
    private AccountFetchMapper accountFetchMapper;

    @Autowired
    PresetFetchMapper presetFetchMapper;

    @Autowired
    UserPresetFetchMapper userPresetFetchMapper;

    @Autowired
    UserPresetModifyMapper userPresetModifyMapper;

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public ResultObject<PresetUpdateInfo> getRecommendedMusic(@RequestParam(value = "version", required = true) Integer version,
                                                              @RequestParam(value = "device_id", required = true) String deviceId) {
        Integer latestVersion = service.getLatestPresetVersion();
        PresetUpdateInfo updateInfo = new PresetUpdateInfo();
        if (latestVersion == null) {
            logger.warn("no preset_music configured in database,please make sure the data is ready!");
            updateInfo.setNeedUpdate(false);
            return new ResultObject<>(updateInfo);
        }

        return new BizTemplate<ResultObject<PresetUpdateInfo>>("query_preset_music") {
            @Override
            protected void checkParams() throws VinciException {
                System.out.println(version);
                System.out.println(latestVersion);
                if (version > latestVersion) {
                    throw new VinciException(ErrorCode.INVALID_PERSET_VERSION, "param version is invalid", "客户端传递的版本号大于预置曲库里的最新版本号");
                }
            }

            @Override
            protected ResultObject<PresetUpdateInfo> process() throws Exception {
                if (version.equals(latestVersion)) {
                    updateInfo.setNeedUpdate(false);
                    return new ResultObject<>(updateInfo);
                } else {
                    updateInfo.setNeedUpdate(true);
                    VinciUserDetailsBuilder builder = accountFetchMapper.getUserDetailsByDeviceId(deviceId);
                    if (builder != null) {
                        List<UserPresetMusic> dataFromContext = service.assemblePresetUpdateInfo(updateInfo, latestVersion, builder.build());

                        //vinci_hint:暂时不保存用户已更新过的预置歌曲到user_preset_music表里,因为目前来说保存它没有什么意义
//                            List<UserPresetMusic> dataFromDB = userPresetFetchMapper.listUserPresetMusicByDeviceId(deviceId);
//                            List<UserPresetMusic> toInsertBatch = service.removeDataIfInDB(dataFromDB, dataFromContext);
//                            if (toInsertBatch.size() > 0) {
//                                userPresetModifyMapper.saveUserPresetMusicBatch(toInsertBatch);
//                            }

                        return new ResultObject<>(updateInfo);
                    } else {
                        throw new VinciException(ErrorCode.ACCOUNT_NOT_FOUND, "account not found", "用户" + deviceId + " 不存在");
                    }
                }
            }
        }.execute();
    }

    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public ResultObject<PresetUpdateInfo> test(@RequestBody FirstMusicGet musicGet) {
        PresetUpdateInfo updateInfo = new PresetUpdateInfo();
        System.out.println(musicGet.getCurrentMood());
        System.out.println(musicGet.getDeviceId());
        return new ResultObject<>(updateInfo);
    }

}



