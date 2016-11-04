package im.vinci.server.tests.integration.zzktmptest;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by zhongzhengkai on 15/12/4.
 */
public class TestHelper {


    public static Client getESClient(){
        String cluster_name = "my-application";
        String node_address = "localhost";
        String node_port = "9300";
        return _getESClient(cluster_name,node_address,node_port);
    }

    public static Client getESClient(String clusterName,String hostAddress,String hostPort){
        return _getESClient(clusterName,hostAddress,hostPort);
    }

    private static Client _getESClient(String clusterName,String hostAddress,String hostPort){
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", clusterName)//指定集群名称
//                .put("client.transport.sniff", true)//探测集群中机器状态,通过这种方式可以只知道其中一个节点的ip和端口,底层自动嗅探到其他的es服务器节点
                .build();
        System.out.println("-------------------------------------------------------------------------------------------");
        System.out.println("elastic search cluster name:" + clusterName + ",node address:" + hostAddress + ",node port:" + hostPort);
        System.out.println("-------------------------------------------------------------------------------------------");
        Client client = null;
        try {
            System.out.println("InetAddress.getByName(hostAddress):"+InetAddress.getByName(hostAddress));
            client = TransportClient.builder().settings(settings).build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostAddress), Integer.parseInt(hostPort)));
        } catch (UnknownHostException e){
            e.printStackTrace();
        }
        return client;
    }

}