package im.vinci.server.tests.integration.elasticsearch;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * Created by zhongzhengkai on 15/11/30.
 */
public class basicSearchAPI {

    private static RestTemplate restTemplate = new RestTemplate();

//    public static void main(String[] args){
//        searchAll();
//        testRestTemplate();
//    }

    public static Client transportClient(){
        String cluster_name = "my-application";
        String node_address = "localhost";
        String node_port = "9300";
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", cluster_name)//指定集群名称
                .put("client.transport.sniff", true)//探测集群中机器状态,通过这种方式可以只知道其中一个节点的ip和端口,底层自动嗅探到其他的es服务器节点
                .build();
        System.out.println("cluster name:" + cluster_name + ",node address:" + node_address + ",node port:" + node_port);
        Client client = null;
        try {
            client = TransportClient.builder().settings(settings).build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(node_address), Integer.parseInt(node_port)));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return client;
    }


    public static void searchAll(){
        SearchResponse response = transportClient().prepareSearch().execute().actionGet();
        System.out.println(response.getHits().getAt(0).getSourceAsString());
    }

    public static void testRestTemplate(){
        String userHasActionURL = "http://123.56.149.244/vinci/notify/user_has_action";
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(userHasActionURL);
        MultiValueMap<String, Object> mvm = new LinkedMultiValueMap<String, Object>();
        mvm.add("user_id", "ggggg2222");
        mvm.add("log_name", "gogogo");
        URI uri = uriComponentsBuilder.build().encode().toUri();
        restTemplate.postForObject(uriComponentsBuilder.build().encode().toUri(), mvm, Object.class);
    }
}
