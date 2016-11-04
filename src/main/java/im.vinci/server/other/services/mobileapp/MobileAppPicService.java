package im.vinci.server.other.services.mobileapp;

import im.vinci.server.other.persistence.mobileapp.MobileAppPicMapper;
import im.vinci.server.utils.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;

/**
 * Created by ASUS on 2016/7/22.
 */
@Service
public class MobileAppPicService {
    @Autowired
    private MobileAppPicMapper mobilAppPicMapper;
    @Autowired
    private Cache cache;
    public String getPicUrl(int id){
       if(cache.get("mobile_app_picurl",String.class)==null){
           cache.put("mobile_app_picurl", mobilAppPicMapper.getPicUrl(id),new Date(1000 * 600));
        }
        return  cache.get("mobile_app_picurl",String.class);
    }
}
