package im.vinci.server.user.domain;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * Created by tim@vinci on 16/10/14.
 */
public class UserFavoriteSong implements Serializable{
    private long id;
    private long userId;
    private long songId;
    private String songType;
    private String songName;
    private long artistId;
    private String artistName;
    private long albumId;
    private String albumName;
    private String logo;
    private String singers;
    private int playSeconds;

    public long getId() {
        return id;
    }

    public UserFavoriteSong setId(long id) {
        this.id = id;
        return this;
    }

    public long getUserId() {
        return userId;
    }

    public UserFavoriteSong setUserId(long userId) {
        this.userId = userId;
        return this;
    }

    public long getSongId() {
        return songId;
    }

    public UserFavoriteSong setSongId(long songId) {
        this.songId = songId;
        return this;
    }

    public String getSongType() {
        return songType;
    }

    public UserFavoriteSong setSongType(String songType) {
        this.songType = songType;
        return this;
    }

    public String getSongName() {
        return songName;
    }

    public UserFavoriteSong setSongName(String songName) {
        this.songName = songName;
        return this;
    }

    public long getArtistId() {
        return artistId;
    }

    public UserFavoriteSong setArtistId(long artistId) {
        this.artistId = artistId;
        return this;
    }

    public String getArtistName() {
        return artistName;
    }

    public UserFavoriteSong setArtistName(String artistName) {
        this.artistName = artistName;
        return this;
    }

    public long getAlbumId() {
        return albumId;
    }

    public UserFavoriteSong setAlbumId(long albumId) {
        this.albumId = albumId;
        return this;
    }

    public String getAlbumName() {
        return albumName;
    }

    public UserFavoriteSong setAlbumName(String albumName) {
        this.albumName = albumName;
        return this;
    }

    public String getLogo() {
        return logo;
    }

    public UserFavoriteSong setLogo(String logo) {
        this.logo = logo;
        return this;
    }

    public String getSingers() {
        return singers;
    }

    public UserFavoriteSong setSingers(String singers) {
        this.singers = singers;
        return this;
    }

    public int getPlaySeconds() {
        return playSeconds;
    }

    public UserFavoriteSong setPlaySeconds(int playSeconds) {
        this.playSeconds = playSeconds;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("userId", userId)
                .add("songId", songId)
                .add("songType", songType)
                .add("songName", songName)
                .add("artistId", artistId)
                .add("artistName", artistName)
                .add("albumId", albumId)
                .add("albumName", albumName)
                .add("logo", logo)
                .add("singers", singers)
                .add("playSeconds", playSeconds)
                .toString();
    }
}
