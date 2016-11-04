package im.vinci.server.other.domain.profile;

/**
 * Created by henryhome on 9/11/15.
 */
public class UserMusicProfile {
    private Integer userId;
    private String musicRegion;
    private String musicCategory;
    private String musicStyle;
    private String favoriteSinger;
    private String musicTimeliness;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getMusicRegion() {
        return musicRegion;
    }

    public void setMusicRegion(String musicRegion) {
        this.musicRegion = musicRegion;
    }

    public String getMusicCategory() {
        return musicCategory;
    }

    public void setMusicCategory(String musicCategory) {
        this.musicCategory = musicCategory;
    }

    public String getMusicStyle() {
        return musicStyle;
    }

    public void setMusicStyle(String musicStyle) {
        this.musicStyle = musicStyle;
    }

    public String getFavoriteSinger() {
        return favoriteSinger;
    }

    public void setFavoriteSinger(String favoriteSinger) {
        this.favoriteSinger = favoriteSinger;
    }

    public String getMusicTimeliness() {
        return musicTimeliness;
    }

    public void setMusicTimeliness(String musicTimeliness) {
        this.musicTimeliness = musicTimeliness;
    }
}
