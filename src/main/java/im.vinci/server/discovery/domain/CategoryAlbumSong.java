package im.vinci.server.discovery.domain;

import java.util.List;

/**
 * Created by ASUS on 2016/8/25.
 */
public class CategoryAlbumSong {

    private String categoryName;

    private List<AlbumSongList> albumSongListList;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<AlbumSongList> getAlbumSongListList() {
        return albumSongListList;
    }

    public void setAlbumSongListList(List<AlbumSongList> albumSongListList) {
        this.albumSongListList = albumSongListList;
    }
}
