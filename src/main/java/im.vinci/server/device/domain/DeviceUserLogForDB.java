//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package im.vinci.server.device.domain;

import com.aliyun.openservices.ons.api.Message;

import java.io.UnsupportedEncodingException;

public class DeviceUserLogForDB {
    private Long id;
    private String name;
    private long create_timestamp;
    private String uuid;
    private String json_data;

    public DeviceUserLogForDB(DeviceUserLog log, String uuid, Message logMessage) throws UnsupportedEncodingException {
        this.name = log.getName();
        this.create_timestamp = log.getCreatetime();
        this.uuid = uuid;
        this.json_data = new String(logMessage.getBody(), "UTF-8");
    }

    public DeviceUserLogForDB(DeviceUserLog log, String uuid, String jsonStr) {
        this.name = log.getName();
        this.create_timestamp = log.getCreatetime();
        this.uuid = uuid;
        this.json_data = jsonStr;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreate_timestamp() {
        return this.create_timestamp;
    }

    public void setCreate_timestamp(long create_timestamp) {
        this.create_timestamp = create_timestamp;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getJson_data() {
        return this.json_data;
    }

    public void setJson_data(String json_data) {
        this.json_data = json_data;
    }
}
