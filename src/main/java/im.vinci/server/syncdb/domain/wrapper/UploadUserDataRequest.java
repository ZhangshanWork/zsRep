package im.vinci.server.syncdb.domain.wrapper;

import im.vinci.server.syncdb.domain.ClientUserData;

import java.io.Serializable;
import java.util.List;

/**
 * 用户数据上传的request
 * Created by tim@vinci on 16/7/27.
 */
public class UploadUserDataRequest implements Serializable{

    private String table;

    private int total;

    private List<ClientUserData> records;

    public String getTable() {
        return table;
    }

    public UploadUserDataRequest setTable(String table) {
        this.table = table;
        return this;
    }

    public int getTotal() {
        return total;
    }

    public UploadUserDataRequest setTotal(int total) {
        this.total = total;
        return this;
    }

    public List<ClientUserData> getRecords() {
        return records;
    }

    public UploadUserDataRequest setRecords(List<ClientUserData> records) {
        this.records = records;
        return this;
    }
}
