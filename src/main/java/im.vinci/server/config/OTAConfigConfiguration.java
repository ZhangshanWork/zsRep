package im.vinci.server.config;

import im.vinci.server.device.security.OTAConfigIPInterceptor;
import im.vinci.server.device.security.OTAConfigSecurityInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by ytl on 15/12/5.
 */
@Configuration
public class OTAConfigConfiguration extends WebMvcConfigurerAdapter  {

    @Configuration
    @Profile(UserProfile.INTG)
    @PropertySource("classpath:/intg/otaconfig.properties")
    static class RestSecurityIntgConfiguration {
    }

    @Configuration
    @Profile(UserProfile.QACI)
    @PropertySource("classpath:/qaci/otaconfig.properties")
    static class RestSecurityQaciConfiguration {
    }

    @Configuration
    @Profile(UserProfile.PROD)
    @PropertySource("classpath:/prod/otaconfig.properties")
    static class RestSecurityProdAConfiguration {
    }

    @Configuration
    @Profile(UserProfile.PROD_US)
    @PropertySource("classpath:/prod_us/otaconfig.properties")
    static class RestSecurityProd_USAConfiguration {
    }

    @Bean
    public OTAConfigSecurityInterceptor otaConfigSecurityInterceptor() {
        return new OTAConfigSecurityInterceptor();
    }

    @Bean
    public OTAConfigIPInterceptor otaConfigIpInterceptor() {
        return new OTAConfigIPInterceptor();
    }

    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(otaConfigSecurityInterceptor()).addPathPatterns("/vinci/device/otaconfig/**")
                .excludePathPatterns("/vinci/device/otaconfig/login").excludePathPatterns("/static/**")
                .excludePathPatterns("/vinci/device/otaconfig/isForbidden");
        registry.addInterceptor(otaConfigIpInterceptor()).addPathPatterns("/vinci/device/otaconfig/**")
                .excludePathPatterns("/vinci/device/otaconfig/isForbidden");
    }
}
