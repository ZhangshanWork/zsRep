package im.vinci.server.songbook.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by mlc on 2016/6/28.
 * 用户曲库表
 */
public class SongBook implements Serializable{
    private long id;
    private String sid;
    private String userId;

    private String title;
    private String artist;
    private Date createDate;
    private String type;
    private String ifdel;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIfdel() {
        return ifdel;
    }

    public void setIfdel(String ifdel) {
        this.ifdel = ifdel;
    }
}
