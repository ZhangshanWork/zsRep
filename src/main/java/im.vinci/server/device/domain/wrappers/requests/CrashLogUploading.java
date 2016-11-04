package im.vinci.server.device.domain.wrappers.requests;

/**
 * Created by henryhome on 9/15/15.
 */
public class CrashLogUploading {

    private String imei;
    private String mac;
    private String appName;
    private String appVersion;
    private String crashType;
    private Integer crashCount;
    private String log;

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

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
}
