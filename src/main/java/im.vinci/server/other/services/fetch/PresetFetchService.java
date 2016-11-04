package im.vinci.server.other.services.fetch;

import im.vinci.server.other.domain.account.VinciUserDetails;
import im.vinci.server.other.domain.preset.UserPresetMusic;
import im.vinci.server.other.domain.wrappers.responses.preset.PresetUpdateInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by zhongzhengkai on 15/12/24.
 */
public interface PresetFetchService {

    Integer getLatestPresetVersion();

    Map<String,List<String>> mapTagNameByPlNameIds(String[] plNameIds);

    List<UserPresetMusic> removeDataIfInDB(List<UserPresetMusic> dataFromDB, List<UserPresetMusic> dataFromContext);

    List<UserPresetMusic> assemblePresetUpdateInfo(PresetUpdateInfo presetUpdateInfo, Integer version, VinciUserDetails account);

}
