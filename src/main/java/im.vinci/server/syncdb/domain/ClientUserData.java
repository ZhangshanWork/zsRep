package im.vinci.server.syncdb.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import im.vinci.server.utils.JsonUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户客户端数据
 * Created by tim@vinci on 16/7/25.
 */
public class ClientUserData implements Serializable{

    @JsonIgnore
    private long id;

    @JsonIgnore
    private long realUserId;

    private String tableName;

    private String dataPk;

    private String data;

    private boolean isDelete;

    private long updateVersion;

    private Date dtCreate;

    private Date dtUpdate;

    public ClientUserData() {
    }

    public ClientUserData(long realUserId, String tableName, String dataPk, String data) {
        this.realUserId = realUserId;
        this.tableName = tableName;
        this.dataPk = dataPk;
        this.data = data;
    }

    public ClientUserData(long realUserId, String tableName, String dataPk, String data, long updateVersion) {
        this.realUserId = realUserId;
        this.tableName = tableName;
        this.dataPk = dataPk;
        this.data = data;
        this.updateVersion = updateVersion;
    }

    public long getId() {
        return id;
    }

    public ClientUserData setId(long id) {
        this.id = id;
        return this;
    }

    public long getRealUserId() {
        return realUserId;
    }

    public ClientUserData setRealUserId(long realUserId) {
        this.realUserId = realUserId;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public ClientUserData setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public String getDataPk() {
        return dataPk;
    }

    public ClientUserData setDataPk(String dataPk) {
        this.dataPk = dataPk;
        return this;
    }

    public String getData() {
        return data;
    }

    public ClientUserData setData(String data) {
        this.data = data;
        return this;
    }

    public boolean getIsDelete() {
        return isDelete;
    }

    public ClientUserData setIsDelete(boolean delete) {
        isDelete = delete;
        return this;
    }
    public long getUpdateVersion() {
        return updateVersion;
    }

    public ClientUserData setUpdateVersion(long updateVersion) {
        this.updateVersion = updateVersion;
        return this;
    }

    public Date getDtCreate() {
        return dtCreate;
    }

    public ClientUserData setDtCreate(Date dtCreate) {
        this.dtCreate = dtCreate;
        return this;
    }

    public Date getDtUpdate() {
        return dtUpdate;
    }

    public ClientUserData setDtUpdate(Date dtUpdate) {
        this.dtUpdate = dtUpdate;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientUserData that = (ClientUserData) o;
        return getId() == that.getId() &&
                getRealUserId() == that.getRealUserId() &&
                getIsDelete() == that.getIsDelete() &&
                getUpdateVersion() == that.getUpdateVersion() &&
                Objects.equal(getTableName(), that.getTableName()) &&
                Objects.equal(getDataPk(), that.getDataPk()) &&
                Objects.equal(getData(), that.getData()) &&
                Objects.equal(getDtCreate(), that.getDtCreate()) &&
                Objects.equal(getDtUpdate(), that.getDtUpdate());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId(), getRealUserId(), getTableName(), getDataPk(), getIsDelete(), getUpdateVersion(), getDtCreate(), getDtUpdate());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("realUserId", realUserId)
                .add("tableName", tableName)
                .add("dataPk", dataPk)
                .add("data", data)
                .add("isDelete", isDelete)
                .add("updateVersion", updateVersion)
                .add("dtCreate", dtCreate)
                .add("dtUpdate", dtUpdate)
                .toString();
    }
}
