package im.vinci.server.search.domain.himalayas;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 喜马拉雅大分类
 * Created by tim@vinci on 15/12/23.
 */
public class HimalayaCategory implements Serializable{
    private int id;
    private String title;
    @JsonProperty("cover_url")
    private String coverUrl;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
}
