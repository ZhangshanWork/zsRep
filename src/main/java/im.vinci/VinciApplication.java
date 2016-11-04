package im.vinci;

import im.vinci.server.config.*;
import im.vinci.server.naturelang.listener.ContextListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Created by henryhome on 2/17/15.
 */
@SpringBootApplication
@Import({
        CacheConfiguration.class,
        ControllerConfiguration.class,
        ServiceConfiguration.class,
        //DaoConfiguration.class,
        ElasticsearchConfiguration.class,
        OnsConfiguration.class,
        OTAConfigConfiguration.class,
})
public class VinciApplication {

    public static void main(String[] args)  {
//        SpringApplication.run(VinciApplication.class, args);
        SpringApplication app = new SpringApplication(VinciApplication.class);
        app.addInitializers(new ContextListener());  //添加启动
        app.run(args);
    }

}
