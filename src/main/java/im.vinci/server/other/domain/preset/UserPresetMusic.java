package im.vinci.server.other.domain.preset;

/**
 * Created by zhongzhengkai on 15/12/24.
 */
public class UserPresetMusic {
    private int id;
    private int accountId;
    private String deviceId;
    private int songId;
    private int musicId;
    private String musicSource;
    private String createTime;

    public UserPresetMusic(int accountId,String deviceId,int songId, int musicId,String musicSource){
        this.accountId = accountId;
        this.deviceId = deviceId;
        this.songId = songId;
        this.musicId = musicId;
        this.musicSource = musicSource;
    }

    public UserPresetMusic(){}

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMusicId() {
        return musicId;
    }

    public void setMusicId(int musicId) {
        this.musicId = musicId;
    }

    public String getMusicSource() {
        return musicSource;
    }

    public void setMusicSource(String musicSource) {
        this.musicSource = musicSource;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public String toString() {
        return "[" + id + "," + accountId + "," + deviceId + "," + songId + "," + musicId + "," + musicSource + "," + createTime + "]";
    }
}


