package im.vinci.server.feed.domain;

import java.io.Serializable;

/**
 * 搜索用的一些请求参数
 * Created by mayuchen on 16/8/26.
 */
public class FeedSearch implements Serializable{

    private String topic;

    private String pageType;

    private int idDeleted;

    // all attention special
    private String attentionType;
    // enable whe special
    private long userId;

    public String getTopic() {
        return topic;
    }

    public FeedSearch setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public String getPageType() {
        return pageType;
    }

    public FeedSearch setPageType(String pageType) {
        this.pageType = pageType;
        return this;
    }

    public int getIdDeleted() {
        return idDeleted;
    }

    public FeedSearch setIdDeleted(int idDeleted) {
        this.idDeleted = idDeleted;
        return this;
    }

    public String getAttentionType() {
        return attentionType;
    }

    public FeedSearch setAttentionType(String attentionType) {
        this.attentionType = attentionType;
        return this;
    }

    public long getUserId() {
        return userId;
    }

    public FeedSearch setUserId(long userId) {
        this.userId = userId;
        return this;
    }
}
