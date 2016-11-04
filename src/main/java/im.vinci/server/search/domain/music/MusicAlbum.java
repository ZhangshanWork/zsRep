package im.vinci.server.search.domain.music;

import im.vinci.server.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tim@vinci on 15/11/26.
 * 返回的专辑基本信息
 */
public class MusicAlbum implements Serializable{
    //3为不可播放
    private int is_check = 1;

    private long album_id;

    private String album_name;

    private String album_logo;

    private long artist_id;

    private String artist_name;

    private String artist_logo;
    /**
     * 专辑别名
     */
    private String sub_title;
    /**
     * 专辑描述
     */
    private String desc;

    /**
     * 专辑中的歌曲数量
     */
    private int song_count;

    /**
     * 公司
     */
    private String company;

    /**
     * 专辑发布时间(second数)
     */
    private long publish_time = -1;

    private boolean playAuthority = true;

    /**
     * 播放次数
     */
    private long play_counts;
    /**
     * 歌曲详细信息
     */
    private List<MusicSong> songs;

    public int getIs_check() {
        return is_check;
    }

    public void setIs_check(int is_check) {
        this.is_check = is_check;
    }

    public long getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(long album_id) {
        this.album_id = album_id;
    }

    public String getAlbum_name() {
        return album_name;
    }

    public void setAlbum_name(String album_name) {
        this.album_name = album_name;
    }

    public String getAlbum_logo() {
        return album_logo;
    }

    public void setAlbum_logo(String album_logo) {
        this.album_logo = album_logo;
    }

    public long getArtist_id() {
        return artist_id;
    }

    public void setArtist_id(long artist_id) {
        this.artist_id = artist_id;
    }

    public String getArtist_name() {
        return artist_name;
    }

    public void setArtist_name(String artist_name) {
        this.artist_name = artist_name;
    }

    public String getArtist_logo() {
        return artist_logo;
    }

    public void setArtist_logo(String artist_logo) {
        this.artist_logo = artist_logo;
    }

    public String getSub_title() {
        return sub_title;
    }

    public void setSub_title(String sub_title) {
        this.sub_title = sub_title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getSong_count() {
        return song_count;
    }

    public void setSong_count(int song_count) {
        this.song_count = song_count;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public long getPublish_time() {
        return publish_time;
    }

    public void setPublish_time(long publish_time) {
        this.publish_time = publish_time;
    }

    public List<MusicSong> getSongs() {
        return songs;
    }

    public void setSongs(List<MusicSong> songs) {
        this.songs = songs;
    }

    public boolean isPlayAuthority() {
        return playAuthority;
    }

    public void setPlayAuthority(boolean playAuthority) {
        this.playAuthority = playAuthority;
    }

    public long getPlay_counts() {
        return play_counts;
    }

    public void setPlay_counts(long play_counts) {
        this.play_counts = play_counts;
    }

    @Override
    public String toString() {
        return JsonUtils.encode(this);
    }
}
