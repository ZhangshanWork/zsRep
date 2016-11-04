package im.vinci.server.user.domain;

/**
 * 用户消息类型
 * Created by tim@vinci on 16/9/13.
 */
public enum UserMessageType {
    AtMe("@我"),
    CommentInFeed("在feed中评论");

    private String desc;

    UserMessageType(String desc) {
        this.desc = desc;
    }

    public static UserMessageType value(String type) {
        try {
            return UserMessageType.valueOf(type);
        } catch (Exception e) {
            return null;
        }
    }
}
