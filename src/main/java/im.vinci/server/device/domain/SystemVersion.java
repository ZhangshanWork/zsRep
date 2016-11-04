package im.vinci.server.device.domain;

/**
 * Created by henryhome on 9/14/15.
 */
public class SystemVersion {

    private Long id;
    private String versionName;
    private Boolean isForced;
    private Boolean isFull;
    private String addr;
    private String hash;
    private String length;
    private Long configId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public Boolean getIsForced() {
        return isForced;
    }

    public void setIsForced(Boolean isForced) {
        this.isForced = isForced;
    }

    public Boolean getIsFull() {
        return isFull;
    }

    public void setIsFull(Boolean isFull) {
        this.isFull = isFull;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public String toString() {
        return "{\"id\":" + id + ",\"version_name\":\"" + versionName
                + "\",\"id_forced\":" + isForced
                + ",\"if_full:\":" + isFull
                + ",\"addr\":\"" + addr
                + "\",\"hash\":\"" + hash
                + "\",\"length\":" + length
                + ",\"config_id\":" + configId + "}";
    }
}
