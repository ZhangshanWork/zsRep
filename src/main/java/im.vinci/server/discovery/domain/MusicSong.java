package im.vinci.server.discovery.domain;

import im.vinci.server.utils.JsonUtils;

import java.io.Serializable;

/**
 * Created by
 * 返回的歌曲基本信息
 */
public class MusicSong implements Serializable{

    private long songId;
    private String songType;
    private String songName;
    private String artist;
    private long artistId;
    private String artistLogo;
    private String albumName;
    private long albumId;
    private String albumLogo;
    private String listenFile;
    private String lyricFile;
    //演唱者
    private String singers;

    /**
     * 网络上的播放次数,需要和本地的区分开
     */
    private long playCounts;

    private String introduction;//歌曲的首页介绍

    private MusicSong(){}

    public MusicSong(String type) {
        this.songType = type;
    }

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public String getSongType() {
        return songType;
    }

    public void setSongType(String songType) {
        this.songType = songType;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public String getArtistLogo() {
        return artistLogo;
    }

    public void setArtistLogo(String artistLogo) {
        this.artistLogo = artistLogo;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getAlbumLogo() {
        return albumLogo;
    }

    public void setAlbumLogo(String albumLogo) {
        this.albumLogo = albumLogo;
    }

    public String getListenFile() {
        return listenFile;
    }

    public void setListenFile(String listenFile) {
        this.listenFile = listenFile;
    }

    public String getLyricFile() {
        return lyricFile;
    }

    public void setLyricFile(String lyricFile) {
        this.lyricFile = lyricFile;
    }

    public String getSingers() {
        return singers;
    }

    public void setSingers(String singers) {
        this.singers = singers;
    }

    public long getPlayCounts() {
        return playCounts;
    }

    public void setPlayCounts(long playCounts) {
        this.playCounts = playCounts;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    @Override
    public String toString() {
        return JsonUtils.encode(this);
    }
}
