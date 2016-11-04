package im.vinci.server.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import im.vinci.server.common.domain.enums.BindDeviceType;
import org.apache.commons.collections4.MapUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 用户基本信息
 * Created by tim@vinci on 16/7/19.
 */
public class UserInfo implements Serializable{

    @JsonProperty("uid")
    private long id;

    @JsonIgnore
    private String password;

    @JsonIgnore
    private String loginSource;

    @JsonIgnore
    private String externalSourceUid;

    private String nickName;

    @JsonIgnore
    private String nickNameCheck;

    private UserLocation location;

    private UserSettings userSettings;

    private UserCounts userCounts;

    //1:male 2:female
    private int sex;

    private String birthDate;

    private String headImg;

    private Map<String,UserBindDevice> bindDevices;

    //是否被当前用户关注,非数据库字段
    private boolean isAttention;

    private Date dtCreate;

    @JsonIgnore
    private Date dtUpdate;

    public long getId() {
        return id;
    }

    public UserInfo setId(long id) {
        this.id = id;
        return this;
    }

    public String getLoginSource() {
        return loginSource;
    }

    public UserInfo setLoginSource(String loginSource) {
        this.loginSource = loginSource;
        return this;
    }

    public String getExternalSourceUid() {
        return externalSourceUid;
    }

    public UserInfo setExternalSourceUid(String externalSourceUid) {
        this.externalSourceUid = externalSourceUid;
        return this;
    }

    public String getNickName() {
        return nickName;
    }

    public String getNickNameCheck() {
        return nickNameCheck;
    }

    public UserInfo setNickName(String nickName) {
        this.nickName = nickName;
        return this;
    }

    public UserInfo setNickNameCheck(String NickNameCheck) {
        this.nickNameCheck = NickNameCheck;
        return this;
    }

    public UserLocation getLocation() {
        return location;
    }

    public UserInfo setLocation(UserLocation location) {
        this.location = location;
        return this;
    }

    public int getSex() {
        return sex;
    }

    public UserInfo setSex(int sex) {
        this.sex = sex;
        return this;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public UserInfo setBirthDate(String birthDate) {
        this.birthDate = birthDate;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserInfo setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getHeadImg() {
        return headImg;
    }

    public UserInfo setHeadImg(String headImg) {
        this.headImg = headImg;
        return this;
    }

    public Date getDtCreate() {
        return dtCreate;
    }

    public UserInfo setDtCreate(Date dtCreate) {
        this.dtCreate = dtCreate;
        return this;
    }

    public Date getDtUpdate() {
        return dtUpdate;
    }

    public UserInfo setDtUpdate(Date dtUpdate) {
        this.dtUpdate = dtUpdate;
        return this;
    }

    public boolean isAttention() {
        return isAttention;
    }

    public UserInfo setAttention(boolean attention) {
        isAttention = attention;
        return this;
    }

    public Map<String, UserBindDevice> getBindDevices() {
        return bindDevices;
    }

    public UserInfo setBindDevices(Map<String, UserBindDevice> bindDevices) {
        this.bindDevices = bindDevices;
        return this;
    }

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public UserInfo setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
        return this;
    }

    public UserCounts getUserCounts() {
        return userCounts;
    }

    public UserInfo setUserCounts(UserCounts userCounts) {
        this.userCounts = userCounts;
        return this;
    }

    public final String getBindDeviceIdByType(BindDeviceType type) {
        if (type == null) {
            return null;
        }
        if (MapUtils.isEmpty(bindDevices)) {
            return null;
        }
        UserBindDevice userBindDevice = bindDevices.get(type.name());
        if (userBindDevice == null) {
            return null;
        }
        return userBindDevice.getDeviceId();
    }
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("password", password)
                .add("loginSource", loginSource)
                .add("externalSourceUid", externalSourceUid)
                .add("nickName", nickName)
                .add("nickNameCheck", nickNameCheck)
                .add("location", location)
                .add("userSettings", userSettings)
                .add("userCounts", userCounts)
                .add("sex", sex)
                .add("birthDate", birthDate)
                .add("headImg", headImg)
                .add("bindDevices", bindDevices)
                .add("isAttention", isAttention)
                .add("dtCreate", dtCreate)
                .add("dtUpdate", dtUpdate)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        return getId() == userInfo.getId() &&
                getSex() == userInfo.getSex() &&
                getBirthDate() == userInfo.getBirthDate() &&
                Objects.equal(getLoginSource(), userInfo.getLoginSource()) &&
                Objects.equal(getExternalSourceUid(), userInfo.getExternalSourceUid()) &&
                Objects.equal(getNickName(), userInfo.getNickName()) &&
                Objects.equal(getLocation(), userInfo.getLocation()) &&
                Objects.equal(getHeadImg(), userInfo.getHeadImg()) &&
                Objects.equal(getDtCreate(), userInfo.getDtCreate()) &&
                Objects.equal(getDtUpdate(), userInfo.getDtUpdate());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
