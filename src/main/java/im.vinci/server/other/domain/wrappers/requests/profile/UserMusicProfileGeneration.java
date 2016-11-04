package im.vinci.server.other.domain.wrappers.requests.profile;

/**
 * Created by henryhome on 10/17/15.
 */
public class UserMusicProfileGeneration {

    private String deviceId;
    private String gender;
    private String age;
    private String musicRegion;
    private String musicCategory;
    private String musicStyle;
    private String favoriteSinger;
    private String musicTimeliness;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
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
