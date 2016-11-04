package im.vinci.server.discovery.domain;

/**
 * Created by ASUS on 2016/8/24.
 */
public class Channel {

    private long channelId;

    private String channelName;

    private String channelImg;

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
}
