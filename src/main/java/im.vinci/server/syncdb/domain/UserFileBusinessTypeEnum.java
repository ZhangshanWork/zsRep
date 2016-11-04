package im.vinci.server.syncdb.domain;

/**
 * Created by tim@vinci on 16/10/20.
 */
public enum UserFileBusinessTypeEnum {
    voice_record;

    public static UserFileBusinessTypeEnum forName(String name) {
        try {
            return UserFileBusinessTypeEnum.valueOf(name);
        }catch (Exception e) {
            return null;
        }
    }
}
