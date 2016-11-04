package im.vinci.server.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;

import java.util.Date;

/**
 * Created by ASUS on 2016/8/17.
 */
public class UserCounts {
    @JsonIgnore
    private long userId;

    private int feedCount;

    private int followerCount;

    private int attentionerCount;

    private int collectionCount;

    private Integer messageUnreadCount;

    @JsonIgnore
    private Date dtCreate;
    @JsonIgnore
    private Date dtUpdate;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getFeedCount() {
        return feedCount;
    }

    public void setFeedCount(int feedCount) {
        this.feedCount = feedCount;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public int getAttentionerCount() {
        return attentionerCount;
    }

    public void setAttentionerCount(int attentionerCount) {
        this.attentionerCount = attentionerCount;
    }

    public int getCollectionCount() {
        return collectionCount;
    }

    public void setCollectionCount(int collectionCount) {
        this.collectionCount = collectionCount;
    }

    public Date getDtCreate() {
        return dtCreate;
    }

    public void setDtCreate(Date dtCreate) {
        this.dtCreate = dtCreate;
    }

    public Date getDtUpdate() {
        return dtUpdate;
    }

    public void setDtUpdate(Date dtUpdate) {
        this.dtUpdate = dtUpdate;
    }

    public Integer getMessageUnreadCount() {
        return messageUnreadCount;
    }

    public UserCounts setMessageUnreadCount(Integer messageUnreadCount) {
        this.messageUnreadCount = messageUnreadCount;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("userId", userId)
                .add("feedCount", feedCount)
                .add("followerCount", followerCount)
                .add("attentionerCount", attentionerCount)
                .add("collectionCount", collectionCount)
                .add("messageUnreadCount", messageUnreadCount)
                .add("dtCreate", dtCreate)
                .add("dtUpdate", dtUpdate)
                .toString();
    }
}
