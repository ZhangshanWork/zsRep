package im.vinci.server.discovery.service;

import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.discovery.domain.*;
import im.vinci.server.discovery.domain.wrappers.AlbumSongResponce;
import im.vinci.server.discovery.domain.wrappers.DiscoveryResponse;
import im.vinci.server.discovery.persistence.DiscoveryHomePageMapper;
import im.vinci.server.utils.BizTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ASUS on 2016/8/23.
 */
@Service
public class DiscoveryHomePageService {

    @Autowired
    private DiscoveryHomePageMapper discoveryHomePageMapper;


    /**
     * 主页显示
     */
    @Transactional
    public DiscoveryResponse showHomePage(){
        return new BizTemplate<DiscoveryResponse>("discovery.homepage"){

            @Override
            protected void checkParams() throws VinciException {

            }

            @Override
            protected DiscoveryResponse process() throws VinciException{
                DiscoveryResponse discoveryResponse = new DiscoveryResponse();
                try {
                    List<ChannelAlbum> channelAlbumList = new ArrayList<ChannelAlbum>();

                    List<Channel> channelList = new ArrayList<Channel>();

                    channelList = discoveryHomePageMapper.getChannelList();

                    for(Channel channelTemp : channelList){
                        List<Long> albumIdList = discoveryHomePageMapper.getAlbumIdList(channelTemp.getChannelId());
                        ChannelAlbum channelAlbumTemp = new ChannelAlbum();
                        channelAlbumTemp.setChannelId(channelTemp.getChannelId());
                        channelAlbumTemp.setChannelName(channelTemp.getChannelName());
                        channelAlbumTemp.setChannelImg(channelTemp.getChannelImg());
                        channelAlbumTemp.setAlbumList(discoveryHomePageMapper.getMusicAlbumList(albumIdList));
                        channelAlbumList.add(channelAlbumTemp);
                    }

                    discoveryResponse.setChannelAlbumList(channelAlbumList);



                    List<CategoryAlbumSong> categoryAlbumSongList = new ArrayList<CategoryAlbumSong>();

                    List<Category> categoryList = new ArrayList<Category>();

                    categoryList = discoveryHomePageMapper.getCategoryList();

                    for(Category categoryTemp : categoryList){

                        List<AlbumSongList> albumSongListList = new ArrayList<AlbumSongList>();


                        List<AlbumSongResponce> albumSongResponceList = discoveryHomePageMapper.getAlbumSongListList(categoryTemp.getCategoryId());
                        for(AlbumSongResponce albumSongResponceTemp : albumSongResponceList){
                            AlbumSongList albumSongListTemp = new AlbumSongList();
                            albumSongListTemp.setAlbumSongListId(albumSongResponceTemp.getAlbumSongListId());
                            albumSongListTemp.setAlbumSongListName(albumSongResponceTemp.getAlbumSongListName());
                            albumSongListTemp.setType(albumSongResponceTemp.getType());
                            List<Long> albumSongIdList = discoveryHomePageMapper.getAlbumSongList(albumSongResponceTemp.getAlbumSongListId());
                            if(albumSongListTemp.getType().equals("album")){
                                List<MusicAlbum> musicAlbumList = discoveryHomePageMapper.getMusicAlbumList(albumSongIdList);
                                albumSongListTemp.setAlbumSongList(musicAlbumList);
                            }else if(albumSongListTemp.getType().equals("song")){
                                List<MusicSong> musicSongList = discoveryHomePageMapper.getMusicSongList(albumSongIdList);
                                albumSongListTemp.setAlbumSongList(musicSongList);
                            }

                            albumSongListList.add(albumSongListTemp);
                        }

                        CategoryAlbumSong categoryAlbumSongTemp = new CategoryAlbumSong();

                        categoryAlbumSongTemp.setCategoryName(categoryTemp.getCategoryName());
                        categoryAlbumSongTemp.setAlbumSongListList(albumSongListList);
                        categoryAlbumSongList.add(categoryAlbumSongTemp);
                    }

                    discoveryResponse.setCategoryAlbumSongList(categoryAlbumSongList);



                } catch (Exception e) {
                    throw new VinciException(ErrorCode.GET_HOME_PAGE_ERROR, "内部错误，操作失败", "未知错误,请重试");
                }
                return discoveryResponse;

            }

        }.execute();
    }


    /**
     * 获取歌单列表：电台到歌单
     */
    @Transactional
    public List<MusicAlbum> getAlbumList(final long channelId){
        return new BizTemplate<List<MusicAlbum>>("discovery.getalbumlist"){
            @Override
            protected void checkParams() throws VinciException{

            }

            @Override
            protected List<MusicAlbum> process() throws VinciException{

                try{

                    return discoveryHomePageMapper.getMusicAlbumList(discoveryHomePageMapper.getChannelAlbumList(channelId));

                }catch (Exception e) {

                    throw new VinciException(ErrorCode.GET_ALBUM_LIST_ERROR, "获取歌单列表错误", "未知错误,请重试");

                }

            }
        }.execute();

    }

    /**
     * 获取歌曲列表：歌单到歌曲
     */
    @Transactional
    public List<MusicSong> getSongList(final long albumId){
        return new BizTemplate<List<MusicSong>>("discovery.getsonglist"){
            @Override
            protected void checkParams() throws VinciException {

            }

            @Override
            protected List<MusicSong> process() throws VinciException{

                try{

                    return discoveryHomePageMapper.getSongList(albumId);

                }catch (Exception e) {

                    throw new VinciException(ErrorCode.GET_SONG_LIST_ERROR, "获取歌曲列表错误", "未知错误,请重试");

                }

            }

        }.execute();

    }


    /**
    * 获取歌曲列表：歌单到歌曲
    */
    @Transactional
    public List<MusicSong> showRecent(final long lastSongId, final int pageSize){
        return new BizTemplate<List<MusicSong>>("discovery.showrecent"){
            @Override
            protected void checkParams() throws VinciException{

                if(pageSize<=0 || pageSize>100){
                    throw new VinciException(ErrorCode.GET_RECENT_PAGESIZE_ERROR, "page或pageSize不合法", "参数不合法");
                }

            }

            @Override
            protected List<MusicSong> process() throws VinciException{

                try{

                    if(lastSongId == 0){
                        return discoveryHomePageMapper.getFirstRecent(pageSize);
                    }else{
                        long lastId = discoveryHomePageMapper.getIdBySongId(lastSongId);
                        return discoveryHomePageMapper.getRecent(lastId,pageSize);
                    }

                }catch (Exception e) {

                    throw new VinciException(ErrorCode.GET_RECENT_LIST_ERROR, "获取最新歌曲列表错误", "未知错误,请重试");

                }

            }
        }.execute();
    }



}
