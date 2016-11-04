package im.vinci.server.songbook.model;

import java.util.Date;

/**
 * Created by mlc on 2016/6/28.
 * 消息表
 */
public class Messages {
    private Long id;
    private String mid;
    private String songBookId;
    private String userId;
    private String imei;
    private String ifsend;
    private String ifsuc;
    private Date sendDate;
    private Date responseDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getSongBookId() {
        return songBookId;
    }

    public void setSongBookId(String songBookId) {
        this.songBookId = songBookId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getIfsend() {
        return ifsend;
    }

    public void setIfsend(String ifsend) {
        this.ifsend = ifsend;
    }

    public String getIfsuc() {
        return ifsuc;
    }

    public void setIfsuc(String ifsuc) {
        this.ifsuc = ifsuc;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public Date getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(Date responseDate) {
        this.responseDate = responseDate;
    }
}
