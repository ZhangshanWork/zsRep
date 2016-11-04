package im.vinci.server.config;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by henryhome on 3/28/15.
 */
@Configuration
public class ElasticsearchConfiguration{

    @Autowired
    Environment env;

    @Configuration
    @Profile(UserProfile.INTG)
    @PropertySource("classpath:/intg/elasticsearch.properties")
    static class ElasticsearchIntgConfiguration {
    }

    @Configuration
    @Profile(UserProfile.QACI)
    @PropertySource("classpath:/qaci/elasticsearch.properties")
    static class ElasticsearchQaciConfiguration {
    }

    @Configuration
    @Profile(UserProfile.PROD)
    @PropertySource("classpath:/prod/elasticsearch.properties")
    static class ElasticsearchProdConfiguration {
    }

    @Configuration
    @Profile(UserProfile.PROD_US)
    @PropertySource("classpath:/prod_us/elasticsearch.properties")
    static class ElasticsearchProd_USConfiguration {
    }

    private static Logger logger = LoggerFactory.getLogger(ElasticsearchConfiguration.class);

    @Bean(name = "esClient",destroyMethod = "close")
    public Client transportClient() throws UnknownHostException{
        String cluster_name = env.getProperty("cluster.name");
        String node_address = env.getProperty("node.address");
        String node_port = env.getProperty("node.port");
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", cluster_name)//指定集群名称
//                .put("client.transport.sniff", true)//探测集群中机器状态,通过这种方式可以只知道其中一个节点的ip和端口,底层自动嗅探到其他的es服务器节点
                .build();
        logger.info("-------------------------------------------------------------------------------------------");
        logger.info("elastic search cluster name:" + cluster_name + ",node address:" + node_address + ",node port:" + node_port);
        logger.info("-------------------------------------------------------------------------------------------");
        Client client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(node_address), Integer.parseInt(node_port)));
        return client;
    }

//    @Bean
//    @Deprecated
//    /**
//     * 为了方便测试,获取的是嵌入在elasticsearch.jar包中德client实例,该es服务器运行在你本机上的JVM上,
//     * 仅仅是为了方便快速地做一些单元测试,这种方式仅用于开发测试环境
//     */
//    public Client embeddedClient() throws UnknownHostException{
//        return nodeBuilder().node().client();
//    }

}


