package im.vinci.server.user.domain.wrappers;

/**
 * 用户消息箱, 在feed评论中回复我
 */

import com.google.common.base.Objects;
import im.vinci.server.feed.domain.Feed;
import im.vinci.server.feed.domain.wrapper.ListFeedCommentsResponse;
import im.vinci.server.user.domain.UserMessage;

import java.io.Serializable;

public class UserMessageComments implements Serializable {

    private Feed feed;

    private ListFeedCommentsResponse feedComments;

    private UserMessage feedMessage;

    public ListFeedCommentsResponse getFeedComments() {
        return feedComments;
    }

    public UserMessageComments setFeedComments(ListFeedCommentsResponse feedComments) {
        this.feedComments = feedComments;
        return this;
    }

    public UserMessage getFeedMessage() {
        return feedMessage;
    }

    public UserMessageComments setFeedMessage(UserMessage feedMessage) {
        this.feedMessage = feedMessage;
        return this;
    }

    public Feed getFeed() {
        return feed;
    }

    public UserMessageComments setFeed(Feed feed) {
        this.feed = feed;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserMessageComments that = (UserMessageComments) o;
        return Objects.equal(feed, that.feed) &&
                Objects.equal(feedComments, that.feedComments) &&
                Objects.equal(feedMessage, that.feedMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(feed, feedComments, feedMessage);
    }
}
