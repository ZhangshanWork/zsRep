package im.vinci.server.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.io.Serializable;
import java.util.Date;

/**
 * 记录了用户都在哪些设备上登录了，用于多终端设备同步
 * Created by tim@vinci on 16/6/20.
 *
 CREATE TABLE `user_bind_device` (
 `id` bigint(20) NOT NULL AUTO_INCREMENT,
 `real_user_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'foreign key ref to real_user(`id`)',
 `device_id` varchar(80) NOT NULL DEFAULT '' COMMENT '设备id',
 `device_type` int(11) NOT NULL DEFAULT '1' COMMENT '1:mobile 2:headset',
 `dt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
 `dt_update` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (`id`),
 UNIQUE KEY `uniq_user_id_device_type` (`real_user_id`,`device_type`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
 */
public class UserBindDevice implements Serializable {
    //primary key
    @JsonIgnore
    private long id;
    //foreign key ref to real_user(`id`)
    @JsonIgnore
    private long realUserId;
    //设备id,只要能标识机器唯一码就可以,一般为sn号
    private String deviceId;
    //1:mobile 2:headphone
    private String deviceType;
    //设备imei号,mobile尤其是iphone可能为随机码
    private String imei;
    //设备mac地址
    private String mac;
    //手机为型号,头机为几代什么颜色
    private JsonNode phoneModel;
    //记录创建时间
    private Date dtCreate;
    //记录更改时间
    @JsonIgnore
    private Date dtUpdate;

    public long getId() {
        return id;
    }

    public UserBindDevice setId(long id) {
        this.id = id;
        return this;
    }

    public long getRealUserId() {
        return realUserId;
    }

    public UserBindDevice setRealUserId(long realUserId) {
        this.realUserId = realUserId;
        return this;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public UserBindDevice setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public UserBindDevice setDeviceType(String deviceType) {
        this.deviceType = deviceType;
        return this;
    }

    public Date getDtCreate() {
        return dtCreate;
    }

    public UserBindDevice setDtCreate(Date dtCreate) {
        this.dtCreate = dtCreate;
        return this;
    }

    public Date getDtUpdate() {
        return dtUpdate;
    }

    public UserBindDevice setDtUpdate(Date dtUpdate) {
        this.dtUpdate = dtUpdate;
        return this;
    }

    public String getImei() {
        return imei;
    }

    public UserBindDevice setImei(String imei) {
        this.imei = imei;
        return this;
    }

    public String getMac() {
        return mac;
    }

    public UserBindDevice setMac(String mac) {
        this.mac = mac;
        return this;
    }

    public JsonNode getPhoneModel() {
        return phoneModel;
    }

    public UserBindDevice setPhoneModel(JsonNode phoneModel) {
        this.phoneModel = phoneModel;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserBindDevice device = (UserBindDevice) o;
        return realUserId == device.realUserId &&
                Objects.equal(deviceId, device.deviceId) &&
                Objects.equal(deviceType, device.deviceType) &&
                Objects.equal(imei, device.imei) &&
                Objects.equal(mac, device.mac) &&
                Objects.equal(phoneModel, device.phoneModel);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(realUserId, deviceId, deviceType, imei, mac, phoneModel);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("realUserId", realUserId)
                .add("deviceId", deviceId)
                .add("deviceType", deviceType)
                .add("imei", imei)
                .add("mac", mac)
                .add("phoneModel", phoneModel)
                .add("dtCreate", dtCreate)
                .add("dtUpdate", dtUpdate)
                .toString();
    }
}
