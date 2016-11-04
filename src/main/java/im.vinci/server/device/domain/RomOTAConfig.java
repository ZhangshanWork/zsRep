package im.vinci.server.device.domain;

import java.util.Date;
import java.util.Map;

/**
 * Created by henryhome on 9/14/15.
 */
public class RomOTAConfig {

    private Long id;
    private OTARegionCode regionCode;
    private OTAHardwareCode hardwareCode;
    private String desc;
    private String descEn;
    private String sysVersionName;
    private Integer status;
    private Date createTime;
    private Map<String, SystemVersion> versionPackage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getSysVersionName() {
        return sysVersionName;
    }

    public void setSysVersionName(String sysVersionName) {
        this.sysVersionName = sysVersionName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Map<String, SystemVersion> getVersionPackage() {
        return versionPackage;
    }

    public void setVersionPackage(Map<String, SystemVersion> versionPackage) {
        this.versionPackage = versionPackage;
    }

    public String getDescEn() {
        return descEn;
    }

    public void setDescEn(String descEn) {
        this.descEn = descEn;
    }

    public OTARegionCode getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(OTARegionCode regionCode) {
        this.regionCode = regionCode;
    }

    public OTAHardwareCode getHardwareCode() {
        return hardwareCode;
    }

    public void setHardwareCode(OTAHardwareCode hardwareCode) {
        this.hardwareCode = hardwareCode;
    }

    @Override
    public String toString() {
        return "RomOTAConfig{" +
                "id=" + id +
                ", regionCode=" + regionCode +
                ", hardwareCode=" + hardwareCode +
                ", desc='" + desc + '\'' +
                ", descEn='" + descEn + '\'' +
                ", sysVersionName='" + sysVersionName + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", versionPackage=" + versionPackage +
                '}';
    }

}
