package im.vinci.server.search.domain.himalayas;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * 喜马拉雅音乐file
 * Created by tim@vinci on 15/12/23.
 */
public class HimalayaTrack implements Serializable{
    //track id
    private long id;
    //track name
    private String title;

    @JsonProperty("cover_url_small")
    private String coverUrlSmall;

    @JsonProperty("cover_url_middle")
    private String coverUrlMiddle;

    @JsonProperty("cover_url_large")
    private String coverUrlLarge;

    //创建时间
    private long createAt;

    private int duration;

    private String playUrl;

    @JsonProperty("plays_count")
    private long playCount;

    @JsonProperty("category_id")
    private Integer categoryId;

    @JsonProperty("category_title")
    private String categoryTitle;

    @JsonProperty("album_id")
    private Long albumId;

    @JsonProperty("album_title")
    private String albumTitle;

    @JsonSetter("created_at")
    public void setCreateAt(String createAt) {
        this.createAt = parseTime(createAt);
    }
    @JsonGetter("create_at")
    public long getCreateAt() {
        return this.createAt;
    }

    private long parseTime(String lastUptrackAt) {
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @JsonGetter("play_url")
    public String getPlayUrl() {
        return playUrl;
    }

    @JsonSetter("play_url_64")
    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    public long getPlayCount() {
        return playCount;
    }

    public void setPlayCount(long playCount) {
        this.playCount = playCount;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public String getAlbumTitle() {
        return albumTitle;
    }

    public void setAlbumTitle(String albumTitle) {
        this.albumTitle = albumTitle;
    }
}
