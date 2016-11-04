package im.vinci.server.feed.domain.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import im.vinci.server.user.domain.UserInfo;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by ASUS on 2016/8/11.
 */
public class ListFeedCommentsResponse implements Serializable{

    @JsonIgnore
    private long id;

    private long commentId;

    private long feedId;//评论的帖子id

    @JsonIgnore
    private long userId;//谁发了这条评论

    private String commentText;//评论内容

    private boolean isDeleted;//是否删除

    private Date dtCreate;//评论发布时间

    @JsonIgnore
    private Date dtUpdate;//评论修改时间

    private UserInfo userInfo;

    public static ListFeedCommentsResponse getDeleteCommentInstance(long feedId, long commentId) {
        return new ListFeedCommentsResponse().setFeedId(feedId).setCommentId(commentId).setDeleted(true);
    }

    public long getId() {
        return id;
    }

    public ListFeedCommentsResponse setId(long id) {
        this.id = id;
        return this;
    }

    public long getCommentId() {
        return commentId;
    }

    public ListFeedCommentsResponse setCommentId(long commentId) {
        this.commentId = commentId;
        return this;
    }

    public long getFeedId() {
        return feedId;
    }

    public ListFeedCommentsResponse setFeedId(long feedId) {
        this.feedId = feedId;
        return this;
    }

    public long getUserId() {
        return userId;
    }

    public ListFeedCommentsResponse setUserId(long userId) {
        this.userId = userId;
        return this;
    }

    public String getCommentText() {
        return commentText;
    }

    public ListFeedCommentsResponse setCommentText(String commentText) {
        this.commentText = commentText;
        return this;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public ListFeedCommentsResponse setDeleted(boolean deleted) {
        isDeleted = deleted;
        return this;
    }

    public Date getDtCreate() {
        return dtCreate;
    }

    public ListFeedCommentsResponse setDtCreate(Date dtCreate) {
        this.dtCreate = dtCreate;
        return this;
    }

    public Date getDtUpdate() {
        return dtUpdate;
    }

    public ListFeedCommentsResponse setDtUpdate(Date dtUpdate) {
        this.dtUpdate = dtUpdate;
        return this;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public ListFeedCommentsResponse setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
        return this;
    }
}
