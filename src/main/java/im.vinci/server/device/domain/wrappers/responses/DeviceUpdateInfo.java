package im.vinci.server.device.domain.wrappers.responses;

/**
 * Created by henryhome on 9/14/15.
 */
public class DeviceUpdateInfo {

    private String type;
    private String desc;
    private String descEn;
    private Boolean isForced;
    private Boolean isFull;
    private String sysVersionName;
    private Integer appVersionCode;
    private String appVersionName;
    private String addr;
    private String hash;
    private String length;
    private Integer lastAppVersionCodeToUpdate;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Boolean getIsForced() {
        return isForced;
    }

    public void setIsForced(Boolean isForced) {
        this.isForced = isForced;
    }

    public Boolean getIsFull() {
        return isFull;
    }

    public void setIsFull(Boolean isFull) {
        this.isFull = isFull;
    }

    public String getSysVersionName() {
        return sysVersionName;
    }

    public void setSysVersionName(String sysVersionName) {
        this.sysVersionName = sysVersionName;
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

    public String getDescEn() {
        return descEn;
    }

    public void setDescEn(String descEn) {
        this.descEn = descEn;
    }


}
