package im.vinci.server.feed.domain.feeds;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.MoreObjects;
import im.vinci.server.utils.JsonUtils;

import java.io.Serializable;

/**
 * Created by tim@vinci on 16/9/6.
 */
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MusicFeedContent implements Serializable{
    private long songId;
    private String songType;
    private String songName;
    private String songSubTitle;
    private String artist;
    private String artistSubTitle;
    private long artistId;
    private String artistLogo;
    private String albumName;
    private String albumSubTitle;
    private long albumId;
    private int playSeconds;
    private String albumLogo;
    private String listenFile;
    private String lyricFile;
    //自动eq调整类型
    private int soundEqualizer;
    private int soundEffect;
    //演唱者
    private String singers;

    public long getSongId() {
        return songId;
    }

    public MusicFeedContent setSongId(long songId) {
        this.songId = songId;
        return this;
    }

    public String getSongType() {
        return songType;
    }

    public MusicFeedContent setSongType(String songType) {
        this.songType = songType;
        return this;
    }

    public String getSongName() {
        return songName;
    }

    public MusicFeedContent setSongName(String songName) {
        this.songName = songName;
        return this;
    }

    public String getSongSubTitle() {
        return songSubTitle;
    }

    public MusicFeedContent setSongSubTitle(String songSubTitle) {
        this.songSubTitle = songSubTitle;
        return this;
    }

    public String getArtist() {
        return artist;
    }

    public MusicFeedContent setArtist(String artist) {
        this.artist = artist;
        return this;
    }

    public String getArtistSubTitle() {
        return artistSubTitle;
    }

    public MusicFeedContent setArtistSubTitle(String artistSubTitle) {
        this.artistSubTitle = artistSubTitle;
        return this;
    }

    public long getArtistId() {
        return artistId;
    }

    public MusicFeedContent setArtistId(long artistId) {
        this.artistId = artistId;
        return this;
    }

    public String getArtistLogo() {
        return artistLogo;
    }

    public MusicFeedContent setArtistLogo(String artistLogo) {
        this.artistLogo = artistLogo;
        return this;
    }

    public String getAlbumName() {
        return albumName;
    }

    public MusicFeedContent setAlbumName(String albumName) {
        this.albumName = albumName;
        return this;
    }

    public String getAlbumSubTitle() {
        return albumSubTitle;
    }

    public MusicFeedContent setAlbumSubTitle(String albumSubTitle) {
        this.albumSubTitle = albumSubTitle;
        return this;
    }

    public long getAlbumId() {
        return albumId;
    }

    public MusicFeedContent setAlbumId(long albumId) {
        this.albumId = albumId;
        return this;
    }

    public int getPlaySeconds() {
        return playSeconds;
    }

    public MusicFeedContent setPlaySeconds(int playSeconds) {
        this.playSeconds = playSeconds;
        return this;
    }

    public String getAlbumLogo() {
        return albumLogo;
    }

    public MusicFeedContent setAlbumLogo(String albumLogo) {
        this.albumLogo = albumLogo;
        return this;
    }

    public String getListenFile() {
        return listenFile;
    }

    public MusicFeedContent setListenFile(String listenFile) {
        this.listenFile = listenFile;
        return this;
    }

    public String getLyricFile() {
        return lyricFile;
    }

    public MusicFeedContent setLyricFile(String lyricFile) {
        this.lyricFile = lyricFile;
        return this;
    }

    public int getSoundEqualizer() {
        return soundEqualizer;
    }

    public MusicFeedContent setSoundEqualizer(int soundEqualizer) {
        this.soundEqualizer = soundEqualizer;
        return this;
    }

    public int getSoundEffect() {
        return soundEffect;
    }

    public MusicFeedContent setSoundEffect(int soundEffect) {
        this.soundEffect = soundEffect;
        return this;
    }

    public String getSingers() {
        return singers;
    }

    public MusicFeedContent setSingers(String singers) {
        this.singers = singers;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("songId", songId)
                .add("songType", songType)
                .add("songName", songName)
                .add("songSubTitle", songSubTitle)
                .add("artist", artist)
                .add("artistSubTitle", artistSubTitle)
                .add("artistId", artistId)
                .add("artistLogo", artistLogo)
                .add("albumName", albumName)
                .add("albumSubTitle", albumSubTitle)
                .add("albumId", albumId)
                .add("playSeconds", playSeconds)
                .add("albumLogo", albumLogo)
                .add("listenFile", listenFile)
                .add("lyricFile", lyricFile)
                .add("soundEqualizer", soundEqualizer)
                .add("soundEffect", soundEffect)
                .add("singers", singers)
                .toString();
    }

    public static void main(String[] args) {
        String s = "{\"album_id\":\"0\",\"album_logo\":\"\",\"album_name\":\"Encore\",\"artist\":\"周杰伦\",\"artist_id\":\"3322\",\"introduction\":\"这是一首歌哈哈哈哈哈哈\",\"listen_file\":\"http://oss-cn-beijing.aliyuncs.com/english-listen-resource/%E5%91%A8%E6%9D%B0%E4%BC%A6/%E5%91%A8%E6%9D%B0%E4%BC%A6-%E5%8F%91%E5%A6%82%E9%9B%AA.mp3\",\"lyric_file\":\"\",\"play_counts\":7697823212,\"play_seconds\":199,\"singers\":\"周杰伦\",\"song_id\":1010160018,\"song_name\":\"发如雪\",\"song_type\":\"xiami\",\"sound_equalizer\":0}";
        System.out.println(JsonUtils.decode(s,MusicFeedContent.class));
    }
}
