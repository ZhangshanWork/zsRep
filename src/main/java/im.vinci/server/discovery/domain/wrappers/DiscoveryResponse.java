package im.vinci.server.discovery.domain.wrappers;

import im.vinci.server.discovery.domain.CategoryAlbumSong;
import im.vinci.server.discovery.domain.ChannelAlbum;

import java.util.List;

/**
 * Created by ASUS on 2016/8/23.
 */
public class DiscoveryResponse {

    private List<ChannelAlbum> channelAlbumList;

    private List<CategoryAlbumSong> categoryAlbumSongList;

    public List<ChannelAlbum> getChannelAlbumList() {
        return channelAlbumList;
    }

    public void setChannelAlbumList(List<ChannelAlbum> channelAlbumList) {
        this.channelAlbumList = channelAlbumList;
    }

    public List<CategoryAlbumSong> getCategoryAlbumSongList() {
        return categoryAlbumSongList;
    }

    public void setCategoryAlbumSongList(List<CategoryAlbumSong> categoryAlbumSongList) {
        this.categoryAlbumSongList = categoryAlbumSongList;
    }
}