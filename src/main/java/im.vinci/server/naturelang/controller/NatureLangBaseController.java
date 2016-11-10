package im.vinci.server.naturelang.controller;

import com.taobao.api.ApiException;
import com.taobao.api.domain.StandardSong;
import im.vinci.server.naturelang.domain.*;
import im.vinci.server.naturelang.listener.Context;
import im.vinci.server.naturelang.service.ApiDotAiChatbotService;
import im.vinci.server.naturelang.service.NatureLangService;
import im.vinci.server.naturelang.service.impl.XunFeiSearchService;
import im.vinci.server.naturelang.service.impl.process.ElasticUtils;
import im.vinci.server.naturelang.utils.CommonUtils;
import im.vinci.server.naturelang.utils.ObjectUtils;
import im.vinci.server.naturelang.utils.SimilarityUtil;
import im.vinci.server.search.domain.music.MusicAlbum;
import im.vinci.server.search.domain.music.MusicSong;
import im.vinci.server.search.service.HimalayaSearchService;
import im.vinci.server.search.service.XiamiMusicSearchService;
import im.vinci.server.utils.Networks;
import im.vinci.server.utils.apiresp.ResponsePageVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mlc on 2016/10/14.
 */
public class NatureLangBaseController {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    Environment env;
    @Autowired
    NatureLangService natureLangSerivce;
    @Autowired
    HimalayaSearchService himalayaSearchService;
    @Autowired
    XiamiMusicSearchService xiamiMusicSearchService;
    @Autowired
    XunFeiSearchService xunfeiSearchService;
    @Autowired
    ApiDotAiChatbotService apiDotAiChatbotService;

    public ResponsePageVo<MusicSong> QueryXiamiSongsByKeyword(MusicSemantic music) throws ApiException {
        return QueryXiamiSongsByKeyword(music,10, true);
    }

    public ResponsePageVo<MusicSong> QueryXiamiSongsByKeyword(MusicSemantic music, int pageSize , boolean isNeedRecommend) throws ApiException {
        ResponsePageVo<MusicSong> musicSongs = this.xiamiMusicSearchService.searchSongsByKeyword(this.xiamiToString(music), 1, pageSize);
        if (isNeedRecommend && !CollectionUtils.isEmpty(musicSongs.getData())) {
            musicSongs.setData(musicSongs.getData().subList(0,1));
            List<StandardSong> songs = this.xiamiMusicSearchService.searchSongSimilarity(musicSongs.getData().get(0).getSong_id(),100L);
            musicSongs.getData().addAll(
                    ObjectUtils.transferToMusicSongList(songs)
            );
        }
        //如果歌曲为oss中的内容，则进行通过oss来取值
        if(!CollectionUtils.isEmpty(Context.getOssMusicSong(music.getArtist()))){
            List<MusicSong> songs0 = new ArrayList<>();
            List<MusicSong> songs = Context.getOssMusicSong(music.getArtist());
            for (MusicSong song : songs) {
                if (song.getSong_name().equals(music.getSong())) {
                    musicSongs.getData().clear();
                    songs0.add(song);
                    continue;
                }
            }
            musicSongs.getData().addAll(songs0);
        }
        logger.debug("search song by keyword:"+musicSongs);
        return musicSongs;
    }

    public MusicAlbum getAlbumDetail(MusicSemantic music) {
        Long index = Long.valueOf(0L);
        List list = this.xiamiMusicSearchService.searchAlbumsByKeyword(music.getAlbum(), 0, 10).getData();
        if(list.size() > 0) {
            index = Long.valueOf(((MusicAlbum)list.get(0)).getAlbum_id());
        }
        return this.xiamiMusicSearchService.getAlbumDetailById(index.longValue());
    }

    public ResponsePageVo<MusicSong> getSongsByArtist_Recommened(MusicSemantic music) {
        ResponsePageVo result = this.xiamiMusicSearchService.searchSongsByKeyword(music.getArtist(), 0, 30);
        //如果歌曲为oss中的内容，则进行通过oss来取值
        if(!CollectionUtils.isEmpty(Context.getOssMusicSong(music.getArtist()))){
            result.setData(Context.getOssMusicSong(music.getArtist()));
        }
        List<MusicSong> musicSongs = result.getData();
        if(!CollectionUtils.isEmpty(musicSongs)) {
            List<MusicSong> musicSongList1 = chooseSongsForSingers(music,musicSongs,true);
            Collections.shuffle(musicSongList1);
            List<MusicSong> musicSongList2 = chooseSongsForSingers(music,musicSongs,false);
            Collections.shuffle(musicSongList2);
            musicSongs.clear();
            musicSongs.addAll(musicSongList1);
            musicSongs.addAll(musicSongList2);
        }
        return result;
    }

    public List chooseSongsForSingers(MusicSemantic music,List<MusicSong> musicSongs,boolean flag) {
        List<MusicSong> musicSongList = new ArrayList<>();
        String singer = music.getArtist();
        for (MusicSong song : musicSongs) {
            if ((singer.equals(song.getArtist()) || singer.equals(song.getSingers()))&&true == flag) {
                musicSongList.add(song);
            } else if(!(singer.equals(song.getArtist()) || singer.equals(song.getSingers()))){
                musicSongList.add(song);
            }
        }
        return musicSongList;
    }

    public String xiamiToString(MusicSemantic music) {
        String xiami = "";
        if(StringUtils.isNotBlank(music.getArtist())) {
            xiami = xiami + music.getArtist() + " ";
        }
        if(StringUtils.isNotBlank(music.getAlbum())) {
            xiami = xiami + music.getAlbum() + " ";
        }
        if(StringUtils.isNotBlank(music.getSong())) {
            xiami = xiami + music.getSong();
        }
        return xiami;
    }

    public String xmlyToString(XMLYSemantic xmly) {
        String hima = "";
        if(StringUtils.isNotBlank(xmly.getCatalog())) {
            hima = hima + xmly.getCatalog();
        }
        if(StringUtils.isNotBlank(xmly.getAlbum())) {
            hima = hima + xmly.getAlbum();
        }
        if(StringUtils.isNotBlank(xmly.getName())) {
            hima = hima + xmly.getName();
        }
        return hima;
    }

    //根据用户指令lang，判定用户听歌与聊天的置信度
    public boolean ifListenOrChat(String lang,MusicSemantic musicSemantic) throws Exception {
        boolean flag = true;
        String prefix_dic_name = "prefix.dic";
        String suffix_dic_name = "suffix.dic";
        float[] score_array = new float[3];
        //如果lang中，歌手和歌曲信息相符，则可认为是听歌
        if (StringUtils.isNotBlank(musicSemantic.getSong())&&!lang.contains(musicSemantic.getSong())) {
            flag = false;
        }
        if (StringUtils.isNotBlank(musicSemantic.getArtist())&&!lang.contains(musicSemantic.getArtist())) {
            flag = false;
        }
        if (StringUtils.isNotBlank(musicSemantic.getAlbum())&&!lang.contains(musicSemantic.getAlbum())) {
            flag = false;
        }
        if (flag == true) {
            return flag;
        }
        String result = xiamiToString(musicSemantic);
        result = result.replace(" ", "");
        ArrayList<String> tokens = new ElasticUtils().preProcess(lang, score_array, prefix_dic_name, suffix_dic_name, true);
        float similarity = SimilarityUtil.levenshtein(result, ObjectUtils.listToString(tokens));
        if (similarity > Float.valueOf(env.getProperty("similarity"))) {
            return true;
        }
        return false;
    }

    public boolean ifNeedRecommendToday(String lang) throws IOException {
        boolean flag = false;
        //由于与推荐冲突 暂时关闭此需求 mlc-2016-10-20
        /*if(lang.contains("推荐")||lang.contains("随便")||lang.contains("来一些")||lang.contains("来几首")
                ||lang.contains("来点")||lang.contains("来些")||lang.contains("给点")){
            if (!Context.IfSingerInLang(lang)) {
                flag = true;
            }
        }*/
        return flag;
    }

    //是否需要对讯飞的结果进行修正
    public boolean ifNeedFixForXunfei(XunfeiModel xunfei, String lang) {
        boolean flag = false;
        if (StringUtils.isNotBlank(xunfei.getFlag()) && "weather".equalsIgnoreCase(xunfei.getFlag()) && !CommonUtils.ifExitPlace(lang)) {
            flag = true;
        }
        return flag;
    }

    /**
     * @param lang
     * @apiNote 针对讯飞发起的过滤,若为空气质量，未提供地点时，使用ip补全
     * */
    public String filterXunfeiLang(HttpServletRequest request, String lang) {
        if(lang.matches("^.*空气.*质量.*$")||lang.contains("pm2.5")){
            lang = CommonUtils.GetAddressByIp(Networks.getClientIpAddress(request)) + lang ;
        }
        return lang;
    }
    //判定muscisemantic的组成结构
    public String ifMusicSemantic(MusicSemantic music,MusicSong musicSong) {
        filterSemantic(music,musicSong,null);
        String result = "";
        if (StringUtils.isNotBlank(music.getSong())&&(!CommonUtils.ifEqual(music.getSong(),musicSong.getSong_name()))) {
            result = "song";
        }
        if (StringUtils.isNotBlank(music.getAlbum())&&(!CommonUtils.ifEqual(music.getAlbum(),musicSong.getAlbum_name()) ))  {
            if(StringUtils.isNotBlank(music.getSong())){
                result = "album1_" + result;    //专辑信息不对，但有歌曲信息
            }else{
                result = "album_" + result;//专辑信息不对，无其它信息
            }
        }
        if (StringUtils.isNotBlank(music.getArtist())&&(!(musicSong.getSingers().contains(music.getArtist())||CommonUtils.ifEqual(music.getArtist(),musicSong.getSingers())))) {
            if(StringUtils.isNotBlank(music.getSong())){ //歌手信息不对，但有歌曲信息
                result = "artist1_" + result;
            }else {
                result = "artist_" + result;//歌手信息不对，无其它信息
            }
        }
        return result;
    }

    //判定muscisemantic的组成结构
    public String ifAlbumSemantic(MusicSemantic music,MusicAlbum musicAlbum) {
        filterSemantic(music,null,musicAlbum);
        String result = "";
        if (StringUtils.isNotBlank(music.getAlbum())&&(!(CommonUtils.ifEqual(music.getAlbum(),musicAlbum.getAlbum_name()))))  {
            result = "album" + result;
        }
        if (StringUtils.isNotBlank(music.getArtist())&&(!(CommonUtils.ifEqual(music.getArtist(),musicAlbum.getArtist_name())))) {
            result = "artist_" + result;
        }
        return result;
    }

    //消除标点和后缀对评判结果的影响
    public void filterSemantic(MusicSemantic music,MusicSong musicSong,MusicAlbum musicAlbum){
        if (music != null) {
            if (StringUtils.isNotBlank(music.getAlbum())) {
                music.setAlbum(SimilarityUtil.filterStringByTokens(music.getAlbum()));
            }
            if (StringUtils.isNotBlank(music.getSong())) {
                music.setSong(SimilarityUtil.filterStringByTokens(music.getSong()));
            }
        }
        if (musicAlbum != null) {
            if (StringUtils.isNotBlank(musicAlbum.getAlbum_name())) {
                musicAlbum.setAlbum_name(SimilarityUtil.filterStringByTokens(musicAlbum.getAlbum_name()));
            }
            if (StringUtils.isNotBlank(musicAlbum.getArtist_name())) {
                musicAlbum.setAlbum_name(SimilarityUtil.filterStringByTokens(musicAlbum.getAlbum_name()));
            }
        }
        if (musicSong != null) {
            if (StringUtils.isNotBlank(musicSong.getSong_name())) {
                musicSong.setSong_name(SimilarityUtil.filterStringByTokens(musicSong.getSong_name()));
            }
            if (StringUtils.isNotBlank(musicSong.getAlbum_name())) {
                musicSong.setAlbum_name(SimilarityUtil.filterStringByTokens(musicSong.getAlbum_name()));
            }
        }
    }

    public String musicInfoToText(MusicSemantic music) {
        String result = "";
        switch (toJudgeMusicElement(music)) {
            case "artist_":
                result = "很抱歉，没有找到歌手" + music.getArtist() + "的歌,";
                break;
            case "album_":
                result = "很抱歉，没有找到专辑" + music.getAlbum()+"中的歌";
                break;
            case "song":
                result = "很抱歉，没有找到" + music.getSong() + "这首歌,";
                break;
            case "artist_album_":
                result = "很抱歉，没有找到歌手" + music.getArtist() + "的" + music.getAlbum() + "专辑,";
                break;
            case "artist_song":
                result = "很抱歉，没有找到歌手" + music.getArtist() + "的" + music.getSong() + "这首歌,";
                break;
            case "artist_album_song":
                result = "很抱歉，没有找到歌手" + music.getArtist() + "的专辑"+music.getAlbum()+"中的"+music.getSong()+"这首歌,";
                break;
            default:
                result ="很抱歉，没有找到,";
        }
        return result;
    }

    /**
     *
     * 将结果转化为文字
     * */
    public String toPlainResult(MusicSemantic semantic,MusicSong musicSong) {
        String result = "";
        if(StringUtils.isNotBlank(semantic.getAlbum())){
            if (StringUtils.isNotBlank(semantic.getAlbum())) {
                result = result + semantic.getAlbum() +"专辑 ";
            }
            if (StringUtils.isNotBlank(semantic.getArtist())) {
                result = semantic.getArtist() + " ";
            }
            result = result + "的" + musicSong.getSong_name();
        }else{
            if (StringUtils.isNotBlank(musicSong.getSingers())) {
                result = musicSong.getSingers() + " ";
            }
            if (StringUtils.isNotBlank(semantic.getAlbum())) {
                result = result + semantic.getAlbum() +"专辑 ";
            }
            if (StringUtils.isNotBlank(result)) {
                result = result +  "的" + musicSong.getSong_name();
            }else{
                result = musicSong.getSong_name();
            }
        }
        if (StringUtils.isNotBlank(result)) {
            result = "为你播放" + result;
        }
        return result;
    }

    /**
     *
     * 将结果转化为文字
     * */
    public String toPlainAlbumResult(MusicSemantic semantic,MusicAlbum musicAlbum) {
        String result = "";
        if (StringUtils.isNotBlank(semantic.getAlbum())) {
            result = result + semantic.getAlbum() +"专辑 ";
        }
        if (!CollectionUtils.isEmpty(musicAlbum.getSongs())) {
            result = result + musicAlbum.getSongs().get(0).getArtist() + "的" + musicAlbum.getSongs().get(0).getSong_name();
        }
        if (StringUtils.isNotBlank(result)) {
            result = "为你播放" + result;
        }
        return result;
    }

    /**
     * 通过两个东西来给出自然语言的反馈, 优先MusicSong;
     * 如果MusicSong为null,则转为MusicSemantic的转化
     */
    public String toPlainTextOfMusic(MusicSemantic music , MusicSong song, MusicAlbum album) {
        String result = "";
        if (song != null) {
            if (StringUtils.isNotBlank(song.getSingers())) {
                result = song.getSingers() + "演唱的";
            } else if (StringUtils.isNoneBlank(song.getArtist())) {
                result = song.getArtist() + "的";
            }
            if (StringUtils.isNotBlank(result)) {
                result = result + song.getSong_name();
            } else {
                result = "歌曲" + song.getSong_name();
            }
        }else if (album != null) {
            if (StringUtils.isNoneBlank(album.getArtist_name())) {
                result = album.getArtist_name() + "的";
            }
            result += "专辑" + album.getAlbum_name();
        }else if (music != null) {
            switch (toJudgeMusicElement(music)) {
                case "artist_":
                    result = music.getArtist() + "的歌,";
                    break;
                case "album_":
                    result = "专辑" + music.getAlbum()+"中的歌";
                    break;
                case "song":
                    result = "歌曲" + music.getSong();
                    break;
                case "artist_album_":
                    result = "歌手" + music.getArtist() + "的" + music.getAlbum() + "专辑,";
                    break;
                case "artist_song":
                    result = music.getArtist() + "的" + music.getSong();
                    break;
                case "artist_album_song":
                    result = "歌手" + music.getArtist() + "的专辑"+music.getAlbum()+"中的"+music.getSong();
                    break;
                default:
                    result ="";
            }
        }
        return result;
    }

    public String toJudgeMusicElement(MusicSemantic musicSemantic) {
        String result = "";
        if (StringUtils.isNotBlank(musicSemantic.getArtist())) {
            result = "artist_";
        }
        if (StringUtils.isNotBlank(musicSemantic.getAlbum())) {
            result = result + "album_";
        }
        if (StringUtils.isNotBlank(musicSemantic.getSong())) {
            result = result + "song";
        }
        return result;
    }

    public void getUnderStandCode(UnderstandModel model, MusicSemantic musicSemantic) {
        if(StringUtils.isNotEmpty(musicSemantic.getSong())) {
            model.setCode(SematicCode.CodeEnum.music_song.toString());
        } else if(StringUtils.isNotEmpty(musicSemantic.getArtist())) {
            model.setCode(SematicCode.CodeEnum.music_artist.toString());
        } else {
            model.setCode(SematicCode.CodeEnum.music_album.toString());
        }
    }
}
