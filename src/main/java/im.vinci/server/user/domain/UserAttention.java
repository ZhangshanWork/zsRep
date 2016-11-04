package im.vinci.server.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by mayuchen on 16/8/2.
 */
public class UserAttention implements Serializable {

    @JsonIgnore
    private long id;

    @JsonIgnore
    private long userId;   //'用户id或称关注人',

    @JsonIgnore
    private long attentionUserId;     //'被关注人id',

    private Date dtCreate;

    @JsonIgnore
    private Date dtUpdate;

    //equal attentionUserId
    private UserInfo userInfo;

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public UserAttention setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
        return this;
    }

    public long getId() {
        return id;
    }

    public UserAttention setId(long id) {
        this.id = id;
        return this;
    }

    public long getUserId() {
        return userId;
    }

    public UserAttention setUserId(long userId) {
        this.userId = userId;
        return this;
    }

    public long getAttentionUserId() {
        return attentionUserId;
    }

    public UserAttention setAttentionUserId(long attentionUserId) {
        this.attentionUserId = attentionUserId;
        return this;
    }

    public Date getDtCreate() {
        return dtCreate;
    }

    public UserAttention setDtCreate(Date dtCreate) {
        this.dtCreate = dtCreate;
        return this;
    }

    public Date getDtUpdate() {
        return dtUpdate;
    }

    public UserAttention setDtUpdate(Date dtUpdate) {
        this.dtUpdate = dtUpdate;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserAttention attention = (UserAttention) o;

        if (id != attention.id) return false;
        if (userId != attention.userId) return false;
        if (attentionUserId != attention.attentionUserId) return false;
        if (dtCreate != null ? !dtCreate.equals(attention.dtCreate) : attention.dtCreate != null) return false;
        return dtUpdate != null ? dtUpdate.equals(attention.dtUpdate) : attention.dtUpdate == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (userId ^ (userId >>> 32));
        result = 31 * result + (int) (attentionUserId ^ (attentionUserId >>> 32));
        result = 31 * result + (dtCreate != null ? dtCreate.hashCode() : 0);
        result = 31 * result + (dtUpdate != null ? dtUpdate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserAttention{" +
                "id=" + id +
                ", userId=" + userId +
                ", attentionUserId=" + attentionUserId +
                ", dtCreate=" + dtCreate +
                ", dtUpdate=" + dtUpdate +
                '}';
    }
}


