package im.vinci.server.device.domain;

/**
 * Created by tim@vinci on 16/1/15.
 */
public enum OTAHardwareCode {
    _01("第一版硬件");

    private String desc;
    OTAHardwareCode(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static OTAHardwareCode getCode(String value) {
        try {
            return OTAHardwareCode.valueOf(value);
        }catch (Exception e) {
            return null;
        }
    }
}
