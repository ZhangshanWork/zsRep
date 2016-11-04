package im.vinci.server.common.exceptions.error;

/**
 * Created by henryhome on 2/21/15.
 */
public class ErrorCode {
    // Request parameter error
    public static final Integer BAD_REQUEST = 400;

    // Authentication error code
    public static final Integer UNAUTHORIZED = 401;
    public static final Integer NEED_LOGIN = 401001;

    // Internal server error
    public static final Integer INTERNAL_SERVER_ERROR = 500;

    // Account module error code (100)
    public static final Integer ACCOUNT_EXISTS      = 100001;
    public static final Integer INVALID_PHONE_NUM   = 100002;
    public static final Integer ACCOUNT_NOT_FOUND   = 100003;
    public static final Integer ARGUMENT_ERROR      = 100004;

    /** 用户登录相关 **/
    //验证码输入错误
    public static final int API_LOGIN_VALIDCODE_SEND_FREQUENCY = 100101;
    public static final int API_LOGIN_VALIDCODE_SEND_FAILED = 100102;
    public static final int API_LOGIN_VALIDCODE_NOT_CORRECT = 100103; //验证码不正确
    public static final int API_LOGIN_VALIDCODE_EXPIRED = 100104; //验证码已过期
    //user info
    public static final int API_USER_HAS_EXISTS = 100110;
    public static final int API_USER_NOT_EXISTS = 100111;
    public static final int INSERT_USER_INFO_ERROR = 100112; //新插入
    public static final int UPDATE_USER_INFO_ERROR = 100113; //新插入
    public static final int API_USER_PHONENUM_NOT_CORRECT = 100112; //输入手机号不正确
    public static final int API_USER_PASSWORD_NOT_RULED = 100113; //输入密码不符合规则
    public static final int API_USER_PASSWORD_NOT_CORRECT = 100114; //输入密码不正确
    public static final int API_USER_THIRD_SIDE_REGISTER_NOT_SUPPORT = 100115; //不支持的第三方登录
    public static final int API_USER_THIRD_SIDE_REGISTER_EXUID_ERROR = 100116; //不支持的第三方登录
    public static final int USER_NICKNAME_DUPLICATE_ERROR = 100117; //用户昵称重复
    public static final int USER_NICKNAME_NOTSTANDARD_ERROR = 100118;//用户昵称有特殊字符不符合规则
    public static final int USER_NICKNAME_SENSITIVE_ERROR = 100119;//用户昵称有敏感词汇

    public static final int USER_FILE_SPACE_AUTH_FAILED = 100120; // 用户文件上传空间授权失败
    //bind device
    public static final int API_BIND_DEVICE_HAS_BINDED = 100201;//要绑定的设备已被其他绑定
    public static final int API_BIND_DEVICE_USER_HAS_DEVICE = 100202;//用户已经绑定了其他设备
    public static final int API_BIND_DEVICE_NOT_BIND = 100203;//设备没有被绑定
    public static final int API_BIND_DEVICE_UNBIND_DEVICE_UNMATCH = 100204;//要解绑的设备不一致
    //file read
    public static final int FILE_READ_ERROR = 100301;//文件读取出错
    public static final int HEAD_IMG_UPLOAD_ERROR = 100302;//头像上传出错
    public static final int HEAD_IMG_READ_ERROR = 100303;//头像读取出错
    public static final int HEAD_IMG_UPLOAD_REACH_MAX_BYTE = 100304;//头像大小超大
    //用户关注
    public static final int USER_ATTENTION_USER_NOT_FOUND = 100401;//用户关注_用户未找到
    public static final int USER_ATTENTION_ALREADY_INSERT = 100402;//用户关注_关注不能重复添加
    public static final int USER_ATTENTION_NOT_EXIST = 100403;//用户关注_关注尚未添加,无法删除
    public static final int USER_ATTENTION_INSERT_ERROR = 100404;//关注添加错误
    public static final int USER_ATTENTION_INSERT_YOURSELF = 100405;//不能自己关注自己
    public static final int USER_ATTENTION_REACH_MAX_COUNT = 100406;//关注到达最大数
    //V圈社区
    public static final int FEED_PUBLISH_NO_USER_ID_ERROR = 100501;//发表feed未传入user_id
    public static final int FEED_PUBLISH_ERROR = 100502;//插入feed错误
    public static final int FEED_DELETE_NO_FEED_ID_ERROR = 100503;//发表feed未传入id
    public static final int FEED_DELETE_ERROR = 100504;//删除feed错误
    public static final int FEED_PUBLISH_USER_ID_ERROR = 100505;//传入user_id与用户登录id不匹配
    public static final int FEED_DELETE_FEED_ID_ERROR = 100506;//删除非本人FEED
    public static final int FEED_DELETE_NOT_EXIST_ERROR = 100507;//删除FEED不存在
    public static final int FEED_PUBLISH_PAGE_TYPE_UNSUPPORTED = 100508;//不支持的feed类型
    public static final int FEED_PUBLISH_PAGE_TYPE_ARGUMENT_ERROR = 100509;//page content参数错误
    public static final int FEED_PUBLISH_PAGE_TYPE_NEED_EMPTY = 100510;//不允许有page type
    public static final int FEED_PUBLISH_CONTENT_UNMATCH_RULE = 100511;//发表的内容不符合规则,字数或者敏感词
    //V圈Feed Comment
    public static final int FEED_COMMENT_FEED_NOT_EXIST_ERROR = 100550; //feed不存在
    public static final int FEED_COMMENT_DELETE_NOT_EXIST_ERROR = 100551; //comment不存在
    public static final int FEED_COMMENT_DELETE_NOT_OWN_ERROR = 100552;//删除非本人comment
    public static final int FEED_COMMENTS_LIST_PARAMETER_ERROR = 100553;//page或pageSize不符合要求
    public static final int USER_INFO_NOT_EXIST_ERROR = 100554;//获取评论列表时用户信息不存在
    public static final int GET_TOTAL_COUNT_ERROR = 100555;//获取评论总数失败
    public static final int FEED_COMMENTS_CONTENT_UNMATCH_RULE = 100556;//发表的内容不符合规则,字数或者敏感词
    public static final int FEED_COMMENTS_PUBLISH_REPLY_USER_NOT_EXIST = 100557;//回复的用户不存在
    public static final int FEED_COMMENT_HAS_DELETED_ERROR = 100558; //comment已被删除


    //V圈消息箱
    public static final int ARGUMENT_ERRORUPDATE_MESSAGE_ISREAD_ERROR = 100701;//消息箱未读更新失败
    public static final int USER_MESSAGE_DATABASE_ERROR = 100702;//消息箱数据库操作失败


    //发现
    public static final int GET_ALBUM_LIST_ERROR = 100801;//获取专辑列表错误
    public static final int GET_SONG_LIST_ERROR = 100802;//获取歌曲列表错误
    public static final int GET_RECENT_LIST_ERROR = 100803;//获取最新上传歌曲列表错误
    public static final int GET_HOME_PAGE_ERROR = 100804;//获取主页错误
    public static final int GET_RECENT_PAGESIZE_ERROR = 100805;//pageSize不合法



    // Device module error code (101)
    public static final Integer DEVICE_NOT_REGISTED = 101001;
    public static final Integer DEVICE_ALREADY_THE_NEWEST = 101002;
    public static final Integer INVALID_DEVICE = 101003;
    public static final Integer INVALID_OTA_FILE_TYPE = 101004;
    public static final Integer DEVICE_UPDATE_NEEDS_TO_DELAY = 101005;
    public static final Integer INVALID_OTA_UPDATE_SYSTEM_VERSION = 101006;
    public static final Integer INVALID_OTA_UPDATE_REGION_OR_HARDWARE_CODE = 101007;
    public static final Integer UNPAIR_REGION_OR_HARDWARE_CODE = 101008;

    // Music Search error code (102)
    public static final Integer MUSIC_NOT_EXIST = 102001;
    public static final Integer MUSIC_PARAM_ERROR = 102002;

    // Ximalayas Search error code (103)
    public static final Integer HIMALAYA_ALBUM_NOT_EXIST = 103001;
    public static final Integer HIMALAYA_PARAM_ERROR = 103002;
    public static final Integer HIMALAYA_REMOTE_SERVER_ERROR = 103003;

    // preset api error code (104)
    public static final Integer INVALID_PERSET_VERSION = 104001;

}



