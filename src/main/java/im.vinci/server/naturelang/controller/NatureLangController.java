package im.vinci.server.naturelang.controller;

import im.vinci.server.naturelang.domain.*;
import im.vinci.server.naturelang.domain.SematicCode.CodeEnum;
import im.vinci.server.naturelang.domain.nation.ApiDotAiResult;
import im.vinci.server.naturelang.listener.Context;
import im.vinci.server.naturelang.service.impl.process.ElasticHandler;
import im.vinci.server.naturelang.utils.CommonUtils;
import im.vinci.server.naturelang.utils.ObjectUtils;
import im.vinci.server.naturelang.utils.SimilarityUtil;
import im.vinci.server.naturelang.utils.TranslateBing;
import im.vinci.server.naturelang.wrapper.UnderStandResult;
import im.vinci.server.search.domain.himalayas.*;
import im.vinci.server.search.domain.music.MusicAlbum;
import im.vinci.server.search.domain.music.MusicSong;
import im.vinci.server.search.service.HimalayaSearchService;
import im.vinci.server.utils.Networks;
import im.vinci.server.utils.apiresp.ResponsePageVo;
import im.vinci.server.utils.apiresp.ResultObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping(
        value = {"/vinci/naturelang"},
        produces = {"application/json;charset=UTF-8"}
)
public class NatureLangController extends NatureLangBaseController{

    @RequestMapping({"/parsesingers"})
    public ResultObject getSingersResult(String lang) throws Exception {
        return new ResultObject(this.natureLangSerivce.getFilteredSingersResult(lang));
    }

    @RequestMapping({"/nationresult"})
    public ResultObject<UnderStandResult> getNationLangResult(String lang) throws Exception {
        UnderStandResult underStandResult = new UnderStandResult();
        UnderstandModel result = new UnderstandModel();
        //预设所有结果均为正确输出
        underStandResult.setFlag("ON");
        if(!StringUtils.isNotBlank(lang)) {
            return null;
        } else {//英文音乐结果合成
            String lang1 = lang.replace(" ", "");
            if(StringUtils.isNotBlank(Context.fetchNationMachine(lang1))) {//机器指令分析
                InstructSemantic list = new InstructSemantic();
                result.setCode(CodeEnum.machine_instruct.toString());
                list.setInstruct(Context.fetchNationMachine(lang1));
                result.setInstructSemantic(list);
            } else {
                lang = _processFrom(lang, result);
                lang1 = lang.replace(" ", "");
                composeMusicNation(lang, lang1, result, underStandResult);
            }
            underStandResult.setUnderStandModel(result);
            return new ResultObject<>(underStandResult);
        }
    }

    //解析来源
    private String _processFrom(String lang, UnderstandModel result) {
        String lang1 = "";
        if(lang.matches("^.*[from|on|in][spotify|soundcloud|alexa|Prime|Amazon|].*$")){
            if(lang.contains("spotify")){
                result.setFrom("spotify");
            } else if (lang.contains("soundcloud")) {
                result.setFrom("soundcloud");
            }else{
                result.setFrom("alexa");
            }
            String[] words = lang.split(" ");
            int i = 0; //哨兵
            for (String word : words) {
                i++;
                if (word.equalsIgnoreCase("spotify") || word.equalsIgnoreCase("soundcloud") || word.equalsIgnoreCase("alexa")
                        || word.equalsIgnoreCase("Prime") || word.equalsIgnoreCase("Amazon")) {
                    break;
                }
            }
            for(int j=0;j<i-2;j++) {
                lang1 += words[j]+" ";
            }
        }else{
            result.setFrom("alexa");
        }
        return lang1;
    }

    @RequestMapping({"/nationresult/v1"})
    public ResultObject<UnderStandResult> getNationLangResult(
            @RequestHeader(value = "vinci-imei", defaultValue = "") String imei,
            String lang) throws Exception {
        UnderStandResult underStandResult = new UnderStandResult();
        UnderstandModel result = new UnderstandModel();
        underStandResult.setUnderStandModel(result);
        ResultObject<UnderStandResult> ro = new ResultObject<>(underStandResult);
        //预设所有结果均为正确输出
        underStandResult.setFlag("ON");
        if(!StringUtils.isNotBlank(lang)) {
            result.setCode(CodeEnum.answer.toString());
            AnswerSemantic answerSemantic = new AnswerSemantic();
            answerSemantic.setText("I didn't hear what you said");
            result.setAnswer(answerSemantic);
            return ro;
        }
        ApiDotAiResult apiDotAiResult = apiDotAiChatbotService.nlu(lang,imei);
        if (apiDotAiResult != null && apiDotAiResult.getStatus() != null && apiDotAiResult.getStatus().getCode() == 200
                && apiDotAiResult.getResult() != null) {
            String action = apiDotAiResult.getResult().getAction();
            if (action != null && action.startsWith("media.music") && apiDotAiResult.getResult().getFulfillment()!=null
                    && StringUtils.isNotBlank(apiDotAiResult.getResult().getFulfillment().getSpeech())) {
                result.setCode(CodeEnum.answer.toString());
                AnswerSemantic answerSemantic = new AnswerSemantic();
                answerSemantic.setText(apiDotAiResult.getResult().getFulfillment().getSpeech());
                result.setAnswer(answerSemantic);
                return ro;
            }
        }
        return getNationLangResult(lang);
    }

    public void filterNationSematic(MusicSemantic semantic,String lang) throws IOException {
        if(StringUtils.isNotBlank(semantic.getSong()) && StringUtils.isBlank(semantic.getArtist())) {
            String enSong = semantic.getSong().replace(" ", "_").replace("\'", "_");
            if(Context.IfSinger(enSong)) {
                semantic.setArtist(semantic.getSong());
                semantic.setSong((String)null);
            }
        }
        adjustResult(semantic, lang);
    }

    /**
     * @param semantic
     * @param lang
     * @apiNote  修正结果信息
     * */
    public void adjustResult(MusicSemantic semantic,String lang) {
        //消除歌手组合对artist造成的影响
        if (StringUtils.isNotBlank(semantic.getArtist())) {
            String max = SimilarityUtil.LCS(semantic.getArtist(), lang.toLowerCase());
            semantic.setArtist(max);
        }
    }


    @RequestMapping(value = {"/nlpresult"})
    public ResultObject<UnderStandResult> getNatureLangResult(String lang) throws Exception {
        String ifMachine = "OFF";
        boolean ifRank = false;
        if(StringUtils.isNotEmpty(lang)) {
            lang = lang.toLowerCase();
        }
        UnderStandResult underStandResult = new UnderStandResult();
        UnderstandModel result = new UnderstandModel();
        XunfeiModel xunfei = new XunfeiModel();
        List list = new ArrayList();
        lang = Context.filterXiamiLang(lang);
        if(Context.ifSpoken(lang)) {
            lang = "喜马拉雅" + lang;
        }
        String lang2 = lang.replace("喜马拉雅", "").replace("喜马妈妈", "").replace("妈妈咪呀", "").replace("喜马爸爸", "").replace("洗马啦啦", "").replace("喜玛拉拉", "").replace("洗马爸爸", "").replace("洗玛啦啦", "").replace("洗马拉拉", "").replace("洗吗啦啦", "").replace("洗码啦啦", "").replace("洗码爸爸", "").replace("喜玛拉雅", "").replace("喜玛妈妈", "").replace("喜玛爸爸", "");
        XMLYSemantic ro;
        //语义分析
        if((!StringUtils.isNotEmpty(lang2) || !Context.IfMachineInstruct(lang)) && !lang.contains("翻译")) {
            //基础查询-作为比对结果
            list = this.natureLangSerivce.getFinalResult(lang);
            if(Context.ifRank(lang)) {
                ifRank = true;
                list = (new ElasticHandler()).cut(lang, true);
            } else if(StringUtils.isNotBlank(Context.fetchSpoken(lang))) {   //this.judge(lang)
                //如果为喜马拉雅，将基础查询结果重置
                list = new ArrayList<>();
                ro = new XMLYSemantic();
                ro.setAlbumId(Context.fetchSpoken(lang));
                ((List)list).add(ro);
            } else if (ifNeedRecommendToday(lang)) {
                xunfei = new XunfeiModel();
                xunfei.setFlag("today");
                xunfei.setText(Context.getPromotion());
                list = new ArrayList<>();
            } else if ((!CollectionUtils.isEmpty(list) && ifListenOrChat(lang, (MusicSemantic) list.get(0))) || Context.IfSingerInLang(lang) || Context.ifWhite(lang)
                    || Context.ifXunfeiOrMusic("music", lang)) {
                lang = Context.filterXiamiLang(lang);
                list = this.natureLangSerivce.getFinalResult(lang);
            } else {
                xunfei = this.xunfeiSearchService.getXunFeiResult(lang);
                if (!StringUtils.isNotEmpty(xunfei.getFlag())) {
                    list = this.natureLangSerivce.getFinalResult(lang);
                } else {
                    list = new ArrayList<>();
                }
            }
        } else {
            ifMachine = "ON";
        }
        //使用语义分析结果，进行结果合成
        underStandResult.setFlag("ON");  //所有结果默认开启ON
        if(!((List)list).isEmpty() && ((List)list).size() >= 1 && !"ON".equals(ifMachine)) {//音乐类处理（含喜马拉雅）
            composeMusic(lang, ifRank, list, result, underStandResult);
        } else {
            composeAnswer(lang, result, underStandResult, xunfei);
        }
        asrFilter(result);//tts语句标准化处理
        underStandResult.setUnderStandModel(result);
        ResultObject ro3 = new ResultObject(underStandResult);
        return ro3;
    }

    /**
     * 智能化应用版本接口
     * @url /nlpresult/v1
     * @time 20160524
     * */
    @RequestMapping(value = {"/nlpresult/v1"})
    public ResultObject<UnderStandResult> getNatureLangResult_v1(HttpServletRequest request,String lang) throws Exception {
        String ifMachine = "OFF";
        boolean ifRank = false;
        if(StringUtils.isNotEmpty(lang)) {
            lang = lang.toLowerCase();
        }
        UnderStandResult underStandResult = new UnderStandResult();
        UnderstandModel result = new UnderstandModel();
        XunfeiModel xunfei = new XunfeiModel();
        List list = new ArrayList();
        lang = Context.filterXiamiLang(lang);
        String lang2 = lang;
        XMLYSemantic ro;
        //语义分析
        if((!StringUtils.isNotEmpty(lang2) || !Context.IfMachineInstruct(lang))) {
            //基础查询-作为比对结果--用户指令明确
            if(Context.ifXunfeiOrMusic("music", lang)||Context.IfSinger(lang)){
                list = this.natureLangSerivce.getFinalResult(lang);
            }
            else if(Context.ifRank(lang)) {
                ifRank = true;
                list = (new ElasticHandler()).cut(lang, true);
            } else if(StringUtils.isNotBlank(Context.fetchSpoken(lang))) {   //this.judge(lang)
                //如果为喜马拉雅，将基础查询结果重置
                list = new ArrayList<>();
                ro = new XMLYSemantic();
                ro.setAlbumId(Context.fetchSpoken(lang));
                ((List)list).add(ro);
            } else if (ifNeedRecommendToday(lang)) {
                xunfei = new XunfeiModel();
                xunfei.setFlag("today");
                xunfei.setText(Context.getPromotion());
                list = new ArrayList<>();
            } /*else if ((!CollectionUtils.isEmpty(list) && ifListenOrChat(lang, (MusicSemantic) list.get(0))) || Context.ifWhite(lang)) {
                list = this.natureLangSerivce.getFinalResult(lang);
            }*/ else {
                lang = filterXunfeiLang(request, lang);
                xunfei = this.xunfeiSearchService.getXunFeiResult(lang);
                if (ifNeedFixForXunfei(xunfei, lang)) {
                    lang =CommonUtils.GetAddressByIp(Networks.getClientIpAddress(request))  +  lang;
                    xunfei = this.xunfeiSearchService.getXunFeiResult(lang);
                }
                if (!StringUtils.isNotEmpty(xunfei.getFlag())) {
                    list = this.natureLangSerivce.getFinalResult(lang);
                } else {
                    list = new ArrayList<>();
                }
            }
        } else {
            ifMachine = "ON";
        }
        //使用语义分析结果，进行结果合成
        underStandResult.setFlag("ON");  //所有结果默认开启ON
        if(!((List)list).isEmpty() && ((List)list).size() >= 1 && !"ON".equals(ifMachine)&&!"翻译".equals(lang)) {//音乐结果合成，含喜马拉雅
            composeMusic_V1(lang, list, result, underStandResult, ifRank);
        } else {//应答结果合成（含翻译）
            composeAnswer_v1(lang, result, underStandResult, xunfei, ifMachine);
        }
        asrFilter(result);//tts语句标准化处理
        underStandResult.setUnderStandModel(result);
        ResultObject ro3 = new ResultObject(underStandResult);
        return ro3;
    }

    public void asrFilter(UnderstandModel model) {
        model.setText(ObjectUtils.iflyAsrFilter(model.getText()));
    }

    public boolean judgeXiamiResult(UnderstandModel model) {
        boolean flag = false;
        if(ObjectUtils.isNotEmperty(model.getHimalayaAlbumDetial())&&model.getHimalayaAlbumDetial().getTotalCount() > 0){
            flag = true;
        }
        if(ObjectUtils.isNotEmperty(model.getHimalayaTrackByKeyword())&& model.getHimalayaTrackByKeyword().getTotalCount() > 0){
            flag = true;
        }
        if(ObjectUtils.isNotEmperty(model.getRecommendTrack())&&ObjectUtils.isNotEmperty(model.getRecommendTrack().getTracks())&&
                model.getRecommendTrack().getTracks().size() > 0){
            flag = true;
        }
        if(ObjectUtils.isNotEmperty(model.getAlbum())&& ObjectUtils.isNotEmperty(model.getAlbum().getSongs())&&model.getAlbum().getSongs().size() > 0){
            flag = true;
        }
        if(ObjectUtils.isNotEmperty(model.getMusicSong())&&ObjectUtils.isNotEmperty(model.getMusicSong().getData())&&model.getMusicSong().getData().size() > 0){
            flag = true;
        }
        return flag;
    }


    public void wisdomResult(UnderstandModel model) {
        String result = "";
        if(null != model.getMusicSemantic()){  //目前暂只处理音乐领域信息
            result = musicInfoToText(model.getMusicSemantic()) + ",试试其它的吧";
            model.setText(result);
        } else if (null != model.getXmlySemantic()) { //暂时使用应答信息
            AnswerSemantic answerSemantic = new AnswerSemantic();
            answerSemantic.setText("很抱歉，没有找到，试试其它的吧");
        } else if (null != model.getAnswer()) {
            //若已为应答，则不作处理
        } else {
            AnswerSemantic answerSemantic = new AnswerSemantic();
            answerSemantic.setText("对不起我没能理解你的话，我会尽快学习，试试其它的吧");
            model.setAnswer(answerSemantic);
        }
    }

    public boolean filterObj(MusicSemantic music) {
        return StringUtils.isNotEmpty(music.getAlbum()) || StringUtils.isNotEmpty(music.getArtist())
                || StringUtils.isNotEmpty(music.getSong()) || StringUtils.isNotEmpty(music.getGenre())
                || StringUtils.isNotEmpty(music.getRank());
    }

    public boolean filterXmly(XMLYSemantic xmly) {
        return StringUtils.isNotEmpty(xmly.getCatalog()) || StringUtils.isNotEmpty(xmly.getSubCatalog())
                || StringUtils.isNotEmpty(xmly.getAlbum()) || StringUtils.isNotEmpty(xmly.getName());
    }

    public boolean filterSpoken(XMLYSemantic xmly) {
        return StringUtils.isNotEmpty(xmly.getAlbumId());
    }

    public void GetXMLYResult(UnderstandModel model, XMLYSemantic xmly) {
        if(StringUtils.isNotEmpty(xmly.getCatalog()) && "ximalaya".equals(xmly.getCatalog())) {
            model.setRecommendTrack(this.getXMLY_DefaultByRecommend(xmly));
            model.setCode(CodeEnum.xmly_recommend.toString());
        } else if(StringUtils.isNotEmpty(xmly.getAlbumId())) {
            model.setHimalayaAlbumDetial(this.natureLangSerivce.getAlbumById(xmly.getAlbumId()+""));
            //根据市场需求，对指定资源的顺序进行调整
            if("3847547".equals(xmly.getAlbumId())
                    ||"2946910".equals(xmly.getAlbumId())
                    ||"3873469".equals(xmly.getAlbumId())
                    ||"406".equals(xmly.getAlbumId())){
                Collections.reverse(model.getHimalayaAlbumDetial().getAlbum().getTracks());
            }
            model.setCode(CodeEnum.xmly_album.toString());
        } else if(StringUtils.isNotEmpty(xmly.getName())) {
            model.setHimalayaTrackByKeyword(this.getXMLY_TrackByKeyword(xmly));
            model.setCode(CodeEnum.xmly_track.toString());
        } else if(StringUtils.isNotEmpty(xmly.getAlbum())) {
            model.setHimalayaAlbumDetial(this.getXMLY_AlbumById(xmly));
            model.setCode(CodeEnum.xmly_album.toString());
        } else if(StringUtils.isNotEmpty(xmly.getCatalog())) {
            model.setRecommendTrack(this.recommendTracksInCategory(xmly));
            model.setCode(CodeEnum.xmly_recommend.toString());
        } else if(StringUtils.isNotEmpty(xmly.getSubCatalog())) {
            model.setHimalayaAlbumDetial(this.recommendTracksInSubCategory(xmly));
            model.setCode(CodeEnum.xmly_album.toString());
        }
    }

    public GetHimalayaRecommendTrackInCategoryResponse getXMLY_DefaultByRecommend(XMLYSemantic xmly) {
        String[] recommendCondition = new String[]{"hot", "daily", "recent", "favorite"};
        List categories = this.himalayaSearchService.getCategories().getCategories();
        int condition1 = (int)(Math.random() * (double)recommendCondition.length);
        int cateIndex1 = (int)(Math.random() * (double)categories.size());
        return this.himalayaSearchService.recommendTracksInCategory((long)((HimalayaCategory)categories.get(cateIndex1)).getId(), "", recommendCondition[condition1], 0, 10);
    }

    public QueryHimalayaTrackByKeywordResponse getXMLY_TrackByKeyword(XMLYSemantic xmly) {
        return this.himalayaSearchService.queryTrackByKeyword(-1, this.xmlyToString(xmly), 0, 10);
    }

    public GetHimalayaAlbumDetailResponse getXMLY_AlbumById(XMLYSemantic xmly) {
        GetHimalayaAlbumDetailResponse album = new GetHimalayaAlbumDetailResponse();
        QueryHimalayaAlbumByKeywordResponse response = this.himalayaSearchService.queryAlbumByKeyword(-1, xmly.getAlbum(), 0, 10);
        if(response.getTotalCount() > 0 && StringUtils.isNotEmpty(((HimalayaAlbum)response.getAlbums().get(0)).getId() + "")) {
            album = this.himalayaSearchService.getAlbumById(((HimalayaAlbum)response.getAlbums().get(0)).getId(), 0, 10);
        }
        return album;
    }

    public GetHimalayaRecommendTrackInCategoryResponse recommendTracksInCategory(XMLYSemantic xmly) {
        int cataid = 0;
        List catagorys = this.himalayaSearchService.getCategories().getCategories();
        Iterator var4 = catagorys.iterator();
        while(var4.hasNext()) {
            HimalayaCategory cata = (HimalayaCategory)var4.next();
            if(StringUtils.isNotEmpty(cata.getTitle()) && cata.getTitle().equals(xmly.getCatalog())) {
                cataid = cata.getId();
                break;
            }
        }
        return this.himalayaSearchService.recommendTracksInCategory((long)cataid, "", "hot", 0, 10);
    }

    public GetHimalayaAlbumDetailResponse recommendTracksInSubCategory(XMLYSemantic xmly) {
        List list = (new HimalayaSearchService()).queryAlbumByKeyword(-1, xmly.getSubCatalog(), 0, 10).getAlbums();
        if(list.size() < 1) {
            return new GetHimalayaAlbumDetailResponse();
        } else {
            int index = (int)((double)list.size() * Math.random());
            return (new HimalayaSearchService()).getAlbumById(((HimalayaAlbum)list.get(index)).getId(), 0, 10);
        }
    }

    public void secondJuge(XunfeiModel xunfei,UnderstandModel model, AnswerSemantic semantic, String string) throws Exception {
        List list = this.natureLangSerivce.getFinalResult(string);
        //借助讯飞的flag字段，标识句子
        if ("today".equals(xunfei.getFlag())) {
            List list1 = this.xiamiMusicSearchService.todaySongs(50L,"");
            Collections.shuffle(list1);
            semantic.setList(list1);
        }
        if(!CollectionUtils.isEmpty(list) && StringUtils.isNotBlank(((MusicSemantic)list.get(0)).getGenre())) {
            semantic.setText(semantic.getText() + Context.getPromotion());
            semantic.setList(Context.getGenreName(((MusicSemantic)list.get(0)).getGenre()));
            //对曲风流派的歌曲进行补充
            Long id = Context.getGenreName(((MusicSemantic) list.get(0)).getGenre()).get(0).getSong_id();
            semantic.getList().addAll(ObjectUtils.transferToMusicSongList(xiamiMusicSearchService.searchSongSimilarity(id,50)));
            Collections.shuffle(semantic.getList());
        }
    }


    public void GetXiaMiResult(UnderstandModel model, MusicSemantic music,String version) throws Exception {
        ResponsePageVo resp = new ResponsePageVo();
        if(StringUtils.isNotBlank(music.getRank())) {
            model.setMusicSong(this.xiamiMusicSearchService.searchRankByType(music.getRank(), 0, 100));
            model.setCode(music.getRank());
        } else if(StringUtils.isNotEmpty(music.getGenre())) {
            resp.setData(Context.getGenreName(music.getGenre()));
            Collections.shuffle(resp.getData());
            if(ObjectUtils.isNotEmperty(Context.getGenreName(music.getGenre()))) {
                resp.setTotalCount(Context.getGenreName(music.getGenre()).size());
            }
            model.setMusicSong(resp);
            model.setCode(CodeEnum.music_recommend.toString());
        } else if(StringUtils.isNotEmpty(music.getSong())) {
            if((Context.IfSinger(music.getSong()) && !StringUtils.isNotBlank(music.getArtist()))) {
                music.setArtist(music.getSong());
                model.setMusicSong(this.getSongsByArtist_Recommened(music));
                music.setSong((String)null);
                model.setCode(CodeEnum.music_recommend.toString());
            } else {
                model.setMusicSong(this.QueryXiamiSongsByKeyword(music));
                model.setCode(CodeEnum.music_song.toString());
            }
        } else if(StringUtils.isNotEmpty(music.getAlbum())) {
            model.setAlbum(this.getAlbumDetail(music));
            model.setCode(CodeEnum.music_album.toString());

        } else if(StringUtils.isNotEmpty(music.getArtist())) {
            model.setMusicSong(this.getSongsByArtist_Recommened(music));
            model.setCode(CodeEnum.music_recommend.toString());
        }
        if("1".equals(version)){
            judgeIfSongInRight(model, music);
        }
    }

    private void judgeIfSongInRight(UnderstandModel model, MusicSemantic music) {
        if (null != model.getMusicSong()&&model.getMusicSong().getData().size() > 0) {
            MusicSong musicSong = model.getMusicSong().getData().get(0);
            switch (ifMusicSemantic(music,musicSong)) {
                case "song":
                    model.setText(musicInfoToText(music) + "为你推荐另外一首"+musicSong.getSingers()+"的"+musicSong.getSong_name());
                    break;
                case "artist_":
                    model.setText(musicInfoToText(music) + "为你推荐相似歌手"+musicSong.getSingers());
                    break;
                case "album_song":
                    model.setText(musicInfoToText(music) +"为你推荐了"+musicSong.getAlbum_name()+"专辑的"+musicSong.getSong_name());
                    break;
                case "album1_song":
                    model.setText(musicInfoToText(music) +"为你推荐了"+musicSong.getAlbum_name()+"专辑的"+musicSong.getSong_name());
                    break;
                case "album1_":
                    model.setText(musicInfoToText(music) +"为你推荐了"+musicSong.getAlbum_name()+"专辑的"+musicSong.getSong_name());
                    break;
                case "artist_song":
                    model.setText(musicInfoToText(music) +"为你推荐了"+musicSong.getSingers()+"的"+musicSong.getSong_name());
                    break;
                case "artist1_song":
                    model.setText(musicInfoToText(music) +"为你推荐了"+musicSong.getSingers()+"的"+musicSong.getSong_name());
                    break;
                case "artist1_":
                    model.setText(musicInfoToText(music) +"为你推荐了"+musicSong.getSingers()+"的"+musicSong.getSong_name());
                    break;
                case "artist1_album1_":
                    model.setText(musicInfoToText(music) +"为你推荐了"+musicSong.getSingers()+"的"+musicSong.getAlbum_name()+"专辑的"+musicSong.getSong_name());
                    break;
                case "artist_album_song":
                    model.setText(musicInfoToText(music) +"为你推荐了"+musicSong.getSingers()+"的"+musicSong.getAlbum_name()+"专辑的"+musicSong.getSong_name());
                    break;
                default:
                    model.setText(toPlainResult(music,musicSong));
            }
        } else if (null != model.getAlbum() && model.getAlbum().getSongs().size() > 0) {
            MusicAlbum musicAlbum = model.getAlbum();
            switch (ifAlbumSemantic(music,musicAlbum)) {
                case "album":
                    model.setText("很抱歉，歌曲库中没有" + music.getAlbum() +"专辑，为你推荐相似专辑"+musicAlbum.getAlbum_name());
                    break;
                case "artist_album":
                    model.setText("很抱歉，歌曲库中没有" + music.getAlbum() +"专辑，为你推荐另外一张专辑"+musicAlbum.getArtist_name()+"的"+musicAlbum.getAlbum_name());
                    break;
                default:
                    model.setText(toPlainAlbumResult(music,musicAlbum));
            }
        }
    }

    //喜马拉雅的结果合成
    private void composeXMLY(List list,UnderstandModel result, UnderStandResult underStandResult) {
        XMLYSemantic ro = (XMLYSemantic)((List)list).get(0);
        if(this.filterSpoken(ro)) {
            result.setCode(CodeEnum.xmly.toString());
            result.setXmlySemantic(ro);
            this.GetXMLYResult(result, ro);
        } else if(this.filterXmly(ro)) {
            result.setCode(CodeEnum.xmly.toString());
            result.setXmlySemantic(ro);
            this.GetXMLYResult(result, ro);
        } else {
            underStandResult.setFlag("OFF");
            result.setCode(CodeEnum.answer.toString());
        }
    }

    //音乐结果合成V1
    private void composeMusic_V1(String lang,List list,UnderstandModel result,UnderStandResult underStandResult,boolean ifRank) throws Exception {
        if(StringUtils.isNotBlank(Context.fetchSpoken(lang))) {   //this.judge(lang)
            composeXMLY(list,result,underStandResult);
        } else {
            MusicSemantic ro2;
            if(ifRank) {
                ro2 = new MusicSemantic();
                ro2.setRank(Context.getRankByKey((List)list));
                this.GetXiaMiResult(result, ro2,"1");
            } else if(this.filterObj((MusicSemantic)((List)list).get(0))) {
                ro2 = (MusicSemantic)((List)list).get(0);
                if (!Context.ifXunfeiOrMusic("music",lang)&&!ifListenOrChat(lang, ro2)) {
                    AnswerSemantic answerSemantic = new AnswerSemantic();
                    answerSemantic.setText("对不起我没能理解你的话，我会尽快学习，试试其它的吧");
                    result.setAnswer(answerSemantic);
                    result.setCode(CodeEnum.answer.toString());
                }else{
                    result.setMusicSemantic(ro2);
                    result.setCode(CodeEnum.music.toString());
                    this.GetXiaMiResult(result, ro2,"1");
                }
            } else {
                result.setCode(CodeEnum.answer.toString());
            }
        }
        if(!this.judgeXiamiResult(result)) {
            wisdomResult(result);
        }
        this.logger.info("用户指令：" + lang);
        this.logger.info("返回结果：" + ((List)list).get(0).toString());
    }

    //音乐类结果合成（含喜马拉雅）
    private void composeMusic(String lang,boolean ifRank,List list,UnderstandModel result,UnderStandResult underStandResult) throws Exception {
        if(StringUtils.isNotBlank(Context.fetchSpoken(lang))) {   //this.judge(lang)
            composeXMLY(list,result,underStandResult);
        } else {
            MusicSemantic ro2;
            if(ifRank) {
                ro2 = new MusicSemantic();
                ro2.setRank(Context.getRankByKey((List)list));
                this.GetXiaMiResult(result, ro2,"0");
            } else if(this.filterObj((MusicSemantic)((List)list).get(0))) {
                ro2 = (MusicSemantic)((List)list).get(0);
                result.setMusicSemantic(ro2);
                result.setCode(CodeEnum.music.toString());
                this.GetXiaMiResult(result, ro2,"0");
            } else {
                underStandResult.setFlag("OFF");
            }
        }
        if(!this.judgeXiamiResult(result)) {
            underStandResult.setFlag("OFF");
        }
        this.logger.info("用户指令：" + lang);
        this.logger.info("返回结果：" + ((List)list).get(0).toString());
    }

    //应答结果合成（含翻译）
    private void composeAnswer_v1(String lang,UnderstandModel result,UnderStandResult underStandResult,XunfeiModel xunfei,String ifMachine) throws Exception {
        AnswerSemantic ro1 = new AnswerSemantic();
        result.setCode(CodeEnum.answer.toString());
        if(lang.contains("翻译")||(StringUtils.isNotBlank(xunfei.getFlag())&&"translation".equalsIgnoreCase(xunfei.getFlag()))) {
            result.setCode(CodeEnum.translate.toString());
            lang = lang.replace("翻译","");
            if (StringUtils.isNotBlank(lang)) {
                ro1.setText(TranslateBing.doTranslate(xunfei.getText()));
            }else{
                ro1.setText("请问您要翻译什么？使用翻译功能，需要加上您所要翻译的内容哦");
            }
            result.setAnswer(ro1);
        } else if(StringUtils.isNotEmpty(xunfei.getFlag())) {
            ro1.setText(xunfei.getText());
            this.secondJuge(xunfei,result, ro1, lang);
            result.setAnswer(ro1);
        } else if ("ON".equals(ifMachine)) {
            underStandResult.setFlag("OFF");
        } else {
            AnswerSemantic answerSemantic = new AnswerSemantic();
            answerSemantic.setText("对不起，我没能理解你的话，我会尽快学习，试试其它的吧");
            result.setAnswer(answerSemantic);
            result.setCode(CodeEnum.answer.toString());
        }
    }

    //应答结果合成（含翻译）
    private void composeAnswer(String lang,UnderstandModel result,UnderStandResult underStandResult,XunfeiModel xunfei) throws Exception {
        AnswerSemantic ro1 = new AnswerSemantic();
        result.setCode(CodeEnum.answer.toString());
        if(lang.contains("翻译")) {
            result.setCode(CodeEnum.translate.toString());
            String lang1 = lang.replace("翻译", "");
            if (StringUtils.isNotBlank(lang1)) {
                ro1.setText(TranslateBing.doTranslate(lang1));
            }else{
                result.setText("请问您要翻译什么？使用翻译口令需要加上您所要翻译的内容哦");
            }
            result.setAnswer(ro1);
        } else if(StringUtils.isNotEmpty(xunfei.getFlag())) {
            ro1.setText(xunfei.getText());
            this.secondJuge(xunfei,result, ro1, lang);
            result.setAnswer(ro1);
        } else {
            underStandResult.setFlag("OFF");
        }
    }

    //音乐结果合成-国际版
    private void composeMusicNation(String lang,String lang1,UnderstandModel result,UnderStandResult underStandResult) throws Exception {
        MusicSemantic list1 = new MusicSemantic();
        if(StringUtils.isNotBlank(Context.fetchNationGerne(lang1))) {
            result.setCode(CodeEnum.music_genre.toString());
            list1.setGenre(Context.fetchNationGerne(lang1).replace("_", " "));
            result.setMusicSemantic(list1);
        } else if(StringUtils.isNotBlank(Context.fetchNationScenarios(lang1))) {
            result.setCode(CodeEnum.music_genre.toString());
            list1.setGenre(Context.fetchNationScenarios(lang1).replace("_", " "));
            result.setMusicSemantic(list1);
        } else if(null != Context.fetchNationMood(lang1) && Context.fetchNationMood(lang1).size() > 0) {
            result.setCode(CodeEnum.music_mood.toString());
            result.setMoodSongs(Context.fetchNationMood(lang1));
            underStandResult.setFlag("ON");
        } else {
            List list2 = this.natureLangSerivce.getNationFinalResult(lang);
            if(!list2.isEmpty() && this.filterObj((MusicSemantic)list2.get(0))) {
                MusicSemantic semantic = (MusicSemantic)list2.get(0);
                this.getUnderStandCode(result, semantic);
                this.filterNationSematic(semantic,lang);
                result.setMusicSemantic(semantic);
            } else {
                underStandResult.setFlag("OFF");
            }
        }
    }

}
