package im.vinci.server.other.domain.preset;

/**
 * Created by zhongzhengkai on 15/12/24.
 */
public class PresetMusic {


    private int id;
    private int songId;
    private String songName;
    private int musicId;
    private String musicSource;
    private int playlistnameId;
    private String playlistname;
    private int version;
    private String createTime;

    public PresetMusic(){}

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

    public int getPlaylistnameId() {
        return playlistnameId;
    }

    public void setPlaylistnameId(int playlistnameId) {
        this.playlistnameId = playlistnameId;
    }

    public String getPlaylistname() {
        return playlistname;
    }

    public void setPlaylistname(String playlistname) {
        this.playlistname = playlistname;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String toString() {
        return "[" + id + "," + songId + "," + songName + "," + musicId + "," + playlistname + "," + playlistnameId + "," + musicSource + "," + version +"," + createTime+"]";
    }

}
