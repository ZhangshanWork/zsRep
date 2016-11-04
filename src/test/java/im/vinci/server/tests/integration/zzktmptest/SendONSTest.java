package im.vinci.server.tests.integration.zzktmptest;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.PropertyKeyConst;

import java.util.Properties;

/**
 * Created by zhongzhengkai on 15/12/1.
 */
public class SendONSTest {
    public static void main(String[] args){
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.ProducerId, "PID_wangzhe_test");
        properties.put(PropertyKeyConst.AccessKey, "aDo4ZtbecPs4GrAh");
        properties.put(PropertyKeyConst.SecretKey, "HO0xFyI8zRdGFV5KrH5oJgpG8qG5mY");
        Producer producer = ONSFactory.createProducer(properties);
        producer.start();

//        producer.send(_genMessage("recommend"));
//        producer.send(_genMessage("poweron"));
//        producer.send(_genMessage("poweroff"));
//        producer.send(_genMessage("screenon"));
        producer.send(_genMessage("xxxx1111"));
    }

    private static Message _genMessage(String tag){
        Message msg = new Message();
        msg.setTopic("TEST_DEVICE_USER_LOG");
        msg.setTag(tag);
        msg.setBody(_genJsonStr(tag).getBytes());
        msg.setKey("zzk_"+System.currentTimeMillis());
        return msg;
    }

    private static String _genJsonStr(String name){
        return "{\"name\":\""+name+"\",\"createtime\":1448539208021,\"info\":{\"mac\":\"00:08:22:d4:bc:fb\",\"imei\":\"864765020126028\",\"rom_version\":\"V1.1.1\",\"sn\":\"\"},\"data\":{\"baseline\":50,\"current\":50,\"mid\":\"1769668828_xiami\",\"source\":false}}";

    }


}
