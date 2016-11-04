package im.vinci.server.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;

import java.util.Date;

/**
 * 用户自己的各种设置都在这里
 * Created by tim@vinci on 16/8/31.
 */
public class UserSettings {
    //数据库id
    @JsonIgnore
    private long id;

    //对应的real user id
    @JsonIgnore
    private long realUserId;

    //收藏成功分享到V圈
    private Boolean collectToShare;

    //运动结束分享到V圈
    private Boolean sportsToShare;

    //评论内容分享到V圈
    private Boolean commentToShare;

    @JsonIgnore
    private Date dtCreate;

    @JsonIgnore
    private Date dtUpdate;

    public long getId() {
        return id;
    }

    public UserSettings setId(long id) {
        this.id = id;
        return this;
    }

    public long getRealUserId() {
        return realUserId;
    }

    public UserSettings setRealUserId(long realUserId) {
        this.realUserId = realUserId;
        return this;
    }

    public Boolean getCollectToShare() {
        return collectToShare;
    }

    public UserSettings setCollectToShare(Boolean collectToShare) {
        this.collectToShare = collectToShare;
        return this;
    }

    public Boolean getSportsToShare() {
        return sportsToShare;
    }

    public UserSettings setSportsToShare(Boolean sportsToShare) {
        this.sportsToShare = sportsToShare;
        return this;
    }

    public Boolean getCommentToShare() {
        return commentToShare;
    }

    public UserSettings setCommentToShare(Boolean commentToShare) {
        this.commentToShare = commentToShare;
        return this;
    }

    public Date getDtCreate() {
        return dtCreate;
    }

    public UserSettings setDtCreate(Date dtCreate) {
        this.dtCreate = dtCreate;
        return this;
    }

    public Date getDtUpdate() {
        return dtUpdate;
    }

    public UserSettings setDtUpdate(Date dtUpdate) {
        this.dtUpdate = dtUpdate;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("realUserId", realUserId)
                .add("collectToShare", collectToShare)
                .add("sportsToShare", sportsToShare)
                .add("commentToShare", commentToShare)
                .add("dtCreate", dtCreate)
                .add("dtUpdate", dtUpdate)
                .toString();
    }
}
