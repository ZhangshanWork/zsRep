package im.vinci.server.device.domain;

import java.util.Date;

/**
 * Created by henryhome on 11/12/15.
 */
public class Device {

    private Integer id;
    private String name;
    private String mac;
    private String imei;
    private Date firstUpdateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public Date getFirstUpdateTime() {
        return firstUpdateTime;
    }

    public void setFirstUpdateTime(Date firstUpdateTime) {
        this.firstUpdateTime = firstUpdateTime;
    }
}
