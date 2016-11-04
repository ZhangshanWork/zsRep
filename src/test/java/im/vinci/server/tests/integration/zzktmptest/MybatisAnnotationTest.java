package im.vinci.server.tests.integration.zzktmptest;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import im.vinci.server.device.domain.Device;
import im.vinci.server.device.persistence.DeviceMapper;
import im.vinci.server.other.domain.account.VinciUserDetailsBuilder;
import im.vinci.server.other.domain.preset.Playlistname;
import im.vinci.server.other.domain.preset.PresetMusic;
import im.vinci.server.other.domain.preset.UserPresetMusic;
import im.vinci.server.other.persistence.fetch.AccountFetchMapper;
import im.vinci.server.other.persistence.fetch.PresetFetchMapper;
import im.vinci.server.other.persistence.fetch.UserPresetFetchMapper;
import im.vinci.server.other.persistence.modify.UserPresetModifyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhongzhengkai on 15/12/21.
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = {VinciApplication.class})
//@WebAppConfiguration
//@IntegrationTest
//@ActiveProfiles(UserProfile.INTG)
public class MybatisAnnotationTest {
    @Autowired
    ApplicationContext context;

    @Autowired
    AccountFetchMapper accountFetchMapper;

    @Autowired
    DeviceMapper deviceFetchMapper;

    @Autowired
    PresetFetchMapper presetFetchMapper;

    @Autowired
    UserPresetFetchMapper userPresetFetchMapper;

   @Autowired
    UserPresetModifyMapper userPresetModifyMapper;

    @Resource(name="onsDeviceUserLogProducer")
    Producer producer;

    //    @Test
    public void testCreateMessageShouldBePrototype() {
        Message msg1 = context.getBean("onsDeviceUserLogMessage", Message.class);
        Message msg2 = context.getBean("onsDeviceUserLogMessage", Message.class);
        //assertNotEquals(msg1, msg2);
    }

//    @Test
    public void testAccountFetchMapper_f4() throws  Exception{
        VinciUserDetailsBuilder v = accountFetchMapper.getUserDetailsByDeviceId("00:08:22:ba:e0:fb");
        System.out.println(v);
    }

    //    @Test
    public void testDdeviceFetchMapper_f5() throws  Exception{
        Device v = deviceFetchMapper.getDeviceByMac("9e:74:3a:10:d2:9a");
        System.out.println(v);
    }

//    @Test
    public void testPresetFetchMapper_f6() throws  Exception{
        PresetMusic v = presetFetchMapper.getOnePersetMusicData();
        System.out.println("____________!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println(v.toString());
    }

//    @Test
    public void testPresetFetchMapper_f7() throws  Exception{
        List<Playlistname> v = presetFetchMapper.listTagNameByPlaylistNameIds(new String[]{"1"});
        System.out.println("____________!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println(v.toString());
    }

//    @Test
    public void testPresetFetchMapper_f9() throws  Exception{
        List<PresetMusic> v = presetFetchMapper.listPresetMusicByVersion(1);
        System.out.println("____________!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println(v.toString());
    }

//    @Test
    public void testPresetFetchMapper_f10() throws  Exception{

        List<UserPresetMusic> list = new ArrayList<UserPresetMusic>(){{
            add(new UserPresetMusic(1,"00:08:22:00:b9:fb",1,1,"xiami"));
            add(new UserPresetMusic(1,"00:08:22:00:b9:fb",2,2,"xiami"));
        }};
        boolean isSaved = userPresetModifyMapper.saveUserPresetMusicBatch(list);
        System.out.println(isSaved);
        if(isSaved) System.out.println("------>batch insert execution done^_^");
        List<UserPresetMusic> fetchList = userPresetFetchMapper.listUserPresetMusicByDeviceId("00:08:22:00:b9:fb");

        List<Integer> ids = new ArrayList<>();
        for (UserPresetMusic e:fetchList) {
            ids.add(e.getId());
        }
        System.out.println(ids.toString());
        boolean isDeleted = userPresetModifyMapper.deleteBatchByIds(ids);
        System.out.println(isDeleted);
    }

}
