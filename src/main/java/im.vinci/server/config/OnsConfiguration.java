package im.vinci.server.config;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import java.util.Properties;

/**
 * Created by tim@vinci on 15/11/13.
 * 阿里云Ons消息队列的配置
 */

@Configuration
public class OnsConfiguration {

    @Autowired
    Environment env;

    @Configuration
    @Profile(UserProfile.INTG)
    @PropertySource("classpath:/intg/ons.properties")
    static class OnsIntgConfiguration {
    }

    @Configuration
    @Profile(UserProfile.QACI)
    @PropertySource("classpath:/qaci/ons.properties")
    static class OnsQaciConfiguration {
    }

    @Configuration
    @Profile(UserProfile.PROD)
    @PropertySource("classpath:/prod/ons.properties")
    static class OnsProdAConfiguration {
    }


    @Configuration
    @Profile(UserProfile.PROD_US)
    @PropertySource("classpath:/prod_us/ons.properties")
    static class OnsProd_USConfiguration {
    }


    @Autowired
    ApplicationContext context;

    @Bean(name = "onsDeviceUserLogProducer",destroyMethod = "shutdown")
    @Profile({UserProfile.PROD,UserProfile.QACI})
    public Producer createProducer() {
        Properties properties = new Properties();

        properties.put(PropertyKeyConst.ProducerId, env.getRequiredProperty("ons.produceId"));
        properties.put(PropertyKeyConst.AccessKey, env.getRequiredProperty("ons.accessKey"));
        properties.put(PropertyKeyConst.SecretKey, env.getRequiredProperty("ons.secretKey"));
        Producer producer = ONSFactory.createProducer(properties);
        producer.start();
        return producer;
    }

    @Bean(name = "onsDeviceUserLogMessage")
    @Scope("prototype")
    public Message createDeviceUserLogMessage() {
        Message msg = new Message();
        msg.setTopic(env.getRequiredProperty("ons.deviceUserLog.topic"));
        return msg;
    }

    @Bean(name = "onsUserClientDataSyncLogMessage")
    @Scope("prototype")
    public Message createUserClientDataSyncLogMessage() {
        Message msg = new Message();
        msg.setTopic(env.getRequiredProperty("ons.user_client_data_sync.topic"));
        return msg;
    }

}
