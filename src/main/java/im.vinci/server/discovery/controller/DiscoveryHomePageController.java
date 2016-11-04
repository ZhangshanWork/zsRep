package im.vinci.server.discovery.controller;

import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.discovery.domain.MusicAlbum;
import im.vinci.server.discovery.domain.MusicSong;
import im.vinci.server.discovery.domain.wrappers.DiscoveryResponse;
import im.vinci.server.discovery.service.DiscoveryHomePageService;
import im.vinci.server.security.ApiSecurityLabel;
import im.vinci.server.utils.apiresp.ResultObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 发现主页展示接口
 * Created by ASUS on 2016/8/23.
 */
@RestController
@RequestMapping(value = "/vinci/discovery", produces = "application/json;charset=UTF-8")
public class DiscoveryHomePageController {

    @Autowired
    DiscoveryHomePageService discoveryHomePageService;

    //主页显示
    @RequestMapping(value = "/homepage")
    @ApiSecurityLabel(isCheckLogin = true)
    public ResultObject<DiscoveryResponse> homePage() {

        return new ResultObject<>(discoveryHomePageService.showHomePage());
    }

    //获取歌单列表：电台到歌单列表
    @RequestMapping(value = "/albumlist" ,method = RequestMethod.POST)
    @ApiSecurityLabel(isCheckLogin = true)
    public ResultObject<List<MusicAlbum>> albumList(@RequestParam("channelId") long channelId) {

        return new ResultObject<>(discoveryHomePageService.getAlbumList(channelId));
    }

    //获取歌曲列表：歌单到歌曲
    @RequestMapping(value = "/songlist" ,method = RequestMethod.POST)
    @ApiSecurityLabel(isCheckLogin = true)
    public ResultObject<List<MusicSong>> songList(@RequestParam("albumId") long albumId) {

        return new ResultObject<>(discoveryHomePageService.getSongList(albumId));
    }

    //最新上传歌曲
    @RequestMapping(value = "/recent" )
    @ApiSecurityLabel(isCheckLogin = true)
    public ResultObject<List<MusicSong>> showRecent(@RequestParam("lastSongId")   long lastSongId,
                                                    @RequestParam("pageSize") int pageSize) {

        if(pageSize<=0 || pageSize>100){
            throw new VinciException(ErrorCode.GET_RECENT_PAGESIZE_ERROR, "page或pageSize不合法", "参数不合法");
        }

        return new ResultObject<>(discoveryHomePageService.showRecent(lastSongId, pageSize));
    }

}
