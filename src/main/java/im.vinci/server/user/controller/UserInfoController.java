package im.vinci.server.user.controller;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import im.vinci.server.common.domain.enums.BindDeviceType;
import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.common.push.PushService;
import im.vinci.server.security.ApiSecurityLabel;
import im.vinci.server.user.domain.UserBindDevice;
import im.vinci.server.user.domain.UserInfo;
import im.vinci.server.user.domain.UserSettings;
import im.vinci.server.user.domain.wrappers.RegisterResponse;
import im.vinci.server.user.service.SmsService;
import im.vinci.server.user.service.UserAttentionService;
import im.vinci.server.user.service.UserCountService;
import im.vinci.server.user.service.UserLoginAndBindDeviceService;
import im.vinci.server.utils.JsonUtils;
import im.vinci.server.utils.UserContext;
import im.vinci.server.utils.apiresp.Result;
import im.vinci.server.utils.apiresp.ResultObject;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 用户注册和登录相关的接口
 * Created by tim@vinci on 16/7/19.
 */
@RestController
@RequestMapping(value = "/vinci/user", produces = "application/json;charset=UTF-8")
public class UserInfoController {


    @Autowired
    private SmsService smsService;

    @Autowired
    private UserLoginAndBindDeviceService userLoginAndBindDeviceService;

    @Autowired
    private UserCountService userCountService;

    @Autowired
    private UserAttentionService userAttentionService;

    @Autowired
    private PushService pushService;

    @RequestMapping(value = "/send_valid_code", method = RequestMethod.POST)
    @ApiSecurityLabel
    public Result sendValidCode(@RequestParam("phone_num") String phoneNum) {
        if (!smsService.sendValidCode(phoneNum)) {
            throw new VinciException(ErrorCode.API_LOGIN_VALIDCODE_SEND_FAILED, "发送验证码失败" + phoneNum, "发送验证码失败");
        }
        return new Result();
    }

    //注册用户
    @RequestMapping(value = "/register_phone_num", method = RequestMethod.POST)
    @ApiSecurityLabel
    public ResultObject<RegisterResponse> registerUser(@RequestParam("phone_num") String phoneNum,
                                                       @RequestParam("code") String validCode,
                                                       @RequestParam("password") String password,
                                                       @RequestParam("phone_model") String phoneModel,
                                                       @RequestHeader("imei") String imei,
                                                       @RequestHeader("mac") String mac,
                                                       @RequestHeader("sn") String sn) {
        smsService.validCode(phoneNum, validCode);
        UserInfo userInfo = userLoginAndBindDeviceService.registerUser(phoneNum, password,
                new UserBindDevice().setDeviceId(sn).setImei(imei).setMac(mac)
                        .setPhoneModel(JsonNodeFactory.instance.objectNode().put("name",phoneModel))
        );
        return new ResultObject<>(new RegisterResponse(userInfo, true));
    }

    //用第三方注册用户
    @RequestMapping(value = "/register_third_side", method = RequestMethod.POST)
    @ApiSecurityLabel
    public ResultObject<RegisterResponse> registerUser(@RequestParam("third_side_type") String type,
                                                       @RequestParam("third_side_user_id") String tuid,
                                                       @RequestParam("phone_model") String phoneModel,
                                                       @RequestHeader("imei") String imei,
                                                       @RequestHeader("mac") String mac,
                                                       @RequestHeader("sn") String sn) {
        Pair<UserInfo, Boolean> pair = userLoginAndBindDeviceService.registerUserThirdSide(type, tuid,
                new UserBindDevice().setDeviceId(sn).setImei(imei).setMac(mac)
                        .setPhoneModel(JsonNodeFactory.instance.objectNode().put("name",phoneModel))
        );
        return new ResultObject<>(new RegisterResponse(pair.getLeft(), pair.getRight()));
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ApiSecurityLabel
    public ResultObject<UserInfo> userLogin(@RequestParam("phone_num") String phoneNum,
                                            @RequestParam("password") String password,
                                            @RequestParam("phone_model") String phoneModel,
                                            @RequestHeader("imei") String imei,
                                            @RequestHeader("mac") String mac,
                                            @RequestHeader("sn") String sn) {
        return new ResultObject<>(userLoginAndBindDeviceService.loginWithPassword(phoneNum, password,
                new UserBindDevice().setDeviceId(sn).setImei(imei).setMac(mac)
                        .setPhoneModel(JsonNodeFactory.instance.objectNode().put("name",phoneModel))
        ));
    }

    @RequestMapping(value = "/info/get")
    @ApiSecurityLabel
    public ResultObject<UserInfo> getUserInfo(@RequestParam("uid") Long uid) {
        if (UserContext.getUserInfo() != null && UserContext.getUserInfo().getId() == uid) {
            return new ResultObject<>(UserContext.getUserInfo());
        }
        UserInfo userInfo = userLoginAndBindDeviceService.getUserInfo(uid, false);
        userInfo.getUserCounts().setMessageUnreadCount(-1);
        UserInfo me = UserContext.getUserInfo();
        if(me != null) {
            userAttentionService.checkIsAttention(me.getId(), Lists.newArrayList(userInfo));
        }
        return new ResultObject<>(userInfo);
    }

    /**
     * 查询头机是否绑定用户
     * @param sn
     * @return 未绑定则返回结果集中的UserInfo属性为空，反之为相应的UserInfo
     */
    @RequestMapping(value = "/headphone/owner", method = RequestMethod.POST)
    @ApiSecurityLabel
    public ResultObject<? super UserInfo> getUserBySn(@RequestHeader("sn") String sn) {
        UserInfo userInfo = userLoginAndBindDeviceService.getUserInfoBySn(sn);
        if(userInfo == null) return new ResultObject<>(new Object());
        return new ResultObject<>(userInfo);
    }

    @RequestMapping(value = "/info/update", method = RequestMethod.POST)
    @ApiSecurityLabel(isCheckLogin = true)
    public ResultObject<UserInfo> updateUserInfo(@RequestBody UserInfo userInfo) {
        return new ResultObject<>(userLoginAndBindDeviceService.updateUserInfo(UserContext.getUserInfo(), userInfo));
    }

    @RequestMapping(value = "/settings/update", method = RequestMethod.POST)
    @ApiSecurityLabel(isCheckLogin = true)
    public ResultObject<UserSettings> updateUserSettings(@RequestBody UserSettings userSettings) {
        return new ResultObject<>(userLoginAndBindDeviceService.updateUserSettings(UserContext.getUserInfo().getId(), userSettings));
    }

    //头像上传
    @RequestMapping(value = "/info/headImgUpload", method = RequestMethod.POST)
    @ApiSecurityLabel(isCheckLogin = false)
    public ResultObject<String> uploadHeadImg(@RequestParam("file") MultipartFile file) {
        //不能超过600k
        if (file.getSize() > 614400) {
            throw new VinciException(ErrorCode.HEAD_IMG_UPLOAD_REACH_MAX_BYTE, "文件超长:" + file.getSize(), "上传头像出错,图片大小不能超过600k");
        }
        try {
            String url = userLoginAndBindDeviceService.uploadHeadImg(UserContext.getUserInfo(), file.getInputStream(),
                    FilenameUtils.getExtension(file.getOriginalFilename()));
            return new ResultObject<>(url);
        } catch (IOException e) {
            throw new VinciException(ErrorCode.HEAD_IMG_UPLOAD_ERROR, "读取上传文件出错:" + e.getMessage(), "上传头像出错");
        }
    }

    //重置密码
    @RequestMapping(value = "/password/reset", method = RequestMethod.POST)
    @ApiSecurityLabel
    public Result resetPassword(@RequestParam("phone_num") String phoneNum,
                                @RequestParam("code") String validCode,
                                @RequestParam("password") String password) {
        smsService.validCode(phoneNum, validCode);
        userLoginAndBindDeviceService.updateNewPassword(phoneNum, -1, password);
        return new Result();
    }

    //更新密码
    @RequestMapping(value = "/password/update", method = RequestMethod.POST)
    @ApiSecurityLabel(isCheckLogin = true)
    public Result updatePassword(
            @RequestParam("uid") long uid,
            @RequestParam("new_password") String password) {
        userLoginAndBindDeviceService.updateNewPassword(null, uid, password);
        return new Result();
    }


    /**
     * 这是由手机发起的绑定头机的动作
     * @param sn 头机的sn号
     */
    @RequestMapping(value = "/headphone/bind", method = RequestMethod.POST)
    @ApiSecurityLabel(isCheckLogin = true)
    public ResultObject<UserInfo> bindHeadphone(@RequestParam("sn") String sn,
                                                @RequestParam("IMEI")String imei,
                                                @RequestParam("mac")String macAddr) {
        if (userLoginAndBindDeviceService.bindHeadphone(UserContext.getUserInfo(), sn, imei, macAddr)) {
            UserBindDevice mobile = UserContext.getUserInfo().getBindDevices().get(BindDeviceType.mobile.name());
            String mobileModel = "";
            if (mobile != null && mobile.getPhoneModel() != null &&
                    mobile.getPhoneModel().get("name") != null && mobile.getPhoneModel().get("name").isTextual()) {
                mobileModel = mobile.getPhoneModel().get("name").asText("");
            }
            pushService.pushMessageAsync(
                    UserContext.getUserInfo().getId(),
                    BindDeviceType.headphone, sn,
                    ImmutableMap.of("action", "headphone.bind"),
                    JsonUtils.encode(
                            ImmutableMap.builder()
                                    .put("uid",UserContext.getUserInfo().getId())
                                    .put("nick_name",UserContext.getUserInfo().getNickName())
                                    .put("head_img",UserContext.getUserInfo().getHeadImg())
                                    .put("mobile_model",mobileModel)
                                    .build()
                            )
            );
            return new ResultObject<>(userLoginAndBindDeviceService.getUserInfo(UserContext.getUserInfo().getId(), true));
        }
        throw new VinciException(ErrorCode.INTERNAL_SERVER_ERROR, "数据库操作失败", "操作失败,请稍后重试");
    }

    /**
     * 可以由头机发起,也可以由手机发起,解除一个账号绑定的头机
     * @param sn 头机的sn号
     */
    @RequestMapping(value = "/headphone/unbind", method = RequestMethod.POST)
    @ApiSecurityLabel(isCheckLogin = true)
    public ResultObject<UserInfo> unbindHeadphone(@RequestParam("sn") String sn,
                                                  @RequestHeader("sn") String sourceSn) {
        if (userLoginAndBindDeviceService.unbindHeadphone(UserContext.getUserInfo(), sn)) {
            if (Objects.equal(sn,sourceSn)) {
                String mobileSn = UserContext.getUserInfo().getBindDeviceIdByType(BindDeviceType.mobile);
                if (StringUtils.hasLength(mobileSn)) {
                    //两个sn号相等,是头机主动解绑,需要给手机发消息
                    pushService.pushMessageAsync(
                            UserContext.getUserInfo().getId(),
                            BindDeviceType.mobile, mobileSn,
                            ImmutableMap.of("action", "headphone.unbind"),
                            JsonUtils.encode(ImmutableMap.of("uid", UserContext.getUserInfo().getId()))
                    );
                }
            } else {
                pushService.pushMessageAsync(
                        UserContext.getUserInfo().getId(),
                        BindDeviceType.headphone, sn,
                        ImmutableMap.of("action", "headphone.unbind"),
                        JsonUtils.encode(ImmutableMap.of("uid", UserContext.getUserInfo().getId()))
                );
            }
            return new ResultObject<>(userLoginAndBindDeviceService.getUserInfo(UserContext.getUserInfo().getId(), true));
        }
        throw new VinciException(ErrorCode.INTERNAL_SERVER_ERROR, "数据库操作失败", "操作失败,请稍后重试");
    }
}
