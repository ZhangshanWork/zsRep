package im.vinci.server.user.service;

import com.aliyun.oss.OSSClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import im.vinci.server.common.domain.enums.BindDeviceType;
import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.user.domain.*;
import im.vinci.server.user.persistence.RealUserAndLoginMapper;
import im.vinci.server.user.persistence.UserBindDeviceMapper;
import im.vinci.server.user.persistence.UserSettingsMapper;
import im.vinci.server.utils.BizTemplate;
import im.vinci.server.utils.JsonUtils;
import im.vinci.server.utils.LocalIdGenerator;
import im.vinci.server.utils.StringContentUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.CharSet;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static com.hankcs.hanlp.HanLP.convertToSimplifiedChinese;
import static jdk.nashorn.internal.objects.NativeString.toLowerCase;
/**
 * 用户注册登录service
 * Created by tim@vinci on 16/7/20.
 */
@Service
public class UserLoginAndBindDeviceService {

    @Autowired
    private RealUserAndLoginMapper realUserAndLoginMapper;

    @Autowired
    private UserBindDeviceMapper userBindDeviceMapper;

    @Autowired
    private UserSettingsMapper userSettingsMapper;

    @Autowired
    private UserCountService userCountService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(8);

    private List<String> sensitive_words = new ArrayList<String>();

    @Autowired
    private Environment env;

    @Autowired
    @Qualifier("mobileUploadImgOssClient")
    private OSSClient client;

    private String uploadDir;

    private String bucketName;

    //password允许的字符
    private static CharSet passwordAllowChar = CharSet.getInstance("a-zA-Z","0-9","`~!@#$%&*()_=+{[}];:'\"<,>.?/");

    @PostConstruct
    private void init(){
        try{
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((new ClassPathResource("sensitive_words")).getInputStream(), "utf8"));
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null)
            {
                sensitive_words.add(lineTxt);
            }
            bufferedReader.close();
        }
        catch (Exception e)
        {
            throw new VinciException(ErrorCode.FILE_READ_ERROR, "文件读取出错", "文件读取出错");
        }
        bucketName = env.getProperty("mobile.upload_img.bucketName");
        uploadDir = env.getProperty("mobile.upload_img.dir");
    }

    /**
     * 注册一个用户
     *
     * @return 返回注册用户, right boolean型为是否新注册用户
     */
    @Transactional
    public UserInfo registerUser(final String phoneNum, final String password, final UserBindDevice userBindDevice) {
        return new BizTemplate<UserInfo>("registerUser") {

            @Override
            protected void checkParams() throws VinciException {
                if (StringUtils.isEmpty(phoneNum)) {
                    throw new VinciException(ErrorCode.API_USER_PHONENUM_NOT_CORRECT, "phonenum is empty", "手机号格式错误");
                }
                if (!phoneNum.startsWith("861") || phoneNum.length() != 13 || !NumberUtils.isDigits(phoneNum)) {
                    throw new VinciException(ErrorCode.API_USER_PHONENUM_NOT_CORRECT, "phonenum is empty", "手机号格式错误");
                }
                if (StringUtils.isEmpty(password)) {
                    throw new VinciException(ErrorCode.API_USER_PASSWORD_NOT_RULED, "password error:"+password, "请输入6-16位密码");
                }
                if (password.length()<6 || password.length()>16) {
                    throw new VinciException(ErrorCode.API_USER_PASSWORD_NOT_RULED, "password error:"+password, "请输入6-16位密码");
                }
                for (int i=0; i<password.length(); i++) {
                    if (!passwordAllowChar.contains(password.charAt(i))) {
                        throw new VinciException(ErrorCode.API_USER_PASSWORD_NOT_RULED, "password error:"+password, "密码中有无效字符");
                    }
                }
            }

            @Override
            protected UserInfo process() throws Exception {
                UserInfo userInfo = realUserAndLoginMapper.getUserInfoByExternalId(RegisterType.phone.name(), phoneNum);
                if (userInfo != null) {
                    throw new VinciException(ErrorCode.API_USER_HAS_EXISTS
                            , String.format("用户已存在:%s,%s", "phoneType", phoneNum), "用户已存在");
                }

                FastDateFormat f = FastDateFormat.getInstance("yyyy-MM-dd");
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.YEAR,-20);

                userInfo = new UserInfo();
                userInfo.setLoginSource(RegisterType.phone.name())
                        .setExternalSourceUid(phoneNum)
                        .setNickName(""+LocalIdGenerator.INSTANCE.generateId())
                        .setNickNameCheck(nomalize(userInfo.getNickName()))
                        .setSex(1)
                        .setHeadImg("")
                        .setLocation(null)
                        .setPassword(passwordEncoder.encode(password))
                        .setBirthDate(f.format(cal.getTime()));
                realUserAndLoginMapper.insertUserInfo(userInfo);
                userCountService.insertUserCount(userInfo.getId());
                userSettingsMapper.insertUserSettings(userInfo.getId());
                return bindMobile(userInfo,userBindDevice);
            }
        }.execute();
    }

    /**
     * 注册一个用户
     *
     * @return 返回注册用户, right boolean型为是否新注册用户
     */
    @Transactional
    public Pair<UserInfo, Boolean> registerUserThirdSide(final String type, final String exUid, final UserBindDevice userBindDevice) {
        return new BizTemplate<Pair<UserInfo, Boolean>>("registerUserThirdSide") {

            @Override
            protected void checkParams() throws VinciException {
                if (StringUtils.isEmpty(type) || RegisterType.of(type) == null) {
                    throw new VinciException(ErrorCode.API_USER_THIRD_SIDE_REGISTER_NOT_SUPPORT, "不支持的第三方登录:" + type, "暂时不支持此方式登录");
                }
                if (StringUtils.isEmpty(exUid)) {
                    throw new VinciException(ErrorCode.API_USER_THIRD_SIDE_REGISTER_EXUID_ERROR, "第三方uid为空", "登录失败,请重试");
                }
            }

            @Override
            protected Pair<UserInfo, Boolean> process() throws Exception {
                UserInfo userInfo = realUserAndLoginMapper.getUserInfoByExternalId(type, exUid);
                if (userInfo != null) {
                    return Pair.of(bindMobile(userInfo,userBindDevice), Boolean.FALSE);
                }

                FastDateFormat f = FastDateFormat.getInstance("yyyy-MM-dd");
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.YEAR,-20);

                userInfo = new UserInfo();
                userInfo.setLoginSource(type)
                        .setExternalSourceUid(exUid)
                        .setNickName(""+LocalIdGenerator.INSTANCE.generateId())
                        .setNickNameCheck(nomalize(userInfo.getNickName()))
                        .setSex(1)
                        .setHeadImg("")
                        .setLocation(null)
                        .setPassword("")
                        .setBirthDate(f.format(cal.getTime()));
                realUserAndLoginMapper.insertUserInfo(userInfo);
                userCountService.insertUserCount(userInfo.getId());
                userSettingsMapper.insertUserSettings(userInfo.getId());
                return Pair.of(bindMobile(userInfo,userBindDevice), Boolean.TRUE);
            }
        }.execute();
    }

    /**
     * 用户通过密码登录
     *
     * @return
     */
    @Transactional
    public UserInfo loginWithPassword(final String phoneNum, final String password, final UserBindDevice userBindDevice) {
        return new BizTemplate<UserInfo>("loginWithPassword") {

            @Override
            protected void checkParams() throws VinciException {
                if (StringUtils.isEmpty(phoneNum)) {
                    throw new VinciException(ErrorCode.API_USER_PHONENUM_NOT_CORRECT, "phonenum is empty", "请输入正确的手机号");
                }
                if (StringUtils.isEmpty(password)) {
                    throw new VinciException(ErrorCode.API_USER_PASSWORD_NOT_RULED, "phonenum is empty", "请输入正确的密码");
                }
            }

            @Override
            protected UserInfo process() throws Exception {
                UserInfo userInfo = realUserAndLoginMapper.getUserInfoByExternalId(RegisterType.phone.name(), phoneNum);
                if (userInfo == null) {
                    throw new VinciException(ErrorCode.API_USER_NOT_EXISTS
                            , String.format("登录用户不存在:%s,%s", "phoneType", phoneNum), "用户不存在");
                }
                if (!passwordEncoder.matches(password, userInfo.getPassword())) {
                    throw new VinciException(ErrorCode.API_USER_PASSWORD_NOT_CORRECT
                            , String.format("登录用户密码错误:%s,%s", "phoneType", phoneNum), "用户名密码错误");
                }
                return bindMobile(userInfo,userBindDevice);
            }
        }.execute();
    }

    /**
     * 更新个人信息
     */
    @Transactional
    public UserInfo updateUserInfo(final UserInfo oldUserInfo , final UserInfo newUserInfo) {
        return new BizTemplate<UserInfo>("updateUserInfo") {

            @Override
            protected void checkParams() throws VinciException {
                if (newUserInfo == null) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "没有传入userInfo", "参数有误请重试");
                }
                if (oldUserInfo == null) {
                    throw new VinciException(ErrorCode.NEED_LOGIN, "用户不存在", "用户不存在");
                }
            }

            @Override
            protected UserInfo process() throws Exception {
                if (!Objects.equal(newUserInfo.getNickName(),oldUserInfo.getNickName())) {
                    //检查nick name是否符合规则
                    if (StringUtils.isEmpty(newUserInfo.getNickName())) {
                        throw new VinciException(ErrorCode.USER_NICKNAME_NOTSTANDARD_ERROR, "用户昵称为空", "昵称不能为空");
                    }
                    newUserInfo.setNickNameCheck(nomalize(newUserInfo.getNickName()));
                    String regex = "^[a-z0-9_A-Z-\u4e00-\u9fa5]+$";
                    if (!newUserInfo.getNickName().matches(regex)) {
                        throw new VinciException(ErrorCode.USER_NICKNAME_NOTSTANDARD_ERROR, "用户昵称有特殊字符不符合规则", "昵称不符合规则");
                    }
                    int length = StringContentUtils.countRealLength(newUserInfo.getNickName());
                    if (length <4 || length > 12) {
                        throw new VinciException(ErrorCode.USER_NICKNAME_NOTSTANDARD_ERROR, "用户昵称有特殊字符不符合规则", "用户昵称长度不符合规范");
                    }
                    for (String tmp : sensitive_words) {
                        if (newUserInfo.getNickNameCheck().contains(tmp)) {
                            throw new VinciException(ErrorCode.USER_NICKNAME_NOTSTANDARD_ERROR, "用户昵称有敏感词汇", "昵称不能包含敏感词汇");
                        }
                    }
                    if ((!newUserInfo.getNickNameCheck().equals(oldUserInfo.getNickNameCheck())) && realUserAndLoginMapper.checkDuplicate(newUserInfo.getNickNameCheck()) > 0) {
                        throw new VinciException(ErrorCode.USER_NICKNAME_DUPLICATE_ERROR, "用户昵称nick_name_check重复", "昵称重复");
                    }
                }
                if (realUserAndLoginMapper.updateUserInfo(oldUserInfo, newUserInfo) <= 0) {
                    throw new VinciException(ErrorCode.UPDATE_USER_INFO_ERROR, "更新real_user_info数据库失败", "更新失败请重试");
                }
                return getUserInfo(oldUserInfo.getId(),true);
            }
        }.execute();
    }

    /**
     * 头像上传
     * @param fileExt 文件名后缀
     */
    @Transactional
    public String uploadHeadImg(final UserInfo userInfo, final InputStream body, final String fileExt){
        return new BizTemplate<String>("user.uploadHeadImg") {

            @Override
            protected void checkParams() throws VinciException {
                if (userInfo == null) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "没有传入userInfo", "参数有误请重试");
                }
                if (body == null) {
                    throw new VinciException(ErrorCode.HEAD_IMG_READ_ERROR, "头像读取出错", "参数有误请重试");
                }
            }

            @Override
            protected String process() throws Exception {
                int dir1 = (int)(userInfo.getId()%100);
                int dir2 = (int)((userInfo.getId()%10000)/100);
                String key = "head_img/"+dir1+"/"+dir2+"/"+userInfo.getId()+"/"+ LocalIdGenerator.INSTANCE.generateId()+"."+fileExt;
                String url = uploadDir+key+"@!";
                try {
                    client.putObject(bucketName, key, body);
                    realUserAndLoginMapper.uploadHeadImg(userInfo.getId(),url);
                } catch (VinciException e) {
                    throw new VinciException(ErrorCode.HEAD_IMG_UPLOAD_ERROR, "头像上传出错", "头像上传出错");
                }
                return url;
            }
        }.execute();

    }


    /**
     * 更新或重置密码
     */
    @Transactional
    public boolean updateNewPassword(final String phoneNum, final long uid, final String newPassword) {
        return new BizTemplate<Boolean>("updateNewPassword") {

            @Override
            protected void checkParams() throws VinciException {
                if (uid<=0 && StringUtils.isEmpty(phoneNum)) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "没有传入userInfo", "参数有误请重试");
                }
            }

            @Override
            protected Boolean process() throws Exception {
                UserInfo userInfo;
                if (uid>0) {
                    userInfo = checkUserInfo(uid);
                } else {
                    userInfo = realUserAndLoginMapper.getUserInfoByExternalId(RegisterType.phone.name(), phoneNum);
                }
                if (userInfo == null) {
                    throw new VinciException(ErrorCode.API_USER_NOT_EXISTS
                            , String.format("用户不存在:phone,%s or uid:%s", phoneNum, uid), "用户不存在");
                }
                for (int i=0; i<newPassword.length(); i++) {
                    if (!passwordAllowChar.contains(newPassword.charAt(i))) {
                        throw new VinciException(ErrorCode.API_USER_PASSWORD_NOT_RULED, "password error:"+newPassword, "密码中有无效字符");
                    }
                }
                if (realUserAndLoginMapper.updateUserPassword(userInfo.getId(), passwordEncoder.encode(newPassword)) <= 0) {
                    throw new VinciException(ErrorCode.UPDATE_USER_INFO_ERROR, "更新real_user_info数据库失败", "更新失败请重试");
                }
                return true;
            }
        }.execute();
    }

    /**
     * 返回userInfo,只有自己的id才可以调用
     */
    public UserInfo getUserInfo(final long id, final boolean isFull) {
        return new BizTemplate<UserInfo>("getUserInfo") {

            @Override
            protected void checkParams() throws VinciException {
                if (id <= 0) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "没有传入userInfo", "参数有误请重试");
                }
            }

            @Override
            protected UserInfo process() throws Exception {
                // UserInfo中的attention属性代表被查询用户是否可以被关注，为true表示不可以，为false表示可以
                UserInfo userInfo = realUserAndLoginMapper.getUserInfoById(id);
                if (userInfo == null) {
                    throw new VinciException(ErrorCode.API_USER_NOT_EXISTS
                            , String.format("用户不存在:uid:%s", id), "用户不存在");
                }
                userInfo = userInfo.setUserCounts(userCountService.getUserCount(id)).setAttention(false);
                userInfo = combine(userInfo,userBindDeviceMapper.getUserBindDeviceByUserInfoId(userInfo.getId()));
                if (isFull) {
                    userInfo.setUserSettings(getUserSettings(id));
                    userInfo.setAttention(true);
                } else {
                    if (userInfo.getUserCounts() == null) {
                        userInfo.setUserCounts(new UserCounts());
                    } else {
                        userInfo.getUserCounts().setMessageUnreadCount(null);
                    }
                    for (UserBindDevice device : userInfo.getBindDevices().values()) {
                        //去掉隐私信息
                        device.setDeviceId("").setImei("").setMac("");
                    }
                }
                return userInfo;
            }
        }.execute();
    }

    /**
     * 返回对应设备Id所绑定的用户, 这个一定是用户自己调用,需要返回所有的东西
     * @param sn 头机设备号
     * @return 如果无绑定用户则返回null，否则返回相应用户userInfo
     */
    public UserInfo getUserInfoBySn(final String sn) {
        return new BizTemplate<UserInfo>("getUserInfoBySn") {

            @Override
            protected void checkParams() throws VinciException {
                if (StringUtils.isEmpty(sn)) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "传入的sn为空", "没有传入头机的设备号");
                }
            }

            @Override
            protected UserInfo process() throws Exception {
                UserBindDevice userBindDevice = userBindDeviceMapper.getUserBindDeviceByDeviceId(sn);
                if (userBindDevice == null) {
                    return null;
                }
                return getUserInfo(userBindDevice.getRealUserId(), true);
            }
        }.execute();

    }
    /**
     * 只检查是否存在,不抛出错误
     * @param id
     * @return
     */
    public UserInfo checkUserInfo(final long id) {
        return new BizTemplate<UserInfo>("checkUserInfo") {

            @Override
            protected void checkParams() throws VinciException {

            }

            @Override
            protected UserInfo process() throws Exception {
                return realUserAndLoginMapper.getUserInfoById(id);
            }
        }.execute();
    }

    private UserSettings getUserSettings(final long userId) {
        return new BizTemplate<UserSettings>("getUserSettings") {

            @Override
            protected void checkParams() throws VinciException {
                if (userId <= 0) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "没有传入userInfo", "参数有误请重试");
                }
            }

            @Override
            protected UserSettings process() throws Exception {
                UserSettings userSettings = userSettingsMapper.getUserSettings(userId);
                if (userSettings == null) {
                    throw new VinciException(ErrorCode.API_USER_NOT_EXISTS
                            , String.format("用户不存在:uid:%s", userId), "用户不存在");
                }
                return userSettings;
            }
        }.execute();
    }


    @Transactional
    public UserSettings updateUserSettings(long userId , final UserSettings userSettings) {
        return new BizTemplate<UserSettings>("user.updateUserSettings") {

            @Override
            protected void checkParams() throws VinciException {
                if (userSettings == null) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR,"没有传入UserSettings","没有传入用户参数");
                }
                JsonNode nodes = JsonUtils.decode(JsonUtils.encode(userSettings),JsonNode.class);
                if (nodes == null || nodes.size() == 0) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR,"没有传入UserSettings","没有传入用户参数");
                }
            }

            @Override
            protected UserSettings process() throws Exception {
                userSettings.setRealUserId(userId);
                userSettingsMapper.updateUserSettings(userSettings);
                return userSettingsMapper.getUserSettings(userId);
            }
        }.execute();
    }
    /**
     * 返回userInfoMap包括其中的UserCount, 但是这个不返回用户的消息箱数量
     *
     */
    public Map<Long,UserInfo> getUserInfoMap(final Collection<Long> uidList) {
        return new BizTemplate<Map<Long,UserInfo>>("getUserInfoListMap") {

            @Override
            protected void checkParams() throws VinciException {

            }

            @Override
            protected Map<Long,UserInfo> process() throws Exception {

                if(CollectionUtils.isEmpty(uidList)) {
                    return Collections.emptyMap();
                }

                Map<Long,UserInfo> userInfoList = realUserAndLoginMapper.getUserInfoListById(uidList);
                if (userInfoList == null || userInfoList.size() == 0) {
                    return Collections.emptyMap();
                }
                Map<Long,UserCounts> userCountsMap = userCountService.getUserCountsMap(uidList);
                for (UserInfo userInfo : userInfoList.values()) {
                    UserCounts userCounts = userCountsMap.get(userInfo.getId());
                    if (userCounts != null) {
                        userCounts.setMessageUnreadCount(-1);
                    }
                    userInfo.setUserCounts(userCounts==null?new UserCounts():userCounts);
                }
                return userInfoList;
            }
        }.execute();
    }

    private UserInfo combine(UserInfo userInfo, List<UserBindDevice> userBindDevices) {
        ImmutableMap.Builder<String, UserBindDevice> builder = ImmutableMap.<String, UserBindDevice>builder();
        if (!CollectionUtils.isEmpty(userBindDevices)) {
            userBindDevices.stream().filter(userBindDevice -> userBindDevice != null)
                    .forEach(userBindDevice -> builder.put(userBindDevice.getDeviceType(), userBindDevice));
        }
        userInfo.setBindDevices(builder.build());
        return userInfo;
    }

    @Transactional
    /**
     * sn 这里绑定的是头机的sn号
     */
    public boolean bindHeadphone(final UserInfo userInfo , final String sn, final String imei, final String macAddr) {
        return new BizTemplate<Boolean>("bindHeadphone") {

            @Override
            protected void checkParams() throws VinciException {
                if (StringUtils.isEmpty(sn)) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "传入的sn为空", "没有传入头机的设备号");
                }
                if (userInfo == null) {
                    throw new VinciException(ErrorCode.NEED_LOGIN, "用户不存在", "用户不存在");
                }
            }

            @Override
            protected Boolean process() throws Exception {
                if (CollectionUtils.size(userInfo.getBindDevices()) == 2) {
                    throw new VinciException(ErrorCode.API_BIND_DEVICE_USER_HAS_DEVICE, "已绑定过设备了", "已绑定其他设备,需要先解绑");
                }
                UserBindDevice userBindDevice = userBindDeviceMapper.getUserBindDeviceByDeviceId(sn);
                if (userBindDevice != null) {
                    throw new VinciException(ErrorCode.API_BIND_DEVICE_HAS_BINDED, "设备已被其他用户绑定", "设备已被其他用户绑定");
                }
                UserBindDevice device = new UserBindDevice().setRealUserId(userInfo.getId())
                        .setDeviceId(sn).setPhoneModel(getHeadphoneModle(sn))
                        .setDeviceType(BindDeviceType.headphone.name())
                        .setImei(StringContentUtils.trimToEmpty(imei)).setMac(StringContentUtils.trimToEmpty(macAddr));
                return userBindDeviceMapper.insertUserDeviceBind(device) > 0;
            }
        }.execute();
    }




    @Transactional
    public boolean unbindHeadphone(final UserInfo userInfo, final String sn) {
        return new BizTemplate<Boolean>("unbindHeadphone") {
            @Override
            protected void checkParams() throws VinciException {
                if (StringUtils.isEmpty(sn)) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "传入的sn为空", "没有传入头机的设备号");
                }
                if (userInfo == null) {
                    throw new VinciException(ErrorCode.NEED_LOGIN, "用户不存在", "用户不存在");
                }
            }

            @Override
            protected Boolean process() throws Exception {
                UserBindDevice userBindDevice = userInfo.getBindDevices().get(BindDeviceType.headphone.name());

                if (userBindDevice == null) {
                    throw new VinciException(ErrorCode.API_BIND_DEVICE_NOT_BIND, "设备已解绑", "设备当前没有绑定");
                }
                if (!Objects.equal(userBindDevice.getDeviceId(), sn)) {
                    throw new VinciException(ErrorCode.API_BIND_DEVICE_UNBIND_DEVICE_UNMATCH, "要解绑的设备号不一致", "要解绑的设备号不一致");
                }
                return userBindDeviceMapper.deleteUserDeviceBind(userBindDevice) > 0;
            }
        }.execute();
    }

    /**
     * 登录注册的时候,会把登录的手机deviceId直接当做绑定手机
     */
    @Transactional
    private UserInfo bindMobile(final UserInfo userInfo, final UserBindDevice userBindDevice) {
        if (userBindDevice == null || StringUtils.isEmpty(userBindDevice.getDeviceId())) {
            throw new VinciException(ErrorCode.ARGUMENT_ERROR,"deviceID不符合规范:"+userBindDevice,"设备号不符合规范");
        }
        List<UserBindDevice> list = userBindDeviceMapper.getUserBindDeviceByUserInfoId(userInfo.getId());
        combine(userInfo,list);
        if (userInfo.getBindDevices().get(BindDeviceType.mobile.name()) != null
                && Objects.equal(userInfo.getBindDevices().get(BindDeviceType.mobile.name()).getDeviceId(),userBindDevice.getDeviceId())) {
            return userInfo;
        }
        UserBindDevice device = userInfo.getBindDevices().get(BindDeviceType.mobile.name());
        userBindDevice.setDeviceType(BindDeviceType.mobile.name()).setRealUserId(userInfo.getId());
        int result = 0;
        if (device != null) {
            result = userBindDeviceMapper.updateUserDeviceBind(userBindDevice);
        } else {
            result = userBindDeviceMapper.insertUserDeviceBind(userBindDevice);
        }
        if (result <= 0) {
            throw new VinciException(ErrorCode.INTERNAL_SERVER_ERROR,"更新绑定手机设备不成功","内部错误,请稍后再试");
        }
        return getUserInfo(userInfo.getId(),true);
    }

    private String nomalize(String nickname){
        return convertToSimplifiedChinese(toLowerCase(toSemiangle(nickname))).replaceAll("[^a-z^A-Z^0-9\\u4e00-\\u9fa5]", "");
    }
    private String toSemiangle(String src) {
        char[] c = src.toCharArray();
        for (int index = 0; index < c.length; index++) {
            if (c[index] == 12288) {// 全角空格
                c[index] = (char) 32;
            } else if (c[index] > 65280 && c[index] < 65375) {// 其他全角字符
                c[index] = (char) (c[index] - 65248);
            }
        }
        return String.valueOf(c);
    }
    private String big5ToChinese( String s )
    {
        try{
            if ( s == null || s.equals( "" ) )
                return("");
            String newstring = null;
            newstring = new String( s.getBytes( "big5" ), "utf-8" );
            return(newstring);
        }
        catch ( UnsupportedEncodingException e )
        {
            return(s);
        }
    }

    /**
     * 获取头机的型号和颜色
     */
    private JsonNode getHeadphoneModle(String sn) {
        char c = 'B';
        if (!StringUtils.isEmpty(sn) && 14 <= sn.length()) {
            c = sn.charAt(9);
        }
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        switch (c) {
            case 'R':
                result.put("name","Vinci一代红色").put("pic","http://images.getvinci.com/headphone_prd/v1/{size}/red.png@!po");
                break;
            case 'F':
                result.put("name","Vinci一代粉色").put("pic","http://images.getvinci.com/headphone_prd/v1/{size}/pink.png@!po");
                break;
            case 'W':
                result.put("name","Vinci一代白色").put("pic","http://images.getvinci.com/headphone_prd/v1/{size}/white.png@!po");
                break;
            case 'G':
                result.put("name","Vinci一代绿色").put("pic","http://images.getvinci.com/headphone_prd/v1/{size}/green.png@!po");
                break;
            case 'P':
                result.put("name","Vinci一代紫色").put("pic","http://images.getvinci.com/headphone_prd/v1/{size}/purple.png@!po");
                break;
            case 'Y':
                result.put("name","Vinci一代黄色").put("pic","http://images.getvinci.com/headphone_prd/v1/{size}/yellow.png@!po");
                break;
            case 'B':
                result.put("name","Vinci一代黑色").put("pic","http://images.getvinci.com/headphone_prd/v1/{size}/black.png@!po");
                break;
            default:
                result.put("name","Vinci一代黑色").put("pic","http://images.getvinci.com/headphone_prd/v1/{size}/black.png@!po");
                break;
        }
        return result;
    }
}
