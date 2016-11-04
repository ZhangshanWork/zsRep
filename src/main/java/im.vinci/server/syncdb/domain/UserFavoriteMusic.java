package im.vinci.server.syncdb.domain;

/**
 * Created by mingjie on 16/10/18.
 */
public class UserFavoriteMusic {
    private long id;
    private long userId;
    private String albumId;
    private String albumLogo;
    private String albumName;
    private String albumSubTitle;
    private String artist;
    private String artistId;
    private String artistSubTitle;
    private byte favorite;
    private boolean isDelete;
    private long lastFavoriteTime;
    private String musicId;
    private int playSeconds;
    private String songName;
    private String songSubTitle;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getAlbumLogo() {
        return albumLogo;
    }

    public void setAlbumLogo(String albumLogo) {
        this.albumLogo = albumLogo;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumSubTitle() {
        return albumSubTitle;
    }

    public void setAlbumSubTitle(String albumSubTitle) {
        this.albumSubTitle = albumSubTitle;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getArtistSubTitle() {
        return artistSubTitle;
    }

    public void setArtistSubTitle(String artistSubTitle) {
        this.artistSubTitle = artistSubTitle;
    }

    public byte getFavorite() {
        return favorite;
    }

    public void setFavorite(byte favorite) {
        this.favorite = favorite;
    }

    public boolean getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(boolean delete) {
        isDelete = delete;
    }

    public long getLastFavoriteTime() {
        return lastFavoriteTime;
    }

    public void setLastFavoriteTime(long lastFavoriteTime) {
        this.lastFavoriteTime = lastFavoriteTime;
    }

    public String getMusicId() {
        return musicId;
    }

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }

    public int getPlaySeconds() {
        return playSeconds;
    }

    public void setPlaySeconds(int playSeconds) {
        this.playSeconds = playSeconds;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongSubTitle() {
        return songSubTitle;
    }

    public void setSongSubTitle(String songSubTitle) {
        this.songSubTitle = songSubTitle;
    }
}
