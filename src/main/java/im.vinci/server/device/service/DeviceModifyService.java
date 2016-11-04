package im.vinci.server.device.service;

import im.vinci.server.common.exceptions.ParameterMissingException;
import im.vinci.server.common.exceptions.device.InvalidDeviceException;
import im.vinci.server.common.exceptions.device.InvalidDeviceUpdateException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.config.RomOTAConfigConstants;
import im.vinci.server.device.domain.*;
import im.vinci.server.device.domain.wrappers.requests.CrashLogUploading;
import im.vinci.server.device.domain.wrappers.requests.OTAInfo;
import im.vinci.server.device.domain.wrappers.responses.DeviceUpdateInfo;
import im.vinci.server.device.persistence.DeviceMapper;
import im.vinci.server.device.persistence.OTATestMacMapper;
import im.vinci.server.device.persistence.RomOTAConfigMapper;
import im.vinci.server.device.persistence.SystemVersionMapper;
import im.vinci.server.utils.VinciUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by henryhome on 9/14/15.
 */
@Service
public class DeviceModifyService  {

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private OTATestMacMapper otaTestMacMapper;

    @Autowired
    private RomOTAConfigMapper romOTAConfigMapper;

    @Autowired
    private SystemVersionMapper systemVersionMapper;

    @Autowired
    private Environment env;

    private Logger logger = LoggerFactory.getLogger(DeviceModifyService.class);

    // 去除apk升级
    // 分为通过Mac判断读取配置,如果在测试机范围内则读取测试配置否则读取升级配置
    public DeviceUpdateInfo updateViaOTA(OTAInfo otaInfo) throws Exception {

        String imei = otaInfo.getImei();
        String mac = otaInfo.getMac();

        // 机器的版本，我们这一版是1.0，后面还有1.0plus和2.0，这个是用于区分不同机器型号的
        String deviceVersion = otaInfo.getDeviceVersion();
        // rom版本
        String sysVersionName = otaInfo.getSysVersionName();
        // 以上两个唯一确定一个rom版本

        if (!checkDevice(imei, mac)) {
            throw new InvalidDeviceException(ErrorCode.DEVICE_NOT_REGISTED,
                "The device with imei = '" + imei + "' and mac = '" + mac + "' is not registered",
                "此设备未注册");
        }

        // 必须有这两个参数
        if (deviceVersion == null || sysVersionName == null) {
            throw new ParameterMissingException();
        }
        OTARegionCode regionCode = OTARegionCode.CN;
        OTAHardwareCode hardwareCode = OTAHardwareCode._01;
        try {
            if (sysVersionName.indexOf('-') > 0) {
                String identity = sysVersionName.substring(sysVersionName.indexOf('-') + 1);
                sysVersionName = sysVersionName.substring(0,sysVersionName.indexOf('-'));
                regionCode = OTARegionCode.valueOf(identity.substring(0,2));
                hardwareCode = OTAHardwareCode.valueOf(identity.substring(2));
            }
        }catch (Exception e) {
            throw new InvalidDeviceUpdateException(ErrorCode.INVALID_OTA_UPDATE_SYSTEM_VERSION,
                    "The device with imei = '" + imei + "' and mac = '" + mac + "' has error systemVersionName:"+sysVersionName,
                    "系统版本号不符合规则");
        }


        // 如果设备是我们的定义测试设备,则使用debug的预备上线配置
        RomOTAConfig romOTAConfig = null;

        if (isDebugDevice(imei)) {
            romOTAConfig = romOTAConfigMapper.getRomOTAConfigByStatus(regionCode,hardwareCode, RomOTAConfigConstants.OTA_TEST_CONFIG);
            if (romOTAConfig == null) {
                throw new InvalidDeviceUpdateException(ErrorCode.DEVICE_ALREADY_THE_NEWEST,
                        "The device "+otaInfo+ " is already using the newest system",
                        "系统已经是最新版本");
            }
            Map<String, SystemVersion> systemVersionMap = getVersionPackagesMap(romOTAConfig);
            romOTAConfig.setVersionPackage(systemVersionMap);
            logger.info("\n*******************************************\nThe rom ota config is " + romOTAConfig+"\n*******************************************");

        } else {
            romOTAConfig = romOTAConfigMapper.getRomOTAConfigByStatus(regionCode,hardwareCode,RomOTAConfigConstants.OTA_DEPLOY_CONFIG);
            if (romOTAConfig == null) {
                throw new InvalidDeviceUpdateException(ErrorCode.DEVICE_ALREADY_THE_NEWEST,
                        "The device "+otaInfo+ " is already using the newest system",
                        "系统已经是最新版本");
            }
            Map<String, SystemVersion> systemVersionMap = getVersionPackagesMap(romOTAConfig);
            romOTAConfig.setVersionPackage(systemVersionMap);
        }

        DeviceUpdateInfo deviceUpdateInfo = getLatestSysUpdateInfo(romOTAConfig, sysVersionName);

        if (deviceUpdateInfo != null) {
            return deviceUpdateInfo;
        } else {
            throw new InvalidDeviceUpdateException(ErrorCode.DEVICE_ALREADY_THE_NEWEST,
                    "The device "+otaInfo+ " is already using the newest system",
                "系统已经是最新版本");
        }
    }

    public void uploadCrashLog(CrashLogUploading crashLogUploading) throws Exception {

        String imei = crashLogUploading.getImei();
        String mac = crashLogUploading.getMac();
        String appName = crashLogUploading.getAppName();
        String appVersion = crashLogUploading.getAppVersion();
        String crashType = crashLogUploading.getCrashType();
        Integer crashCount = crashLogUploading.getCrashCount() == null ? 1 : crashLogUploading.getCrashCount();

        String log = crashLogUploading.getLog();

        if (!checkDevice(imei, mac)) {
            throw new InvalidDeviceException(ErrorCode.DEVICE_NOT_REGISTED,
                "The device with imei = '" + imei + "' and mac = '" + mac + "' is not registed",
                "此设备未注册");
        }

        // 必须有此参数
        if (log == null) {
            throw new ParameterMissingException();
        }

        CrashLog crashLog = new CrashLog();
        crashLog.setImei(imei);
        crashLog.setMac(mac);
        crashLog.setAppName(appName);
        crashLog.setAppVersion(appVersion);
        crashLog.setCrashType(crashType);
        crashLog.setCrashCount(crashCount);

        String encodedLog = VinciUtils.byte2hex(VinciUtils.encryptMD5(log.getBytes()));
        crashLog.setLogSign(encodedLog);

        crashLog.setLog(log);

        deviceMapper.addCrashLog(crashLog);
    }

    public Integer addDevice(Device device) {

        String mac = device.getMac();
        String imei = device.getImei();

        if (mac == null && imei == null) {
            throw new InvalidDeviceException(ErrorCode.INVALID_DEVICE, "The mac and imei is both empty", "mac和imei不能都为空");
        }

        deviceMapper.addDevice(device);

        return device.getId();
    }

    public void updateDevice(Device device) throws Exception {

        deviceMapper.updateDevice(device);
    }

    public void deleteDevice(Integer deviceId) throws Exception {

        deviceMapper.deleteDevice(deviceId);
    }

    /********************************* private methods ***********************************/

    private Boolean checkDevice(String imei, String mac) {
        if (imei == null && mac == null) {
            return false;
        }

        return true;
    }

    private Boolean isDebugDevice(String imei) {
        Long isExit = otaTestMacMapper.getOtaTestMacByMac(imei);
        return (isExit != null ? true : false);
    }


    private Boolean isFirstUpdate(String imei, String mac) {

        Device device = deviceMapper.getDeviceByImei(imei);

        if (device != null) {
            return false;
        }

        device = deviceMapper.getDeviceByMac(mac);

        if (device != null) {
            return false;
        }

        return true;
    }

    private DeviceUpdateInfo getLatestSysUpdateInfo(RomOTAConfig romOTAConfig, String sysVersionName) {

        DeviceUpdateInfo deviceUpdateInfo = null;

        logger.info("The system version name is " + romOTAConfig.getSysVersionName());
        logger.info("The sysVersionName is " + sysVersionName);

        if (sysVersionName != romOTAConfig.getSysVersionName() && romOTAConfig.getVersionPackage() != null
                && romOTAConfig.getVersionPackage().containsKey(sysVersionName)) {
            deviceUpdateInfo = new DeviceUpdateInfo();
            deviceUpdateInfo.setType("sys");
            deviceUpdateInfo.setDesc(romOTAConfig.getDesc());
            deviceUpdateInfo.setDescEn(romOTAConfig.getDescEn());
            SystemVersion version = romOTAConfig.getVersionPackage().get(sysVersionName);
            deviceUpdateInfo.setIsForced(version.getIsForced());
            deviceUpdateInfo.setIsFull(version.getIsFull());
            deviceUpdateInfo.setSysVersionName(romOTAConfig.getSysVersionName());
            deviceUpdateInfo.setAddr(version.getAddr());
            deviceUpdateInfo.setHash(version.getHash());
            deviceUpdateInfo.setLength(version.getLength());
        }

        return deviceUpdateInfo;
    }

    private DeviceUpdateInfo getLatestAppUpdateInfo(ApkOTAConfig apkOTAConfig, Integer appVersionCode) {

        DeviceUpdateInfo deviceUpdateInfo = null;

        if (appVersionCode != apkOTAConfig.getAppVersionCode()) {
            deviceUpdateInfo = new DeviceUpdateInfo();
            deviceUpdateInfo.setType("app");
            deviceUpdateInfo.setDesc(apkOTAConfig.getDesc());

            if (apkOTAConfig.getLastAppVersionCodeToUpdate() >= appVersionCode) {
                deviceUpdateInfo.setIsForced(true);
            } else {
                deviceUpdateInfo.setIsForced(false);
            }

            deviceUpdateInfo.setAppVersionCode(apkOTAConfig.getAppVersionCode());
            deviceUpdateInfo.setAppVersionName(apkOTAConfig.getAppVersionName());
            deviceUpdateInfo.setAddr(apkOTAConfig.getAddr());
            deviceUpdateInfo.setHash(apkOTAConfig.getHash());
            deviceUpdateInfo.setLength(apkOTAConfig.getLength());
            deviceUpdateInfo.setLastAppVersionCodeToUpdate(apkOTAConfig.getLastAppVersionCodeToUpdate());
        }

        return deviceUpdateInfo;
    }

    private Map<String, SystemVersion> getVersionPackagesMap(RomOTAConfig romOTAConfig) {
        Map<String, SystemVersion> versionPackages = new HashMap<String, SystemVersion>();
        if (romOTAConfig != null && romOTAConfig.getId() != null) {
            List<SystemVersion> systemVersions = systemVersionMapper.getVersionPackagesByConfigId(romOTAConfig.getId());
            for(SystemVersion systemVersion : systemVersions) {
                versionPackages.put(systemVersion.getVersionName(), systemVersion);
            }
        }
        return versionPackages;
    }
}



