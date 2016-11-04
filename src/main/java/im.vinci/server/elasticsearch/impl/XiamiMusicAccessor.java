package im.vinci.server.elasticsearch.impl;

import im.vinci.server.elasticsearch.esconst.Indices;
import im.vinci.server.elasticsearch.index.XiamiMusic;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.max.MaxBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Created by zhongzhengkai on 16/1/14.
 */
@Service
public class XiamiMusicAccessor implements XiamiMusic {
    @Resource(name = "esClient")
    private Client esClient;

    @Override
    public String getMaxPlayCountMusicId() {
        return _getMaxPlayCountMusicId(null);
    }

    @Override
    public String getMaxPlayCountMusicIdByArtist(String artistName) {
        return _getMaxPlayCountMusicId(artistName);
    }

    @Override
    public List<String> getTopNMaxPlayCountMusicIds(int topN) {
        return _getTopNMaxPlayCountMusicIds(null,topN);
    }

    @Override
    public List<String> getTopNMaxPlayCountMusicIdsByArtist(String artistName,int topN) {
        return _getTopNMaxPlayCountMusicIds(artistName,topN);
    }

    private String _getMaxPlayCountMusicId(String artistName){
        TermQueryBuilder singersTermQuery = null;
        if (artistName != null) {
            singersTermQuery = termQuery("singers.singers_na", artistName);
        }

        MaxBuilder agg = AggregationBuilders.max("max_play_count").field("play_count");
        SearchRequestBuilder reqBuilder = esClient.prepareSearch(Indices.XIAMI_MUSIC.getIndexName()).setTypes("xiami_music")
                .setSearchType(SearchType.COUNT);
        if (singersTermQuery != null) {
            reqBuilder.setQuery(singersTermQuery);
        }
        SearchResponse res = reqBuilder.addAggregation(agg)
                .execute()
                .actionGet();

        Aggregation aggResult = res.getAggregations().get("max_play_count");
        Object valueOfAgg = aggResult.getProperty("value");
        if (valueOfAgg == null) {//没有查到这个歌手
            return null;
        }

        Double maxPlayCount = (Double) valueOfAgg;

        SearchResponse res2nd = esClient.prepareSearch(Indices.XIAMI_MUSIC.getIndexName()).setTypes("xiami_music")
                .setQuery(boolQuery()
                        .must(rangeQuery("play_count").gte(maxPlayCount)).must(singersTermQuery))
                .setSize(1)
                .execute()
                .actionGet();

        SearchHits hits = res2nd.getHits();
        long total = hits.getTotalHits();
        if(total<=0){
            return null;
        }
        SearchHit hit = hits.getHits()[0];
        Map source = hit.getSource();
        return source.get("song_id") + "_xiami";
    }

    private List<String> _getTopNMaxPlayCountMusicIds(String artistName,int topN){
        SearchRequestBuilder reqBuilder = esClient.prepareSearch(Indices.XIAMI_MUSIC.getIndexName())
                .setTypes("xiami_music")
                .setFetchSource(new String[]{"play_count"},new String[]{});

        if (artistName != null) {
            reqBuilder.setQuery(termQuery("singers.singers_na", artistName));
        }

        SearchResponse res = reqBuilder.addSort("play_count", SortOrder.DESC).setSize(topN).execute()
                .actionGet();
        SearchHit[] hits = res.getHits().hits();
        List<String> ids = new ArrayList<>();
        for(SearchHit hit : hits){
            ids.add(hit.getId() + "_xiami");
        }
        return ids;
    }

}
