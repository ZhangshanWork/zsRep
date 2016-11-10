package im.vinci.server.search.controller;

import com.google.common.collect.Lists;
import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.search.domain.music.MusicAlbum;
import im.vinci.server.search.domain.music.MusicSong;
import im.vinci.server.search.domain.music.MusicUserTags;
import im.vinci.server.search.service.XiamiMusicSearchService;
import im.vinci.server.utils.apiresp.APIResponse;
import im.vinci.server.utils.apiresp.ResponsePageVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * Created by tim@vinci on 15/11/26.
 * 歌曲搜索接口
 */
@RestController
@RequestMapping(
        value = {"","/vinci/music"},
        produces = "application/json;charset=UTF-8"
)
public class MusicSearchController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private XiamiMusicSearchService musicSearch;

    @RequestMapping(value = "/song/{song_id}", method = RequestMethod.GET)
    public APIResponse<MusicSong> searchMusicSong(@PathVariable("song_id") long songId)
            throws Exception {
        MusicSong song = musicSearch.getSongDetailById(songId);
        if (song != null) {
            try {
                List<MusicUserTags> tags = musicSearch.getTagTags("song",songId);
                if (tags != null) {
                    song.setTags(Lists.newArrayList());
                    song.setTagCounts(Lists.newArrayList());
                    for (MusicUserTags t : tags) {
                        if (t != null && StringUtils.hasText(t.getTagName())) {
                            song.getTags().add(t.getTagName());
                            song.getTagCounts().add(t.getCount());
                        }
                    }
                }
            }catch (Exception e) {
                logger.warn("获取音乐tag({})出错:{}",songId,e.toString());
            }
        }
        return APIResponse.returnSuccess(song);
    }

    @RequestMapping(value = "/album/{album_id}", method = RequestMethod.GET)
    public APIResponse<MusicAlbum> searchMusicAlbum(@PathVariable("album_id") long albumId)
            throws Exception {
        return APIResponse.returnSuccess(musicSearch.getAlbumDetailById(albumId));
    }


    @RequestMapping(value = "/song/search/{keyword}", method = RequestMethod.GET)
    public APIResponse<ResponsePageVo<MusicSong>> searchMusicSongByKeyword(@PathVariable("keyword") String keyword,
                                                                           @RequestParam(value = "page",defaultValue = "1") int page,
                                                                           @RequestParam(value = "page_size",defaultValue = "10") int pageSize)
            throws Exception {
        return APIResponse.returnSuccess(musicSearch.searchSongsByKeyword(keyword,page,pageSize));
    }

    @RequestMapping(value = "/album/search/{keyword}", method = RequestMethod.GET)
    public APIResponse<ResponsePageVo<MusicAlbum>> searchMusicAlbumByKeyword(@PathVariable("keyword") String keyword,
                                                                             @RequestParam(value = "page",defaultValue = "1") int page,
                                                                             @RequestParam(value = "page_size",defaultValue = "10") int pageSize)
            throws Exception {
        return APIResponse.returnSuccess(musicSearch.searchAlbumsByKeyword(keyword, page, pageSize));
    }

    @RequestMapping(value = "/search/tags", method = RequestMethod.GET)
    public APIResponse<List<MusicUserTags>> getTagTags(@RequestParam("type") String type,
                                                       @RequestParam("id") long id)
            throws Exception {
        return APIResponse.returnSuccess(musicSearch.getTagTags(type, id));
    }

    @ExceptionHandler(value = VinciException.class)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public APIResponse handleDeviceExceptions(VinciException ex) {
        return APIResponse.returnFail(ex.getErrorCode(),ex.getErrorMsgToUser());
    }
}
