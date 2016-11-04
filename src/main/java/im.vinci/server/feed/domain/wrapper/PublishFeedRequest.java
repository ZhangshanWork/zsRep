package im.vinci.server.feed.domain.wrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * Feed基本内容
 * Created by frank on 16-8-5.
 */
public class PublishFeedRequest implements Serializable {

    private String content;

    private String topic;

    private JsonNode pageContent;

    public String getContent() {
        return content;
    }

    public PublishFeedRequest setContent(String content) {
        this.content = content;
        return this;
    }

    public String getTopic() {
        return topic;
    }

    public PublishFeedRequest setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public JsonNode getPageContent() {
        return pageContent;
    }

    public PublishFeedRequest setPageContent(JsonNode pageContent) {
        this.pageContent = pageContent;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("content", content)
                .add("topic", topic)
                .add("pageContent", pageContent)
                .toString();
    }
}
