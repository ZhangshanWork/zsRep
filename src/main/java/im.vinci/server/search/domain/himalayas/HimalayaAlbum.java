package im.vinci.server.search.domain.himalayas;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.common.base.Splitter;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

/**
 * 喜马拉雅专辑信息
 * Created by tim@vinci on 15/12/23.
 */
public class HimalayaAlbum {
    //album id
    private long id;
    //album name
    private String title;

    @JsonProperty("cover_url_small")
    private String coverUrlSmall;

    @JsonProperty("cover_url_middle")
    private String coverUrlMiddle;

    @JsonProperty("cover_url_large")
    private String coverUrlLarge;

    @JsonProperty("category_id")
    private int categoryId;

    @JsonProperty("category_title")
    private String categoryTitle;

    // album desc
    private String intro;

    private List<String> tags;

    //专辑最后更新时间
    @JsonProperty("last_uptrack_at")
    private long lastUptrackAt;

    //专辑中的声音file数量
    @JsonProperty("tracks_count")
    private int trackCount;

    @JsonProperty("plays_count")
    private long playCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<HimalayaTrack> tracks;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverUrlSmall() {
        return coverUrlSmall;
    }

    public void setCoverUrlSmall(String coverUrlSmall) {
        this.coverUrlSmall = coverUrlSmall;
    }

    public String getCoverUrlMiddle() {
        return coverUrlMiddle;
    }

    public void setCoverUrlMiddle(String coverUrlMiddle) {
        this.coverUrlMiddle = coverUrlMiddle;
    }

    public String getCoverUrlLarge() {
        return coverUrlLarge;
    }

    public void setCoverUrlLarge(String coverUrlLarge) {
        this.coverUrlLarge = coverUrlLarge;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public List<String> getTags() {
        return tags;
    }

    @JsonSetter("tags")
    public void setTags(String tags) {
        if (StringUtils.isEmpty(tags)) {
            this.tags = Collections.emptyList();
        }
        this.tags = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(tags);
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    public long getLastUptrackAt() {
        return lastUptrackAt;
    }

    public void setLastUptrackAt(long lastUptrackAt) {
        this.lastUptrackAt = lastUptrackAt;
    }

    @JsonSetter("last_uptrack_at")
    public void setLastUptrackAt(String lastUptrackAt) {
        this.lastUptrackAt = parseUpdate(lastUptrackAt);
    }
    @JsonSetter("updated_at")
    public void setUpdateAt(String lastUptrackAt) {
        this.lastUptrackAt = parseUpdate(lastUptrackAt);

    }
    private long parseUpdate(String lastUptrackAt) {
        if (StringUtils.isEmpty(lastUptrackAt)) {
            return 0;
        }
        String t = StringUtils.replace(lastUptrackAt, "T", " ");


        if (t.length() > 3 && t.endsWith(":00")) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
            t = t.substring(0, t.length() - 3) + "00";
            try {
                return sdf.parse(t).getTime();
            } catch (ParseException e) {
                //ignore
            }
        } else if (t.length()>3 && t.endsWith("Z")) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            t = t.substring(0, t.length() - 1);
            try {
                return sdf.parse(t).getTime();
            } catch (ParseException e) {
                //ignore
            }
        }
        return 0;

    }
    public int getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(int trackCount) {
        this.trackCount = trackCount;
    }

    public long getPlayCount() {
        return playCount;
    }

    public void setPlayCount(long playCount) {
        this.playCount = playCount;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }

    public List<HimalayaTrack> getTracks() {
        return tracks;
    }

    public void setTracks(List<HimalayaTrack> tracks) {
        this.tracks = tracks;
    }

}
