package im.vinci.server.naturelang.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.TypeReference;
import im.vinci.server.naturelang.domain.Genre;
import im.vinci.server.naturelang.domain.Mood;
import im.vinci.server.naturelang.domain.OssMusicSong;
import im.vinci.server.naturelang.service.impl.process.ElasticHandler;
import im.vinci.server.naturelang.utils.ObjectUtils;
import im.vinci.server.search.domain.himalayas.HimalayaAlbum;
import im.vinci.server.search.domain.music.MusicSong;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class Context {
    private static final HashMap<String, Genre> genres = new HashMap<>();
    private static final HashMap<String, OssMusicSong> ossMusics = new HashMap<>();
    private static final HashMap<String, HimalayaAlbum> ossHimalaya = new HashMap<>();
    private static final Map<String, CaseInsensitiveMap> ctx = new HashMap<>();
    private static final Map<String, List<String>> whitewords = new HashMap<>();
    private static List<Mood> moods;

    public Context() {
    }

    public static void init() throws Exception {
        loadGenre();
        loadWhiteWords();
        loadXiamiRank();
        loadXiamiFilterConfig();
        loadHimaFilterConfig();
        loadRank();
        loadStop();
        loadNationGenre1();
        loadNationMachine();
        loadNationScenarios();
        loadNationPrefix();
        loadNationMood();
        loadSpoken();
        loadNatioPref();
        getAllSingers();
        loadChatWhiteList();
        loadPromotion();
        loadOss();
        loadMusicElements();
        loadDownloadMusicElements();
        loadXunfeiElements();
        loadHimalaya();
        loadPrefixInstruct();
        loadSuffixInstruct();
    }

    public static Genre getGenres(String title) {
        return (Genre) genres.get(title);
    }

    public static void loadXiamiRank() throws Exception {
        CaseInsensitiveMap result = new CaseInsensitiveMap();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/xiamirank.prop")).getInputStream(), "utf8")));
        Iterator var2 = properties.keySet().iterator();

        while (var2.hasNext()) {
            Object str = var2.next();
            result.put(str + "", properties.getProperty(str + ""));
        }

        ctx.put("xiamirank", result);
    }

    public static String getXiamiRankByType(String string) throws Exception {
        return !StringUtils.isNotBlank(string) ? "music_all" : ((CaseInsensitiveMap) ctx.get("xiamirank")).getOrDefault(string, "music_all") + "";
    }

    public static void loadWhiteWords() throws IOException {
        CaseInsensitiveMap result = new CaseInsensitiveMap();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/whitewords.prop")).getInputStream(), "utf8")));
        Iterator var2 = properties.keySet().iterator();

        while (var2.hasNext()) {
            Object str = var2.next();
            result.put(str + "", "");
        }
        ctx.put("whitewords", result);
    }

    public static boolean ifWhiteWords(String name) {
        boolean flag = false;
        CaseInsensitiveMap result = ctx.get("whitewords");
        Iterator iterator = result.keySet().iterator();
        String temp = "";
        while (iterator.hasNext()) {
            temp =  (String)iterator.next();
            if (name.equals(temp)) {
                flag = true;
                break;
            }
        }
        return flag;
    }


    public static void loadGenre() throws IOException {
        JSONReader reader = new JSONReader(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/localRecommend.json")).getInputStream(), "utf8")));
        List<Genre> list = JSON.parseObject(reader.readString(), new TypeReference<List<Genre>>() {
        });
        Iterator var2 = list.iterator();

        while (var2.hasNext()) {
            Genre genre = (Genre) var2.next();
            genres.put(genre.getPlaylistname(), genre);
        }
    }

    public static void loadOss() throws IOException {
        JSONReader reader = new JSONReader(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/oss.json")).getInputStream(), "utf8")));
        List<OssMusicSong> list = JSON.parseObject(reader.readString(), new TypeReference<List<OssMusicSong>>() {
        });
        Iterator var2 = list.iterator();

        while (var2.hasNext()) {
            OssMusicSong ossMusicSong = (OssMusicSong) var2.next();
            ossMusics.put(ossMusicSong.getCatalog(), ossMusicSong);
        }
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

    /**
     *
     * @return
     */
    public static List<MusicSong> getWhiteNoise() {
        List<MusicSong> ossMusicSong = ossMusics.get("whitenoise").getMusics();
        Collections.shuffle(ossMusicSong);
        return ossMusicSong;
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
     * @apiNote 加载喜马拉雅外语资源
     * */
    public static void loadHimalaya() throws IOException {
        JSONReader reader = new JSONReader(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/himalaya.json")).getInputStream(), "utf8")));
        List<HimalayaAlbum> list = JSON.parseObject(reader.readString(), new TypeReference<List<HimalayaAlbum>>() {
        });
        Iterator var2 = list.iterator();

        while (var2.hasNext()) {
            HimalayaAlbum ossHima = (HimalayaAlbum) var2.next();
            ossHimalaya.put(ossHima.getId()+"", ossHima);
        }
    }

    /**
     * @param id  oss中的歌曲id
     * 通过歌曲id，获取对应的曲库信息
     * */
    public static MusicSong getSongByIdInOss(Long id) {
        Iterator<OssMusicSong> iterator = ossMusics.values().iterator();
        while (iterator.hasNext()) {
            List<MusicSong> musicSongList = iterator.next().getMusics();
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
            return new ArrayList<>();
        }

    }


    public static List<MusicSong> getGenreName(String name) {
        return ((Genre) genres.get(name)).getMusics();
    }

    public static void getAllSingers() throws IOException {
        CaseInsensitiveMap result = new CaseInsensitiveMap();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/xiami_singer.dic")).getInputStream(), "utf8")));
        Iterator var2 = properties.keySet().iterator();

        while (var2.hasNext()) {
            Object str = var2.next();
            result.put(str + "", properties.getProperty(str + ""));
        }
        ctx.put("allSingers", result);
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
        Map properties = ctx.get("allSingers");
        Set<String> set = properties.keySet();
        for (String str : set) {
            if (str.matches("[a-zA-Z_ ]+")&&lang.matches("[a-zA-Z0-9 ]+[的]{0,1}[a-zA-Z0-9 ]+[的歌]{0,1}")||lang.matches("[a-zA-Z_ ]+")) {
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
        Map properties = ctx.get("allSingers");
        if (StringUtils.isEmpty(str)) {
            return false;
        } else if(str.matches("[a-zA-Z_]+")){
                if (properties.containsKey(str)) {
                    return true;
                }
        } else{
            Set<String> set = properties.keySet();
           /* for (String temp : set) {
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

    public static Properties getAllMachineInstructor() throws IOException {
        new CaseInsensitiveMap();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/instruct.dic")).getInputStream(), "utf8")));
        return properties;
    }

    public static boolean IfMachineInstruct(String str) throws IOException {
        Properties properties = getAllMachineInstructor();
        Set set = properties.keySet();
        Iterator var3 = set.iterator();

        String obj1;
        do {
            if (!var3.hasNext()) {
                return false;
            }

            Object obj = var3.next();
            obj1 = obj + "";
        } while (!obj1.contains(str) && !str.contains(obj1));

        return true;
    }

    public static Properties getAllXMLYJudge() throws IOException {
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/judge.dic")).getInputStream(), "utf8")));
        return properties;
    }

    public static boolean IfXMLYJudege(String str) throws IOException {
        Properties properties = getAllXMLYJudge();
        Set set = properties.keySet();
        Iterator var3 = set.iterator();

        String obj1;
        do {
            if (!var3.hasNext()) {
                return false;
            }

            Object obj = var3.next();
            obj1 = obj + "";
        } while (!obj1.contains(str) && !str.contains(obj1));

        return true;
    }

    public static void loadXiamiFilterConfig() throws IOException {
        CaseInsensitiveMap result = new CaseInsensitiveMap();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/xiamifilters.prop")).getInputStream(), "utf8")));
        Iterator var2 = properties.keySet().iterator();

        while (var2.hasNext()) {
            Object str = var2.next();
            result.put(str + "", properties.getProperty(str + ""));
        }

        ctx.put("xiamifilters", result);
    }

    public static String filterXiamiLang(String lang) throws IOException {
        if (StringUtils.isEmpty(lang)) {
            return "";
        } else {
            CaseInsensitiveMap properties = (CaseInsensitiveMap) ctx.get("xiamifilters");
            Set objects = properties.keySet();
            Iterator var3 = objects.iterator();

            while (var3.hasNext()) {
                Object obj = var3.next();
                String temp = obj + "";
                if (lang.contains(obj + "")) {
                    lang = lang.replace(temp, properties.get(temp) + " ");
                }
            }

            return lang;
        }
    }

    public static void loadHimaFilterConfig() throws IOException {
        CaseInsensitiveMap result = new CaseInsensitiveMap();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/xmlyfilters.prop")).getInputStream(), "utf8")));
        Iterator var2 = properties.keySet().iterator();

        while (var2.hasNext()) {
            Object str = var2.next();
            result.put(str + "", properties.getProperty(str + ""));
        }

        ctx.put("himafilters", result);
    }

    public static String filterHimaLang(String lang) throws IOException {
        if (StringUtils.isEmpty(lang)) {
            return "";
        } else {
            Map properties = (Map) ctx.get("himafilters");
            Set objects = properties.keySet();
            Iterator var3 = objects.iterator();

            while (var3.hasNext()) {
                Object obj = var3.next();
                String temp = obj + "";
                if (lang.contains(obj + "")) {
                    lang = lang.replace(temp, properties.get(temp) + "");
                }
            }

            return lang;
        }
    }

    public static void loadRank() throws IOException {
        CaseInsensitiveMap result = new CaseInsensitiveMap();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/rank.prop")).getInputStream(), "utf8")));
        Iterator var2 = properties.keySet().iterator();

        while (var2.hasNext()) {
            Object str = var2.next();
            result.put(str + "", properties.getProperty(str + ""));
        }

        ctx.put("rank", result);
    }

    public static boolean ifRank(String lang) {
        if (StringUtils.isEmpty(lang)) {
            return false;
        } else {
            CaseInsensitiveMap properties = (CaseInsensitiveMap) ctx.get("rank");
            Set objects = properties.keySet();
            Iterator var3 = objects.iterator();

            Object obj;
            do {
                if (!var3.hasNext()) {
                    return false;
                }

                obj = var3.next();
                String temp = obj + "";
            } while (!lang.contains(obj + ""));

            return true;
        }
    }

    public static String getRankByKey(List<String> lang) throws IOException {
        if (CollectionUtils.isEmpty(lang)) {
            return "";
        } else {
            CaseInsensitiveMap properties = (CaseInsensitiveMap) ctx.get("rank");
            Iterator var2 = lang.iterator();

            String obj;
            do {
                if (!var2.hasNext()) {
                    return "";
                }

                obj = (String) var2.next();
            }
            while (!ObjectUtils.isNotEmperty(properties.get(obj)) || !StringUtils.isNotBlank(properties.get(obj) + ""));

            return properties.get(obj) + "";
        }
    }

    public static void loadStop() throws UnsupportedEncodingException, IOException {
        CaseInsensitiveMap result = new CaseInsensitiveMap();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/stopword.dic")).getInputStream(), "utf8")));
        Iterator var2 = properties.keySet().iterator();

        while (var2.hasNext()) {
            Object str = var2.next();
            result.put(str + "", properties.getProperty(str + ""));
        }

        ctx.put("stop", result);
    }

    public static boolean ifStopWord(String word) throws IOException {
        if (StringUtils.isEmpty(word)) {
            return false;
        } else {
            CaseInsensitiveMap properties = (CaseInsensitiveMap) ctx.get("stop");
            return StringUtils.isNotBlank(properties.get(word) + "");
        }
    }

    public static void loadNationGenre1() throws Exception {
        CaseInsensitiveMap result = new CaseInsensitiveMap();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nation/genre.prop")).getInputStream(), "utf8")));
        Iterator var2 = properties.keySet().iterator();

        while (var2.hasNext()) {
            Object str = var2.next();
            result.put(str + "", properties.getProperty(str + ""));
        }

        ctx.put("nation_genre1", result);
    }

    public static void loadNationMachine() throws Exception {
        CaseInsensitiveMap result = new CaseInsensitiveMap();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nation/machine.prop")).getInputStream(), "utf8")));
        Iterator var2 = properties.keySet().iterator();

        while (var2.hasNext()) {
            Object str = var2.next();
            result.put(str + "", properties.getProperty(str + ""));
        }

        ctx.put("nation_machine", result);
    }

    public static void loadNationScenarios() throws Exception {
        CaseInsensitiveMap result = new CaseInsensitiveMap();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nation/scenarios.prop")).getInputStream(), "utf8")));
        Iterator var2 = properties.keySet().iterator();

        while (var2.hasNext()) {
            Object str = var2.next();
            result.put(str + "", properties.getProperty(str + ""));
        }

        ctx.put("nation_scenarios", result);
    }

    public static void loadNationPrefix() throws Exception {
        CaseInsensitiveMap result = new CaseInsensitiveMap();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nation/prefix.prop")).getInputStream(), "utf8")));
        Iterator var2 = properties.keySet().iterator();

        while (var2.hasNext()) {
            Object str = var2.next();
            result.put(str + "", properties.getProperty(str + ""));
        }

        ctx.put("nation_prefix", result);
    }

    public static void loadSpoken() throws Exception {
        CaseInsensitiveMap result = new CaseInsensitiveMap();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/spoken.prop")).getInputStream(), "utf8")));
        Iterator var2 = properties.keySet().iterator();

        while (var2.hasNext()) {
            Object str = var2.next();
            result.put(str + "", properties.getProperty(str + ""));
        }

        ctx.put("spoken", result);
    }

    public static void loadNationMood() throws Exception {
        JSONReader reader = new JSONReader(new BufferedReader(new InputStreamReader((new ClassPathResource("nation/mood.json")).getInputStream(), "utf8")));
        List<Mood> moods1 = JSON.parseObject(reader.readString(), new TypeReference<List<Mood>>() {
        });
        Iterator var2 = moods1.iterator();

        while (var2.hasNext()) {
            Mood mood = (Mood) var2.next();
            mood.setMoods(Arrays.asList(mood.getKeywords().split(";")));
            mood.setSongList(Arrays.asList(mood.getSongs().split(";")));
        }

        moods = moods1;
    }

    public static List fetchNationMood(String lang) throws Exception {
        if (!StringUtils.isNotBlank(lang)) {
            return null;
        } else {
            Iterator var1 = moods.iterator();

            Mood mood;
            do {
                if (!var1.hasNext()) {
                    return null;
                }

                mood = (Mood) var1.next();
            } while (!mood.getKeywords().replace(" ", "").toLowerCase().contains(lang.toLowerCase()));

            return mood.getSongList();
        }
    }

    public static String fetchNationScenarios(String lang) throws Exception {
        ArrayList list = (new ElasticHandler()).cut(lang, true);
        CaseInsensitiveMap result = (CaseInsensitiveMap) ctx.get("nation_scenarios");
        Set set = result.keySet();
        Iterator var4 = list.iterator();

        String str;
        do {
            if (!var4.hasNext()) {
                return "";
            }

            str = (String) var4.next();
        } while (!result.containsKey(str));

        return result.get(str) + "";
    }

    public static String fetchNationMachine(String lang) throws Exception {
        ArrayList list = (new ElasticHandler()).cut(lang, true);
        CaseInsensitiveMap result = (CaseInsensitiveMap) ctx.get("nation_machine");
        Set set = result.keySet();
        Iterator var4 = list.iterator();

        String str;
        do {
            if (!var4.hasNext()) {
                return "";
            }

            str = (String) var4.next();
        } while (!result.containsKey(str));

        return result.get(str) + "";
    }

    public static String fetchNationGerne(String lang) throws Exception {
        ArrayList list = (new ElasticHandler()).cut(lang, true);
        CaseInsensitiveMap result = (CaseInsensitiveMap) ctx.get("nation_genre1");
        Set set = result.keySet();
        Iterator var4 = list.iterator();

        String str;
        do {
            if (!var4.hasNext()) {
                return "";
            }

            str = (String) var4.next();
        } while (!result.containsKey(str));

        return result.get(str) + "";
    }

    public static String fetchSpoken(String lang) throws Exception {
        ArrayList list = (new ElasticHandler()).cut(lang, true);
        CaseInsensitiveMap result = (CaseInsensitiveMap) ctx.get("spoken");
        Set set = result.keySet();
        Iterator var4 = list.iterator();

        String str;
        do {
            if (!var4.hasNext()) {
                return "";
            }

            str = (String) var4.next();
        } while (!result.containsKey(str));

        return result.get(str) + "";
    }

    public static boolean ifSpoken(String lang) throws Exception {
        ArrayList list = (new ElasticHandler()).cut(lang, true);
        CaseInsensitiveMap result = (CaseInsensitiveMap) ctx.get("spoken");
        Set set = result.keySet();
        Iterator var4 = list.iterator();

        String str;
        do {
            if (!var4.hasNext()) {
                return false;
            }

            str = (String) var4.next();
        } while (!result.containsKey(str));

        return true;
    }

    public static void loadNatioPref() throws Exception {
        CaseInsensitiveMap result = new CaseInsensitiveMap();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nation/prefix.prop")).getInputStream(), "utf8")));
        Iterator var2 = properties.keySet().iterator();

        while (var2.hasNext()) {
            Object str = var2.next();
            result.put(str + "", properties.getProperty(str + ""));
        }

        ctx.put("nation_prefix", result);
    }

    public static String filterNationPref(String enLang) throws IOException {
        CaseInsensitiveMap result = (CaseInsensitiveMap) ctx.get("nation_prefix");
        ArrayList list = (new ElasticHandler()).cut(enLang, true);
        Iterator var3 = list.iterator();

        while (var3.hasNext()) {
            String str = (String) var3.next();
            if (result.containsKey(str)) {
                enLang = enLang.replace(str, "");
            }
        }

        return enLang;
    }


    public static void loadChatWhiteList() throws Exception {
        CaseInsensitiveMap result = new CaseInsensitiveMap();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/writelist.prop")).getInputStream(), "utf8")));
        Iterator var2 = properties.keySet().iterator();

        while (var2.hasNext()) {
            Object str = var2.next();
            result.put(str + "", properties.getProperty(str + ""));
        }

        ctx.put("whitelist", result);
    }

    public static Boolean ifWhite(String lang) throws IOException {
        boolean flag = false;
        CaseInsensitiveMap result = (CaseInsensitiveMap) ctx.get("whitelist");
        ArrayList list = (new ElasticHandler()).cut(lang, true);
        Iterator var3 = list.iterator();

        while (var3.hasNext()) {
            String str = (String) var3.next();
            if (result.containsKey(str)) {
                flag = true;
                break;
            }
        }

        return flag;
    }


    public static void loadPromotion() throws Exception {
        CaseInsensitiveMap result = new CaseInsensitiveMap();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/promotion.dic")).getInputStream(), "utf8")));
        Iterator var2 = properties.keySet().iterator();

        while (var2.hasNext()) {
            Object str = var2.next();
            result.put(str + "", properties.getProperty(str + ""));
        }

        ctx.put("promotion", result);
    }

    //判断用户的意图是否为音乐点播
    public static void loadMusicElements() throws Exception {
        CaseInsensitiveMap result = new CaseInsensitiveMap();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/music.prop")).getInputStream(), "utf8")));
        Iterator var2 = properties.keySet().iterator();

        while (var2.hasNext()) {
            Object str = var2.next();
            result.put(str + "", properties.getProperty(str + ""));
        }

        ctx.put("music", result);
    }


    //判断用户的意图是否为音乐下载
    public static void loadDownloadMusicElements() throws Exception {
        CaseInsensitiveMap result = new CaseInsensitiveMap();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/prefix_download_music.dic")).getInputStream(), "utf8")));
        Iterator var2 = properties.keySet().iterator();

        while (var2.hasNext()) {
            Object str = var2.next();
            result.put(str + "", properties.getProperty(str + ""));
        }

        ctx.put("download_music", result);
    }

    /**
     * //判断用户的意图是否为音乐点播
     * @param lang
     * @return 1:music 2:download other:none
     * @throws Exception
     */
    public static int ifMusicInstruct(String lang) throws Exception{
        int flag = 0;//默认为非音乐指令信息
        CaseInsensitiveMap map = ctx.get("music");
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
        map = ctx.get("download_music");
        set = map.keySet();
        for (String str : set) {
            if (lang.matches(str)){
                flag = 2;
                break;
            }
        }
        return flag;
    }



    //判断用户意图是否为询问、聊天
    public static void loadXunfeiElements() throws Exception {
        CaseInsensitiveMap result = new CaseInsensitiveMap();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/xunfei.prop")).getInputStream(), "utf8")));
        Iterator var2 = properties.keySet().iterator();

        while (var2.hasNext()) {
            Object str = var2.next();
            result.put(str + "", properties.getProperty(str + ""));
        }

        ctx.put("xunfei", result);
    }

    /**
     * 判断用户意图是否为询问、聊天
     * @param type 业务领域，当前为xunfei和music
     * @return boolean
     *
     * */
    public static boolean ifXunfeiOrMusic(String type,String lang) {
        boolean flag = false;
        CaseInsensitiveMap result =  ctx.get(type);
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

    //加载前缀口语词
    public static void loadPrefixInstruct() throws IOException {
        CaseInsensitiveMap result = new CaseInsensitiveMap();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/prefix_instruct.dic")).getInputStream(), "utf8")));
        Iterator var2 = properties.keySet().iterator();

        while (var2.hasNext()) {
            Object str = var2.next();
            result.put(str + "", properties.getProperty(str + ""));
        }
        ctx.put("prefixInstruct", result);
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

            Map properties = ctx.get("prefixInstruct");
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


    //加载后缀口语词
    public static void loadSuffixInstruct() throws IOException {
        CaseInsensitiveMap result = new CaseInsensitiveMap();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/suffix_instruct.dic")).getInputStream(), "utf8")));
        Iterator var2 = properties.keySet().iterator();

        while (var2.hasNext()) {
            Object str = var2.next();
            result.put(str + "", properties.getProperty(str + ""));
        }
        ctx.put("suffixInstruct", result);
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

            Map properties = ctx.get("suffixInstruct");
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


}
