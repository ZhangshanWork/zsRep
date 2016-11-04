package im.vinci.server.syncdb.controller;

import im.vinci.server.security.ApiSecurityLabel;
import im.vinci.server.syncdb.domain.wrapper.DownloadFavoriteMusicRequest;
import im.vinci.server.syncdb.domain.wrapper.DownloadFavoriteMusicResponse;
import im.vinci.server.syncdb.service.UserFavoriteMusicOperateService;
import im.vinci.server.utils.apiresp.ResultObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by mingjie on 16/10/20.
 */
@RestController
@RequestMapping(value = "/vinci/user/favoritemusic", produces = "application/json;charset=UTF-8")
public class FavoriteMusicController {

    @Autowired
    private UserFavoriteMusicOperateService userFavoriteMusicOperateService;

    @RequestMapping(value = "/get",method = RequestMethod.POST)
    @ApiSecurityLabel(isCheckLogin = true)
    public ResultObject<DownloadFavoriteMusicResponse> getUserData(@RequestBody DownloadFavoriteMusicRequest request) {
        return new ResultObject<>(userFavoriteMusicOperateService.getUserData(request.getUserId(), request.getLastFavoriteMusicId(), request.getPageSize()));
    }
}
