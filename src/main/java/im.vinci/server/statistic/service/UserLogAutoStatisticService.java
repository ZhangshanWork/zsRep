package im.vinci.server.statistic.service;

import im.vinci.server.statistic.domain.EventSourceType;
import im.vinci.server.statistic.domain.UserAgeRangeStat;
import im.vinci.server.statistic.domain.UserGenderStat;
import im.vinci.server.statistic.persistence.AccountStatsFetchMapper;
import im.vinci.server.statistic.utils.JFreeChartUtil;
import im.vinci.server.statistic.utils.JFreeChartUtil.DotData;
import im.vinci.server.statistic.utils.MailUtils;
import im.vinci.server.utils.DateUtils;
import im.vinci.server.utils.JsonUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.Map.Entry;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

/**
 * Created by zhongzhengkai on 15/12/11.
 */
@Service
@Configurable
//@EnableScheduling
public class UserLogAutoStatisticService {
    private static Logger logger = LoggerFactory.getLogger(UserLogAutoStatisticService.class);

    @Autowired
    Environment env;

    @Resource(name = "esClient")
    private Client esClient;

    @Autowired
    AccountStatsFetchMapper mapper;

    private Map<String, String> nameDescriptionMap = new HashMap<String, String>() {{
        put("pre", "上一首歌");
        put("next", "下一首歌");
        put("poweron", "开机器");
        put("poweroff", "关机器");
        put("screenon", "开屏幕");
        put("screenoff", "熄屏幕");
        put("wirein", "插入耳机");
        put("wireout", "拔出耳机");
        put("click", "暂停或播放歌曲");

        //都来自于doubleclick日志,V1.8之后通过type为true来表示收藏歌曲,false表示取消收藏,V1.8之前没有type字段,通通表示收藏歌曲
        put("doubleclick", "收藏歌曲");

        put("separation", "摘下耳机");
        put("contact", "带上耳机");
        put("lightness", "调节亮度");
        put("guide", "查看说明");
        put("charge", "充电");
        put("vol", "调节音量");

        //都来自于tcp_longpress日志,V1.8之前无法区分是按触摸板还是屏幕,V1.8之后通过type字段来表示,为touchpad时表示长按触摸板,为screen时表示长按pingmu
        put("tcp_longpress", "长按");

        put("voice", "下达语音命令");
        put("musicplay", "音乐播放结束");
        put("nlpsearch", "nlp搜索");
        put("xmlysearch", "xmly搜索");//来自于voice
        put("recommend", "调用推荐接口");
        put("upgrade", "系统升级");
        put("visualtype", "选择可视化");
        put("homepress", "回到主页");
        put("infohelp", "查看帮助手册");
        put("language", "语言选择");
        put("custom_visual", "自定义可视化文字");
        put("share", "分享");
        put("other", "其他");
        put("total", "总的log数量");
    }};

    private Map<String, String> visualTypeCodeNameMap = new LinkedHashMap<String, String>() {{
        put("circle", "圆圈");
        put("video", "视频");
        put("circleline", "圆圈线");
        put("ray", "射线");
        put("vt_born_to_beauty", "天然淳朴");
        put("vt_cannot_listen", "听不见");
        put("vt_like_you", "黑凤梨");
        put("vt_normal", "生来低调");
        put("vt_simple_ink", "毕竟走心");
        put("vt_sleep_together", "一起睡觉");
        put("custom", "自定义文字");
        put("vt_dot_colors", "粒子");
    }};

    //四零后|五零后|五五后|六零后|六五后|七零后|七五后|八零后|八五后|九零后|九五后|零零后|零五后|一零后
    private Map<String, Integer> ageRangeValue = new HashMap<String, Integer>() {{
        put("四零后", 1);
        put("五零后", 2);
        put("五五后", 3);
        put("六零后", 4);
        put("六五后", 5);
        put("七零后", 6);
        put("七五后", 7);
        put("八零后", 8);
        put("八五后", 9);
        put("九零后", 10);
        put("九五后", 11);
        put("零零后", 12);
        put("零五后", 13);
        put("一零后", 14);
    }};

    private Map<String, Integer> genderNumber = new HashMap<String, Integer>() {{
        put("MALE", 1);
        put("FEMALE", 2);
    }};

    /**
     * 统计推荐系统相关的每日数据并返回html字符串
     *
     * @return
     */
    private Map<String, String> _statDailyData(int preDayValue) throws Exception {
        Map<String, String> toReturn = new HashMap<>();//key:toNLP|给nlp小组的内容,toPM|给产品经理的内容
        logger.info("--->>> start to execute statRecommendDailyData!");
        Map resultMapOfRec = new LinkedHashMap();//存放推荐系统各种统计结果的容器(当日最新一次的)
        Map resultMapOfRec2 = new LinkedHashMap();//存放推荐系统各种统计结果的容器(当日累计的)
        Map resultMapOfLog = new HashMap();//存放各种其他统计结果的容器
        Map<String, HashSet<String>> versionDauMap = new HashMap<>();//存放各个版本对应的日活跃的容器
        Map versionLogCountMap = new HashMap();//存放各个版本对应的日活跃的容器


        //------------获取account_extra_info数据来计算出用户的版本分布
        SearchResponse versionStatResp = esClient.prepareSearch("account_extra_info")
                .setTypes("account_extra_info")
                .addAggregation(AggregationBuilders.terms("group_by_version").field("rom_version"))
                .setSize(0)
                .execute()
                .actionGet();
        Terms versionStatAgg = versionStatResp.getAggregations().get("group_by_version");
        List<Terms.Bucket> versionStatList = versionStatAgg.getBuckets();
        long userTotalCount = 0;//所用的用户总数
        for (Terms.Bucket entry : versionStatList) {
            String version = (String) entry.getKey();
            userTotalCount += entry.getDocCount();
            versionDauMap.put(version, new HashSet<String>());
            versionLogCountMap.put(version, 0);
        }
        //----------------------------------------end----------------------------------------


        HashSet totalUserCountSet = new HashSet();

        long totalPlaySongCount = 0;//所有用户的播歌曲数目
        long totalLikeSongCount = 0;//所有用户的喜欢歌曲数目
        long totalPassSongCount = 0;//所有用户的切歌曲数目
        long totalPlayOverSongCount = 0;//所有用户的播完歌曲数目
        long totalPlaySongDuration = 0;//所有用户的听歌时长

        //------------推荐系统相关------------
        long totalUseRecSysCount = 0;//所有用户的使用推荐系统次数
        long totalPassRecSongCount = 0;//所有用户的切推荐歌曲数目
        long totalPlayRecSongCount = 0;//所有用户的播推荐歌曲数目
        long totalPlayOverRecSongCount = 0;//所有用户的播完推荐歌曲数目
        long totalLikeRecSongCount = 0;//所有用户的喜欢推荐歌曲数目
        long totalPlayRecSongDuration = 0;//所有用户的听推荐歌时长
        long invalidTotalRecSongCount = 0;//总的推荐歌曲次数(无效的)

        Map<Integer,Integer> totalDurationEachHour = new LinkedHashMap<Integer,Integer>();
        for(int i =0;i<24;i++){
            totalDurationEachHour.put(i,0);
        }
        Map<Integer,HashSet<String>> totalUserCountEachHour = new LinkedHashMap<Integer,HashSet<String>>();
        for(int i =0;i<24;i++){
            totalUserCountEachHour.put(i,new HashSet<String>());
        }

        int validHits = 0;
        int copyCursor = 0;
        Map<String, Long> logStatMap = new LinkedHashMap<String, Long>() {{
            put("pre", 0L);
            put("next", 0L);
            put("poweron", 0L);
            put("poweroff", 0L);
            put("screenon", 0L);
            put("screenoff", 0L);
            put("wirein", 0L);
            put("wireout", 0L);
            put("click", 0L);
            put("doubleclick", 0L);
            put("separation", 0L);
            put("contact", 0L);
            put("lightness", 0L);
            put("guide", 0L);
            put("charge", 0L);
            put("vol", 0L);
            put("tcp_longpress", 0L);
            put("voice", 0L);
            put("musicplay", 0L);
            put("nlpsearch", 0L);
            put("xmlysearch", 0L);
            put("recommend", 0L);
            put("upgrade", 0L);
            put("visualtype", 0L);
            put("homepress", 0L);
            put("infohelp", 0L);
            put("language", 0L);
            put("custom_visual", 0L);
            put("share", 0L);
            put("other", 0L);
            put("total", 0L);
        }};

        //某些类型的log,还需要看额外的信息
        Map<String, LogExtra> logStatExtraInfoMap = new LinkedHashMap<String, LogExtra>() {{
            put("language", new LanguageLogExtra());
            put("custom_visual", new CustomVisualLogExtra());
            put("tcp_longpress", new TcpLongPressLogExtra());
            put("doubleclick", new DoubleClickLogExtra());
            put("voice", new VoiceLogExtra());
            put("nlpsearch", new NlpSearchExtra());
        }};

        Map<String, Integer> visualTypeCountMap = new LinkedHashMap<String, Integer>() {{
            put("circle", 0);
            put("video", 0);
            put("circleline", 0);
            put("ray", 0);
        }};

        boolean isLogEnough = false;
        boolean isRecommendLogEnough = false;


        int voiceSuccess = 0;//voice指令成功的数量
        int voiceFailed = 0;//voice指令失败的数量
        StringBuilder voiceKeywordSB = new StringBuilder("<fieldset>------------用户的语音指令收集数据[keyword]------------<br/>");
        Map<String, Integer> voiceKeywordContainer = new HashMap<>();
        Map<String, KeywordDetail> voiceKeywordContainerDetail = new HashMap<>();

        Calendar cal = Calendar.getInstance();

        //<<<<<<------------滚动查询头一天的所有user_logs,并开始map各个需要统计的key
        SearchResponse response = esClient.prepareSearch("user_logs")
                .setScroll(new TimeValue(60000))
//                .setSearchType(SearchType.QUERY_AND_FETCH)
                .setQuery(boolQuery().must(rangeQuery("create_time")
                        .from(DateUtils.preDayZeroClockTimestamp(preDayValue))
                        .to(DateUtils.preDayZeroClockTimestamp(preDayValue - 1))))
                .setSize(10000)
                .execute()
                .actionGet();

        logger.info("--->total_hits:" + response.getHits().getTotalHits());
        Map<String, UserStatData> userStatMap = new HashMap();
        while (true) {
            SearchHit[] tmpHits = response.getHits().getHits();
            if (tmpHits.length == 0) {
                break;
            }

            for (SearchHit hit : tmpHits) {
                Map source = hit.getSource();
                String imei = (String) source.get("imei");
                String logName = (String) source.get("name");
                String romVersion = (String) source.get("rom_version");
                long createTime = (long) source.get("create_time");
                cal.setTimeInMillis(createTime);
                int curHour = cal.get(Calendar.HOUR_OF_DAY);

                Object versionCount = versionLogCountMap.get(romVersion);
                if (versionCount != null) {
                    versionLogCountMap.put(romVersion, (int) versionCount + 1);
                    HashSet versionUserSet = versionDauMap.get(romVersion);
                    versionUserSet.add(imei);
                }
                _statLogCount(logName,source,logStatMap,logStatExtraInfoMap);
                //15位的imei码才是有效的,早起有一批头机没有imei码,有mac码但是mac是变的,所有这里以imei作为用户的唯一标示符
                if (imei.length() > 0 && imei.length() == 15) {

                    totalUserCountEachHour.get(curHour).add(imei);
                    totalUserCountSet.add(imei);
                    validHits++;

                    //带mid的homepress log也是一种特殊的切歌事件
                    boolean isPassLog = logName.equals("next") || (logName.equals("homepress") && source.get("mid") != null);

                    if (isPassLog) {
                        totalPlaySongCount++;
                        Integer duration = (Integer) source.get("duration");//播放时长单位秒
                        Integer mlength = (Integer) source.get("mlength");//老版本的log可能没有mlength
                        mlength = (mlength != null && mlength > 0 && mlength <= 4800) ? mlength : 0;
                        if (duration < mlength - 30) {
                            if (duration == -1) {//-1是一种特殊情况,歌没播就切了
                                duration = 0;
                                source.put("duration", 0);
                            }
                            boolean isDurationInvalid = (duration >= 0 && duration < 10800);
                            if (isDurationInvalid) {
                                totalPlaySongDuration += duration;
                                totalDurationEachHour.put(curHour, totalDurationEachHour.get(curHour) + duration);
                            }
                            if (isValidRecommendLog(source, true, true)) {
                                totalPassRecSongCount++;

                                if (isDurationInvalid) {//前端记录的duration值可能为负数,为-1时表示用户切得太快了没有获取到播放时长的值
                                    if (!userStatMap.containsKey(imei))
                                        userStatMap.put(imei, new UserStatData(imei));
                                    UserStatData tmpUserStatData = userStatMap.get(imei);
                                    tmpUserStatData.stat(source, logName);
                                }
                            } else {
                                invalidTotalRecSongCount++;
                            }

                            totalPassSongCount++;
                        } else {
                            //对于结束次数统计来说,不需要判断mlength
                            if (isValidRecommendLog(source, false, true)) {
                                if (!userStatMap.containsKey(imei)) userStatMap.put(imei, new UserStatData(imei));
                                UserStatData tmpUserStatData = userStatMap.get(imei);
                                tmpUserStatData.stat(source, logName);
                                totalPlayOverRecSongCount++;
                                totalPlayOverSongCount++;
                            } else {
                                invalidTotalRecSongCount++;
                            }
                        }
                    } else if (logName.equals("doubleclick")) {
                        if (isValidRecommendLog(source, false, true)) {
                            if (!userStatMap.containsKey(imei)) userStatMap.put(imei, new UserStatData(imei));
                            userStatMap.get(imei).stat(source,logName);
                        }
                    } else if (logName.equals("voice")) {
                        String voiceType = (String) source.get("type");

                        if(voiceType.equals("xmly")){
                            logStatMap.put("xmlysearch", logStatMap.get("xmlysearch") + 1);
                        }

                        if (voiceType.equals("failed")) {
                            voiceFailed++;
                        } else {

                            voiceSuccess++;
                            String tmpKeyWord = (String) source.get("keyword");
                            String tmpUnderstandText = (String) source.get("understand");
                            String tmpUnderstandType = (String) source.get("type");
                            boolean isOnline = (boolean) source.get("is_online");
                            Integer tmpValue = voiceKeywordContainer.get(tmpKeyWord);
                            if (tmpValue == null) {
                                voiceKeywordContainer.put(tmpKeyWord, 1);
                                if (isOnline)
                                    voiceKeywordContainerDetail.put(tmpKeyWord, new KeywordDetail(1, 0, tmpUnderstandText, tmpUnderstandType,romVersion));
                                else
                                    voiceKeywordContainerDetail.put(tmpKeyWord, new KeywordDetail(0, 1, tmpUnderstandText, tmpUnderstandType,romVersion));

                            } else {
                                voiceKeywordContainer.put(tmpKeyWord, tmpValue + 1);
                                KeywordDetail tmpKeywordDetail = voiceKeywordContainerDetail.get(tmpKeyWord);
                                if (isOnline)
                                    tmpKeywordDetail.incOnlineCount();
                                else
                                    tmpKeywordDetail.incOfflineCount();
                                tmpKeywordDetail.appendTextIfNotContain(romVersion,tmpUnderstandText);
                                tmpKeywordDetail.appendTypeIfNotContain(tmpUnderstandType);
                            }
                        }
                    } else if (logName.equals("visualtype")) {//统计可视化类型选择次数
                        String visualType = (String) source.get("type");
                        try {
                            visualTypeCountMap.put(visualType, visualTypeCountMap.get(visualType) + 1);
                        } catch (NullPointerException e) {
                            visualTypeCountMap.put(visualType, 1);
                        }
                    }
                }
            }

            response = esClient.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
            tmpHits = response.getHits().getHits();
        }
        System.out.println("totalUserCount:" + totalUserCountSet.size());
        logger.info("--->valid_hits:" + validHits);
        //map各个需要统计的key结束------------>>>>>>

        //计算用户单次使用推荐系统的平均长度
        Iterator iterator = userStatMap.entrySet().iterator();
        int userCountOfUsingRecSys = userStatMap.size();
        //最新一次使用推荐系统的各个指标
        float avgLRSPlayDuration = 0;
        float avgLRSPlaySongCount = 0;
        float avgLRSPlayOverSongCount = 0;
        float avgLRSLikeSongCount = 0;
        float avgLRSPlayOverAndPlayRatio = 0;
        float avgLRSLikeAndPlayRatio = 0;

        //当日累计使用推荐系统的各个指标
        float avgUsingRecSysCount = 0;
        float avgUsingRecSysDuration = 0;
        float avgPlayRecSongCount = 0;
        float avgPlayOverRecSongCount = 0;
        float avgLikeRecSongCount = 0;
        float avgPlayOverAndPlayRatio = 0;
        float avgLikeAndPlayRatio = 0;

        if (userCountOfUsingRecSys > 0) {

            long sumLRSPlayDuration = 0;
            long sumLRSPlaySongCount= 0;
            long sumLRSPlayOverSongCount= 0;
            long sumLRSLikeSongCount= 0;

            long sumUsingRecSysCount = 0;
            long sumUsingRecSysDuration = 0;
            long sumPlayRecSongCount = 0;
            long sumPlayOverRecSongCount = 0;
            long sumLikeRecSongCount = 0;

            while (iterator.hasNext()) {
                Entry<String, UserStatData> entry = (Entry) iterator.next();
                UserStatData statData = entry.getValue();

                sumLRSPlayDuration += statData.latestRecSysPlayDuration;
                sumLRSPlaySongCount += statData.latestRecSysPlaySongCount;
                sumLRSPlayOverSongCount += statData.latestRecSysPlayOverSongCount;
                sumLRSLikeSongCount += statData.latestRecSysLikeSongCount;
                //不算每个用户的播完与播的百分比,再去求平均值
//                if (statData.latestRecSysPlaySongCount > 0) {
//                    System.out.println("good statData:"+statData.toString());
//                    sumLRSPlayOverAndPlayRatio += (float)statData.latestRecSysPlayOverSongCount / statData.latestRecSysPlaySongCount;
//                    sumLRSLikeAndPlayRatio += (float)statData.latestRecSysLikeSongCount / statData.latestRecSysPlaySongCount;
//                }else{
//                    System.out.println("bad statData:"+statData.toString());
//                }

                sumUsingRecSysCount += statData.useRecCount;
                sumUsingRecSysDuration += statData.playDuration;
                sumPlayRecSongCount += statData.playRecSongCount;
                sumPlayOverRecSongCount += statData.playOverRecSongCount;
                sumLikeRecSongCount += statData.likeRecSongCount;
                //不算每个用户的播完与播的百分比,再去求平均值
//                if(statData.playRecSongCount>0){
//                    sumPlayOverAndPlayRatio += (float)statData.playOverRecSongCount / statData.playRecSongCount;
//                    sumLikeAndPlayRatio += (float)statData.likeRecSongCount / statData.playRecSongCount;
//                }
            }

            System.out.println("userCountOfUsingRecSys:"+userCountOfUsingRecSys);
            System.out.println("sumUsingRecSysCount:"+sumUsingRecSysCount);
            System.out.println("sumUsingRecSysDuration:"+sumUsingRecSysDuration);
            System.out.println("sumPlayRecSongCount:"+sumPlayRecSongCount);
            System.out.println("sumPlayOverRecSongCount:"+sumPlayOverRecSongCount);
            System.out.println("sumLikeRecSongCount:"+sumLikeRecSongCount);
            System.out.println("sumLRSLikeSongCount:"+sumLRSLikeSongCount);
            System.out.println("sumLRSPlayOverSongCount:"+sumLRSPlayOverSongCount);
            System.out.println("sumLRSPlaySongCount:"+sumLRSPlaySongCount);

            //最新一次使用推荐系统的各个指标
            avgLRSPlayDuration = (float)sumLRSPlayDuration / userCountOfUsingRecSys;
            avgLRSPlaySongCount = (float)sumLRSPlaySongCount / userCountOfUsingRecSys;
            avgLRSPlayOverSongCount = (float)sumLRSPlayOverSongCount / userCountOfUsingRecSys;
            avgLRSLikeSongCount = (float)sumLRSLikeSongCount / userCountOfUsingRecSys;
            avgLRSPlayOverAndPlayRatio = avgLRSPlayOverSongCount / avgLRSPlaySongCount;
            avgLRSLikeAndPlayRatio = avgLRSLikeSongCount / avgLRSPlaySongCount;

            //当日累计使用推荐系统的各个指标
            avgUsingRecSysCount = (float)sumUsingRecSysCount / userCountOfUsingRecSys;
            avgUsingRecSysDuration = (float)sumUsingRecSysDuration / userCountOfUsingRecSys;
            avgPlayRecSongCount = (float)sumPlayRecSongCount / userCountOfUsingRecSys;
            avgPlayOverRecSongCount = (float)sumPlayOverRecSongCount / userCountOfUsingRecSys;
            avgLikeRecSongCount = (float)sumLikeRecSongCount / userCountOfUsingRecSys;
            avgPlayOverAndPlayRatio = avgPlayOverRecSongCount / avgPlayRecSongCount;
            avgLikeAndPlayRatio = avgLikeRecSongCount / avgPlayRecSongCount;

        }

        int dau = totalUserCountSet.size();
        cal.setTimeInMillis(new Date().getTime() - 86400000);//获得头一天的各种年月日数据
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);
        cal.setTimeInMillis(new Date().getTime());//获得当天的各种年月日数据
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH) + 1;
        int currentDay = cal.get(Calendar.DAY_OF_MONTH);

        //Double.toString(Math.round(averagePlayRatio * 10000) * 0.01) + "%"
        resultMapOfRec.put("平均使用时间(分钟)", String.format("%.2f", avgLRSPlayDuration/60));
        resultMapOfRec.put("平均听歌数量", String.format("%.2f", avgLRSPlaySongCount));
        resultMapOfRec.put("平均听完的歌数量", String.format("%.2f", avgLRSPlayOverSongCount));
        resultMapOfRec.put("平均喜欢的歌数量", String.format("%.2f", avgLRSLikeSongCount));
        resultMapOfRec.put("平均听完的歌占总歌曲的百分比(%)", String.format("%.2f", avgLRSPlayOverAndPlayRatio*100));
        resultMapOfRec.put("平均喜欢的歌占总歌曲的百分比(%)", String.format("%.2f", avgLRSLikeAndPlayRatio*100));

        resultMapOfRec2.put("使用人数", userCountOfUsingRecSys);
        resultMapOfRec2.put("使用人数占当日活跃用户的百分比(%)", String.format("%.2f", (float) userCountOfUsingRecSys / dau * 100));
        resultMapOfRec2.put("平均使用次数", String.format("%.2f", avgUsingRecSysCount));
        resultMapOfRec2.put("平均使用时间(分钟)", String.format("%.2f", avgUsingRecSysDuration/60));
        resultMapOfRec2.put("平均听歌数目", String.format("%.2f", avgPlayRecSongCount));
        resultMapOfRec2.put("平均听完的歌数目", String.format("%.2f", avgPlayOverRecSongCount));
        resultMapOfRec2.put("平均喜欢的歌数目", String.format("%.2f", avgLikeRecSongCount));
        resultMapOfRec2.put("平均听完的歌占总歌曲的百分比(%)", String.format("%.2f", avgPlayOverAndPlayRatio*100));
        resultMapOfRec2.put("平均喜欢的歌占总歌曲的百分比(%)", String.format("%.2f", avgLikeAndPlayRatio*100));

        double avgListenLength = dau > 0 ? Math.ceil((totalPlaySongDuration / 60) / dau) : 0;
        resultMapOfLog.put("用户日平均听头机时长(分钟)", avgListenLength);

//        esClient.prepareIndex("user_logs_stat", DateUtils.preDayYMDStr(0)).setSource("").execute().actionGet();

        StringBuilder htmlBuilder = new StringBuilder();
        StringBuilder toRecSysHtmlBuilder = new StringBuilder();
        String preDayStr = DateUtils.preDayYMDStr(preDayValue);
        //------------获取account_extra_info数据来计算出用户的版本分布
        htmlBuilder.append("<br/><fieldset><b>------------截止至" + preDayStr + "日期的用户版本分布数据------------</b>" +
                "<table style=\"border:solid blue 2px\">");
        htmlBuilder.append("<tr><th  style=\"border:solid blue 1px;text-align:middle\">rom版本号</th>" +
                "<th  style=\"border:solid blue 1px;text-align:middle\">该版本总用户数  (占头机总用户数的百分比)</th><th  style=\"border:solid blue 1px;text-align:middle\">该版本当日活跃用户数  (占该版本总用户数百分比)</th>"
                + "<th  style=\"border:solid blue 1px;text-align:middle\">产生的log总数</th><tr>");
        for (Terms.Bucket entry : versionStatList) {
            String version = (String) entry.getKey(); //版本号
            long userCount = entry.getDocCount(); //这个版本对应的总用户数
            int versionDau = versionDauMap.get(version).size(); //这个版本对应的活跃用户数
            String ratio = userTotalCount > 0 ? String.format("%.2f", (float) userCount / userTotalCount * 100) + "%" : "0%";
            String ratio2 = userCount > 0 ? String.format("%.2f", (float) versionDau / userCount * 100) + "%" : "0%";

            htmlBuilder.append("<tr><th  style=\"border:solid blue 1px;text-align:left\">"
                    + version
                    + "</th><td style=\"border:solid blue 1px;width:190px;text-align:center\">"
                    + userCount  + "   (" + ratio + ")"//这个版本对应的总用户数
                    + "</td><td style=\"border:solid blue 1px;width:190px;text-align:center\">"
                    + versionDau + "   (" + ratio2 + ")" //这个版本对应的活跃用户数
                    + "</td><td style=\"border:solid blue 1px;width:190px;text-align:center\">"
                    + versionLogCountMap.get(version) //当日活跃用户产生的log总数
                    + "</td></tr>");
        }
        htmlBuilder.append("</table></fieldset>");
        //----------------------------------------end----------------------------------------


        Map<String, LogStatData> logStatData = new HashMap<>();
        logStatData.put("vinci_dau", new LogStatData(year, month, dayOfMonth, weekOfMonth, "vinci_dau", dau));

        htmlBuilder.append("<br/><fieldset><b>" + preDayStr + "当日在线日活跃用户数量:<font color=\"red\">"
                + dau + "</font>,    平均听头机时长(分钟):<font color=\"red\">" + avgListenLength + "</font></b></fieldset>");


        htmlBuilder.append("<br/><fieldset><b>------------" + preDayStr + "当日的各个小时的统计数据------------</b>" +
                "<table style=\"border:solid blue 2px\"><tr><th  style=\"border:solid blue 1px;text-align:left\">时间(指整个小时)</th>");
        for(int hour=0;hour<24;hour++){
            htmlBuilder.append("<th  style=\"border:solid blue 1px;text-align:left\">"
                    + hour+"点"
                    + "</th>");
        }
        htmlBuilder.append("</tr><tr><th  style=\"border:solid blue 1px;text-align:left\">用户数量</th>");
        for(int hour=0;hour<24;hour++){
            htmlBuilder.append("<td  style=\"border:solid blue 1px;text-align:left\">"
                    + totalUserCountEachHour.get(hour).size()
                    + "</td>");
        }
        htmlBuilder.append("</tr><tr><th  style=\"border:solid blue 1px;text-align:left\">听头机总时长(分钟)</th>");
        for(int hour=0;hour<24;hour++){
            htmlBuilder.append("<td  style=\"border:solid blue 1px;text-align:left\">"
                    + String.format("%.2f",totalDurationEachHour.get(hour)/60f)
                    + "</td>");
        }
        htmlBuilder.append("</tr><tr><th  style=\"border:solid blue 1px;text-align:left\">听头机平均时长(分钟)</th>");
        for (int hour = 0; hour < 24; hour++) {
            int userCountEachHour = totalUserCountEachHour.get(hour).size();
            htmlBuilder.append("<td  style=\"border:solid blue 1px;text-align:left\">"
                    + (userCountEachHour > 0 ? String.format("%.2f", (totalDurationEachHour.get(hour) / userCountEachHour) / 60f) : 0)
                    + "</td>");
        }
        htmlBuilder.append("</tr></table></fieldset>");

//        //------------>>>>>>推荐系统相关的数据
//        toRecSysHtmlBuilder.append("<br/><fieldset><b>------------" + preDayStr + "当日的最新一次使用推荐系统的指标统计------------</b>" +
//                "<table style=\"border:solid blue 2px\">");
//        Iterator it = resultMapOfRec.entrySet().iterator();
//        while (it.hasNext()) {
//            Entry entry = (Entry) it.next();
//            toRecSysHtmlBuilder.append("<tr><th  style=\"border:solid blue 1px;text-align:left\">"
//                    + entry.getKey()
//                    + "</th><td style=\"border:solid blue 1px;width:190px;text-align:center\">"
//                    + entry.getValue()
//                    + "</td></tr>");
//        }
//        toRecSysHtmlBuilder.append("</table></fieldset>");

        toRecSysHtmlBuilder.append("<br/><fieldset><b>------------" + preDayStr + "当日的累计使用推荐系统的指标统计------------</b>" +
                "<table style=\"border:solid blue 2px\">");
        Iterator it1_2 = resultMapOfRec2.entrySet().iterator();
        while (it1_2.hasNext()) {
            Entry entry = (Entry) it1_2.next();
            toRecSysHtmlBuilder.append("<tr><th  style=\"border:solid blue 1px;text-align:left\">"
                    + entry.getKey()
                    + "</th><td style=\"border:solid blue 1px;width:190px;text-align:center\">"
                    + entry.getValue()
                    + "</td></tr>");
        }
        toRecSysHtmlBuilder.append("</table></fieldset>");
        //<<<<<<------------
        toReturn.put("toRecSys",toRecSysHtmlBuilder.toString());

        htmlBuilder.append("<br/><fieldset><p>------------<b>" + preDayStr + "当日的log数量统计数据</b>------------</p><table style=\"border:solid blue 2px\">");
        htmlBuilder.append("<tr style= \"background-color:silver\"><th>log名称</th><th>数量</th><th>备注</th></tr>");
        Iterator it2 = logStatMap.entrySet().iterator();
        while (it2.hasNext()) {
            Entry<String, Long> entry = (Entry) it2.next();
            String logName = entry.getKey();
            LogExtra extra = logStatExtraInfoMap.get(logName);
            htmlBuilder.append("<tr><th  style=\"border:solid blue 1px;text-align:left\">"
                    + nameDescriptionMap.get(logName)
                    + "</th><td style=\"border:solid blue 1px;width:190px;text-align:center\">"
                    + entry.getValue()
                    + "</td><td style=\"border:solid blue 1px;width:190px;text-align:center\">"
                    + (extra != null ? extra.getExtraInfo() : "")
                    + "</td></tr>");
            logStatData.put(entry.getKey(), new LogStatData(year, month, dayOfMonth, weekOfMonth, entry.getKey(), entry.getValue()));
        }
        htmlBuilder.append("</table></fieldset>");

        htmlBuilder.append("<br/><fieldset><p>------------<b>" + preDayStr + "当日的可视化类型选择统计数据</b>------------</p><table style=\"border:solid blue 2px\">");
        Iterator it3 = visualTypeCountMap.entrySet().iterator();
        while (it3.hasNext()) {
            Entry<String, Integer> entry = (Entry) it3.next();
            String codeName = visualTypeCodeNameMap.get(entry.getKey());
            htmlBuilder.append("<tr><th  style=\"border:solid blue 1px;text-align:left\">"
                    + (codeName != null ? codeName : entry.getKey())
                    + "</th><td style=\"border:solid blue 1px;width:190px;text-align:center\">"
                    + entry.getValue()
                    + "</td></tr>");
        }
        htmlBuilder.append("</table></fieldset>");

        toReturn.put("toPM", htmlBuilder.toString());
        _recordStatLogs(logStatData);//把这些每日统计数据记录下来,方便以后有别的用处

        voiceKeywordSB.append("<b>头机理解成功次数:" + voiceSuccess + ",失败次数:" + voiceFailed
                + "</b><br/>成功理解的语音统计如下表所示<br/><table><tr><th>语音文本</th>" + "<th>理解类型</th>" +
                "<th>语义文本(xiami:song,singer,album , xmly:catalog,subCatalog,album,name)</th><th>总重复次数</th><th>在线重复次数</th><th>离线重复次数</th></tr>");
        //将hashmap的value降序排列
        List<Entry<String, Integer>> list = new LinkedList<>();
        list.addAll(voiceKeywordContainer.entrySet());
        Collections.sort(list, new Comparator<Entry<String, Integer>>() {
            public int compare(Entry<String, Integer> obj1, Entry<String, Integer> obj2) {//从高往低排序
                if (obj1.getValue() < obj2.getValue())
                    return 1;
                if (obj1.getValue() == obj2.getValue())
                    return 0;
                else
                    return -1;
            }
        });
        for (Iterator<Entry<String, Integer>> ite = list.iterator(); ite.hasNext(); ) {
            Entry<String, Integer> entry = ite.next();
            String key = entry.getKey();
            KeywordDetail detail = voiceKeywordContainerDetail.get(key);

            StringBuilder textTable = new StringBuilder("<table>");
            HashMap<String,String> textMap = detail.understandTextMap;
            Set<Entry<String,String>> tmpSet = textMap.entrySet();
            for(Entry<String,String> entry1 : tmpSet){
                textTable.append("<tr><td style=\"border:solid silver 1px\">" + entry1.getKey() + "</td><td style=\"border:solid silver 1px\">" + entry1.getValue() + "</td></tr>");
            }
            textTable.append("</table>");

            voiceKeywordSB.append("<tr><td style=\"border:solid blue 1px\">" + key + "</td><td style=\"border:solid blue 1px\">" + detail.understandType + "</td><td style=\"border:solid blue 1px\">"
                    + textTable.toString() + "</td><td style=\"border:solid blue 1px\">" + entry.getValue()
                    + "</td><td style=\"border:solid blue 1px\">" + detail.onlineCount + "</td><td style=\"border:solid blue 1px\">" + detail.offlineCount + "</td></tr>");
        }

//        Set<String> keywords = voiceKeywordContainer.keySet();
//        voiceKeywordSB.append("<b>头机理解成功次数:"+voiceSuccess+",失败次数:"+voiceFailed+"</b><br/>成功理解的语音统计如下表所示<br/><table><tr><th>用户语音的文本</th><th>重复次数</th></tr>");
//        for (String keyword : keywords) {
//            voiceKeywordSB.append("<tr><td>"+keyword + "</td><td>"+voiceKeywordContainer.get(keyword)+"</td><tr>");
//        }

        voiceKeywordSB.append("</table></fieldset>");
        toReturn.put("toNLP", voiceKeywordSB.toString());
        System.out.println("voice成功次数:" + voiceSuccess);
        System.out.println("voice失败次数:" + voiceFailed);

        _drawStatPicture(currentYear, currentMonth, currentDay, toReturn);

        return toReturn;
    }

    //绘制各种统计图,把带全路径的完整名称附加到map中
    //这里的年月日指的是当前时间的年月日
    private void _drawStatPicture(int year, int month, int dayOfMonth, Map<String, String> picPathMap) throws Exception {
        String ymdStr = year + "_" + month + "_" + dayOfMonth;
        String ymStr = year + "_" + month;

        //------------ 绘制当月的日活跃数据,日log总数数据------------
        Map<String, ArrayList<DotData>> dauDataSource = new HashMap<>();
        Map<String, ArrayList<DotData>> logCountDataSource = new HashMap<>();

        long startTime;
        long endTime;
        if (dayOfMonth == 1) {//如果是1号,就查看上个月的日活跃数据
            startTime = DateUtils.preMonthFirstDayTimestamp();
            endTime = DateUtils.preDayZeroClockTimestamp(0);
        } else {
            startTime = DateUtils.preDayZeroClockTimestamp(dayOfMonth - 1);
            endTime = DateUtils.preDayZeroClockTimestamp(0);
        }
        System.out.println("from:" + startTime + ",to:" + endTime + ",dayOfMonth:" + dayOfMonth);
        SearchResponse logStatResp = esClient.prepareSearch("user_logs")
                .setQuery(rangeQuery("create_time")
                        .from(startTime)
                        .to(endTime)
                )
                .setSize(0)
                .addAggregation(AggregationBuilders.dateHistogram("split_by_day")
                        .field("create_time")
                        .interval(DateHistogramInterval.DAY)
                        .timeZone("Asia/Shanghai")
                        .minDocCount(1)
                        .subAggregation(AggregationBuilders.cardinality("unique_mac").field("mac")))
                .execute()
                .actionGet();
        System.out.println(logStatResp);
        Histogram splitByDayAgg = logStatResp.getAggregations().get("split_by_day");
        ArrayList<DotData> dauDotArr = new ArrayList<>();
        ArrayList<DotData> logCountDotArr = new ArrayList<>();
        for (Histogram.Bucket entry : splitByDayAgg.getBuckets()) {
            DateTime key = (DateTime) entry.getKey();
            Cardinality uniqueMac = entry.getAggregations().get("unique_mac");
            //这里通过key.getDayOfMonth()不太对,如keyAsString:2016-02-01T00:00:00.000+08:00,对应的dayOfMonth值为31,所以暂时先截取字符串来获得
            String tmpDayOfMonth = entry.getKeyAsString().substring(8, 10);
            System.out.println("dayOfMonth:" + key.getDayOfMonth() + "dayOfMonthFromCalendar:" + tmpDayOfMonth + ",keyAsString:" + entry.getKeyAsString());
            dauDotArr.add(new DotData(tmpDayOfMonth, uniqueMac.getValue()));
            logCountDotArr.add(new DotData(tmpDayOfMonth, entry.getDocCount()));
        }
        dauDataSource.put("daily_active_user", dauDotArr);
        logCountDataSource.put("daily_user_log_count", logCountDotArr);
        picPathMap.put("dauStatOfHistory", JFreeChartUtil.drawLineChart(dauDataSource, "DAU_of_" + ymStr, "day_of_month", "user_count", ymdStr + "_dauStatOfHistory"));
        picPathMap.put("logCountStatOfHistory", JFreeChartUtil.drawLineChart(logCountDataSource, "daily_log_count_of_" + ymStr, "day_of_month", "log_count", ymdStr + "_logCountStatOfHistory"));
        //------------ end ------------

        //------------ 绘制迄今为止的周活跃数据 ------------
        Map<String, ArrayList<DotData>> wauDataSource = new HashMap<>();
        SearchResponse wauResp = esClient.prepareSearch("user_logs")
                .setSize(0)
                .addAggregation(AggregationBuilders.dateHistogram("split_by_week")
                        .field("create_time")
                        .interval(DateHistogramInterval.WEEK)
                        .timeZone("Asia/Shanghai")
                        .minDocCount(1)
                        .subAggregation(AggregationBuilders.cardinality("unique_mac").field("mac")))
                .execute()
                .actionGet();
//        System.out.println(wauResp);
        Histogram splitByWeekAgg = wauResp.getAggregations().get("split_by_week");
        ArrayList<DotData> wauDotArr = new ArrayList<>();
        int wauBucketsLen = splitByWeekAgg.getBuckets().size();
        int curIdx = 1;
        for (Histogram.Bucket entry : splitByWeekAgg.getBuckets()) {
            if (curIdx < wauBucketsLen) {//去掉最近这一周正在进行的统计数据
                DateTime key = (DateTime) entry.getKey();
                Cardinality uniqueMac = entry.getAggregations().get("unique_mac");
                wauDotArr.add(new DotData(key.getWeekOfWeekyear() + "", uniqueMac.getValue()));
                curIdx++;
            }
        }
        wauDataSource.put("weekly_active_user", wauDotArr);
        picPathMap.put("wauStatOfHistory", JFreeChartUtil.drawLineChart(wauDataSource, "WAU_of_" + ymStr, "week_of_year", "user_count", ymdStr + "_wauStatOfHistory"));
        //------------ end ------------

        //------------ 绘制迄今为止的月活跃数据 ------------
        Map<String, ArrayList<DotData>> mauDataSource = new HashMap<>();
        SearchResponse mauResp = esClient.prepareSearch("user_logs")
                .setSize(0)
                .addAggregation(AggregationBuilders.dateHistogram("split_by_month")
                        .field("create_time")
                        .interval(DateHistogramInterval.MONTH)
                        .timeZone("Asia/Shanghai")
                        .minDocCount(1)
                        .subAggregation(AggregationBuilders.cardinality("unique_mac").field("mac")))
                .execute()
                .actionGet();
        Histogram splitByMonthAgg = mauResp.getAggregations().get("split_by_month");
        ArrayList<DotData> mauDotArr = new ArrayList<>();
        for (Histogram.Bucket entry : splitByMonthAgg.getBuckets()) {
            DateTime key = (DateTime) entry.getKey();
            Cardinality uniqueMac = entry.getAggregations().get("unique_mac");
            //keyAsString:2016-02-01T00:00:00.000+08:00
            //mauDotArr.add(new DotData(key.getYear() + "_" + key.getMonthOfYear(), uniqueMac.getValue()));
            mauDotArr.add(new DotData(entry.getKeyAsString().substring(0, 7), uniqueMac.getValue()));
        }
        mauDataSource.put("monthly_active_user", mauDotArr);
        picPathMap.put("mauStatOfHistory", JFreeChartUtil.drawLineChart(mauDataSource, "MAU_of_" + ymStr, "month_of_year", "user_count", ymdStr + "_mauStatOfHistory"));
        //------------ end ------------
    }

    /**
     * 统计截止至当前日期的用户的各种信息
     *
     * @return
     */
    private String _statUserProfile(int preDayValue) {
        List<UserGenderStat> list = new ArrayList<>();
        int totalUserCount = 0;
        List<UserAgeRangeStat> list2 = new ArrayList<>();

        try {
            list = mapper.statMaleFemale();
//            totalUserCount = mapper.statUserTotalCount();
            list2 = mapper.statAgeRange();
            //线上运行时,报过错:com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: Communications link failure
            //这里不保证查询成功,mapper可能会报错,所以为了不影响整个发邮件逻辑的中断,还是要catch以下
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                if (list.size() == 0) list = mapper.statMaleFemale();
                if (list2.size() == 0) list2 = mapper.statAgeRange();
            } catch (Exception ex2) {
                logger.warn("------>_statUserProfile execute failure twice!!!!!");
                ex2.printStackTrace();
            }
        }

        int validUserCount = 0;
        StringBuilder htmlBuilder = new StringBuilder();
        if(list.size()>0){
            htmlBuilder.append("<br/><fieldset><b>------------截止至日期" + DateUtils.preDayYMDStr(preDayValue) + "的用户性别统计数据------------</b><table style=\"border:solid blue 2px\">");
            list.sort(new Comparator<UserGenderStat>() {
                public int compare(UserGenderStat o1, UserGenderStat o2) {
                    Object genderNumber1 = genderNumber.get(o1.getGender());
                    Object genderNumber2 = genderNumber.get(o2.getGender());
                    if (genderNumber1 == null || genderNumber2 == null) {
                        return 1;
                    } else if ((int) genderNumber1 > (int) genderNumber2) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });
            for (UserGenderStat e : list) {
                htmlBuilder.append("<tr><th  style=\"border:solid blue 1px;text-align:left\">"
                        + e.getGender()
                        + "</th><td style=\"border:solid blue 1px;width:190px;text-align:center\">"
                        + e.getCount()
                        + "</td></tr>");
                validUserCount += e.getCount();
            }
            htmlBuilder.append("<tr><th  style=\"border:solid blue 1px;text-align:left\">总数"
                    + "</th><td style=\"border:solid blue 1px;width:190px;text-align:center\">"
                    + validUserCount
                    + "</td></tr>");

//        htmlBuilder.append("<tr><th  style=\"border:solid blue 1px;text-align:left\">总的用户"
//                + "</th><td style=\"border:solid blue 1px;width:190px;text-align:center\">"
//                + totalUserCount
//                + "</td></tr>");
            htmlBuilder.append("</table>");
        }

        if(list2.size()>0){
            validUserCount = 0;
            htmlBuilder.append("<b>------------截止至日期" + DateUtils.preDayYMDStr(preDayValue) + "的用户年龄范围统计数据------------</b><table style=\"border:solid blue 2px\">");
            list2.sort(new Comparator<UserAgeRangeStat>() {
                public int compare(UserAgeRangeStat o1, UserAgeRangeStat o2) {
                    Object ageRangeNumber1 = ageRangeValue.get(o1.getAgeRange());
                    Object ageRangeNumber2 = ageRangeValue.get(o2.getAgeRange());
                    if (ageRangeNumber1 == null || ageRangeNumber2 == null) {
                        return 1;
                    } else if ((int) ageRangeNumber1 > (int) ageRangeNumber2) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });
            for (UserAgeRangeStat e : list2) {
                if (e.getAgeRange().length() > 0) {//某些用户的年龄范围是一个长度为0的字符串
                    htmlBuilder.append("<tr><th  style=\"border:solid blue 1px;text-align:left\">"
                            + e.getAgeRange()
                            + "</th><td style=\"border:solid blue 1px;width:190px;text-align:center\">"
                            + e.getCount()
                            + "</td></tr>");
                    validUserCount += e.getCount();
                }
            }
            htmlBuilder.append("<tr><th  style=\"border:solid blue 1px;text-align:left\">总数"
                    + "</th><td style=\"border:solid blue 1px;width:190px;text-align:center\">"
                    + validUserCount
                    + "</td></tr>");

//        htmlBuilder.append("<tr><th  style=\"border:solid blue 1px;text-align:left\">总的用户"
//                + "</th><td style=\"border:solid blue 1px;width:190px;text-align:center\">"
//                + totalUserCount
//                + "</td></tr>");
            htmlBuilder.append("</table></fieldset>");
        }

        return htmlBuilder.toString();
    }

    private void _statLogCount(String logName, Map source,Map<String, Long> logStatMap,Map<String, LogExtra> logStatExtraInfotMap) {
        try {
            logStatMap.put(logName, logStatMap.get(logName) + 1);
        } catch (NullPointerException e) {
            logStatMap.put("other", logStatMap.get("other") + 1);
        }

        if (logName.equals("language")) {
            LanguageLogExtra extra = (LanguageLogExtra) logStatExtraInfotMap.get(logName);
            extra.countLanguage((String) source.get("language"));
        } else if (logName.equals("custom_visual")) {
            CustomVisualLogExtra extra = (CustomVisualLogExtra) logStatExtraInfotMap.get(logName);
            extra.appendText((String) source.get("text"));
        } else if (logName.equals("doubleclick")) {
            DoubleClickLogExtra extra = (DoubleClickLogExtra) logStatExtraInfotMap.get(logName);
            extra.countFavourite(source.get("favorite") == null || (boolean) source.get("favorite"));
        } else if (logName.equals("tcp_longpress")) {
            TcpLongPressLogExtra extra = (TcpLongPressLogExtra) logStatExtraInfotMap.get(logName);
            Object pressType = source.get("type");
            extra.countPressType(pressType == null ? null : (String) pressType);
        } else if (logName.equals("voice")) {
            VoiceLogExtra extra = (VoiceLogExtra) logStatExtraInfotMap.get(logName);
            Object voiceType = source.get("type");
            extra.countVoice(voiceType == null ? null : (String) voiceType);
        } else if (logName.equals("nlpsearch")) {
            NlpSearchExtra extra = (NlpSearchExtra) logStatExtraInfotMap.get(logName);
            Object searchType = source.get("type");
            extra.countType(searchType == null ? null : (String) searchType);
        }


        logStatMap.put("total", logStatMap.get("total") + 1);
    }

//    @Scheduled(cron = "0 19 4 * * *")
    public void dailyStat() {
        try {
            System.out.println("---------------start to execute dailyStat--------------");
            int preDayValue = 1;
            String common = "<div>日期:" + DateUtils.preDayYMDStr(preDayValue) + "<br/>服务器环境:"
                    + System.getProperty("spring.profiles.active") + "</div><br/>";
            String htmlToPM = common;
            String htmlToNLP = common;
            String htmlToRecSys = common;
            Map<String, String> calcResult = _statDailyData(preDayValue);
            htmlToPM += calcResult.get("toPM");
            htmlToPM += _statUserProfile(preDayValue);
            htmlToNLP += calcResult.get("toNLP");
            htmlToRecSys += calcResult.get("toRecSys");
            List<String> picNames = new ArrayList<>();

            //目前我们可能会统计的各种图
            String[] ourStatPicNames = new String[]{"dauStatOfHistory", "logCountStatOfHistory", "wauStatOfHistory", "mauStatOfHistory"};
            for (String picName : ourStatPicNames) {
                if (calcResult.get(picName) != null) picNames.add(calcResult.get(picName));
            }

            String[] PM_AddressList = new String[]{"stat_report@vinci.im"};
            String[] RecSys_AddressList = new String[]{"stat_report@vinci.im"};
            String[] NLP_AddressList = new String[]{"stat_report@vinci.im"};
//            String[] PM_AddressList = new String[]{"fantasticsoul@vinci.im"};
//            String[] RecSys_AddressList = new String[]{"fantasticsoul@vinci.im"};
//            String[] NLP_AddressList = new String[]{"fantasticsoul@vinci.im"};
//            String[] PM_AddressList = new String[]{"fantasticsoul@vinci.im"};
//            String[] RecSys_AddressList = new String[]{"fantasticsoul@vinci.im"};
//            String[] NLP_AddressList = new String[]{};
                try {
                    if (picNames.size() > 0) {
                        MailUtils.sendMailWithPics("【数据】vinci_server日统计数据", htmlToPM, picNames.toArray(new String[]{}), PM_AddressList);
                    } else {
                        MailUtils.sendMail("【数据】vinci_server日统计数据", htmlToPM, PM_AddressList);
                    }
                } catch (Exception e1) {
                    logger.warn("------>>>exception occurred in PM_AddressList while send mail to:" + PM_AddressList);
                    e1.printStackTrace();
                }
                try {
                    MailUtils.sendMail("【数据】NLP统计数据", htmlToNLP, NLP_AddressList);
                } catch (Exception e2) {
                    logger.warn("------>>>exception occurred in NLP_AddressList while send mail to:" + NLP_AddressList);
                    e2.printStackTrace();
                }
                try {
                    MailUtils.sendMail("【数据】推荐系统日统计数据", htmlToRecSys, RecSys_AddressList);
                } catch (Exception e3) {
                    logger.warn("------>>>exception occurred in RecSys_AddressList while send mail to:" + RecSys_AddressList);
                    e3.printStackTrace();
                }
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn(e.getMessage());
        }
    }


    /**
     * 判断这条log是否是有效的可用于推荐系统计算的log
     * 判断依据,包含from属性,且对应的值是recommend,返回true
     *
     * @param source
     * @param needCheckMLength 是否检查mlength属性为不为空
     * @return
     */
    private boolean isValidRecommendLog(Map source, boolean needCheckMLength, boolean needCheckRomVersion) {
        if (needCheckMLength) {
            Object mlength = source.get("mlength");//老版本的log可能没有mlength
            if (mlength == null || (int) mlength <= 0) {
                return false;
            }
        }

        if (needCheckRomVersion) {
            String romVersion = (String) source.get("rom_version");
//            logger.info("The rom version is " + romVersion);
            //对于推荐系统需要统计的各种指标,V1.8.0以下都是不可用的
            List<String> versionNumList = Arrays.asList(romVersion.replace("V", "").split("\\."));

            if (Integer.valueOf(versionNumList.get(0)) < 1) {
                return false;
            } else {
                if (Integer.valueOf(versionNumList.get(0)) == 1) {
                    if (Integer.valueOf(versionNumList.get(1)) < 8) {
                        return false;
                    }
                }
            }
        }

        Object fromValue = source.get("from");
        if (fromValue != null) {
            String fromValueStr = (String) fromValue;
            if (fromValueStr.equals(EventSourceType.RECOMMEND.value())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //key:欲记录的数据的type,value:这条数据对应的LogStatData
    private void _recordStatLogs(Map<String, LogStatData> statMap) {
        System.out.println("before _recordStatLogs");
        BulkRequestBuilder bulkRequest = esClient.prepareBulk();
        for (Iterator ite = statMap.entrySet().iterator(); ite.hasNext(); ) {
            Entry<String, LogStatData> entry = (Entry) ite.next();
            LogStatData value = entry.getValue();
            //这样setId是为了防止重复执行定时任务会产生多余的统计数据
            IndexRequestBuilder indexRequest = esClient.prepareIndex("log_stat", entry.getKey(),
                    value.getName() + value.getYear() + "_" + value.getMonth() + "_" + value.getDay_of_month())
                    .setSource(JsonUtils.encode(value));
            bulkRequest.add(indexRequest);
        }
        System.out.println("before bulkRequest.execute()");
        BulkResponse res = bulkRequest.execute().actionGet();//加上actionGet保证这个函数的调用时同步的,后面的逻辑要用到头一天的数据
        if (res.hasFailures()) {
            System.out.println(res.buildFailureMessage());
        }
        System.out.println("bulkRequest took time " + res.getTook() + res.getItems());
        System.out.println("bulkRequest BulkItemResponse " + res.getItems());
    }


//    public static Client transportClient(){
//        String cluster_name = "my-application";
//        String node_address = "localhost";
//        String node_port = "9300";
//        Settings settings = Settings.settingsBuilder()
//                .put("cluster.name", cluster_name)//指定集群名称
//                .put("client.transport.sniff", true)//探测集群中机器状态,通过这种方式可以只知道其中一个节点的ip和端口,底层自动嗅探到其他的es服务器节点
//                .build();
//        System.out.println("cluster name:" + cluster_name + ",node address:" + node_address + ",node port:" + node_port);
//        Client client = null;
//        try {
//            client = TransportClient.builder().settings(settings).build()
//                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(node_address), Integer.parseInt(node_port)));
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//        return client;
//    }

}

class KeywordDetail {
    public int onlineCount = 0;
    public int offlineCount = 0;
    //存放各个版本的语义文本
    public HashMap<String,String> understandTextMap = new HashMap<String,String>();
    public HashMap<String,Integer> appendCountMap = new HashMap<String,Integer>();
    public String understandType = "";
    public int appendTypeCount = 0;

    public KeywordDetail(int onlineCount, int offlineCount, String understandText, String understandType,String romVersion) {
        this.offlineCount = offlineCount;
        this.onlineCount = onlineCount;
        appendTextIfNotContain(romVersion,understandText);
        appendTypeIfNotContain(understandType);
    }

    public KeywordDetail() {
    }

    public void incOnlineCount() {
        this.onlineCount++;
    }

    public void incOfflineCount() {
        this.offlineCount++;
    }

    public void appendTextIfNotContain(String romVersion, String text) {
        if (understandTextMap.get(romVersion) == null) {
            understandTextMap.put(romVersion, "");
        }
        String understandText = understandTextMap.get(romVersion);

        if (understandText.indexOf(text) == -1) {
            if (appendCountMap.get(romVersion) == null) {
                understandText += ("| " + text + " | ");
            } else {
                understandText += (text + " | ");
            }
            appendCountMap.put(romVersion, 1);
        }
        understandTextMap.put(romVersion, understandText);
    }

    public void appendTypeIfNotContain(String type) {
        if (this.understandType.indexOf(type) == -1) {
            if (this.appendTypeCount == 0) {
                this.understandType += ("| " + type + " | ");
            } else {
                this.understandType += (type + " | ");
            }
            this.appendTypeCount++;
        }
    }
}

/**
 * 某个用户一天里的各种数据统计
 */
class UserStatData {

    public String imei = "";

    public static Map<String,String> passSongLogName = new HashMap<String,String>(){{
        put("next","next");
        put("homepress","homepress");
    }};

    //------------------------所有的听歌数据统计------------------------

    public long passSongCount = 0;

    public long likeSongCount = 0;

    public long playOverSongCount = 0;

    public long playSongCount = 0;

    //用户总的听头机时长,duration值来自于pre,next,musicplay,homepress(正在听歌时的homepress)
    public long playDuration = 0;


    //------------------------累计使用推荐系统的数据统计------------------------

    //某个用户使用推荐系统的次数,依赖于pre,next,homepress(一种特殊的切歌)里recommend_list_id来计算
    public long useRecCount = 0;

    //喜欢的推荐歌曲的数目
    public long likeRecSongCount = 0;

    //播放的推荐歌曲的数目
    public long playRecSongCount = 0;

    //听完推荐歌曲的数目
    public long playOverRecSongCount = 0;

    //某个用户使用推荐系统听的所有歌的总时长
    public long playRecSongDuration = 0;


    //------------------------最近一次使用推荐系统的数据统计------------------------

    //最新一次使用推荐系统的总时间
    public long latestRecSysPlayDuration = 0;

    //最新一次使用推荐系统听歌数目
    public long latestRecSysPlaySongCount = 0;

    //最新一次使用推荐系统听完的歌数目
    public long latestRecSysPlayOverSongCount = 0;

    //最新一次使用推荐系统喜欢的歌数目
    public long latestRecSysLikeSongCount = 0;


    public UserStatData(String imei) {
        this.imei = imei;
    }

    private Map<String, Integer> useRecommendMap = new HashMap<String, Integer>();

    private String latestRecSysId = null;
    private long latestRecSysLogCT = 0;

    /**
     * 统计这个用户的各种听歌数据
     * @param source es返回的文档
     * @param logName doubleclick,pre,next,musicplay,homepress
     */
    public void stat(Map source,String logName) {
        long createTime = (long)source.get("create_time");
        //其他log里叫recommend_list_id,homepress里叫list_id,这里优先取recommend_list_id
        Object listIdObj = source.get("recommend_list_id") != null ? source.get("recommend_list_id") : source.get("list_id");
        String version = (String)source.get("rom_version");

        //V1.8.0的musicplay漏加了recommend_list_id字段,这里校正下
        if (version.equals("V1.8.0") && logName.equals("musicplay") && latestRecSysId!=null) {
            listIdObj = latestRecSysId;
        }

        if (listIdObj != null) {
            String listId = (String) listIdObj;
            if (!useRecommendMap.containsKey(listId)) {
                useRecCount++;
                useRecommendMap.put(listId, 0);
            }

            //------------最近一次使用推荐系统的统计------------
            if (latestRecSysId == null || (!latestRecSysId.equals(listId) && createTime > latestRecSysLogCT)) {
                latestRecSysId = listId;
                latestRecSysLogCT = createTime;
                latestRecSysPlayDuration = 0;
                latestRecSysPlaySongCount = 0;
                latestRecSysPlayOverSongCount = 0;
                latestRecSysLikeSongCount = 0;

            }
            if (latestRecSysId.equals(listId)) {
                if (passSongLogName.containsKey(logName)) {
                    latestRecSysPlayDuration += Long.parseLong(source.get("duration").toString());
                    latestRecSysPlaySongCount++;
                    //给个30s的误差,离结束还有30秒之内的时间,都算这首歌播放结束
                    if (Long.parseLong(source.get("mlength").toString()) - Long.parseLong(source.get("duration").toString()) <= 30) {
                        latestRecSysPlayOverSongCount++;
                    }
                } else if (logName.equals("doubleclick")) {
                    latestRecSysLikeSongCount++;
                }
            }

            //------------end------------

            //------------累计使用推荐系统的统计(顺带也包含了所有播歌数据的统计)------------
            if (passSongLogName.containsKey(logName)) {
                playRecSongCount++;
                playSongCount++;
                long duration = Long.parseLong(source.get("duration").toString());
                if (Long.parseLong(source.get("mlength").toString()) - duration <= 30) {
                    playOverRecSongCount++;
                    playOverSongCount++;
                }else{
                    passSongCount++;
                }
                playRecSongDuration += duration;
                playDuration += duration;
            } else if(logName.equals("doubleclick")){
                Object isLiked = source.get("favorite");
                if (isLiked == null ) {
                    isLiked = true;
                }

                if ((Boolean)isLiked) {
                    likeSongCount++;
                    likeRecSongCount++;
                }
            }
            //------------end------------
        } else {
            //------------所有播歌数据的统计------------
            if (passSongLogName.containsKey(logName)) {
                playSongCount++;
                long duration = Long.parseLong(source.get("duration").toString());
                if (Long.parseLong(source.get("mlength").toString()) - duration <= 30) {
                    playOverSongCount++;
                } else {
                    passSongCount++;
                }
                playDuration += duration;
            } else if (logName.equals("doubleclick")) {
                Object isLiked = source.get("favorite");
                if (isLiked == null ) {
                    isLiked = true;
                }

                if ((Boolean)isLiked) {
                    likeSongCount++;
                    likeRecSongCount++;
                }
            }
        }
    }

    public String toString() {
        return "{imei:"+imei+",latestRecSysPlaySongCount:" + latestRecSysPlaySongCount + ",latestRecSysPlayOverSongCount:" + latestRecSysPlayOverSongCount
                + ",latestRecSysPlayDuration:" + latestRecSysPlayDuration + ",latestRecSysLikeSongCount:" + latestRecSysLikeSongCount
                + ",playRecSongCount:" + playRecSongCount + ",playOverRecSongCount:" + playOverRecSongCount + ",latestRecSysId:" + latestRecSysId
                + ",playSongCount," + playSongCount + ",playOverSongCount:" + playOverSongCount + "}";
    }

}

//该pojo类记录statistic每日统计的数据
class LogStatData {
    private int year;
    private int month;
    private int day_of_month;
    private int week_of_month;
    private String name;
    private long count;

    public LogStatData(int year, int month, int day_of_month, int week_of_month, String name, long count) {
        this.year = year;
        this.month = month;
        this.day_of_month = day_of_month;
        this.week_of_month = week_of_month;
        this.name = name;
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public int getDay_of_month() {
        return day_of_month;
    }

    public void setDay_of_month(int day_of_month) {
        this.day_of_month = day_of_month;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWeek_of_month() {
        return week_of_month;
    }

    public void setWeek_of_month(int week_of_month) {
        this.week_of_month = week_of_month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

}

interface LogExtra{
    public String getExtraInfo();
}


class LanguageLogExtra implements LogExtra{
    private Map<String, Integer> map = new HashMap<>();

    public LanguageLogExtra() {

    }

    public void countLanguage(String language) {
        try {
            map.put(language, map.get(language) + 1);
        } catch (NullPointerException e) {
            map.put(language, 1);
        }
    }

    public String getExtraInfo() {
        Set<Entry<String, Integer>> set = map.entrySet();
        String info = "";
        for (Entry<String, Integer> entry : set) {
            info += entry.getKey() + ":" + entry.getValue() + ",";
        }
        int len = info.length();
        if (len > 0) {
            return info.substring(0, len - 1);
        } else {
            return info;
        }
    }
}

class CustomVisualLogExtra implements LogExtra{

    private String info = "";

    public CustomVisualLogExtra() {

    }

    public void appendText(String text) {
        if (info.indexOf(text) == -1) {
            info += text + " | ";
        }
    }

    public String getExtraInfo() {
        return info;
    }
}

class TcpLongPressLogExtra implements LogExtra {
    private int screenCount = 0;
    private int touchPadCount = 0;
    private int unkownCount = 0;

    public void countPressType(String type) {
        if (type == null) {
            unkownCount++;
        } else {
            if (type.equals("screen")) {
                screenCount++;
            } else if (type.equals("touchpad")) {
                touchPadCount++;
            } else {
                unkownCount++;
            }
        }
    }

    public String getExtraInfo() {
        return "触摸板:" + touchPadCount + ", 屏幕:" + screenCount + ", 不确定类型:" + unkownCount;
    }
}

class DoubleClickLogExtra implements LogExtra {
    private int favouriteTrueCount = 0;
    private int favouriteFalseCount = 0;

    public void countFavourite(boolean isFavourite) {
        if (isFavourite) {
            favouriteTrueCount++;
        } else {
            favouriteFalseCount++;
        }
    }

    public String getExtraInfo() {
        return "收藏歌曲:" + favouriteTrueCount + ", 取消收藏:" + favouriteFalseCount;
    }
}

class VoiceLogExtra implements LogExtra {
    private int stepCount = 0;
    private int settingStepCount = 0;
    private int myHeartBeatCount = 0;
    private int unknownCount = 0;

    public void countVoice(String voiceType) {
        if (voiceType == null) {
            unknownCount++;
        } else if (voiceType.equals("step")) {
            stepCount++;
        } else if (voiceType.equals("setting_step")) {
            settingStepCount++;
        } else if (voiceType.equals("heart")) {
            myHeartBeatCount++;
        }
    }

    public String getExtraInfo() {
        String to_return = "我的心率:" + myHeartBeatCount + ", 今日步数:" + stepCount + ", 设定计步目标:" + settingStepCount;
        if (unknownCount > 0) {
            to_return += ",未知类型语音:" + unknownCount;
        }
        return to_return;
    }
}

class NlpSearchExtra implements LogExtra {
    private int translateCount = 0;
    private int unknownCount = 0;

    public void countType(String searchType) {
        if (searchType == null) {
            unknownCount++;
        } else if (searchType.equals("translate")) {
            translateCount++;
        }
    }

    public String getExtraInfo() {
        String to_return = "搜索英语听力:" + translateCount;
        if (unknownCount > 0) {
            to_return += ",未知搜索语音:" + unknownCount;
        }
        return to_return;
    }
}

