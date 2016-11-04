package im.vinci.server.device.domain;

/**
 * Created by henryhome on 9/14/15.
 */
public class ApkOTAConfig {

    private String desc;
    private Integer appVersionCode;
    private String appVersionName;
    private String addr;
    private String hash;
    private String length;
    private Integer lastAppVersionCodeToUpdate;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getAppVersionCode() {
        return appVersionCode;
    }

    public void setAppVersionCode(Integer appVersionCode) {
        this.appVersionCode = appVersionCode;
    }

    public String getAppVersionName() {
        return appVersionName;
    }

    public void setAppVersionName(String appVersionName) {
        this.appVersionName = appVersionName;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public Integer getLastAppVersionCodeToUpdate() {
        return lastAppVersionCodeToUpdate;
    }

    public void setLastAppVersionCodeToUpdate(Integer lastAppVersionCodeToUpdate) {
        this.lastAppVersionCodeToUpdate = lastAppVersionCodeToUpdate;
    }
}
