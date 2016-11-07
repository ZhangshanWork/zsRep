package im.vinci.server.config;

import im.vinci.server.common.push.PushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * 客户端推送的配置
 * Created by tim@vinci on 16/7/25.
 */
@Configuration
public class MobileMsgPushConfiguration {
    @Configuration
    @Profile(UserProfile.INTG)
    @PropertySource("classpath:/intg/push.properties")
    static class MysqlIntgConfiguration {
    }

    @Configuration
    @Profile(UserProfile.QACI)
    @PropertySource("classpath:/qaci/push.properties")
    static class MysqlQaciConfiguration {
    }

    @Configuration
    @Profile(UserProfile.PROD)
    @PropertySource("classpath:/prod/push.properties")
    static class MysqlProdAConfiguration {
    }

    @Autowired
    Environment env;

    @Bean
    public PushService createPushService() {
        String headphoneAppKey = env.getRequiredProperty("push.headphone.appkey");
        String headphoneSecret = env.getRequiredProperty("push.headphone.secret");
        String mobileAppKey = env.getRequiredProperty("push.mobile.appkey");
        String mobileSecret = env.getRequiredProperty("push.mobile.secret");
        return new PushService(headphoneAppKey,headphoneSecret,mobileAppKey,mobileSecret);
    }
}
