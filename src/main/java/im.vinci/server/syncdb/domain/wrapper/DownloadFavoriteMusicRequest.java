package im.vinci.server.syncdb.domain.wrapper;

/**
 * Created by mingjie on 16/10/20.
 */
public class DownloadFavoriteMusicRequest {
    private int lastFavoriteMusicId;
    private long userId;
    private int pageSize;

    public int getLastFavoriteMusicId() {
        return lastFavoriteMusicId;
    }

    public void setLastFavoriteMusicId(int lastFavoriteMusicId) {
        this.lastFavoriteMusicId = lastFavoriteMusicId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
