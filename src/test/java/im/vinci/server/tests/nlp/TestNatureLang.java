package im.vinci.server.tests.nlp;

import com.alibaba.fastjson.JSON;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.domain.*;
import com.taobao.api.request.*;
import com.taobao.api.response.*;
import im.vinci.server.naturelang.service.impl.process.ElasticHandler;
import im.vinci.server.naturelang.utils.SimilarityUtil;
import im.vinci.server.search.domain.himalayas.HimalayaAlbum;
import im.vinci.server.search.domain.himalayas.HimalayaTrack;
import im.vinci.server.search.domain.music.MusicSong;
import im.vinci.server.search.service.HimalayaSearchService;
import im.vinci.server.search.service.XiamiMusicSearchService;
import im.vinci.server.tests.utils.CreateFileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TemplateQueryBuilder;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestNatureLang {
	private static enum RecommendTrackCondition {
		hot,
		daily,
		recent,
		favorite
	}
	private  DefaultTaobaoClient client = new DefaultTaobaoClient("http://gw.api.taobao.com/router/rest"
            , "23064829", "29ed3de5990627239d0fdbddd3e94b51", "json", 2000, 5000);
	@Test
	public void testXiami() {
		List<MusicSong> lists = new XiamiMusicSearchService().searchSongsByKeyword("张惠妹", 0, 10).getData();
		for(MusicSong song:lists){
			System.out.println(song.getArtist()+" "+song.getSong_name()+" "+song.getAlbum_name()+" "+song.getTags());
		}
	}
	@Test

	public void testCut() throws IOException {
		ArrayList<String>  list = new ElasticHandler().cut("beyond的海阔天空", true);
		for(String str:list){
			System.out.println(str);
		}
	}

	@Test
	public void testHima(){
		List<HimalayaTrack> list  = new HimalayaSearchService().queryTrackByKeyword(-1, "" +
				"赵本山&范伟&高秀敏卖拐", 0, 10).getTracks();
		for(HimalayaTrack track:list){
			System.out.println(track.getTitle());
		}
	}

	@Test
	public void testHimaRecommend() {
		List<HimalayaTrack> tracks = new HimalayaSearchService().recommendTracksInCategory(1,"", RecommendTrackCondition.daily.toString(),0,10).getTracks();
		for (HimalayaTrack track : tracks) {
			System.out.println(track.getTitle());
		}
	}

	@Test
	public void testXiamiAPI() throws Exception {
		DefaultTaobaoClient client = new DefaultTaobaoClient("http://gw.api.taobao.com/router/rest"
				, "23064829", "29ed3de5990627239d0fdbddd3e94b51", "json", 2000, 5000);
		AlibabaXiamiApiRankSongsGetRequest req = new AlibabaXiamiApiRankSongsGetRequest();
		req.setType("jingge");
		AlibabaXiamiApiRankSongsGetResponse rsp = client.execute(req);
		System.out.println(rsp.getData().getSongs().size());
	}

	@Test
	public void testXiamiRank() throws Exception {
		List<MusicSong> list = new XiamiMusicSearchService().searchRankByType("music_all", 1, 10).getData();
		for(MusicSong song:list) {
			System.out.println(song.getSong_name());
		}
	}

	@Test
	public void testHimaAlbum() throws Exception {
		List<HimalayaAlbum> albums =  new HimalayaSearchService().queryAlbumByKeyword(-1, "英语四级", 1, 1).getAlbums();
        for(HimalayaAlbum album:albums) {
			System.out.println(album.getTitle() + "  " + album.getTrackCount());
        }
	}


    @Test
    public void testHimaAlbumDetail() throws Exception {
        HimalayaAlbum album =  new HimalayaSearchService().getAlbumById(3647110, 1,100).getAlbum();
            System.out.println(album.getTitle() + "  " + album.getTrackCount());
            List<HimalayaTrack> tracks = album.getTracks();
            for (HimalayaTrack track : tracks) {
                System.out.println(track.getTitle());
        }
    }

	@Test
	public void testReplaceQuote() throws Exception {
		String string = "岳云鹏精选集（清晰）";
		System.out.println(string.replaceAll("\\(.+?\\)",""));
		System.out.println(string.replaceAll("\\（.+?\\）",""));
		System.out.println(string);
	}

	@Test
    public void testProp() throws Exception{
   	 Properties properties = new Properties();
     properties.load(new BufferedReader(new InputStreamReader(new ClassPathResource("nlp/xiamirank.prop").getInputStream(),  "utf8")));
        Set<Object> set = properties.keySet();
        for(Object obj:set) {
            System.out.println((obj+" = ")+properties.getProperty(obj+""));
        }
   }

	/*@Test
	public void testXiamiJx() throws Exception{
		AlibabaXiamiApiSearchCollectsGetRequest req = new AlibabaXiamiApiSearchCollectsGetRequest();
		req.setLimit(10L);
		req.setPage(1L);
		req.setKey("要睡觉啦");
		req.setOrder("weight");
		AlibabaXiamiApiSearchCollectsGetResponse rsp = client.execute(req);
		List<StandardCollect> list = rsp.getData().getCollects();
		for(StandardCollect sc:list) {
			System.out.println(sc.getCollectName()+"  "+sc.getComments()+" "+sc.getDescription());
		}
	}*/

	@Test
	public void testSimilarySogns() throws ApiException {
		AlibabaXiamiApiSongSimilarGetRequest req = new AlibabaXiamiApiSongSimilarGetRequest();
		req.setId(1773346703L);
		req.setLimit(50L);
		AlibabaXiamiApiSongSimilarGetResponse rsp = client.execute(req);
		List<StandardSong>  list = rsp.getData();
		System.out.println(list);
		for(StandardSong song:list) {
			System.out.println(song.getSongName());
		}
	}

	@Test
	public void testEnglish() {
		String string = "adam ketty";
		boolean isWord=string.matches("[a-zA-Z ]+");
		System.out.println(isWord);
	}

	@Test
	public void testTokens() throws IOException {
		String str = "播放@眼缘";
		List<String> list = new ElasticHandler().cut(str, true);
		for (String temp : list) {
			System.out.println(temp);
		}
	}

	@Test
	public void testArtist() throws ApiException {
		AlibabaXiamiApiSearchArtistsGetRequest req = new AlibabaXiamiApiSearchArtistsGetRequest();
		req.setLimit(10L);
		req.setPage(1L);
		req.setKey("我是歌手");
		AlibabaXiamiApiSearchArtistsGetResponse rsp = client.execute(req);
		for (StandardArtist artist : rsp.getData().getArtists()) {
			System.out.println(artist.getArtistId()+"  "+artist.getArtistName());
		}
	}

	@Test
	public void testArtistById() throws ApiException {
		AlibabaXiamiApiArtistAlbumsGetRequest req = new AlibabaXiamiApiArtistAlbumsGetRequest();
		req.setLimit(10L);
		req.setPage(1L);
		req.setId(127544L);
		AlibabaXiamiApiArtistAlbumsGetResponse rsp = client.execute(req);
		for (StandardAlbum album : rsp.getData().getAlbums()) {
			System.out.println(album.getAlbumId()+" "+album.getArtistName()+" "+album.getAlbumName()+" "+album.getRecommends());
		}
		System.out.println(rsp.getBody());
	}

	@Test
	public void testGetAlbumById() throws ApiException {
		AlibabaXiamiApiAlbumDetailGetRequest req = new AlibabaXiamiApiAlbumDetailGetRequest();
		req.setId(2100272080L);
		AlibabaXiamiApiAlbumDetailGetResponse response = client.execute(req);
		for (Song song : response.getData().getSongs()) {
			System.out.println(song.getName() + " " + song.getArtistName());
		}
	}

	@Test
	public void testGetSongsByKeyWords() throws ApiException {
		AlibabaXiamiApiSearchSongsGetRequest req = new AlibabaXiamiApiSearchSongsGetRequest();
		req.setKey("刘德华冰雨");
		req.setPage(1L);
		req.setLimit(10L);
		req.setIsPub("all");
		req.setCategory("-1");
		AlibabaXiamiApiSearchSongsGetResponse rsp = client.execute(req);
        System.out.println(rsp.getBody());
        for (SearchSongsDataSongsData songs : rsp.getData().getSongs().getData()) {
            System.out.println(songs.getArtistName() + " " + songs.getSongName());
		}
	}

	@Test
    public void testGetSongById() throws UnsupportedEncodingException, ApiException {
        AlibabaXiamiApiSongDetailGetRequest req = new AlibabaXiamiApiSongDetailGetRequest();
        req.setId(16925L);
        AlibabaXiamiApiSongDetailGetResponse rsp = client.execute(req);
        SongDetail songDetail = null;
        songDetail = rsp.getData();
        byte[] bytes = songDetail.getSong().getSongName().getBytes();
        for (byte b : bytes) {
            System.out.println(b);
        }
        System.out.println( songDetail.getSong().getSongName() +" ; 歌手：" + songDetail.getSong().getSingers());
    }

    @Test
    public void changeFileToNew() throws IOException {
       List<String> list =  FileUtils.readLines(new File("F:\\codebase\\vinci-server\\src\\main\\resources\\nlp\\xiamirank.prop"));
        for(String str : list) {
            System.out.println(str);
        }
    }

    @Test
    public void trimPadding() throws Exception {
        String string = " ceshi yanshi ";
        System.out.println(string.trim());
    }

    @Test
    public void testSimilarity() throws ApiException {
        AlibabaXiamiApiSongSimilarGetRequest req = new AlibabaXiamiApiSongSimilarGetRequest();
        req.setId(1769227477L);
        req.setLimit(50L);
        AlibabaXiamiApiSongSimilarGetResponse rsp = client.execute(req);
        List<StandardSong> list = rsp.getData();
        for (StandardSong song : list) {
            System.out.println(song.getSongName()+" "+song.getArtistName());
        }

    }

    @Test
    public void testTodaySongs() throws ApiException {
        AlibabaXiamiApiRecommendDailySongsGetRequest req = new AlibabaXiamiApiRecommendDailySongsGetRequest();
        req.setLimit(1000L);
        req.setIds("");
        AlibabaXiamiApiRecommendDailySongsGetResponse rsp = client.execute(req);
        List<RecommendSong> list = rsp.getData().getSongs();
        System.out.println("%%%%%%%%%%%%%%%%%%%%% " + list.size());
        for (RecommendSong song : list) {
            System.out.println(song.getSongName()+" "+song.getArtistName());
        }

    }

    @Test
    public void testMatches() {
        String str = "everything's";
        String lang1 = "Justin Bieber的i will show you";
        String lang = "不变的音乐";
        System.out.println(str);
        System.out.println(str.matches(("[a-zA-Z' ]+")));
        System.out.println(lang.matches("[a-zA-Z0-9 ]+[的]{0,1}[a-zA-Z0-9 ]+[的歌]{0,1}"));
        System.out.println(lang.matches("^(不变).*(的)+.*(音)+$"));
    }

    @Test
    public void testLoadHimalayaEnglish() throws Exception {
        List<HimalayaAlbum> himalayaAlbumList = new ArrayList<>();
        Properties properties = new Properties();
        properties.load(new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/spoken.prop")).getInputStream(), "utf8")));
        Set<String> set = properties.stringPropertyNames();
        for (String str : set) {
            HimalayaAlbum album = new HimalayaSearchService().getAlbumById(Long.valueOf(properties.getProperty(str)),1,1000).getAlbum();
            himalayaAlbumList.add(album);
        }


        for (HimalayaAlbum album : himalayaAlbumList) {
            List<HimalayaTrack> list = album.getTracks();
            System.out.println("###################################################");
            System.out.println(album.getTitle());
            for (HimalayaTrack track : list) {
                System.out.println(track.getTitle()+" "+track.getPlayUrl());
                download(track.getPlayUrl(),"d://himalaya/"+album.getTitle()+"/"+track.getTitle()+".mp3");
            }
        }
        System.out.println(JSON.toJSON(himalayaAlbumList).toString());
    }

    public static void download(String urlString,String filename)throws Exception{
        URL url = new URL(urlString);
        URLConnection con = url.openConnection();
        InputStream is = con.getInputStream();

        byte[] bs = new byte[1024];
        int len;
        if(!CreateFileUtil.createFile(filename)){
            System.out.println("文件初始化异常");
            return ;
        }
        OutputStream os = new FileOutputStream(filename);
        while((len = is.read(bs))!=-1){
            os.write(bs, 0, len);
        }
        os.close();
        is.close();
    }

    @Test
    public void testRegex() {
        //String lang = "北京的空气的质量";
        String lang = "放刘德华冰雨";
        System.out.println(lang.matches("^.{0,3}[播放].*$"));

        System.out.println(lang.matches("^[我想听].*$"));
        System.out.println(lang.matches("^.*空气.*质量.*$"));
    }


    @Test
    public void testSimialrityUtils() {
        System.out.println(SimilarityUtil.levenshtein("everything s not lost","everything's not lost(live)"));
    }


    @Test
    public void testSimialrityUtilsTokens() {
        System.out.println(SimilarityUtil.filterStringByTokens("在, 也不见"));
    }


    @Test
    public void testTrim() {
        System.out.println(SimilarityUtil.trimString("   hello world    "));
    }

    @Test
    public void testNode() throws IOException {
        HashMap var17 = new HashMap();
        String cluster_name = "vinci-es-cluster-01";
        String node_address = "123.57.237.69";
        String node_port = "9300";
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", cluster_name)//指定集群名称
//                .put("client.transport.sniff", true)//探测集群中机器状态,通过这种方式可以只知道其中一个节点的ip和端口,底层自动嗅探到其他的es服务器节点
                .build();
        Client client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(node_address), Integer.parseInt(node_port)));
        BufferedReader bodyReader = null;
        bodyReader = new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/nlp.template")).getInputStream(), "utf8"));
        String line = null;
        StringBuilder strBuffer = new StringBuilder();
        while((line = bodyReader.readLine()) != null) {
            strBuffer.append(line);
            strBuffer.append("\n");
        }
        var17.put("query", "always and forever");
        var17.put("field_type", "best_fields");
        var17.put("singers_type", "singers.smart^1.0");
        var17.put("album_name_type", "album_name.smart^1.0");
        var17.put("song_name_type", "song_name.smart^1024 ");

        var17.put("context_type", "keyword.smart^4096.0");
        TemplateQueryBuilder qb = QueryBuilders.templateQuery(strBuffer.toString(), var17);
        SearchResponse response = client.prepareSearch(new String[]{"all"})
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setQuery(qb)
                .execute()
                .actionGet();
        System.out.println(response.toString());
    }


    @Test
    public void testHanNlpSite() {
       /* String[] testCase = new String[]{
                "武胜县新学乡政府大楼门前锣鼓喧天",
                "蓝翔给宁夏固原市彭阳县红河镇黑牛沟村捐赠了挖掘机",
        };*/
        String[] testCase = new String[]{
                "今天武胜县的天气怎么样"
        };
        Segment segment = HanLP.newSegment().enablePlaceRecognize(true);
        for (String sentence : testCase)
        {
            List<Term> termList = segment.seg(sentence);
            System.out.println(termList);
        }
    }

    @Test
    public void testFilter() {
        String str = "everything's not lost";
        str = str.replace("'", " ");
        System.out.println(str);
    }

    @Test
    public void testLowLine() {
        String str = "is_not";
        System.out.println(str.matches("[a-zA-Z' _]+"));
    }

    @Test
    public void testAbstractHours() {
        Date date = new Date();
        System.out.println(date.getHours());
    }

    @Test
    public void testRegular() {
        String regex = "((\\d{1,2}|半)((点钟)|点|(个小时)|(小时))(\\d{0,2}|半)?)?(\\d{1,2}(分钟|分))?(\\d{1,2}(秒钟|秒))?(以后|后)?";
        String regexDate = "(((\\d{4}|今|明|去|前|后|大前|大后)年)?(的)?(\\d{1,2}月)?(\\d{1,2}(日|号))?)|(今天|明天|后天|昨天|前天|大前天|大后天)?";
        String msg = "2015年的9月10号8点四十提醒我上班";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(msg);
        while (m.find()) {
            System.out.println(m.group(0));
        }

        Pattern p1 = Pattern.compile(regexDate);
        Matcher m1 = p1.matcher(msg);
        while (m1.find()) {
            System.out.println(m1.group(0));
        }
    }

    @Test
    public void testDaxieRegular() {
        Map<String ,Integer> map = new HashMap();
           map.put("零", 0);
           map.put("一", 1);
           map.put("二", 2);
           map.put("两", 2);
           map.put("三", 3);
           map.put("四", 4);
           map.put("五", 5);
           map.put("六", 6);
           map.put("七", 7);
           map.put("八", 8);
           map.put("九", 9);
        String msg = "五点四十五";
        String regex = "[一二三四五六七八九]?千?[一二三四五六七八九]?百?[一二三四五六七八九]?十?[一二三四五六七八九零]?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(msg);
        List<String> list = new ArrayList<>();//十位
        List<String> list1 = new ArrayList<>();//个位
        List<String> list2 = new ArrayList<>();//百位
        List<String> list3 = new ArrayList<>();//千位
        while (matcher.find()) {
            if(StringUtils.isNotBlank(matcher.group())){
                if (matcher.group().contains("千")) {
                    list3.add(matcher.group());
                } else if (matcher.group().contains("百")) {
                    list2.add(matcher.group());
                } else if (matcher.group().contains("十")) {
                    list.add(matcher.group());
                }
                else {
                    list1.add(matcher.group());
                }
            }
        }
        int result = 0;
        for (String str : list) {
            if (str.startsWith("十")) {
                int gewei = map.get(str.replace("十", ""));
                result = gewei+10;
            } else if (str.endsWith("十")) {
                int shiwei = map.get(str.replace("十", ""));
                result = shiwei * 10;
            }else{
                int shiwei = map.get(str.substring(0, 1));
                int gewei = map.get(str.substring(str.length() - 1, str.length()));
                result = shiwei * 10 + gewei;
            }
            msg = msg.replace(str, result + "");
        }

        for (String str : list1) {
            result = map.get(str);
            msg = msg.replace(str, result + "");
        }

        for (String str : list3) {
            int j = 1;
            for(int i=1;i<str.length();i++) {
                if (j % 2 != 0) {
                    j++;
                    result = map.get(str.substring(i - 1, i));
                }
            }
            result = result * 1000;
            map.get(1);
        }
        System.out.println(msg);

    }
    

    @Test
    public void testRegular1() {
        String regex = "半个?小时[以后|后]";
        String msg = "半个小时后提醒我";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(msg);
        while (m.find()) {
            System.out.println(m.group());
        }
    }



    public static void main(String[] args) {
        //定义正则表达式。这个正则表达式用来提取IP地址和访问时间
        String regex = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\s-\\s-\\s\\[([^\\]]+)\\]";
        //把正则表达式编译成Pattern对象，调用compile()方法，并在调用参数中指定正则表达式
        Pattern p1 = Pattern.compile(regex);

        //要处理的字符串。
        String input1 = "66.249.73.207 - - [19/Dec/2013:04:00:03 +0800] \"GET /robots.txt HTTP/1.1\" 200 441 \"-\" \"Mozilla/5.0 ";

        //匹配对象。利用已经指定好正则表达式的模式对象p1来对指定源字符串input1进行匹配。
        Matcher m = p1.matcher(input1);

        //调用匹配对象m的find()方法来在源字符串input1中寻找一个新的符合指定正则表达式要求的子字符串
        while(m.find()){
            System.out.println(m.group(0)); //输出整个符合正则表达式要求的字符串
            //System.out.println(m.group()); //group()方法中不带参数，与group(0)相同
            System.out.println(m.group(1)); //输出第一组圆括号里的内容
            System.out.println(m.group(2)); //输出第二组圆括号里的内容
        }

        System.out.println("");

        //匹配QQ号
        String regex2 = "\\d{6,10}";
        Pattern p2 = Pattern.compile(regex2);
        String input2 = "我的QQ号是5649237.";
        Matcher m2 = p2.matcher(input2);

        while(m2.find()){
            System.out.println(m2.group());
        }

    }

}
