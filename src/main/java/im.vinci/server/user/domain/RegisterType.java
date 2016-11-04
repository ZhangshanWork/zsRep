package im.vinci.server.user.domain;

/**
 * 注册时的渠道
 * Created by tim@vinci on 16/7/21.
 */
public enum RegisterType {
    phone,
    weixin,
    weibo,
    qq;

    public static RegisterType of(String name) {
        try {
            return RegisterType.valueOf(name);
        }catch (Exception e) {
            return null;
        }
    }
}
