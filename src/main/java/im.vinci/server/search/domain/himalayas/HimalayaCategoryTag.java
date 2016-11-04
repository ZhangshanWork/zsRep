package im.vinci.server.search.domain.himalayas;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 喜马拉雅category tag
 * Created by tim@vinci on 15/12/23.
 */
public class HimalayaCategoryTag implements Serializable {
    private String name;
    @JsonProperty("cover_url_small")
    private String coverUrlSmall;
    @JsonProperty("cover_url_large")
    private String coverUrlLarge;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverUrlSmall() {
        return coverUrlSmall;
    }

    public void setCoverUrlSmall(String coverUrlSmall) {
        this.coverUrlSmall = coverUrlSmall;
    }

    public String getCoverUrlLarge() {
        return coverUrlLarge;
    }

    public void setCoverUrlLarge(String coverUrlLarge) {
        this.coverUrlLarge = coverUrlLarge;
    }
}
