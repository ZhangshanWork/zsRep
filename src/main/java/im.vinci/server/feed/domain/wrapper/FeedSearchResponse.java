package im.vinci.server.feed.domain.wrapper;

import com.google.common.base.MoreObjects;
import im.vinci.server.feed.domain.Feed;

import java.io.Serializable;
import java.util.List;

/**
 * Feed搜索结果,返回给用户的
 * Created by tim@vinci on 16/9/2.
 */
public class FeedSearchResponse implements Serializable{

    private boolean hasMore;

    private List<Feed> feeds;

    public boolean isHasMore() {
        return hasMore;
    }

    public FeedSearchResponse setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
        return this;
    }

    public List<Feed> getFeeds() {
        return feeds;
    }

    public FeedSearchResponse setFeeds(List<Feed> feeds) {
        this.feeds = feeds;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("hasMore", hasMore)
                .add("feeds", feeds)
                .toString();
    }
}
