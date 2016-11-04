package im.vinci.server.search.domain.music;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by tim@vinci on 15/11/27.
 * 用户打的标签
 */
public class MusicUserTags implements Serializable{
    @JsonProperty("tag")
    private String tagName;
    private long count;

    public MusicUserTags() {
    }
    public MusicUserTags(String tagName, Long count) {
        this.tagName = tagName;
        this.count = (count!=null?count:0);
    }
    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
