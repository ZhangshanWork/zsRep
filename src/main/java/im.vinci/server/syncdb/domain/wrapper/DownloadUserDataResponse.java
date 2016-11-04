package im.vinci.server.syncdb.domain.wrapper;

import im.vinci.server.syncdb.domain.ClientUserData;

import java.io.Serializable;
import java.util.List;

/**
 * 下载用户数据的response
 * Created by tim@vinci on 16/7/27.
 */
public class DownloadUserDataResponse implements Serializable{

    private List<ClientUserData> records;

    //这次返回最大的update_version
    private long currentUpdateVersion;

    //是否还有数据,如果是true，可以用current_update_version再次请求
    private boolean hasMore;

    public DownloadUserDataResponse() {
    }

    public DownloadUserDataResponse(List<ClientUserData> records, long currentUpdateVersion, boolean hasMore) {
        this.records = records;
        this.currentUpdateVersion = currentUpdateVersion;
        this.hasMore = hasMore;
    }

    public List<ClientUserData> getRecords() {
        return records;
    }

    public DownloadUserDataResponse setRecords(List<ClientUserData> records) {
        this.records = records;
        return this;
    }

    public long getCurrentUpdateVersion() {
        return currentUpdateVersion;
    }

    public DownloadUserDataResponse setCurrentUpdateVersion(long currentUpdateVersion) {
        this.currentUpdateVersion = currentUpdateVersion;
        return this;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public DownloadUserDataResponse setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
        return this;
    }

}
