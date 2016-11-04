package im.vinci.server.device.domain.wrappers.requests;

/**
 * Created by henryhome on 9/14/15.
 */
public class OTAInfo {

    private String imei;
    private String mac;
    private String deviceVersion;
    private String sysVersionName;
    private Integer appVersionCode;
    private String sn;

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

    public String getDeviceVersion() {
        return deviceVersion;
    }

    public void setDeviceVersion(String deviceVersion) {
        this.deviceVersion = deviceVersion;
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

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("OTAInfo{");
        sb.append("imei='").append(imei).append('\'');
        sb.append(", mac='").append(mac).append('\'');
        sb.append(", deviceVersion='").append(deviceVersion).append('\'');
        sb.append(", sysVersionName='").append(sysVersionName).append('\'');
        sb.append(", appVersionCode=").append(appVersionCode);
        sb.append('}');
        return sb.toString();
    }
}
