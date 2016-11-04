package im.vinci.server.syncdb.domain.wrapper;

import im.vinci.server.syncdb.domain.UserFavoriteMusic;

import java.util.List;

/**
 * Created by mingjie on 16/10/20.
 */
public class DownloadFavoriteMusicResponse {
    private List<UserFavoriteMusic> data;

    public List<UserFavoriteMusic> getData() {
        return data;
    }

    public void setData(List<UserFavoriteMusic> data) {
        this.data = data;
    }
}
