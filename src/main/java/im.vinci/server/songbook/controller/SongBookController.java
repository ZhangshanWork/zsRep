package im.vinci.server.songbook.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taobao.api.ApiException;
import im.vinci.server.songbook.model.SongBook;
import im.vinci.server.songbook.service.SongBookService;
import im.vinci.server.utils.apiresp.ResultObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by mlc on 2016/6/28.
 */
@RestController
@RequestMapping(
        value = {"/vinci/songbook"},
        produces = {"application/json;charset=UTF-8"}
)
public class SongBookController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    SongBookService songBookService;
    /**
     * @apiNote 提交曲库接口
     *
     * */
    @RequestMapping({"/submitSongBooks"})
    public ResultObject submitSongBooks(List<SongBook> songBookList) throws Exception {
        songBookService.doSave(songBookList);
        return new ResultObject();
    }
    /**
     * @apiNote 删除歌曲
     *
     * */
    @RequestMapping({"/doDelSong"})
    public ResultObject doDelSong(SongBook songBook) {
        songBookService.doDel(songBook);
        return new ResultObject();
    }
    /**
     * @apiNote 获取当前所有歌曲
     *
     * */
    @RequestMapping({"/doGetSongs"})
    public ResultObject<List<SongBook>> doGetSongs(HttpServletRequest request, @RequestBody JSONObject json) {
        SongBook songBook  = JSON.parseObject(json.toJSONString(), SongBook.class);
        System.out.println(json);
        List<SongBook> songBookList = songBookService.getSongBook(songBook);
        return new ResultObject<List<SongBook>>(songBookList);
    }

    /**
     * @apiNote 获取新曲库
     * 虾米top榜（50%）+ 平台播放量最高的歌曲（10%）+用户收藏歌曲的推荐见歌曲（40%）
     */
    @RequestMapping({"/doRefreshSongRepos"})
    public ResultObject doRefreshSongRepos(String deviceId,int size) throws ApiException {
        return new ResultObject(songBookService.doRefreshSongRepos(deviceId,size));
    }

    /**
     * @apiNote 用户新曲库
     */
    @RequestMapping({"/doRefreshToday"})
    public ResultObject doRefreshToday(String deviceId,int size) throws ApiException {
        return new ResultObject(songBookService.doRefreshToday(deviceId,size));
    }

    /**
     * @apiNote 设置统一预置曲库
     */
    @RequestMapping({"/doLoadSongRepos"})
    public ResultObject doLoadSongRepos(int size) throws ApiException {
        return new ResultObject(songBookService.doLoadSongRepos(size));
    }

}
