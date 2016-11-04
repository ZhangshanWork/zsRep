package im.vinci.server.discovery.domain;

import im.vinci.server.utils.JsonUtils;

import java.io.Serializable;

/**
 * Created by
 * 返回的专辑基本信息
 */
public class MusicAlbum implements Serializable{

    private long albumId;

    private String albumName;

    private String albumLogo;

    private long artistId;

    private String artistName;

    private String artistLogo;

    /**
     * 专辑描述
     */
    private String description;

    /**
     * 专辑中的歌曲数量
     */
    private int songCount;

    /**
     * 公司
     */
    private String company;

    /**
     * 播放次数
     */
    private long playCounts;

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumLogo() {
        return albumLogo;
    }

    public void setAlbumLogo(String albumLogo) {
        this.albumLogo = albumLogo;
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtistLogo() {
        return artistLogo;
    }

    public void setArtistLogo(String artistLogo) {
        this.artistLogo = artistLogo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSongCount() {
        return songCount;
    }

    public void setSongCount(int songCount) {
        this.songCount = songCount;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public long getPlayCounts() {
        return playCounts;
    }

    public void setPlayCounts(long playCounts) {
        this.playCounts = playCounts;
    }

    @Override
    public String toString() {
        return JsonUtils.encode(this);
    }
}
