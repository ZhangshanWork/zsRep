package im.vinci.server.device.domain;

import java.util.Date;

/**
 * Created by henryhome on 9/15/15.
 */
public class CrashLog {

    private long id;
    private String imei;
    private String mac;
    private String crashType;
    private Integer crashCount;
    private String appName;
    private String appVersion;
    private String logSign;
    private String log;
    private Date createTime;
    private Date lastModifyTime;


    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getCrashType() {
        return crashType;
    }

    public void setCrashType(String crashType) {
        this.crashType = crashType;
    }

    public Integer getCrashCount() {
        return crashCount;
    }

    public void setCrashCount(Integer crashCount) {
        this.crashCount = crashCount;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getLogSign() {
        return logSign;
    }

    public void setLogSign(String logSign) {
        this.logSign = logSign;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public long getId() {
        return id;
    }

    public CrashLog setId(long id) {
        this.id = id;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public CrashLog setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public CrashLog setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
        return this;
    }
}



