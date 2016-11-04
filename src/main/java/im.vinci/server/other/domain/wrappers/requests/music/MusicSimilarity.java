package im.vinci.server.other.domain.wrappers.requests.music;

/**
 * Created by henryhome on 11/23/15.
 */
public class MusicSimilarity {

    private String userId;
    private String musicId;
    private Integer size;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMusicId() {
        return musicId;
    }

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
