package im.vinci.server.naturelang.domain;

import java.util.List;

/**
 * Created by mlc on 2016/3/9.
 */
public class Mood {
    private String keywords;
    private String songs;
    private List<String> moods;
    private List<String> songList;

    public List<String> getMoods() {
        return moods;
    }

    public void setMoods(List<String> moods) {
        this.moods = moods;
    }

    public List<String> getSongList() {
        return songList;
    }

    public void setSongList(List<String> songList) {
        this.songList = songList;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getSongs() {
        return songs;
    }

    public void setSongs(String songs) {
        this.songs = songs;
    }
}
