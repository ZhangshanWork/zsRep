package im.vinci.server.naturelang.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
public class ContextListener implements ApplicationContextInitializer{
    Logger log = LoggerFactory.getLogger(this.getClass());
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		// TODO Auto-generated method stub
		System.out.println("###########################");
			try {
				Context.init();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("==========基础数据初始化发生异常啦============");
			}
	}
}
