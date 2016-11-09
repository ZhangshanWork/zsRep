package im.vinci.server.naturelang.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.xiaoleilu.hutool.util.DateUtil;
import im.vinci.server.naturelang.domain.*;
import im.vinci.server.naturelang.domain.SematicCode.CodeEnum;
import im.vinci.server.naturelang.listener.Context;
import im.vinci.server.naturelang.service.DispatcherService;
import im.vinci.server.naturelang.service.back.PmBack;
import im.vinci.server.naturelang.service.back.RecordBack;
import im.vinci.server.naturelang.service.back.ClockBack;
import im.vinci.server.naturelang.service.back.WeatherBack;
import im.vinci.server.naturelang.utils.CommonUtils;
import im.vinci.server.naturelang.utils.ObjectUtils;
import im.vinci.server.naturelang.utils.TranslateBing;
import im.vinci.server.naturelang.wrapper.ResponseResult;
import im.vinci.server.search.domain.music.MusicAlbum;
import im.vinci.server.search.domain.music.MusicSong;
import im.vinci.server.utils.apiresp.ResponsePageVo;
import im.vinci.server.utils.apiresp.ResultObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(
        value = {"/vinci/naturelang"},
        produces = {"application/json;charset=UTF-8"}
)
public class NatureLangV2Controller extends NatureLangBaseController{
   
    @Autowired
    private DispatcherService dispatcherService;
    /**
     * v2新版系统接口
     * @url /nlpresult/v1
     * */
    @RequestMapping(value = {"/nlpresult/v2"})
    public ResultObject<ResponseResult> getNatureLangResult_v2(@RequestBody Parameter parameter) throws Exception {
        ResponseResult responseResult = new ResponseResult();
        HashMap<String, Object> map = new HashMap<>();
        String originQuery = "";//存储原始值，供回写使用
        if (StringUtils.isNotEmpty(parameter.getQuery())) {
            originQuery = parameter.getQuery();//存储原始值，供回写使用
            parameter.setQuery(Context.filterXiamiLang(parameter.getQuery()));
        }
        //确定所属服务
        ServiceRet serviceRet = dispatcherService.dispatch(parameter.getQuery());
        if (serviceRet.getRc() != 0 || null == serviceRet.getService()) {
            responseResult.setService("bad");
            responseResult.setSpeech("好像不能理解，我会尽快学习，试试其它的吧");
        }else{
            responseResult.setService(serviceRet.getService().toLowerCase());
            if (StringUtils.isNoneBlank(serviceRet.getOperation())) {
                responseResult.setOperation(serviceRet.getOperation().toLowerCase());
            }
            responseResult.setQuery(originQuery);

            switch (serviceRet.getService()) {
                case "music" ://音乐1
                    processMusic(responseResult, parameter, map);
                    break;
                case "music_download": //音乐下载
                    processDownloadMusic(responseResult, parameter, map);
                    break;
                case "weather"://天气2
                    processWeather(responseResult, parameter, map);
                    break;
                case "schedule"://提醒3
                    processSchedule(responseResult, parameter, map);
                    break;
                case "translation"://翻译4
                    processTranslation(responseResult, parameter, map);
                    break;
                case "PM2.5"://空气质量5
                    processPM25(responseResult, parameter, map);
                    break;
                case "map"://导航6
                    processMap(responseResult, parameter, map);
                    break;
                case "openQA"://褒贬，问候，情绪7
                    processOpenQA(responseResult, parameter, map);
                    break;
                case "datetime"://日期8
                    processDateTime(responseResult, parameter, map);
                    break;
                case "calc"://计算9
                    processCalc(responseResult, parameter, map);
                    break;
                case "baike"://百科10
                    processBaike(responseResult, parameter, map);
                    break;
                case "chat"://社区问答，闲聊11
                    processChat(responseResult, parameter, map);
                    break;
                case "faq"://社区问答，闲聊11
                    responseResult.setService("chat");
                    processChat(responseResult, parameter, map);
                    break;
                case "machine"://机器指令 12
                    processMachine(responseResult, parameter, map);
                    break;
                case "record"://录音服务
                    processRecord(responseResult, parameter, map);
                    break;

            }
        }
        return new ResultObject<ResponseResult>(responseResult);
    }

    //翻译
    private void processTranslation(ResponseResult responseResult, Parameter parameter, Map<String, Object> map) throws Exception {
        Map map1 = xunfeiSearchService.reponseXunfei(parameter.getQuery());
        JSONObject jsonObject = (JSONObject) map1.get("result");
        responseResult.setSemantic(jsonObject.getJSONObject("semantic"));
        String content = jsonObject.getJSONObject("semantic").getJSONObject("slots").getString("content");
        String result = TranslateBing.doTranslate(content);
        responseResult.setSpeech(result);
    }


    //褒贬，问候，情绪
    private void processOpenQA(ResponseResult responseResult, Parameter parameter, Map<String, Object> map) throws Exception {
        Map map1 = xunfeiSearchService.reponseXunfei(parameter.getQuery());
        JSONObject jsonObject = (JSONObject) map1.get("result");
        responseResult.setSpeech(jsonObject.getJSONObject("answer").getString("text"));

    }

    //地图
    private void processMap(ResponseResult responseResult, Parameter parameter, Map<String, Object> map) throws Exception {
        Map map1 = xunfeiSearchService.reponseXunfei(parameter.getQuery());
        JSONObject jsonObject = (JSONObject) map1.get("result");
        responseResult.setSemantic(jsonObject.getJSONObject("semantic"));
        responseResult.setSpeech("已开始为您设置路线");

    }

    //定时提醒
    private void processSchedule(ResponseResult responseResult, Parameter parameter, Map<String, Object> map) throws Exception {
        Response response_schedule = new ClockBack(parameter).response;

        JSONObject semantic = new JSONObject();
        JSONObject slot = new JSONObject();
        JSONObject datetime = new JSONObject();
        semantic.put("content", response_schedule.getSemantic().getContent());
        slot.put("name", response_schedule.getSemantic().getSlots().getName());

        if(response_schedule.getOperation().equals("CONCENTRATE")){
            slot.put("whiteNoise",Context.getWhiteNoise());
        }

        datetime.put("type", response_schedule.getSemantic().getSlots().getDatetime().getType());
        datetime.put("date", response_schedule.getSemantic().getSlots().getDatetime().getDate());
        datetime.put("dateOrig", response_schedule.getSemantic().getSlots().getDatetime().getDate());
        datetime.put("time", response_schedule.getSemantic().getSlots().getDatetime().getTime());
        datetime.put("timeOrig", response_schedule.getSemantic().getSlots().getDatetime().getTimeOrig());
        datetime.put("repeat", response_schedule.getSemantic().getSlots().getDatetime().isRepeat());
        datetime.put("duration", response_schedule.getSemantic().getSlots().getDatetime().getDuration());
        datetime.put("index", response_schedule.getSemantic().getSlots().getDatetime().getIndex());
        datetime.put("count", response_schedule.getSemantic().getSlots().getDatetime().getCount());

        slot.put("datetime", datetime);
        semantic.put("slot", slot);

        responseResult.setSemantic(semantic);
        responseResult.setOperation(response_schedule.getOperation().toLowerCase());

        if(null != response_schedule.getRtext() && !response_schedule.getRtext().equals("")){
            responseResult.setAsked(response_schedule.getRtext());
            responseResult.setSpeech(null);
            return;
        }
        switch (response_schedule.getOperation()){
            case "CREATE":
                responseResult.setSpeech("已开始为您设定");
                break;
            case "VIEW":
                responseResult.setSpeech("已开始为您查询");
                break;
            case "DELETE":
                responseResult.setSpeech("已删除");
                break;
            case "CONCENTRATE":
                responseResult.setSpeech("进入专注");
                break;
        }

    }


    //空气质量
    private void processPM25(ResponseResult responseResult, Parameter parameter, Map<String, Object> map) throws Exception {
        PMResponse response_pm = new PmBack().getPM(parameter);
        JSONObject semantic = new JSONObject();

        JSONObject slot = new JSONObject();
        slot.put("sourceName", response_pm.getSemantic().getSlots().getSourceName());
        slot.put("aqi", response_pm.getSemantic().getSlots().getAqi());
        slot.put("publishDateTime", response_pm.getSemantic().getSlots().getPublishDateTime());
        slot.put("subArea", response_pm.getSemantic().getSlots().getSubArea());
        slot.put("publishDateTimeLong", response_pm.getSemantic().getSlots().getPublishDateTimeLong());
        slot.put("pm25", response_pm.getSemantic().getSlots().getPm25());
        slot.put("positionName", response_pm.getSemantic().getSlots().getPositionName());
        slot.put("quality", response_pm.getSemantic().getSlots().getQuality());
        slot.put("area", response_pm.getSemantic().getSlots().getArea());
        semantic.put("slot", slot);

        JSONObject location = new JSONObject();
        location.put("cityAddr", response_pm.getSemantic().getLocation().getCityAddr());
        location.put("city", response_pm.getSemantic().getLocation().getCity());
        location.put("type", response_pm.getSemantic().getLocation().getType());
        location.put("areaAddr", response_pm.getSemantic().getLocation().getAreaAddr());
        location.put("area", response_pm.getSemantic().getLocation().getArea());
        location.put("province", response_pm.getSemantic().getLocation().getProvince());
        location.put("provinceAddr", response_pm.getSemantic().getLocation().getProvinceAddr());
        semantic.put("location", location);

        responseResult.setSemantic(semantic);

        if(null != response_pm.getRtext() && !response_pm.getRtext().equals("")){
            responseResult.setAsked(response_pm.getRtext());
            responseResult.setSpeech(null);
            return;
        }
        String answer = "";
        if (ObjectUtils.isNotEmperty(slot.get("area"))) {
            answer = answer + " 地区：" + slot.getString("area");
        }
        if (ObjectUtils.isNotEmperty(slot.get("pm25"))) {
            answer = answer + " pm2.5：" + slot.getString("pm25");
        }
        if (ObjectUtils.isNotEmperty(slot.get("quality"))) {
            answer = answer + " 空气质量：" + slot.getString("quality");
        }
        if (StringUtils.isNotBlank(answer) && ObjectUtils.isNotEmperty(slot.get("publishDateTime"))) {
            answer = " 信息发布时间: " + slot.getString("publishDateTime") + "," + answer;
        }

        responseResult.setSpeech(answer);
    }




//        Map map1 = xunfeiSearchService.reponseXunfei(parameter.getQuery());
//        JSONObject json = (JSONObject) map1.get("result");
//        responseResult.setSemantic(json.getJSONObject("semantic"));
//        JSONObject jsonObject = json.getJSONObject("data").getJSONArray("result").getJSONObject(0);
//        String answer = "";
//        if(ObjectUtils.isNotEmperty(jsonObject.get("area"))){
//            answer = answer + " 地区：" +jsonObject.getString("area");
//        }
//        if(ObjectUtils.isNotEmperty(jsonObject.get("pm25"))){
//            answer = answer + " pm2.5：" +jsonObject.getString("pm25");
//        }
//        if(ObjectUtils.isNotEmperty(jsonObject.get("quality"))){
//            answer = answer + " 空气质量：" +jsonObject.getString("quality");
//        }
//        if (StringUtils.isNotBlank(answer)&&ObjectUtils.isNotEmperty(jsonObject.get("publishDateTime"))) {
//            answer = " 信息发布时间: " + jsonObject.getString("publishDateTime") + "," + answer;
//        }
//        responseResult.setSpeech(answer);

    //天气
    private void processWeather(ResponseResult responseResult, Parameter parameter, Map<String, Object> map) throws Exception {
        WeatherResponse response_weather = new WeatherBack().get_weather(parameter);

        JSONObject semantic = new JSONObject();

        JSONObject slot = new JSONObject();
        JSONObject location = new JSONObject();
        JSONObject datetime = new JSONObject();

        slot.put("airQuality",response_weather.getSemantic().getSlots().getAirQuality());
        slot.put("sourceName",response_weather.getSemantic().getSlots().getSourceName());
        slot.put("date",response_weather.getSemantic().getSlots().getDate());
        slot.put("lastUpdateTime",response_weather.getSemantic().getSlots().getLastUpdateTime());
        slot.put("dateLong",response_weather.getSemantic().getSlots().getDateLong());
        slot.put("city",response_weather.getSemantic().getSlots().getCity());
        slot.put("wind",response_weather.getSemantic().getSlots().getWind());
        slot.put("windLevel",response_weather.getSemantic().getSlots().getWindLevel());
        slot.put("weather",response_weather.getSemantic().getSlots().getWeather());
        slot.put("tempRange",response_weather.getSemantic().getSlots().getTempRange());
        semantic.put("slot",slot);

        location.put("cityAddr", response_weather.getSemantic().getLocation().getCityAddr());
        location.put("city", response_weather.getSemantic().getLocation().getCity());
        location.put("type", response_weather.getSemantic().getLocation().getType());
        location.put("areaAddr", response_weather.getSemantic().getLocation().getAreaAddr());
        location.put("area", response_weather.getSemantic().getLocation().getArea());
        location.put("province", response_weather.getSemantic().getLocation().getProvince());
        location.put("provinceAddr", response_weather.getSemantic().getLocation().getProvinceAddr());
        semantic.put("location", location);

        datetime.put("date",response_weather.getSemantic().getDatetime().getDate());
        datetime.put("type",response_weather.getSemantic().getDatetime().getType());
        datetime.put("dateOrig",response_weather.getSemantic().getDatetime().getDateOrig());
        semantic.put("datetime",datetime);

        responseResult.setSemantic(semantic);
//        Map map1 = xunfeiSearchService.reponseXunfei(parameter.getQuery());
//        JSONObject json = (JSONObject) map1.get("result");
//        responseResult.setSemantic(json.getJSONObject("semantic"));

//        String nowDate = response_weather.getSemantic().getDatetime().getDate();
//        if(nowDate.equalsIgnoreCase("CURRENT_DAY")){
//            nowDate = DateUtil.today();
//        }
//        JSONObject jsonObject = json.getJSONObject("data").getJSONArray("result").getJSONObject(0);
//        JSONArray jsonArray = json.getJSONObject("data").getJSONArray("result");
//        for(int i=0;i<jsonArray.size();i++) {
//            //如果存在时间此时间重合
//            if(nowDate.equals(jsonArray.getJSONObject(i).getString("date"))){
//                jsonObject = jsonArray.getJSONObject(i);
//                break;
//            }
//        }
        if(null != response_weather.getRtext() && !response_weather.getRtext().equals("")){
            responseResult.setAsked(response_weather.getRtext());
            responseResult.setSpeech(null);
            return;
        }
        String nowDate = response_weather.getSemantic().getSlots().getDate();
        if(null != nowDate && nowDate.equalsIgnoreCase("CURRENT_DAY")){
            nowDate = DateUtil.today();
        }
        String answer = "";
        if(ObjectUtils.isNotEmperty(response_weather.getSemantic().getLocation().getProvince())){
            answer = answer + response_weather.getSemantic().getLocation().getProvince();
        }
        if(ObjectUtils.isNotEmperty(response_weather.getSemantic().getSlots().getCity())){
            answer = answer + response_weather.getSemantic().getSlots().getCity();
        }
        if(ObjectUtils.isNotEmperty(response_weather.getSemantic().getSlots().getWeather())){
            answer = answer + " 天气：" +response_weather.getSemantic().getSlots().getWeather();
        }
        if(ObjectUtils.isNotEmperty(response_weather.getSemantic().getSlots().getTempRange())){
            answer = answer + " 温度：" +response_weather.getSemantic().getSlots().getTempRange();
        }
        if(ObjectUtils.isNotEmperty(response_weather.getSemantic().getSlots().getWind())){
            answer = answer + response_weather.getSemantic().getSlots().getWind();
        }
        if(ObjectUtils.isNotEmperty(response_weather.getSemantic().getSlots().getWindLevel())){
            answer = answer + " 风力：" +response_weather.getSemantic().getSlots().getWindLevel();
        }
        if (StringUtils.isNotBlank(answer)&&ObjectUtils.isNotEmperty(response_weather.getSemantic().getSlots().getDate())) {
            answer = " 日期: " + nowDate + "," + answer;
        }

        responseResult.setSpeech(answer);
    }

    //日期
    private void processDateTime(ResponseResult responseResult, Parameter parameter, Map<String, Object> map) throws Exception {
        Map map1 = xunfeiSearchService.reponseXunfei(parameter.getQuery());
        JSONObject jsonObject = (JSONObject) map1.get("result");
        responseResult.setSpeech(ObjectUtils.iflyAsrFilter(jsonObject.getJSONObject("answer").getString("text")));

    }


    //计算
    private void processCalc(ResponseResult responseResult, Parameter parameter, Map<String, Object> map) throws Exception {
        Map map1 = xunfeiSearchService.reponseXunfei(parameter.getQuery());
        JSONObject jsonObject = (JSONObject) map1.get("result");
        responseResult.setSpeech(ObjectUtils.iflyAsrFilter(jsonObject.getJSONObject("answer").getString("text")));

    }


    //百科
    private void processBaike(ResponseResult responseResult, Parameter parameter, Map<String, Object> map) throws Exception {
        Map map1 = xunfeiSearchService.reponseXunfei(parameter.getQuery());
        JSONObject jsonObject = (JSONObject) map1.get("result");
        responseResult.setSpeech(ObjectUtils.iflyAsrFilter(jsonObject.getJSONObject("answer").getString("text")));

    }


    //社区问答，闲聊
    private void processChat(ResponseResult responseResult, Parameter parameter, Map<String, Object> map) throws Exception {
        Map map1 = xunfeiSearchService.reponseXunfei(parameter.getQuery());
        JSONObject jsonObject = (JSONObject) map1.get("result");
        responseResult.setSpeech(ObjectUtils.iflyAsrFilter(jsonObject.getJSONObject("answer").getString("text")));

    }

    //机器指令
    private void processMachine(ResponseResult responseResult, Parameter parameter, Map<String, Object> map) throws Exception {
        return;
    }

    //录音服务
    private void processRecord(ResponseResult responseResult, Parameter parameter, Map<String, Object> map) throws Exception {
        if(responseResult.getOperation().equals("set")){
            responseResult.setSpeech("准备开始录音");
            return;
        }
        RecordResponse response_record = new RecordBack().selectRecord(parameter);
        JSONObject semantic = new JSONObject();

        JSONObject slot = new JSONObject();
        slot.put("datetime",response_record.getSemantic().getSlots().getDatetime());
        slot.put("dateOrig",response_record.getSemantic().getSlots().getDateOrig());
        slot.put("type",response_record.getSemantic().getSlots().getType());
        slot.put("name",response_record.getSemantic().getSlots().getName());

        semantic.put("slot", slot);

        //responseResult.setSpeech(response_record.getRtext());
        responseResult.setSemantic(semantic);
        if(slot.get("type").equals("wrong")){
            responseResult.setSpeech("对不起，无法理解您的录音播放要求");
        }
        responseResult.setSpeech("为您播放录音");

        return;
    }

    //音乐
    private void processDownloadMusic(ResponseResult responseResult, Parameter parameter, Map<String, Object> map) throws Exception {
        List<MusicSemantic> musicSemantics = natureLangSerivce.getFinalResult(parameter.getQuery());
        MusicSemantic musicSemantic = musicSemantics.get(0);
        //防止把歌手识别成歌名
        if (Context.IfSinger(musicSemantic.getSong()) && !StringUtils.isNotBlank(musicSemantic.getArtist())) {
            musicSemantic.setArtist(musicSemantic.getSong());
            musicSemantic.setSong(null);
        }
        //过滤一些特殊字符
        filterSemantic(musicSemantic,null,null);

        map.put("slots",musicSemantic);  //设置语义存储
        responseResult.setSemantic(map);
        GetXiaMiResultForDownload(responseResult, musicSemantic);
    }

    private void GetXiaMiResultForDownload(ResponseResult responseResult, MusicSemantic music) throws Exception {
        ResponsePageVo resp = new ResponsePageVo();
        if(StringUtils.isNotBlank(music.getRank())) {
            responseResult.setData(this.xiamiMusicSearchService.searchRankByType(music.getRank(), 1, 10));
            responseResult.setOperation(music.getRank());
        } else if(StringUtils.isNotEmpty(music.getGenre())) {
            resp.setData(Context.getGenreName(music.getGenre()));
            Collections.shuffle(resp.getData());
            if(ObjectUtils.isNotEmperty(Context.getGenreName(music.getGenre()))) {
                resp.setTotalCount(Context.getGenreName(music.getGenre()).size());
            }
            responseResult.setData(resp);
            responseResult.setOperation(CodeEnum.music_genre.toString());
        } else if(StringUtils.isNotEmpty(music.getSong())) {
            responseResult.setData(this.QueryXiamiSongsByKeyword(music, 30, false));
            responseResult.setOperation(CodeEnum.music_song.toString());
            filterMusicDownloadDataForSongName(responseResult , music);
        } else if(StringUtils.isNotEmpty(music.getAlbum())) {
            MusicAlbum album = this.getAlbumDetail(music);
            responseResult.setOperation(CodeEnum.music_album.toString());
            filterMusicDownloadDataForAlbumName(responseResult , music , album);
        } else if(StringUtils.isNotEmpty(music.getArtist())) {
            responseResult.setData(this.QueryXiamiSongsByKeyword(music, 100, false));
            responseResult.setOperation(CodeEnum.music_artist.toString());
            filterMusicDownloadDataForArtistName(responseResult , music);
        }
        if (StringUtils.isEmpty(responseResult.getSpeech())) {
            responseResult.setSpeech(ObjectUtils.iflyAsrFilter("正在为你下载"));
        }
    }

    private void filterMusicDownloadDataForAlbumName(ResponseResult responseResult, MusicSemantic music, MusicAlbum album) {
        if (album == null) {
            responseResult.setData(null);
            responseResult.setSpeech(ObjectUtils.iflyAsrFilter("没有找到"+ toPlainTextOfMusic(music, null, null)));
            return;
        }
        if (music.getAlbum().equals(album.getAlbum_name()) || music.getAlbum().equals(album.getSub_title())) {
            responseResult.setData(album);
            responseResult.setSpeech(ObjectUtils.iflyAsrFilter("正在为你下载"+ toPlainTextOfMusic(null,null,album)));
        } else {
            responseResult.setData(null);
            responseResult.setSpeech(ObjectUtils.iflyAsrFilter("没有找到"+ toPlainTextOfMusic(music,null,null)+ ", 是要找" + toPlainTextOfMusic(null,null,album)+ "么"));
        }
    }

    @SuppressWarnings("unchecked")
    private void filterMusicDownloadDataForArtistName(ResponseResult model, MusicSemantic music) {
        ResponsePageVo<MusicSong> musicSongResponsePageVo = (ResponsePageVo<MusicSong>) model.getData();
        if (musicSongResponsePageVo == null || CollectionUtils.isEmpty(musicSongResponsePageVo.getData())) {
            model.setSpeech(ObjectUtils.iflyAsrFilter("没有找到"+ toPlainTextOfMusic(music, null, null)));
            return;
        }
        List<MusicSong> songs = Lists.newArrayListWithCapacity(10);
        for (MusicSong musicSong : musicSongResponsePageVo.getData()) {
            if (musicSong.getSingers().contains(music.getArtist())
                    || CommonUtils.ifEqual(music.getArtist(),musicSong.getSingers())
                    ) {
                songs.add(musicSong);
            }
            if (songs.size() >= 10) {
                break;
            }
        }
        musicSongResponsePageVo.setPageSize(10);
        musicSongResponsePageVo.setTotalCount(songs.size());
        musicSongResponsePageVo.setPage(1);
        musicSongResponsePageVo.setData(songs);
        if (songs.size() > 0) {
            model.setSpeech(ObjectUtils.iflyAsrFilter("正在为你下载"+ toPlainTextOfMusic(music, null, null)));
        } else {
            model.setSpeech(ObjectUtils.iflyAsrFilter("没有找到" + toPlainTextOfMusic(music, null, null)));
        }
    }

    @SuppressWarnings("unchecked")
    private void filterMusicDownloadDataForSongName(ResponseResult model , MusicSemantic music) {
        ResponsePageVo<MusicSong> musicSongResponsePageVo = (ResponsePageVo<MusicSong>) model.getData();
        if (musicSongResponsePageVo == null || CollectionUtils.isEmpty(musicSongResponsePageVo.getData())) {
            model.setSpeech(ObjectUtils.iflyAsrFilter("没有找到"+ toPlainTextOfMusic(music, null, null)));
            return;
        }
        MusicSong mostSimilarSong = null;
        MusicSong sameSong = null;
        for (MusicSong musicSong : musicSongResponsePageVo.getData()) {
            //歌曲名不一样就放弃
            if (!CommonUtils.ifEqual(music.getSong(),musicSong.getSong_name())) {
                continue;
            }
            if (StringUtils.isNoneBlank(music.getArtist())) {
                //用户说了歌手
                if (musicSong.getSingers().contains(music.getArtist())
                        || CommonUtils.ifEqual(music.getArtist(),musicSong.getSingers())
                        ) {
                    //歌手也一致,直接返回
                    sameSong = musicSong;
                    break;
                } else if (mostSimilarSong == null) {
                    //说的歌手不一致,先放到最相似歌曲中,等待后续处理
                    mostSimilarSong = musicSong;
                }
            } else {
                //用户没说歌手,直接返回
                sameSong = musicSong;
                break;
            }
        }
        if (sameSong != null) {
            musicSongResponsePageVo.setPageSize(10);
            musicSongResponsePageVo.setTotalCount(1);
            musicSongResponsePageVo.setPage(1);
            musicSongResponsePageVo.setData(Lists.newArrayList(sameSong));
            model.setSpeech(ObjectUtils.iflyAsrFilter("正在为你下载"+ toPlainTextOfMusic(null, sameSong, null)));
            return;
        }
        musicSongResponsePageVo.setPageSize(10);
        musicSongResponsePageVo.setTotalCount(0);
        musicSongResponsePageVo.setPage(1);
        musicSongResponsePageVo.setData(Collections.emptyList());
        model.setSpeech("没有找到"+ toPlainTextOfMusic(music, null, null));
        if (mostSimilarSong != null) {
            model.setSpeech( model.getSpeech() + ", 试一试"+ toPlainTextOfMusic(null, mostSimilarSong, null));
        }
        model.setSpeech(ObjectUtils.iflyAsrFilter(model.getSpeech()));
    }


    //音乐
    private void processMusic(ResponseResult responseResult, Parameter parameter, Map<String, Object> map) throws Exception {
        List<MusicSemantic> musicSemantics = natureLangSerivce.getFinalResult(parameter.getQuery());
        map.put("slots",musicSemantics.get(0));  //设置语义存储
        responseResult.setSemantic(map);
        GetXiaMiResult(responseResult, musicSemantics.get(0));
    }

    private void GetXiaMiResult(ResponseResult responseResult, MusicSemantic music) throws Exception {
        ResponsePageVo resp = new ResponsePageVo();
        if(StringUtils.isNotBlank(music.getRank())) {
            responseResult.setData(this.xiamiMusicSearchService.searchRankByType(music.getRank(), 0, 100));
            responseResult.setOperation(music.getRank());
        } else if(StringUtils.isNotEmpty(music.getGenre())) {
            resp.setData(Context.getGenreName(music.getGenre()));
            Collections.shuffle(resp.getData());
            if(ObjectUtils.isNotEmperty(Context.getGenreName(music.getGenre()))) {
                resp.setTotalCount(Context.getGenreName(music.getGenre()).size());
            }

            responseResult.setData(resp);
            responseResult.setOperation(CodeEnum.music_recommend.toString());
        } else if(StringUtils.isNotEmpty(music.getSong())) {
            if((Context.IfSinger(music.getSong()) && !StringUtils.isNotBlank(music.getArtist()))) {
                music.setArtist(music.getSong());
                responseResult.setData(this.getSongsByArtist_Recommened(music));
                music.setSong((String)null);
                responseResult.setOperation(CodeEnum.music_recommend.toString());
            } else {
                responseResult.setData(this.QueryXiamiSongsByKeyword(music));
                responseResult.setOperation(CodeEnum.music_song.toString());
            }
        } else if(StringUtils.isNotEmpty(music.getAlbum())) {
            responseResult.setData(this.getAlbumDetail(music));
            responseResult.setOperation(CodeEnum.music_album.toString());
        } else if(StringUtils.isNotEmpty(music.getArtist())) {
            responseResult.setData(this.getSongsByArtist_Recommened(music));
            responseResult.setOperation(CodeEnum.music_recommend.toString());
        }
        judgeIfSongInRight(responseResult, music);
    }


    private void judgeIfSongInRight(ResponseResult model, MusicSemantic music) {
        if (null != model.getData()&&model.getService().equals("music")&&!model.getOperation().equals("music_album")) {
            MusicSong musicSong = ((ResponsePageVo<MusicSong>)model.getData()).getData().get(0);
            switch (ifMusicSemantic(music,musicSong)) {
                case "song":
                    model.setSpeech(musicInfoToText(music) + "为你推荐另外一首"+musicSong.getSingers()+"的"+musicSong.getSong_name());
                    break;
                case "artist_":
                    model.setSpeech(musicInfoToText(music) + "为你推荐相似歌手"+musicSong.getSingers());
                    break;
                case "album_song":
                    model.setSpeech(musicInfoToText(music) +"为你推荐了"+musicSong.getAlbum_name()+"专辑的"+musicSong.getSong_name());
                    break;
                case "album1_song":
                    model.setSpeech(musicInfoToText(music) +"为你推荐了"+musicSong.getAlbum_name()+"专辑的"+musicSong.getSong_name());
                    break;
                case "album1_":
                    model.setSpeech(musicInfoToText(music) +"为你推荐了"+musicSong.getAlbum_name()+"专辑的"+musicSong.getSong_name());
                    break;
                case "artist_song":
                    model.setSpeech(musicInfoToText(music) +"为你推荐了"+musicSong.getSingers()+"的"+musicSong.getSong_name());
                    break;
                case "artist1_song":
                    model.setSpeech(musicInfoToText(music) +"为你推荐了"+musicSong.getSingers()+"的"+musicSong.getSong_name());
                    break;
                case "artist1_":
                    model.setSpeech(musicInfoToText(music) +"为你推荐了"+musicSong.getSingers()+"的"+musicSong.getSong_name());
                    break;
                case "artist1_album1_":
                    model.setSpeech(musicInfoToText(music) +"为你推荐了"+musicSong.getSingers()+"的"+musicSong.getAlbum_name()+"专辑的"+musicSong.getSong_name());
                    break;
                case "artist_album_song":
                    model.setSpeech(musicInfoToText(music) +"为你推荐了"+musicSong.getSingers()+"的"+musicSong.getAlbum_name()+"专辑的"+musicSong.getSong_name());
                    break;
                default:
                    model.setSpeech(toPlainResult(music,musicSong));
            }
        } else if (null != model.getData()) {
            MusicAlbum musicAlbum = (MusicAlbum)model.getData();
            switch (ifAlbumSemantic(music,musicAlbum)) {
                case "album":
                    model.setSpeech("很抱歉，歌曲库中没有" + music.getAlbum() +"专辑，为你推荐相似专辑"+musicAlbum.getAlbum_name());
                    break;
                case "artist_album":
                    model.setSpeech("很抱歉，歌曲库中没有" + music.getAlbum() +"专辑，为你推荐另外一张专辑"+musicAlbum.getArtist_name()+"的"+musicAlbum.getAlbum_name());
                    break;
                default:
                    model.setSpeech(toPlainAlbumResult(music,musicAlbum));
            }

        }

        model.setSpeech(ObjectUtils.iflyAsrFilter(model.getSpeech()));
    }

}
