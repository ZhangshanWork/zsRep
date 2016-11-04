package im.vinci.server.syncdb.controller;

import im.vinci.server.security.ApiSecurityLabel;
import im.vinci.server.syncdb.domain.UserSyncFileAuth;
import im.vinci.server.syncdb.service.UserFileSyncService;
import im.vinci.server.utils.UserContext;
import im.vinci.server.utils.apiresp.ResultObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户同步文件和授权接口
 * Created by tim@vinci on 16/10/19.
 */
@RestController
@RequestMapping(value = "/vinci/user/syncfile", produces = "application/json;charset=UTF-8")
public class UserSyncFileController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserFileSyncService userFileSyncService;

    @RequestMapping(value = "/auth",method = RequestMethod.POST)
    @ApiSecurityLabel(isCheckLogin = true)
    public ResultObject<UserSyncFileAuth> downloadUserData(@RequestParam("b_type")String businessType) {
        return new ResultObject<>(userFileSyncService.auth(UserContext.getUserInfo().getId(),businessType));
    }
}
