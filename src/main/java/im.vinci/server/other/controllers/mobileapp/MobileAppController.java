package im.vinci.server.other.controllers.mobileapp;

import im.vinci.server.other.services.mobileapp.MobileAppPicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;

/**
 * Created by ASUS on 2016/7/22.
 */
@RestController
@RequestMapping(value = "/vinci/MobileApp", produces = "application/json;charset=UTF-8")
public class MobileAppController {
    @Autowired
    private MobileAppPicService mobileAppPicService;

    @RequestMapping(value = "/getMobileAppPicurl", method= RequestMethod.GET)
    public HashMap<String,String> getURL()
        throws Exception {
        HashMap<String,String> pic=new HashMap();
        String picPath = mobileAppPicService.getPicUrl(1);
        pic.put("pic",picPath);
        return pic;
    }


}
