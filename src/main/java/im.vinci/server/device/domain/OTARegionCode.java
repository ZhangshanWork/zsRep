package im.vinci.server.device.domain;

/**
 * ota升级的地域code
 * Created by tim@vinci on 16/1/15.
 */
public enum OTARegionCode {
    CN("中国"),
    US("美国");

    private String desc;
    OTARegionCode(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static OTARegionCode getCode(String value) {
        try {
            return OTARegionCode.valueOf(value);
        }catch (Exception e) {
            return null;
        }
    }
}
