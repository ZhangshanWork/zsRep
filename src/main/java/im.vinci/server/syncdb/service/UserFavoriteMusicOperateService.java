package im.vinci.server.syncdb.service;

import com.google.gson.Gson;
import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.syncdb.domain.ClientUserData;
import im.vinci.server.syncdb.domain.UserFavoriteMusic;
import im.vinci.server.syncdb.persistence.UserDataSyncMapper;
import im.vinci.server.syncdb.persistence.UserFavoriteMusicOperator;
import im.vinci.server.utils.BizTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import im.vinci.server.syncdb.domain.wrapper.DownloadFavoriteMusicResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据同步service
 * Created by tim@vinci on 16/7/26.
 */
@Service
public class UserFavoriteMusicOperateService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserDataSyncMapper userDataSyncMapper;

    @Autowired
    private UserFavoriteMusicOperator favoriteDataOperator;

    public DownloadFavoriteMusicResponse getUserData(long userId, int lastFavoriteMusicId, int pageSize) {
        return new BizTemplate<DownloadFavoriteMusicResponse>(getClass().getSimpleName() + ".getUserData") {
            @Override
            protected void checkParams() throws VinciException {
                if (userId <= 0) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "userId小于等于0", "userId必须大于0");
                }
                if (lastFavoriteMusicId < 0) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "lastFavoriteMusicId小于0", "lastFavoriteMusicId不能小于0");
                }
                if (pageSize <= 0) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "pageSize小于等于0", "pageSize必须大于0");
                }
            }

            @Override
            protected DownloadFavoriteMusicResponse process() throws Exception {
                DownloadFavoriteMusicResponse response = new DownloadFavoriteMusicResponse();

                List<UserFavoriteMusic> list = favoriteDataOperator.getUserData(userId, lastFavoriteMusicId, pageSize);
                for (UserFavoriteMusic music : list) {
                    music.setFavorite((byte) 1);
                }

                response.setData(list);

                return response;
            }
        }.execute();
    }

    public boolean updateData(final List<ClientUserData> list)
    {
        return new BizTemplate<Boolean>(getClass().getSimpleName() + ".updateData") {
            @Override
            protected void checkParams() throws VinciException {

            }

            @Override
            protected Boolean process() throws Exception {
                try {
                    Gson gson = new Gson();
                    List<UserFavoriteMusic> insertList = new ArrayList<UserFavoriteMusic>();
                    List<UserFavoriteMusic> deleteList = new ArrayList<UserFavoriteMusic>();
                    for (ClientUserData data : list) {
                        try {
                            if (!StringUtils.isEmpty(data.getData())) {
                                UserFavoriteMusic music = gson.fromJson(data.getData(), UserFavoriteMusic.class);
                                if (music != null && music.getFavorite() == 1) {
                                    music.setUserId(data.getRealUserId());
                                    if (music.getIsDelete()) {
                                        deleteList.add(music);
                                    } else {
                                        insertList.add(music);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            logger.info("parse data error, not FavoriteMusic");
                        }
                    }

                    if(!insertList.isEmpty())
                    {
                        favoriteDataOperator.insertData(insertList);
                    }

                    if(!deleteList.isEmpty())
                    {
                        favoriteDataOperator.deleteData(deleteList);
                    }

                    return true;
                }catch(Exception e)
                {
                    logger.error("occurred unexpected error:", e);
                    return false;
                }
            }
        }.execute();
    }

}
