package im.vinci.server.feed.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import im.vinci.server.user.domain.UserInfo;

import java.io.Serializable;
import java.util.Date;

/**
 * Feed基本内容
 * Created by frank on 16-8-5.
 */
public class Feed  implements Serializable {

    @JsonIgnore
    private long id;

    private long feedId;

    @JsonIgnore
    private long userId;

    //这个用于返回结果时替代userId
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UserInfo userInfo;

    private String content;

    private String topic;

    private String pageType;

    private String pageContent;

    //当前帖子的评论数
    private int commentCount;

    private boolean isDeleted;

    private Date dtCreate;

    public static Feed getDeleteFeedInstance(long feedId) {
        return new Feed().setFeedId(feedId).setDeleted(true);
    }

    public long getId() {
        return id;
    }

    public Feed setId(long id) {
        this.id = id;
        return this;
    }

    public long getFeedId() {
        return feedId;
    }

    public Feed setFeedId(long feedId) {
        this.feedId = feedId;
        return this;
    }

    public long getUserId() {
        return userId;
    }

    public Feed setUserId(long userId) {
        this.userId = userId;
        return this;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public Feed setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Feed setContent(String content) {
        this.content = content;
        return this;
    }

    public String getTopic() {
        return topic;
    }

    public Feed setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public String getPageType() {
        return pageType;
    }

    public Feed setPageType(String pageType) {
        this.pageType = pageType;
        return this;
    }

    public String getPageContent() {
        return pageContent;
    }

    public Feed setPageContent(String pageContent) {
        this.pageContent = pageContent;
        return this;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public Feed setCommentCount(int commentCount) {
        this.commentCount = commentCount;
        return this;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public Feed setDeleted(boolean deleted) {
        isDeleted = deleted;
        return this;
    }

    public Date getDtCreate() {
        return dtCreate;
    }

    public Feed setDtCreate(Date dtCreate) {
        this.dtCreate = dtCreate;
        return this;
    }
}
