package im.vinci.server.discovery.domain;

import java.util.List;

/**
 * Created by ASUS on 2016/8/25.
 */
public class AlbumSongList<T> {

    private long albumSongListId;

    private String albumSongListName;

    private String type;

    private List<T> albumSongList;

    public long getAlbumSongListId() {
        return albumSongListId;
    }

    public void setAlbumSongListId(long albumSongListId) {
        this.albumSongListId = albumSongListId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAlbumSongListName() {
        return albumSongListName;
    }

    public void setAlbumSongListName(String albumSongListName) {
        this.albumSongListName = albumSongListName;
    }

    public List<T> getAlbumSongList() {
        return albumSongList;
    }

    public void setAlbumSongList(List<T> albumSongList) {
        this.albumSongList = albumSongList;
    }
}
