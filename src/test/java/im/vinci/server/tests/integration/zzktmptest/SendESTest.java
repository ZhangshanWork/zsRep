package im.vinci.server.tests.integration.zzktmptest;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * Created by zhongzhengkai on 15/12/1.
 */
public class SendESTest {

    public static void main(String[] args){
        Client client = TestHelper.getESClient("vinci","101.200.159.42","9300");
        SearchResponse response = client.prepareSearch("userlogs")
                .setTypes("poweron")
                .setPostFilter(QueryBuilders.rangeQuery("createtime").from(0).to(20000000000000L))
                .setFrom(1)
                .setSize(10)
                .setExplain(true)
                .execute().actionGet();
        String ret = response.getHits().getAt(0).getSourceAsString();
        System.out.println(ret);

    }


}
