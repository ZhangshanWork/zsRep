package im.vinci.server.common.domain.enums;

/**
 * Created by tim@vinci on 16/7/21.
 */
public enum BindDeviceType {
    headphone,
    mobile;
    public static BindDeviceType of(String name) {
        try {
            return BindDeviceType.valueOf(name);
        }catch (Exception e) {
            return null;
        }
    }
}
