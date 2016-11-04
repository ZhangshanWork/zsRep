package im.vinci.server.feed.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Date;

/**
 * FeedComments 基本内容
 * Created by ASUS on 2016/8/10.
 */
public class FeedComments implements Serializable {

    @JsonIgnore
    private long id;

    private long commentId;

    private long feedId;//评论的帖子id

    private long userId;//谁发了这条评论

    private long replyToUserId; //回复给哪些用户

    private String commentText;//评论内容

    private int isDeleted;//是否删除

    private Date dtCreate;

    @JsonIgnore
    private Date dtUpdate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCommentId() {
        return commentId;
    }

    public void setCommentId(long commentId) {
        this.commentId = commentId;
    }

    public long getFeedId() {
        return feedId;
    }

    public void setFeedId(long feedId) {
        this.feedId = feedId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public int getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(int isDeleted) {
        this.isDeleted = isDeleted;
    }

    public long getReplyToUserId() {
        return replyToUserId;
    }

    public FeedComments setReplyToUserId(long replyToUserId) {
        this.replyToUserId = replyToUserId;
        return this;
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
}
