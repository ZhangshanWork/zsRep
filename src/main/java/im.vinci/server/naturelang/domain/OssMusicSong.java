package im.vinci.server.naturelang.domain;

import im.vinci.server.search.domain.music.MusicSong;

import java.util.List;

/**
 * Created by mlc on 2016/5/9.
 */
public class OssMusicSong {
    private String catalog;
    private List<MusicSong> musics;

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public List<MusicSong> getMusics() {
        return musics;
    }

    public void setMusics(List<MusicSong> musics) {
        this.musics = musics;
    }
}
