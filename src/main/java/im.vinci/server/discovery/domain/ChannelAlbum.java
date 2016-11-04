package im.vinci.server.discovery.domain;

import java.util.List;

/**
 * Created by ASUS on 2016/8/25.
 */
public class ChannelAlbum {

    private long channelId;

    private String channelName;

    private String channelImg;

    private List<MusicAlbum> albumList;

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelImg() {
        return channelImg;
    }

    public void setChannelImg(String channelImg) {
        this.channelImg = channelImg;
    }

    public List<MusicAlbum> getAlbumList() {
        return albumList;
    }

    public void setAlbumList(List<MusicAlbum> albumList) {
        this.albumList = albumList;
    }
}
