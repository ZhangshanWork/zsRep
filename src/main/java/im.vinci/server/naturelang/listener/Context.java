package im.vinci.server.naturelang.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import im.vinci.server.naturelang.domain.Genre;
import im.vinci.server.naturelang.domain.Mood;
import im.vinci.server.naturelang.domain.OssMusicSong;
import im.vinci.server.naturelang.service.impl.process.ElasticHandler;
import im.vinci.server.naturelang.utils.ObjectUtils;
import im.vinci.server.search.domain.himalayas.HimalayaAlbum;
import im.vinci.server.search.domain.music.MusicSong;
import im.vinci.server.utils.StringContentUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Context {
    private static final HashMap<String, Genre> genres = new HashMap<>();
    private static final HashMap<String, OssMusicSong> ossMusics = new HashMap<>();
    private static final HashMap<String, HimalayaAlbum> ossHimalaya = new HashMap<>();
    private static List<Mood> moods;
    private static final Map<String, CaseInsensitiveMap<String,String>> ctx = new HashMap<>();
    public Context() {
    }

    public static void init() throws Exception {
        loadPropConfigFile(
                "whitewords","nlp/whitewords.prop",
                "whitelist","nlp/writelist.prop",
                "xiamirank","nlp/xiamirank.prop",
                "allSingers","nlp/xiami_singer.dic",
                "xiamifilters","nlp/xiamifilters.prop",
                "himafilters","nlp/xmlyfilters.prop",
                "rank","nlp/rank.prop",
                "stop","nlp/stopword.dic",
                "spoken","nlp/spoken.prop",
                "nation_prefix","nation/prefix.prop",
                "nation_scenarios","nation/scenarios.prop",
                "nation_music_purpose_postfix", "nation/music_purpose_postfix.properties",
                "nation_machine","nation/machine.prop",
                "nation_genre","nation/genre.prop",
                "nation_music_play_purpose_prefix", "nation/music_purpose_prefix.properties",
                "download_music","nlp/prefix_download_music.dic",
                "music","nlp/music.prop",
                "promotion","nlp/promotion.dic",
                "suffixInstruct","nlp/suffix_instruct.dic", //加载后缀口语词
                "prefixInstruct","nlp/prefix_instruct.dic", //加载前缀口语词
                "xunfei","nlp/xunfei.prop",     //判断用户意图是否为询问、聊天
                "AllMachineInstructor","nlp/instruct.dic",
                "AllXMLYJudge","nlp/judge.dic",
                "regex","nlp/regex.prop"        //汇集各类正则，进行过滤
                );
        loadGenre();
        loadNationMood();
        loadOss();
        loadHimalaya();

        processMusicPostfix("nation_scenarios");
        processMusicPostfix("nation_genre");
    }

    private static void processMusicPostfix(String keyFile) {
        CaseInsensitiveMap<String,String> scene = ctx.get(keyFile);
        CaseInsensitiveMap<String,String> music_purpose_postfix = ctx.get("nation_music_purpose_postfix");
        CaseInsensitiveMap<String,String> result = new CaseInsensitiveMap<>();
        for (String word : scene.keySet()) {
            StrSubstitutor strSubstitutor = new StrSubstitutor(ImmutableMap.of("keyword",word));
            String sceneName = scene.get(word);
            for (String postfix : music_purpose_postfix.values()) {
                String value = StringUtils.deleteWhitespace(strSubstitutor.replace(postfix));
                result.put(value,sceneName);
            }
        }
        ctx.put(keyFile,result);
    }


    private static void loadPropConfigFile(String... args) throws Exception{
        if (args == null || args.length %2 != 0) {
            throw new RuntimeException("load nlp基础数据参数输入错误,参数要是2的倍数");
        }
        for (int i=0; i<args.length; i+=2) {
            String key = args[i];
            String fileName = args[i+1];
            CaseInsensitiveMap<String,String> result = new CaseInsensitiveMap<>();
            Properties properties = new Properties();
            properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource(fileName)).getInputStream(), "utf8")));

            for (String str : properties.stringPropertyNames()) {
                result.put(str, properties.getProperty(str,""));
            }

            ctx.put(key, result);
        }
    }

    public static Genre getGenres(String title) {
        return genres.get(title);
    }


    public static String getXiamiRankByType(String string) throws Exception {
        return !StringUtils.isNotBlank(string) ? "music_all" : ctx.get("xiamirank").getOrDefault(string, "music_all");
    }

    public static boolean ifWhiteWords(String name) {
        boolean flag = false;
        CaseInsensitiveMap<String,String> result = ctx.get("whitewords");
        Iterator iterator = result.keySet().iterator();
        String temp;
        while (iterator.hasNext()) {
            temp =  (String)iterator.next();
            if (name.equals(temp)) {
                flag = true;
                break;
            }
        }
        return flag;
    }



    /**
     * 定时定向推送
     * @return
     */
    public static List<String> generateRecmmdList() {
        List<String> ids = new ArrayList<>();
        List<MusicSong> ossMusicSong = ossMusics.get("recomd").getMusics();
        List<MusicSong> ximalaya = ossMusics.get("ximalaya").getMusics();
        Collections.shuffle(ossMusicSong);
        for(int i=0;i<7;i++) {
            ids.add(ossMusicSong.get(i).getSong_id()+"_xiami");
        }

        Collections.shuffle(ximalaya);
        for(int i=0;i<2;i++) {
            ids.add(ximalaya.get(i).getSong_id()+"_xiami");
        }

        Collections.shuffle(ids);
        return ids;
    }


   /* *//**
     * 判断当前歌手是否包含在oss中
     * *//*
    public static  List<MusicSong> ifSingerInOss(String singer) {
        if (null != ossMusics.get(singer)) {
            return ossMusics.get(singer).getMusics();
        } else {
            return new ArrayList<>();
        }
    }*/


    /**
     * @param id  oss中的歌曲id
     * 通过歌曲id，获取对应的曲库信息
     * */
    public static MusicSong getSongByIdInOss(Long id) {
        for (OssMusicSong ossMusicSong : ossMusics.values()) {
            List<MusicSong> musicSongList = ossMusicSong.getMusics();
            for (MusicSong temp : musicSongList) {
                if (id == temp.getSong_id()) {
                    return temp;
                }
            }
        }
        return new MusicSong("xiami");
    }


    public static HimalayaAlbum getOssHimalaya(String id) {
        return ossHimalaya.get(id);
    }


    public static List<MusicSong> getOssMusicSong(String catalog) {
        if (null != ossMusics.get(catalog)) {
            return ossMusics.get(catalog).getMusics();
        } else {
            return Collections.emptyList();
        }

    }


    public static List<MusicSong> getGenreName(String name) {
        return (genres.get(name)).getMusics();
    }



    public static boolean IfSinger(String str) throws IOException {
        if (StringUtils.isEmpty(str)) {
            return false;
        } else {
            if (str.matches("[a-zA-Z ]+")) {
                str = str.trim().trim().replace(" ","_");
            }
            Map properties = ctx.get("allSingers");
            return properties.containsKey(str);
        }
    }

    //提取lang中的歌手信息
    public static String getSingerInlang(String lang) {
        lang = lang.trim();
        List<String> list = new ArrayList<>();
        CaseInsensitiveMap<String,String> properties = ctx.get("allSingers");
        Set<String> set = properties.keySet();
        for (String str : set) {
            if (str.matches("[a-zA-Z_ ]+")&&lang.matches("[a-zA-Z0-9 ]+[的]?[a-zA-Z0-9 ]+[的歌]?")||lang.matches("[a-zA-Z_ ]+")) {
                lang = lang.replace("的歌","");
                if (lang.contains("的")) {
                    String[] array = lang.split("的");
                    lang = array[0].trim();
                }
                lang = lang.replace(" ", "_").toLowerCase();
                if (properties.containsKey(lang)) {
                    return lang;
                }
            }else{
                if (lang.startsWith(str)) {
                    list.add(str);
                }
            }
        }
        return chooseMaxLength(list);
    }


    public static boolean IfSingerInLang(String str) throws IOException {
        CaseInsensitiveMap<String,String> properties = ctx.get("allSingers");
        if (StringUtils.isEmpty(str)) {
            return false;
        } else if(str.matches("[a-zA-Z_]+")){
                if (properties.containsKey(str)) {
                    return true;
                }
        } else{
           /* Set<String> set = properties.keySet();
            for (String temp : set) {
                    if (str.contains(temp)) {
                            return true;
                    }
            }*/
            if (properties.containsKey(str)) {
                return true;
            }
        }
        return false;
    }


    public static boolean IfMachineInstruct(String str) throws IOException {

        CaseInsensitiveMap<String,String> properties = ctx.get("AllMachineInstructor");
        Iterator<String> var3 = properties.keySet().iterator();

        String obj;
        do {
            if (!var3.hasNext()) {
                return false;
            }
            obj = var3.next();
        } while (!obj.contains(str) && !str.contains(obj));

        return true;
    }

    public static boolean IfXMLYJudege(String str) throws IOException {
        CaseInsensitiveMap<String,String> properties = ctx.get("AllXMLYJudge");

        Iterator<String> var3 = properties.keySet().iterator();

        String obj;
        do {
            if (!var3.hasNext()) {
                return false;
            }
            obj = var3.next();
        } while (!obj.contains(str) && !str.contains(obj));

        return true;
    }



    public static String filterXiamiLang(String lang) throws IOException {
        if (StringUtils.isEmpty(lang)) {
            return "";
        } else {
            CaseInsensitiveMap<String,String> properties = ctx.get("xiamifilters");

            for (String obj : properties.keySet()) {
                if (lang.contains(obj)) {
                    lang = lang.replace(obj, properties.get(obj) + " ");
                }
            }

            return lang;
        }
    }


    public static String filterHimaLang(String lang) throws IOException {
        if (StringUtils.isEmpty(lang)) {
            return "";
        } else {
            CaseInsensitiveMap<String,String> properties = ctx.get("himafilters");

            for (String obj : properties.keySet()) {
                if (lang.contains(obj)) {
                    lang = lang.replace(obj, properties.get(obj));
                }
            }

            return lang;
        }
    }


    public static boolean ifRank(String lang) {
        if (StringUtils.isEmpty(lang)) {
            return false;
        } else {
            CaseInsensitiveMap<String,String> properties = ctx.get("rank");
            Iterator<String> var3 = properties.keySet().iterator();

            String obj;
            do {
                if (!var3.hasNext()) {
                    return false;
                }
                obj = var3.next();
            } while (!lang.contains(obj));

            return true;
        }
    }

    public static String getRankByKey(List<String> lang) throws IOException {
        if (CollectionUtils.isEmpty(lang)) {
            return "";
        } else {
            CaseInsensitiveMap<String,String> properties = ctx.get("rank");
            Iterator<String> var2 = lang.iterator();

            String obj;
            do {
                if (!var2.hasNext()) {
                    return "";
                }

                obj = var2.next();
            }
            while (!ObjectUtils.isNotEmperty(properties.get(obj)) || !StringUtils.isNotBlank(properties.get(obj)));

            return properties.get(obj);
        }
    }



    public static boolean ifStopWord(String word) throws IOException {
        if (StringUtils.isEmpty(word)) {
            return false;
        } else {
            CaseInsensitiveMap<String,String> properties = ctx.get("stop");
            return StringUtils.isNotBlank(properties.get(word));
        }
    }



    /**
     * 识别是否有play music类的前缀,并去掉前缀和改为小写
     * @return 有前缀返回后面的,没有前缀返回原字串的小写
     */
    private static Pair<Boolean,String> judgePlayMusicAndCutPrefixLowerCase(String lang) {
        CaseInsensitiveMap<String,String> musicPlayPrefix = ctx.get("nation_music_play_purpose_prefix");

        lang = lang.toLowerCase().trim();
        String bestPrefix = "";
        for (String prefix : musicPlayPrefix.values()) {
            if (lang.startsWith(prefix)) {
                if (prefix.length() > bestPrefix.length()) {
                    bestPrefix = prefix;
                }
            }
        }
        if (bestPrefix.length() > 0) {
            lang = lang.substring(bestPrefix.length()).trim();
            return Pair.of(true,lang);
        }
        return Pair.of(false,lang);
    }

    public static String fetchNationScenarios(String lang) throws Exception {
        lang = judgePlayMusicAndCutPrefixLowerCase(lang).getRight();

        ArrayList<String> list = (new ElasticHandler()).cut(StringUtils.deleteWhitespace(lang), true);
        CaseInsensitiveMap<String,String> result = ctx.get("nation_scenarios");
        Iterator<String> var4 = list.iterator();

        String str;
        do {
            if (!var4.hasNext()) {
                return "";
            }

            str = var4.next();
        } while (!result.containsKey(str));

        return StringUtils.replace(result.getOrDefault(str,""),"_","");
    }

    public static String fetchNationGenre(String lang) throws Exception {
        lang = judgePlayMusicAndCutPrefixLowerCase(lang).getRight();

        ArrayList<String> list = (new ElasticHandler()).cut(StringUtils.deleteWhitespace(lang), true);
        CaseInsensitiveMap<String,String> result = ctx.get("nation_genre");
        Iterator<String> var4 = list.iterator();

        String str;
        do {
            if (!var4.hasNext()) {
                return "";
            }

            str = var4.next();
        } while (!result.containsKey(str));

        return StringUtils.replace(result.getOrDefault(str,""),"_"," ");
    }

    public static Mood fetchNationMood(String lang) throws Exception {
        if (StringUtils.isBlank(lang)) {
            return null;
        }
        String playPurposeLang = judgePlayMusicAndCutPrefixLowerCase(lang).getRight();
        String langWithoutSpace = StringContentUtils.deleteWhiteSpaceAndOtherSignAndToLowerCase(lang);

        for (Mood mood : moods) {
            for (String key : mood.getSpecial_word().keySet()) {
                double score = StringUtils.getJaroWinklerDistance(key,langWithoutSpace);
                if (score > 0.9) {
                    Mood result = new Mood();
                    result.setMood(mood.getMood());
                    result.setAnswer(mood.getSpecial_word().get(key));
                    result.setSongs(mood.getSongs());
                    return result;
                }
            }
            if (StringUtils.isBlank(playPurposeLang)) {
                continue;
            }
            for (String key : mood.getKeywords()) {
                double score = StringUtils.getJaroWinklerDistance(key, playPurposeLang);
                if (score > 0.9) {
                    Mood result = new Mood();
                    result.setMood(mood.getMood());
                    result.setSongs(mood.getSongs());
                    return result;
                }
            }
        }

        return null;

//
//            Mood mood;
//            do {
//                if (!var1.hasNext()) {
//                    return Collections.emptyList();
//                }
//
//                mood = var1.next();
//            } while (!mood.getKeywords().replace(" ", "").toLowerCase().contains(lang.toLowerCase()));
//
//            return mood.getSongList();
    }


    public static String fetchNationMachine(String lang) throws Exception {
        CaseInsensitiveMap<String,String> result = ctx.get("nation_machine");
        return result.getOrDefault(StringContentUtils.deleteWhiteSpaceAndOtherSignAndToLowerCase(lang),"");
    }

    public static String fetchSpoken(String lang) throws Exception {
        ArrayList<String> list = (new ElasticHandler()).cut(lang, true);
        CaseInsensitiveMap<String,String> result = ctx.get("spoken");
        Iterator<String> var4 = list.iterator();

        String str;
        do {
            if (!var4.hasNext()) {
                return "";
            }

            str = var4.next();
        } while (!result.containsKey(str));

        return result.get(str);
    }

    public static boolean ifSpoken(String lang) throws Exception {
        ArrayList<String> list = (new ElasticHandler()).cut(lang, true);
        CaseInsensitiveMap<String,String> result = ctx.get("spoken");
        Iterator<String> var4 = list.iterator();

        String str;
        do {
            if (!var4.hasNext()) {
                return false;
            }

            str = var4.next();
        } while (!result.containsKey(str));

        return true;
    }

    public static String filterNationPref(String enLang) throws IOException {
        CaseInsensitiveMap<String,String> result = ctx.get("nation_prefix");
        ArrayList<String> list = (new ElasticHandler()).cut(enLang, true);

        for (String str : list) {
            if (result.containsKey(str)) {
                enLang = enLang.replace(str, "");
            }
        }

        return enLang;
    }




    public static Boolean ifWhite(String lang) throws IOException {
        boolean flag = false;
        CaseInsensitiveMap<String,String> result = ctx.get("whitelist");
        ArrayList<String> list = (new ElasticHandler()).cut(lang, true);

        for (String str : list) {
            if (result.containsKey(str)) {
                flag = true;
                break;
            }
        }

        return flag;
    }



    /**
     * //判断用户的意图是否为音乐点播
     * @param lang
     * @return 1:music 2:download other:none
     * @throws Exception
     */
    public static int ifMusicInstruct(String lang) throws Exception{
        int flag = 0;//默认为非音乐指令信息
        CaseInsensitiveMap<String,String> map = ctx.get("music");
        Set<String> set = map.keySet();
        for (String str : set) {
            if (lang.matches(str)){
                flag = 1;
                break;
            }
        }
        if (flag > 0) {
            return flag;
        }
        //暂时去掉download的理解
/*        map = ctx.get("download_music");
        set = map.keySet();
        for (String str : set) {
            if (lang.matches(str)){
                flag = 2;
                break;
            }
        }*/
        return flag;
    }


    /**
     * 判断用户意图是否为询问、聊天
     * @param type 业务领域，当前为xunfei和music
     * @return boolean
     *
     * */
    public static boolean ifXunfeiOrMusic(String type,String lang) {
        boolean flag = false;
        CaseInsensitiveMap<String,String> result =  ctx.get(type);
        if (result == null) {
            return false;
        }
        Set<String> set = result.keySet();
        for (String str : set) {
            if (lang.matches(str)) {
                flag = true;
                break;
            }
        }
        return flag;
    }



    public static String getPromotion() {
        CaseInsensitiveMap result = ctx.get("promotion");
        List list = Arrays.asList(result.keySet().toArray());
        int size = list.size();
        return list.get((int) (Math.random() * size)) + "";
    }


    //判定并消除前缀口语词
    public static String filterPrefixInstruct(String str) throws IOException {
        List<String> list = new ArrayList<>();
        if (StringUtils.isEmpty(str)) {
            return "";
        } else {
            if (str.matches("[a-zA-Z ]+")) {
                str = str.replace(" ","_");
            }

            CaseInsensitiveMap<String,String> properties = ctx.get("prefixInstruct");
            Set<String> set =  properties.keySet();
            for (String temp : set) {
                if (str.startsWith(temp)) {
                    list.add(temp);
                }
            }

            if (list.size() == 1) {
                str = str.replaceFirst(list.get(0),"");
            }else if(list.size() > 1){
                str = str.replaceFirst(chooseMaxLength(list),"");
            }
            return str;
        }
    }

    //选择排序
    public static String chooseMaxLength(List<String> list) {
        String result = "";
        for (String str : list) {
            if (str.length() > result.length()) {
                result = str;
            }
        }
        return result;
    }



    //判定并消除后缀口语词
    public static String filterSuffixInstruct(String str) throws IOException {
        String bak = str;
        List<String> list = new ArrayList<>();
        if (StringUtils.isEmpty(str)) {
            return "";
        } else {
            if (str.matches("[a-zA-Z ]+")) {
                str = str.replace(" ","_");
            }

            CaseInsensitiveMap<String,String> properties = ctx.get("suffixInstruct");
            Set<String> set =  properties.keySet();
            for (String temp : set) {
                if (str.endsWith(temp)) {
                    list.add(temp);
                }
            }

            if (list.size() == 1) {
                str = str.replaceFirst(list.get(0),"");
            }else if(list.size() > 1){
                str = chooseMaxLength(list);
                bak = bak.replace(str, "");
            }
            if (IfSinger(str)) {
                return str;
            }else{
                return bak;
            }
        }
    }

    /**
     * 清除用户口语词
     *
     * @param lang 用户口语输入
     * @return string
     */
    public static String filterLang(String lang) throws IOException {
        lang = filterPrefixInstruct(lang);
        lang = filterSuffixInstruct(lang);
        return lang;
    }

    /**
     *
     * @return
     */
    public static List<MusicSong> getWhiteNoise() {
        List<MusicSong> ossMusicSong = ossMusics.get("whitenoise").getMusics();
        Collections.shuffle(ossMusicSong);
        return ossMusicSong;
    }

    // ------------------------------------- private load config file -----------------------------

    /**
     * @apiNote 加载喜马拉雅外语资源
     * */
    private static void loadHimalaya() throws IOException {
        JSONReader reader = new JSONReader(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/himalaya.json")).getInputStream(), "utf8")));
        List<HimalayaAlbum> list = JSON.parseObject(reader.readString(), new TypeReference<List<HimalayaAlbum>>() {
        });

        for (HimalayaAlbum ossHima : list) {
            ossHimalaya.put(ossHima.getId() + "", ossHima);
        }
    }
    private static void loadGenre() throws IOException {
        JSONReader reader = new JSONReader(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/localRecommend.json")).getInputStream(), "utf8")));
        List<Genre> list = JSON.parseObject(reader.readString(), new TypeReference<List<Genre>>() {
        });

        for (Genre genre : list) {
            genres.put(genre.getPlaylistname(), genre);
        }
    }

    private static void loadOss() throws IOException {
        JSONReader reader = new JSONReader(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/oss.json")).getInputStream(), "utf8")));
        List<OssMusicSong> list = JSON.parseObject(reader.readString(), new TypeReference<List<OssMusicSong>>() {
        });

        for (OssMusicSong ossMusicSong : list) {
            ossMusics.put(ossMusicSong.getCatalog(), ossMusicSong);
        }
    }
    private static void loadNationMood() throws Exception {
        JSONReader reader = new JSONReader(new BufferedReader(new InputStreamReader((new ClassPathResource("nation/mood.json")).getInputStream(), "utf8")));
        moods = JSON.parseObject(reader.readString(), new TypeReference<List<Mood>>() {
        });

        CaseInsensitiveMap<String,String> music_purpose_postfix = ctx.get("nation_music_purpose_postfix");
        for (Mood mood : moods) {
            List<String> result = Lists.newArrayList();
            for (String word : mood.getKeywords()) {
                StrSubstitutor strSubstitutor = new StrSubstitutor(ImmutableMap.of("keyword", word));
                for (String postfix : music_purpose_postfix.values()) {
                    String value = StringUtils.deleteWhitespace(strSubstitutor.replace(postfix));
                    result.add(value);
                }
            }
            mood.setKeywords(result);

            CaseInsensitiveMap<String,String> sentence = new CaseInsensitiveMap<>();
            for (Map.Entry<String,String> entry: mood.getSpecial_word().entrySet()) {
                String question = entry.getKey();
                sentence.put(StringContentUtils.deleteWhiteSpaceAndOtherSignAndToLowerCase(question),entry.getValue());
            }
            mood.setSpecial_word(sentence);
        }
    }

    /**
     * 判断lang是否契合指定的regex（具体内容在regex.prop）
     * @param lang
     * @param regex
     * @return
     */
    public static boolean ifMatchRegex(String lang, String regex) {
        if(StringUtils.isNotBlank(matchRegexResult(lang, regex))){
            return true;
        }
        return false;
    }

    /**
     * 获取匹配的结果（具体内容在regex.prop）
     * @param lang
     * @param regex
     * @return
     */
    private static String matchRegexResult(String lang, String regex) {
        String result = "";
        CaseInsensitiveMap<String,String> map = ctx.get("regex");
        String expression = map.get(regex);
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(lang);
        while (matcher.find()) {
            result = matcher.group();
        }
        return result;
    }

}
