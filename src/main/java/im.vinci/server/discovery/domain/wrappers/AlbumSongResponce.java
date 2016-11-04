package im.vinci.server.discovery.domain.wrappers;

/**
 * Created by ASUS on 2016/8/25.
 */
public class AlbumSongResponce {

    private long categoryId;

    private String type;

    private long albumSongListId;

    private String albumSongListName;

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getAlbumSongListId() {
        return albumSongListId;
    }

    public void setAlbumSongListId(long albumSongListId) {
        this.albumSongListId = albumSongListId;
    }

    public String getAlbumSongListName() {
        return albumSongListName;
    }

    public void setAlbumSongListName(String albumSongListName) {
        this.albumSongListName = albumSongListName;
    }
}
