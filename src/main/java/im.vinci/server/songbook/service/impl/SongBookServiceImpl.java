package im.vinci.server.songbook.service.impl;

import com.google.common.base.Joiner;
import com.taobao.api.ApiException;
import com.taobao.api.domain.RecommendSong;
import im.vinci.server.recomd.service.RecomdService;
import im.vinci.server.search.domain.music.MusicSong;
import im.vinci.server.search.service.XiamiMusicSearchService;
import im.vinci.server.songbook.model.SongBook;
import im.vinci.server.songbook.persistence.SongBookDao;
import im.vinci.server.songbook.service.SongBookService;
import im.vinci.server.utils.apiresp.ResponsePageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by mlc on 2016/6/28.
 */
@Service
public class SongBookServiceImpl implements SongBookService{
    @Autowired
    SongBookDao songBookDao;

    @Autowired
    XiamiMusicSearchService xiamiMusicSearchService;
    @Autowired
    RecomdService recomdService;

    @Override
    public List<SongBook> getSongBook(SongBook songBook) {
        return songBookDao.getSongBook(songBook);
    }

    @Override
    public void doSave(List<SongBook> list) {
        for(SongBook songBook:list) {
            songBookDao.doSave(songBook);
        }
    }

    @Override
    public void doDel(SongBook songBook) {
        songBookDao.doDel(songBook);
    }

    @Override
    public List doRefreshSongRepos(String deviceId, int size) throws ApiException {
        Set<String> idsSet = new HashSet<>();
        if (size == 0 || size < 500) {
            size = 500;
        }

        ResponsePageVo<MusicSong> musicSongResponsePageVo = xiamiMusicSearchService.searchRankByType("music_all", 1, 30);
        List<MusicSong> musicSongList = musicSongResponsePageVo.getData();//排行榜歌曲资源
        //用户收藏的歌曲
        List<String> idsRecmds = doRefreshToday(deviceId, size);

        List<String> topCollects = recomdService.getTopCollections(100);

        //装载id -- 虾米排行榜
        for (MusicSong musicSong : musicSongList) {
            idsSet.add(musicSong.getSong_id()+"");
        }

        //装载id -- 用户收藏歌曲推荐结果
        for (String id : idsRecmds) {
            idsSet.add(id);
        }

        //装载id -- 平台播放top榜
        for (String id : topCollects) {
            idsSet.add(id);
        }
        List result = new ArrayList<>(idsSet);
        if (result.size() > size) {
            result = result.subList(0, size - 1);
        }
        return result;
    }

    @Override
    public List<String> doRefreshToday(String deviceId, int size) throws ApiException {
        List<String> list = new ArrayList<>();
        //用户收藏的歌曲
        List<String> idsList = recomdService.getCollectionListStr(deviceId);

        if (!CollectionUtils.isEmpty(idsList)) {
            String ids = Joiner.on(',').skipNulls().join(idsList);
            List<RecommendSong> recommendSongList = xiamiMusicSearchService.todaySongs((long) size, ids);//用户收藏歌曲推荐资源
            for (RecommendSong recommendSong : recommendSongList) {
                list.add(recommendSong.getSongId() + "");
            }
        }
        return list;
    }

    @Override
    public List doLoadSongRepos(int size) {
        Set<String> idsSet = new HashSet<>();
        if (size == 0 || size < 500) {
            size = 500;
        }
        //虾米榜
        ResponsePageVo<MusicSong> musicSongResponsePageVo = xiamiMusicSearchService.searchRankByType("music_all", 1, 30);
        List<MusicSong> musicSongList = musicSongResponsePageVo.getData();//排行榜歌曲资源
        for (MusicSong song : musicSongList) {
            idsSet.add(song.getSong_id()+"");
        }
        //新歌榜
        ResponsePageVo<MusicSong> musicSongResponsePageVo1 = xiamiMusicSearchService.searchRankByType("newmusic_all", 1, 30);
        List<MusicSong> musicSongList1 = musicSongResponsePageVo.getData();//排行榜歌曲资源
        for (MusicSong song : musicSongList1) {
            idsSet.add(song.getSong_id()+"");
        }

        //全球媒体榜
        ResponsePageVo<MusicSong> musicSongResponsePageVo2 = xiamiMusicSearchService.searchRankByType("hito", 1, 30);
        List<MusicSong> musicSongList2 = musicSongResponsePageVo.getData();//排行榜歌曲资源
        for (MusicSong song : musicSongList2) {
            idsSet.add(song.getSong_id()+"");
        }

        //全球媒体榜
        ResponsePageVo<MusicSong> musicSongResponsePageVo3 = xiamiMusicSearchService.searchRankByType("hito", 1, 30);
        List<MusicSong> musicSongList3 = musicSongResponsePageVo.getData();//排行榜歌曲资源
        for (MusicSong song : musicSongList3) {
            idsSet.add(song.getSong_id()+"");
        }
        List result = new ArrayList<>(idsSet);
        if (result.size() > size) {
            result = result.subList(0, size - 1);
        }
        return result;
    }


}
