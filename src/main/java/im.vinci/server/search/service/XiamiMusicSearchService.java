package im.vinci.server.search.service;

import com.google.common.collect.Lists;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.domain.*;
import com.taobao.api.request.*;
import com.taobao.api.response.*;
import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.naturelang.domain.MusicSemantic;
import im.vinci.server.naturelang.listener.Context;
import im.vinci.server.search.domain.music.MusicAlbum;
import im.vinci.server.search.domain.music.MusicSong;
import im.vinci.server.search.domain.music.MusicUserTags;
import im.vinci.server.utils.BizTemplate;
import im.vinci.server.utils.apiresp.ResponsePageVo;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

//import org.junit.After;
//import org.junit.Before;

/**
 * Created by tim@vinci on 15/11/26.
 * 虾米搜索
 */
@Service
public class XiamiMusicSearchService implements InitializingBean {

    private Client client_new;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private DefaultTaobaoClient client = new DefaultTaobaoClient("http://gw.api.taobao.com/router/rest"
            , "23064829", "29ed3de5990627239d0fdbddd3e94b51", "json", 2000, 5000);

    @PostConstruct
    public void initESClient() {
        try {
            Settings settings = Settings.settingsBuilder()
                    .put("cluster.name", "vinci").build();// cluster.name在elasticsearch.yml
            client_new = TransportClient.builder().settings(settings).build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("101.200.159.42"),9300));
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println(new XiamiMusicSearchService().searchSongsByKeyword("刘德华 冰雨",1,10));
    }
    /*
        Search function
    */

    public SearchResponse search(MusicSemantic semantic, int pageNumber, int pageSize){
        SearchResponse response = new SearchResponse();
        SearchRequestBuilder searchRequestBuilder = client_new.prepareSearch("xiami_music_merge");
        searchRequestBuilder.setTypes("musicv1");

        int page_num = pageNumber;
        int page_size = pageSize;
        searchRequestBuilder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);

        if (semantic.getSong()!=null) {

            searchRequestBuilder.setQuery(QueryBuilders.termQuery("song_name.origin", semantic.getSong().toLowerCase()));
            searchRequestBuilder.setFrom(0);
            searchRequestBuilder.setSize(page_size);  //To make sure that all the related results will be returned
            searchRequestBuilder.setExplain(true);
            response = searchRequestBuilder.execute().actionGet();

            if (response.getHits().hits().length != 0) {
                return (response);
            }
        }

        if (semantic.getArtist()!=null) {
            searchRequestBuilder.setQuery(QueryBuilders.termQuery("singer_name.origin", semantic.getArtist().toLowerCase()));
            searchRequestBuilder.setFrom(0);
            searchRequestBuilder.setSize(page_size);  //To make sure that all the related results will be returned
            searchRequestBuilder.setExplain(true);
            response = searchRequestBuilder.execute().actionGet();

            if (response.getHits().hits().length == 0) {
                searchRequestBuilder.setQuery(QueryBuilders.termQuery("singer_eng_name.origin", semantic.getArtist().toLowerCase()));
                searchRequestBuilder.setFrom(0);
                searchRequestBuilder.setSize(page_size);  //To make sure that all the related results will be returned
                searchRequestBuilder.setExplain(true);
                response = searchRequestBuilder.execute().actionGet();

                if (response.getHits().hits().length != 0) {
                    return (response);
                }
            }
            else {
                return (response);
            }
        }

        if (semantic.getAlbum()!=null){
            searchRequestBuilder.setQuery(QueryBuilders.termQuery("album_name.origin", semantic.getAlbum().toLowerCase()));
            searchRequestBuilder.setFrom(0);
            searchRequestBuilder.setSize(page_size);  //To make sure that all the related results will be returned
            searchRequestBuilder.setExplain(true);
            response = searchRequestBuilder.execute().actionGet();
        }
        return (response);
    }

    public MusicSong getSongDetailById(final long id) {
        return new BizTemplate<MusicSong>("Search_GetSongDetailById") {

            @Override
            protected void checkParams() throws VinciException {
            }

            @Override
            protected MusicSong process() throws Exception {
                //加入本地oss判定
                if((id+"").contains("101016")){
                    MusicSong song = Context.getSongByIdInOss(id);
                    return song;
                    }
                AlibabaXiamiApiSongDetailGetRequest request = new AlibabaXiamiApiSongDetailGetRequest();
                request.setId(id);
                AlibabaXiamiApiSongDetailGetResponse response = client.execute(request);
                if (response.isSuccess() && response.getBody() != null) {
                    logger.debug("搜索虾米歌曲({}):{}",id,response.getBody());
                    MusicSong song = null;
                    if (response.getData() != null && response.getData().getSong() != null) {
                        song = parse2MusicSong(response.getData().getSong());
                    }
                    if (song == null) {
                        throw new VinciException(ErrorCode.MUSIC_NOT_EXIST,"get music detail("+id+") from xiami is not exist","没有找到歌曲");
                    }
                    return song;
                }
                throw new VinciException(ErrorCode.INTERNAL_SERVER_ERROR,"search xiami timeout","搜索歌曲超时");
            }
        }.execute();
    }

    public MusicAlbum getAlbumDetailById(final long id) {
        return new BizTemplate<MusicAlbum>("Search_GetAlbumDetailById") {

            @Override
            protected void checkParams() throws VinciException {
            }

            @Override
            protected MusicAlbum process() throws Exception {
                AlibabaXiamiApiAlbumDetailGetRequest request = new AlibabaXiamiApiAlbumDetailGetRequest();
                request.setId(id);
                request.setFullDes(true);
                AlibabaXiamiApiAlbumDetailGetResponse response = client.execute(request);
                if (response.isSuccess() && response.getBody() != null) {
                    logger.debug("搜索虾米专辑({}):{}",id,response.getBody());
                    MusicAlbum album = null;
                    if (response.getData() != null) {
                        album = parse2Album(response.getData());
                    }
                    if (album == null) {
                        throw new VinciException(ErrorCode.MUSIC_NOT_EXIST,"get album detail("+id+") from xiami is not exist","没有找到专辑");
                    }
                    return album;
                }
                throw new VinciException(ErrorCode.INTERNAL_SERVER_ERROR,"search xiami timeout","搜索专辑超时");
            }
        }.execute();

    }

    /*
        Set attributes of Albums
     */
    public List<StandardAlbum> AlbumSetting(SearchResponse response) {
        SearchResponse response_album = response;
        SearchHits searchHits = response_album.getHits();
        SearchHit[] hits = searchHits.getHits();

        int count=0;

        List<StandardAlbum> Album_list = new ArrayList<StandardAlbum>();

        for (int i = 0; i < hits.length; i++) {
            SearchHit hit = hits[i];
            StandardAlbum album = new StandardAlbum();
            album.setAlbumId(Long.parseLong(hit.getSource().get("album_id").toString()));
            album.setAlbumName(hit.getSource().get("album_name").toString());
            album.setArtistId(Long.parseLong(hit.getSource().get("singer_id").toString()));
            //album.setSinger(hit.getSource().get("singer_name").toString());
            album.setArtistName(hit.getSource().get("singer_name").toString());
            album.setSongCount((long) (hits.length));
            album.setDescription(hit.getSource().get("album_des").toString());
            Album_list.add(i, album);

        }
        return Album_list;

    }

    /*
        Set attributes of songs
     */

    public SearchSongsDataSongs SongSetting(SearchResponse response) throws Exception {

        SearchResponse response_new = response;
        SearchHits searchHits = response_new.getHits();
        SearchHit[] hits = searchHits.getHits();
        int count=0;

        SearchSongsDataSongs Songs_new = new SearchSongsDataSongs();

        List<SearchSongsDataSongsData> song_list = new ArrayList<SearchSongsDataSongsData>();

        for (int i = 0; i < hits.length; i++) {
            SearchHit hit = hits[i];
            SearchSongsDataSongsData song = new SearchSongsDataSongsData();

            song.setAlbumId(Long.parseLong(hit.getSource().get("album_id").toString()));
            song.setAlbumName(hit.getSource().get("album_name").toString());
            song.setArtistId(Long.parseLong(hit.getSource().get("singer_id").toString()));
            song.setArtistName(hit.getSource().get("singer_name").toString());
            song.setSinger(hit.getSource().get("singer_name").toString());
            //song.setArtistSubTitle(hit.getSource().get("singer_eng_name").toString());
            song.setSongId(Long.parseLong(hit.getSource().get("song_id").toString()));
            /*
                set the listen file based on song_source
             */

            if ((hit.getSource().get("song_style").toString().equals(""))||(hit.getSource().get("song_style").toString().equals(" "))){
                song.setSongName(hit.getSource().get("song_name").toString().toUpperCase());
            }
            else {
                song.setSongName(hit.getSource().get("song_name").toString().toUpperCase()+" ("+hit.getSource().get("song_style").toString()+")");
            }
            song.setSubTitle(hit.getSource().get("song_source").toString());

            if (hit.getSource().get("song_source").toString().equals("oss")){
                String singer_name = hit.getSource().get("singer_name").toString().toUpperCase();
                String song_name = URLEncoder.encode(song.getSongName(), "UTF-8");
                String url = "http://english-listen-resource.oss-cn-beijing.aliyuncs.com/"+URLEncoder.encode(singer_name.replace(" ",""), "UTF-8")+"/"+URLEncoder.encode(singer_name, "UTF-8").replace("+","%20")+"-"+song_name.replace("+","%20")+".mp3";
                song.setListenFile(url);
            }
            /*
            else if (hit.getSource().get("song_source").toString().equals("xiami")){
                song.setListenFile(getSongDetailById(song.getSongId()).getListen_file());
            }

            else if (hit.getSource().get("song_source").toString().equals("baidu")) {
                System.out.println("find listen file in baidu");
            }
            */
            song.setPlayCounts(Long.parseLong(hit.getSource().get("play_count").toString()));
            song_list.add(i,song);
        }

        Songs_new.setData(song_list);

        return Songs_new;

    }

    /*
        Check if query is empty
     */
    public boolean IsEmpty(MusicSemantic semantic) {

        if ((semantic.getArtist()==null)&&(semantic.getSong()==null)&&(semantic.getAlbum()==null)){
            return false;
        }
        else{
            return true;
        }
    }

    /*
        Refine the returned results if user wants to search by song name --> return oss file first, then xiami ...
     */
    public ResponsePageVo<MusicSong> SongRefine(ResponsePageVo<MusicSong> Songs, int pageNumber, int pageSize) {
        int page_num = pageNumber;
        int page_size = pageSize;
        int count=0;

        ResponsePageVo<MusicSong> response_xiami = new ResponsePageVo<MusicSong>();

        ResponsePageVo<MusicSong> Songs_all = new ResponsePageVo<MusicSong>();
        List<MusicSong> Song_List_all = new ArrayList<MusicSong>();

        ResponsePageVo<MusicSong> Songs_oss = new ResponsePageVo<MusicSong>();
        List<MusicSong> Song_List_oss = new ArrayList<MusicSong>();

        ResponsePageVo<MusicSong> Songs_xiami = new ResponsePageVo<MusicSong>();
        List<MusicSong> Song_List_xiami = new ArrayList<MusicSong>();

        ResponsePageVo<MusicSong> Songs_other = new ResponsePageVo<MusicSong>();
        List<MusicSong> Song_List_other = new ArrayList<MusicSong>();

        for (MusicSong Song : Songs.getData()) {
            if (count==0){
                Song_List_all.add(Song);
                Songs_all.setData(Song_List_all);
            }
            if (Song.getSong_type().equals("oss")) {
                Song_List_oss.add(Song);
                Songs_oss.setData(Song_List_oss);
                return Songs_oss;
            }
            count++;
        }
        for (MusicSong Song : Songs.getData()) {
            if (Song.getSong_type().equals("xiami")) {
                if (getSongDetailById(Song.getSong_id()).getListen_file()!=null) {
                    Song.setListen_file(getSongDetailById(Song.getSong_id()).getListen_file());
                    Song_List_xiami.add(Song);
                    Songs_xiami.setData(Song_List_xiami);
                    return Songs_xiami;
                }
            } else {
                /*
                  get listen file information via Xiami Api
                */
                //response_xiami = searchSongsByKeyword(Song.getSong_name()+" "+Song.getSingers(),page_num,10000);
                response_xiami = searchSongsByKeyword(Song.getSong_name().toLowerCase(), page_num, 10000);
                if (response_xiami.getData() != null) {
                    for (MusicSong XiamiSong : response_xiami.getData()) {
                        if (XiamiSong.getArtist().toLowerCase().contains(Song.getSingers().toLowerCase())) {
                            Song_List_other.add(XiamiSong);
                            Songs_other.setData(Song_List_other);
                            return Songs_other;
                        }
                    }
                }
            }
        }
        return Songs_all;
    }
    /*
        Refine the returned results if user wants to search by singer name --> return oss file first, then xiami ...
    */
    public ResponsePageVo<MusicSong> SingerRefine(ResponsePageVo<MusicSong> Songs, int pageNumber, int pageSize) {
        int page_num = pageNumber;
        int page_size = pageSize;
        int count_oss=0;
        int count_xiami=0;
        int count_other=0;

        ResponsePageVo<MusicSong> response_xiami = new ResponsePageVo<MusicSong>();

        ResponsePageVo<MusicSong> Songs_all = new ResponsePageVo<MusicSong>();
        List<MusicSong> Song_List_all = new ArrayList<MusicSong>();

        for (MusicSong Song : Songs.getData()) {
            if (Song.getSong_type().equals("oss")) {
                Song_List_all.add(Song);
                Songs_all.setData(Song_List_all);
                count_oss++;
            }
            if (count_oss >= page_size) {
                return Songs_all;
            }
        }
        for (MusicSong Song : Songs.getData()) {
            if (Song.getSong_type().equals("xiami")) {
                if (getSongDetailById(Song.getSong_id()).getListen_file() != null) {
                    Song.setListen_file(getSongDetailById(Song.getSong_id()).getListen_file());
                    Song_List_all.add(Song);
                    Songs_all.setData(Song_List_all);
                    count_xiami++;
                }
            }
            if (count_oss + count_xiami >= page_size) {
                return Songs_all;
            }
        }
        for (MusicSong Song : Songs.getData()) {
            /*
                 get listen file information via Xiami Api
            */
            //response_xiami = searchSongsByKeyword(Song.getSong_name()+" "+Song.getSingers(),page_num,10000);
            response_xiami = searchSongsByKeyword(Song.getSingers(), page_num, page_size-count_oss-count_xiami);
            if (response_xiami.getData() != null) {
                for (MusicSong XiamiSong : response_xiami.getData()) {
                    Song_List_all.add(XiamiSong);
                    Songs_all.setData(Song_List_all);
                    count_other++;
                }
            }
            if ((count_oss + count_xiami + count_other) >= page_size) {
                return Songs_all;
            }
        }
        if ((count_oss>0)||(count_xiami>0)||(count_other>0)){
            return Songs_all;
        }
        else{
            return Songs;
        }
    }

    /*
        New added api for searching songs
    */

    public ResponsePageVo<MusicSong> searchSongsByKeywordVinci1(MusicSemantic semantic, int pageNumber, int pageSize) {
        return new BizTemplate<ResponsePageVo<MusicSong>>("Search_SearchSongsByKeywordVinci") {
            private int page_num = pageNumber;
            private int page_size = pageSize;

            @Override
            protected void checkParams() throws VinciException {
                if (!IsEmpty(semantic)){
                    throw new VinciException(ErrorCode.MUSIC_PARAM_ERROR, "search music keyword is empty", "搜索关键词不能为空");
                }

                if (page_num <= 0) {
                    page_num = 1;
                }
                if (page_size > 100) {
                    page_size = 100;
                }

            }

            @Override
            protected ResponsePageVo<MusicSong> process() throws Exception {

                ResponsePageVo<MusicSong> Songs_either = new ResponsePageVo<MusicSong>();
                List<MusicSong> Song_List_either = new ArrayList<MusicSong>();

                ResponsePageVo<MusicSong> Songs_orgin = new ResponsePageVo<MusicSong>();
                List<MusicSong> Song_List_orgin = new ArrayList<MusicSong>();

                ResponsePageVo<MusicSong> Songs_both = new ResponsePageVo<MusicSong>();
                List<MusicSong> Song_List_both = new ArrayList<MusicSong>();

                XiamiMusicSearchService esc = new XiamiMusicSearchService();
                esc.initESClient();
                SearchResponse response_new = new SearchResponse();
                response_new = esc.search(semantic, page_num, 10000);

                if (response_new.getHits().hits().length == 0) {
                    //System.out.println("search music song from our dataset is not exist");
                    ResponsePageVo<MusicSong> Songs_Xiami = new ResponsePageVo<MusicSong>();
                    Songs_Xiami = searchSongsByKeyword(semantic.getArtist()+semantic.getAlbum()+semantic.getSong(),page_num,page_size);
                    if (Songs_Xiami==null){
                        //System.out.println("search music song from xiami is not exist");
                        return null;
                    }
                    else{
                        return Songs_Xiami;
                    }
                }

                if (response_new.isContextEmpty() && !response_new.isTimedOut()) {
                    logger.debug("搜索虾米音乐({}):{}", semantic, response_new.getHits());

                    ResponsePageVo<MusicSong> songs = null;

                    SearchSongsDataSongs Songs_new = SongSetting(response_new);

                    if (response_new.isContextEmpty() && response_new.getHits().hits() != null) {
                        songs = parseSearchMusicByKeyword(Songs_new);
                    }
                    if (songs == null) {
                        throw new VinciException(ErrorCode.MUSIC_NOT_EXIST, "search music song(" + semantic.getSong() + ") from our dataset is not exist", "没有找到歌曲");
                    }

                    songs.setPage(page_num);
                    songs.setPageSize(page_size);

                    boolean song_either_flag = false;
                    boolean song_both_flag = false;
                    int count = 1;
                    /*
                    Check if the returned results match both attributes (e.g., album name, aritist name, etc)
                     */
                    for (MusicSong song : songs.getData()) {
                        if ((song.getAlbum_name() != null) || (song.getSingers() != null)) {
                            if ((song.getAlbum_name() != null) && (song.getSingers() != null) && (semantic.getAlbum() != null) && (semantic.getArtist() != null)) {
                                if ((song.getAlbum_name().toLowerCase().equals(semantic.getAlbum().toLowerCase())) && (song.getSingers().toLowerCase().contains(semantic.getArtist().toLowerCase()))) {
                                    Song_List_both.add(song);
                                    Songs_both.setData(Song_List_both);
                                    song_both_flag = true;
                                }
                            }
                            /*
                             Check if the returned results match either attribute (e.g., album name or aritist name, etc)
                            */
                            if ((song.getAlbum_name() != null) && (semantic.getAlbum() != null)) {
                                if (song.getAlbum_name().toLowerCase().equals(semantic.getAlbum().toLowerCase())) {
                                    song_either_flag = true;
                                    Song_List_either.add(song);
                                    Songs_either.setData(Song_List_either);
                                }
                            }
                            if ((song.getSingers() != null) && (semantic.getArtist() != null)) {
                                if (song.getSingers().toLowerCase().contains(semantic.getArtist().toLowerCase())) {
                                    song_either_flag = true;
                                    Song_List_either.add(song);
                                    Songs_either.setData(Song_List_either);
                                }
                            }

                        }
                        Song_List_orgin.add(song);
                        Songs_orgin.setData(Song_List_orgin);

                        //if (count > page_size) {
                        if (count > response_new.getHits().hits().length) {
                            break;
                        }
                        count = count + 1;
                    }
                    if (song_both_flag == true) {
                        if (semantic.getSong()!=null) {
                            return SongRefine(Songs_both, page_num, page_size);
                        }
                        else {
                            return SingerRefine(Songs_both, page_num, page_size);
                        }
                    } else {
                        if (song_either_flag == true) {
                            if (semantic.getSong()!=null){
                                return SongRefine(Songs_either,page_num, page_size);
                            }
                            else {
                                return SingerRefine(Songs_either,page_num, page_size);
                            }
                        } else {
                            if (semantic.getSong()!=null){
                                return SongRefine(Songs_orgin,page_num, page_size);
                            }
                            else {
                                return SingerRefine(Songs_orgin,page_num, page_size);
                            }
                        }
                    }
                }
                throw new VinciException(ErrorCode.INTERNAL_SERVER_ERROR, "search music song timeout", "搜索歌曲超时");
            }
        }.execute();
    }

    /*
        xiami search song api
    */

    public ResponsePageVo<MusicSong> searchSongsByKeyword(final String keyword, int pageNumber, int pageSize) {
        return new BizTemplate<ResponsePageVo<MusicSong>>("Search_SearchSongsByKeyword") {
            private int page_num = pageNumber;
            private int page_size = pageSize;

            @Override
            protected void checkParams() throws VinciException {
                if (StringUtils.isEmpty(keyword)) {
                    throw new VinciException(ErrorCode.MUSIC_PARAM_ERROR,"search xiami music keyword is empty","搜索关键词不能为空");
                }
                if (page_num <=0) {
                    page_num = 1;
                }
                if (page_size > 100) {
                    page_size = 100;
                }

            }

            @Override
            protected ResponsePageVo<MusicSong> process() throws Exception {
                AlibabaXiamiApiSearchSongsGetRequest request = new AlibabaXiamiApiSearchSongsGetRequest();
                request.setKey(keyword);
                request.setLimit((long) page_size);
                request.setPage((long) page_num);
                AlibabaXiamiApiSearchSongsGetResponse response = client.execute(request);

                if (response.isSuccess() && response.getBody() != null) {
                    logger.debug("搜索虾米音乐({}):{}",keyword,response.getBody());

                    ResponsePageVo<MusicSong> songs = null;
                    if (response.getData() != null && response.getData().getSongs() != null) {
                        songs = parseSearchMusicByKeyword(response.getData().getSongs());
                    }
                    if (songs == null) {
                        throw new VinciException(ErrorCode.MUSIC_NOT_EXIST,"search music song("+keyword+") from xiami is not exist","没有找到歌曲");
                    }
                    songs.setPage(page_num);
                    songs.setPageSize(page_size);

                    return songs;
                }
                throw new VinciException(ErrorCode.INTERNAL_SERVER_ERROR,"search xiami timeout","搜索歌曲超时");
            }
        }.execute();
    }


    /*
        New added api for searching albums
    */

    public ResponsePageVo<MusicAlbum> searchAlbumsByKeywordVinci(MusicSemantic semantic, int pageNumber, int pageSize) {
        return new BizTemplate<ResponsePageVo<MusicAlbum>>("Search_SearchAlbumsByKeywordVinci") {
            private int page_num = pageNumber;
            private int page_size = pageSize;

            @Override
            protected void checkParams() throws VinciException {
                if (!IsEmpty(semantic)) {
                    throw new VinciException(ErrorCode.MUSIC_PARAM_ERROR,"search music keyword is empty","搜索关键词不能为空");
                }
                if (page_num <=0) {
                    page_num = 1;
                }
                if (page_size > 100) {
                    page_size = 100;
                }
            }

            @Override
            protected ResponsePageVo<MusicAlbum> process() throws Exception {

                String Album_name;
                ResponsePageVo<MusicAlbum> albums_new = null;

                XiamiMusicSearchService esc = new XiamiMusicSearchService();
                esc.initESClient();
                SearchResponse response_new=new SearchResponse();

                response_new = esc.search(semantic, page_num, 10000);

                if (response_new.getHits().hits().length == 0){
                    //System.out.println("search music album from our dataset is not exist");
                    ResponsePageVo<MusicAlbum> albums_Xiami = null;
                    albums_Xiami = searchAlbumsByKeyword(semantic.getAlbum(),page_num,page_size);
                    if (albums_Xiami==null){
                        //System.out.println("search music album from xiami is not exist");
                        return null;
                    }
                    else{
                        return albums_Xiami;
                    }
                }

                if (response_new.isContextEmpty()&& !response_new.isTimedOut()) {
                    logger.debug("搜索虾米音乐({}):{}", semantic, response_new.getHits());

                    List<StandardAlbum> Album_new=AlbumSetting(response_new);
                    if (response_new.isContextEmpty() && response_new.getHits().hits() != null) {
                        albums_new = parseSearchAlbumByKeyword(Album_new);
                        if (albums_new != null) {
                            albums_new.setTotalCount(response_new.getHits().hits().length);
                        }
                    }
                    if (albums_new == null) {
                        throw new VinciException(ErrorCode.MUSIC_NOT_EXIST, "search music album(" + semantic.getAlbum() + ") from our dataset is not exist", "没有找到专辑");
                    }

                    albums_new.setPage(page_num);
                    albums_new.setPageSize(page_size);

                    return albums_new;
                }
                throw new VinciException(ErrorCode.INTERNAL_SERVER_ERROR,"search music album timeout","搜索专辑超时");
            }
        }.execute();
    }

    /*
        xiami search album api
     */
    public ResponsePageVo<MusicAlbum> searchAlbumsByKeyword(final String keyword, int pageNumber, int pageSize) {
        return new BizTemplate<ResponsePageVo<MusicAlbum>>("Search_SearchAlbumsByKeyword") {
            private int page_num = pageNumber;
            private int page_size = pageSize;

            @Override
            protected void checkParams() throws VinciException {
                if (StringUtils.isEmpty(keyword)) {
                    throw new VinciException(ErrorCode.MUSIC_PARAM_ERROR,"search xiami music keyword is empty","搜索关键词不能为空");
                }
                if (page_num <=0) {
                    page_num = 1;
                }
                if (page_size > 100) {
                    page_size = 100;
                }
            }

            @Override
            protected ResponsePageVo<MusicAlbum> process() throws Exception {
                AlibabaXiamiApiSearchAlbumsGetRequest request = new AlibabaXiamiApiSearchAlbumsGetRequest();
                request.setKey(keyword);
                request.setLimit((long) page_size);
                request.setPage((long) page_num);
                AlibabaXiamiApiSearchAlbumsGetResponse response = client.execute(request);
                if (response.isSuccess() && response.getBody() != null) {
                    logger.debug("搜索虾米音乐专辑({}):{}",keyword,response.getBody());

                    ResponsePageVo<MusicAlbum> albums = null;
                    if (response.getData() != null && response.getData().getAlbums()!= null) {
                        albums = parseSearchAlbumByKeyword(response.getData().getAlbums());
                        if (albums != null) {
                            albums.setTotalCount(response.getData().getTotalNumber()!=null?response.getData().getTotalNumber().intValue():0);
                        }
                    }
                    if (albums == null) {
                        throw new VinciException(ErrorCode.MUSIC_NOT_EXIST,"search music album("+keyword+") from xiami is not exist","没有找到专辑");
                    }

                    albums.setPage(page_num);
                    albums.setPageSize(page_size);
                    return albums;
                }
                throw new VinciException(ErrorCode.INTERNAL_SERVER_ERROR,"search xiami timeout","搜索专辑超时");
            }
        }.execute();
    }


    public List<MusicUserTags> getTagTags(String type, long id) {
        return new BizTemplate<List<MusicUserTags>>("Search_GetTagTags") {

            @Override
            protected void checkParams() throws VinciException {
                if (!"song".equalsIgnoreCase(type) && !"album".equalsIgnoreCase(type) && !"artist".equalsIgnoreCase(type)) {
                    throw new VinciException(ErrorCode.MUSIC_PARAM_ERROR,"search xiami tagTags is invalid:"+type,"type参数错误");
                }
            }

            @Override
            protected List<MusicUserTags> process() throws Exception {
                AlibabaXiamiApiTagTagsRequest request = new AlibabaXiamiApiTagTagsRequest();
                request.setObjectType(type);
                request.setObjectId(id);
                AlibabaXiamiApiTagTagsResponse response = client.execute(request);
                if (response.isSuccess() && response.getBody() != null) {
                    logger.debug("搜索虾米音乐Tag({},{}):{}",type,id,response.getBody());
                    List<MusicUserTags> tags = null;
                    if (response.getData() != null && response.getData().getTags() != null) {
                        tags = Lists.newArrayListWithExpectedSize(response.getData().getTags().size());
                        for (XiamiTag tag : response.getData().getTags()) {
                            tags.add(new MusicUserTags(tag.getTag(),tag.getCount()));
                        }
                    }
                    if (tags == null) {
                        throw new VinciException(ErrorCode.MUSIC_NOT_EXIST,"search music tags("+type+","+id+") from xiami is not exist","没有找到歌曲");
                    }
                    return tags;
                }
                throw new VinciException(ErrorCode.INTERNAL_SERVER_ERROR,"search xiami timeout","搜索用户tag超时");
            }
        }.execute();
    }


    private ResponsePageVo<MusicSong> parseSearchMusicByKeyword(SearchSongsDataSongs songs) {
        if (songs == null) {
            return null;
        }
        ResponsePageVo<MusicSong> result = new ResponsePageVo<>();
        result.setData(Lists.newArrayList());
        result.setTotalCount(songs.getCount()!=null?songs.getCount().intValue():0);
        for (SearchSongsDataSongsData song : songs.getData()) {
            MusicSong s = parse2MusicSong(song);
            if (s != null) {
                result.getData().add(s);
            }
        }
        return result;
    }

    private MusicAlbum parse2Album(AlbumDetail detail) {
        if (detail == null) {
            return null;
        }
        MusicAlbum album = new MusicAlbum();
        album.setAlbum_id(detail.getAlbumId()!=null?detail.getAlbumId():0);
        album.setAlbum_name(detail.getAlbumName());
        album.setAlbum_logo(detail.getLogo());
        album.setArtist_id(detail.getArtistId()!=null?detail.getArtistId():0);
        album.setArtist_name(detail.getArtistName());
        album.setArtist_logo(detail.getArtistLogo());
        album.setCompany(detail.getCompany());
        album.setDesc(detail.getDescription());
        album.setSub_title(detail.getSubTitle());
        album.setPublish_time(detail.getGmtPublish());
        album.setIs_check(detail.getIsCheck()!=null?detail.getIsCheck().intValue():3);
        album.setPlay_counts(detail.getPlayCount() != null ? detail.getPlayCount() : 0);
        album.setPlayAuthority(detail.getPlayAuthority() != null && detail.getPlayAuthority() == 1);
        List<MusicSong> songs = Lists.newArrayList();
        for (Song s : detail.getSongs()) {
            MusicSong song = parse2MusicSong(s);
            if (song != null) {
                song.setPlayAuthority(album.isPlayAuthority());
                songs.add(song);
            }
        }
        album.setSong_count(songs.size());
        album.setSongs(songs);
        return album;
    }

    private MusicSong parse2MusicSong(Song s){
        if (s == null) {
            return null;
        }
        MusicSong song = new MusicSong("xiami");
        song.setSong_id(s.getSongId());
        song.setPlayAuthority(s.getPlayAuthority() != null && s.getPlayAuthority() == 1);
        song.setSong_name(s.getSongName());
        song.setListen_file(s.getListenFile());
        song.setArtist_id(s.getArtistId() != null ? s.getArtistId() : 0);
        song.setArtist(s.getArtistName());
        song.setSingers(s.getArtistName());
        song.setArtist_logo(s.getArtistLogo());
        song.setAlbum_id(s.getAlbumId()!=null?s.getAlbumId():0);
        song.setAlbum_name(s.getAlbumName());
        song.setAlbum_logo(s.getAlbumLogo());
        song.setSingers(s.getSingers());
        song.setLyric_file(s.getLyricFile());
        song.setPlay_seconds(s.getPlaySeconds()!=null?s.getPlaySeconds().intValue():0);
        song.setPlay_counts(s.getPlayCounts()!=null?s.getPlayCounts():0);
        //song.setArtist_sub_title(s.getTitle());    //english name
        return song;
    }

    private ResponsePageVo<MusicAlbum> parseSearchAlbumByKeyword(List<StandardAlbum> albums) {
        if (albums == null) {
            return null;
        }
        ResponsePageVo<MusicAlbum> result = new ResponsePageVo<>();
        result.setData(Lists.newArrayList());
        for (StandardAlbum detail : albums) {

            MusicAlbum album = new MusicAlbum();
            album.setAlbum_id(detail.getAlbumId()!=null?detail.getAlbumId():0);
            album.setAlbum_name(detail.getAlbumName());
            album.setSub_title(detail.getSubTitle());
            album.setAlbum_logo(detail.getLogo());
            album.setArtist_id(detail.getArtistId()!=null?detail.getArtistId():0);
            album.setArtist_name(detail.getArtistName());
            //album.setArtist_logo(detail.getArtistLogo());
            album.setCompany(detail.getCompany());
            album.setDesc(detail.getDescription());
            album.setSub_title(detail.getSubTitle());
            //album.setPublish_time(detail.getGmtPublish());
            album.setIs_check(detail.getIsCheck()!=null?detail.getIsCheck().intValue():3);
            album.setPlay_counts(detail.getPlayCount() != null ? detail.getPlayCount() : 0);
            //album.setPlayAuthority(detail.getIsCheck()!=3);
            album.setSong_count(detail.getSongCount() != null ? detail.getSongCount().intValue() : 0);
            album.setDesc(detail.getDescription());
            result.getData().add(album);

        }
        return result;
    }
    private MusicSong parse2MusicSong(SearchSongsDataSongsData s){
        if (s == null) {
            return null;
        }
        MusicSong song = new MusicSong("xiami");
        //song.setSong_style(s.getSongStyle());
        song.setSong_id(s.getSongId());
        song.setPlayAuthority(s.getIsPlay() != null && s.getIsPlay() == 1);
        song.setSong_name(s.getSongName());
        song.setSong_sub_title(s.getSubTitle());
        song.setListen_file(s.getListenFile());
        song.setArtist_id(s.getArtistId() != null ? s.getArtistId() : 0);
        song.setArtist(s.getArtistName());
        song.setSingers(s.getArtistName());
        song.setArtist_sub_title(s.getArtistSubTitle());
        song.setArtist_logo(s.getArtistLogo());
        song.setAlbum_id(s.getAlbumId()!=null?s.getAlbumId():0);
        song.setAlbum_name(s.getAlbumName());
        song.setAlbum_sub_title(s.getAlbumSubTitle());
        song.setAlbum_logo(s.getAlbumLogo());
        song.setSingers(s.getSinger());
        song.setLyric_file(s.getLyricFile());
        song.setPlay_seconds(s.getPlaySeconds()!=null?s.getPlaySeconds().intValue():0);
        song.setPlay_counts(s.getPlayCounts()!=null?s.getPlayCounts():0);
        return song;
    }
    @Override
    public void afterPropertiesSet() throws Exception {
    }

    /**
     * @param rangType
     * @param pageNumber
     * @param pageSize
     * @return
     */
    public ResponsePageVo<MusicSong> searchRankByType(final String rangType, int pageNumber, int pageSize) {
        return new BizTemplate<ResponsePageVo<MusicSong>>("Search_SearchSongsByKeyword") {
            private int page_num = pageNumber;
            private int page_size = pageSize;

            @Override
            protected void checkParams() throws VinciException {
            }

            @Override
            protected ResponsePageVo<MusicSong> process() throws Exception {
                AlibabaXiamiApiRankSongsGetRequest request = new AlibabaXiamiApiRankSongsGetRequest();
                request.setType(rangType);
                AlibabaXiamiApiRankSongsGetResponse response = client.execute(request);
                if (response.isSuccess() && response.getBody() != null) {
                    logger.debug("搜索虾米排行榜({}):{}",rangType,response.getBody());

                    ResponsePageVo<MusicSong> songs = null;
                    if (response.getData() != null && response.getData().getSongs() != null) {
                        songs = parseRankSongsByType(response.getData());
                    }
                    if (songs == null) {
                        throw new VinciException(ErrorCode.MUSIC_NOT_EXIST,"search music song("+rangType+") from xiami is not exist","没有找到榜单");
                    }
                    songs.setPage(page_num);
                    songs.setPageSize(page_size);
                    return songs;
                }
                throw new VinciException(ErrorCode.INTERNAL_SERVER_ERROR,"search xiami timeout","搜索榜单超时");
            }
        }.execute();
    }


    /**
     * 将返回的榜单推荐结果，解析成为musicsong
     * @param types
     * @return
     */
    private ResponsePageVo<MusicSong> parseRankSongsByType(RankSongsData types) {
        if (types == null) {
            return null;
        }
        ResponsePageVo<MusicSong> result = new ResponsePageVo<>();
        result.setData(Lists.newArrayList());
        result.setTotalCount(types.getSongs()!=null?types.getSongs().size():0);
        for (RankSong song : types.getSongs()) {
            MusicSong s = parse2MusicSong(song);
            if (s != null) {
                result.getData().add(s);
            }
        }
        return result;
    }

    /**
     * @return
     */
    private MusicSong parse2MusicSong(RankSong s){
        if (s == null) {
            return null;
        }
        MusicSong song = new MusicSong("xiami");
        song.setSong_id(s.getSongId());
        song.setPlayAuthority(s.getPlayAuthority() != null && s.getPlayAuthority() == 1);
        song.setSong_name(s.getSongName());
        song.setArtist_id(s.getArtistId() != null ? s.getArtistId() : 0);
        song.setArtist(s.getArtistName());
        song.setArtist_logo(s.getArtistLogo());
        song.setAlbum_id(s.getAlbumId()!=null?s.getAlbumId():0);
        song.setAlbum_name(s.getAlbumName());
        song.setAlbum_logo(s.getAlbumLogo());
        song.setSingers(s.getSingers());
        song.setListen_file(s.getListenFile());
        song.setLyric_file(s.getLyricFile());
        song.setPlay_seconds(s.getPlaySeconds()!=null?s.getPlaySeconds().intValue():0);
        song.setPlay_counts(s.getPlayCounts()!=null?s.getPlayCounts():0);
        return song;
    }

    //获取相似歌曲列表
    public List<StandardSong> searchSongSimilarity(long songId,long size) throws ApiException {
        if (size <= 0) {
            size = 50L;
        }
        AlibabaXiamiApiSongSimilarGetRequest req = new AlibabaXiamiApiSongSimilarGetRequest();
        req.setId(songId);
        req.setLimit(size);
        AlibabaXiamiApiSongSimilarGetResponse rsp = client.execute(req);
        List<StandardSong> list = rsp.getData();
        return list;
    }

    //获取今日推荐
    public List<RecommendSong> todaySongs(Long limit,String ids) throws ApiException {
        AlibabaXiamiApiRecommendDailySongsGetRequest req = new AlibabaXiamiApiRecommendDailySongsGetRequest();
        req.setLimit(limit);
        req.setIds(ids);
        AlibabaXiamiApiRecommendDailySongsGetResponse rsp = client.execute(req);
        List<RecommendSong> list = rsp.getData().getSongs();
        return list;
    }

}
