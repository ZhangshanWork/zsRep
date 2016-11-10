package im.vinci.server.recomd.service.impl;

import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.domain.RecommendSong;
import com.taobao.api.domain.StandardSong;
import im.vinci.server.naturelang.listener.Context;
import im.vinci.server.naturelang.utils.ObjectUtils;
import im.vinci.server.other.domain.wrappers.requests.music.MusicSimilarity;
import im.vinci.server.recomd.domain.RecomdInput;
import im.vinci.server.recomd.service.RecomdService;
import im.vinci.server.search.service.XiamiMusicSearchService;
import im.vinci.server.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mlc on 2016/9/21.
 */
@Service
public class RecomdServiceImpl implements RecomdService {
    @Resource(name = "esClient")
    private Client client;
    @Autowired
    private XiamiMusicSearchService xiamiMusicSearchService;

    private static DefaultTaobaoClient tclient = new DefaultTaobaoClient("http://gw.api.taobao.com/router/rest"
            , "23064829", "29ed3de5990627239d0fdbddd3e94b51", "json", 500000, 500000);

    //特定用户群
    private final String[] imeis = {"38:fd:fe:60:07:a2", "38:fd:fe:60:07:23", "38:fd:fe:60:0b:fb", "38:fd:fe:60:1c:0f", "38:fd:fe:60:17:64", "38:fd:fe:60:13:b2"};
    @Override
    public List<String> getRecomdList(RecomdInput recomdInput) throws ApiException {
        //对特定用户群，在时间：22：00 - 24：00；心率：60～90次/分   考虑时间跨度，时间设置在22~23
        //Arrays.binarySearch(imeis, recomdInput.getDevice_id());
        if (recomdInput.getHeartheat_current() >= 60 && recomdInput.getHeartheat_current() <= 90
                && DateUtils.getNowHours() >= 21 && DateUtils.getNowHours() <= 22) {

            return Context.generateRecmmdList();
        }
        List<String> list = new ArrayList<>();
        SearchHit[] searchHitses = getCollectionRecords(recomdInput.getDevice_id());
        if (searchHitses.length == 0) {
            list = getTodayRecommd(recomdInput.getSize());
        } else {
            list = getRecomdByIMEI(searchHitses, recomdInput.getSize());
        }
        return list;
    }

    //获取当前用户的收藏列表
    private SearchHit[] getCollectionRecords(String deviceId) {
        String flag = "sn";
        if (StringUtils.isNotBlank(deviceId)){
            if(deviceId.matches("([A-Fa-f0-9]{2}:){5}[A-Fa-f0-9]{2}")){
                flag = "mac";
            }
        }
        SearchResponse response = client.prepareSearch("user_logs")
                .setTypes("doubleclick")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.termQuery(flag, deviceId))
                .addSort(SortBuilders.fieldSort("create_time").order(SortOrder.DESC))
                .setSize(10)
                .execute().actionGet();
        return response.getHits().getHits();
    }

    //获取用户的收藏列表
    public List<String> getCollectionListStr(String deviceId) {
        List<String> result = new ArrayList<>();
        SearchHit[] searchHitses = getCollectionRecords(deviceId);
        for (SearchHit searchHit : searchHitses) {
            String id = ObjectUtils.objToString(searchHit.getSource().get("mid"));
            result.add(id);
        }
        return result;
    }

    //获取当前平台的播放量的top榜
    @Override
    public List<String> getTopCollections(int size) {
        List<String> list = new ArrayList<>();
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch("user_logs");
        searchRequestBuilder.setTypes("pre","next");
        TermsBuilder termsb = AggregationBuilders.terms("mid_").field("mid").order(Terms.Order.count(false)).size(size + 1);
        searchRequestBuilder.setQuery(QueryBuilders.rangeQuery("duration").from(0)).addAggregation(termsb);
        SearchResponse searchRes = searchRequestBuilder.execute().actionGet();

        //获取结果
        Terms fieldATerms = searchRes.getAggregations().get("mid_");
        for (Terms.Bucket filedABucket : fieldATerms.getBuckets()) {
            String musicId = (String) filedABucket.getKey();
            musicId = musicId.replace("_xiami", "");
            if (musicId.equals("0")) {
                continue;
            }
            list.add(musicId);
        }
        return list;
    }

    //获取相似歌曲
    @Override
    public List<String> getSimilarMusic(MusicSimilarity musicSimilarity) throws ApiException {
        List<String> result = new ArrayList<>();
        List<StandardSong> lists = xiamiMusicSearchService.searchSongSimilarity(Long.valueOf(musicSimilarity.getMusicId()),10L);
        for (StandardSong song : lists) {
            result.add(song.getSongId() + "");
        }
        return result;
    }


    //获取当天推荐的热门歌曲
    private List<String> getTodayRecommd(int size) throws ApiException {
        List<String> list = new ArrayList<>();
        List<RecommendSong> recommendSongList = xiamiMusicSearchService.todaySongs(50L,"");
        Collections.shuffle(recommendSongList);
        for (RecommendSong song : recommendSongList) {
            list.add(song.getSongId() + "_xiami");
        }
        return list;
    }

    private List<String> getRecomdByIMEI(SearchHit[] searchHitses,int size) throws ApiException {
        List<String> result = new ArrayList<>();
        List<String> imeiList = new ArrayList<>();
        for (SearchHit searchHit : searchHitses) {
            String id = ObjectUtils.objToString(searchHit.getSource().get("mid"));
            imeiList.add(id);
        }
        Collections.shuffle(imeiList);
        for(String baseId:imeiList){
            Long id = Long.valueOf(baseId.replace("_xiami", "").replace("_spotify",""));
            List<StandardSong> standardSongs = xiamiMusicSearchService.searchSongSimilarity(id, 100);
                for (StandardSong standardSong : standardSongs) {
                    result.add(standardSong.getSongId()+"_xiami");
                }
        }
        Collections.shuffle(result);
        result = result.subList(0, 30);
        return result;
    }

}
