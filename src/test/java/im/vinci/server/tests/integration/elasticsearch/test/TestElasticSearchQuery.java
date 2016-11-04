package im.vinci.server.tests.integration.elasticsearch.test;

import im.vinci.server.elasticsearch.domain.base.BasicDocModel;
import im.vinci.server.elasticsearch.esconst.Indices;
import im.vinci.server.tests.integration.zzktmptest.TestHelper;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.engine.DocumentMissingException;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.max.MaxBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.*;
/**
 * Created by zhongzhengkai on 16/1/16.
 */
public class TestElasticSearchQuery {

    // prod外网  123.57.23.226
    // prod内网  10.172.220.110


//    Client esClient = TestHelper.getESClient();
//    Client esClient = TestHelper.getESClient("vinci","101.200.159.42","9300"); //stage es实例
    Client esClient = TestHelper.getESClient("vinci-es-cluster-01","123.57.23.226","9300");//production es实例

    //production es实例
//    Client esClient = TestHelper.getESClient("vinci-es-cluster-01","123.57.23.226","9300");

/*    @Test
    public void getMaxPlayCountMusicIdOfArtist() {
        System.out.println("final resutl is:"+getMaxPlayCountMusicIdOfArtist("Twins"));
    }*/


//    @Test
//    public void getMaxPlayCountMusicIds() {
//        System.out.println("final resutl is:"+_getMaxPlayCountMusicIds("Twins",10));
//    }

//    @Test
    public void genWAU() {
        _genWAU();
    }

//    @Test
    public void testAccountExtraInfoUpdate() throws Exception{
        _testAccountExtraInfoUpdate();
    }

//    @Test
    public void testAccountExtraInfoSave() throws Exception{
        _testAccountExtraInfoSave();
    }

//    @Test
    public void getOneSongInfoFromXiamiMusic() throws Exception{
        _getOneSongInfoFromXiamiMusic();
    }

    public String getMaxPlayCountMusicIdOfArtist(String artistName){
        TermQueryBuilder singersTermQuery=termQuery("singers.singers_na",artistName);

        long startTime = System.currentTimeMillis();
//{
//    "query": {
//        "term": {
//            "singers.singers_na": "Twins"
//        }
//    },
//    "size": 2,
//    "aggs": {
//        "max_pc": {
//            "max": {
//                "field": "play_count"
//            }
//        }
//    }
//}
        //构造的查询json如上所示
        MaxBuilder agg = AggregationBuilders.max("max_play_count").field("play_count");
        SearchRequestBuilder reqBuilder = esClient.prepareSearch(Indices.XIAMI_MUSIC.getIndexName()).setTypes("xiami_music")
                .setSearchType(SearchType.COUNT);
        if (singersTermQuery != null) {
            reqBuilder.setQuery(singersTermQuery);
        }
        SearchResponse res = reqBuilder.addAggregation(agg)
                .execute()
                .actionGet();


//        {
//            "aggregations": {
//                "max_play_count": {
//                    "value": 1532910
//                }
//            }
//        }
        Aggregation aggResult = res.getAggregations().get("max_play_count");
        Object valueOfAgg = aggResult.getProperty("value");
        if (valueOfAgg == null) {//没有查到这个歌手
            return null;
        }

        Double maxPlayCount = (Double) valueOfAgg;

        SearchResponse res2nd = esClient.prepareSearch(Indices.XIAMI_MUSIC.getIndexName()).setTypes("xiami_music")
                .setQuery(boolQuery()
                        .must(rangeQuery("play_count").gte(maxPlayCount)).must(singersTermQuery))
                .setSize(10)
                .execute()
                .actionGet();

        System.out.println("result is:"+maxPlayCount);
        System.out.println(res2nd.toString());

        SearchHits hits = res2nd.getHits();
        long total = hits.getTotalHits();
        System.out.println("total is:"+total);
        if(total<=0){
            return null;
        }
        SearchHit hit = hits.getHits()[0];
        Map source = hit.getSource();
        System.out.println(source.get("song_id") + "_xiami");
        System.out.println(System.currentTimeMillis()-startTime);
        return source.get("song_id") + "_xiami";
    }


    public List<String> _getMaxPlayCountMusicIds(String artistName, int listLength){
        SearchRequestBuilder reqBuilder = esClient.prepareSearch(Indices.XIAMI_MUSIC.getIndexName())
                .setTypes("xiami_music")
                .setFetchSource(new String[]{"play_count"},new String[]{});

        if (artistName != null) {
            reqBuilder.setQuery(termQuery("singers.singers_na", artistName));
        }

        SearchResponse res = reqBuilder.addSort("play_count", SortOrder.DESC).setSize(listLength).execute()
                .actionGet();
        System.out.println(res.toString());
        SearchHit[] hits = res.getHits().hits();
        List<String> ids = new ArrayList<>();
        for(SearchHit hit : hits){
            ids.add(hit.getId() + "_xiami");
        }
        return ids;
    }
    public void _genWAU(){
        SearchResponse WAU = esClient.prepareSearch("user_logs")
                .setSize(0)
                .addAggregation(
                        AggregationBuilders.dateHistogram("split_by_week")
                                .field("create_time")
                                .interval(DateHistogramInterval.WEEK)
                                .timeZone("Asia/Shanghai")
                                .minDocCount(1)
                                .subAggregation(AggregationBuilders.cardinality("unique_mac").field("mac")))
                .execute()
                .actionGet();
        System.out.println(WAU.toString());


        Histogram aggResult = WAU.getAggregations().get("split_by_week");
//        InternalMultiBucketAggregation aggResult = WAU.getAggregations().get("split_by_week");
//        BucketCollector aggResult = WAU.getAggregations().get("split_by_week");
        System.out.println(aggResult.getBuckets());
        for (Histogram.Bucket entry : aggResult.getBuckets()) {
            DateTime key = (DateTime) entry.getKey();    // Key
            String keyAsString = entry.getKeyAsString(); // Key as String
            long docCount = entry.getDocCount();         // Doc count
            entry.getAggregations().get("unique_mac");
            System.out.println(keyAsString+" "+key+" "+docCount);
            Cardinality uniqueMac = entry.getAggregations().get("unique_mac");
            System.out.println(uniqueMac.getValue());
        }

        System.out.println(((Object[])aggResult.getProperty("unique_mac")));
        System.out.println(((org.elasticsearch.search.aggregations.metrics.cardinality.InternalCardinality)(((Object[])aggResult.getProperty("unique_mac"))[0])).getValue());
        System.out.println(((Object[])aggResult.getProperty("unique_mac")).length);

        System.out.println(aggResult.getBuckets());

//        List<Histogram.Bucket> list = (List<Histogram.Bucket>) aggResult.getProperty("buckets");
//        System.out.println(list);
    }

    public void _testAccountExtraInfoUpdate() throws Exception{
        BasicDocModel model = new BasicDocModel(){
            @Override
            public void initDocMap(Map<String,Object> map) {
                map.put("create_time",333);
                map.put("imei","gogogo");
            }

        };
        XContentBuilder builder = model.getXContentBuilder();
        Map<String,Object> docMap= model.getDocAsMap();

        System.out.println(docMap);
        builder = model.reInitDocXContentBuilder(new BasicDocModel.ReInitDocXContentAction(){
            @Override
            public void reInit(XContentBuilder builderRef) throws IOException{
                builderRef.startObject();
                builderRef.field("create_time",555);
                builderRef.field("imei","exexex");
                builderRef.endObject();
            }
        });
        System.out.println(docMap);

        builder = model.reInitDocMap(new BasicDocModel.ReInitMapAction(){
            @Override
            public void reInit(Map<String,Object> docMapRef){
                docMapRef.put("create_time",666);
                docMapRef.put("imei","exexex");
            };
        });
        System.out.println(docMap);
        System.out.println(builder.string());

        try{
            UpdateResponse response = esClient.prepareUpdate("account_extra_info","account_extra_info","00:08:22:92:0d:fc")
                    .setDoc(model.getXContentBuilder()).get();
            System.out.println(response.toString());
        }catch(DocumentMissingException e){
            System.out.println("missing!!!!!");
        }
    }

    public void _testAccountExtraInfoSave() throws Exception{
        IndexResponse response = esClient.prepareIndex("account_extra_info","account_extra_info","zzk1")
                .setSource(new Object[]{"mac","zzk1","create_time",777,"sn","i am vinci"}).execute().actionGet();
    }

    public void _getOneSongInfoFromXiamiMusic(){
        GetResponse resp = esClient.prepareGet("xiami_music","xiami_music","3451928").get();
        System.out.println("--->"+resp.toString());
        System.out.println(resp.getSourceAsString());
    }

}

