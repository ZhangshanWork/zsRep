package im.vinci.server.device.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * Created by tim@vinci on 15/11/16.
 * 用户log的类
 */
public class DeviceUserLog implements Serializable{
    public static class DeviceInfo implements Serializable{
        /**
         * 机器mac地址
         */
        private String mac;

        /**
         * 机器IMEI
         */
        private String imei;

        /**
         * rom版本号
         */
        private String rom_version;

        /**
         * sn号
         */
        private String sn;

        public String getMac() {
            return mac;
        }

        public void setMac(String mac) {
            this.mac = mac;
        }

        public String getImei() {
            return imei;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public String getRom_version() {
            return rom_version;
        }

        public void setRom_version(String rom_version) {
            this.rom_version = rom_version;
        }

        public String getSn() {
            return sn;
        }

        public void setSn(String sn) {
            this.sn = sn;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("mac", mac)
                    .add("imei", imei)
                    .add("rom_version", rom_version)
                    .add("sn", sn)
                    .toString();
        }
    }
    /**
     * 用户log的分类
     */
    private String name;

    private long createtime;

    private String eventid;
    /**
     * log来自于那种用户,headset:头机用户,WeiXin:微信用户
     */
    private String agent;
    /**
     * 机器信息
     */
    private DeviceInfo info;

    /**
     * 每种类型的自定义data
     */
    private JsonNode data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }

    public DeviceInfo getInfo() {
        return info;
    }

    public void setInfo(DeviceInfo info) {
        this.info = info;
    }

    public long getCreatetime() {
        return createtime;
    }

    public void setCreatetime(long createtime) {
        this.createtime = createtime;
    }

    public String getEventid() {
        return eventid;
    }

    public void setEventid(String eventid) {
        this.eventid = eventid;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("createtime", createtime)
                .add("eventid", eventid)
                .add("agent", agent)
                .add("info", info)
                .add("data", data)
                .toString();
    }

}
