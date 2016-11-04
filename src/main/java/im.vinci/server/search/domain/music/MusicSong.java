package im.vinci.server.search.domain.music;

import im.vinci.server.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tim@vinci on 15/11/26.
 * 返回的歌曲基本信息
 */
public class MusicSong implements Serializable{

    private long song_id;
    private String song_type;
    private String song_name;
    private String song_sub_title;
    private String artist;
    private String artist_sub_title;
    private long artist_id;
    private String artist_logo;
    private String album_name;
    private String album_sub_title;
    private long album_id;
    private int play_seconds;
    private String album_logo;
    private String listen_file;
    private String lyric_file;
    //自动eq调整类型
    private int sound_equalizer;
    private int sound_effect;
    //演唱者
    private String singers;
    /**
     * 是否可以播放
     */
    private boolean playAuthority;

    /**
     * 网络上的播放次数,需要和本地的区分开
     */
    private long play_counts;

    private List<String> tags;

    private List<Long> tagCounts;

    private MusicSong(){}

    public MusicSong(String type) {
        this.song_type = type;
    }

    public long getSong_id() {
        return song_id;
    }

    public void setSong_id(long song_id) {
        this.song_id = song_id;
    }

    public String getSong_type() {
        return song_type;
    }

    public void setSong_type(String song_type) {
        this.song_type = song_type;
    }

    public String getSong_name() {
        return song_name;
    }

    public void setSong_name(String song_name) {
        this.song_name = song_name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getArtist_id() {
        return artist_id;
    }

    public void setArtist_id(long artist_id) {
        this.artist_id = artist_id;
    }

    public String getAlbum_name() {
        return album_name;
    }

    public void setAlbum_name(String album_name) {
        this.album_name = album_name;
    }

    public long getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(long album_id) {
        this.album_id = album_id;
    }

    public int getPlay_seconds() {
        return play_seconds;
    }

    public void setPlay_seconds(int play_seconds) {
        this.play_seconds = play_seconds;
    }

    public String getAlbum_logo() {
        return album_logo;
    }

    public void setAlbum_logo(String album_logo) {
        this.album_logo = album_logo;
    }

    public String getListen_file() {
        return listen_file;
    }

    public void setListen_file(String listen_file) {
        this.listen_file = listen_file;
    }

    public String getLyric_file() {
        return lyric_file;
    }

    public void setLyric_file(String lyric_file) {
        this.lyric_file = lyric_file;
    }

    public int getSound_equalizer() {
        return sound_equalizer;
    }

    public void setSound_equalizer(int sound_equalizer) {
        this.sound_equalizer = sound_equalizer;
    }

    public int getSound_effect() {
        return sound_effect;
    }

    public void setSound_effect(int sound_effect) {
        this.sound_effect = sound_effect;
    }

    public String getSingers() {
        return singers;
    }

    public void setSingers(String singers) {
        this.singers = singers;
    }

    public long getPlay_counts() {
        return play_counts;
    }

    public void setPlay_counts(long play_counts) {
        this.play_counts = play_counts;
    }

    public String getArtist_logo() {
        return artist_logo;
    }

    public void setArtist_logo(String artist_logo) {
        this.artist_logo = artist_logo;
    }

    public boolean isPlayAuthority() {
        return playAuthority;
    }

    public void setPlayAuthority(boolean playAuthority) {
        this.playAuthority = playAuthority;
    }

    public String getSong_sub_title() {
        return song_sub_title;
    }

    public void setSong_sub_title(String song_sub_title) {
        this.song_sub_title = song_sub_title;
    }

    public String getArtist_sub_title() {
        return artist_sub_title;
    }

    public void setArtist_sub_title(String artist_sub_title) {
        this.artist_sub_title = artist_sub_title;
    }

    public String getAlbum_sub_title() {
        return album_sub_title;
    }

    public void setAlbum_sub_title(String album_sub_title) {
        this.album_sub_title = album_sub_title;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<Long> getTagCounts() {
        return tagCounts;
    }

    public void setTagCounts(List<Long> tagCounts) {
        this.tagCounts = tagCounts;
    }



    @Override
    public String toString() {
        return JsonUtils.encode(this);
    }
}
