package im.vinci.server.config;

import com.aliyun.oss.OSSClient;
import im.vinci.server.other.domain.profile.UserMusicProfileConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import javax.servlet.MultipartConfigElement;

/**
 * Created by henryhome on 2/12/15.
 */
@Configuration
public class ServiceConfiguration {

    @Autowired
    public Environment env;

    @Configuration
    @Profile(UserProfile.INTG)
    @PropertySource(value = {"classpath:/intg/authentication.properties",
            "classpath:/intg/user.properties",
            "classpath:/intg/system.properties",
            "classpath:/intg/mobile.properties"})
    static class ServiceIntgConfiguration {
    }

    @Configuration
    @Profile(UserProfile.QACI)
    @PropertySource(value = {"classpath:/qaci/authentication.properties",
            "classpath:/qaci/user.properties",
            "classpath:/qaci/system.properties",
            "classpath:/qaci/mobile.properties"})
    static class ServiceQaciConfiguration {
    }

    @Configuration
    @Profile(UserProfile.PROD)
    @PropertySource(value = {"classpath:/prod/authentication.properties",
            "classpath:/prod/user.properties",
            "classpath:/prod/system.properties",
            "classpath:/prod/mobile.properties"})
    static class ServiceProdAConfiguration {
    }

	@Configuration
	@Profile(UserProfile.PROD_US)
	@PropertySource(value = {"classpath:/prod_us/authentication.properties",
			"classpath:/prod_us/device.properties",
			"classpath:/prod_us/user.properties"})
	static class ServiceProd_USConfiguration {
	}


    @Bean
    public UserMusicProfileConstant userMusicProfileConstant() {
        UserMusicProfileConstant userMusicProfileConstant = new UserMusicProfileConstant();
        userMusicProfileConstant.setEnv(env);
        return userMusicProfileConstant;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize("2MB");
        factory.setMaxRequestSize("5MB");
        factory.setFileSizeThreshold("1MB");
        return factory.createMultipartConfig();
    }

    @Bean(name = "mobileUploadImgOssClient", destroyMethod = "shutdown")
    public OSSClient client() {
        OSSClient client = new OSSClient(env.getProperty("mobile.upload_img.endpoint"),
                env.getProperty("mobile.upload_img.accessKeyId"), env.getProperty("mobile.upload_img.accessKeySecret"));
        return client;
    }
}



