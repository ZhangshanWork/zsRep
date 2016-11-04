package im.vinci.server.tests.integration.config;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;

import static org.junit.Assert.assertNotNull;

/**
 * Created by tim@vinci on 15/11/16.
 */

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = {VinciApplication.class})
//@WebAppConfiguration
//@IntegrationTest
//@ActiveProfiles(UserProfile.INTG)
public class OnsConfigurationTest {

    @Autowired
    ApplicationContext context;

    @Resource(name="onsDeviceUserLogProducer")
    Producer producer;

//    @Test
    public void testCreateMessageShouldBePrototype() {
        Message msg1 = context.getBean("onsDeviceUserLogMessage", Message.class);
        Message msg2 = context.getBean("onsDeviceUserLogMessage", Message.class);
        //assertNotEquals(msg1, msg2);
    }
//    @Test
    public void testSendMessage() {
        Message msg1 = context.getBean("onsDeviceUserLogMessage", Message.class);
        msg1.setBody("abce".getBytes());
        msg1.setKey("1234");
        assertNotNull(producer.send(msg1));
    }
}