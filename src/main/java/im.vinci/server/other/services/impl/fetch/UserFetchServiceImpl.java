package im.vinci.server.other.services.impl.fetch;

import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.elasticsearch.impl.XiamiMusicAccessor;
import im.vinci.server.naturelang.service.NatureLangService;
import im.vinci.server.other.domain.account.VinciUserDetails;
import im.vinci.server.other.domain.profile.UserMusicProfile;
import im.vinci.server.other.domain.wrappers.requests.user.FirstMusicGet;
import im.vinci.server.other.persistence.fetch.AccountFetchMapper;
import im.vinci.server.other.persistence.fetch.UserFetchMapper;
import im.vinci.server.other.services.fetch.UserFetchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class UserFetchServiceImpl implements UserFetchService {

    @Autowired
    private UserFetchMapper userFetchMapper;
    @Autowired
    private NatureLangService natureLangSerivce;
    @Autowired
    AccountFetchMapper accountFetchMapper;
    @Autowired
    XiamiMusicAccessor xiamiMusicAccessor;

    @Override
    public String getFirstMusic(FirstMusicGet firstMusicGet) throws Exception {
        String deviceId = firstMusicGet.getDeviceId();
//        String currentMood = firstMusicGet.getCurrentMood();
//
//        // Compare current mood with music tags to fetch music list
//        List<Integer> musicIdList = musicFetchMapper.getMusicIdListByCurrentMood(currentMood);
//
//        // If no music is found, use default music list
//        if (musicIdList == null || musicIdList.isEmpty()) {
//            musicIdList = musicFetchMapper.getDefaultMusicIdList();
//        }
//
//        int length = musicIdList.size();
//        Random random = new Random();
//        int index = random.nextInt(length);
//        String result = musicIdList.get(index) + "_xiami";
//
//        return result;

        VinciUserDetails details = accountFetchMapper.getUserDetailsByDeviceId(deviceId).build();
        if (details == null) {
            throw new VinciException(ErrorCode.ACCOUNT_NOT_FOUND, "account not found", "用户device_id:" + deviceId + " 不存在");
        }
        int userId = details.getId();
        UserMusicProfile profile = userFetchMapper.getUserMusicProfileByUserId(userId);
        String favSinger = profile.getFavoriteSinger();

        //没有喜欢的歌手,则直接返回播放次数最多歌曲id
        if (favSinger == null || favSinger.length() == 0) {
            return xiamiMusicAccessor.getMaxPlayCountMusicId();
        }

        List<String> singerList = natureLangSerivce.getFilteredSingersResult(favSinger);
        //nlp没有从用户的语言文本里解析出歌手名单,则直接返回播放次数最多歌曲id
        if (singerList.size() == 0) {
            return xiamiMusicAccessor.getMaxPlayCountMusicId();
        }

        return xiamiMusicAccessor.getMaxPlayCountMusicIdByArtist(singerList.get(0));
    }

    /**
     * 在某个artistName歌手播放次数最多的前topN首歌里随机randomNumber首出来
     * @param artistName
     * @param topN
     * @param randomCount
     * @return
     * @throws Exception
     */
    @Override
    public List<String> ranMusicsInTopRankList(String artistName, int topN, int randomCount) throws Exception {
        List<String> songIds ;
        if(artistName==null || artistName.length()==0){
            songIds = xiamiMusicAccessor.getTopNMaxPlayCountMusicIds(topN);
        }else{
            List<String> singerList = natureLangSerivce.getFilteredSingersResult(artistName);
            if (singerList.size() == 0) {
                songIds = xiamiMusicAccessor.getTopNMaxPlayCountMusicIds(topN);
            }else{
                songIds = xiamiMusicAccessor.getTopNMaxPlayCountMusicIdsByArtist(singerList.get(0),topN);
            }
        }

        if (songIds.size() <= randomCount) {
            return songIds;
        } else {
            topN = songIds.size();//实际得到的个可能没有topN首,安全起见,这里重新赋值一次
            List<String> selectedSongIds = new ArrayList<>();
            HashSet<Integer> selectedNumber = new HashSet<>();
            while (randomCount > 0) {
                int tmpNumber = (int) Math.floor(topN * Math.random());
                if (!selectedNumber.contains(tmpNumber)) {
                    selectedNumber.add(tmpNumber);
                    selectedSongIds.add(songIds.get(tmpNumber));
                    randomCount--;
                }
            }
            return selectedSongIds;
        }
    }
}



