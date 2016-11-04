package im.vinci.server.other.services.impl.fetch;

import im.vinci.server.other.domain.account.VinciUserDetails;
import im.vinci.server.other.domain.preset.Playlistname;
import im.vinci.server.other.domain.preset.PresetMusic;
import im.vinci.server.other.domain.preset.UserPresetMusic;
import im.vinci.server.other.domain.wrappers.responses.preset.PresetUpdateInfo;
import im.vinci.server.other.persistence.fetch.PresetFetchMapper;
import im.vinci.server.other.persistence.fetch.UserPresetFetchMapper;
import im.vinci.server.other.persistence.modify.UserPresetModifyMapper;
import im.vinci.server.other.services.fetch.PresetFetchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by zhongzhengkai on 15/12/24.
 */
@Service
public class PresetFetchServiceImpl implements PresetFetchService {

    @Autowired
    PresetFetchMapper presetFetchMapper;

    @Autowired
    UserPresetFetchMapper userPresetFetchMapper;

    @Autowired
    UserPresetModifyMapper userPresetModifyMapper;

    @Override
    public Integer getLatestPresetVersion() {
        PresetMusic music = presetFetchMapper.getOnePersetMusicData();
        if (music != null) {
            return music.getVersion();
        } else {
            return null;
        }
    }

    @Override
    public Map<String,List<String>> mapTagNameByPlNameIds(String[] plNameIds) {
        List<Playlistname> list = presetFetchMapper.listTagNameByPlaylistNameIds(plNameIds);
        Map<String, List<String>> map = new HashMap<>();
        for (Playlistname e : list) {
            List<String> tags = Arrays.asList(e.getTags().split(","));
            map.put(e.getId() + "", tags);
        }
        return map;
    }

    @Override
    public List<UserPresetMusic> assemblePresetUpdateInfo(PresetUpdateInfo presetUpdateInfo, Integer version, VinciUserDetails account) {
        List<PresetMusic> list = presetFetchMapper.listPresetMusicByVersion(version);
        List<UserPresetMusic> toInsert = new ArrayList<>();
        Set<String> plNameIdsSet = new HashSet<>();
        Map<String, List<String>> plNameIdMusicsMap = new HashMap<>();
        Map<String, String> plId_plNameMap = new HashMap<>();
        for (PresetMusic e : list) {
            String plNameId = e.getPlaylistnameId() + "";
            plNameIdsSet.add(plNameId);
            List<String> tmpList = plNameIdMusicsMap.get(plNameId);
            if (tmpList == null) {
                tmpList = new ArrayList<>();
                plNameIdMusicsMap.put(plNameId, tmpList);
            }
            tmpList.add(e.getSongId() + "_" + e.getMusicSource());//这里不去重了,靠数值保证在preset_misic表里不会有重复的歌曲
            plId_plNameMap.put(plNameId, e.getPlaylistname());
            toInsert.add(new UserPresetMusic(account.getId(),account.getDeviceId(),e.getSongId(),e.getMusicId(),e.getMusicSource()));
        }

        PresetUpdateInfo.Presets presets = presetUpdateInfo.new Presets();
        presets.setVersion(version + "");
        List<PresetUpdateInfo.Category> categories = new ArrayList<>();
        presets.setCategories(categories);
        presetUpdateInfo.setPresets(presets);

        if(plNameIdsSet.size()>0){
            String[] plNameIds = plNameIdsSet.toArray(new String[]{});
            Map<String, List<String>> plNameIdTagsMap = mapTagNameByPlNameIds(plNameIds);
            Iterator it = plNameIdMusicsMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, List<String>> entry = (Map.Entry) it.next();
                PresetUpdateInfo.Category category = presetUpdateInfo.new Category();
                String plNameId = entry.getKey();
                category.setPlaylistName(plId_plNameMap.get(plNameId));
                category.setMusics(entry.getValue());
                category.setTags(plNameIdTagsMap.get(plNameId));
                categories.add(category);
            }
        }
        return toInsert;
    }

    @Override
    public List<UserPresetMusic> removeDataIfInDB(List<UserPresetMusic> dataFromDB, List<UserPresetMusic> dataFromContext) {
        Map<String,UserPresetMusic> songIdDataMap = new HashMap<>();
        List<UserPresetMusic> handledData = new ArrayList<>();
        for (UserPresetMusic e : dataFromDB) {
            songIdDataMap.put(e.getSongId()+"_"+e.getMusicSource(),e);//注意,给前端的id是带来源的,如xiami_10000
        }
        for (UserPresetMusic e : dataFromContext) {
            if (songIdDataMap.get(e.getSongId() + "_" + e.getMusicSource()) == null) {
                handledData.add(e);
            }
        }
        return handledData;
    }

}
